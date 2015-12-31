module Bearfacts
  class Profile < Proxy

    def mock_xml
      read_file('fixtures', 'xml', "bearfacts_profile_#{@student_id}.xml")
    end

    def request_path
      "/student/#{@student_id}"
    end

    # The Profile path is also a subpath for other BearFacts API endpoints, so we need to block
    # bogus URI matches.
    def mock_request
      super.merge(uri_end_of_path: true)
    end

  end
end
