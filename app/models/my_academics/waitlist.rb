module MyAcademics
  class Waitlist < UserSpecificModel

    include Cache::LiveUpdatesEnabled

    def get_feed_internal
      courses = []
      feed = {
        waitlist: {
          courses: courses
        }
      }
      semester_feed = Semesters.new(@uid).merge({})
      semester_feed.each { |semester|
        if semester[:timeBucket] == 'current'
          semester[:classes].each { |this_class|
            if this_class[:sections][0][:wait_position] > 0
              courses << {
                courseName: "#{this_class[:dept]} #{this_class[:courseCatalog]}",
                courseTitle: this_class[:title],
                schedule: this_class[:sections][0][:schedules][0][:schedule],
                location: this_class[:sections][0][:schedules][0][:buildingDescr],
                positionOnWaitlist: this_class[:sections][0][:wait_position],
                totalOnWaitlist: this_class[:sections][0][:wait_total],
                waitlistAvailable: this_class[:sections][0][:available_seats],
                waitlistEnrolled: this_class[:sections][0][:enrollment_total],
                extraInfo: ''
              }
            end
          }
        end
      }
      feed
    end
  end
end
