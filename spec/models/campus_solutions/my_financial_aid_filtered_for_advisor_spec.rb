describe CampusSolutions::MyFinancialAidFilteredForAdvisor do

  context 'mock proxy' do
    context 'no advisor session' do
      it 'should deny access' do
        state = { 'fake' => true, 'user_id' => random_id }
        expect{
          CampusSolutions::MyFinancialAidFilteredForAdvisor.from_session(state).get_feed
        }.to raise_exception /Only advisors have access/
      end
    end
    context 'advisor session' do
      let(:original_advisor_user_id) { random_id }
      subject {
        state = { 'fake' => true, 'user_id' => random_id, 'original_advisor_user_id' => original_advisor_user_id }
        CampusSolutions::MyFinancialAidFilteredForAdvisor.from_session(state)
      }
      it 'should filter out \'Expected Family Contribution\' and similar' do
        feed = subject.get_feed
        expect(feed[:filteredForAdvisor]).to be true
        json = feed.to_json
        expect(json).to include 'SHIP Health Insurance', 'Student Standing', 'Estimated Cost of Attendance'
        expect(json).to_not include 'EFC', 'Family', 'Parent'
      end
    end
  end
end
