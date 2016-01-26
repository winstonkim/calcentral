module CampusSolutions
  class EnrollmentTerms < Proxy

    include CampusSolutionsIdRequired
    include EnrollmentCardFeatureFlagged

    def initialize(options = {})
      super options
      initialize_mocks if @fake
    end

    def build_feed(response)
      return {} if response.parsed_response.blank?
      {
        enrollment_terms: response['UC_SR_ENROLLMENT_TERMS']
      }
    end

    def xml_filename
      'enrollment_terms.xml'
    end

    def url
      # This fake URL allows tests against mock proxies until we get a real URL.
      "#{@settings.base_url}/NOT_A_REAL_URL.v1/get/enrollment_terms?EMPLID=#{@campus_solutions_id}"
    end
  end
end
