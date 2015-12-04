describe 'My Academics course captures card', :testui => true do

  if ENV["UI_TEST"]

    include ClassLogger

    begin

      driver = WebDriverUtils.launch_browser

      test_users = UserUtils.load_test_users
      testable_users = []
      test_users.each do |user|
        unless user['courseCapture'].nil?
          uid = user['uid'].to_s
          logger.info("UID is #{uid}")
          course = user['courseCapture']['course']
          class_page = user['courseCapture']['classPagePath']
          lecture_count = user['courseCapture']['lectures']
          video_you_tube_id = user['courseCapture']['video']
          video_itunes = user['courseCapture']['itunesVideo']
          audio_url = user['courseCapture']['audio']
          audio_download = user['courseCapture']['audioDownload']
          audio_itunes = user['courseCapture']['itunesAudio']

          begin
            splash_page = CalCentralPages::SplashPage.new(driver)
            splash_page.load_page
            splash_page.basic_auth uid
            my_academics = CalCentralPages::MyAcademicsClassPage.new(driver)
            my_academics.load_class_page class_page
            my_academics.course_capture_heading_element.when_visible(WebDriverUtils.academics_timeout)
            testable_users.push(uid)

            if video_you_tube_id.nil? && !audio_url.nil?
              my_academics.audio_source_element.when_present(timeout=WebDriverUtils.academics_timeout)
              has_right_default_tab = my_academics.audio_element.visible?
              it "shows the audio tab by default for UID #{uid}" do
                expect(has_right_default_tab).to be true
              end
              has_video_tab = my_academics.video_tab?
              it "shows no video tab for UID #{uid}" do
                expect(has_video_tab).to be false
              end
            elsif video_you_tube_id.nil? && audio_url.nil?
              my_academics.no_course_capture_msg_element.when_present(timeout=WebDriverUtils.page_load_timeout)
              has_no_course_capture_message = my_academics.no_course_capture_msg_element.visible?
              it "shows a 'no recordings' message for UID #{uid}" do
                expect(has_no_course_capture_message).to be true
              end
            elsif audio_url.nil? && !video_you_tube_id.nil?
              my_academics.video_thumbnail_element.when_present(timeout=WebDriverUtils.academics_timeout)
              has_right_default_tab = my_academics.video_thumbnail_element.visible?
              it "shows the video tab by default for UID #{uid}" do
                expect(has_right_default_tab).to be true
              end
              has_audio_tab = my_academics.audio_tab?
              it "shows no audio tab for UID #{uid}" do
                expect(has_audio_tab).to be false
              end
            else
              my_academics.video_thumbnail_element.when_present(timeout=WebDriverUtils.academics_timeout)
              has_right_default_tab = my_academics.video_thumbnail_element.visible?
              it "shows the video tab by default for UID #{uid}" do
                expect(has_right_default_tab).to be true
              end
            end

            unless video_you_tube_id.nil?
              my_academics.video_thumbnail_element.when_present(timeout=WebDriverUtils.page_event_timeout)
              all_visible_video_lectures = my_academics.video_select_element.options.length
              thumbnail_present = my_academics.video_thumbnail_element.attribute('src').include? video_you_tube_id
              it "shows all the available lecture videos for UID #{uid}" do
                expect(all_visible_video_lectures).to eql(lecture_count)
              end
              it "shows the right video thumbnail for UID #{uid}" do
                expect(thumbnail_present).to be true
              end
              if my_academics.has_html5_player? driver
                auto_play = my_academics.you_tube_video_auto_plays? driver
                it "plays the video automatically when clicked for UID #{uid}" do
                  expect(auto_play).to be true
                end
              else
                logger.info 'No HTML5 player present'
              end
              unless video_itunes.nil?
                itunes_video_link_present = WebDriverUtils.verify_external_link(driver, my_academics.itunes_video_link_element, "#{course} - Free Podcast by UC Berkeley on iTunes")
                it "shows an iTunes video URL for UID #{uid}" do
                  expect(itunes_video_link_present).to be true
                end
              end
            end

            unless audio_url.nil?
              unless video_you_tube_id.nil?
                my_academics.audio_tab_element.when_present(timeout=WebDriverUtils.page_event_timeout)
                my_academics.audio_tab
              end
              my_academics.audio_source_element.when_present(timeout=WebDriverUtils.page_event_timeout)
              all_visible_audio_lectures = my_academics.audio_select_element.options.length
              audio_player_present = my_academics.audio_source_element.attribute('src').include? audio_url
              it "shows all the available lecture audio recordings for UID #{uid}" do
                expect(all_visible_audio_lectures).to eql(lecture_count)
              end
              it "shows the right audio player content for UID #{uid}" do
                expect(audio_player_present).to be true
              end
              unless audio_download.nil?
                audio_download_link_present = my_academics.audio_download_link_element.attribute('href').eql? audio_download
                it "shows an audio download link for UID #{uid}" do
                  expect(audio_download_link_present).to be true
                end
              end
              unless audio_itunes.nil?
                itunes_audio_link_present = WebDriverUtils.verify_external_link(driver, my_academics.itunes_audio_link_element, "#{course} - Free Podcast by UC Berkeley on iTunes")
                it "shows an iTunes audio URL for UID #{uid}" do
                  expect(itunes_audio_link_present).to be true
                end
              end
            end

            unless video_you_tube_id.nil? && audio_url.nil?
              has_report_problem_link = WebDriverUtils.verify_external_link(driver, my_academics.report_problem_link_element, 'Request Support or Give Feedback | Educational Technology Services')
              it "offers a 'Report a Problem' link for UID #{uid}" do
                expect(has_report_problem_link).to be true
              end
            end

          rescue => e
            logger.error e.message + "\n" + e.backtrace.join("\n ")
          end
        end
      end
      it 'has a course capture UI for at least one of the test users' do
        expect(testable_users.length).to be > 0
      end
    rescue => e
      logger.error e.message + "\n" + e.backtrace.join("\n ")
    ensure
      WebDriverUtils.quit_browser(driver)
    end
  end
end
