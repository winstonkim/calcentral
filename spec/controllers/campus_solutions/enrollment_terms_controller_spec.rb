describe CampusSolutions::EnrollmentTermsController do

  let(:user_id) { '12345' }
  before do
    allow(Settings.features).to receive(:cs_enrollment_card).and_return true
    allow_any_instance_of(HubEdos::UserAttributes).to receive(:has_role?).with(:student).and_return true
  end

  context 'enrollment terms feed' do
    let(:feed) { :get }
    it_behaves_like 'an unauthenticated user'

    context 'authenticated user' do
      let(:feed_key) { 'enrollmentTerms' }
      it_behaves_like 'a successful feed'
      it 'has some field mapping info' do
        session['user_id'] = user_id
        get feed
        json = JSON.parse(response.body)
        expect(json['feed']['enrollmentTerms'][0]['termId']).to eq '2162'
      end
    end
  end

end
