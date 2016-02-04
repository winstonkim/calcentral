describe CampusSolutions::FinancialAidDataController do

  let(:user_id) { '12345' }

  context 'financial data feed' do
    let(:feed) { :get }
    it_behaves_like 'an unauthenticated user'

    context 'authenticated user' do
      let(:feed_key) { 'coa' }
      it_behaves_like 'a successful feed'
      it 'has some field mapping info' do
        session['user_id'] = user_id
        get feed, {:aid_year => '2016', :format => 'json'}
        json = JSON.parse response.body
        expect(json['feed']['coa']['title']).to eq 'Estimated Cost of Attendance'
      end
    end

    context 'authenticated user' do
      let(:filtered_feed) { { key: 'value' } }
      before {
        session['user_id'] = user_id
        session['original_advisor_user_id'] = random_id
        model = double(get_feed_as_json: filtered_feed)
        expect(model).to receive(:aid_year=).with '2016'
        expect(CampusSolutions::MyFinancialAidFilteredForAdvisor).to receive(:from_session).once.and_return model
        expect(CampusSolutions::MyFinancialAidData).to_not receive :from_session
      }
      it 'invokes the filtered feed when advisor-view-as mode' do
        get feed, {:aid_year => '2016', :format => 'json'}
        json = JSON.parse response.body
        expect(json['key']).to eq 'value'
      end
    end
  end

end
