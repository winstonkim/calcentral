module CalCentralPages

  class SettingsPage

    include PageObject
    include CalCentralPages
    include ClassLogger

    h1(:page_heading, :xpath => '//h1[contains(.,"Settings")]')

    # bConnected
    div(:connected_as, :xpath => '//div[@data-ng-if="api.user.profile.googleEmail && api.user.profile.hasGoogleAccessToken"]')
    checkbox(:calendar_opt_in, :id => 'cc-settings-service-calendar-optin')
    button(:disconnect_button, :xpath => '//button[contains(.,"Disconnect")]')
    button(:disconnect_yes_button, :xpath => '//button[@data-ng-click="api.user.removeOAuth(service)"]')
    button(:disconnect_no_button, :xpath => '//button[@data-ng-click="showValidation = false"]')
    button(:connect_button, :xpath => '//button[@data-ng-click="api.user.enableOAuth(service)"]')

     def load_page
      logger.info('Loading settings page')
      navigate_to "#{WebDriverUtils.base_url}/settings"
    end

    def disconnect_bconnected
      logger.info('Checking if user is connected to Google')
      if disconnect_button_element.visible?
        logger.info('User is connected, so disconnecting from Google')
        disconnect_button
        WebDriverUtils.wait_for_element_and_click disconnect_yes_button_element
        disconnect_yes_button_element.when_not_present(timeout=WebDriverUtils.page_event_timeout)
        connect_button_element.when_visible timeout
        logger.info('Pausing so that OAuth token is revoked')
        sleep timeout
      else
        logger.info('User not connected')
      end
    end

  end
end
