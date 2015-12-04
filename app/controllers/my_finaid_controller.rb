class MyFinaidController < ApplicationController

  before_filter :api_authenticate

  def get_feed
    render json: Finaid::Merged.from_session(session).get_feed_as_json
  end

end
