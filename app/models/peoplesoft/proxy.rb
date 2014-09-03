module Peoplesoft
  class Proxy < BaseProxy

    include ClassLogger
    include Cache::UserCacheExpiry

    APP_ID = 'Peoplesoft'

    def initialize(options = {})
      super(Settings.peoplesoft_proxy, options)
    end

    def get
      self.class.smart_fetch_from_cache({id: @uid, user_message_on_exception: 'An error occurred retrieving data from Peoplesoft. Please try again later.'}) do
        internal_get
      end
    end

    private

    def internal_get
      url = @settings.base_url
      response = get_response url
      if response.code >= 400
        raise Errors::ProxyError.new("Connection failed: #{response.code} #{response.body}; url = #{url}", {
          body: 'An error occurred retrieving data from Peoplesoft. Please try again later.',
          statusCode: response.code
        })
      else
        xml = response.body
        convert_xml(xml).merge(
          {
            statusCode: 200
          })
      end
    end

    def convert_xml(xml)
      Hash.from_xml(xml)
    end

  end
end
