module CampusSolutions
  class EnrollmentTermsController < CampusSolutionsController

    def get
      render json: CampusSolutions::MyEnrollmentTerms.from_session(session).get_feed_as_json
    end

  end
end
