class MyWaitlistController < ApplicationController

  before_filter :api_authenticate

  def get_feed
    render :json => MyAcademics::Waitlist.from_session(session).get_feed_as_json
  end

end
