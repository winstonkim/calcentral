module CampusSolutions
  module DelegatedAccessFeatureFlagged
    def is_feature_enabled
      Settings.features.cs_delegated_access
    end
    alias_method(:is_cs_delegated_access_feature_enabled, :is_feature_enabled)
  end
end
