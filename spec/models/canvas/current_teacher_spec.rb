describe Canvas::CurrentTeacher do

  let(:uid)             { rand(99999).to_s }

  subject { Canvas::CurrentTeacher.new(uid) }

  describe '#user_currently_teaching?' do

    let(:current_term_db_row) {{
      'term_yr' => '2014',
      'term_cd' => 'B',
      'term_status_desc' => 'Current Term',
      'term_name' => 'Spring',
      'term_start_date' => Time.gm(2014, 1, 21),
      'term_end_date' => Time.gm(2014, 5, 9)
    }}

    let(:summer_term_db_row) {{
      'term_yr' => '2014',
      'term_cd' => 'C',
      'term_status_desc' => 'Current Summer',
      'term_name' => 'Summer',
      'term_start_date' => Time.gm(2014, 5, 27),
      'term_end_date' => Time.gm(2014, 8, 15)
    }}

    let(:fall_term_db_row) {{
      'term_yr' => '2014',
      'term_cd' => 'D',
      'term_status_desc' => 'Future Term',
      'term_name' => 'Fall',
      'term_start_date' => Time.gm(2014, 8, 28),
      'term_end_date' => Time.gm(2014, 12, 12)
    }}

    let(:current_terms) {
      [
        Berkeley::Term.new(fall_term_db_row),
        Berkeley::Term.new(summer_term_db_row),
        Berkeley::Term.new(current_term_db_row),
      ]
    }

    let(:spring_2012_instructor_uid) { '238382' }
    let(:summer_2014_instructor_uid) { '904715' }

    before do
      allow(Canvas::Terms).to receive(:current_terms).and_return(current_terms)
    end

    context 'when uid is unavailable' do
      subject { Canvas::CurrentTeacher.new(nil) }
      its(:user_currently_teaching?) { should be_falsey }
    end

    context 'when user is instructing in current canvas terms', if: CampusOracle::Queries.test_data? do
      subject { Canvas::CurrentTeacher.new(summer_2014_instructor_uid) }
      its(:user_currently_teaching?) { should be_truthy }
    end

    context 'when user is not instructing in current canvas terms', if: CampusOracle::Queries.test_data? do
      subject { Canvas::CurrentTeacher.new(spring_2012_instructor_uid) }
      its(:user_currently_teaching?) { should be_falsey }
    end

    context 'when response is cached', if: CampusOracle::Queries.test_data? do
      subject { Canvas::CurrentTeacher.new(summer_2014_instructor_uid) }
      it 'does not make calls to dependent objects' do
        expect(subject.user_currently_teaching?).to be_truthy
        expect(Canvas::Terms).to_not receive(:current_terms)
        expect(CampusOracle::Queries).to_not receive(:has_instructor_history?)
        expect(subject.user_currently_teaching?).to be_truthy
      end
    end
  end

end
