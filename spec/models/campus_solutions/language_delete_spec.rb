describe CampusSolutions::LanguageDelete do

  let(:user_id) { '12346' }

  context 'deleting language' do
    let(:params) { {} }
    let(:proxy) { CampusSolutions::LanguageDelete.new(fake: true, user_id: user_id, params: params) }

    context 'converting params to Campus Solutions field names' do
      let(:params) { {
        bogus: 'foo',
        jpmCatItemId: 'EN'
      } }
      subject {
        proxy.construct_cs_post(params)
      }
      it 'should convert the CalCentral params to Campus Solutions params without exploding on bogus fields' do
        expect(subject[:query][:JPM_CAT_ITEM_ID]).to eq 'EN'
        expect(subject[:query].keys.length).to eq 2
      end
    end

    context 'performing a delete' do
      let(:params) { {
        jpmCatItemId: 'EN'
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
    let(:create_params) { {
      languageCode: 'EN',
      isNative: 'N',
      isTranslateToNative: 'N',
      isTeachLanguage: 'N',
      speakProf: '1',
      readProf: '2',
      teachLang: '3'
    } }
    before {
      CampusSolutions::LanguagePost.new(fake: false, user_id: user_id, params: create_params).get
    }

    let(:proxy) { CampusSolutions::LanguageDelete.new(fake: false, user_id: user_id, params: params) }
    subject { proxy.get }

    context 'a successful delete' do
      let(:params) { {
        jpmCatItemId: 'EN'
      } }
      context 'performing a real delete' do
        it_behaves_like 'a proxy that got data successfully'
      end
    end

  end
end
