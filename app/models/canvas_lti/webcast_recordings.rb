module CanvasLti
  class WebcastRecordings
    extend Cache::Cacheable
    include ClassLogger

    def initialize(uid, course_policy, canvas_course_id, options = {})
      @uid = uid.to_i unless uid.nil?
      @course_policy = course_policy
      @canvas_course_id = canvas_course_id
      @options = options
    end

    # Authorization checks are performed by the controller.
    def get_feed
      self.class.fetch_from_cache "#{@canvas_course_id}/#{@uid}" do
        get_feed_internal
      end
    end

    def get_feed_internal
      ccn_list = []
      if @canvas_course_id
        response = Canvas::CourseSections.new(course_id: @canvas_course_id).sections_list
        if canvas_sections = response[:body]
          canvas_sections.each do |canvas_section|
            if (campus_section = Canvas::Terms.sis_section_id_to_ccn_and_term(canvas_section['sis_section_id']))
              @term_yr ||= campus_section[:term_yr]
              @term_cd ||= campus_section[:term_cd]
              ccn = campus_section[:ccn].to_i
              ccn_list << ccn if ccn > 0
            end
          end
        end
      end
      Webcast::Merged.new(@uid, @course_policy, @term_yr, @term_cd, ccn_list, @options).get_feed
    end

  end
end
