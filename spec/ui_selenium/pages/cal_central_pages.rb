module CalCentralPages

  include PageObject
  include ClassLogger

  PageObject.javascript_framework = (:angularjs)

  # Header
  link(:my_dashboard_link, :text => 'My Dashboard')
  link(:my_academics_link, :text => 'My Academics')
  link(:my_campus_link, :text => 'My Campus')
  link(:my_finances_link, :text => 'My Finances')

  # Email Badge
  button(:email_badge, :xpath => '//button[@title="bMail"]')
  div(:email_count, :xpath => '//button[@title="bMail"]/div[@data-ng-bind="badge.count"]')
  h4(:unread_email_heading, :xpath => '//h4[text()="Unread bMail Messages"]')
  link(:email_one_link, :xpath => '//button[@title="bMail"]/following-sibling::div//a[@data-ng-href="http://bmail.berkeley.edu/"]')
  div(:email_one_sender, :xpath => '//button[@title="bMail"]/following-sibling::div//div[@data-ng-bind="item.editor"]')
  span(:email_one_subject, :xpath => '//button[@title="bMail"]/following-sibling::div//span[@data-ng-bind="item.title"]')
  span(:email_one_summary, :xpath => '//button[@title="bMail"]/following-sibling::div//span[@data-ng-bind="item.summary"]')

  # Status Popover
  list_item(:status_loading, :xpath => '//li[@data-ng-show="statusLoading"]')
  button(:status_icon, :xpath => '//button[@title="Show your status"]')
  span(:status_alert_count, :xpath => '//span[@data-ng-if="hasAlerts"]/span[@data-ng-bind="count"]')
  h4(:status_popover_heading, :xpath => '//div[@class="cc-popover-title"]/h4')
  div(:no_status_alert, :xpath => '//div[@class="cc-popover-noitems ng-scope"]')
  image(:no_status_alert_icon, :xpath => '//div[@class="cc-popover-noitems ng-scope"]/i[@class="cc-left fa fa-check-circle cc-icon-green"]')
  div(:reg_status_alert, :xpath => '//li[@data-ng-if="studentInfo.regStatus.needsAction && api.user.profile.features.regstatus"]//div')
  link(:reg_status_alert_link, :xpath => '//li[@data-ng-if="studentInfo.regStatus.needsAction && api.user.profile.features.regstatus"]/a')
  image(:reg_status_alert_icon, :xpath => '//li[@data-ng-if="studentInfo.regStatus.needsAction && api.user.profile.features.regstatus"]//i[@class="cc-left fa fa-exclamation-circle cc-icon-red"]')
  div(:block_status_alert, :xpath => '//li[@data-ng-if="studentInfo.regBlock.needsAction || studentInfo.regBlock.errored"]//div')
  span(:block_status_alert_number, :xpath => '//li[@data-ng-if="studentInfo.regBlock.needsAction || studentInfo.regBlock.errored"]//span[@data-ng-bind="studentInfo.regBlock.activeBlocks"]')
  link(:block_status_alert_link, :xpath => '//li[@data-ng-if="studentInfo.regBlock.needsAction || studentInfo.regBlock.errored"]//a')
  image(:block_status_alert_icon, :xpath => '//li[@data-ng-if="studentInfo.regBlock.needsAction || studentInfo.regBlock.errored"]//i[@class="cc-left fa fa-exclamation-circle cc-icon-red"]')
  div(:amount_due_status_alert, :xpath => '//li[@data-ng-if="minimumAmountDue && minimumAmountDue > 0"]//div')
  link(:amount_due_status_alert_link, :xpath => '//li[@data-ng-if="minimumAmountDue && minimumAmountDue > 0"]//a')
  image(:amount_due_status_alert_icon, :xpath => '//li[@data-ng-if="minimumAmountDue && minimumAmountDue > 0"]//i[@class="cc-left fa fa-exclamation-triangle cc-icon-gold"]')
  image(:amount_overdue_status_alert_icon, :xpath => '//li[@data-ng-if="minimumAmountDue && minimumAmountDue > 0"]//i[@class="cc-left fa fa-exclamation-circle cc-icon-red"]')
  div(:finaid_status_alert, :xpath => '//li[@data-ng-if="countUndatedFinaid > 0"]//div')
  link(:finaid_status_alert_link, :xpath => '//li[@data-ng-if="countUndatedFinaid > 0"]//a')
  image(:finaid_status_alert_icon, :xpath => '//li[@data-ng-if="countUndatedFinaid > 0"]//i[@class="cc-left fa fa-exclamation-circle cc-icon-red"]')
  span(:finaid_status_alert_count, :xpath => '//li[@data-ng-if="countUndatedFinaid > 0"]//span[@data-ng-bind="countUndatedFinaid"]')

  # Gear - Settings, Profile, Log Out
  link(:gear_link, :xpath => '//i[@class="fa fa-cog"]')
  button(:settings_link, :xpath => '//button[@data-ng-click="api.popover.clickThrough(\'Gear - Settings\');api.util.redirect(\'settings\')"]')
  button(:profile_link, :xpath => '//button[contains(text(),"Profile")]')
  button(:logout_link, :xpath => '//button[contains(text(),"Log out")]')

  # Footer
  div(:toggle_footer_link, :xpath => '//div[@class=\'cc-footer-berkeley\']')
  button(:opt_out_button, :xpath => '//button[text()="Opt out of CalCentral"]')
  button(:opt_out_yes, :xpath => '//button[@data-ng-click="api.user.optOut()"]')
  button(:out_out_no, :xpath => '//button[@data-ng-click="deleteSelf=false"]')
  text_field(:basic_auth_uid_input, :name => 'email')
  text_field(:basic_auth_password_input, :name => 'password')
  button(:basic_auth_login_button, :xpath => '//button[contains(text(),"Login")]')

  def click_my_dashboard_link
    logger.info('Clicking My Dashboard link')
    WebDriverUtils.wait_for_page_and_click my_dashboard_link_element
  end

  def click_my_academics_link
    logger.info('Clicking My Academics link')
    WebDriverUtils.wait_for_page_and_click my_academics_link_element
  end

  def click_my_campus_link
    logger.info('Clicking My Campus link')
    WebDriverUtils.wait_for_page_and_click my_campus_link_element
  end

  def click_my_finances_link
    logger.info('Clicking My Finances link')
    WebDriverUtils.wait_for_page_and_click my_finances_link_element
  end

  def click_email_badge
    logger.info('Clicking email badge on Dashboard')
    WebDriverUtils.wait_for_page_and_click email_badge_element
  end

  def show_unread_email
    unless unread_email_heading_element.visible?
      email_badge
      unread_email_heading_element.when_visible(timeout=WebDriverUtils.page_event_timeout)
    end
  end

  def wait_for_status_popover
    logger.debug('Waiting for status popover to be present')
    status_icon_element.when_present(timeout=WebDriverUtils.page_event_timeout)
  end

  def status_popover_visible?
    status_icon_element.when_visible(timeout=WebDriverUtils.page_event_timeout)
    true
  rescue
    false
  end

  def open_status_popover
    logger.debug('Waiting for status popover to become visible')
    status_icon_element.when_visible(timeout=WebDriverUtils.page_load_timeout)
    unless status_popover_heading_element.visible?
      logger.debug('Clicking status popover to open the content')
      status_icon
    end
  end

  def click_reg_status_alert
    WebDriverUtils.wait_for_page_and_click reg_status_alert_element
  end

  def click_block_status_alert
    WebDriverUtils.wait_for_element_and_click block_status_alert_element
  end

  def click_settings_link
    logger.info('Clicking the link to the Settings page')
    WebDriverUtils.wait_for_page_and_click gear_link_element
    WebDriverUtils.wait_for_element_and_click settings_link_element
  end

  def click_profile_link(driver)
    logger.info 'Clicking Profile link'
    WebDriverUtils.wait_for_page_and_click gear_link_element
    WebDriverUtils.wait_for_element_and_click profile_link_element
    CalCentralPages::MyProfilePage::MyProfileBasicInfoCard.new driver
  end

  def click_logout_link
    logger.info('Logging out of CalCentral')
    WebDriverUtils.wait_for_page_and_click gear_link_element
    WebDriverUtils.wait_for_element_and_click logout_link_element
  end

  def log_out(splash_page)
    navigate_to WebDriverUtils.base_url
    toggle_footer_link_element.when_visible(timeout=WebDriverUtils.page_load_timeout)
    if title.include? 'Dashboard'
      click_logout_link
      splash_page.sign_in_element.when_visible timeout
    end
  end

  def opt_out
    logger.info('Opting out of CalCentral')
    WebDriverUtils.wait_for_page_and_click toggle_footer_link_element
    WebDriverUtils.wait_for_element_and_click opt_out_button_element
    WebDriverUtils.wait_for_element_and_click opt_out_yes_element
  end

  def basic_auth(uid)
    logger.info('Logging in using basic auth')
    WebDriverUtils.wait_for_page_and_click toggle_footer_link_element
    WebDriverUtils.wait_for_element_and_type(basic_auth_uid_input_element, uid)
    WebDriverUtils.wait_for_element_and_type(basic_auth_password_input_element, UserUtils.basic_auth_pass)
    login = basic_auth_login_button_element
    basic_auth_login_button
    login.when_not_present(timeout=WebDriverUtils.page_load_timeout)
    basic_auth_login_button_element.when_present(timeout=WebDriverUtils.page_load_timeout)
  end

  def click_class_link_by_text(link_text)
    WebDriverUtils.wait_for_page_and_click link_element(:link_text => link_text)
  end

  def click_class_link_by_url(url)
    WebDriverUtils.wait_for_page_and_click link_element(:xpath => "//a[@href='#{url}']")
  end

end
