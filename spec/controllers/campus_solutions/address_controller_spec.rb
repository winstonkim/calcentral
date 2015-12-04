describe CampusSolutions::AddressController do

  let(:user_id) { '12345' }

  context 'updating address' do
    it 'should not let an unauthenticated user post' do
      post :post, {format: 'json', uid: '100'}
      expect(response.status).to eq 401
    end

    context 'authenticated user' do
      before do
        session['user_id'] = user_id
        User::Auth.stub(:where).and_return([User::Auth.new(uid: '1234', is_superuser: false, active: true)])
      end
      it 'should let an authenticated user post' do
        post :post,
             {
               bogus_field: 'abc',
               addressType: 'HOME',
               address1: '1 Test Lane',
               address2: 'peters road',
               city: 'ventura',
               state: 'CA',
               postal: '93001',
               country: 'USA'
             }
        expect(response.status).to eq 200
        json = JSON.parse(response.body)
        expect(json['statusCode']).to eq 200
        expect(json['feed']).to be
        expect(json['feed']['address']).to be
      end
    end
  end

  context 'deleting address' do
    it 'should not let an unauthenticated user delete' do
      delete :delete, {format: 'json', type: '100'}
      expect(response.status).to eq 401
    end

    context 'authenticated user' do
      before do
        session['user_id'] = user_id
        User::Auth.stub(:where).and_return([User::Auth.new(uid: '1234', is_superuser: false, active: true)])
      end
      it 'should let an authenticated user delete' do
        delete :delete,
               {
                 bogus_field: 'abc',
                 type: 'HOME'
               }
        expect(response.status).to eq 200
        json = JSON.parse(response.body)
        expect(json['statusCode']).to eq 200
        expect(json['feed']).to be
        expect(json['feed']['status']).to be
      end
    end
  end
end

