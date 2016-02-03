module CampusSolutions
  class EnrollmentTerms < Proxy

    include CampusSolutionsIdRequired
    include EnrollmentCardFeatureFlagged

    def initialize(options = {})
      super options
      initialize_mocks if @fake
    end

    def build_feed(response)
      return {} unless (response.parsed_response && response['UC_SR_ENROLLMENT_TERMS'] && response['UC_SR_ENROLLMENT_TERMS']['CAREERS'])
      terms = []
      response['UC_SR_ENROLLMENT_TERMS']['CAREERS'].each do |career|
        Array.wrap(career['TERM']).each do |term|
          term['ACAD_CAREER'] = career['ACAD_CAREER']
          terms << term
        end
      end
      {
        enrollment_terms: terms.sort_by { |term| term['TERM_ID'] },
        student_id: response['UC_SR_ENROLLMENT_TERMS']['STUDENT_ID']
      }
    end

    def xml_filename
      'enrollment_terms.xml'
    end

    def url
      "#{@settings.base_url}/UC_SR_CURR_TERMS.v1/GetCurrentItems?EMPLID=#{@campus_solutions_id}"
    end
  end
end
