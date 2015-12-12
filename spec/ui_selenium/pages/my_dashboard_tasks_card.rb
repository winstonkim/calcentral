module CalCentralPages

  class MyDashboardTasksCard < MyDashboardPage

    include PageObject
    include CalCentralPages
    include ClassLogger

    # TASKS
    button(:completed_tasks_tab, :xpath => '//button[contains(.,"completed")]')
    paragraph(:no_tasks_message, :xpath => '//p[contains(.,"You have no tasks and assignments.")]')
    button(:scheduled_tasks_tab, :xpath => '//button[contains(.,"scheduled")]')
    button(:unsched_tasks_tab, :xpath => '//button[contains(.,"unscheduled")]')
    button(:completed_tasks_tab, :xpath => '//div[@class="cc-widget-tasks-container"]//li[3]/button')

    # ADD/EDIT TASK
    button(:new_task_button, :xpath => '//button[contains(.,"New Task")]')
    text_field(:new_task_title_input, :xpath => '//input[@data-ng-model="addEditTask.title"]')
    text_field(:new_task_date_input, :xpath => '//input[@data-ng-model="addEditTask.dueDate"]')
    text_field(:new_task_notes_input, :xpath => '//textarea[@data-ng-model="addEditTask.notes"]')
    button(:add_new_task_button, :xpath => '//button[contains(.,"Add Task")]')
    button(:cancel_new_task_button, :xpath => '//button[contains(.,"Cancel")]')
    paragraph(:new_task_date_validation_error, :xpath => '//p[@data-ng-show="cc_widget_tasks_form.add_task_due_date.$error.ccDateValidator"]')

    # TASKS: OVERDUE
    span(:overdue_task_count, :xpath => '//span[@data-ng-bind="overdueTasks.length"]')
    elements(:overdue_task, :list_item, :xpath => '//li[@data-ng-repeat="task in overdueTasks | limitTo: overdueLimit"]')
    elements(:overdue_task_toggle, :link, :xpath => '//li[@data-ng-repeat="task in overdueTasks | limitTo: overdueLimit"]//span[contains(.,"Show more information about")]')
    elements(:overdue_task_edit_button, :button, :xpath => '//li[@data-ng-repeat="task in overdueTasks | limitTo: overdueLimit"]//button[contains(.,"Edit")]')
    elements(:overdue_task_delete_button, :button, :xpath => '//li[@data-ng-repeat="task in overdueTasks | limitTo: overdueLimit"]//button[contains(.,"Delete")]')
    elements(:overdue_task_save_button, :button, :xpath => '//li[@data-ng-repeat="task in overdueTasks | limitTo: overdueLimit"]//button[contains(.,"Save")]')
    elements(:overdue_task_cbx, :checkbox, :xpath => '//li[@data-ng-repeat="task in overdueTasks | limitTo: overdueLimit"]//input[@id="cc-widget-tasks-checkbox-0"]')
    elements(:overdue_task_title, :div, :xpath => '//li[@data-ng-repeat="task in overdueTasks | limitTo: overdueLimit"]//strong')
    elements(:overdue_task_title_input, :text_field, :xpath => '//li[@data-ng-repeat="task in overdueTasks | limitTo: overdueLimit"]//input[@data-ng-model="addEditTask.title"]')
    elements(:overdue_task_course, :span, :xpath => '//li[@data-ng-repeat="task in overdueTasks | limitTo: overdueLimit"]//span[@data-ng-bind="task.course_code"]')
    elements(:overdue_task_date, :div, :xpath => '//li[@data-ng-repeat="task in overdueTasks | limitTo: overdueLimit"]//div[@class="cc-widget-tasks-col cc-widget-tasks-col-date"]/span')
    elements(:overdue_task_time, :div, :xpath => '//li[@data-ng-repeat="task in overdueTasks | limitTo: overdueLimit"]//div[@data-ng-if="task.emitter==\'bCourses\' && task.dueDate.hasTime"]')
    elements(:overdue_task_date_input, :text_field, :xpath => '//li[@data-ng-repeat="task in overdueTasks | limitTo: overdueLimit"]//input[@name="add_task_due_date"]')
    elements(:overdue_task_notes, :div, :xpath => '//li[@data-ng-repeat="task in overdueTasks | limitTo: overdueLimit"]//div[@data-ng-bind-html="task.notes | linky"]')
    elements(:overdue_task_notes_input, :text_field, :xpath => '//li[@data-ng-repeat="task in overdueTasks | limitTo: overdueLimit"]//textarea[@data-ng-model="addEditTask.notes"]')
    elements(:overdue_task_bcourses_link, :link, :xpath => '//li[@data-ng-repeat="task in overdueTasks | limitTo: overdueLimit"]//a[contains(.,"View in bCourses")]')
    button(:overdue_show_more_button, :xpath => '//div[@data-cc-show-more-limit="overdueLimit"]/button')

    # TASKS: TODAY
    span(:today_task_count, :xpath => '//span[@data-ng-bind="dueTodayTasks.length"]')
    elements(:today_task, :list_item, :xpath => '//li[@data-ng-repeat="task in dueTodayTasks | limitTo: dueTodayLimit"]')
    elements(:today_task_toggle, :link, :xpath => '//li[@data-ng-repeat="task in dueTodayTasks | limitTo: dueTodayLimit"]//span[contains(.,"Show more information about")]')
    elements(:today_task_edit_button, :button, :xpath => '//li[@data-ng-repeat="task in dueTodayTasks | limitTo: dueTodayLimit"]//button[contains(.,"Edit")]')
    elements(:today_task_delete_button, :button, :xpath => '//li[@data-ng-repeat="task in dueTodayTasks | limitTo: dueTodayLimit"]//button[contains(.,"Delete")]')
    elements(:today_task_save_button, :button, :xpath => '//li[@data-ng-repeat="task in dueTodayTasks | limitTo: dueTodayLimit"]//button[contains(.,"Save")]')
    elements(:today_task_cancel_button, :button, :xpath => '//li[@data-ng-repeat="task in dueTodayTasks | limitTo: dueTodayLimit"]//button[contains(.,"Cancel")][2]')
    elements(:today_task_cbx, :checkbox, :xpath => '//li[@data-ng-repeat="task in dueTodayTasks | limitTo: dueTodayLimit"]//input[@id="cc-widget-tasks-checkbox-0"]')
    elements(:today_task_title, :div, :xpath => '//li[@data-ng-repeat="task in dueTodayTasks | limitTo: dueTodayLimit"]//strong')
    elements(:today_task_course, :span, :xpath => '//li[@data-ng-repeat="task in dueTodayTasks | limitTo: dueTodayLimit"]//span[@data-ng-bind="task.course_code"]')
    elements(:today_task_title_input, :text_field, :xpath => '//li[@data-ng-repeat="task in dueTodayTasks | limitTo: dueTodayLimit"]//input[@data-ng-model="addEditTask.title"]')
    elements(:today_task_date, :div, :xpath => '//li[@data-ng-repeat="task in dueTodayTasks | limitTo: dueTodayLimit"]//div[@class="cc-widget-tasks-col cc-widget-tasks-col-date"]/span')
    elements(:today_task_time, :div, :xpath => '//li[@data-ng-repeat="task in dueTodayTasks | limitTo: dueTodayLimit"]//div[@data-ng-if="task.emitter==\'bCourses\' && task.dueDate.hasTime"]')
    elements(:today_task_date_input, :text_field, :xpath => '//li[@data-ng-repeat="task in dueTodayTasks | limitTo: dueTodayLimit"]//input[@name="add_task_due_date"]')
    elements(:today_task_notes, :div, :xpath => '//li[@data-ng-repeat="task in dueTodayTasks | limitTo: dueTodayLimit"]//div[@data-ng-bind-html="task.notes | linky"]')
    elements(:today_task_notes_input, :text_field, :xpath => '//li[@data-ng-repeat="task in dueTodayTasks | limitTo: dueTodayLimit"]//textarea[@data-ng-model="addEditTask.notes"]')
    elements(:today_task_bcourses_link, :link, :xpath => '//li[@data-ng-repeat="task in dueTodayTasks | limitTo: dueTodayLimit"]//a[contains(.,"View in bCourses")]')
    button(:today_show_more_button, :xpath => '//div[@data-cc-show-more-limit="dueTodayLimit"]/button')
    paragraph(:today_task_date_validation_error, :xpath => '//li[@data-ng-repeat="task in dueTodayTasks | limitTo: dueTodayLimit"]//p[@data-ng-show="cc_widget_tasks_form.add_task_due_date.$error.ccDateValidator"]')

    # TASKS: FUTURE
    span(:future_task_count, :xpath => '//span[@data-ng-bind="futureTasks.length"]')
    elements(:future_task, :list_item, :xpath => '//li[@data-ng-repeat="task in futureTasks | limitTo: futureLimit"]')
    elements(:future_task_toggle, :link, :xpath => '//li[@data-ng-repeat="task in futureTasks | limitTo: futureLimit"]//span[contains(.,"Show more information about")]')
    elements(:future_task_edit_button, :button, :xpath => '//li[@data-ng-repeat="task in futureTasks | limitTo: futureLimit"]//button[contains(.,"Edit")]')
    elements(:future_task_delete_button, :button, :xpath => '//li[@data-ng-repeat="task in futureTasks | limitTo: futureLimit"]//button[contains(.,"Delete")]')
    elements(:future_task_save_button, :button, :xpath => '//li[@data-ng-repeat="task in futureTasks | limitTo: futureLimit"]//button[contains(.,"Save")]')
    elements(:future_task_cbx, :checkbox, :xpath => '//li[@data-ng-repeat="task in futureTasks | limitTo: futureLimit"]//input[@id="cc-widget-tasks-checkbox-0"]')
    elements(:future_task_title, :div, :xpath => '//li[@data-ng-repeat="task in futureTasks | limitTo: futureLimit"]//strong')
    elements(:future_task_course, :span, :xpath => '//li[@data-ng-repeat="task in futureTasks | limitTo: futureLimit"]//span[@data-ng-bind="task.course_code"]')
    elements(:future_task_title_input, :text_field, :xpath => '//li[@data-ng-repeat="task in futureTasks | limitTo: futureLimit"]//input[@data-ng-model="addEditTask.title"]')
    elements(:future_task_date, :div, :xpath => '//li[@data-ng-repeat="task in futureTasks | limitTo: futureLimit"]//div[@class="cc-widget-tasks-col cc-widget-tasks-col-date"]/span')
    elements(:future_task_time, :div, :xpath => '//li[@data-ng-repeat="task in futureTasks | limitTo: futureLimit"]//div[@data-ng-if="task.emitter==\'bCourses\' && task.dueDate.hasTime"]')
    elements(:future_task_date_input, :text_field, :xpath => '//li[@data-ng-repeat="task in futureTasks | limitTo: futureLimit"]//input[@name="add_task_due_date"]')
    elements(:future_task_notes, :div, :xpath => '//li[@data-ng-repeat="task in futureTasks | limitTo: futureLimit"]//div[@data-ng-bind-html="task.notes | linky"]')
    elements(:future_task_notes_input, :text_field, :xpath => '//li[@data-ng-repeat="task in futureTasks | limitTo: futureLimit"]//textarea[@data-ng-model="addEditTask.notes"]')
    elements(:future_task_bcourses_link, :link, :xpath => '//li[@data-ng-repeat="task in futureTasks | limitTo: futureLimit"]//a[contains(.,"View in bCourses")]')
    button(:future_show_more_button, :xpath => '//div[@data-cc-show-more-limit="futureLimit"]/button')

    # TASKS: UNSCHEDULED
    span(:unsched_task_count, :xpath => '//span[@data-ng-bind="unscheduledTasks.length"]')
    elements(:unsched_task, :list_item, :xpath => '//li[@data-ng-repeat="task in unscheduledTasks | limitTo:unscheduledLimit"]')
    elements(:unsched_task_toggle, :link, :xpath => '//li[@data-ng-repeat="task in unscheduledTasks | limitTo:unscheduledLimit"]//span[contains(.,"Show more information about")]')
    elements(:unsched_task_edit_button, :button, :xpath => '//li[@data-ng-repeat="task in unscheduledTasks | limitTo:unscheduledLimit"]//button[contains(.,"Edit")]')
    elements(:unsched_task_delete_button, :button, :xpath => '//li[@data-ng-repeat="task in unscheduledTasks | limitTo:unscheduledLimit"]//button[contains(.,"Delete")]')
    elements(:unsched_task_save_button, :button, :xpath => '//li[@data-ng-repeat="task in unscheduledTasks | limitTo:unscheduledLimit"]//button[contains(.,"Save")]')
    elements(:unsched_task_cbx, :checkbox, :xpath => '//li[@data-ng-repeat="task in unscheduledTasks | limitTo:unscheduledLimit"]//input[@id="cc-widget-tasks-checkbox-0"]')
    elements(:unsched_task_title, :div, :xpath => '//li[@data-ng-repeat="task in unscheduledTasks | limitTo:unscheduledLimit"]//strong')
    elements(:unsched_task_title_input, :text_field, :xpath => '//li[@data-ng-repeat="task in unscheduledTasks | limitTo:unscheduledLimit"]//input[@data-ng-model="addEditTask.title"]')
    elements(:unsched_task_date, :div, :xpath => '//li[@data-ng-repeat="task in unscheduledTasks | limitTo:unscheduledLimit"]//div[@data-ng-if="task.updatedDate && task.bucket === \'Unscheduled\'"]/span')
    elements(:unsched_task_date_input, :text_field, :xpath => '//li[@data-ng-repeat="task in unscheduledTasks | limitTo:unscheduledLimit"]//input[@name="add_task_due_date"]')
    elements(:unsched_task_notes, :div, :xpath => '//li[@data-ng-repeat="task in unscheduledTasks | limitTo:unscheduledLimit"]//div[@data-ng-bind-html="task.notes | linky"]')
    elements(:unsched_task_notes_input, :text_field, :xpath => '//li[@data-ng-repeat="task in unscheduledTasks | limitTo:unscheduledLimit"]//textarea[@data-ng-model="addEditTask.notes"]')
    button(:unsched_show_more_button, :xpath => '//div[@data-cc-show-more-limit="unscheduledLimit"]/button')

    # TASKS: COMPLETED
    span(:completed_task_count, :xpath => '//span[@data-ng-bind="completedTasks.length"]')
    elements(:completed_task, :list_item, :xpath => '//li[@data-ng-repeat="task in completedTasks | limitTo:completedLimit"]')
    elements(:completed_task_toggle, :link, :xpath => '//li[@data-ng-repeat="task in completedTasks | limitTo:completedLimit"]//span[contains(.,"Show more information about")]')
    elements(:completed_task_edit_button, :button, :xpath => '//li[@data-ng-repeat="task in completedTasks | limitTo:completedLimit"]//button[contains(.,"Edit")]')
    elements(:completed_task_delete_button, :button, :xpath => '//li[@data-ng-repeat="task in completedTasks | limitTo:completedLimit"]//button[contains(.,"Delete")]')
    elements(:completed_task_cbx, :checkbox, :xpath => '//li[@data-ng-repeat="task in completedTasks | limitTo:completedLimit"]//input[@id="cc-widget-tasks-checkbox-0"]')
    elements(:completed_task_title, :div, :xpath => '//li[@data-ng-repeat="task in completedTasks | limitTo:completedLimit"]//strong')
    elements(:completed_task_title_input, :text_field, :xpath => '//li[@data-ng-repeat="task in completedTasks | limitTo:completedLimit"]//input[@data-ng-model="addEditTask.title"]')
    elements(:completed_task_date_input, :text_field, :xpath => '//li[@data-ng-repeat="task in completedTasks | limitTo:completedLimit"]//input[@name="add_task_due_date"]')
    elements(:completed_task_notes_input, :text_field, :xpath => '//li[@data-ng-repeat="task in completedTasks | limitTo:completedLimit"]//textarea[@data-ng-model="addEditTask.notes"]')
    button(:completed_show_more_button, :xpath => '//div[@data-cc-show-more-limit="completedLimit"]/button')

    # ADD NEW TASK

    def edit_new_task(title, date, note)
      logger.info "Task title is #{title}, date is #{date}, and note is #{note}"
      WebDriverUtils.wait_for_element_and_type(new_task_title_input_element, title) unless title.nil?
      WebDriverUtils.wait_for_element_and_type(new_task_date_input_element, date) unless date.nil?
      WebDriverUtils.wait_for_element_and_type(new_task_notes_input_element, note) unless note.nil?
    end

    def click_add_task_button
      logger.info('Clicking add task button')
      WebDriverUtils.wait_for_page_and_click add_new_task_button_element
      add_new_task_button_element.when_not_visible(timeout=WebDriverUtils.google_task_timeout)
    end

    # OVERDUE TASKS

    def edit_overdue_task(index, title, date, note)
      logger.info "Task title is #{title}, date is #{date}, and note is #{note}"
      WebDriverUtils.wait_for_element_and_type(overdue_task_title_input_elements[index], title) unless title.nil?
      WebDriverUtils.wait_for_element_and_type(overdue_task_date_input_elements[index], date) unless date.nil?
      WebDriverUtils.wait_for_element_and_type(overdue_task_notes_input_elements[index], note) unless note.nil?
    end

    def complete_overdue_task(index)
      logger.info('Completing overdue task')
      overdue_task_cbx_elements[index].when_visible(timeout=WebDriverUtils.google_task_timeout)
      task_count = overdue_task_count.to_i
      WebDriverUtils.wait_for_element_and_click overdue_task_cbx[index]
      wait_until(WebDriverUtils.google_task_timeout) { overdue_task_count.to_i == (task_count - 1) }
    end

    def delete_all_overdue_tasks
      WebDriverUtils.wait_for_page_and_click scheduled_tasks_tab_element
      while overdue_task_toggle_elements.any? do
        task_count = overdue_task_count.to_i
        WebDriverUtils.wait_for_element_and_click overdue_task_toggle_elements[0]
        WebDriverUtils.wait_for_element_and_click overdue_task_delete_button_elements[0]
        wait_until(WebDriverUtils.google_task_timeout) { overdue_task_count.to_i == (task_count - 1) }
      end
    end

    # TODAY'S TASKS

    def edit_today_task(index, title, date, note)
      logger.info "Task title is #{title}, date is #{date}, and note is #{note}"
      WebDriverUtils.wait_for_element_and_type(today_task_title_input_elements[index], title) unless title.nil?
      WebDriverUtils.wait_for_element_and_type(today_task_date_input_elements[index], date) unless date.nil?
      WebDriverUtils.wait_for_element_and_type(today_task_notes_input_elements[index], note) unless note.nil?
    end

    def complete_today_task(index)
      logger.info('Completing today task')
      today_task_cbx_elements[index].when_visible(timeout=WebDriverUtils.google_task_timeout)
      task_count = today_task_count.to_i
      WebDriverUtils.wait_for_element_and_click today_task_cbx[index]
      wait_until(WebDriverUtils.google_task_timeout) { today_task_count.to_i == (task_count - 1) }
    end

    def delete_all_today_tasks
      WebDriverUtils.wait_for_page_and_click scheduled_tasks_tab_element
      while today_task_toggle_elements.any? do
        task_count = today_task_count.to_i
        WebDriverUtils.wait_for_element_and_click today_task_toggle_elements[0]
        WebDriverUtils.wait_for_element_and_click today_task_delete_button_elements[0]
        wait_until(WebDriverUtils.google_task_timeout) { today_task_count.to_i == (task_count - 1) }
      end
    end

    # FUTURE TASKS

    def edit_future_task(index, title, date, note)
      logger.info "Task title is #{title}, date is #{date}, and note is #{note}"
      WebDriverUtils.wait_for_element_and_type(future_task_title_input_elements[index], title) unless title.nil?
      WebDriverUtils.wait_for_element_and_type(future_task_date_input_elements[index], date) unless date.nil?
      WebDriverUtils.wait_for_element_and_type(future_task_notes_input_elements[index], note) unless note.nil?
    end

    def complete_future_task(index)
      logger.info('Completing future task')
      future_task_cbx_elements[index].when_visible(timeout=WebDriverUtils.google_task_timeout)
      task_count = future_task_count.to_i
      WebDriverUtils.wait_for_element_and_click future_task_cbx[index]
      wait_until(WebDriverUtils.google_task_timeout) { future_task_count.to_i == (task_count - 1) }
    end

    def delete_all_future_tasks
      WebDriverUtils.wait_for_page_and_click scheduled_tasks_tab_element
      while future_task_toggle_elements.any? do
        task_count = future_task_count.to_i
        WebDriverUtils.wait_for_element_and_click future_task_toggle_elements[0]
        WebDriverUtils.wait_for_element_and_click future_task_delete_button_elements[0]
        wait_until(WebDriverUtils.google_task_timeout) { future_task_count.to_i == (task_count - 1) }
      end
    end

    # UNSCHEDULED TASKS

    def edit_unsched_task(index, title, date, note)
      logger.info "Task title is #{title}, date is #{date}, and note is #{note}"
      WebDriverUtils.wait_for_element_and_type(unsched_task_title_input_elements[index], title) unless title.nil?
      WebDriverUtils.wait_for_element_and_type(unsched_task_date_input_elements[index], date) unless date.nil?
      WebDriverUtils.wait_for_element_and_type(unsched_task_notes_input_elements[index], note) unless note.nil?
    end

    def complete_unsched_task(index)
      logger.info('Completing unsched task')
      unsched_task_cbx_elements[index].when_visible(timeout=WebDriverUtils.google_task_timeout)
      task_count = unsched_task_count.to_i
      WebDriverUtils.wait_for_element_and_click unsched_task_cbx[index]
      wait_until(WebDriverUtils.google_task_timeout) { unsched_task_count.to_i == (task_count - 1) }
    end

    def delete_all_unsched_tasks
      WebDriverUtils.wait_for_page_and_click unsched_tasks_tab_element
      while unsched_task_toggle_elements.any? do
        task_count = unsched_task_count.to_i
        WebDriverUtils.wait_for_element_and_click unsched_task_toggle_elements[0]
        WebDriverUtils.wait_for_element_and_click unsched_task_delete_button_elements[0]
        wait_until(WebDriverUtils.google_task_timeout) { unsched_task_count.to_i == (task_count - 1) }
      end
    end

    # COMPLETED TASKS

    def uncomplete_task(index)
      logger.info('Un-completing task')
      WebDriverUtils.wait_for_element_and_click completed_task_cbx_elements[index]
    end

    def all_completed_task_titles
      titles = []
      completed_task_title_elements.each { |title| titles << title.text }
      titles
    end

    def delete_all_completed_tasks
      WebDriverUtils.wait_for_element_and_click completed_tasks_tab_element
      while completed_task_toggle_elements.any? do
        task_count = completed_task_count.to_i
        WebDriverUtils.wait_for_element_and_click completed_task_toggle_elements[0]
        WebDriverUtils.wait_for_element_and_click completed_task_delete_button_elements[0]
        wait_until(WebDriverUtils.google_task_timeout) { completed_task_count.to_i == (task_count - 1) }
      end
    end

    def delete_all_tasks
      logger.info('Deleting all existing tasks')
      load_page
      delete_all_unscheduled_tasks
      delete_all_today_tasks
      delete_all_future_tasks
      delete_all_overdue_tasks
      delete_all_completed_tasks
    end
  end
end
