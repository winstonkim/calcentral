describe CampusSolutions::Holds do

  let(:user_id) { '12349' }

  shared_examples 'a proxy that gets data' do
    subject { proxy.get }
    it_should_behave_like 'a simple proxy that returns errors'
    it_behaves_like 'a proxy that properly observes the holds feature flag'
    it_behaves_like 'a proxy that got data successfully'
    it 'returns data with the expected structure' do
      expect(subject[:feed][:serviceIndicators]).to be
    end
  end

  context 'mock proxy' do
    let(:proxy) { CampusSolutions::Holds.new(fake: true, user_id: user_id) }
    subject { proxy.get }
    it_should_behave_like 'a proxy that gets data'
    it 'should get specific mock data' do
      expect(subject[:feed][:serviceIndicators][0][:serviceIndicatorDescr]).to eq 'Financial Aid Hold Code'
    end
  end

  context 'real proxy', testext: true do
    let(:proxy) { CampusSolutions::Holds.new(fake: false, user_id: user_id) }
    it_should_behave_like 'a proxy that gets data'
  end

end
