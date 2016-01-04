class ApiMyFinAidPage

  include PageObject
  include ClassLogger

  def get_json(driver)
    logger.info('Parsing JSON from /api/my/finaid')
    navigate_to "#{WebDriverUtils.base_url}/api/my/finaid"
    wait = Selenium::WebDriver::Wait.new(:timeout => WebDriverUtils.page_load_timeout)
    wait.until { driver.find_element(:xpath => '//pre[contains(.,"Finaid::Merged")]') }
    body = driver.find_element(:xpath, '//pre').text
    @parsed = JSON.parse(body)
  end

  def title(item)
    item['title']
  end

  def summary(item)
    item['summary']
  end

  def source(item)
    item['source']
  end

  def type(item)
    item['type']
  end

  def date(item)
    item['date']
  end

  def date_epoch(item)
    Time.at(date(item)['epoch'])
  end

  def term_year(item)
    item['termYear']
  end

  def status(item)
    item['status']
  end

  def source_url(item)
    item['sourceUrl']
  end

  def all_activity
    @parsed['activities']
  end

  def undated_messages
    dateless_messages = all_activity.select { |item| date(item).blank? }
    dateless_messages.sort_by { |item| title(item) }
  end

  def dated_messages
    dated_messages = all_activity.select { |item| !date(item).blank? }
    sorted_messages = dated_messages.sort_by { |item| date_epoch(item) }
    sorted_messages.reverse!
  end

  def all_messages_sorted
    undated_messages.push(*dated_messages)
  end

  def all_message_titles_sorted
    titles = []
    all_messages_sorted.each { |message| titles << title(message) }
    titles
  end

  def all_message_summaries_sorted
    summaries = []
    all_messages_sorted.each { |message| summaries << summary(message).gsub(/\s+/, '') }
    summaries
  end

  def all_message_sources_sorted
    sources = []
    all_messages_sorted.each { |message| sources << source(message) }
    sources
  end

  def all_message_dates_sorted
    dates = []
    all_messages_sorted.each do |message|
      unless date(message).blank?
        date = date_epoch(message)
        ui_date = date.strftime('%b %-d')
        # Include the year in the visible date if it does not match the current year
        ui_date << date.strftime(' %Y') if date.strftime('%Y') != Date.today.strftime('%Y')
        dates << ui_date
      end
    end
    dates
  end

  def all_message_source_urls_sorted
    urls = []
    all_messages_sorted.each { |message| urls << source_url(message).gsub(/\/\s*\z/, '') }
    urls
  end

  def all_message_types_sorted
    types = []
    all_messages_sorted.each { |message| types << type(message) }
    types
  end

  def all_undated_alert_messages
    undated_alerts = []
    undated_messages.each { |message| undated_alerts << message if type(message) == 'alert' }
    undated_alerts
  end

  def all_message_years_sorted
    years = []
    all_messages_sorted.each { |message| years << term_year(message) }
    years
  end

  def all_message_statuses_sorted
    statuses = []
    all_messages_sorted.each { |message| statuses << status(message) }
    statuses
  end

end
