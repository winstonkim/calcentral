describe CampusSolutions::CountryController do
  context 'country feed' do
    let(:feed) { :get }
    it_behaves_like 'an unauthenticated user'
    context 'authenticated user' do
      let(:user_id) { random_id }
      let(:feed_key) { 'countries' }
      it_behaves_like 'a successful feed'
    end
  end
end
