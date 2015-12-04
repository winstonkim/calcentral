describe 'GoogleEventsList (Recent Items)' do

  before(:each) do
    @random_id = Time.now.to_f.to_s.gsub(".", "")
  end

  it "should return a fake calendar list response for processing badges info on calendar feed" do
    proxy = GoogleApps::EventsRecentItems.new(:fake => true)
    response_array = [
      proxy.recent_items.first
    ]
    response_array.size.should == 1
    response_array[0].data['items'].size.should == 47
  end

end
