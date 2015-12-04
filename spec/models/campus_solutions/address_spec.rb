# encoding: UTF-8
describe CampusSolutions::Address do

  let(:user_id) { '12345' }

  context 'post' do
    let(:params) { {} }
    let(:proxy) { CampusSolutions::Address.new(fake: true, user_id: user_id, params: params) }

    context 'filtering out fields not on the whitelist' do
      let(:params) { {
        bogus: 1,
        invalid: 2,
        address1: '1 Test Lane'
      } }
      subject { proxy.filter_updateable_params(params) }
      it 'should strip out invalid fields' do
        expect(subject.keys.length).to eq 16
        expect(subject[:bogus]).to be_nil
        expect(subject[:invalid]).to be_nil
        expect(subject[:address1]).to eq '1 Test Lane'
      end
    end

    context 'converting params to Campus Solutions field names' do
      let(:params) { {
        addressType: 'HOME',
        address1: '1 Test Lane'
      } }
      subject {
        result = proxy.construct_cs_post(params)
        MultiXml.parse(result)['UC_CC_ADDR_UPD_REQ']
      }
      it 'should convert the CalCentral params to Campus Solutions params without exploding on bogus fields' do
        expect(subject['ADDRESS1']).to eq '1 Test Lane'
        expect(subject['ADDRESS_TYPE']).to eq 'HOME'
      end
    end

    context 'performing a post' do
      let(:params) { {
        addressType: 'HOME',
        address1: '1 Test Lane'
      } }
      subject {
        proxy.get
      }
      it_should_behave_like 'a simple proxy that returns errors'
      it_behaves_like 'a proxy that properly observes the profile feature flag'
      it_behaves_like 'a proxy that got data successfully'
    end
  end

  context 'with a real external service', testext: true do
    let(:params) { {
      addressType: 'HOME',
      address1: 'Über dem Faß Rénard',
      address2: 'peters road',
      city: 'ventura',
      state: 'CA',
      postal: '93001',
      country: 'USA'
    } }
    let(:proxy) { CampusSolutions::Address.new(fake: false, user_id: user_id, params: params) }

    context 'performing a real post' do
      subject {
        proxy.get
      }
      it_should_behave_like 'a simple proxy that returns errors'
      it_behaves_like 'a proxy that got data successfully'
    end
  end
end
