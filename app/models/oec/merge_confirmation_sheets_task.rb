module Oec
  class MergeConfirmationSheetsTask < Task

    include Validator

    def run_internal
      term_folder = @remote_drive.find_first_matching_item @term_code
      imports_folder = @remote_drive.find_first_matching_item(Oec::Folder.sis_imports, term_folder)
      most_recent_import = @remote_drive.find_folders(imports_folder.id).sort_by(&:title).last
      raise RuntimeError, "No SIS imports found for term #{@term_code}" unless most_recent_import

      confirmations_folder = @remote_drive.find_first_matching_item(Oec::Folder.confirmations, term_folder)
      raise RuntimeError, "No '#{Oec::Folder.confirmations}' folder found for term #{@term_code}" unless confirmations_folder
      merged_confirmations_folder = @remote_drive.find_first_matching_item(Oec::Folder.merged_confirmations, term_folder)
      raise RuntimeError, "No '#{Oec::Folder.merged_confirmations}' folder found for term #{@term_code}" unless merged_confirmations_folder

      overrides = @remote_drive.find_first_matching_item(Oec::Folder.overrides, term_folder)
      supervisors_sheet = @remote_drive.find_first_matching_item('supervisors', overrides)
      raise RuntimeError, "No supervisor sheet found in overrides for term #{@term_code}" unless supervisors_sheet

      supervisors = Oec::Supervisors.from_csv @remote_drive.export_csv(supervisors_sheet)

      merged_course_confirmations = Oec::SisImportSheet.new(export_name: 'Merged course confirmations')
      merged_supervisor_confirmations = Oec::Supervisors.new(export_name: 'Merged supervisor confirmations')

      department_names = Oec::CourseCode.by_dept_code(@course_code_filter).keys.map { |code| Berkeley::Departments.get(code, concise: true) }

      @remote_drive.get_items_in_folder(confirmations_folder.id).each do |department_item|
        next unless department_names.include? department_item.title

        if (sis_import_sheet = @remote_drive.find_first_matching_item(department_item.title, most_recent_import))
          sis_import = Oec::SisImportSheet.from_csv @remote_drive.export_csv(sis_import_sheet)
        else
          raise RuntimeError, "Could not find sheet '#{department_item.title}' in folder '#{most_recent_import.title}'"
        end

        confirmation_sheet = @remote_drive.spreadsheet_by_id department_item.id

        if (course_confirmation_worksheet = confirmation_sheet.worksheets.find { |w| w.title == 'Courses' })
          course_confirmation = Oec::CourseConfirmation.from_csv @remote_drive.export_csv(course_confirmation_worksheet)
          log :info, "Retrieved course confirmations from department sheet '#{department_item.title}'"
        else
          raise RuntimeError, "Could not find worksheet 'Courses' in sheet '#{confirmation_sheet.title}'"
        end

        if (supervisor_confirmation_worksheet = confirmation_sheet.worksheets.find { |w| w.title == 'Report Viewers' })
          supervisor_confirmation = Oec::SupervisorConfirmation.from_csv @remote_drive.export_csv(supervisor_confirmation_worksheet)
          log :info, "Retrieved supervisor confirmations from department sheet '#{department_item.title}'"
        else
          raise RuntimeError, "Could not find worksheet 'Report Viewers' in sheet '#{confirmation_sheet.title}'"
        end

        course_confirmation.each do |course_confirmation_row|
          validate('Merged course confirmations', "#{course_confirmation_row['COURSE_ID']}-#{course_confirmation_row['LDAP_UID']}") do |errors|
            sis_import_rows = sis_import.select { |row| row['LDAP_UID'] == course_confirmation_row['LDAP_UID'] && row['COURSE_ID'] == course_confirmation_row['COURSE_ID']  }
            if sis_import_rows.none?
              errors.add 'No SIS import row found matching confirmation row'
              validate_and_add(merged_course_confirmations, course_confirmation_row, %w(COURSE_ID LDAP_UID), strict: false)
            else
              errors.add 'Multiple SIS import rows found matching confirmation row' if sis_import_rows.count > 1
              sis_import_rows.each do |sis_import_row|
                merged_row = sis_import_row.merge course_confirmation_row
                validate_and_add(merged_course_confirmations, merged_row, %w(COURSE_ID LDAP_UID), strict: false)
              end
            end
          end
        end

        supervisor_confirmation.each do |supervisor_confirmation_row|
          validate('Merged supervisor confirmations', supervisor_confirmation_row['LDAP_UID']) do |errors|
            supervisor_rows = supervisors.select { |row| row['LDAP_UID'] == supervisor_confirmation_row['LDAP_UID']}
            if supervisor_rows.none?
              errors.add 'No supervisors row found matching confirmation row'
              validate_and_add(merged_supervisor_confirmations, supervisor_confirmation_row, %w(LDAP_UID), strict: false)
            else
              errors.add 'Multiple supervisor rows found matching confirmation row' if supervisor_rows.count > 1
              supervisor_rows.each do |supervisor_row|
                merged_row = supervisor_row.merge supervisor_confirmation_row
                validate_and_add(merged_supervisor_confirmations, merged_row, %w(LDAP_UID), strict: false)
              end
            end
          end
        end

        log :info, "Merged all rows from department sheet '#{department_item.title}'"
      end

      if !valid?
        log :warn, 'Confirmation sheets generated validation errors:'
        log_validation_errors
      end
      export_sheet(merged_course_confirmations, merged_confirmations_folder)
      export_sheet(merged_supervisor_confirmations, merged_confirmations_folder)
    end

  end
end
