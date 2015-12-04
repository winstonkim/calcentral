module CampusSolutions
  class PostingProxy < DirectProxy

    attr_reader :params

    def initialize(settings, options = {})
      super options
      @params = options[:params]
      initialize_mocks if @fake
    end

    def self.expires_in
      1.seconds
    end

    def mock_request
      super.merge(method: :post)
    end

    def request_options
      updateable_params = filter_updateable_params params
      cs_post = construct_cs_post updateable_params
      super.merge(
        method: :post,
        body: cs_post,
        headers: {'Content-Type'=>'application/xml; charset=UTF-8'}
      )
    end

    def default_post_params
      {
        EMPLID: @campus_solutions_id
      }
    end

    # lets us restrict updated params to what's on the whitelist of field mappings.
    def filter_updateable_params(params)
      return {} unless params
      updateable = {}
      known_fields = self.class.field_mappings
      # make sure every required field is present even if blank
      known_fields.values.each do |mapping|
        if mapping[:is_required]
          updateable[mapping[:field_name]] = ''
        end
      end
      params.each do |calcentral_param_name, value|
        if known_fields[calcentral_param_name.to_sym].present?
          updateable[calcentral_param_name.to_sym] = value
        end
      end
      updateable
    end

    def construct_cs_post(filtered_params)
      cs_post = default_post_params
      filtered_params.each do |calcentral_param_name, value|
        mapping = self.class.field_mappings[calcentral_param_name]
        next if mapping.blank?
        cs_param_name = mapping[:campus_solutions_name]
        cs_post[cs_param_name] = value
      end
      # CampusSolutions will barf if it encounters whitespace or newlines
      cs_post.to_xml(root: request_root_xml_node, dasherize: false, indent: 0)
    end

    def request_root_xml_node
      ''
    end

    def response_root_xml_node
      'PostResponse'
    end

    def error_response_root_xml_node
      'UC_CM_FAULT_DOC'
    end

    def build_feed(response)
      parsed = response.parsed_response
      parsed[response_root_xml_node] || parsed[error_response_root_xml_node]
    end

  end
end
