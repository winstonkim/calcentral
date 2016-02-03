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
    let(:terms) { subject[:feed][:enrollmentTerms] }
    it_should_behave_like 'a proxy that gets data'

    it 'includes student ID' do
      expect(subject[:feed][:studentId]).to eq '12701798'
    end

    it 'returns expected attributes for all terms' do
      expect(terms).to all(include(:acadCareer, :termId, :termDescr))
    end

    it 'returns terms in order of ID' do
      expect(terms.map { |term| term[:termId] }).to eq %w(2162 2165 2168)
    end

    it 'map terms to correct academic career' do
      expect(terms[0][:acadCareer]).to eq 'UGRD'
      expect(terms[1][:acadCareer]).to eq 'GRAD'
      expect(terms[2][:acadCareer]).to eq 'GRAD'
    end
  end
end
