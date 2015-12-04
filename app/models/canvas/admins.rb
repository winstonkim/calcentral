module Canvas
  class Admins < Proxy
    include PagedProxy

    def self.add_admin_to_servers(admin_id, canvas_hosts)
      canvas_hosts.each do |canvas_host|
        worker = Canvas::Admins.new(url_root: canvas_host)
        result = worker.add_new_admin(admin_id)
        added = result[:added]
        if added
          logger.warn "Added admin #{admin_id} to #{canvas_host}"
        else
          logger.info "User #{admin_id} is already an admin on #{canvas_host}"
        end
      end
    end

    def initialize(options = {})
      super(options)
      account_id = options[:account_id] || settings.account_id
      raise ArgumentError, 'Account ID option must be a String or Fixnum' unless [String,Fixnum].include? account_id.class
      @account_id = account_id
    end

    def admin_user?(uid, options = {})
      admins = admins_list options
      admins[:body].present? && admins[:body].index {|acct| acct['user']['sis_login_id'] == uid.to_s}.present?
    end

    def admins_list(options)
      optional_cache(options, key: @account_id, default: true) { paged_get request_path }
    end

    def add_admin(canvas_user_id)
      wrapped_post request_path, {
        'user_id' => canvas_user_id,
        'send_confirmation' => false
      }
    end

    def add_new_admin(canvas_login_id)
      if admin_user? canvas_login_id, cache: false
        added = false
      else
        profile = Canvas::SisUserProfile.new(user_id: canvas_login_id).get
        canvas_user_id = profile['id']
        add_admin canvas_user_id
        added = true
      end
      {added: added}
    end

    private

    def request_path
      "accounts/#{@account_id}/admins"
    end

    def mock_interactions
      mock_paged_interaction("canvas_admins_#{@account_id}", uri_matching: request_path, method: :get)
      on_request(uri_matching: request_path, method: :post).
        respond_with_file('fixtures', 'json', 'canvas_add_admin.json')
    end

  end
end
