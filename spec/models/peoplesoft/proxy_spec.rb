require 'spec_helper'

describe Peoplesoft::Proxy do

  include SafeJsonParser

  let (:real_oski_proxy) { Peoplesoft::Proxy.new({user_id: '61889', fake: false}) }
  let (:peoplesoft_uri) { URI.parse(Settings.peoplesoft_proxy.base_url) }

  context 'proper caching behaviors', testext: true do
    include_context 'it writes to the cache'
    it 'should write to cache' do
      real_oski_proxy.get
    end
  end

  context 'getting real data feed', testext: true do
    subject { real_oski_proxy.get }
    it 'should have some expected data' do
      expect(subject).to be
      expect(subject[:statusCode]).to eq 200
      json = safe_json subject[:body]
      expect(json['STUDENT_STUDY_TERM']['STUDENTID']).to eq 'SR12201'
    end
  end

  context 'server errors' do
    include_context 'short-lived cache write of Hash on failures'
    after(:each) { WebMock.reset! }
    subject { real_oski_proxy.get }

    context 'unreachable remote server (connection errors)' do
      before(:each) {
        stub_request(:any, /.*#{peoplesoft_uri.hostname}.*/).to_raise(Errno::ECONNREFUSED)
      }
      its([:body]) { should eq('An error occurred retrieving data from Peoplesoft. Please try again later.') }
      its([:statusCode]) { should eq(503) }
    end

    context 'error on remote server (5xx errors)' do
      before(:each) {
        stub_request(:any, /.*#{peoplesoft_uri.hostname}.*/).to_return(status: 506)
      }
      its([:body]) { should eq('An error occurred retrieving data from Peoplesoft. Please try again later.') }
      its([:statusCode]) { should eq(506) }
    end
  end

end
