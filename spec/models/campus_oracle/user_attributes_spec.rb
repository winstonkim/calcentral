describe CampusOracle::UserAttributes do

  context 'obtaining user attributes feed' do

    subject {CampusOracle::UserAttributes.new(user_id: uid).get_feed_internal}

    shared_examples_for 'a parser for roles' do |expected_roles|
      it 'only sets expected roles' do
        expected_roles.each do |role|
          expect(subject[:roles][role]).to be_truthy
        end
        subject[:roles].each do |role, value|
          expect(value).to be_falsey unless expected_roles.include?(role)
        end
      end
    end

    context 'working against test data', if: CampusOracle::Queries.test_data? do
      context 'student with blank REG_STATUS_CD' do
        let(:uid) {300847}
        shared_examples 'expected feed values' do
          it 'includes expected feed values' do
            expect(subject[:education_level]).to eq 'Masters'
            expect(subject[:reg_status][:code]).to eq ' '
            expect(subject[:reg_status][:summary]).to eq 'Not Registered'
            expect(subject[:reg_status][:explanation]).to eq 'In order to be officially registered, you must pay at least 20% of your registration fees, have no outstanding blocks, and be enrolled in at least one class.'
            expect(subject[:reg_status][:needsAction]).to eq true
          end
        end
        context 'normal term' do
          include_examples 'expected feed values'
          it 'does not report term transition' do
            expect(subject[:reg_status]).not_to include(:transitionTerm)
          end
          it 'includes residency status' do
            expect(subject[:california_residency][:summary]).to eq 'Non-Resident'
          end
        end
        context 'term transition' do
          before { Settings.terms.stub(:fake_now).and_return(DateTime.parse('2013-07-10')) }
          include_examples 'expected feed values'
          it 'reports term transition' do
            expect(subject[:reg_status][:transitionTerm]).to eq true
          end
          it 'omits residency status' do
            expect(subject[:california_residency]).to eq nil
          end
        end
      end
      context 'student with Education Abroad REG_SPECIAL_PGM_CD' do
        let(:uid) {300853}
        it 'includes expected feed values' do
          expect(subject[:education_abroad]).to be_truthy
        end
      end
      describe 'roles' do
        context 'student' do
          let(:uid) {300846}
          it_behaves_like 'a parser for roles', [:student, :registered]
        end
        context 'staff member and ex-student' do
          let(:uid) {238382}
          it_behaves_like 'a parser for roles', [:exStudent, :staff]
        end
        context 'user without affiliations data' do
          let(:uid) {321765}
          it_behaves_like 'a parser for roles', []
        end
        context 'guest' do
          let(:uid) {19999969}
          it_behaves_like 'a parser for roles', [:guest]
        end
        context 'concurrent enrollment student' do
          let(:uid) {321703}
          it_behaves_like 'a parser for roles', [:concurrentEnrollmentStudent]
        end
        context 'user with expired CalNet account' do
          let(:uid) {6188989}
          it_behaves_like 'a parser for roles', [:student, :registered, :expiredAccount]
        end
      end
    end

  end

  context 'checking if user is staff or faculty member' do
    let(:uid) { rand(99999).to_s }
    subject { CampusOracle::UserAttributes.new(user_id: uid) }
    before { allow(subject).to receive(:get_feed).and_return(fake_feed) }
    let(:fake_feed) do
      {
        :roles => {:faculty => false, :staff => false}
      }
    end

    context 'if user is not found by uid provided' do
      let(:fake_feed) { nil }
      it 'returns false' do
        expect(subject.is_staff_or_faculty?).to eq false
      end
    end

    context 'if user is not staff or faculty member' do
      before { fake_feed[:roles][:student] = true }
      it 'returns false' do
        expect(subject.is_staff_or_faculty?).to eq false
      end
    end

    context 'if user is staff member' do
      before { fake_feed[:roles][:staff] = true }
      it 'returns true' do
        expect(subject.is_staff_or_faculty?).to eq true
      end
    end

    context 'if user is faculty member' do
      before { fake_feed[:roles][:faculty] = true }
      it 'returns true' do
        expect(subject.is_staff_or_faculty?).to eq true
      end
    end
  end

end
