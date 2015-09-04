module HubEdos
  class Proxy < BaseProxy

    include ClassLogger
    include Cache::UserCacheExpiry
    include Proxies::Mockable

    APP_ID = 'integrationhub'
    APP_NAME = 'Integration Hub'

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
      internal_response = self.class.smart_fetch_from_cache(id: instance_key) do
        get_internal
      end
      if internal_response[:noStudentId] || internal_response[:statusCode] < 400
        internal_response
      else
        {
          errored: true
        }
      end
    end

    def get_internal
      logger.info "Fake = #{@fake}; Making request to #{url} on behalf of user #{@uid}; cache expiration #{self.class.expires_in}"
      response = get_response(url, request_options)
      logger.debug "Remote server status #{response.code}, Body = #{response.body}"
      feed = response.parsed_response
      {
        statusCode: response.code,
        feed: feed
      }
    end

    def url
      @settings.base_url
    end

    def request_options
      {}
    end

  end
end
