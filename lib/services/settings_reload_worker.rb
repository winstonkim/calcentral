class SettingsReloadWorker < TorqueBox::Messaging::MessageProcessor
  include ClassLogger

  def on_message(body)
    logger.info "Message received on #{ServerRuntime.get_settings['hostname']}"
    CalcentralConfig.reload_settings
  end

  def on_error(exception)
    logger.error "Got an exception handling a message: #{exception.inspect}"
    raise exception
  end

  def self.request_reload
    Messaging.publish '/topics/settings_reload'
  end
end
