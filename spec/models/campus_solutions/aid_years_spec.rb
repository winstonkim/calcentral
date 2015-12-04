describe CampusSolutions::AidYears do

  shared_examples 'a proxy that gets data' do
    subject { proxy.get }
    it_should_behave_like 'a simple proxy that returns errors'
    it_behaves_like 'a proxy that properly observes the finaid feature flag'
    it_behaves_like 'a proxy that got data successfully'
    it 'returns data with the expected structure' do
      expect(subject[:feed][:finaidSummary][:finaidYears]).to be
    end
  end

  context 'mock proxy' do
    let(:proxy) { CampusSolutions::AidYears.new(fake: true, user_id: '12345') }
    it_should_behave_like 'a proxy that gets data'
  end

  context 'real proxy', testext: true do
    let(:proxy) { CampusSolutions::AidYears.new(fake: false, user_id: '12345') }
    it_should_behave_like 'a proxy that gets data'
  end

end
