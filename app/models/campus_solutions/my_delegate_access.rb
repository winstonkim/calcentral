module CampusSolutions
  class MyDelegateAccess < UserSpecificModel

    include DelegatedAccessFeatureFlagged

    def get_feed
      return {} unless is_feature_enabled
      CampusSolutions::DelegateStudents.new(user_id: @uid).get
    end

    def update(params = {})
      CampusSolutions::DelegateAccessCreate.new(user_id: @uid, params: params).get
    end

  end
end
