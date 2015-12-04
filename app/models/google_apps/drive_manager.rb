module GoogleApps
  class DriveManager

    include ClassLogger

    def initialize(uid, options = {})
      @uid = uid
      @options = options
    end

    def get_items_in_folder(parent_id, mime_type = nil)
      options = {:parent_id => parent_id}
      options.merge!({ :mime_type => mime_type }) unless mime_type.nil?
      find_items(options)
    end

    def find_folders_by_title(title, options = {})
      options.merge!(mime_type: 'application/vnd.google-apps.folder')
      find_items_by_title(title, options)
    end

    def find_folders(parent_id = 'root')
      find_items(mime_type: 'application/vnd.google-apps.folder', parent_id: parent_id)
    end

    def find_items_by_title(title, options = {})
      find_items options.merge({ :title => title })
    end

    def download(file)
      result = get_google_api.execute(:uri => file.download_url)
      log_response result
      raise Errors::ProxyError, "Failed to download '#{file.title}' (id: #{file.id}).\nError: #{result.data['error']}" if result.error?
      result.body
    end

    def find_items(options = {})
      client = get_google_api
      drive_api = client.discovered_api('drive', 'v2')
      items = []
      query = 'trashed=false'
      parent_id = options[:parent_id]
      # Shared items will not be found under our own root folder.
      unless parent_id || options[:shared]
        parent_id = 'root'
      end
      query.concat " and '#{parent_id}' in parents" if parent_id
      query.concat " and title='#{escape options[:title]}'" if options.has_key? :title
      query.concat " and mimeType='#{options[:mime_type]}'" if options.has_key? :mime_type
      query.concat ' and sharedWithMe' if options[:shared]
      page_token = nil
      begin
        parameters = { :q => query }
        parameters[:pageToken] = page_token unless page_token.nil?
        result = client.execute(:api_method => drive_api.files.list, :parameters => parameters)
        log_response result
        case result.status
          when 200
            files = result.data
            items.concat files.items
            page_token = files.next_page_token
          when 404
            logger.debug 'No items found, returning empty array'
            page_token = nil
          else
            raise Errors::ProxyError, "Error in find_items(#{options}): #{result.data['error']['message']}"
            page_token = nil
        end
      end while page_token.to_s != ''
      items
    end

    def create_folder(title, parent_id = 'root')
      client = get_google_api
      drive_api = client.discovered_api('drive', 'v2')
      metadata = {
        :title => title,
        :mimeType => 'application/vnd.google-apps.folder'
      }
      dir = drive_api.files.insert.request_schema.new metadata
      dir.parents = [{ :id => parent_id }] if parent_id
      result = client.execute(:api_method => drive_api.files.insert, :body_object => dir)
      log_response result
      raise Errors::ProxyError, "Error in create_folder(#{title}, ...): #{result.data['error']['message']}" if result.error?
      result.data
    end

    def upload_file(title, description, parent_id, mime_type, file_path)
      client = get_google_api
      drive_api = client.discovered_api('drive', 'v2')
      metadata = {
        :title => title,
        :description => description,
        :mimeType => mime_type
      }
      file = drive_api.files.insert.request_schema.new metadata
      # Target directory is optional
      file.parents = [{ :id => parent_id }] if parent_id
      media = Google::APIClient::UploadIO.new(file_path, mime_type)
      result = client.execute(
        :api_method => drive_api.files.insert,
        :body_object => file,
        :media => media,
        :parameters => { :uploadType => 'multipart', :alt => 'json'})
      log_response result
      raise Errors::ProxyError, "Error in upload_file(#{title}): #{result.data['error']['message']}" if result.error?
      result.data
    end

    def trash_item(item, opts={})
      client = get_google_api
      drive = client.discovered_api('drive', 'v2')
      api_method = opts[:permanently_delete] ? drive.files.delete : drive.files.trash
      result = client.execute(:api_method => api_method, :parameters => { :fileId => item.id })
      log_response result
      raise Errors::ProxyError, "Error in trash_item(#{id}): #{result.data['error']['message']}" if result.error?
      result.data
    end

    def empty_trash
      client = get_google_api
      drive = client.discovered_api('drive', 'v2')
      result = client.execute(:api_method => drive.files.empty_trash)
      log_response result
      raise Errors::ProxyError, "Error in empty_trash: #{result.data['error']['message']}" if result.error?
      result.data
    end

    def copy_item_to_folder(item, folder_id, copy_title=nil)
      copy_title ||= item.title
      if (copy = copy_item(item.id, copy_title))
        old_parent_id = copy.parents.first.id
        add_parent(copy.id, folder_id) && remove_parent(copy.id, old_parent_id)
      end
      copy
    end

    def copy_item(id, copy_title)
      client = get_google_api
      drive_api = client.discovered_api('drive', 'v2')
      copy_schema = drive_api.files.copy.request_schema.new({'title' => copy_title})
      result = client.execute(
        :api_method => drive_api.files.copy,
        :body_object => copy_schema,
        :parameters => { :fileId => id }
      )
      log_response result
      raise Errors::ProxyError, "Error in copy_item(#{id}): #{result.data['error']['message']}" if result.error?
      result.data
    end

    def add_parent(id, parent_id)
      client = get_google_api
      drive_api = client.discovered_api('drive', 'v2')
      new_parent = drive_api.parents.insert.request_schema.new({'id' => parent_id})
      result = client.execute(
        :api_method => drive_api.parents.insert,
        :body_object => new_parent,
        :parameters => { :fileId => id }
      )
      log_response result
      raise Errors::ProxyError, "Error in add_parent(#{id}, #{parent_id}): #{result.data['error']['message']}" if result.error?
      result.data
    end

    def remove_parent(id, parent_id)
      client = get_google_api
      drive_api = client.discovered_api('drive', 'v2')
      result = client.execute(
        :api_method => drive_api.parents.delete,
        :parameters => {
          'fileId' => id,
          'parentId' => parent_id
        }
      )
      log_response result
      raise Errors::ProxyError, "Error in remove_parent(#{id}, #{parent_id}): #{result.data['error']['message']}" if result.error?
      result.data
    end

    def folder_id(folder)
      folder ? folder.id : 'root'
    end

    def folder_title(folder)
      folder ? folder.title : 'root'
    end

    private

    def get_google_api
      if @client.nil?
        credential_store = GoogleApps::CredentialStore.new(@uid, @options)
        @client = GoogleApps::Client.client
        storage = Google::APIClient::Storage.new credential_store
        auth = storage.authorize
        credentials = credential_store.load_credentials
        if auth.nil? || (auth.expired? && auth.refresh_token.nil?)
          logger.warn "OAuth2 object #{auth.nil? ? 'is nil' : 'is expired'}"
          flow = Google::APIClient::InstalledAppFlow.new({ :client_id => credentials[:client_id],
                                                           :client_secret => credentials[:client_secret],
                                                           :scope => credentials[:scope] })
          auth = flow.authorize storage
        end
        @client.authorization = auth
        token_hash = @client.authorization.fetch_access_token!
        credential_store.write_credentials(credentials.merge token_hash)
      end
      @client
    end

    def log_response(api_response)
      request_description = if api_response.request.api_method
        "#{api_response.request.api_method.id} #{api_response.request.parameters}"
      else
        api_response.request.uri
      end
      logger.debug "Google Drive API request #{request_description} returned status #{api_response.status}"
    end

    def escape(arg)
      arg.gsub('\'', %q(\\\'))
    end

  end
end
