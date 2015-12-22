module ServiceAlerts
  class Alert < ActiveRecord::Base
    include DatedFeed
    include HtmlSanitizer

    self.table_name = 'service_alerts'

    attr_accessible :title, :snippet, :body, :publication_date, :display

    validates :title, presence: true
    validates :body, presence: true
    validates :publication_date, presence: true

    after_initialize :set_default_publication_date

    def self.get_latest
      self.where(display: true).order(created_at: :desc).first
    end

    def set_default_publication_date
      self.publication_date ||= Time.zone.today.in_time_zone.to_datetime
    end

    def preview
      body.try :html_safe
    end

    def to_feed
      {
        title: title,
        link: '/alert',
        timestamp: format_date(publication_date.to_datetime, '%b %d'),
        snippet: sanitize_html(snippet || body),
        body: body
      }
    end

  end
end
