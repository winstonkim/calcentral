class ServiceAlertsController < ApplicationController

  def get_feed
    render json: ServiceAlerts::Merged.new.get_feed_as_json
  end

end
