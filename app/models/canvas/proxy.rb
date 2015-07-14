module Canvas
  require 'signet/oauth_2/client'

  class Proxy < BaseProxy
    include ClassLogger, SafeJsonParser
    include Cache::UserCacheExpiry
    include Proxies::Mockable

    attr_accessor :client
    APP_ID = "Canvas"
    APP_NAME = "bCourses"

    def self.new(*args, &blk)
      # Initialize mocks only after subclass initialization has set instance variables needed for URL matching.
      proxy = super
      proxy.initialize_mocks if proxy.fake
      proxy
    end

    def initialize(options = {})
      super(Settings.canvas_proxy, options)
      if @fake
        @uid = @settings.test_user_id
      end
      access_token = if @fake
                       'fake_access_token'
                     elsif options[:access_token]
                       options[:access_token]
                     else
                       @settings.admin_access_token
                     end
      @client = Signet::OAuth2::Client.new(:access_token => access_token)
    end

    def request(api_path, fetch_options = {})
      self.class.smart_fetch_from_cache(
        {id: @uid,
         user_message_on_exception: "Remote server unreachable",
         return_nil_on_generic_error: true}) do
        request_internal(api_path, fetch_options)
      end
    end

    def request_uncached(api_path, fetch_options = {})
      begin
        request_internal(api_path, fetch_options)
      rescue => e
        self.class.handle_exception(e, self.class.cache_key(@uid), {
          id: @uid,
          user_message_on_exception: "Remote server unreachable",
          return_nil_on_generic_error: true
        })
      end
    end

    def request_internal(api_path, fetch_options = {})
      fetch_options.reverse_merge!(
        :method => :get,
        :uri => "#{api_root}/#{api_path}"
      )
      logger.info "Making request with @fake = #{@fake}, options = #{fetch_options}, cache expiration #{self.class.expires_in}"
      ActiveSupport::Notifications.instrument('proxy', {url: fetch_options[:uri], class: self.class}) do
        if (nonstandard_connection = fetch_options[:non_oauth_connection])
          response = nonstandard_connection.get(fetch_options[:uri])
        else
          response = @client.fetch_protected_resource(fetch_options)
        end
        # Canvas proxy returns nil for error response.
        if response.status >= 400
          if existence_check && response.status == 404
            logger.debug("404 status returned for URL '#{fetch_options[:uri]}', UID #{@uid}")
            return nil
          end
          raise Errors::ProxyError.new('Connection failed', response: response, url: fetch_options[:uri], uid: @uid)
        else
          response
        end
      end
    end

    def self.access_granted?(user_id)
      user_id && has_account?(user_id)
    end

    def api_root
      "#{@settings.url_root}/api/v1"
    end

    def self.has_account?(user_id)
      Settings.canvas_proxy.fake || (Canvas::SisUserProfile.new(user_id: user_id).sis_user_profile != nil)
    end

    def self.canvas_current_terms
      terms = []
      terms_from_campus = Berkeley::Terms.fetch
      terms_from_canvas = Canvas::Terms.fetch

      # Get current and next term, and optionally future fall term, from campus data
      terms.push terms_from_campus.current
      terms.push terms_from_campus.next if terms_from_campus.next
      if (future_term = terms_from_campus.future) && future_term.name == 'Fall'
        terms.push future_term
      end

      # Return subset of terms that have SIS ids in Canvas, warn on missing SIS ids
      sis_ids_from_canvas = terms_from_canvas.map{|term| term['sis_term_id']}
      terms.reject do |term|
        if !sis_ids_from_canvas.include? term_to_sis_id(term.year, term.code)
          logger.warn("SIS ID #{term_to_sis_id(term.year, term.code)} not found in Canvas")
          true
        else
          false
        end
      end
    end

    def self.current_sis_term_ids
      canvas_current_terms.collect do |term|
        term_to_sis_id(term.year, term.code)
      end
    end

    def self.sis_section_id_to_ccn_and_term(sis_term_id)
      if (parsed = /SEC:(?<term_yr>\d+)-(?<term_cd>[[:upper:]])-(?<ccn>\d+).*/.match(sis_term_id))
        {
          term_yr: parsed[:term_yr],
          term_cd: parsed[:term_cd],
          ccn: sprintf('%05d', parsed[:ccn].to_i)
        }
      end
    end

    def self.sis_term_id_to_term(sis_term_id)
      if (parsed = /TERM:(?<term_yr>\d+)-(?<term_cd>[[:upper:]])$/.match(sis_term_id))
        {
          term_yr: parsed[:term_yr],
          term_cd: parsed[:term_cd]
        }
      end
    end

    def self.term_to_sis_id(term_yr, term_cd)
      "TERM:#{term_yr}-#{term_cd}"
    end

    def next_page_params(response)
      # If the response's link header included a "next" page pointer...
      if response && (next_link = LinkHeader.parse(response['link']).find_link(['rel', 'next']))
        # ... then extract the query string from its URL.
        next_query_string = /.+\?(.+)/.match(next_link.href)[1]
      else
        nil
      end
    end

    def existence_check
      false
    end

    private

    def mock_request
      super.merge(uri_matching: "#{api_root}/#{request_path}")
    end

    def request_path
      ''
    end
  end
end
