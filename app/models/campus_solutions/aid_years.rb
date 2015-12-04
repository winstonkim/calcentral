module CampusSolutions
  class AidYears < DirectProxy

    include Cache::UserCacheExpiry
    include FinaidFeatureFlagged
    include CampusSolutionsIdRequired

    def initialize(options = {})
      super options
      initialize_mocks if @fake
    end

    def xml_filename
      'aid_years.xml'
    end

    def build_feed(response)
      return {} if response.parsed_response.blank?
      response.parsed_response
    end

    def url
      "#{@settings.base_url}/UC_FA_GET_T_C.v1/get?EMPLID=#{@campus_solutions_id}&INSTITUTION=UCB01"
    end

  end
end
