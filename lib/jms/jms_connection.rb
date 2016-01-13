require 'activemq.rb'

# JMS Connection is relatively heavyweight & can be used across multiple threads.
# JMS Session and Consumer must be single-threaded.
# Connection start should be called after sessions & consumers are set up.
class JmsConnection
  attr_reader :connection

  def initialize(connection_settings)
    java_import 'javax.jms.Session'
    java_import 'org.apache.activemq.ActiveMQConnectionFactory'

    # JMS listeners do not generate any exceptions when a server goes away. Without failover,
    # a dangling connection and session will stay open and useless forever (or until
    # explicitly closed). With failover, at least there is a chance that a useful
    # connection will be re-established sometime.
    # The alternative is to periodically attempt to create a new test connection on
    # another thread, and then close the dangling connection/session from there.
    # The following URL means: wait at least 5 minutes before trying to reconnect; wait at most
    # 4 hours; do not block failed "send" attempts forever.
    failover_url = "failover:(#{connection_settings.url})?initialReconnectDelay=300000&maxReconnectDelay=14400000&timeout=30000&randomize=false"

    connection_factory = connection_settings.username ?
        ActiveMQConnectionFactory.new(connection_settings.username, connection_settings.password, failover_url) :
        ActiveMQConnectionFactory.new(failover_url)
    @connection = connection_factory.createConnection
    @sessions = []
    @consumers = []

    connection_settings.queues.each do |queue_name|
      session = @connection.createSession(false, Session.AUTO_ACKNOWLEDGE)
      consumer = session.createConsumer(session.createQueue(queue_name))
      @sessions << session
      @consumers << consumer
    end
  end

  def close
    # ActiveMQ API specifies "no need to close the sessions, producers, and consumers of a closed connection."
    @connection.close()
  end

  def count
    @listeners.map(&:count).reduce(:+) if @listeners
  end

  def finalize
    close
  end

  def start_listening_with(&proc)
    @listeners = @consumers.map do |consumer|
      listener = JmsTextListener.new(&proc)
      consumer.setMessageListener(listener)
      listener
    end
    @connection.start
  end

  # For testing with local ActiveMQ deployments.
  def send_message(message_text, queue_name)
    begin
      session = @connection.createSession(false, Session.AUTO_ACKNOWLEDGE)
      producer = session.createProducer(session.createQueue(queue_name))
      producer.send(session.createTextMessage(message_text))
    ensure
      producer.close() if producer
      session.close() if session
    end
  end
  # For testing error handling of text message listener.
  def send_bytes_message(queue_name)
    begin
      session = @connection.createSession(false, Session.AUTO_ACKNOWLEDGE)
      producer = session.createProducer(session.createQueue(queue_name))
      producer.send(session.createBytesMessage())
    ensure
      producer.close() if producer
      session.close() if session
    end
  end

end
