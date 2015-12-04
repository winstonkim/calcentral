module CalCentralPages

  class MyAcademicsClassPage < MyAcademicsPage

    include PageObject
    include CalCentralPages

    span(:class_breadcrumb, :xpath => '//h1/span[@data-ng-bind="selectedCourse.course_code"]')
    span(:section_breadcrumb, :xpath => '//h1/span[@data-ng-bind="selectedSection"]')

    # CLASS INFO
    h2(:class_info_heading, :xpath => '//h2[text()="Class Information"]')
    div(:course_title, :xpath => '//h3[text()="Class Title"]/following-sibling::div[@data-ng-bind="selectedCourse.title"]')
    div(:role, :xpath => '//h3[text()="My Role"]/following-sibling::div[@data-ng-bind="selectedCourse.role"]')
    elements(:student_section_label, :td, :xpath => '//h3[text()="My Enrollment"]/following-sibling::div[@data-ng-if="selectedCourse.sections.length && !isInstructorOrGsi"]//td[@data-ng-bind="sec.section_label"]')
    elements(:student_section_ccn, :td, :xpath => '//h3[text()="My Enrollment"]/following-sibling::div[@data-ng-if="selectedCourse.sections.length && !isInstructorOrGsi"]//td[@data-ng-bind="sec.ccn"]')
    elements(:section_units, :td, :xpath => '//h3[text()="Class Info"]/following-sibling::div[@data-ng-if="!isInstructorOrGsi"]//td[@data-ng-if="section.units"]')
    elements(:section_grade_option, :td, :xpath => '//h4[text()="Course Offering"]/following-sibling::div[@data-ng-if="!isInstructorOrGsi"]//td[@data-ng-bind="section.gradeOption"]')
    elements(:section_schedule_label, :div, :xpath => '//div[@data-ng-repeat="section in selectedCourse.sections"]/div[@data-ng-bind="section.section_label"]')
    elements(:student_section_schedule, :div, :xpath => '//h4[text()="Section Schedules"]/following-sibling::div[@data-ng-repeat="section in selectedCourse.sections"]//div[@data-ng-repeat="schedule in section.schedules"]')
    elements(:teaching_section_schedule, :div, :xpath => '//h3[text()="Section Schedules"]/following-sibling::div[@data-ng-repeat="section in selectedCourse.sections"]//div[@data-ng-repeat="schedule in section.schedules"]')
    h3(:cross_listing_heading, :xpath => '//h3[text()="Cross-listed As"]')
    elements(:cross_listing, :span, :xpath => '//span[@data-ng-bind="listing.course_code"]')

    # INSTRUCTORS
    elements(:section_instructors_heading, :h3, :xpath => '//h3[@data-ng-bind="section.section_label"]')

    # COURSE CAPTURES
    h2(:course_capture_heading, :xpath => '//h2[text()="Course Captures"]')
    link(:video_tab, :text => 'Video')
    div(:no_video_msg, :xpath => '//div[contains(.,"No video content available.")]')
    select(:video_select, :xpath => '//select[@data-ng-model="selectedVideo"]')
    button(:video_thumbnail, :xpath => '//button[@id="cc-youtube-image-placeholder"]/img')
    div(:html5_player, :xpath => '//div[@class="html5-player-chrome"]')
    div(:video_pause_button, :xpath => '//div[@class="ytp-button ytp-button-pause"]')
    link(:itunes_video_link, :xpath => '//li[@class="cc-widget-webcast-itunes-link"]/a')
    link(:audio_tab, :text => 'Audio')
    div(:no_audio_msg, :xpath => '//div[contains(.,"No audio content available.")]')
    select(:audio_select, :xpath => '//select[@data-ng-model="selectedAudio"]')
    audio(:audio, :xpath => '//audio')
    audio(:audio_source, :xpath => '//audio/source')
    link(:audio_download_link, :xpath => '//li[@data-ng-if="selectedAudio.downloadUrl"]/a')
    link(:itunes_audio_link, :xpath => '//li[@data-ng-if="iTunes.audio"]/a')
    div(:no_course_capture_msg, :xpath => '//div[contains(text(),"There are no recordings available.")]')
    link(:report_problem_link, :xpath => '//a[contains(text(),"Report a problem with this recording")]')

    def all_student_section_labels
      labels = []
      student_section_label_elements.each { |label| labels.push(label.text) }
      labels
    end

    def all_student_section_ccns
      ccns = []
      student_section_ccn_elements.each { |ccn| ccns.push(ccn.text) }
      ccns
    end

    def all_section_units
      units = []
      section_units_elements.each { |unit| units.push(unit.text) }
      units
    end

    def all_section_grade_options
      options = []
      section_grade_option_elements.each { |option| options.push(option.text) }
      options
    end

    def all_section_schedule_labels
      labels = []
      section_schedule_label_elements.each { |label| labels.push(label.text) }
      labels
    end

    def all_student_section_schedules
      schedules = []
      student_section_schedule_elements.each { |schedule| schedules.push(schedule.text) }
      schedules
    end

    def all_teaching_section_schedules
      schedules = []
      teaching_section_schedule_elements.each { |schedule| schedules.push(schedule.text) }
      schedules
    end

    def all_section_instructors(driver, section_labels)
      instructors = []
      section_labels.each do |section|
        instructor_elements = driver.find_elements(:xpath => "//h3[text()='#{section}']/following-sibling::ul//a[@data-ng-bind='instructor.name']")
        instructor_elements.each { |instructor| instructors.push((instructor.text).gsub("\n- opens in new window", '')) }
      end
      instructors
    end

    def all_course_instructors(driver, sections)
      instructors = []
      sections.each do |section|
        instructors.push(all_section_instructors(driver, section))
      end
      instructors
    end

    def all_cross_listings
      listings = []
      cross_listing_elements.each { |listing| listings.push(listing.text) }
      listings
    end

    def has_html5_player?(driver)
      video_thumbnail
      wait_until(timeout=WebDriverUtils.page_event_timeout) { driver.find_element(:xpath, '//iframe') }
      driver.switch_to.frame driver.find_element(:xpath, '//iframe')
      begin
        html5_player_element.present? ? true : false
      ensure
        driver.switch_to.default_content
      end
    end

    def you_tube_video_auto_plays?(driver)
      begin
        driver.switch_to.frame driver.find_element(:xpath, '//iframe')
        auto_play = video_pause_button_element.present?
      ensure
        driver.switch_to.default_content
      end
      auto_play
    end
  end
end
