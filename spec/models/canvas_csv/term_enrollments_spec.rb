describe CanvasCsv::TermEnrollments do

  let(:frozen_moment_in_time) { Time.at(1388563200) }
  let(:current_sis_term_ids) { ['TERM:2013-D', 'TERM:2014-B'] }
  let(:export_dir) { subject.instance_eval { @export_dir } }

  # Define example Section Report CSV response for stubbing
  let(:sections_report_csv_header_string) { 'canvas_section_id,section_id,canvas_course_id,course_id,name,status,start_date,end_date,canvas_account_id,account_id' }
  let(:sections_report_csv_string) do
    [
      sections_report_csv_header_string,
      '20,SEC:2014-D-25123,24,CRS:COMPSCI-9D-2014-D,COMPSCI 9D SLF 001,active,,,36,ACCT:COMPSCI',
      '19,SEC:2014-D-25124,24,CRS:COMPSCI-9D-2014-D,COMPSCI 9D SLF 002,active,,,36,ACCT:COMPSCI',
      '21,SEC:2014-D-25125,24,,COMPSCI 9D SLF 003,active,,,36,ACCT:COMPSCI',
      '22,,24,CRS:COMPSCI-9D-2014-D,COMPSCI 9D SLF 003,active,,,36,ACCT:COMPSCI',
    ].join("\n")
  end
  let(:sections_report_csv) { CSV.parse(sections_report_csv_string, :headers => :first_row) }
  let(:empty_sections_report_csv) { CSV.parse(sections_report_csv_header_string + "\n", :headers => :first_row) }

  # Define example Section Enrollment API responses for stubbing
  let(:section_enrollment1) do
    {'course_id'=>24, 'course_section_id'=>20, 'type'=>'StudentEnrollment', 'user_id'=>165, 'role'=>'StudentEnrollment', 'sis_import_id'=>185, 'sis_course_id'=>'CRS:COMPSCI-9D-2014-D', 'course_integration_id'=>nil, 'enrollment_state' => 'active', 'sis_section_id'=>'SEC:2014-D-25123', 'user'=>{ 'sis_login_id'=>'1000123', 'login_id'=>'1000123', 'sis_user_id'=>'24716840' }}
  end
  let(:section_enrollment2) do
    {'course_id'=>24, 'course_section_id'=>19, 'type'=>'StudentEnrollment', 'user_id'=>166, 'role'=>'StudentEnrollment', 'sis_import_id'=>nil, 'sis_course_id'=>'CRS:COMPSCI-9D-2014-D', 'course_integration_id'=>nil, 'enrollment_state' => 'active', 'sis_section_id'=>'SEC:2014-D-25124', 'user'=>{ 'sis_login_id'=>'1000124', 'login_id'=>'1000124', 'sis_user_id'=>'24716841' }}
  end
  let(:section_enrollment3) do
    {'course_id'=>24, 'course_section_id'=>21, 'type'=>'StudentEnrollment', 'user_id'=>167, 'role'=>'StudentEnrollment', 'sis_import_id'=>185, 'sis_course_id'=>'CRS:COMPSCI-9D-2014-D', 'course_integration_id'=>nil, 'enrollment_state' => 'active', 'sis_section_id'=>'SEC:2014-D-25125', 'user'=>{ 'sis_login_id'=>'1000125', 'login_id'=>'1000125', 'sis_user_id'=>'24716842' }}
  end
  let(:section_enrollment4) do
    {'course_id'=>24, 'course_section_id'=>22, 'type'=>'StudentEnrollment', 'user_id'=>168, 'role'=>'StudentEnrollment', 'sis_import_id'=>185, 'sis_course_id'=>'CRS:COMPSCI-9D-2014-D', 'course_integration_id'=>nil, 'enrollment_state' => 'active', 'sis_section_id'=>'SEC:2014-D-25126', 'user'=>{ 'sis_login_id'=>'1000126', 'login_id'=>'1000126', 'sis_user_id'=>'24716843' }}
  end

  # Define example CSV file contents for stubbing
  let(:section_enrollments_csv_header_string) { 'course_id,canvas_section_id,sis_section_id,canvas_user_id,sis_login_id,sis_user_id,role,sis_import_id,enrollment_state' }
  let(:term1_section_enrollments_csv_string) do
    [
      section_enrollments_csv_header_string,
      '1209,1412606,SEC:2014-C-25128,4906376,7977,115223,StudentEnrollment,101,active',
      '1209,1412606,SEC:2014-C-25128,4906377,7978,115224,StudentEnrollment,101,active',
      '1209,1412607,SEC:2014-C-25129,4906376,7977,115225,StudentEnrollment,,active',
      '1209,1412607,SEC:2014-C-25129,4906377,7978,115226,StudentEnrollment,101,active',
    ].join("\n")
  end
  let(:term2_section_enrollments_csv_string) do
    [
      section_enrollments_csv_header_string,
      '1278,1413864,SEC:2014-C-24111,4906376,7977,109812,StudentEnrollment,101,active',
      '1278,1413864,SEC:2014-C-24111,4906377,7978,109813,StudentEnrollment,101,active',
      '1278,1413865,SEC:2014-C-24112,4906376,7977,109814,StudentEnrollment,101,invited',
      '1278,1413865,SEC:2014-C-24112,4906377,7978,109815,StudentEnrollment,,active',
    ].join("\n")
  end
  let(:term1_sections_report_csv) { CSV.parse(term1_section_enrollments_csv_string, :headers => :first_row) }
  let(:term2_sections_report_csv) { CSV.parse(term2_section_enrollments_csv_string, :headers => :first_row) }

  let(:expected_term1_filepath) { "#{export_dir}/canvas-2014-01-01-TERM_2013-D-term-enrollments-export.csv" }
  let(:expected_term2_filepath) { "#{export_dir}/canvas-2014-01-01-TERM_2014-B-term-enrollments-export.csv" }

  before do
    allow(Canvas::Proxy).to receive(:current_sis_term_ids).and_return(['TERM:2013-D', 'TERM:2014-B'])

    # set static times for consistent testing output
    allow(Time).to receive(:now).and_return(frozen_moment_in_time)
    allow(DateTime).to receive(:now).and_return(frozen_moment_in_time.to_datetime)

    # stub behavior for Canvas::Report::Sections
    allow_any_instance_of(Canvas::Report::Sections).to receive(:get_csv).and_return(sections_report_csv)

    # stub behavior for Canvas::SectionEnrollments
    section_enrollments_worker_1 = double(:list_enrollments => [section_enrollment1])
    section_enrollments_worker_2 = double(:list_enrollments => [section_enrollment2])
    section_enrollments_worker_3 = double(:list_enrollments => [section_enrollment3])
    section_enrollments_worker_4 = double(:list_enrollments => [section_enrollment4])
    allow(Canvas::SectionEnrollments).to receive(:new).with(:section_id => '20').and_return(section_enrollments_worker_1)
    allow(Canvas::SectionEnrollments).to receive(:new).with(:section_id => '19').and_return(section_enrollments_worker_2)
    allow(Canvas::SectionEnrollments).to receive(:new).with(:section_id => '21').and_return(section_enrollments_worker_3)
    allow(Canvas::SectionEnrollments).to receive(:new).with(:section_id => '22').and_return(section_enrollments_worker_4)

    # setup default values for global canvas synchronization settings
    CanvasCsv::Synchronization.create(:last_guest_user_sync => 1.weeks.ago.utc)
    CanvasCsv::Synchronization.get.update(:latest_term_enrollment_csv_set => (frozen_moment_in_time - 1.day))
  end

  after do
    delete_files_if_exists([expected_term1_filepath, expected_term2_filepath])
    CanvasCsv::Synchronization.delete_all
  end

  describe '.api_to_csv_enrollment' do
    it 'converts Canvas API Enrollment hash to CSV hash' do
      result = subject.class.api_to_csv_enrollment(section_enrollment1)
      expect(result).to be_an_instance_of Hash
      expect(result['course_id']).to eq 24
      expect(result['canvas_section_id']).to eq 20
      expect(result['sis_section_id']).to eq 'SEC:2014-D-25123'
      expect(result['canvas_user_id']).to eq 165
      expect(result['role']).to eq 'StudentEnrollment'
      expect(result['sis_import_id']).to eq 185
      expect(result['sis_login_id']).to eq '1000123'
      expect(result['sis_user_id']).to eq '24716840'
      expect(result['enrollment_state']).to eq 'active'
    end
  end

  describe '.csv_to_api_enrollment' do
    it 'converts cached CSV hash to Canvas API Enrollment hash' do
      cached_csv_hash = subject.class.api_to_csv_enrollment(section_enrollment1)
      result = subject.class.csv_to_api_enrollment(cached_csv_hash)
      expect(result).to be_an_instance_of Hash
      expect(result['course_id']).to eq 24
      expect(result['course_section_id']).to eq 20
      expect(result['sis_section_id']).to eq 'SEC:2014-D-25123'
      expect(result['user_id']).to eq 165
      expect(result['role']).to eq 'StudentEnrollment'
      expect(result['sis_import_id']).to eq 185
      expect(result['enrollment_state']).to eq 'active'
      expect(result['user']['sis_user_id']).to eq '24716840'
      expect(result['user']['sis_login_id']).to eq '1000123'
      expect(result['user']['login_id']).to eq '1000123'
    end
  end

  describe '#initialize' do
    it 'sets export directory to Canvas settings value' do
      result = subject.instance_eval { @export_dir }
      expect(result).to eq Settings.canvas_proxy.export_directory
    end
  end

  describe '#term_enrollments_csv_filepaths' do
    it 'provides files for current date by default' do
      csv_filepaths = subject.term_enrollments_csv_filepaths
      expect(csv_filepaths).to be_an_instance_of Hash
      expect(csv_filepaths['TERM:2013-D']).to eq "#{export_dir}/canvas-2014-01-01-TERM_2013-D-term-enrollments-export.csv"
      expect(csv_filepaths['TERM:2014-B']).to eq "#{export_dir}/canvas-2014-01-01-TERM_2014-B-term-enrollments-export.csv"
    end

    it 'provides files for date specified' do
      csv_filepaths = subject.term_enrollments_csv_filepaths(Time.at(1388863600))
      expect(csv_filepaths).to be_an_instance_of Hash
      expect(csv_filepaths['TERM:2013-D']).to eq "#{export_dir}/canvas-2014-01-04-TERM_2013-D-term-enrollments-export.csv"
      expect(csv_filepaths['TERM:2014-B']).to eq "#{export_dir}/canvas-2014-01-04-TERM_2014-B-term-enrollments-export.csv"
    end
  end

  describe '#enrollment_csv_filepath' do
    it 'returns expected filepath for date and term_id provided' do
      result = subject.enrollment_csv_filepath(Time.now, 'TERM:2014-D')
      expect(result).to eq "#{export_dir}/canvas-2014-01-01-TERM_2014-D-term-enrollments-export.csv"
    end
  end

  describe '#latest_term_enrollment_set_date' do
    let(:sync_setting) { double(:sync_setting, :latest_term_enrollment_csv_set => frozen_moment_in_time.in_time_zone) }
    it 'returns the date for the latest CSV term enrollments set' do
      allow(CanvasCsv::Synchronization).to receive(:get).and_return(sync_setting)
      result = subject.latest_term_enrollment_set_date
      expect(result).to be_an_instance_of Date
      expect(result).to eq frozen_moment_in_time.to_date
    end

    it 'obtains the latest set date only once' do
      expect(CanvasCsv::Synchronization).to receive(:get).once.and_return(sync_setting)
      result_1 = subject.latest_term_enrollment_set_date
      result_2 = subject.latest_term_enrollment_set_date
      expect(result_1).to be_an_instance_of Date
      expect(result_2).to be_an_instance_of Date
      expect(result_1).to eq frozen_moment_in_time.to_date
      expect(result_2).to eq frozen_moment_in_time.to_date
    end
  end

  describe '#export_enrollments_to_csv_set' do
    it 'generates csv exports for each term' do
      expect(subject).to receive(:populate_term_csv_file).and_return(nil).twice
      subject.export_enrollments_to_csv_set
    end

    it 'updates tracking timestamp when finished exporting enrollments to csv set' do
      subject.export_enrollments_to_csv_set
      sync_settings = CanvasCsv::Synchronization.get
      expect(sync_settings.latest_term_enrollment_csv_set).to eq frozen_moment_in_time.to_datetime.in_time_zone
    end
  end

  describe '#populate_term_csv_file' do
    context 'when sections report is empty' do
      before { allow_any_instance_of(Canvas::Report::Sections).to receive(:get_csv).and_return(empty_sections_report_csv) }
      it 'should escape execution' do
        enrollments_csv = subject.make_enrollment_export_csv("#{export_dir}/canvas-2014-01-01-TERM_2014-D-term-enrollments-export.csv")
        expect_any_instance_of(Canvas::SectionEnrollments).to_not receive(:new)
        subject.populate_term_csv_file(current_sis_term_ids[0], enrollments_csv)
      end
    end

    it 'populates csv export for term specified' do
      enrollments_csv = subject.make_enrollment_export_csv("#{export_dir}/canvas-2014-01-01-TERM_2014-D-term-enrollments-export.csv")
      subject.populate_term_csv_file(current_sis_term_ids[0], enrollments_csv)
      enrollments_csv.close
      enrollments_csv = CSV.read(enrollments_csv.path, {headers: true})
      expect(enrollments_csv.count).to eq 4
      expect(enrollments_csv[0]['canvas_section_id']).to eq '20'
      expect(enrollments_csv[1]['canvas_section_id']).to eq '19'
      expect(enrollments_csv[2]['canvas_section_id']).to eq '21'
      expect(enrollments_csv[3]['canvas_section_id']).to eq '22'
      expect(enrollments_csv[0]['sis_section_id']).to eq 'SEC:2014-D-25123'
      expect(enrollments_csv[1]['sis_section_id']).to eq 'SEC:2014-D-25124'
      expect(enrollments_csv[2]['sis_section_id']).to eq 'SEC:2014-D-25125'
      expect(enrollments_csv[3]['sis_section_id']).to eq 'SEC:2014-D-25126'
      expect(enrollments_csv[0]['canvas_user_id']).to eq '165'
      expect(enrollments_csv[1]['canvas_user_id']).to eq '166'
      expect(enrollments_csv[2]['canvas_user_id']).to eq '167'
      expect(enrollments_csv[3]['canvas_user_id']).to eq '168'
      expect(enrollments_csv[0]['sis_login_id']).to eq '1000123'
      expect(enrollments_csv[1]['sis_login_id']).to eq '1000124'
      expect(enrollments_csv[2]['sis_login_id']).to eq '1000125'
      expect(enrollments_csv[3]['sis_login_id']).to eq '1000126'
      expect(enrollments_csv[0]['sis_import_id']).to eq '185'
      expect(enrollments_csv[1]['sis_import_id']).to eq nil
      expect(enrollments_csv[2]['sis_import_id']).to eq '185'
      expect(enrollments_csv[3]['sis_import_id']).to eq '185'
      expect(enrollments_csv[0]['role']).to eq 'StudentEnrollment'
      expect(enrollments_csv[1]['role']).to eq 'StudentEnrollment'
    end
  end

  describe '#make_enrollment_export_csv' do
    it 'returns CSV file object with headers initialized' do
      export_csv = subject.make_enrollment_export_csv("#{export_dir}/canvas-2014-01-01-TERM_2013-D-term-enrollments-export.csv")
      expect(export_csv).to be_an_instance_of CSV
      export_csv << [5,'SEC:2013-D-26109',165,'23828759','StudentEnrollment',185]
      expect(export_csv.headers).to eq ['course_id', 'canvas_section_id', 'sis_section_id', 'canvas_user_id', 'sis_login_id', 'sis_user_id', 'role', 'sis_import_id', 'enrollment_state']
      export_csv.close
    end
  end

  describe '#load_current_term_enrollments' do
    before do
      allow(CSV).to receive(:read).with(expected_term1_filepath, {headers: true}).and_return(term1_sections_report_csv)
      allow(CSV).to receive(:read).with(expected_term2_filepath, {headers: true}).and_return(term2_sections_report_csv)
    end

    it 'loads canvas section enrollments hash from latest CSV set' do
      subject.export_enrollments_to_csv_set
      result = subject.load_current_term_enrollments
      expect(result).to be_an_instance_of Hash
      expect(result.keys).to eq ['SEC:2014-C-25128', 'SEC:2014-C-25129', 'SEC:2014-C-24111', 'SEC:2014-C-24112']
      result.each do |canvas_section_id,csv_rows|
        expect(csv_rows).to be_an_instance_of Array
        expect(csv_rows.count).to eq 2
        expect(csv_rows[0]).to be_an_instance_of CSV::Row
        expect(csv_rows[1]).to be_an_instance_of CSV::Row
      end
      expect(result['SEC:2014-C-25128'][0]['sis_section_id']).to eq 'SEC:2014-C-25128'
      expect(result['SEC:2014-C-25128'][1]['sis_section_id']).to eq 'SEC:2014-C-25128'
      expect(result['SEC:2014-C-25128'][0]['canvas_user_id']).to eq '4906376'
      expect(result['SEC:2014-C-25128'][1]['canvas_user_id']).to eq '4906377'
      expect(result['SEC:2014-C-24112'][0]['sis_section_id']).to eq 'SEC:2014-C-24112'
      expect(result['SEC:2014-C-24112'][1]['sis_section_id']).to eq 'SEC:2014-C-24112'
      expect(result['SEC:2014-C-24112'][0]['canvas_user_id']).to eq '4906376'
      expect(result['SEC:2014-C-24112'][1]['canvas_user_id']).to eq '4906377'
    end
  end

  describe '#cached_canvas_section_enrollments' do
    let(:csv_enrollments_hash) { {
      'SEC:2014-C-12345' => [
        {
          'course_id' => '209901',
          'canvas_section_id' => '1413864',
          'sis_section_id' => 'SEC:2014-C-12345',
          'canvas_user_id' => '4906376',
          'role' => 'StudentEnrollment',
          'sis_import_id' => '101',
          'sis_user_id' => '12890',
          'sis_login_id' => '7977',
          'enrollment_state' => 'active',
        },
        {
          'course_id' => '209901',
          'canvas_section_id' => '1413864',
          'sis_section_id' => 'SEC:2014-C-12345',
          'canvas_user_id' => '4906377',
          'role' => 'StudentEnrollment',
          'sis_import_id' => '101',
          'sis_user_id' => '12891',
          'sis_login_id' => '7978',
          'enrollment_state' => 'active',
        },
      ]
    } }
    before do
      allow(CSV).to receive(:read).with(expected_term1_filepath, {headers: true}).and_return(term1_sections_report_csv)
      allow(CSV).to receive(:read).with(expected_term2_filepath, {headers: true}).and_return(term2_sections_report_csv)
    end

    it 'does not load current term enrollments if already present' do
      expect(subject).to_not receive(:load_current_term_enrollments)
      csv_enrollments = csv_enrollments_hash
      subject.instance_eval { @canvas_section_id_enrollments = csv_enrollments }
      result = subject.cached_canvas_section_enrollments('SEC:2014-C-12345')
      expect(result).to eq [
        {
          'course_id'=>'209901',
          'course_section_id'=>'1413864',
          'sis_section_id'=>'SEC:2014-C-12345',
          'user_id'=>'4906376',
          'role'=>'StudentEnrollment',
          'sis_import_id'=>'101',
          'enrollment_state' => 'active',
          'user'=>{
            'sis_user_id'=>'12890',
            'sis_login_id'=>'7977',
            'login_id'=>'7977'
          }
        },
        {
          'course_id'=>'209901',
          'course_section_id'=>'1413864',
          'sis_section_id'=>'SEC:2014-C-12345',
          'user_id'=>'4906377',
          'role'=>'StudentEnrollment',
          'sis_import_id'=>'101',
          'enrollment_state' => 'active',
          'user'=>{
            'sis_user_id'=>'12891',
            'sis_login_id'=>'7978',
            'login_id'=>'7978'
          }
        }
      ]
    end

    it 'returns empty array when no enrollments available for specified section' do
      subject.export_enrollments_to_csv_set
      result = subject.cached_canvas_section_enrollments('SEC:2014-C-25999')
      puts "result: #{result.inspect}"
      expect(result).to be_an_instance_of Array
      expect(result.count).to eq 0
    end

    it 'returns canvas api formatted enrollments for canvas sis section id specified' do
      subject.export_enrollments_to_csv_set
      result = subject.cached_canvas_section_enrollments('SEC:2014-C-24111')
      expect(result).to be_an_instance_of Array
      expect(result.count).to eq 2
      expect(result[0]).to be_an_instance_of Hash
      expect(result[1]).to be_an_instance_of Hash
      expect(result[0]['course_id']).to eq '1278'
      expect(result[1]['course_id']).to eq '1278'
      expect(result[0]['course_section_id']).to eq '1413864'
      expect(result[1]['course_section_id']).to eq '1413864'
      expect(result[0]['sis_section_id']).to eq 'SEC:2014-C-24111'
      expect(result[1]['sis_section_id']).to eq 'SEC:2014-C-24111'
      expect(result[0]['user_id']).to eq '4906376'
      expect(result[1]['user_id']).to eq '4906377'
      expect(result[0]['role']).to eq 'StudentEnrollment'
      expect(result[1]['role']).to eq 'StudentEnrollment'
      expect(result[0]['enrollment_state']).to eq 'active'
      expect(result[1]['enrollment_state']).to eq 'active'
      expect(result[0]['sis_import_id']).to eq '101'
      expect(result[1]['sis_import_id']).to eq '101'
      expect(result[0]['user']['sis_user_id']).to eq '109812'
      expect(result[1]['user']['sis_user_id']).to eq '109813'
      expect(result[0]['user']['sis_login_id']).to eq '7977'
      expect(result[1]['user']['sis_login_id']).to eq '7978'
      expect(result[0]['user']['login_id']).to eq '7977'
      expect(result[1]['user']['login_id']).to eq '7978'
    end
  end

end
