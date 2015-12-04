module CalCentralPages

  class MyDashboardPage

    include PageObject
    include CalCentralPages
    include ClassLogger

    wait_for_expected_title('Dashboard | CalCentral', WebDriverUtils.page_load_timeout)

    h3(:connect_bconnected_heading, :xpath => '//h3[contains(.,"Connect to bConnected")]')
    button(:connect_bconnected_button, :xpath => '//button[contains(.,"Connect")]')
    h2(:notifications_heading, :xpath => '//h2[contains(.,"Notifications")]')

    # LIVE UPDATES
    div(:live_update_notice, :xpath => '//div[contains(.,"New data is available.")]')
    button(:live_update_load_button, :xpath => '//button[text()="Load"]')

    def load_page
      logger.info('Loading My Dashboard page')
      navigate_to "#{WebDriverUtils.base_url}/dashboard"
    end

    def click_live_update_button(timeout)
      logger.info('Waiting for live update button for ' + timeout.to_s + ' seconds')
      live_update_load_button_element.when_visible(timeout=timeout)
      logger.info('Found button, clicking it')
      live_update_load_button
      live_update_load_button_element.when_not_visible(timeout=WebDriverUtils.page_event_timeout)
      sleep(WebDriverUtils.page_event_timeout)
    end
  end
end
