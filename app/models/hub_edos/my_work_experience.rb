module HubEdos
  class MyWorkExperience < UserSpecificModel

    include ClassLogger
    include Cache::LiveUpdatesEnabled
    include Cache::FreshenOnWarm
    include Cache::JsonAddedCacher
    include CampusSolutions::ProfileFeatureFlagged

    def get_feed_internal
      return {} unless is_cs_profile_feature_enabled
      HubEdos::WorkExperience.new({user_id: @uid}).get
    end

  end
end
