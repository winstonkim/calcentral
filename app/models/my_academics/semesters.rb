module MyAcademics
  class Semesters
    include AcademicsModule
    include SafeJsonParser

    def initialize(uid)
      super(uid)
    end

    def merge(data)
      if Settings.peoplesoft_proxy.fake
        merge_fake data
      else
        merge_peoplesoft data
      end
    end

    def merge_peoplesoft(data)
      # for demo purposes, we hardcode to Oski's UID. We only have a single term in the demo feed.
      proxy = Peoplesoft::Proxy.new({user_id: '61889', fake: false})
      json = HashConverter.symbolize(safe_json(proxy.get[:body]))
      semesters = []

      # now convert some Peoplesoft representations of data into our native form.
      term = json[:STUDENT_STUDY_TERM][:TERM]
      term_code = Berkeley::TermCodes.from_english(term[:name])
      term[:termCode] = term_code[:term_cd]
      term[:termYear] = term_code[:term_yr]
      term[:slug] = Berkeley::TermCodes.to_slug(term_code[:term_yr], term_code[:term_cd])
      term[:timeBucket] = 'current'
      term[:gradingInProgress] = false
      term[:classes] = term.delete :CLASS

      term[:classes].each { |this_class|
        this_class[:course_code] = this_class.delete :courseCode
        this_class[:dept_desc] = this_class.delete :deptDescr
        this_class[:slug] = this_class[:dept].downcase.gsub(/[^a-z0-9-]+/, '_') +
          '-' + this_class[:courseCatalog].strip.downcase.gsub(/[^a-z0-9-]+/, '_')
        sections = []

        sections << this_class[:SECTION]

        if sections[0][:SCHEDULE].is_a?(Array)
          sections[0][:schedules] = sections[0].delete :SCHEDULE
        else
          sections[0][:schedules] = []
          sections[0][:schedules][0] = sections[0].delete :SCHEDULE
        end

        sections[0][:schedules].each { |this_schedule|
          if this_schedule[:start_time].present?
            this_schedule[:schedule] = this_schedule[:class_meeting_days] + ' ' + this_schedule[:start_time] + '-' + this_schedule[:end_time]
          end
        }

        sections[0][:instructors] = []
        sections[0][:instructors][0] = sections[0].delete :INSTRUCTOR
        sections[0][:instructors][0].keys.each { |k|
          sections[0][:instructors][0][k.downcase] = sections[0][:instructors][0].delete k
        }
        this_class[:sections] = sections
      }
      semesters << term
      data[:semesters] = semesters
    end

    def merge_fake(data)
      # fake semester data feed for SIS PIP demo.
      # we'll gradually replace this with API calls to Tamer's Peoplesoft APIs.
      json = safe_json(File.read(Rails.root.join('fixtures', 'json', 'semesters.json').to_s))
      data[:semesters] = HashConverter.symbolize json['semesters']
    end

    def merge_oracle(data)
      proxy = CampusOracle::UserCourses::All.new({:user_id => @uid})
      feed = proxy.get_all_campus_courses
      transcripts = CampusOracle::UserCourses::Transcripts.new({:user_id => @uid}).get_all_transcripts
      semesters = []

      feed.keys.each do |term_key|
        (term_yr, term_cd) = term_key.split("-")
        semester = semester_info(term_yr, term_cd)
        feed[term_key].each do |course|
          next unless course[:role] == 'Student'
          class_item = class_info(course)
          class_item[:sections].each do |section|
            if section[:is_primary_section]
              section[:gradeOption] = Berkeley::GradeOptions.grade_option_for_enrollment(section[:cred_cd], section[:pnp_flag])
              section[:units] = section[:unit]
            end
          end
          class_item[:transcript] = find_transcript_data(transcripts, term_yr, term_cd, course[:dept], course[:catid])
          semester[:classes] << class_item
        end
        semesters << semester unless semester[:classes].empty?
      end

      data[:semesters] = semesters
    end

    def find_transcript_data(transcripts, term_yr, term_cd, dept_name, catalog_id)
      matching_transcripts = transcripts.select do |t|
        t['term_yr'] == term_yr &&
          t['term_cd'] == term_cd &&
          t['dept_name'] == dept_name &&
          t['catalog_id'] == catalog_id
      end
      if matching_transcripts.present?
        matching_transcripts.collect do |t|
          {
            units: t['transcript_unit'],
            grade: t['grade']
          }
        end
      else
        nil
      end
    end
  end
end
