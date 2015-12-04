module CalCentralPages

  class MyCampusPage

    include PageObject
    include CalCentralPages
    include ClassLogger

    wait_for_expected_title('Campus - Academic Departments | CalCentral', WebDriverUtils.page_load_timeout)

    h3(:academic_heading, :xpath => '//h3[text()="Academic"]')
    h3(:administrative_heading, :xpath => '//h3[text()="Administrative"]')

    def load_page
      logger.info('Loading the My Campus page')
      navigate_to "#{WebDriverUtils.base_url}/campus"
    end

  end
end
