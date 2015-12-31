describe Proxies::Mockable do

  class TestMockable
    include Proxies::Mockable
    def initialize(settings, opts)
      @settings = settings
      @opts = opts
    end
    def mock_request
      super.merge(@opts)
    end
  end

  let(:settings) { double(base_url: base_url) }
  let(:base_url) { 'https://apis-dev.berkeley.edu/bearfacts-apis/student' }
  let(:not_quite_base_url) { 'http://apis-dev.berkeley.edu:4343/bearfacts-apis/student' }
  let(:uid) { random_id }
  subject { TestMockable.new(settings, uri_matching: "#{base_url}/#{uid}", uri_end_of_path: uri_end_of_path) }

  context 'URI subpath match' do
    let(:uri_end_of_path) { nil }
    it 'matches if there are no trailing path separators' do
      subject.set_response
      request_signature = WebMock::RequestSignature.new('get', "#{not_quite_base_url}/#{uid}")
      expect(WebMock::StubRegistry.instance.registered_request? request_signature).to be_present
    end
    it 'also matches a longer path' do
      subject.set_response
      request_signature = WebMock::RequestSignature.new('get', "#{not_quite_base_url}/#{uid}/reg/finalexams")
      expect(WebMock::StubRegistry.instance.registered_request? request_signature).to be_present
    end
  end

  context 'URI end-of-path match' do
    let(:uri_end_of_path) { true }
    it 'matches if there are no trailing path separators' do
      subject.set_response
      request_signature = WebMock::RequestSignature.new('get', "#{not_quite_base_url}/#{uid}")
      expect(WebMock::StubRegistry.instance.registered_request? request_signature).to be_present
    end
    it 'does not match a longer path' do
      subject.set_response
      request_signature = WebMock::RequestSignature.new('get', "#{not_quite_base_url}/#{uid}/reg/finalexams")
      expect(WebMock::StubRegistry.instance.registered_request? request_signature).to be_nil
    end
  end

end
