module CampusSolutions
  module HoldsFeatureFlagged
    def is_feature_enabled
      Settings.features.cs_holds
    end
  end
end
