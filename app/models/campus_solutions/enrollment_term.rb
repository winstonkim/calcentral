module CampusSolutions
  class EnrollmentTerm < Proxy

    include CampusSolutionsIdRequired
    include DatedFeed
    include EnrollmentCardFeatureFlagged

    def initialize(options = {})
      super options
      initialize_mocks if @fake
    end

    def build_feed(response)
      return {} if response.parsed_response.blank?
      enrollment_term = response['UC_SR_CLASS_ENROLLMENT']
      if enrollment_term['ENROLLMENT_PERIODS']
        enrollment_term['ENROLLMENT_PERIODS'].each { |period| format_cs_datetime period }
      end
      if enrollment_term['SCHEDULE_OF_CLASSES_PERIOD']
        format_cs_datetime enrollment_term['SCHEDULE_OF_CLASSES_PERIOD']
      end
      {
        enrollment_term: enrollment_term
      }
    end

    def format_cs_datetime(hash)
      if hash['DATETIME']
        hash['DATETIME'] = format_date DateTime.rfc3339(hash['DATETIME'])
      end
    end

    def xml_filename
      'enrollment_term.xml'
    end

    def url
      # This fake URL allows tests against mock proxies until we get a real URL.
      "#{@settings.base_url}/NOT_A_REAL_URL.v1/get/enrollment_term?EMPLID=#{@campus_solutions_id}&TERM_ID=#{@term_id}"
    end
  end
end
