describe 'The My Dashboard task manager', :testui => true do

  if ENV["UI_TEST"]

    today = Date.today
    yesterday = Date.yesterday
    tomorrow = Date.tomorrow
    task_wait = WebDriverUtils.google_task_timeout

    before(:all) do
      @driver = WebDriverUtils.launch_browser
    end

    after(:all) do
      WebDriverUtils.quit_browser @driver
    end

    before(:context) do
      splash_page = CalCentralPages::SplashPage.new @driver
      cal_net_auth_page = CalNetAuthPage.new @driver
      splash_page.log_into_dashboard(@driver, cal_net_auth_page, UserUtils.qa_username, UserUtils.qa_password)
      bconnected_card = CalCentralPages::MyProfileBconnectedCard.new @driver
      bconnected_card.load_page
      bconnected_card.disconnect_bconnected
      google_page = GooglePage.new @driver
      google_page.connect_calcentral_to_google(UserUtils.qa_gmail_username, UserUtils.qa_gmail_password)
      @tasks_card = CalCentralPages::MyDashboardPage::MyDashboardTasksCard.new @driver
      @tasks_card.scheduled_tasks_tab_element.when_present WebDriverUtils.page_load_timeout
    end

    context 'for Google tasks' do

      before(:example) do
        @tasks_card.delete_all_tasks
      end

      context 'when adding a task' do

        it 'allows a user to create only one task at a time' do
          @tasks_card.click_new_task
          @tasks_card.new_task_title_input_element.when_visible task_wait
          @tasks_card.click_new_task
          @tasks_card.new_task_title_input_element.when_not_visible task_wait
        end

        it 'allows a user to cancel the creation of a new task' do
          @tasks_card.click_new_task
          @tasks_card.edit_new_task('Cancel Task', today, nil)
          @tasks_card.click_cancel_task
          expect(@tasks_card.today_task_elements.any?).to be false
        end

        it 'requires that a new task have a title' do
          @tasks_card.click_new_task
          @tasks_card.edit_new_task(nil, today, nil)
          expect(@tasks_card.add_new_task_button_element.enabled?).to be false
          @tasks_card.click_cancel_task
        end

        it 'requires that a new task have a valid date format' do
          @tasks_card.click_new_task
          @tasks_card.edit_new_task('Bad Date Task', '08/08/14', nil)
          @tasks_card.new_task_date_validation_error_element.when_visible task_wait
          expect(@tasks_card.add_new_task_button_element.enabled?).to be false
          expect(@tasks_card.new_task_date_validation_error).to include('Please use mm/dd/yyyy date format')
        end

        it 'allows a user to create a task without a note' do
          @tasks_card.add_task('Note-less task', today, nil)
          @tasks_card.wait_for_today_tasks
          @tasks_card.verify_task(@tasks_card.today_task_elements[0], 'Note-less task', today, '')
        end

        it 'allows the user to show more overdue tasks in ascending date order' do
          (1..11).each do |i|
            title = "task #{i.to_s}"
            date = today - i
            @tasks_card.add_task(title, date, nil)
            if i > 1 && (i-1) % 10 == 0
              WebDriverUtils.wait_for_page_and_click @tasks_card.overdue_show_more_button_element
            end
            expect(@tasks_card.overdue_task_title_elements[0].text).to eql(title)
            expect(@tasks_card.overdue_task_count).to eql(i.to_s)
          end
        end

        it 'allows the user to show more tasks due today in ascending creation sequence' do
          (1..11).each do |i|
            title = "task #{i.to_s}"
            @tasks_card.add_task(title, today, nil)
            if i > 1 && (i-1) % 10 == 0
              WebDriverUtils.wait_for_page_and_click @tasks_card.today_show_more_button_element
            end
            expect(@tasks_card.today_task_title_elements.last.text).to eql(title)
            expect(@tasks_card.today_task_count).to eql(i.to_s)
          end
        end

        it 'allows the user to show more tasks due in the future in ascending date order' do
          (1..11).each do |i|
            title = "task #{i.to_s}"
            date = today + i
            @tasks_card.add_task(title, date, nil)
            if i > 1 && (i-1) % 10 == 0
              WebDriverUtils.wait_for_page_and_click @tasks_card.future_show_more_button_element
            end
            expect(@tasks_card.future_task_title_elements.last.text).to eql(title)
            expect(@tasks_card.future_task_count).to eql(i.to_s)
          end
        end

        it 'allows the user to show more unscheduled tasks in descending creation sequence' do
          (1..11).each do |i|
            title = "task #{i.to_s}"
            @tasks_card.add_task(title, nil, nil)
            if i > 1 && (i-1) % 10 == 0
              WebDriverUtils.wait_for_page_and_click @tasks_card.unsched_show_more_button_element
            end
            expect(@tasks_card.unsched_task_title_elements[0].text).to eql(title)
            expect(@tasks_card.unsched_task_count).to eql(i.to_s)
          end
        end
      end

      context 'when editing an existing task' do

        it 'allows a user to edit the title of an existing task' do
          @tasks_card.add_task('Original Title', today, nil)
          @tasks_card.wait_for_today_tasks
          @tasks_card.verify_task(@tasks_card.today_task_elements[0], 'Original Title', today, '')
          @tasks_card.edit_and_save_task(@tasks_card.today_task_elements[0], 'Edited Title', nil, nil)
          @tasks_card.verify_task(@tasks_card.today_task_elements[0], 'Edited Title', today, '')
        end

        it 'requires that an edited task have a title' do
          @tasks_card.add_task('Task Must Have a Title', today, nil)
          @tasks_card.wait_for_today_tasks
          @tasks_card.verify_task(@tasks_card.today_task_elements[0], 'Task Must Have a Title', today, '')
          @tasks_card.edit_today_task(0, '', nil, nil)
          expect(@tasks_card.today_task_save_button_elements[0].enabled?).to be false
        end

        it 'allows a user to make an unscheduled task overdue' do
          @tasks_card.add_task('Unscheduled task that will be due yesterday', nil, nil)
          @tasks_card.wait_for_unsched_tasks
          @tasks_card.edit_and_save_task(@tasks_card.unsched_task_elements[0], nil, yesterday, nil)
          @tasks_card.wait_until(task_wait) { @tasks_card.unsched_task_elements.empty? }
          @tasks_card.wait_for_overdue_tasks
          @tasks_card.verify_task(@tasks_card.overdue_task_elements[0], 'Unscheduled task that will be due yesterday', yesterday, '')
        end

        it 'allows a user to make an unscheduled task due today' do
          @tasks_card.add_task('Unscheduled task that will be due today', nil, nil)
          @tasks_card.wait_for_unsched_tasks
          @tasks_card.edit_and_save_task(@tasks_card.unsched_task_elements[0], nil, today, nil)
          @tasks_card.wait_until(task_wait) { @tasks_card.unsched_task_elements.empty? }
          @tasks_card.wait_for_today_tasks
          @tasks_card.verify_task(@tasks_card.today_task_elements[0], 'Unscheduled task that will be due today', today, '')
        end

        it 'allows a user to make an unscheduled task due in the future' do
          @tasks_card.add_task('Unscheduled task that will be scheduled for tomorrow', nil, nil)
          @tasks_card.wait_for_unsched_tasks
          @tasks_card.edit_and_save_task(@tasks_card.unsched_task_elements[0], nil, tomorrow, nil)
          @tasks_card.wait_for_future_tasks
          @tasks_card.verify_task(@tasks_card.future_task_elements[0], 'Unscheduled task that will be scheduled for tomorrow', tomorrow, '')
        end

        it 'allows a user to make an overdue task unscheduled' do
          @tasks_card.add_task('Overdue task that will be unscheduled', yesterday, nil)
          @tasks_card.wait_for_overdue_tasks
          @tasks_card.edit_and_save_task(@tasks_card.overdue_task_elements[0], nil, '', nil)
          @tasks_card.wait_until(task_wait) { @tasks_card.overdue_task_elements.empty? }
          @tasks_card.wait_for_unsched_tasks
          @tasks_card.verify_task(@tasks_card.unsched_task_elements[0], 'Overdue task that will be unscheduled', today, '')
        end

        it 'requires that an edited task have a valid date format' do
          @tasks_card.add_task('Today task', today, nil)
          @tasks_card.wait_for_today_tasks
          @tasks_card.edit_today_task(0, nil, '08/11/14', '')
          expect(@tasks_card.today_task_save_button_elements[0].enabled?).to be false
          @tasks_card.today_task_date_validation_error_element.when_visible task_wait
          expect(@tasks_card.today_task_date_validation_error).to include('Please use mm/dd/yyyy date format')
        end

        it 'allows a user to add notes to an existing task' do
          @tasks_card.add_task('Note-less task', nil, nil)
          @tasks_card.wait_for_unsched_tasks
          @tasks_card.edit_and_save_task(@tasks_card.unsched_task_elements[0], nil, nil, 'A note for the note-less task')
          @tasks_card.verify_task(@tasks_card.unsched_task_elements[0], 'Note-less task', today, 'A note for the note-less task')
        end

        it 'allows a user to edit notes on an existing task' do
          @tasks_card.add_task('Task with a note', nil, 'The original note for this task')
          @tasks_card.wait_for_unsched_tasks
          @tasks_card.edit_and_save_task(@tasks_card.unsched_task_elements[0], nil, nil, 'The edited note for this task')
          @tasks_card.verify_task(@tasks_card.unsched_task_elements[0], 'Task with a note', today, 'The edited note for this task')
        end

        it 'allows a user to remove notes from an existing task' do
          @tasks_card.add_task('Task with a note', nil, 'The note for this task')
          @tasks_card.wait_for_unsched_tasks
          @tasks_card.edit_and_save_task(@tasks_card.unsched_task_elements[0], nil, nil, '')
          @tasks_card.verify_task(@tasks_card.unsched_task_elements[0], 'Task with a note', nil, '')
        end

        it 'allows a user to edit multiple scheduled tasks at once' do
          @tasks_card.add_task('Overdue task', WebDriverUtils.ui_date_input_format(yesterday), 'Overdue task notes')
          @tasks_card.add_task('Today task', WebDriverUtils.ui_date_input_format(today), 'Today task notes')
          @tasks_card.add_task('Future task', WebDriverUtils.ui_date_input_format(tomorrow), 'Future task notes')
          # Edit each task without saving
          @tasks_card.edit_overdue_task(0, 'Overdue task edited', (yesterday - 1), 'Overdue task notes edited')
          @tasks_card.edit_today_task(0, 'Today task edited', nil, 'Today task notes edited')
          @tasks_card.edit_future_task(0, 'Future task edited', (tomorrow + 1), 'Future task notes edited')
          # Save all three tasks
          WebDriverUtils.wait_for_page_and_click @tasks_card.overdue_task_save_button_elements[0]
          WebDriverUtils.wait_for_page_and_click @tasks_card.future_task_save_button_elements[0]
          WebDriverUtils.wait_for_page_and_click @tasks_card.today_task_save_button_elements[0]
          # Verify edits successful
          @tasks_card.verify_task(@tasks_card.overdue_task_elements[0], 'Overdue task edited', (yesterday - 1), 'Overdue task notes edited')
          @tasks_card.verify_task(@tasks_card.today_task_elements[0], 'Today task edited', today, 'Today task notes edited')
          @tasks_card.verify_task(@tasks_card.future_task_elements[0], 'Future task edited', (tomorrow + 1), 'Future task notes edited')
        end

        it 'allows a user to cancel the edit of an existing task' do
          @tasks_card.add_task('The original task title', today, 'The original task notes')
          WebDriverUtils.wait_for_page_and_click @tasks_card.today_task_toggle_elements[0]
          WebDriverUtils.wait_for_element_and_click @tasks_card.today_task_edit_button_elements[0]
          WebDriverUtils.wait_for_element_and_click @tasks_card.today_task_cancel_button_elements[0]
          @tasks_card.today_task_edit_button_elements[0].when_not_visible task_wait
        end
      end

      context 'when completing tasks' do

        it 'allows the user to show more completed tasks sorted first by descending task date and then by descending task creation date' do
          expected_task_titles = []
          WebDriverUtils.wait_for_element_and_click @tasks_card.scheduled_tasks_tab_element
          (1..3).each do |i|
            title ="overdue task #{i.to_s}"
            @tasks_card.add_task(title, yesterday, nil)
            @tasks_card.wait_until(task_wait) { @tasks_card.overdue_task_title_elements[0].text == title }
            expected_task_titles << title
            @tasks_card.complete_task(@tasks_card.overdue_task_elements[0])
          end
          (1..3).each do |i|
            title = "today task #{i.to_s}"
            @tasks_card.add_task(title, today, nil)
            @tasks_card.wait_until(task_wait) { @tasks_card.today_task_title_elements[0].text == title }
            expected_task_titles << title
            @tasks_card.complete_task(@tasks_card.today_task_elements[0])
          end
          (1..3).each do |i|
            title = "future task #{i.to_s}"
            @tasks_card.add_task(title, tomorrow, nil)
            @tasks_card.wait_until(task_wait) { @tasks_card.future_task_title_elements[0].text == title }
            expected_task_titles << title
            @tasks_card.complete_task(@tasks_card.future_task_elements[0])
          end
          WebDriverUtils.wait_for_element_and_click @tasks_card.unsched_tasks_tab_element
          (1..3).each do |i|
            title = "unscheduled task #{i.to_s}"
            @tasks_card.add_task(title, nil, nil)
            @tasks_card.wait_until(task_wait) { @tasks_card.unsched_task_title_elements[0].text == title }
            expected_task_titles << title
            @tasks_card.complete_task(@tasks_card.unsched_task_elements[0])
          end
          @tasks_card.wait_for_completed_tasks
          expect(@tasks_card.completed_task_count).to eql('12')
          @tasks_card.completed_show_more_button
          @tasks_card.completed_show_more_button_element.when_not_visible(timeout=task_wait)
          titles = []
          @tasks_card.completed_task_title_elements.each { |title| titles << title.text }
          expect(titles).to eql(expected_task_titles.reverse!)
        end

        it 'allows the user to mark a completed tasks as un-completed' do
          @tasks_card.add_task('Today to be completed', today, nil)
          @tasks_card.complete_task(@tasks_card.today_task_elements[0])
          @tasks_card.wait_for_completed_tasks
          expect(@tasks_card.completed_task_title_elements[0].text).to eql('Today to be completed')
          WebDriverUtils.wait_for_element_and_click @tasks_card.completed_task_cbx_elements[0]
          @tasks_card.wait_until(task_wait) { @tasks_card.completed_task_elements.empty? }
          @tasks_card.wait_for_today_tasks
          expect(@tasks_card.today_task_title_elements[0].text).to eql('Today to be completed')
        end
      end
    end

  end
end
