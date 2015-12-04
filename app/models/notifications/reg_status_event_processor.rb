module Notifications
  class RegStatusEventProcessor < AbstractEventProcessor

    def accept?(event)
      return false unless super event
      event["topic"] == "Bearfacts:RegStatus"
    end

    def process_internal(event, timestamp)
      response = []
      # The payload might not have packaged a single UID as an array, or the UID might even be nil.
      uids = Array(event['payload']['uid'])
      uids.each do |uid|
        next if uid == 0
        singular_event = {
          topic: event['topic'],
          timestamp: timestamp,
          uid: uid,
        }
        entry = process_individual_uids(uid, timestamp, singular_event)
        response << entry if entry.present?
      end
      response
    end

    private

    def process_individual_uids(uid, timestamp, event)
      sis_current_term = Berkeley::Terms.fetch.sis_current_term
      reg_status = CampusOracle::Queries.get_reg_status uid, sis_current_term.year, sis_current_term.code

      if reg_status == nil
        Rails.logger.info "#{self.class.name} Registration status for #{uid} could not be determined, skipping event."
      end

      return unless reg_status != nil

      if reg_status["reg_status_cd"].upcase == "Z"
        # code Z, student deceased, remove from our system
        User::Api.delete "#{uid}"
        Rails.logger.info "#{self.class.name} Got a code Z indicating deceased student; removing #{uid} from system"
        return
      end

      return if is_dupe?(uid, event, timestamp, "RegStatusTranslator")

      entry = nil
      use_pooled_connection {
        entry = Notifications::Notification.new(
          {
            :uid => uid,
            :data => {
              :event => event,
              :timestamp => timestamp,
              :reg_status => reg_status
            },
            :translator => "RegStatusTranslator",
            :occurred_at => timestamp
          })
      }
      entry
    end

  end
end
