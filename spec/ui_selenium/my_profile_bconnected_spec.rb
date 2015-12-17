describe 'Profile bConnected', :testui => true do

  if ENV["UI_TEST"]

    before(:each) do
      @driver = WebDriverUtils.launch_browser
    end

    after(:each) do
      WebDriverUtils.quit_browser(@driver)
    end

    context 'as any user' do

      before(:example) do
        @splash_page = CalCentralPages::SplashPage.new(@driver)
        @splash_page.load_page
        @splash_page.click_sign_in_button
        @cal_net = CalNetAuthPage.new(@driver)
        @cal_net.login(UserUtils.qa_username, UserUtils.qa_password)
        @bconnected_card = CalCentralPages::MyProfileBconnectedCard.new(@driver)
        @bconnected_card.load_page
        @bconnected_card.disconnect_bconnected
        google = GooglePage.new(@driver)
        google.connect_calcentral_to_google(UserUtils.qa_gmail_username, UserUtils.qa_gmail_password)
      end

      context 'when connected' do
        it 'shows "connected"' do
          @bconnected_card.load_page
          @bconnected_card.disconnect_button_element.when_visible(timeout=WebDriverUtils.page_load_timeout)
          expect(@bconnected_card.connect_button?).to be false
          expect(@bconnected_card.connected_as).to include("#{UserUtils.qa_gmail_username}")
        end
      end

      context 'when disconnecting' do
        it 'keeps you connected if you\'re not sure' do
          @bconnected_card.load_page
          @bconnected_card.connected_as_element.when_visible(timeout=WebDriverUtils.page_load_timeout)
          @bconnected_card.disconnect_button
          @bconnected_card.disconnect_no_button_element.when_visible(timeout=WebDriverUtils.page_event_timeout)
          @bconnected_card.disconnect_no_button
          @bconnected_card.wait_until(WebDriverUtils.page_event_timeout, 'Disconnect button has not appeared') do
            @bconnected_card.disconnect_button?
          end
          expect(@bconnected_card.connect_button?).to be false
          expect(@bconnected_card.connected_as).to include("#{UserUtils.qa_gmail_username}")
        end
        it 'disconnects you if you\'re sure' do
          @bconnected_card.load_page
          @bconnected_card.connected_as_element.when_visible(timeout=WebDriverUtils.page_load_timeout)
          @bconnected_card.disconnect_button
          @bconnected_card.disconnect_yes_button_element.when_visible(timeout=WebDriverUtils.page_event_timeout)
          @bconnected_card.disconnect_yes_button
          @bconnected_card.wait_until(WebDriverUtils.page_event_timeout, 'Connect button has not appeared') do
            @bconnected_card.connect_button?
          end
          expect(@bconnected_card.disconnect_button?).to be false
          expect(@bconnected_card.connected_as?).to be false
        end
      end

      context 'when a connected but opting out of CalCentral' do
        it 'disconnects the user from Google apps' do
          my_dashboard = CalCentralPages::MyDashboardPage.new(@driver)
          my_dashboard.opt_out
          @splash_page.wait_for_expected_title?
          @splash_page.click_sign_in_button
          @cal_net.login(UserUtils.qa_username, UserUtils.qa_password)
          my_dashboard.click_email_badge
          my_dashboard.wait_until(WebDriverUtils.page_load_timeout, 'Not-connected message has not appeared') do
            my_dashboard.email_not_connected_heading_element.visible?
          end
        end
      end

      context 'when connected as a user without current student classes' do
        it 'shows no "class calendar" option for a non-student' do
          @bconnected_card.load_page
          @bconnected_card.connected_as_element.when_visible(timeout=WebDriverUtils.page_load_timeout)
          expect(@bconnected_card.calendar_opt_in?).to be false
        end
      end

    end

    context 'as a student with current student enrollment' do

      context 'when connected' do
        before(:example) do
          @splash_page = CalCentralPages::SplashPage.new(@driver)
          @splash_page.load_page
          @splash_page.click_sign_in_button
          cal_net = CalNetAuthPage.new(@driver)
          cal_net.login(UserUtils.oski_username, UserUtils.oski_password)
          @bconnected_card = CalCentralPages::MyProfileBconnectedCard.new(@driver)
          @bconnected_card.load_page
          @bconnected_card.disconnect_bconnected
          google = GooglePage.new(@driver)
          google.connect_calcentral_to_google(UserUtils.oski_gmail_username, UserUtils.oski_gmail_password)
        end
        it 'shows a "class calendar" option' do
          @bconnected_card.load_page
          @bconnected_card.connected_as_element.when_visible(timeout=WebDriverUtils.page_load_timeout)
          expect(@bconnected_card.calendar_opt_in_element.enabled?).to be true
          expect(@bconnected_card.calendar_opt_in_element.checked?).to be false
        end
      end

    end
  end
end
