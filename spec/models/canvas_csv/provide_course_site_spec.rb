describe CanvasCsv::ProvideCourseSite do

  let(:uid)   { rand(99999).to_s }
  let(:site_name) { 'Introduction to Computer Programming for Scientists and Engineers' }
  let(:site_course_code) { 'ENGIN 7 - LEC 001' }
  let(:sis_course_id) { 'CRS:ENGIN-7-2015-B' }
  let(:canvas_course_id) { rand(99999).to_s }
  let(:current_terms) { [
    {:yr=>'2014', :cd=>'D', :slug=>'fall-2014', :name=>'Fall 2014'},
    {:yr=>'2015', :cd=>'B', :slug=>'spring-2015', :name=>'Spring 2015'},
  ] }
  subject { CanvasCsv::ProvideCourseSite.new uid }

  it_should_behave_like 'a background job worker'

  #####################################
  # Instance Methods

  describe '#initialize' do
    it 'raises exception if uid is not a String' do
      expect { CanvasCsv::ProvideCourseSite.new(1234) }.to raise_error(ArgumentError, 'uid must be a String')
    end
    its(:uid)       { should eq uid }
    it 'initializes the import data hash' do
      expect(subject.instance_eval { @import_data }).to eq({})
    end
  end

  describe '#create_course_site' do
    let(:task_steps){ [
      :prepare_users_courses_list,
      :identify_department_subaccount,
      :prepare_course_site_definition,
      :prepare_section_definitions,
      :import_course_site,
      :retrieve_course_site_details,
      :import_sections,
      :add_instructor_to_section,
      :expire_instructor_sites_cache,
      :import_enrollments_in_background,
    ] }
    before do
      allow(subject).to receive(:current_terms).and_return(current_terms)
      task_steps.each do |step|
        allow(subject).to receive(step)
      end
    end

    it 'raises error if term slug does not match current term' do
      expect { subject.create_course_site(site_name, site_course_code, 'fall-5429', ['1136', '1204']) }.to raise_error(RuntimeError, 'term_slug does not match a current term')
      cached_object = BackgroundJob.find(subject.background_job_id)
      expect(cached_object.background_job_report[:jobStatus]).to eq 'Error'
      expect(cached_object.background_job_report[:errors]).to eq ['term_slug does not match a current term']
    end

    it 'intercepts raised exceptions and updates status' do
      allow(subject).to receive(:import_course_site).and_raise(RuntimeError, 'Course site could not be created!')
      expect { subject.create_course_site(site_name, site_course_code, 'fall-2014', ['1136', '1204']) }.to raise_error(RuntimeError, 'Course site could not be created!')
      cached_object = BackgroundJob.find(subject.background_job_id)
      expect(cached_object.background_job_report[:jobStatus]).to eq 'Error'
      expect(cached_object.background_job_report[:errors]).to eq ['Course site could not be created!']
    end

    it 'sets term and ccns for import' do
      subject.create_course_site(site_name, site_course_code, 'fall-2014', ['21136', '21204'])
      expect(subject.instance_eval { @import_data['term_slug'] }).to eq 'fall-2014'
      expect(subject.instance_eval { @import_data['term'][:yr] }).to eq '2014'
      expect(subject.instance_eval { @import_data['term'][:cd] }).to eq 'D'
      expect(subject.instance_eval { @import_data['ccns'] }).to eq ['21136', '21204']
    end

    it 'sets job type to course_creation' do
      subject.create_course_site(site_name, site_course_code, 'fall-2014', ['21136', '21204'])
      cached_object = BackgroundJob.find(subject.background_job_id)
      expect(cached_object.background_job_report[:jobType]).to eq 'course_creation'
    end

    context 'when provisioning as instructor' do
      it 'makes calls to each step of import in proper order' do
        task_steps.each {|step| expect(subject).to receive(step).ordered}
        subject.create_course_site(site_name, site_course_code, 'fall-2014', ['1136', '1204'])
      end
      it 'calculates 10 total steps to completion' do
        subject.create_course_site(site_name, site_course_code, 'fall-2014', ['1136', '1204'])
        expect(subject.instance_eval { @background_job_total_steps }).to eq 10
      end
    end

    context 'when provisioning by ccn' do
      before do
        task_steps.delete(:add_instructor_to_section)
        task_steps.delete(:expire_instructor_sites_cache)
      end
      it 'makes calls to each step of import except adding instructor and breaking cache' do
        task_steps.each {|step| expect(subject).to receive(step).ordered}
        expect(subject).to_not receive(:add_instructor_to_section)
        expect(subject).to_not receive(:expire_instructor_sites_cache)
        subject.create_course_site(site_name, site_course_code, 'fall-2014', ['1136', '1204'], true)
      end
      it 'calculates 8 total steps to completion' do
        subject.create_course_site(site_name, site_course_code, 'fall-2014', ['1136', '1204'], true)
        expect(subject.instance_eval { @background_job_total_steps }).to eq 8
      end
    end
  end

  describe '#edit_sections' do
    let(:course_site_term) { {term_yr: '2015', term_cd: 'B'} }
    let(:canvas_course_info) do
      {
        canvasCourseId: canvas_course_id,
        term: course_site_term,
        'sis_course_id' => random_id
      }
    end
    let(:ccns_to_remove) { [random_ccn] }
    let(:ccns_to_add) { [random_ccn] }
    let(:task_steps){ [
      :prepare_users_courses_list,
      :prepare_section_definitions,
      :prepare_section_deletions,
      :import_sections,
      :refresh_sections_cache,
      :import_enrollments_in_background
    ] }
    before do
      allow(subject).to receive(:current_terms).and_return(current_terms)
      task_steps.each do |step|
        allow(subject).to receive(step)
      end
    end
    context 'when all goes well' do
      before do
        # The class isn't particularly test-friendly.
        allow(subject).to receive(:section_definitions).and_return(ccns_to_add)
        task_steps.each do |step|
          allow(subject).to receive(step).ordered
        end
      end
      it 'sets job type to edit_sections' do
        subject.edit_sections(canvas_course_info, ccns_to_remove, ccns_to_add)
        cached_object = BackgroundJob.find(subject.background_job_id)
        expect(cached_object.background_job_report[:jobType]).to eq 'edit_sections'
      end
      context 'when adding sections to course site' do
        let(:ccns_to_remove) { [] }
        before do
          task_steps.delete(:prepare_section_deletions)
        end
        it 'executes all steps in order' do
          task_steps.each {|step| expect(subject).to receive(step).ordered}
          expect(subject).to_not receive(:prepare_section_deletions)
          subject.edit_sections(canvas_course_info, ccns_to_remove, ccns_to_add)
        end
        it 'calculates 5 total steps to completion' do
          subject.edit_sections(canvas_course_info, ccns_to_remove, ccns_to_add)
          expect(subject.instance_eval { @background_job_total_steps }).to eq 5
        end
      end
      context 'when removing sections from course site' do
        let(:ccns_to_add) { [] }
        before do
          allow(subject).to receive(:section_definitions).and_return(ccns_to_remove)
          task_steps.delete(:prepare_users_courses_list)
          task_steps.delete(:prepare_section_definitions)
        end
        it 'executes all steps in order' do
          task_steps.each {|step| expect(subject).to receive(step).ordered}
          expect(subject).to_not receive(:prepare_users_courses_list)
          expect(subject).to_not receive(:prepare_section_definitions)
          subject.edit_sections(canvas_course_info, ccns_to_remove, ccns_to_add)
        end
        it 'calculates 4 total steps to completion' do
          subject.edit_sections(canvas_course_info, ccns_to_remove, ccns_to_add)
          expect(subject.instance_eval { @background_job_total_steps }).to eq 4
        end
      end
      context 'when adding and removing sections from course site' do
        it 'executes all steps in order' do
          task_steps.each {|step| expect(subject).to receive(step).ordered}
          subject.edit_sections(canvas_course_info, ccns_to_remove, ccns_to_add)
        end
        it 'calculates 6 total steps to completion' do
          subject.edit_sections(canvas_course_info, ccns_to_remove, ccns_to_add)
          expect(subject.instance_eval { @background_job_total_steps }).to eq 6
        end
      end
    end
    context 'on unexpected error' do
      before do
        allow(subject).to receive(:prepare_section_deletions).and_raise(RuntimeError, 'Unable to remove memberships')
      end
      it 'returns a proper message' do
        expect {subject.edit_sections(canvas_course_info, ccns_to_remove, ccns_to_add) }.to raise_error(RuntimeError, 'Unable to remove memberships')
        cached_object = BackgroundJob.find(subject.background_job_id)
        expect(cached_object.background_job_report[:jobType]).to eq 'edit_sections'
        expect(cached_object.background_job_report[:jobStatus]).to eq 'Error'
        expect(cached_object.background_job_report[:errors]).to eq ['Unable to remove memberships']
      end
    end
    context 'when no changes would be made' do
      before do
        allow(subject).to receive(:section_definitions).and_return([])
      end
      it 'reports an error' do
        expect {subject.edit_sections(canvas_course_info, ccns_to_remove, ccns_to_add) }.to raise_error(RuntimeError, 'No changes to sections requested')
        cached_object = BackgroundJob.find(subject.background_job_id)
        expect(cached_object.background_job_report[:jobStatus]).to eq 'Error'
      end
    end
    context 'if the course site is not in a current term' do
      let(:course_site_term) { {term_yr: '2014', term_cd: 'B'} }
      it 'reports an error' do
        expect {subject.edit_sections(canvas_course_info, ccns_to_remove, ccns_to_add) }.to raise_error(RuntimeError, "Course site #{canvas_course_id} does not match a current term")
        cached_object = BackgroundJob.find(subject.background_job_id)
        expect(cached_object.background_job_report[:jobStatus]).to eq 'Error'
      end
    end
  end

  describe '#prepare_users_courses_list' do
    before do
      subject.instance_eval do
        @import_data['term_slug'] = 'fall-2013'
        @import_data['term'] = {yr: '2013', cd: 'D'}
        @import_data['ccns'] = ['21136', '21204']
      end
      @filtered_courses_list = [
        {
          :course_code=>'COMPSCI 10',
          :dept=>'COMPSCI',
          :slug=>'compsci-10',
          :title=>'The Beauty and Joy of Computing',
          :role=>'Instructor',
          :sections=>[
            {:ccn=>'21136', :instruction_format=>'DIS', :is_primary_section=>false, :section_label=>'DIS 102', :section_number=>'102', :schedules=>[{:buildingName=>'SODA', :room_number=>'0320', :schedule=>'M 8:00A-9:00A'}], :instructors=>[{:name=>'Seth Mark Beckley', :uid=>'937403'}]},
            {:ccn=>'21204', :instruction_format=>'DIS', :is_primary_section=>false, :section_label=>'DIS 109', :section_number=>'109', :schedules=>[{:buildingName=>'SODA', :room_number=>'0320', :schedule=>'M 12:00P-1:00P'}], :instructors=>[{:name=>'Seth Mark Beckley', :uid=>'937403'}]}
          ]
        }
      ]
    end

    it 'raises exception if term slug not present in import data set' do
      subject.instance_eval do
        @import_data['term_slug'] = nil
        @import_data['term'] = nil
      end
      expect { subject.prepare_users_courses_list }.to raise_error(RuntimeError, 'Unable to prepare course list. Term code not present.')
    end

    it 'raises exception if course control numbers are not present in import data set' do
      subject.instance_eval { @import_data['ccns'] = nil }
      expect { subject.prepare_users_courses_list }.to raise_error(RuntimeError, 'Unable to prepare course list. CCNs not present.')
    end

    it 'assigns user courses set to import data hash' do
      allow(subject).to receive(:candidate_courses_list).and_return(true)
      expect(subject).to receive(:filter_courses_by_ccns).and_return(@filtered_courses_list)
      subject.prepare_users_courses_list
      assigned_courses = subject.instance_eval { @import_data['courses'] }
      expect(assigned_courses).to be_an_instance_of Array
      expect(assigned_courses.count).to eq 1
      expect(assigned_courses[0]).to be_an_instance_of Hash
      expect(assigned_courses[0][:course_code]).to eq 'COMPSCI 10'
    end

    it 'lets admins specify CCNs directly' do
      fake_formatter = instance_double(MyAcademics::Teaching)
      expect(MyAcademics::Teaching).to receive(:new).with(uid).and_return(fake_formatter)
      expect(fake_formatter).to receive(:courses_list_from_ccns).and_return(@filtered_courses_list)
      subject.instance_eval { @import_data['is_admin_by_ccns'] = true }
      expect(subject).to_not receive(:candidate_courses_list)
      expect(subject).to_not receive(:filter_courses_by_ccns)
      subject.prepare_users_courses_list
    end

    it 'updates completed steps list' do
      allow(subject).to receive(:candidate_courses_list).and_return(true)
      expect(subject).to receive(:filter_courses_by_ccns).and_return('user_courses_list')
      subject.prepare_users_courses_list
      cached_object = BackgroundJob.find(subject.background_job_id)
      expect(cached_object.background_job_report[:completedSteps]).to eq ['Prepared courses list']
    end
  end

  describe '#identify_department_subaccount' do
    before do
      allow(subject).to receive(:subaccount_for_department).and_return('ACCT:COMPSCI')
      subject.instance_eval { @import_data['courses'] = [{:course_code => 'ENGIN 7', :dept => 'COMPSCI', :sections => []}] }
    end

    it 'raises exception if import courses not present' do
      subject.instance_eval { @import_data['courses'] = nil }
      expect { subject.identify_department_subaccount }.to raise_error(RuntimeError, 'Unable identify department subaccount. Course list not loaded or empty.')
    end

    it 'adds department id to import data' do
      subject.identify_department_subaccount
      expect(subject.instance_eval { @import_data['subaccount'] }).to eq 'ACCT:COMPSCI'
    end

    it 'updates completed steps list' do
      subject.identify_department_subaccount
      cached_object = BackgroundJob.find(subject.background_job_id)
      expect(cached_object.background_job_report[:completedSteps]).to eq ['Identified department sub-account']
    end
  end

  describe '#prepare_course_site_definition' do
    before do
      subject.instance_eval do
        @import_data['term'] = {yr: '2013', cd: 'D', slug: 'fall-2013'}
        @import_data['subaccount'] = 'ACCT:COMPSCI'
        @import_data['courses'] = [
          {
            :course_code => 'MEC ENG 98',
            :slug => 'mec_eng-98',
            :dept => 'MEC ENG',
            :title => 'Supervised Independent Group Studies',
            :role => 'Instructor',
            :sections => [
              { :ccn => rand(99999).to_s, :instruction_format => 'GRP', :is_primary_section => true, :section_label => 'GRP 015', :section_number => '015' }
            ]
          }
        ]
      end
      course_site_definition = {
        'course_id' => 'CRS:MEC_ENG-98-2013-D',
        'short_name' => 'MEC ENG 98 GRP 015',
        'long_name' => 'Supervised Independent Group Studies',
        'account_id' => 'ACCT:COMPSCI',
        'term_id' => 'TERM:2013-D',
        'status' => 'active',
      }
      allow(subject).to receive(:generate_course_site_definition).and_return(course_site_definition)
    end

    it 'raises exception if course term is not present' do
      subject.instance_eval { @import_data['term'] = nil }
      expect { subject.prepare_course_site_definition }.to raise_error(RuntimeError, 'Unable to prepare course site definition. Term data is not present.')
    end

    it 'raises exception if department subaccount is not present' do
      subject.instance_eval { @import_data['subaccount'] = nil }
      expect { subject.prepare_course_site_definition }.to raise_error(RuntimeError, 'Unable to prepare course site definition. Department subaccount ID not present.')
    end

    it 'raises exception if courses list is not present' do
      subject.instance_eval { @import_data['courses'] = nil }
      expect { subject.prepare_course_site_definition }.to raise_error(RuntimeError, 'Unable to prepare course site definition. Courses list is not present.')
    end

    it 'populates the course site definition' do
      subject.prepare_course_site_definition
      course_site_definition = subject.instance_eval { @import_data['course_site_definition'] }
      expect(course_site_definition).to be_an_instance_of Hash
      expect(course_site_definition['status']).to eq 'active'
      expect(course_site_definition['course_id']).to eq 'CRS:MEC_ENG-98-2013-D'
      expect(course_site_definition['account_id']).to eq 'ACCT:COMPSCI'
      expect(course_site_definition['term_id']).to eq 'TERM:2013-D'
      expect(course_site_definition['short_name']).to eq 'MEC ENG 98 GRP 015'
      expect(course_site_definition['long_name']).to eq 'Supervised Independent Group Studies'
    end

    it 'sets the sis course id' do
      subject.prepare_course_site_definition
      sis_course_id = subject.instance_eval { @import_data['sis_course_id'] }
      expect(sis_course_id).to eq 'CRS:MEC_ENG-98-2013-D'
    end

    it 'sets the course site short name' do
      subject.prepare_course_site_definition
      course_site_short_name = subject.instance_eval { @import_data['course_site_short_name'] }
      expect(course_site_short_name).to eq 'MEC ENG 98 GRP 015'
    end

    it 'updates completed steps list' do
      subject.prepare_course_site_definition
      cached_object = BackgroundJob.find(subject.background_job_id)
      expect(cached_object.background_job_report[:completedSteps]).to eq ['Prepared course site definition']
    end
  end

  describe '#prepare_section_definitions' do
    before do
      subject.instance_eval do
        @import_data['term'] = {yr: '2013', cd: 'D', slug: 'fall-2013'}
        @import_data['sis_course_id'] = 'CRS:MEC_ENG-98-2013-D'
        @import_data['courses'] = [
          {
            :course_code => 'MEC ENG 98',
            :slug => 'mec_eng-98',
            :dept => 'MEC ENG',
            :title => 'Supervised Independent Group Studies',
            :role => 'Instructor',
            :sections => [{
              ccn: '12345',
              instruction_format: 'GRP',
              instructors: [{uid: uid, instructor_func: 1}],
              is_primary_section: true,
              section_label: 'GRP 015',
              section_number: '015'
            }]
          }
        ]
      end
      section_definitions = [{'name' => 'MEC ENG 98 GRP 015', 'course_id' => 'CRS:MEC_ENG-98-2013-D', 'section_id' => 'SEC:2013-D-12345', 'status' => 'active'}]
      allow(subject).to receive(:generate_section_definitions).and_return(section_definitions)
    end

    it 'raises exception if course term is not present' do
      subject.instance_eval { @import_data['term'] = nil }
      expect { subject.prepare_section_definitions }.to raise_error(RuntimeError, 'Unable to prepare section definitions. Term data is not present.')
    end

    it 'raises exception if SIS course ID is not present' do
      subject.instance_eval { @import_data['sis_course_id'] = nil }
      expect { subject.prepare_section_definitions }.to raise_error(RuntimeError, 'Unable to prepare section definitions. SIS Course ID is not present.')
    end

    it 'raises exception if courses list is not present' do
      subject.instance_eval { @import_data['courses'] = nil }
      expect { subject.prepare_section_definitions }.to raise_error(RuntimeError, 'Unable to prepare section definitions. Courses list is not present.')
    end

    it 'populates the section definitions' do
      subject.prepare_section_definitions
      section_definitions = subject.section_definitions
      expect(section_definitions).to be_an_instance_of Array
      expect(section_definitions[0]['status']).to eq 'active'
      expect(section_definitions[0]['name']).to eq 'MEC ENG 98 GRP 015'
      expect(section_definitions[0]['course_id']).to eq 'CRS:MEC_ENG-98-2013-D'
      expect(section_definitions[0]['section_id']).to eq 'SEC:2013-D-12345'
    end

    it 'updates completed steps list' do
      subject.prepare_section_definitions
      cached_object = BackgroundJob.find(subject.background_job_id)
      expect(cached_object.background_job_report[:completedSteps]).to eq ['Prepared section definitions']
    end
  end

  describe '#import_course_site' do
    let(:canvas_sis_import_proxy) { double }
    before do
      @course_row = {'course_id'=>'CRS:COMPSCI-47A-2013-D', 'short_name'=>'COMPSCI 47A SLF 001', 'long_name'=>'Completion of Work in Computer Science 61A', 'account_id'=>'ACCT:COMPSCI', 'term_id'=>'TERM:2013-D', 'status'=>'active'}
      allow(canvas_sis_import_proxy).to receive(:import_courses).and_return(true)
      allow(subject).to receive(:make_courses_csv).and_return('/csv/filepath')
    end

    it 'raises exception if course site import fails' do
      allow(canvas_sis_import_proxy).to receive(:import_courses).and_return(nil)
      allow(Canvas::SisImport).to receive(:new).and_return(canvas_sis_import_proxy)
      expect { subject.import_course_site(@course_row) }.to raise_error(RuntimeError, 'Course site could not be created.')
    end

    it 'sets sections csv file path' do
      allow(Canvas::SisImport).to receive(:new).and_return(canvas_sis_import_proxy)
      subject.import_course_site(@course_row)
      filepath = subject.instance_eval { @import_data['courses_csv_file'] }
      expect(filepath).to eq '/csv/filepath'
    end

    it 'updates completed steps list' do
      allow(Canvas::SisImport).to receive(:new).and_return(canvas_sis_import_proxy)
      subject.import_course_site(@course_row)
      cached_object = BackgroundJob.find(subject.background_job_id)
      expect(cached_object.background_job_report[:completedSteps]).to eq ['Imported course']
    end
  end

  describe '#import_sections' do
    let(:canvas_sis_import_proxy) { double }
    before do
      @section_rows = [
        {'section_id'=>'SEC:2013-D-26178', 'course_id'=>'CRS:COMPSCI-47A-2013-D', 'name'=>'COMPSCI 47A SLF 001', 'status'=>'active'},
        {'section_id'=>'SEC:2013-D-26181', 'course_id'=>'CRS:COMPSCI-47A-2013-D', 'name'=>'COMPSCI 47B SLF 001', 'status'=>'active'}
      ]
      allow(canvas_sis_import_proxy).to receive(:import_sections).and_return(true)
      allow(subject).to receive(:make_sections_csv).and_return('/csv/filepath')
    end

    it 'raises exception if section imports fails' do
      allow(canvas_sis_import_proxy).to receive(:import_sections).and_return(nil)
      allow(Canvas::SisImport).to receive(:new).and_return(canvas_sis_import_proxy)
      expect { subject.import_sections(@section_rows) }.to raise_error(RuntimeError, 'Course site was created without any sections or members! Section import failed.')
    end

    it 'sets sections csv file path' do
      allow(Canvas::SisImport).to receive(:new).and_return(canvas_sis_import_proxy)
      subject.import_sections(@section_rows)
      filepath = subject.instance_eval { @import_data['sections_csv_file'] }
      expect(filepath).to eq '/csv/filepath'
    end

    it 'updates completed steps list' do
      allow(Canvas::SisImport).to receive(:new).and_return(canvas_sis_import_proxy)
      subject.import_sections(@section_rows)
      cached_object = BackgroundJob.find(subject.background_job_id)
      expect(cached_object.background_job_report[:completedSteps]).to eq ['Imported sections']
    end
  end

  describe '#add_instructor_to_section' do
    let(:section_id) {random_id}
    let(:section_definition) do
      {'section_id' => section_id}
    end
    let(:teacher_role_id) { 1773 }
    before { subject.instance_eval { @import_data['canvas_course_id'] = 99999 } }
    it 'adds teacher enrollments to section' do
      expect_any_instance_of(CanvasLti::CourseAddUser).to receive(:add_user_to_course_section).with(uid, teacher_role_id,
        "sis_section_id:#{section_id}").and_return({'role' => 'TeacherEnrollment', 'role_id' => teacher_role_id})
      subject.add_instructor_to_section section_definition
    end
  end

  describe '#retrieve_course_site_details' do
    context 'when SIS course ID is not present' do
      before { subject.instance_eval { @import_data['sis_course_id'] = nil } }
      it 'raises exception' do
        expect { subject.retrieve_course_site_details }.to raise_error(RuntimeError, 'Unable to retrieve course site details. SIS Course ID not present.')
      end
    end

    context 'when SIS course ID is present' do
      before { subject.instance_eval { @import_data['sis_course_id'] = 'CRS:ENGIN-7-2015-B' } }
      let(:sis_course_proxy) { Canvas::SisCourse.new }
      before do
        expect(Canvas::SisCourse).to receive(:new).with(sis_course_id: sis_course_id).and_return sis_course_proxy
        expect(sis_course_proxy).to receive(:canvas_course_id).and_return canvas_course_id
        allow(Settings.canvas_proxy).to receive(:url_root).and_return 'https://berkeley.instructure.com'
      end

      context 'on no response from Canvas::SisCourse' do
        let(:canvas_course_id) { nil }
        it 'raises exception' do
          expect { subject.retrieve_course_site_details }.to raise_error(RuntimeError, 'Unexpected error obtaining course site URL for CRS:ENGIN-7-2015-B')
        end
      end

      it 'sets course site URL' do
        subject.retrieve_course_site_details
        expect(subject.instance_eval { @import_data['course_site_url'] }).to eq "https://berkeley.instructure.com/courses/#{canvas_course_id}"
      end

      it 'updates completed steps list' do
        subject.retrieve_course_site_details
        cached_object = BackgroundJob.find(subject.background_job_id)
        expect(cached_object.background_job_report[:completedSteps]).to eq ['Retrieved new course site details']
      end
    end
  end

  describe '#expire_instructor_sites_cache' do
    it 'clears canvas course site cache for user/instructor' do
      expect(Canvas::MergedUserSites).to receive(:expire).with(subject.uid).and_return(nil)
      subject.expire_instructor_sites_cache
    end

    it 'updates completed steps list' do
      subject.expire_instructor_sites_cache
      cached_object = BackgroundJob.find(subject.background_job_id)
      expect(cached_object.background_job_report[:completedSteps]).to eq ['Clearing bCourses course site cache']
    end
  end

  describe '#import_enrollments_in_background' do
    let(:course_id) {random_id}
    let(:section_ids) {[random_id, random_id]}
    let(:section_definitions) {[{
      'section_id' => section_ids[0],
      'status' => 'active'
    }, {
      'section_id' => random_id,
      'status' => 'deleted'
      }, {
      'section_id' => section_ids[1],
      'status' => 'active'
    }]}
    let(:maintainer) {double}
    it 'should forward to a background job handler' do
      expect(CanvasCsv::SiteMembershipsMaintainer).to receive(:background).and_return(maintainer)
      expect(maintainer).to receive(:import_memberships).with(course_id, section_ids, anything, anything)
      subject.import_enrollments_in_background(course_id, section_definitions)
    end
  end

  describe '#current_terms' do
    let(:term_codes) do
      [
        double(year: 2013, code: 'C', slug: 'summer-2013', to_english: 'Summer 2013'),
        double(year: 2013, code: 'D', slug: 'fall-2013', to_english: 'Fall 2013')
      ]
    end
    it 'returns array of term hashes' do
      expect(Canvas::Terms).to receive(:current_terms).and_return(term_codes)
      result = subject.current_terms
      expect(result).to be_an_instance_of Array
      expect(result.count).to eq 2
      expect(result[0]).to be_an_instance_of Hash
      expect(result[1]).to be_an_instance_of Hash
      expect(result[0][:yr]).to eq '2013'
      expect(result[1][:yr]).to eq '2013'
      expect(result[0][:cd]).to eq 'C'
      expect(result[1][:cd]).to eq 'D'
      expect(result[0][:slug]).to eq 'summer-2013'
      expect(result[1][:slug]).to eq 'fall-2013'
    end
  end

  describe '#candidate_courses_list' do
    it 'should raise exception if user id not initialized' do
      subject.instance_eval { @uid = nil }
      expect { subject.candidate_courses_list }.to raise_error(RuntimeError, 'User ID not found for candidate')
    end

    it 'should map academic semester data to ordered canvas terms' do
      fake_feed = {
        :teachingSemesters => [
          {name: 'Summer 2015', slug: 'summer-2015', termCode: 'C', termYear: '2015', timeBucket: 'future', classes: []},
          {name: 'Spring 2015', slug: 'spring-2015', termCode: 'B', termYear: '2015', timeBucket: 'future', classes: []},
          {name: 'Fall 2014', slug: 'fall-2014', termCode: 'D', termYear: '2014', timeBucket: 'current', classes: []},
        ]
      }
      allow(subject).to receive(:current_terms).and_return(current_terms)
      allow_any_instance_of(MyAcademics::Merged).to receive(:get_feed).and_return(fake_feed)
      result = subject.candidate_courses_list
      expect(result).to be_a Array
      expect(result.count).to eq 2
      expect(result[0][:slug]).to eq 'fall-2014'
      expect(result[0][:timeBucket]).to eq 'current'
      expect(result[0][:classes]).to eq []
      expect(result[1][:slug]).to eq 'spring-2015'
      expect(result[1][:timeBucket]).to eq 'future'
      expect(result[1][:classes]).to eq []
    end

    it 'should get properly formatted candidate course list from fake Oracle MV', :if => CampusOracle::Connection.test_data? do
      allow_any_instance_of(Bearfacts::Proxy).to receive(:lookup_student_id).and_return(nil)
      terms_feed = CanvasCsv::ProvideCourseSite.new('238382').candidate_courses_list
      expect(terms_feed.length).to eq 1
      expect(terms_feed[0][:name]).to eq 'Fall 2013'
      feed = terms_feed[0][:classes]
      expect(feed.length).to eq 2
      bio1a = feed.select {|course| course[:course_code] == 'BIOLOGY 1A'}[0]
      expect(bio1a.empty?).to be_falsey
      expect(bio1a[:sections].first[:courseCode]).to eq bio1a[:course_code]
      expect(bio1a[:title]).to eq 'General Biology Lecture'
      expect(bio1a[:role]).to eq 'Instructor'
      expect(bio1a[:dept]).to eq 'BIOLOGY'
      expect(bio1a[:sections].length).to eq 3
      expect(bio1a[:sections][0][:is_primary_section]).to be_truthy
      expect(bio1a[:sections][1][:is_primary_section]).to be_falsey
      expect(bio1a[:sections][2][:is_primary_section]).to be_falsey

      cogsci = feed.select {|course| course[:course_code] == 'COG SCI C147'}[0]
      expect(cogsci.empty?).to be_falsey
      expect(cogsci[:title]).to eq 'Language Disorders'
    end
  end

  describe '#filter_courses_by_ccns' do
    before do
      @selected_ccns = 3.times.map { random_ccn }
      @candidate_courses_list = [
        {
          :course_code => 'ENGIN 7',
          :slug => 'engin-7',
          :dept => 'COMPSCI',
          :title => 'Introduction to Computer Programming for Scientists and Engineers',
          :role => 'Instructor',
          :sections => [
            { :ccn => random_ccn, :instruction_format => 'LEC', :is_primary_section => true, :section_label => 'LEC 002', :section_number => '002' },
            { :ccn => @selected_ccns[2], :instruction_format => 'DIS', :is_primary_section => false, :section_label => 'DIS 102', :section_number => '102' }
          ]
        },
        {
          :course_code => 'MEC ENG 98',
          :slug => 'mec_eng-98',
          :dept => 'MEC ENG',
          :title => 'Supervised Independent Group Studies',
          :role => 'Instructor',
          :sections => [
            { :ccn => random_ccn, :instruction_format => 'GRP', :is_primary_section => true, :section_label => 'GRP 015', :section_number => '015' }
          ]
        },
        {
          :course_code => 'MEC ENG H194',
          :slug => 'mec_eng-h194',
          :dept => 'MEC ENG',
          :title => 'Honors Undergraduate Research',
          :role => 'Instructor',
          :sections => [
            { :ccn => @selected_ccns[1], :instruction_format => 'IND', :is_primary_section => true, :section_label => 'IND 015', :section_number => '015' }
          ]
        },
        {
          :course_code => 'MEC ENG 297',
          :slug => 'mec_eng-297',
          :dept => 'MEC ENG',
          :title => 'Engineering Field Studies',
          :role => 'Instructor',
          :sections => [
            { :ccn => random_ccn, :instruction_format => 'IND', :is_primary_section => true, :section_label => 'IND 024', :section_number => '024' }
          ]
        }
      ]
      @term_slug = 'fall-2013'
      @candidate_terms_list = [
          {
            name: 'Fall 2013',
            slug: @term_slug,
            termCode: 'D',
            termYear: '2013',
            classes: @candidate_courses_list
          }
      ]
    end

    describe '#filter_courses_by_ccns' do
      it 'should raise exception when term slug not found in courses list' do
        expect { subject.filter_courses_by_ccns(@candidate_terms_list, 'summer-2011', @selected_ccns) }.to raise_error(ArgumentError, 'No courses found!')
      end
      it 'should filter courses data by POSTed CCN selection' do
        expect(@selected_ccns.length).to eq 3
        filtered = subject.filter_courses_by_ccns(@candidate_terms_list, @term_slug, @selected_ccns)
        expect(filtered.length).to eq 2
        expect(filtered[0][:course_code]).to eq 'ENGIN 7'
        expect(filtered[0][:dept]).to eq 'COMPSCI'
        expect(filtered[0][:sections].length).to eq 1
        expect(filtered[0][:sections][0][:section_label]).to eq 'DIS 102'
        expect(filtered[1][:course_code]).to eq 'MEC ENG H194'
        expect(filtered[1][:dept]).to eq 'MEC ENG'
        expect(filtered[1][:sections].length).to eq 1
        expect(filtered[1][:sections][0][:section_label]).to eq 'IND 015'
      end
    end

  end

  describe '#generate_course_site_definition' do
    let(:term_yr)     { '2013' }
    let(:term_cd)     { 'D' }
    let(:subaccount)  { 'ACCT:ENGIN' }
    let(:campus_course_slug) { 'engin-7' }

    it 'should raise exception when sis course id fails to generate' do
      allow(subject).to receive(:generate_unique_sis_course_id).and_return(nil)
      expect do
        subject.generate_course_site_definition(site_name, site_course_code, term_yr, term_cd, subaccount, campus_course_slug)
      end.to raise_error(RuntimeError, 'Could not define new course site!')
    end

    it 'should generate a Course import CSV row for the selected courses' do
      allow_any_instance_of(Canvas::ExistenceCheck).to receive(:course_defined?).and_return(false)
      canvas_course = subject.generate_course_site_definition(site_name, site_course_code, term_yr, term_cd, subaccount, campus_course_slug)
      expect(canvas_course['course_id'].present?).to be_truthy
      expect(canvas_course['course_id']).to eq 'CRS:ENGIN-7-2013-D'
      expect(canvas_course['short_name']).to eq 'ENGIN 7 - LEC 001'
      expect(canvas_course['long_name']).to eq 'Introduction to Computer Programming for Scientists and Engineers'
      expect(canvas_course['account_id']).to eq 'ACCT:ENGIN'
      expect(canvas_course['term_id']).to eq 'TERM:2013-D'
      expect(canvas_course['status']).to eq 'active'
    end

    it 'should generate a unique Course SIS ID for the selected courses' do
      # RSpec does not currently redefine any_instance class stubs: http://stackoverflow.com/questions/18092601/rspec-any-instance-stub-does-not-restub-old-instances
      stub_existence_check = double
      expect(stub_existence_check).to receive(:course_defined?).and_return(false)
      allow(Canvas::ExistenceCheck).to receive(:new).and_return(stub_existence_check)

      first_canvas_course = subject.generate_course_site_definition(site_name, site_course_code, term_yr, term_cd, subaccount, campus_course_slug)
      first_course_sis_id = first_canvas_course['course_id']
      expect(stub_existence_check).to receive(:course_defined?).twice do |id|
        id == first_course_sis_id
      end
      second_canvas_course = subject.generate_course_site_definition(site_name, site_course_code, term_yr, term_cd, subaccount, campus_course_slug)
      second_course_sis_id = second_canvas_course['course_id']
      expect(second_course_sis_id.present?).to be_truthy
      expect(second_course_sis_id).to_not eq first_course_sis_id
    end
  end

  describe '#generate_unique_sis_course_id' do
    it 'should generate a standard sis course id when a canvas course does not already exist' do
      stub_existence_proxy = double
      allow(stub_existence_proxy).to receive(:course_defined?).and_return(false)
      result = subject.generate_unique_sis_course_id(stub_existence_proxy, 'eth_std-c73abc', '2015', 'F')
      result.should == 'CRS:ETH_STD-C73ABC-2015-F'
    end

    it 'should return a unique course id when a canvas course already exists' do
      # emulate course defined already twice
      stub_existence_proxy = double
      allow(stub_existence_proxy).to receive(:course_defined?).and_return(true, true, false)
      allow(SecureRandom).to receive(:hex).and_return('e2383290', '697c834e')
      result = subject.generate_unique_sis_course_id(stub_existence_proxy, 'eth_std-c73abc', '2015', 'F')
      expect(result).to eq 'CRS:ETH_STD-C73ABC-2015-F-697C834E'
    end

    it 'should raise exception if unique course id not generated after 20 attempts' do
      stub_existence_proxy = double
      course_defined_responses = (1..21).to_a.map {|e| true} # array of 20 true responses
      allow(stub_existence_proxy).to receive(:course_defined?).and_return(course_defined_responses)
      expect do
        subject.generate_unique_sis_course_id(stub_existence_proxy, 'eth_std-c73abc', '2015', 'F')
      end.to raise_error(CanvasCsv::ProvideCourseSite::IdNotUniqueException)
    end
  end

  describe '#generate_section_definitions' do
    let(:term_yr) { '2013' }
    let(:term_cd) { 'D' }
    let(:ccns) { 3.times.map { random_ccn } }
    let(:sis_course_id) { 'CRS:ENGIN-7-2013-D-8383' }
    let(:courses_list) do
      [{
        course_code: 'ENGIN 7',
        slug: 'engin-7',
        title: 'Introduction to Computer Programming for Scientists and Engineers',
        role: 'Instructor',
        sections: [{
          ccn: ccns[0],
          courseCode: 'ENGIN 7',
          instruction_format: 'LEC',
          instructors: [{uid: lecture_instructor_id, instructor_func: 1}],
          is_primary_section: true,
          section_label: 'LEC 002',
          section_number: '002'
        },
        {
          ccn: ccns[1],
          courseCode: 'ENGIN 7',
          instruction_format: 'DIS',
          instructors: [{uid: discussion_instructor_1_id, instructor_func: 1}, {uid: discussion_instructor_2_id, instructor_func: 1}],
          is_primary_section: false,
          section_label: 'DIS 102',
          section_number: '102'
        }]
      },
      {
        course_code: 'MEC ENG 98',
        slug: 'mec_eng-98',
        title: 'Supervised Independent Group Studies',
        role: 'Instructor',
        sections: [{
          ccn: ccns[2],
          courseCode: 'MEC ENG 98',
          instruction_format: 'GRP',
          instructors: [{uid: group_instructor_id, instructor_func: 1}],
          is_primary_section: true,
          section_label: 'GRP 015',
          section_number: '015'
        }]
      }]
    end
    [:lecture_instructor_id, :discussion_instructor_1_id, :discussion_instructor_2_id, :group_instructor_id].each do |id|
      let(id) { random_id }
    end
    before { allow_any_instance_of(Canvas::ExistenceCheck).to receive(:section_defined?).and_return(false) }

    it 'should raise exception if campus_section_data argument is empty' do
      expect do
        subject.generate_section_definitions('2013', 'D', 'CRS:ENGIN-7-2013-D', [])
      end.to raise_error(ArgumentError, '\'campus_section_data\' argument is empty')
    end

    it 'should generate Canvas Section import CSV rows for the selected courses' do
      canvas_sections_list = subject.generate_section_definitions(term_yr, term_cd, sis_course_id, courses_list)
      expect(canvas_sections_list.length).to eq 3
      canvas_sections_list.each do |row|
        expect(row['course_id']).to eq sis_course_id
        expect(row['status']).to eq 'active'
        campus_section = Canvas::Terms.sis_section_id_to_ccn_and_term(row['section_id'])
        expect(campus_section[:term_yr]).to eq term_yr
        expect(campus_section[:term_cd]).to eq term_cd
        expect(ccns.include?(campus_section[:ccn])).to be_truthy
      end
      expect(canvas_sections_list[0]['name']).to eq 'ENGIN 7 LEC 002'
      expect(canvas_sections_list[1]['name']).to eq 'ENGIN 7 DIS 102'
      expect(canvas_sections_list[2]['name']).to eq 'MEC ENG 98 GRP 015'
    end

    it 'should generate a unique parsable Section SIS ID for the selected sections' do
      term_yr = '2013'
      term_cd = 'D'
      ccn = random_ccn
      courses_list = [
          {:course_code => 'ENGIN 7',
           :dept => 'ENGIN',
           :slug => 'engin-7',
           :title =>
               'Introduction to Computer Programming for Scientists and Engineers',
           :role => 'Instructor',
           :sections =>
               [{:ccn => ccn,
                 :instructors => [{uid: uid, instructor_func: 1}],
                 :instruction_format => 'DIS',
                 :is_primary_section => false,
                 :section_label => 'DIS 102',
                 :section_number => '102'}]}
      ]
      sis_course_id = 'CRS:ENGIN-7-2013-D-8383'

      # RSpec does not currently redefine any_instance class stubs: http://stackoverflow.com/questions/18092601/rspec-any-instance-stub-does-not-restub-old-instances
      stub_existence_check = double
      expect(stub_existence_check).to receive(:section_defined?).and_return(false)
      allow(Canvas::ExistenceCheck).to receive(:new).and_return(stub_existence_check)

      first_canvas_section = subject.generate_section_definitions(term_yr, term_cd, sis_course_id, courses_list)[0]
      first_canvas_section_id = first_canvas_section['section_id']

      expect(stub_existence_check).to receive(:section_defined?).twice do |id|
        id == first_canvas_section_id
      end

      second_canvas_section = subject.generate_section_definitions(term_yr, term_cd, sis_course_id, courses_list)[0]
      second_canvas_section_id = second_canvas_section['section_id']
      expect(second_canvas_section_id.present?).to be_truthy
      expect(second_canvas_section_id).to_not eq first_canvas_section_id
      campus_section = Canvas::Terms.sis_section_id_to_ccn_and_term(second_canvas_section_id)
      expect(campus_section[:term_yr]).to eq term_yr
      expect(campus_section[:term_cd]).to eq term_cd
      expect(campus_section[:ccn]).to eq ccn
    end

    context 'site creator is not explicit instructor for any section' do
      it 'stores no explicitly instructed sections' do
        subject.generate_section_definitions(term_yr, term_cd, sis_course_id, courses_list)
        expect(subject.instance_eval { @import_data['explicit_sections_for_instructor'] }).to be_empty
      end
    end

    context 'site creator is explicit instructor for one section' do
      let(:discussion_instructor_2_id) { uid }
      it 'stores explicitly instructed section' do
        subject.generate_section_definitions(term_yr, term_cd, sis_course_id, courses_list)
        explicitly_instructed = subject.instance_eval { @import_data['explicit_sections_for_instructor'] }
        expect(explicitly_instructed).to have(1).item
        expect(explicitly_instructed.first).to include({
          'course_id' => sis_course_id,
          'name' => 'ENGIN 7 DIS 102',
          'status' => 'active'
        })
      end
    end
  end

  describe '#subaccount_for_department' do
    it 'should return the subaccount if it exists in Canvas' do
      allow_any_instance_of(Canvas::ExistenceCheck).to receive(:account_defined?).and_return(true)
      result = subject.subaccount_for_department('COMPSCI')
      expect(result).to eq 'ACCT:COMPSCI'
    end

    it 'should replace forwards slashes with underscores in the subaccount name' do
      allow_any_instance_of(Canvas::ExistenceCheck).to receive(:account_defined?).and_return(true)
      result = subject.subaccount_for_department('MALAY/I')
      expect(result).to eq 'ACCT:MALAY_I'
    end

    it 'should raise exception if the subaccount does not exist in Canvas' do
      allow_any_instance_of(Canvas::ExistenceCheck).to receive(:account_defined?).and_return(false)
      expect { subject.subaccount_for_department('COMPSCI') }.to raise_error(RuntimeError, 'Could not find bCourses account for department COMPSCI')
    end
  end

  describe '#find_term' do
    before(:each) do
      term_codes_array = [
        {yr: '3026', cd: 'C', slug: 'summer-3026'},
        {yr: '3026', cd: 'D', slug: 'fall-3026'},
        {yr: '3027', cd: 'B', slug: 'spring-3027'},
      ]
      allow(subject).to receive(:current_terms).and_return(term_codes_array)
    end

    it 'should return matching term code hash' do
      result = subject.find_term(:slug => 'spring-3027')
      expect(result[:yr]).to eq '3027'
      expect(result[:cd]).to eq 'B'
      expect(result[:slug]).to eq 'spring-3027'

      result = subject.find_term(:yr => '3026')
      expect(result[:yr]).to eq '3026'
      expect(result[:cd]).to eq 'C'
      expect(result[:slug]).to eq 'summer-3026'
    end

    it 'should return nil when no match' do
      result = subject.find_term(:yr => '3028')
      expect(result).to be_nil

      result = subject.find_term(:meat => 'hotdog')
      expect(result).to be_nil
    end
  end

  describe '#background_job_report' do
    before do
      subject.instance_eval { @background_job_type = 'course_creation'}
      subject.instance_eval { @background_job_status = 'error'}
      subject.instance_eval { @background_job_id = 'Canvas::BackgroundJob.1383330151057-67f4b934525501cb'}
      subject.instance_eval { @background_job_completed_steps = ['step1 description', 'step2 description']}
    end

    context 'when job status is completed' do
      it 'includes course site details' do
        subject.instance_eval do
          @background_job_status = 'Completed'
          @background_job_type = 'course_creation'
          @import_data['course_site_url'] = 'https://example.com/courses/999'
          @import_data['course_site_short_name'] = 'COMPSCI-10'
          background_job_save
        end
        cached_object = BackgroundJob.find(subject.background_job_id)
        expect(cached_object.background_job_report['courseSite']).to be_an_instance_of Hash
        expect(cached_object.background_job_report['courseSite'][:short_name]).to eq 'COMPSCI-10'
        expect(cached_object.background_job_report['courseSite'][:url]).to eq 'https://example.com/courses/999'
        expect(cached_object.background_job_report['error']).to_not be
      end
    end
  end

  describe '#refresh_sections_cache' do
    it 'clears course sections cache' do
      expect_any_instance_of(Canvas::CourseSections).to receive(:sections_list).with(true).and_return(statusCode: 200, body: [])
      allow(subject).to receive(:expire_instructor_sites_cache).and_return(nil)
      subject.refresh_sections_cache(canvas_course_id)
    end

    it 'clears instructors merged academics cache' do
      allow_any_instance_of(Canvas::CourseSections).to receive(:sections_list).and_return(statusCode: 200, body: [])
      expect(subject).to receive(:expire_instructor_sites_cache).and_return(nil)
      subject.refresh_sections_cache(canvas_course_id)
    end
  end
end
