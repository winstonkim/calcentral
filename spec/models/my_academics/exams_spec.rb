describe 'MyAcademics::Exams' do

  it 'should format fake Bearfacts exam data correctly' do
    proxy = Bearfacts::Exams.new({:user_id => '61889', :fake => true})
    Bearfacts::Exams.stub(:new).and_return(proxy)

    feed = {}
    MyAcademics::Exams.new('61889').merge(feed)

    feed[:examSchedule].should_not be_nil
    feed[:examSchedule][0][:course_code].should == 'Psychology C120'
    feed[:examSchedule][0][:time].should == '8:00 AM'
    feed[:examSchedule][0][:locations][0][:raw].should == '390 HEARST MIN'
    feed[:examSchedule][0][:locations][0]['roomNumber'].should == '390'
    feed[:examSchedule][0][:locations][0]['display'].should == 'Hearst Memorial Mining Building'
    # Make sure the date epoch matches the expected date.
    Time.at(feed[:examSchedule][0][:date][:epoch]).to_s.start_with?('2013-05-14').should be_truthy
    # making sure sorting works in right order
    feed[:examSchedule][0][:date][:epoch].should < feed[:examSchedule][1][:date][:epoch]
    feed[:examSchedule][1][:date][:epoch].should < feed[:examSchedule][2][:date][:epoch]
  end

  it 'should properly handle a student with an exam in an unparseable room' do
    proxy = Bearfacts::Exams.new({:user_id => '865826', :fake => true})
    Bearfacts::Exams.stub(:new).and_return(proxy)

    feed = {}
    MyAcademics::Exams.new('865826').merge(feed)

    feed[:examSchedule].should_not be_nil
    feed[:examSchedule][0][:locations][0][:raw].should == 'F295 HAAS'
  end

  context 'when exam does not match current term' do
    before { Settings.terms.stub(:fake_now).and_return(DateTime.parse('2013-07-10')) }
    it 'should not return any exam schedules' do
      proxy = Bearfacts::Exams.new({:user_id => '865826', :fake => true})
      Bearfacts::Exams.stub(:new).and_return(proxy)
      feed = {}
      MyAcademics::Exams.new('865826').merge(feed)
      feed[:examSchedule].should be_nil
    end
  end

  it 'should handle badly formatted BearfactsExamProxy XML responses' do
    proxy = Bearfacts::Exams.new({:user_id => '865826', :fake => true})
    Bearfacts::Exams.stub(:new).and_return(proxy)
    Bearfacts::Exams.any_instance.stub(:get).and_return({xml_doc: nil, statusCode: 200})

    feed = {}
    MyAcademics::Exams.new('865826').merge(feed)

    feed[:examSchedule].should be_nil
  end

  context 'failing bearfacts proxy' do
    let(:feed) {{}}
    let(:uid) {'212381'}
    before(:each) do
      stub_request(:any, /#{Regexp.quote(Settings.bearfacts_proxy.base_url)}.*/).to_raise(Errno::EHOSTUNREACH)
      Bearfacts::Profile.new({:user_id => uid, :fake => false})
    end

    subject do
      MyAcademics::Exams.new(uid).merge(feed)
      feed
    end

    it { should be_blank }

  end
end
