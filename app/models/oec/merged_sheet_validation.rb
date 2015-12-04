module Oec
  module MergedSheetValidation

    include Validator

    def build_and_validate_export_sheets
      course_confirmations_file = @remote_drive.find_nested [@term_code, Oec::Folder.merged_confirmations, 'Merged course confirmations'], on_failure: :error
      course_confirmations = Oec::SisImportSheet.from_csv(@remote_drive.export_csv(course_confirmations_file), dept_code: nil)

      supervisor_confirmations_file = @remote_drive.find_nested [@term_code, Oec::Folder.merged_confirmations, 'Merged supervisor confirmations'], on_failure: :error
      supervisor_confirmations = Oec::Supervisors.from_csv @remote_drive.export_csv(supervisor_confirmations_file)

      instructors = Oec::Instructors.new
      course_instructors = Oec::CourseInstructors.new
      courses = Oec::Courses.new
      students = Oec::Students.new
      course_students = Oec::CourseStudents.new
      supervisors = Oec::Supervisors.new

      if (previous_course_supervisors = @remote_drive.find_nested [@term_code, Oec::Folder.overrides, 'course_supervisors'])
        course_supervisors = Oec::CourseSupervisors.from_csv @remote_drive.export_csv(previous_course_supervisors)
      else
        course_supervisors = Oec::CourseSupervisors.new
      end

      ccns = Set.new
      suffixed_ccns = {}

      default_dates = default_term_dates
      participating_dept_names = Oec::CourseCode.participating_dept_names

      supervisor_confirmations.each do |confirmation|
        validate_and_add(supervisors, confirmation, %w(LDAP_UID))
      end

      course_confirmations.each do |confirmation|
        next unless confirmation['EVALUATE'] && confirmation['EVALUATE'].casecmp('Y') == 0

        validate('courses', confirmation['COURSE_ID']) do |errors|
          errors.add('Blank instructor LDAP_UID') && next if confirmation['LDAP_UID'].blank?
          errors.add("Incorrect term code in COURSE_ID #{confirmation['COURSE_ID']}") && next unless confirmation['COURSE_ID'].start_with?(@term_code)
          if default_dates
            course_dates = confirmation.slice('START_DATE', 'END_DATE')
            if confirmation['MODULAR_COURSE'].blank?
              errors.add "Unexpected dates #{confirmation['START_DATE']} to #{confirmation['END_DATE']} for non-modular course" unless course_dates == default_dates
            elsif confirmation['MODULAR_COURSE'] == 'Y'
              errors.add "Default term dates #{confirmation['START_DATE']} to #{confirmation['END_DATE']} for modular course" if course_dates == default_dates
            end
            errors.add "START_DATE #{confirmation['START_DATE']} does not match term code #{@term_code}" unless confirmation['START_DATE'].end_with? @term_code[0..3]
            errors.add "END_DATE #{confirmation['END_DATE']} does not match term code #{@term_code}" unless confirmation['END_DATE'].end_with? @term_code[0..3]
          end
          errors.add "Unexpected MODULAR_COURSE value #{confirmation['MODULAR_COURSE']}" unless confirmation['MODULAR_COURSE'].blank? || confirmation['MODULAR_COURSE'] == 'Y'
          validate_and_add(courses, confirmation, %w(COURSE_ID))
        end

        if confirmation['LDAP_UID'].present?
          validate_and_add(instructors, confirmation, %w(LDAP_UID))
          validate_and_add(course_instructors, confirmation, %w(LDAP_UID COURSE_ID))
        end

        if confirmation['DEPT_FORM'].present?
          dept_supervisors = supervisors.matching_dept_name(confirmation['DEPT_FORM'])
          validate('courses', confirmation['COURSE_ID']) do |errors|
            errors.add "DEPT_FORM #{confirmation['DEPT_FORM']} not found among participating departments" unless participating_dept_names.include? confirmation['DEPT_FORM']
            errors.add "No supervisors found for DEPT_FORM #{confirmation['DEPT_FORM']}" if dept_supervisors.none?
            dept_supervisors.each do |supervisor|
              course_supervisor_row = {
                'COURSE_ID' => confirmation['COURSE_ID'],
                'LDAP_UID' => supervisor['LDAP_UID'],
                'DEPT_NAME' => confirmation['DEPT_FORM']
              }
              validate_and_add(course_supervisors, course_supervisor_row, %w(LDAP_UID COURSE_ID))
            end
          end
        end

        next unless (ccn = confirmation['COURSE_ID'].split('-')[2])
        ccn, suffix = ccn.split('_')
        if suffix
          suffixed_ccns[ccn] ||= Set.new
          suffixed_ccns[ccn] << suffix
        else
          ccns << ccn
        end
      end

      courses.group_by { |course| course['CROSS_LISTED_NAME'] }.each do |cross_listed_name, courses|
        next if cross_listed_name.blank?
        course_ids = courses.map { |course| course['COURSE_ID'] }
        comparison_course_id = course_ids.shift
        expected_instructor_ids = course_instructors.uids_for_course_id(comparison_course_id).sort
        course_ids.each do |course_id|
          validate('courses', course_id) do |errors|
            instructor_ids = course_instructors.uids_for_course_id(course_id).sort
            if instructor_ids != expected_instructor_ids
              errors.add "Instructor list (#{instructor_ids.join ', '}) differs from instructor list (#{expected_instructor_ids.join ', '}) of cross-listed course #{comparison_course_id}"
            end
          end
        end
      end

      Oec::Queries.students_for_cntl_nums(@term_code, ccns).each do |student_row|
        validate_and_add(students, Oec::Worksheet.capitalize_keys(student_row), %w(LDAP_UID))
      end

      log :warn, "Getting students for #{ccns.length} non-suffixed CCNs" unless ccns.none?
      Oec::Queries.enrollments_for_cntl_nums(@term_code, ccns).each do |enrollment_row|
        validate_and_add(course_students, Oec::Worksheet.capitalize_keys(enrollment_row), %w(LDAP_UID COURSE_ID))
      end

      # Course IDs with suffixes need a little extra wrangling to match up with Oracle queries.
      log :warn, "Getting students for #{suffixed_ccns.length} suffixed CCNs" unless suffixed_ccns.none?
      Oec::Queries.enrollments_for_cntl_nums(@term_code, suffixed_ccns.keys).each do |enrollment_row|
        ccn = enrollment_row['course_id'].split('-')[2].split('_')[0]
        suffixed_ccns[ccn].each do |suffix|
          capitalized_row = Oec::Worksheet.capitalize_keys enrollment_row
          capitalized_row['COURSE_ID'] = "#{capitalized_row['COURSE_ID']}_#{suffix}"
          validate_and_add(course_students, capitalized_row, %w(LDAP_UID COURSE_ID))
        end
      end
      if valid?
        log :info, 'Validation passed.'
        [instructors, course_instructors, courses, students, course_students, supervisors, course_supervisors]
      else
        @status = 'Error'
        log :error, 'Validation failed!'
        log_validation_errors
        nil
      end
    end

  end
end
