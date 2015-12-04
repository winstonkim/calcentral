module CampusSolutions
  class Phone < PostingProxy

    include ProfileFeatureFlagged
    include CampusSolutionsIdRequired

    def initialize(options = {})
      super(Settings.campus_solutions_proxy, options)
      initialize_mocks if @fake
    end

    def self.field_mappings
      @field_mappings ||= FieldMapping.to_hash(
        [
          FieldMapping.required(:type, :PHONE_TYPE),
          FieldMapping.required(:countryCode, :COUNTRY_CODE),
          FieldMapping.required(:phone, :PHONE),
          FieldMapping.required(:extension, :EXTENSION),
          FieldMapping.required(:isPreferred, :PREF_PHONE_FLAG)
        ]
      )
    end

    def request_root_xml_node
      'PERSON_PHONE'
    end

    def xml_filename
      'phone.xml'
    end

    def url
      "#{@settings.base_url}/UC_CC_PERS_PHONE.v1/phone/post/"
    end

  end
end
