describe Advising::MyAdvising do
  let (:uid) { '61889' }
  let (:fake_oski_model) { Advising::MyAdvising.new(uid, fake: true) }
  let (:real_oski_model) { Advising::MyAdvising.new(uid, fake: false) }
  let (:advising_uri) { URI.parse(Settings.advising_proxy.base_url) }

  describe 'proper caching behaviors' do
    before do
      # Avoid caching student ID checks.
      allow_any_instance_of(Advising::MyAdvising).to receive(:lookup_student_id).and_return(11667051)
    end

    context 'on success' do
      subject { fake_oski_model }
      include_context 'Live Updates cache'
      it 'should write to cache' do
        subject.get_feed
      end
    end

    context 'server 404s' do
      subject { real_oski_model }
      include_context 'Live Updates cache'
      before do
        stub_request(:any, /.*#{advising_uri.hostname}.*/).to_return(status: 404)
      end
      after { WebMock.reset! }
      it 'should write to cache' do
        subject.get_feed
      end
    end

    context 'server errors' do
      subject { real_oski_model }
      include_context 'short-lived Live Updates cache'
      after(:each) { WebMock.reset! }

      context 'unreachable remote server (connection errors)' do
        before do
          stub_request(:any, /.*#{advising_uri.hostname}.*/).to_raise(Errno::ECONNREFUSED)
        end
        it 'reports an error' do
          feed = subject.get_feed
          expect(feed[:body]).to eq('Failed to connect with your department\'s advising system.')
          expect(feed[:statusCode]).to eq 503
        end
      end

      context 'error on remote server (5xx errors)' do
        let!(:status) { 506 }
        include_context 'expecting logs from server errors'
        before do
          stub_request(:any, /.*#{advising_uri.hostname}.*/).to_return(status: status)
        end
        it 'reports an error' do
          feed = subject.get_feed
          expect(feed[:body]).to eq('Failed to connect with your department\'s advising system.')
          expect(feed[:statusCode]).to eq 503
        end
      end

      context 'timeouts' do
        before do
          stub_request(:any, /.*#{advising_uri.hostname}.*/).to_raise(Timeout::Error)
        end
        it 'reports an error' do
          feed = subject.get_feed
          expect(feed[:body]).to eq('Failed to connect with your department\'s advising system.')
          expect(feed[:statusCode]).to eq 503
        end
      end
    end

    context 'disabled feature flag' do
      subject { real_oski_model }
      include_context 'Live Updates cache'
      before do
        Settings.features.stub(:advising).and_return(false)
      end
      it 'returns an empty feed' do
        feed = subject.get_feed
        expect(feed[:name]).to be_nil
        expect(feed[:body]).to be_nil
      end
    end

  end

  describe '#get_parsed_response' do

    context 'fetching fake data feed' do
      subject { fake_oski_model.get_parsed_response }

      it_behaves_like 'a polite HTTP client'

      it 'has correctly parsed JSON' do
        expect(subject[:sid]).to eq '11667051'
        expect(subject[:pastAppointments]).to_not be_empty
        expect(subject[:futureAppointments]).to_not be_empty
      end

      it 'filters out and logs bad data' do
        expect(Rails.logger).to receive(:warn).at_least(4).times
        all_appointments = subject[:pastAppointments].concat subject[:futureAppointments]
        expect(all_appointments.select{ |appt| appt[:dateTime].blank? }).to be_empty
        expect(all_appointments.select{ |appt| appt[:location].blank? && app[:method].blank? }).to be_empty
        expect(all_appointments.select{ |appt| appt[:staff][:name].blank? }).to be_empty
        expect(all_appointments.select{ |appt| appt[:urlToEditAppointment].blank? }).to be_empty
      end

      it 'orders appointments by date' do
        expect(subject[:futureAppointments].sort_by { |appt| appt[:dateTime] }).to eq subject[:futureAppointments]
        expect(subject[:pastAppointments].sort_by { |appt| appt[:dateTime] }.reverse).to eq subject[:pastAppointments]
      end
    end

    context 'with a non-student' do
      subject { Advising::MyAdvising.new('211159').get_parsed_response }
      it 'should return empty response' do
        expect(subject).to eq({})
      end
    end

    context 'getting real data feed', testext: true do
      subject { real_oski_model.get_parsed_response }
      it 'reports success' do
        expect(subject[:statusCode]).to eq 200
      end
    end

    context 'server 404s' do
      before do
        stub_request(:any, /.*#{advising_uri.hostname}.*/).to_return(status: 404)
      end
      after(:each) { WebMock.reset! }
      subject { real_oski_model.get_parsed_response }
      it 'returns the expected data' do
        expect(subject[:body]).to eq('No advising data could be found for your account.')
        expect(subject[:statusCode]).to eq 404
      end
    end

  end

end
