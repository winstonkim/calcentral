class DelegateActAsController < ActAsController

  skip_before_filter :check_reauthentication, :only => [:stop_delegate_act_as]

  def initialize
    super(act_as_session_key: 'original_delegate_user_id')
  end

  def act_as_authorization(uid_param)
    delegate_user_id = CalnetCrosswalk::ByUid.new(user_id: uid_param).lookup_delegate_user_id
    raise NotAuthorizedError.new('The current user is not authorized to act as a delegate.') unless delegate_user_id
  end

  def after_successful_start(session, params)
    # Do nothing
  end

end
