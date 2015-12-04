describe Financials::MyFinancials do

  let(:uid) { '61889' }
  before(:each) {
    # We might consider extracting this tricky cache-checker to a shared context.
    # Note that the counter needs to be an instance variable rather than a magic "let"-defined variable.
    @cached_count = 0
    allow(Rails.cache).to receive(:write) do |key, arg1, expires|
      if key.include? described_class.cache_key(uid)
        @cached_count += 1
        @expires_in = expires[:expires_in]
      end
    end
  }

  subject {
    feed = Financials::MyFinancials.new(uid).get_feed
    JSON.parse(feed)
  }

  shared_examples 'a feed with the common live-updates fields' do
    it 'should have a lastModified field' do
      expect(subject['lastModified']).to be
      expect(@cached_count).to eq 1
    end
    it 'should have a feedName field' do
      expect(subject['feedName']).to eq 'Financials::MyFinancials'
    end
  end

  # We combine expectations in one larger test so as to reduce proxy load
  # when running in the testext environment.
  context 'when following a happy path for #get_feed' do
    it 'includes the expected data' do
      expect(subject).to be
      expect(subject['summary']).to be
      expect(subject['currentTerm']).to eq Berkeley::Terms.fetch.current.to_english
      expect(subject['apiVersion'].gsub('.', '').to_i).to be >= 106
    end
    it_behaves_like 'a feed with the common live-updates fields'
  end

  context 'error on remote server' do
    let(:student_id) { '11667051' }
    let(:real_proxy) { Financials::Proxy.new({user_id: uid, student_id: student_id, fake: false}) }
    before {
      allow(Financials::Proxy).to receive(:new).and_return(real_proxy)
    }

    let! (:body) { 'An unknown error occurred.' }
    let! (:status) { 506 }
    include_context 'expecting logs from server errors'
    before do
      stub_request(:any, /.*#{Settings.financials_proxy.base_url}.*/).to_return(status: status, body: body)
    end
    it 'reports an error' do
      expect(subject['body']).to eq('My Finances is currently unavailable. Please try again later.')
      expect(@expires_in).to eq Settings.cache.expiration.failure
      expect(subject['statusCode']).to eq 503
    end
  end

  context 'when working with a faked proxy' do
    let(:fake_proxy) { Financials::Proxy.new({user_id: uid, student_id: student_id, fake: true}) }
    before {
      allow(Financials::Proxy).to receive(:new).and_return(fake_proxy)
    }

    context 'when a student whose data is missing gets the feed' do
      let(:uid) { '300940' }
      let(:student_id) { '99999979' }
      it 'should return a specific error message explaining the missing data' do
        expect(subject['body']).to eq("You are seeing this message because CalCentral does not have CARS billing data for your account. If you are a new student, your account may not have been assessed charges yet. Please try again later. Current or former students should contact us for further assistance using the Feedback link below.")
      end
      it 'should return a 404 status and be a long-lived cache' do
        expect(subject['statusCode']).to eq(404)
        expect(@expires_in).to_not eq Settings.cache.expiration.failure
      end
      it_behaves_like 'a feed with the common live-updates fields'
    end

    context 'when a non-student calls the proxy' do
      let(:uid) { '212377' }
      let(:student_id) { nil }
      it 'should return a specific error message explaining that non-students lack financials' do
        expect(subject['body']).to eq("You are seeing this message because CalCentral does not have CARS billing data for your account. If you are a new student, your account may not have been assessed charges yet. Please try again later. Current or former students should contact us for further assistance using the Feedback link below.")
      end
      it 'should return a 404 status and be a long-lived cache' do
        expect(subject['statusCode']).to eq(404)
        expect(@expires_in).to_not eq Settings.cache.expiration.failure
      end
      it_behaves_like 'a feed with the common live-updates fields'
    end
  end

end
