describe 'My Dashboard Up Next card', :testui => true do

  if ENV["UI_TEST"]

    include ClassLogger

    today = Time.now
    id = today.to_i.to_s

    before(:all) do
      @driver = WebDriverUtils.launch_browser
    end

    after(:all) do
      WebDriverUtils.quit_browser(@driver)
    end

    before(:context) do
      splash_page = CalCentralPages::SplashPage.new(@driver)
      splash_page.load_page
      splash_page.click_sign_in_button
      cal_net_auth_page = CalNetAuthPage.new(@driver)
      cal_net_auth_page.login(UserUtils.qa_username, UserUtils.qa_password)
      settings_page = CalCentralPages::SettingsPage.new(@driver)
      settings_page.load_page
      settings_page.disconnect_bconnected

      @google = GooglePage.new(@driver)
      @google.connect_calcentral_to_google(UserUtils.qa_gmail_username, UserUtils.qa_gmail_password)

      # On Up Next card, get initial set of today's events
      @up_next_card = CalCentralPages::MyDashboardPage::MyDashboardUpNextCard.new(@driver)
      @up_next_card.load_page
      @up_next_card.events_list_element.when_present(timeout=WebDriverUtils.page_load_timeout)
      @up_next_card.day_element.when_visible(timeout=WebDriverUtils.page_event_timeout)
      logger.info("Up Next card shows #{@up_next_card.day} #{@up_next_card.date}")
      @initial_event_times = @up_next_card.all_event_times
      @initial_event_summaries = @up_next_card.all_event_summaries
      @initial_event_locations = @up_next_card.all_event_locations
      @initial_hangout_link_count = @up_next_card.hangout_link_count
      @initial_event_start_times = @up_next_card.all_event_start_times
      @initial_event_end_times = @up_next_card.all_event_end_times
      @initial_event_organizers = @up_next_card.all_event_organizers
      logger.info("#{@initial_event_times}")
      logger.info("#{@initial_event_summaries}")
      logger.info("#{@initial_event_locations}")
      logger.info("There are #{@initial_hangout_link_count.to_s} video links")
      logger.info("#{@initial_event_start_times}")
      logger.info("#{@initial_event_end_times}")
      logger.info("#{@initial_event_organizers}")

      # Put a new event on Google calendar
      @google.load_calendar
      @event_title = "Event #{id}"
      @event_location = "#{id} DWINELLE"
      event = @google.send_invite(@event_title, @event_location)
      @event_time = event[0].strftime("%l:%M\n%p").gsub(' ', '')
      @event_start_time = event[0].strftime("%-m/%-d/%y %l:%M %P").gsub('  ', ' ')
      @event_end_time = event[1].strftime("%-m/%-d/%y %l:%M %P").gsub('  ', ' ')
      logger.info("Event start time is #{@event_start_time}")
      logger.info("Event end time is #{@event_end_time}")

      # On the Dashboard, wait a moment for the new event.  If not there, clear the cache and reload.
      UserUtils.clear_cache(@driver, splash_page, @up_next_card)
      splash_page.click_sign_in_button
      cal_net_auth_page.login(UserUtils.qa_username, UserUtils.qa_password)
      @up_next_card.load_page
      @up_next_card.events_list_element.when_present(timeout=WebDriverUtils.page_load_timeout)
      @up_next_card.day_element.when_visible(timeout=WebDriverUtils.page_event_timeout)
    end

    it 'shows the current date' do
      expect(@up_next_card.day).to eql(today.strftime("%A"))
      expect(@up_next_card.date).to eql(today.strftime("%^b %-d"))
    end

    it 'shows event times' do
      logger.info("#{@up_next_card.all_event_times}")
      expect(@up_next_card.all_event_times).to eql(@initial_event_times.push(@event_time).sort)
    end

    it 'shows event summaries' do
      logger.info("#{@up_next_card.all_event_summaries}")
      expect(@up_next_card.all_event_summaries).to eql(@initial_event_summaries.push(@event_title).sort)
    end

    it 'shows event locations' do
      logger.info("#{@up_next_card.all_event_locations}")
      expect(@up_next_card.all_event_locations).to eql(@initial_event_locations.push(@event_location).sort)
    end

    context 'when expanded' do

      it 'shows event video hangout links' do
        expect(@up_next_card.hangout_link_count).to eql(@initial_hangout_link_count + 1)
      end

      it 'shows event start times' do
        logger.info("#{@up_next_card.all_event_start_times}")
        expect(@up_next_card.all_event_start_times).to eql(@initial_event_start_times.push(@event_start_time).sort)
      end

      it 'shows event end times' do
        logger.info("#{@up_next_card.all_event_end_times}")
        expect(@up_next_card.all_event_end_times).to eql(@initial_event_end_times.push(@event_end_time).sort)
      end

      it 'shows event organizers' do
        logger.info("#{@up_next_card.all_event_organizers}")
        expect(@up_next_card.all_event_organizers).to eql(@initial_event_organizers.push(UserUtils.qa_gmail_username).sort)
      end

    end

    context 'when opening an event in bCal' do

      before(:example) do
        @up_next_card.click_bcal_link id
        @up_next_card.wait_until(timeout=WebDriverUtils.page_event_timeout) { @driver.window_handles.length > 1 }
        @driver.switch_to.window(@driver.window_handles.last)
        @google.event_title_displayed_element.when_visible(timeout=WebDriverUtils.page_load_timeout)
      end

      it 'shows the event detail in Google calendar' do
        expect(@google.event_title_displayed).to eql(@event_title)
      end

      after(:example) do
        if @driver.window_handles.length > 1 && @driver.title.include?('Google Calendar')
          @driver.close
        end
        @driver.switch_to.window(@driver.window_handles.first)
      end

    end
  end
end
