describe CampusSolutions::EnrollmentTerms do
  let(:user_id) { '12348' }
  shared_examples 'a proxy that gets data' do
    subject { proxy.get }
    it_should_behave_like 'a simple proxy that returns errors'
    it_behaves_like 'a proxy that properly observes the enrollment card flag'
    it_behaves_like 'a proxy that got data successfully'
    it 'returns data with the expected structure' do
      expect(subject[:feed][:enrollmentTerms][0]).to be
    end
  end

  context 'mock proxy' do
    let(:proxy) { CampusSolutions::EnrollmentTerms.new(fake: true, user_id: user_id) }
    subject { proxy.get }
    it_should_behave_like 'a proxy that gets data'
    it 'returns specific mock data' do
      expect(subject[:feed][:enrollmentTerms][0]).to eq(termId: '2176', termDescr: 'Spring 2017')
      expect(subject[:feed][:enrollmentTerms][1]).to eq(termId: '2179', termDescr: 'Summer 2017')
    end
  end
end
