module GoogleApps
  class EventsUpdate < Events

    def update_event(event_id, body)
      request(
        api: 'calendar',
        api_version: 'v3',
        resource: 'events',
        params: {'calendarId' => 'primary', 'eventId' => event_id, 'sendNotifications' => false},
        method: 'update',
        body: stringify_body(body),
        headers: {'Content-Type' => 'application/json'}
      ).first
    end
  end
end
