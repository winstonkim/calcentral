class DelegateActAsController < ActAsController

  skip_before_filter :check_reauthentication, :only => [:stop_delegate_act_as]

  def initialize
    super(act_as_session_key: 'original_delegate_user_id')
  end

  def act_as_authorization(uid_param)
    acting_user_id = current_user.real_user_id
    response = CampusSolutions::DelegateStudents.new(user_id: acting_user_id).get
    if response[:feed] && (students = response[:feed][:students])
      campus_solutions_id = CalnetCrosswalk::ByUid.new(user_id: uid_param).lookup_campus_solutions_id
      student = students.detect { |s| campus_solutions_id == s[:campusSolutionsId] }
      authorized = student && [:financial, :viewEnrollments, :viewGrades].any? { |k| student[:privileges][k] }
      raise NotAuthorizedError.new("User #{acting_user_id} is not authorized to delegate-view-as #{student}") unless authorized
    else
      raise NotAuthorizedError.new "User #{acting_user_id} does not have delegate affiliation"
    end
  end

  def after_successful_start(session, params)
    # Do nothing
  end

end
