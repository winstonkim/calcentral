require 'set'

describe Canvas::CoursePolicy do
  let(:user_id)             { Settings.canvas_proxy.test_user_id }
  let(:user)                { AuthenticationState.new('user_id' => user_id) }
  let(:canvas_course_id)    { 1121 }
  let(:course)              { Canvas::Course.new(:user_id => user.user_id, :canvas_course_id => canvas_course_id) }
  let(:course_user_hash) do
    {
      'id' => 3323890,
      'name' => 'Stu Testb',
      'sortable_name' => 'Testb, Stu',
      'short_name' => 'Stu Testb',
      'sis_user_id' => '300846',
      'sis_login_id' => '300846',
      'sis_import_id' => 6132026,
      'login_id' => '300846',
      'enrollments'=>[
        {
          'id'=>19986607,
          'root_account_id'=>90242,
          'user_id'=>3323890,
          'associated_user_id'=>nil,
          'course_id'=>1121,
          'course_section_id'=>1237014,
          'sis_import_id'=>nil,
          'enrollment_state'=>'active',
          'type'=>'StudentEnrollment',
          'role'=>'StudentEnrollment',
          'limit_privileges_to_course_section'=>false,
          'html_url'=>'https://berkeley.instructure.com/courses/1121/users/3323890',
          'start_at'=>nil,
          'end_at'=>nil,
          'last_activity_at'=>'2014-02-21T19:12:10Z',
          'updated_at'=>'2014-01-31T00:09:55Z',
          'created_at'=>'2013-08-22T21:28:15Z',
        }
      ]
    }
  end
  let(:course_teacher_hash)   { course_user_hash.merge({'enrollments' => [{'type' => 'TeacherEnrollment', 'role' => 'TeacherEnrollment'}]}) }
  let(:course_ta_hash)        { course_user_hash.merge({'enrollments' => [{'type' => 'TaEnrollment', 'role' => 'TaEnrollment'}]}) }
  let(:course_observer_hash)  { course_user_hash.merge({'enrollments' => [{'type' => 'ObserverEnrollment', 'role' => 'ObserverEnrollment'}]}) }
  let(:course_designer_hash)  { course_user_hash.merge({'enrollments' => [{'type' => 'DesignerEnrollment', 'role' => 'DesignerEnrollment'}]}) }
  let(:user_is_admin) { false }
  let(:invariable_course_user_hash) { course_user_hash }
  subject { Canvas::CoursePolicy.new(user, course) }
  before do
    allow_any_instance_of(Canvas::CourseUser).to receive(:course_user).and_return(invariable_course_user_hash)
    allow_any_instance_of(Canvas::Admins).to receive(:admin_user?).and_return(user_is_admin)
  end

  shared_examples 'a canvas user requirement' do
    context 'when no canvas user found for current user' do
      before { allow_any_instance_of(Canvas::SisUserProfile).to receive(:get).and_return(nil) }
      it 'returns false' do
        expect(authorization_method).to be false
      end
    end
  end

  describe '#can_add_users?' do
    it_should_behave_like 'a canvas user requirement' do
      let(:authorization_method) { subject.can_add_users? }
    end

    context 'when user is a primary account admin' do
      before do
        canvas_admins = double()
        canvas_admins.stub(:admin_user?).and_return(true)
        allow(Canvas::Admins).to receive(:new).and_return(canvas_admins)
      end
      it 'should return true' do
        expect(subject.can_add_users?).to be true
      end
    end

    context 'when user is a course admin' do
      let(:invariable_course_user_hash) { course_teacher_hash }
      it 'should return true' do
        expect(subject.can_add_users?).to be true
      end
    end

    context 'when user is only a student' do
      it 'should return false' do
        expect(subject.can_add_users?).to be false
      end
    end
  end

  describe '#can_export_grades?' do
    it_should_behave_like 'a canvas user requirement' do
      let(:authorization_method) { subject.can_export_grades? }
    end

    context 'when user is a primary account admin' do
      before do
        canvas_admins = double()
        canvas_admins.stub(:admin_user?).and_return(true)
        allow(Canvas::Admins).to receive(:new).and_return(canvas_admins)
      end
      it 'should return true' do
        expect(subject.can_export_grades?).to be true
      end
    end

    context 'when user is a course teacher' do
      let(:invariable_course_user_hash) { course_teacher_hash }
      it 'should return true' do
        expect(subject.can_export_grades?).to be true
      end
    end

    context 'when user is only a designer' do
      let(:invariable_course_user_hash) { course_designer_hash }
      it 'should return false' do
        expect(subject.can_export_grades?).to be false
      end
    end

    context 'when user is a teachers assistant' do
      let(:invariable_course_user_hash) { course_ta_hash }
      it 'should return false' do
        expect(subject.can_export_grades?).to be false
      end
    end

    context 'when user is only an observer' do
      let(:invariable_course_user_hash) { course_observer_hash }
      it 'should return true' do
        expect(subject.can_export_grades?).to be false
      end
    end

    context 'when user is only a student' do
      it 'should return false' do
        expect(subject.can_export_grades?).to be false
      end
    end

  end

  describe '#can_view_course_roster_photos?' do
    context 'when user is a canvas course teacher' do
      let(:invariable_course_user_hash) { course_teacher_hash }
      it 'returns true' do
        expect(subject.can_view_course_roster_photos?).to be true
      end
    end

    context 'when user is a canvas course teachers assistant' do
      let(:invariable_course_user_hash) { course_ta_hash }
      it 'returns true' do
        expect(subject.can_view_course_roster_photos?).to be true
      end
    end

    context 'when user is not a canvas course teacher or assistant' do
      it 'returns false' do
        expect(subject.can_view_course_roster_photos?).to be false
      end
    end
  end

  describe '#can_view_official_sections?' do
    subject { Canvas::CoursePolicy.new(user, course).can_view_official_sections? }
    context 'student' do
      it { should be_falsey }
    end
    context 'teacher' do
      let(:invariable_course_user_hash) { course_teacher_hash }
      it { should be_truthy }
    end
    context 'ta' do
      let(:invariable_course_user_hash) { course_ta_hash }
      it { should be_truthy }
    end
    context 'designer' do
      let(:invariable_course_user_hash) { course_designer_hash }
      it { should be_truthy }
    end
    context 'observer' do
      let(:invariable_course_user_hash) { course_observer_hash }
      it { should be_falsey }
    end
    context 'admin' do
      let(:invariable_course_user_hash) { nil }
      let(:user_is_admin) { true }
      it { should be_truthy }
    end
    context 'non-member' do
      let(:invariable_course_user_hash) { nil }
      it { should be_falsey }
    end
  end

  describe '#can_edit_official_sections?' do
    subject { Canvas::CoursePolicy.new(user, course).can_edit_official_sections? }
    context 'student' do
      it { should be_falsey }
    end
    context 'teacher' do
      before { allow_any_instance_of(Canvas::CurrentTeacher).to receive(:user_currently_teaching?).and_return(has_sections) }
      let(:invariable_course_user_hash) { course_teacher_hash }
      context 'with assigned sections in the term' do
        let(:has_sections) { true }
        it { should be_truthy }
      end
      context 'with no campus sections in the term' do
        let(:has_sections) { false }
        it { should be_falsey }
      end
    end
    context 'ta' do
      let(:invariable_course_user_hash) { course_ta_hash }
      it { should be_falsey }
    end
    context 'designer' do
      let(:invariable_course_user_hash) { course_designer_hash }
      it { should be_falsey }
    end
    context 'observer' do
      let(:invariable_course_user_hash) { course_observer_hash }
      it { should be_falsey }
    end
    context 'admin' do
      let(:invariable_course_user_hash) { nil }
      let(:user_is_admin) { true }
      it { should be_falsey }
    end
    context 'non-member' do
      let(:invariable_course_user_hash) { nil }
      it { should be_falsey }
    end
  end

  describe '#is_canvas_user?' do
    it_should_behave_like 'a canvas user requirement' do
      let(:authorization_method) { subject.is_canvas_user? }
    end

    context 'if canvas user does exist' do
      it 'returns true' do
        expect(subject.is_canvas_course_user?).to be true
        expect(subject.can_view_webcast_sign_up?).to be false
      end
    end
  end

  describe '#is_canvas_course_teacher_or_assistant?' do
    it_should_behave_like 'a canvas user requirement' do
      let(:authorization_method) { subject.is_canvas_course_teacher_or_assistant? }
    end

    context 'if canvas user is a teacher' do
      it 'returns true' do
        allow(Canvas::CourseUser).to receive(:is_course_teacher?).and_return(true)
        expect(subject.is_canvas_course_teacher_or_assistant?).to be true
        expect(subject.can_view_webcast_sign_up?).to be true
      end
    end

    context 'if canvas user is a teachers assistant' do
      it 'returns true' do
        allow(Canvas::CourseUser).to receive(:is_course_teachers_assistant?).and_return(true)
        expect(subject.is_canvas_course_teacher_or_assistant?).to be true
        expect(subject.can_view_webcast_sign_up?).to be true
      end
    end

    context 'if canvas user is not a teacher or assistant' do
      it 'returns false' do
        allow(Canvas::CourseUser).to receive(:is_course_teacher?).and_return(false)
        allow(Canvas::CourseUser).to receive(:is_course_teachers_assistant?).and_return(false)
        expect(subject.is_canvas_course_teacher_or_assistant?).to be false
      end
    end
  end

  describe '#is_canvas_course_teacher?' do
    it_should_behave_like 'a canvas user requirement' do
      let(:authorization_method) { subject.is_canvas_course_teacher? }
    end

    context 'if canvas user is a teacher' do
      it 'returns true' do
        allow(Canvas::CourseUser).to receive(:is_course_teacher?).and_return(true)
        expect(subject.is_canvas_course_teacher?).to be true
      end
    end

    context 'if canvas user is not a teacher' do
      it 'returns false' do
        allow(Canvas::CourseUser).to receive(:is_course_teacher?).and_return(false)
        expect(subject.is_canvas_course_teacher?).to be false
        expect(subject.can_view_webcast_sign_up?).to be false
      end
    end
  end

  describe '#is_canvas_course_teachers_assistant?' do
    it_should_behave_like 'a canvas user requirement' do
      let(:authorization_method) { subject.is_canvas_course_teachers_assistant? }
    end

    context 'if canvas user is a teachers assistant' do
      it 'returns true' do
        allow(Canvas::CourseUser).to receive(:is_course_teachers_assistant?).and_return(true)
        expect(subject.is_canvas_course_teachers_assistant?).to be true
      end
    end

    context 'if canvas user is not a teachers assistant' do
      it 'returns false' do
        allow(Canvas::CourseUser).to receive(:is_course_teachers_assistant?).and_return(false)
        expect(subject.is_canvas_course_teachers_assistant?).to be false
      end
    end
  end

  describe '#is_canvas_account_admin?' do
    it 'returns true when user is a canvas root account administrator' do
      canvas_admins = double()
      allow(canvas_admins).to receive(:admin_user?).and_return(true)
      allow(Canvas::Admins).to receive(:new).and_return(canvas_admins)
      expect(subject.is_canvas_account_admin?).to be true
      expect(subject.can_view_webcast_sign_up?).to be true
    end

    it 'returns false when user is not a canvas root account administrator' do
      expect(subject.is_canvas_account_admin?).to be false
    end
  end

  describe '#is_canvas_course_user?' do

    context 'if user is not a member of the course' do
      before { allow_any_instance_of(Canvas::CourseUser).to receive(:course_user).and_return(nil) }
      it 'returns false' do
        expect(subject.is_canvas_course_user?).to be false
        expect(subject.can_view_webcast_sign_up?).to be false
      end
    end

    context 'if user is a member of the course' do
      it 'returns true' do
        expect(subject.is_canvas_course_user?).to be true
        expect(subject.can_view_webcast_sign_up?).to be false
      end
    end
  end

  describe '#is_canvas_course_admin' do
    context 'if user is a student within the course' do
      it 'returns false' do
        expect(subject.is_canvas_course_admin?).to be false
      end
    end

    context 'if user is a teacher within the course' do
      let(:invariable_course_user_hash) { course_teacher_hash }
      it 'returns true' do
        expect(subject.is_canvas_course_admin?).to be true
      end
    end

    context 'if user is a teachers assistant within the course' do
      let(:invariable_course_user_hash) { course_ta_hash }
      it 'returns true' do
        expect(subject.is_canvas_course_admin?).to be true
      end
    end

    context 'if user is a designer within the course' do
      let(:invariable_course_user_hash) { course_designer_hash }
      it 'returns true' do
        expect(subject.is_canvas_course_admin?).to be true
      end
    end
  end

end
