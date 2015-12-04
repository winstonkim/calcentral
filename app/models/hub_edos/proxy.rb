module HubEdos
  class Proxy < BaseProxy

    include ClassLogger
    include Cache::UserCacheExpiry
    include Proxies::Mockable
    include CampusSolutions::ProfileFeatureFlagged
    include User::Student
    include SafeJsonParser

    APP_ID = 'integrationhub'
    APP_NAME = 'Integration Hub'

    def initialize(settings, options)
      super
      if @fake
        @campus_solutions_id = lookup_campus_solutions_id
        initialize_mocks
      end
    end

    def instance_key
      @uid
    end

    def json_filename
      ''
    end

    def mock_json
      read_file('fixtures', 'json', json_filename)
    end

    def mock_request
      super.merge(uri_matching: url)
    end

    def get
      if is_feature_enabled
        internal_response = self.class.smart_fetch_from_cache(id: instance_key) do
          get_internal
        end
        if internal_response[:noStudentId] || internal_response[:studentNotFound] || internal_response[:statusCode] < 400
          internal_response
        else
          internal_response.merge({
                                    errored: true
                                  })
        end
      else
        {}
      end
    end

    def get_internal
      @campus_solutions_id ||= lookup_campus_solutions_id
      if @campus_solutions_id.nil?
        logger.warn "Lookup of campus_solutions_id for uid #{@uid} failed, cannot call Campus Solutions API"
        {
          feed: empty_feed,
          noStudentId: true
        }
      else
        logger.info "Fake = #{@fake}; Making request to #{url} on behalf of user #{@uid}; cache expiration #{self.class.expires_in}"
        opts = request_options.merge({
                                       on_error: {
                                         rescue_status: 404
                                       }
                                     })
        response = get_response(url, opts)
        logger.debug "Remote server status #{response.code}, Body = #{response.body.force_encoding('UTF-8')}"
        if response.code == 404
          if response['StudentResponse'] && response['StudentResponse']['message'] == 'Student Not Found'
            student_not_found = true
            logger.warn "Student Not Found for UID #{@uid}, Campus Solutions ID #{@campus_solutions_id}"
          else
            logger.error "Unexpected 404 response for UID #{@uid}, Campus Solutions ID #{@campus_solutions_id}: #{response}"
          end
        end
        feed = build_feed response
        {
          statusCode: response.code,
          feed: feed,
          studentNotFound: student_not_found
        }
      end
    end

    def url
      @settings.base_url
    end

    def request_options
      opts = {
        headers: {
          'Accept' => 'application/json'
        }
      }
      if @settings.app_id.present? && @settings.app_key.present?
        # app ID and token are used on the prod/staging Hub servers
        opts[:headers].merge!({
                                'app_id' => @settings.app_id,
                                'app_key' => @settings.app_key
                              })
      else
        # basic auth is used on Hub dev servers
        opts.merge!({
                      basic_auth: {
                        username: @settings.username,
                        password: @settings.password
                      }
                    })
      end
      opts
    end

    def parse_response(response)
      safe_json response.body.force_encoding('UTF-8')
    end

    def build_feed(response)
      parse_response response
    end

    def empty_feed
      {}
    end

  end
end

