describe Notifications::JmsMessageHandler do

  before do
    @reg_status_processor = double("Notifications::RegStatusEventProcessor")
    @reg_status_processor.stub(:process) { true }
    @final_grades_processor = double("Notifications::FinalGradesEventProcessor")
    @final_grades_processor.stub(:process) { true }

    @handler = Notifications::JmsMessageHandler.new [@reg_status_processor, @final_grades_processor]
    @messages = []
    File.open("#{Rails.root}/fixtures/jms_recordings/ist_jms.txt", 'r').each("\n\n") do |msg_yaml|
      msg = YAML::load(msg_yaml)
      @messages.push msg
    end
  end

  it "should do nothing with an empty message" do
    @handler.handle({})
  end

  it "should process a fake test jms message" do
    @reg_status_processor.should_receive(:process)
    @final_grades_processor.should_receive(:process)
    @handler.handle @messages[0]
  end

  it "should handle malformed JSON gracefully" do
    suppress_rails_logging {
      @handler.handle({:text => "pure lunacy"})
    }
  end

  it "should pass the proper timestamp to the processors" do
    @reg_status_processor.should_receive(:process).exactly(4).times
    @final_grades_processor.should_receive(:process).exactly(4).times
    @messages.each do |msg|
      @handler.handle msg
    end
  end
end
