describe CanvasCourseGradeExportController do

  let(:course_grades) do
    [
      {'uid' => '1001', 'grade' => 84.9, 'comment' => ''},
      {'uid' => '1002', 'grade' => 45.9, 'comment' => ''},
      {'uid' => '1003', 'grade' => 78.2, 'comment' => ''},
      {'uid' => '1004', 'grade' => 95.7, 'comment' => ''},
    ]
  end

  let(:canvas_course_id) { '1164764' }

  before do
    session['user_id'] = '4868640'
    session['canvas_user_id'] = '43232321'
    session['canvas_course_id'] = canvas_course_id
    allow_any_instance_of(Canvas::CoursePolicy).to receive(:can_export_grades?).and_return(true)
    allow_any_instance_of(Canvas::CourseUsers).to receive(:course_grades).and_return(course_grades)
  end

  describe 'when preparing course enrollments cache' do
    let(:torquebox_fake_background_proxy) { double }
    let(:background_job_id) { 'canvas.egrades.12345.1383330151058' }
    before do
      allow(torquebox_fake_background_proxy).to receive(:canvas_course_student_grades).with(true).and_return(nil)
      allow_any_instance_of(CanvasLti::Egrades).to receive(:background).and_return(torquebox_fake_background_proxy)
      allow_any_instance_of(CanvasLti::Egrades).to receive(:save).and_return(nil)
      allow_any_instance_of(CanvasLti::Egrades).to receive(:background_job_id).and_return(background_job_id)
    end

    it_should_behave_like 'an endpoint' do
      let(:make_request) { post :prepare_grades_cache, :canvas_course_id => canvas_course_id, :format => :csv }
      let(:error_text) { 'Something went wrong' }
      before { allow_any_instance_of(CanvasLti::Egrades).to receive(:background_job_initialize).and_raise(RuntimeError, error_text) }
    end

    it_should_behave_like 'an authenticated endpoint' do
      let(:make_request) { post :prepare_grades_cache, :canvas_course_id => canvas_course_id, :format => :csv }
    end

    it 'makes call to load canvas course student grades with forced cacheing' do
      expect(torquebox_fake_background_proxy).to receive(:canvas_course_student_grades).with(true).and_return(nil)
      allow_any_instance_of(CanvasLti::Egrades).to receive(:background).and_return(torquebox_fake_background_proxy)
      post :prepare_grades_cache, :canvas_course_id => canvas_course_id, :format => :csv
      expect(response.status).to eq(200)
      json_response = JSON.parse(response.body)
      expect(json_response).to be_an_instance_of Hash
      expect(json_response['jobRequestStatus']).to eq 'Success'
    end

    it 'returns background job id' do
      allow_any_instance_of(CanvasLti::Egrades).to receive(:background).and_return(torquebox_fake_background_proxy)
      post :prepare_grades_cache, :canvas_course_id => canvas_course_id, :format => :csv
      expect(response.status).to eq(200)
      json_response = JSON.parse(response.body)
      expect(json_response).to be_an_instance_of Hash
      expect(json_response['jobRequestStatus']).to eq 'Success'
      expect(json_response['jobId']).to eq background_job_id
    end
  end

  describe 'when resolving issues with course site state related to grade export' do
    before { allow_any_instance_of(CanvasLti::Egrades).to receive(:resolve_issues).and_return(nil) }

    it_should_behave_like 'an endpoint' do
      let(:make_request) { post :resolve_issues, canvas_course_id: 'embedded', :enableGradingScheme => 1, :format => :csv }
      let(:error_text) { 'Something went wrong' }
      before { allow_any_instance_of(CanvasLti::Egrades).to receive(:resolve_issues).and_raise(RuntimeError, error_text) }
    end

    it_should_behave_like 'an authenticated endpoint' do
      let(:make_request) { post :resolve_issues, canvas_course_id: 'embedded', :enableGradingScheme => 1, :format => :csv }
    end

    it 'supports enableGradingScheme option' do
      expect_any_instance_of(CanvasLti::Egrades).to receive(:resolve_issues).with(true, false)
      post :resolve_issues, :canvas_course_id => canvas_course_id, :enableGradingScheme => 1, :format => :csv
      expect(response.status).to eq(200)
      json_response = JSON.parse(response.body)
      expect(json_response).to be_an_instance_of Hash
      expect(json_response['status']).to eq 'Resolved'
    end

    it 'supports unmuteAssignments option' do
      expect_any_instance_of(CanvasLti::Egrades).to receive(:resolve_issues).with(false, true)
      post :resolve_issues, :canvas_course_id => canvas_course_id, :unmuteAssignments => 1, :format => :csv
      expect(response.status).to eq(200)
      json_response = JSON.parse(response.body)
      expect(json_response).to be_an_instance_of Hash
      expect(json_response['status']).to eq 'Resolved'
    end
  end

  describe '#job_status' do
    let(:background_job_id) { 'CanvasLti::Egrades.1383330151057-67f4b934525501cb' }

    it_should_behave_like 'an endpoint' do
      let(:error_text) { 'Something went wrong' }
      let(:make_request) { get :job_status, canvas_course_id: canvas_course_id, jobId: background_job_id }
      before { allow(BackgroundJob).to receive(:find).and_raise(RuntimeError, 'Something went wrong') }
    end

    it_should_behave_like 'an authenticated endpoint' do
      let(:make_request) { get :job_status, canvas_course_id: canvas_course_id, jobId: background_job_id }
    end

    it 'returns error if egrades background job not found' do
      get :job_status, canvas_course_id: canvas_course_id, jobId: background_job_id
      assert_response :success
      json_response = JSON.parse(response.body)
      expect(json_response['jobId']).to eq background_job_id
      expect(json_response['jobStatus']).to eq 'Error'
      expect(json_response['errors']).to eq ['Unable to find Canvas::EGrades background job']
    end

    it 'returns status of canvas egrades background job' do
      egrades = CanvasLti::Egrades.new(:canvas_course_id => canvas_course_id)
      egrades.background_job_initialize
      egrades.background_job_set_total_steps(2)
      egrades.background_job_complete_step('step 1')

      get :job_status, canvas_course_id: canvas_course_id, jobId: egrades.background_job_id
      assert_response :success
      json_response = JSON.parse(response.body)
      expect(json_response['jobId']).to eq egrades.background_job_id
      expect(json_response['jobStatus']).to eq 'Processing'
      expect(json_response['completedSteps'].count).to eq 1
      expect(json_response['completedSteps'][0]).to eq 'step 1'
      expect(json_response['percentComplete']).to eq 0.50
      expect(json_response['errors']).to eq nil
    end
  end

  describe 'when serving grade export option data' do
    let(:official_course_sections) do
      [
        {'dept_name'=>'CHEM', 'catalog_id'=>'3BL', 'term_yr'=>'2014', 'term_cd'=>'C', 'course_cntl_num'=>'22280', 'primary_secondary_cd'=>'P', 'section_num'=>'001', 'instruction_format'=>'LEC'},
        {'dept_name'=>'CHEM', 'catalog_id'=>'3BL', 'term_yr'=>'2014', 'term_cd'=>'C', 'course_cntl_num'=>'22345', 'primary_secondary_cd'=>'S', 'section_num'=>'208', 'instruction_format'=>'LAB'}
      ]
    end
    let(:course_settings) do
      {
        'grading_standard_enabled' => true,
        'grading_standard_id' => 0
      }
    end
    let(:muted_assignments) do
      [
        {'name' => 'Assignment 4', 'due_at' => 'Oct 13, 2015 at 8:30am', 'points_possible' => 25},
        {'name' => 'Assignment 7', 'due_at' => 'Oct 18, 2015 at 9:30am', 'points_possible' => 100},
      ]
    end
    let(:section_terms) { [{:term_cd => 'C', :term_yr => '2014'}, {:term_cd => 'D', :term_yr => '2015'}] }
    let(:export_options) do
      {
        :officialSections => official_course_sections,
        :gradingStandardEnabled => false,
        :sectionTerms => section_terms,
        :mutedAssignments => muted_assignments
      }
    end
    before do
      allow_any_instance_of(CanvasLti::Egrades).to receive(:export_options).and_return(export_options)
    end

    it_should_behave_like 'an endpoint' do
      let(:make_request) { get :export_options, canvas_course_id: 'embedded', :format => :csv }
      let(:error_text) { 'Something went wrong' }
      before { allow_any_instance_of(CanvasLti::Egrades).to receive(:export_options).and_raise(RuntimeError, error_text) }
    end

    it_should_behave_like 'an authenticated endpoint' do
      let(:make_request) { get :export_options, canvas_course_id: 'embedded', :format => :csv }
    end

    it 'provides export options' do
      get :export_options, canvas_course_id: 'embedded'
      expect(response.status).to eq(200)
      json_response = JSON.parse(response.body)
      expect(json_response).to be_an_instance_of Hash

      official_sections = json_response['officialSections']
      expect(official_sections).to be_an_instance_of Array
      expect(official_sections.count).to eq 2
      expect(official_sections[0]['course_cntl_num']).to eq '22280'
      expect(official_sections[1]['course_cntl_num']).to eq '22345'

      expect(json_response['gradingStandardEnabled']).to eq false

      section_terms = json_response['sectionTerms']
      expect(section_terms).to be_an_instance_of Array
      expect(section_terms.count).to eq 2

      muted_assignments = json_response['mutedAssignments']
      expect(muted_assignments).to be_an_instance_of Array
    end

    it 'supports canvas course id parameter when absent in session' do
      session['canvas_course_id'] = nil
      get :export_options, canvas_course_id: canvas_course_id
      expect(response.status).to eq(200)
      json_response = JSON.parse(response.body)
      expect(json_response).to be_an_instance_of Hash
      official_sections = json_response['officialSections']
      expect(official_sections).to be_an_instance_of Array
      expect(official_sections.count).to eq 2
      expect(official_sections[0]['course_cntl_num']).to eq '22280'
      expect(official_sections[1]['course_cntl_num']).to eq '22345'
    end
  end

  describe 'when serving egrades download' do

    it_should_behave_like 'an endpoint' do
      let(:make_request) { get :download_egrades_csv, canvas_course_id: 'embedded', :format => :csv, :term_cd => 'D', :term_yr => '2014', :ccn => '1234', :type => 'final' }
      let(:error_text) { 'Something went wrong' }
      before { allow_any_instance_of(CanvasLti::Egrades).to receive(:official_student_grades_csv).and_raise(RuntimeError, error_text) }
    end

    it_should_behave_like 'an authenticated endpoint' do
      let(:make_request) { get :download_egrades_csv, canvas_course_id: 'embedded', :format => :csv, :term_cd => 'D', :term_yr => '2014', :ccn => '1234' }
    end

    context 'when the canvas course id is not present in the session' do
      before { session['canvas_course_id'] = nil }
      it 'returns 403 error' do
        get :download_egrades_csv, canvas_course_id: 'embedded', :format => :csv, :term_cd => 'D', :term_yr => '2014', :ccn => '1234'
        expect(response.status).to eq(403)
        expect(response.body).to eq " "
      end
    end

    context 'when user is not authorized to download egrades csv' do
      before { allow_any_instance_of(Canvas::CoursePolicy).to receive(:can_export_grades?).and_return(false) }
      it 'returns 403 error' do
        get :download_egrades_csv, canvas_course_id: 'embedded', :format => :csv, :term_cd => 'D', :term_yr => '2014', :ccn => '1234'
        expect(response.status).to eq(403)
        expect(response.body).to eq ' '
      end
    end

    context 'when user is authorized to download egrades csv' do
      let(:csv_string) { "uid,grade,comment\n872584,F,\"\"\n4000123,B,\"\"\n872527,A+,\"\"\n872529,D-,\"\"\n" }
      before { allow_any_instance_of(CanvasLti::Egrades).to receive(:official_student_grades_csv).and_return(csv_string) }
      it 'raises exception if term code not provided' do
        get :download_egrades_csv, canvas_course_id: 'embedded', :format => :csv, :term_yr => '2014', :ccn => '1234', :type => 'final'
        expect(response.status).to eq(400)
        json_response = JSON.parse(response.body)
        expect(json_response['error']).to eq 'term_cd required'
      end

      it 'raises exception if term year not provided' do
        get :download_egrades_csv, canvas_course_id: 'embedded', :format => :csv, :term_cd => 'D', :ccn => '1234', :type => 'final'
        expect(response.status).to eq(400)
        json_response = JSON.parse(response.body)
        expect(json_response['error']).to eq 'term_yr required'
      end

      it 'raises exception if course control number not provided' do
        get :download_egrades_csv, canvas_course_id: 'embedded', :format => :csv, :term_cd => 'D', :term_yr => '2014', :type => 'final'
        expect(response.status).to eq(400)
        json_response = JSON.parse(response.body)
        expect(json_response['error']).to eq 'ccn required'
      end

      it 'raises exception if type not provided' do
        get :download_egrades_csv, canvas_course_id: 'embedded', :format => :csv, :term_cd => 'D', :term_yr => '2014', :ccn => '1234'
        expect(response.status).to eq(400)
        json_response = JSON.parse(response.body)
        expect(json_response['error']).to eq 'type required'
      end

      it 'serves egrades csv file download' do
        get :download_egrades_csv, canvas_course_id: 'embedded', :format => :csv, :term_cd => 'D', :term_yr => '2014', :ccn => '1234', :type => 'current'
        expect(response.status).to eq(200)
        expect(response.headers['Content-Type']).to eq 'text/csv'
        expect(response.headers['Content-Disposition']).to eq 'attachment; filename=egrades-current-1234-Fall-2014-1164764.csv'
        expect(response.body).to be_an_instance_of String
        response_csv = CSV.parse(response.body, {headers: true})
        expect(response_csv.count).to eq 4
        response_csv.each do |user_grade|
          expect(user_grade).to be_an_instance_of CSV::Row
          expect(user_grade['uid']).to be_an_instance_of String
          expect(user_grade['grade']).to be_an_instance_of String
          expect(user_grade['comment']).to be_an_instance_of String
        end

        expect(response_csv[0]['uid']).to eq '872584'
        expect(response_csv[0]['grade']).to eq 'F'
        expect(response_csv[0]['comment']).to eq ''

        expect(response_csv[1]['uid']).to eq '4000123'
        expect(response_csv[1]['grade']).to eq 'B'
        expect(response_csv[1]['comment']).to eq ''

        expect(response_csv[2]['uid']).to eq '872527'
        expect(response_csv[2]['grade']).to eq 'A+'
        expect(response_csv[2]['comment']).to eq ''

        expect(response_csv[3]['uid']).to eq '872529'
        expect(response_csv[3]['grade']).to eq 'D-'
        expect(response_csv[3]['comment']).to eq ''
      end

      it 'supports canvas course id parameter when absent in session' do
        session['canvas_course_id'] = nil
        get :download_egrades_csv, canvas_course_id: canvas_course_id, :format => :csv, :term_cd => 'D', :term_yr => '2014', :ccn => '1234', :type => 'current'
        expect(response.status).to eq(200)
        expect(response.headers['Content-Type']).to eq 'text/csv'
        expect(response.headers['Content-Disposition']).to eq 'attachment; filename=egrades-current-1234-Fall-2014-1164764.csv'
        expect(response.body).to be_an_instance_of String
        response_csv = CSV.parse(response.body, {headers: true})
        expect(response_csv.count).to eq 4
      end
    end

  end

  describe 'when indicating if a course site has official sections' do
    before do
      session['user_id'] = nil
      allow_any_instance_of(CanvasLti::OfficialCourse).to receive(:is_official_course?).and_return(true)
    end
    it_should_behave_like 'an endpoint' do
      let(:make_request) { get :is_official_course, :format => :csv, :canvas_course_id => '1234' }
      let(:error_text) { 'Something went wrong' }
      before { allow(CanvasLti::OfficialCourse).to receive(:new).and_raise(RuntimeError, error_text) }
    end

    context 'when the canvas course id is not present in the params' do
      it 'returns 403 error' do
        get :is_official_course, :format => :csv
        expect(response.status).to eq(403)
        expect(response.body).to eq ' '
      end
    end

    it 'should set cross origin access control headers' do
      get :is_official_course, :format => :csv, :canvas_course_id => '1234'
      expect(response.header['Access-Control-Allow-Origin']).to eq "#{Settings.canvas_proxy.url_root}"
      expect(response.header['Access-Control-Allow-Origin']).to_not eq '*'
      expect(response.header['Access-Control-Allow-Methods']).to eq 'GET'
      expect(response.header['Access-Control-Max-Age']).to eq '86400'
    end

    it 'indicates if the course site has official sections' do
      get :is_official_course, :format => :csv, :canvas_course_id => '1234'
      expect(response.status).to eq(200)
      json_response = JSON.parse(response.body)
      expect(json_response).to be_an_instance_of Hash
      expect(json_response['isOfficialCourse']).to eq true
    end
  end

end
