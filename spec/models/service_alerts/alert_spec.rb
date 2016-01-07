describe ServiceAlerts::Alert do

  let(:title) { 'Have You Seen This Server?' }
  let(:body) { '<p>All eyes were on ETS headquarters this morning as a top-secret experiment went awry.</p>' }

  it 'insists on a title' do
    attrs = {
      body: body,
      display: true
    }
    expect { described_class.create! attrs }.to raise_error ActiveRecord::RecordInvalid
  end

  it 'insists on a body' do
    attrs = {
      title: title,
      display: true
    }
    expect { described_class.create! attrs }.to raise_error ActiveRecord::RecordInvalid
  end

  it 'sets publication date to creation date by default' do
    alert = described_class.create!(title: title, body: body)
    expect(alert.publication_date).to eq Time.zone.today.in_time_zone.to_datetime
  end

  it 'allows override of publication date' do
    date = Time.zone.parse('2015-12-25').to_datetime
    alert = described_class.create!(title: title, body: body, publication_date: date)
    expect(alert.publication_date).to eq date
  end

  it 'does not display newly created alerts by default' do
    alert = described_class.create!(title: title, body: body)
    expect(alert.display).to eq false
  end

  context 'when no alert is set to display' do
    before do
      described_class.create!(title: 'Title 1', body: 'Body 1')
      described_class.create!(title: 'Title 2', body: 'Body 2')
    end

    it 'returns nil for latest' do
      expect(described_class.get_latest).to eq nil
    end
  end

  context 'when an alert is set to display' do
    before do
      described_class.create!(title: 'Title 1', body: 'Body 1')
      described_class.create!(title: 'Title 2', body: 'Body 2', display: true)
      described_class.create!(title: 'Title 3', body: 'Body 3', display: true)
      described_class.create!(title: 'Title 4', body: 'Body 4')
    end

    it 'returns the most recent alert for which display is true' do
      expect(described_class.get_latest.title).to eq 'Title 3'
    end

    it 'returns feed attributes' do
      feed = described_class.get_latest.to_feed
      expect(feed.keys).to match_array [:title, :body, :timestamp]
      expect(feed.values).to all be_present
      expect(feed[:timestamp].keys).to match_array [:epoch, :dateTime, :dateString]
      expect(feed[:timestamp].values).to all be_present
    end

    it 'allows custom snippet to be set' do
      described_class.create!(title: title, body: body, snippet: 'WHAT? - AND LIKEWISE - WHERE?', display: true)
      feed = described_class.get_latest.to_feed
      expect(feed.keys).to match_array [:title, :body, :timestamp, :snippet]
      expect(feed[:snippet]).to eq 'WHAT? - AND LIKEWISE - WHERE?'
    end
  end
end
