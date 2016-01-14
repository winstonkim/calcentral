class UserApiController < ApplicationController

  extend Cache::Cacheable

  def self.expire(id = nil)
    # no-op; this class uses the cache only to reduce the number of writes to User::Visit. We want to just expire
    # with time, not when the cache is forcibly cleared.
  end

  def am_i_logged_in
    response.headers["Cache-Control"] = "no-cache, no-store, private, must-revalidate"
    response.headers["Pragma"] = "no-cache"
    response.headers["Expires"] = "-1"
    render :json => {
      :amILoggedIn => !!session['user_id']
    }.to_json
  end

  def mystatus
    ActiveRecordHelper.clear_stale_connections
    status = {}
    features = HashConverter.camelize Settings.features.marshal_dump

    if session['user_id']
      # wrap User::Visit.record_session inside a cache lookup so that we have to write User::Visit records less often.
      directly_authenticated = current_user.directly_authenticated?
      self.class.fetch_from_cache session['user_id'] do
        User::Visit.record session['user_id'] if directly_authenticated
        true
      end
      delegate_acting_as_student = !directly_authenticated && current_user.original_delegate_user_id.present?
      status.merge!({
        :isBasicAuthEnabled => Settings.developer_auth.enabled,
        :isLoggedIn => true,
        :features => features,
        :actingAsUid => directly_authenticated || delegate_acting_as_student ? false : current_user.real_user_id,
        :delegateActingAsStudent => directly_authenticated ? false : delegate_acting_as_student,
        :youtubeSplashId => Settings.youtube_splash_id
      })
      status.merge!(User::Api.from_session(session).get_feed)
    else
      status.merge!({
        :isBasicAuthEnabled => Settings.developer_auth.enabled,
        :isLoggedIn => false,
        :features => features,
        :youtubeSplashId => Settings.youtube_splash_id
      })
    end
    render :json => status.to_json
  end

  def record_first_login
    User::Api.from_session(session).record_first_login if current_user.directly_authenticated?
    render :nothing => true, :status => 204
  end

  def delete
    if session['user_id'] && current_user.directly_authenticated?
      User::Api.delete(session['user_id'])
      render :nothing => true, :status => 204
    else
      render :nothing => true, :status => 403
    end
  end

  def calendar_opt_in
    expire_current_user
    if session['user_id'] && current_user.directly_authenticated?
      Calendar::User.where(uid: session['user_id']).first_or_create
      render :nothing => true, :status => 204
    else
      render :nothing => true, :status => 403
    end
  end

  def calendar_opt_out
    expire_current_user
    if session['user_id'] && current_user.directly_authenticated?
      Calendar::User.where(uid: session['user_id']).delete_all
      render :nothing => true, :status => 204
    else
      render :nothing => true, :status => 403
    end
  end

end
