class BaseProxy
  extend Cache::Cacheable
  include ClassLogger
  include Proxies::HttpClient

  attr_accessor :fake, :settings

  def initialize(settings, options = {})
    @settings = settings
    @fake = (options[:fake] != nil) ? options[:fake] : @settings.fake
    @uid = options[:user_id]
  end

end
