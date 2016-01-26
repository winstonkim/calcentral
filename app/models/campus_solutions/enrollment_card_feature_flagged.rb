module CampusSolutions
  module EnrollmentCardFeatureFlagged
    def is_feature_enabled
      Settings.features.cs_enrollment_card
    end
  end
end
