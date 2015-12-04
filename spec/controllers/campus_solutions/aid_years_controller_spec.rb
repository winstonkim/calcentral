describe CampusSolutions::AidYearsController do

  let(:user_id) { '12345' }

  context 'aid years feed' do
    let(:feed) { :get }
    it_behaves_like 'an unauthenticated user'

    context 'authenticated user' do
      let(:feed_key) { 'finaidSummary' }
      it_behaves_like 'a successful feed'
      it 'has some field mapping info' do
        session['user_id'] = user_id
        get feed
        json = JSON.parse(response.body)
        expect(json['feed']['finaidSummary']['finaidYears'][0]['id']).to eq '2015'
      end
    end
  end

end
