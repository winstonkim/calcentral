module CalCentralPages

  class MyToolboxPage

    include PageObject
    include CalCentralPages
    include ClassLogger

    # View As
    text_area(:view_as_input, :id => 'cc-toolbox-view-as-uid')
    button(:view_as_submit_button, :xpath => '//button[text()="Submit"]')
    div(:saved_users, :xpath => '//div[@class="cc-toolbox-user-section ng-scope"][1]')
    button(:clear_saved_users_button, :xpath => '//span[text()="Saved Users"]/following-sibling::button[text()="clear all"]')
    elements(:saved_user_view_as_button, :div, :xpath => '//div[@class="cc-toolbox-user-section ng-scope"][1]//button[@data-ng-click="admin.updateIDField(user.ldap_uid)"]')
    elements(:saved_user_delete_button, :button, :xpath => '//button[text()="delete"]')
    div(:recent_users, :xpath => '//div[@class="cc-toolbox-user-section ng-scope"][2]')
    button(:clear_recent_users_button, :xpath => '//span[text()="Recent Users"]/following-sibling::button[text()="clear all"]')
    elements(:recent_user_view_as_button, :div, :xpath => '//div[@class="cc-toolbox-user-section ng-scope"][2]//button[@data-ng-click="admin.updateIDField(user.ldap_uid)"]')
    elements(:recent_user_save_button, :button, :xpath => '//button[text()="save"]')

    # UID/SID Lookup
    text_area(:lookup_input, :id => 'cc-toolbox-id')
    button(:lookup_button, :xpath => '//button[text()="Look Up"]')
    table(:lookup_results_table, :xpath => '//form[@data-ng-submit="admin.lookupUser()"]//table')

    def load_page
      navigate_to "#{WebDriverUtils.base_url}/toolbox"
    end

    # VIEW-AS

    def view_as_user(id)
      WebDriverUtils.wait_for_element_and_type(view_as_input_element, id)
      view_as_submit_button
    end

    def clear_all_saved_users
      saved_users_element.when_present(timeout=WebDriverUtils.page_load_timeout)
      WebDriverUtils.wait_for_element_and_click clear_saved_users_button_element if clear_saved_users_button?
    end

    def view_as_first_saved_user
      wait_until(timeout=WebDriverUtils.page_load_timeout) { saved_user_view_as_button_elements.any? }
      saved_user_view_as_button_elements[0].click
    end

    def clear_all_recent_users
      recent_users_element.when_present(timeout=WebDriverUtils.page_load_timeout)
      clear_recent_users_button if clear_recent_users_button?
    end

    def view_as_first_recent_user
      wait_until(timeout=WebDriverUtils.page_load_timeout) { recent_user_view_as_button_elements.any? }
      recent_user_view_as_button_elements[0].click
    end

    def save_first_recent_user
      wait_until(timeout=WebDriverUtils.page_load_timeout) { recent_user_save_button_elements.any? }
      recent_user_save_button_elements[0].click
    end

    # LOOK UP USER

    def look_up_user(id)
      WebDriverUtils.wait_for_element_and_type(lookup_input_element, id)
      lookup_button
    end

  end

end