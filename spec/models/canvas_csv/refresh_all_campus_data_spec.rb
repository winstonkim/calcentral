describe CanvasCsv::RefreshAllCampusData do

  context 'incremental refresh mode' do
    subject { CanvasCsv::RefreshAllCampusData.new 'incremental' }
    let(:today) { Time.now.strftime '%F' }
    before do
      allow(Canvas::Terms).to receive(:current_sis_term_ids).and_return current_sis_term_ids
    end

    context 'with multiple terms' do
      let(:current_sis_term_ids) { ['TERM:2013-D', 'TERM:2014-B'] }
      it 'establishes the csv import files' do
        expect(subject.users_csv_filename).to eq "tmp/canvas/canvas-#{today}-users-incremental.csv"
        expect(subject.term_to_memberships_csv_filename['TERM:2013-D']).to eq "tmp/canvas/canvas-#{today}-TERM_2013-D-enrollments-incremental.csv"
        expect(subject.term_to_memberships_csv_filename['TERM:2014-B']).to eq "tmp/canvas/canvas-#{today}-TERM_2014-B-enrollments-incremental.csv"
      end
      it 'makes calls to each step of refresh in proper order' do
        expect(subject).to receive(:make_csv_files).ordered.and_return true
        expect(subject).to receive(:import_csv_files).ordered.and_return true
        subject.run
      end
      it 'should send call to populate incremental update csv for users and enrollments' do
        CanvasCsv::MaintainUsers.any_instance.should_receive(:refresh_existing_user_accounts).once.and_return nil
        expect_any_instance_of(CanvasCsv::RefreshAllCampusData).to receive(:refresh_existing_term_sections).twice.and_return nil
        subject.make_csv_files
      end
    end

    describe 'term-specific work' do
      let(:current_sis_term_ids) { ['TERM:2014-B'] }
      let(:ccn) { random_id }
      let(:canvas_term_sections_csv_string) do
        [
          'canvas_section_id,section_id,canvas_course_id,course_id,name,status,start_date,end_date,canvas_account_id,account_id',
          "#{ccn}2,SEC:2014-B-2#{ccn},#{random_id},CRS:#{ccn},DIS 101,active,,,105300,ACCT:LAW",
          "#{random_id},,#{random_id},CRS:#{ccn},INFORMAL 2,active,,,105300,ACCT:EDUC",
          "#{random_id},SEC:2014-B-#{random_id},#{random_id},,LAB 201,active,,,105300,ACCT:EDUC",
          "#{ccn}1,SEC:2014-B-1#{ccn},#{random_id},CRS:#{ccn},DIS 102,active,,,105300,ACCT:LAW"
        ].join("\n")
      end
      let(:canvas_term_sections_csv_table) { CSV.parse(canvas_term_sections_csv_string, {headers: true}) }
      before do
        allow_any_instance_of(CanvasCsv::MaintainUsers).to receive(:refresh_existing_user_accounts)
        allow_any_instance_of(Canvas::Report::Sections).to receive(:get_csv).and_return(canvas_term_sections_csv_table)
      end
      it 'passes well constructed parameters to the memberships maintainer' do
        expect(CanvasCsv::SiteMembershipsMaintainer).to receive(:new) do |course_id, section_ids, enrollments_csv, users_csv, known_users, options|
          expect(course_id).to eq "CRS:#{ccn}"
          expect(section_ids.size).to eq 2
          expect(section_ids[0]).to eq "SEC:2014-B-2#{ccn}"
          expect(section_ids[1]).to eq "SEC:2014-B-1#{ccn}"
          expect(known_users).to eq []
          expect(options[:batch_mode]).to be_falsey
          expect(options[:cached_enrollments_provider]).to be_an_instance_of CanvasCsv::TermEnrollments
          double(refresh_sections_in_course: nil)
        end
        subject.make_csv_files
      end

    end

    describe '#refresh_existing_term_sections' do
      let(:current_sis_term_ids) { ['TERM:2014-D'] }
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
      let(:cached_enrollments_provider) { CanvasCsv::TermEnrollments.new }

      context 'when canvas sections csv is present' do
        before { allow_any_instance_of(Canvas::Report::Sections).to receive(:get_csv).and_return(sections_report_csv) }
        it 'passes only sections with course_id and section_id to site membership maintainer process for each course' do
          users_csv = subject.instance_eval { make_users_csv(@users_csv_filename) }
          known_uids = []
          term = subject.instance_eval { @term_to_memberships_csv_filename.keys[0] }
          enrollments_csv = subject.instance_eval { @term_to_memberships_csv_filename.values[0] }
          expected_course_id = 'CRS:COMPSCI-9D-2014-D'
          expected_sis_section_ids = ['SEC:2014-D-25123', 'SEC:2014-D-25124']
          sis_user_id_changes = { 'sis_login_id:7978' => '2018903' }
          expect(CanvasCsv::SiteMembershipsMaintainer).to receive(:process).with(expected_course_id, expected_sis_section_ids, enrollments_csv, users_csv, known_uids, false, cached_enrollments_provider, sis_user_id_changes).once
          subject.refresh_existing_term_sections(term, enrollments_csv, known_uids, users_csv, cached_enrollments_provider, sis_user_id_changes)
        end
      end

      context 'when canvas sections csv is empty' do
        before { allow_any_instance_of(Canvas::Report::Sections).to receive(:get_csv).and_return(empty_sections_report_csv) }
        it 'does not perform any processing' do
          expect(CanvasCsv::SiteMembershipsMaintainer).to_not receive(:process)
        end
      end
    end

  end

  describe 'user-accounts-only mode' do
    subject { CanvasCsv::RefreshAllCampusData.new 'accounts' }
    it 'only refreshes existing user accounts and leaves memberships alone' do
      expect_any_instance_of(CanvasCsv::MaintainUsers).to receive(:refresh_existing_user_accounts).once.and_return nil
      expect(subject).to receive(:refresh_existing_term_sections).never
      expect(subject).to receive(:csv_count).once.and_return 1
      expect_any_instance_of(Canvas::SisImport).to receive(:import_users).once.and_return true
      expect_any_instance_of(Canvas::SisImport).to receive(:import_batch_term_enrollments).never
      expect_any_instance_of(Canvas::SisImport).to receive(:import_all_term_enrollments).never
      subject.run
    end
  end

end
