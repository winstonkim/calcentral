module GoogleApps
  class CreateTaskList < Tasks

    def initialize(options = {})
      super options
      @json_filename='google_create_task_list.json'
    end

    def mock_request
      super.merge(method: :post,
                  uri_matching: 'https://www.googleapis.com/tasks/v1/users/@me/lists')
    end

    def create_task_list(body)
      request(
        api: 'tasks',
        api_version: 'v1',
        resource: 'tasklists',
        method: 'insert',
        body: stringify_body(body),
        headers: {'Content-Type' => 'application/json'}
      ).first
    end

  end
end
