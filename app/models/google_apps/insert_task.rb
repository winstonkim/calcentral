module GoogleApps
  class InsertTask < Tasks

    def initialize(options = {})
      super options
      @json_filename='google_insert_task.json'
    end

    def mock_request
      super.merge(method: :post,
                  uri_matching: 'https://www.googleapis.com/tasks/v1/lists/MDkwMzQyMTI0OTE3NTY4OTU0MzY6NzAzMjk1MTk3OjA/tasks')
    end

    def insert_task(task_list_id, body)
      request(
        api: 'tasks',
        api_version: 'v1',
        resource: 'tasks',
        method: 'insert',
        params: {tasklist: task_list_id},
        body: stringify_body(body),
        headers: {'Content-Type' => 'application/json'}
      ).first
    end

  end
end
