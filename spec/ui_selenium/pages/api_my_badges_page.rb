class ApiMyBadgesPage

  include PageObject
  include ClassLogger

  def get_json(driver)
    logger.info('Parsing JSON from /api/my/badges')
    navigate_to "#{WebDriverUtils.base_url}/api/my/badges"
    body = driver.find_element(:xpath, '//pre').text
    @parsed = JSON.parse(body)
  end

  def residency_summary
    @parsed['studentInfo']['californiaResidency']['summary']
  end

  def residency_explanation
    @parsed['studentInfo']['californiaResidency']['explanation']
  end

  def residency_needs_action
    @parsed['studentInfo']['californiaResidency']['needsAction']
  end

  def reg_status_summary
    @parsed['studentInfo']['regStatus']['summary']
  end

  def reg_status_explanation
    @parsed['studentInfo']['regStatus']['explanation']
  end

  def reg_status_needs_action
    @parsed['studentInfo']['regStatus']['needsAction']
  end

  def active_block_needs_action
    @parsed['studentInfo']['regBlock']['needsAction']
  end

  def active_block_number_str
    @parsed['studentInfo']['regBlock']['activeBlocks'].to_s
  end

end
