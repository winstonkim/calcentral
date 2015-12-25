module GoogleApps
  class EventsGet < Events

    def get_event(event_id)
      request(
        api: 'calendar',
        api_version: 'v3',
        params: {'calendarId' => 'primary', 'eventId' => event_id},
        resource: 'events',
        method: 'get',
        headers: {'Content-Type' => 'application/json'}
      ).first
    end
  end
end
