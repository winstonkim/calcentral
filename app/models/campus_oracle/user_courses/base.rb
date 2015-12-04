module CampusOracle
  module UserCourses

    APP_ID = 'Campus'

    class Base < BaseProxy

      def initialize(options = {})
        super(Settings.campusdb, options)
        @uid = @settings.fake_user_id if @fake
        @academic_terms = Berkeley::Terms.fetch.campus.values
      end

      def self.expires_in
        self.bearfacts_derived_expiration
      end

      def self.access_granted?(uid)
        !uid.blank?
      end

      def merge_enrollments(campus_classes)
        previous_item = {}
        enrollments = CampusOracle::Queries.get_enrolled_sections(@uid, @academic_terms)
        enrollments.each do |row|
          if (item = row_to_feed_item(row, previous_item))
            item[:role] = 'Student'
            semester_key = "#{item[:term_yr]}-#{item[:term_cd]}"
            campus_classes[semester_key] ||= []
            campus_classes[semester_key] << item
            previous_item = item
          end
        end
      end

      def merge_explicit_instructing(campus_classes)
        previous_item = {}
        assigneds = CampusOracle::Queries.get_instructing_sections(@uid, @academic_terms)
        is_instructing = assigneds.present?
        assigneds.each do |row|
          if (item = row_to_feed_item(row, previous_item))
            item[:role] = 'Instructor'
            semester_key = "#{item[:term_yr]}-#{item[:term_cd]}"
            campus_classes[semester_key] ||= []
            campus_classes[semester_key] << item
            previous_item = item
          end
        end
        is_instructing
      end

      # This is done in a separate step so that all secondary sections
      # are ordered after explicitly assigned primary sections.
      def merge_nested_instructing(campus_classes)
        campus_classes.values.each do |semester|
          semester.each do |course|
            if course[:role] == 'Instructor'
              primaries = course[:sections].select { |s| s[:is_primary_section] }
              if primaries.present? && Berkeley::CourseOptions::MAPPING[course[:course_option]]
                secondaries = CampusOracle::UserCourses::SecondarySections.new({user_id: @uid}).get_all_secondary_sections(course)
                if secondaries.present?
                  # Remember any explicit assignments to avoid duplicates and maintain ordering.
                  explicit_secondaries = course[:sections].select { |s| !s[:is_primary_section] }.collect { |s| s[:ccn] }
                  # Use a hash to avoid duplicates when an instructor is assigned more than one primary.
                  nested_secondaries = {}
                  primaries.each do |prim|
                    secondaries.each do |sec|
                      if explicit_secondaries.include?(sec['course_cntl_num']) ||
                        Berkeley::CourseOptions.nested?(course[:course_option], prim[:section_number], sec['section_num'], sec['instruction_format'])
                        nested_secondaries[sec['course_cntl_num']] = row_to_section_data(sec)
                      end
                    end
                  end
                  course[:sections] = primaries.concat(nested_secondaries.values) unless nested_secondaries.empty?
                end
              end
            end
          end
        end
      end

      def merge_cross_listing_hashes(campus_classes)
        campus_classes.values.each do |semester|
          cross_listed_sections = []
          semester.each do |course|
            if course[:role] == 'Instructor'
              course[:sections].each do |section|
                if section[:is_primary_section] && section[:cross_listed_flag] == 'Y'
                  cross_listed_sections.append section
                end
              end
            end
          end
          if cross_listed_sections.present?
            term_yr = semester.first[:term_yr]
            term_cd = semester.first[:term_cd]
            cross_listing_hashes = CampusOracle::Queries.get_cross_listings(term_yr, term_cd,
              cross_listed_sections.collect {|s| s[:ccn]})
            cross_listed_sections.each do |s|
              s[:cross_listing_hash] = cross_listing_hashes[s[:ccn].to_i]
            end
          end
        end
      end

      def row_to_feed_item(row, previous_item)
        unless (course_item = new_course_item(row, previous_item))
          previous_item[:sections] << row_to_section_data(row)
          nil
        else
          course_name = row['course_title'].present? ? row['course_title'] : row['course_title_short']
          course_item.merge({
                               term_yr: row['term_yr'],
                               term_cd: row['term_cd'],
                               dept: row['dept_name'],
                               dept_desc: row['dept_description'],
                               catid: row['catalog_id'],
                               course_catalog: row['catalog_id'],
                               course_option: row['course_option'],
                               emitter: 'Campus',
                               name: course_name,
                               sections: [
                                 row_to_section_data(row)
                               ]
                             })
        end
      end

      def new_course_item(row, previous_item)
        matched = (row['dept_name'] == previous_item[:dept]) &&
          (row['catalog_id'] == previous_item[:catid]) &&
          (row['term_yr'] == previous_item[:term_yr]) &&
          (row['term_cd'] == previous_item[:term_cd])
        if matched
          nil
        else
          course_ids_from_row(row)
        end
      end

      # Create IDs for a given course item:
      #   "id" : unique for the UserCourses feed across terms; used by Classes
      #   "slug" : URL-friendly ID without term information; used by Academics
      #   "course_code" : the short course name as displayed in the UX
      def course_ids_from_row(row)
        slug = row['dept_name'].downcase.gsub(/[^a-z0-9-]+/, '_') +
          '-' + row['catalog_id'].downcase.gsub(/[^a-z0-9-]+/, '_')
        course_data = {
          id: "#{slug}-#{row['term_yr']}-#{row['term_cd']}",
          slug: slug,
          course_code: "#{row['dept_name']} #{row['catalog_id']}"
        }
        course_data
      end

      def row_to_section_data(row)
        section_data = {
          ccn: row['course_cntl_num'].to_s,
          instruction_format: row['instruction_format'],
          is_primary_section: (row['primary_secondary_cd'] == 'P'),
          section_label: "#{row['instruction_format']} #{row['section_num']}",
          section_number: row['section_num']
        }
        if row['primary_secondary_cd'] == 'P'
          section_data[:units] = row['unit']
          section_data[:pnp_flag] = row['pnp_flag']
          section_data[:cred_cd] = row['cred_cd']
          section_data[:cross_listed_flag] = row['cross_listed_flag']
        end
        # enroll_status and grade only apply to enrollment records and will be skipped for instructors.
        if row['enroll_status'] == 'W'
          section_data[:waitlistPosition] = row['wait_list_seq_num'].to_i
          section_data[:enroll_limit] = row['enroll_limit'].to_i
        end
        section_data[:grade] = row['grade'].strip if row['grade'].present?
        section_data
      end

      def merge_detailed_section_data(campus_classes)
        campus_classes.values.each do |semester|
          semester.each do |course|
            # Remove any duplicates from campus data.
            course[:sections].uniq!
            course[:sections].each do |section|
              proxy = CampusOracle::CourseSections.new({term_yr: course[:term_yr],
                  term_cd: course[:term_cd],
                  ccn: section[:ccn]})
              section.merge!(proxy.get_section_data)
            end
          end
        end
      end

    end
  end
end
