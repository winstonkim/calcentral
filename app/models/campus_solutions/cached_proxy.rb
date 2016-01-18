module CampusSolutions
  class CachedProxy < Proxy

    include Cache::UserCacheExpiry

    def get
      return {} unless is_feature_enabled
      response = self.class.smart_fetch_from_cache(id: instance_key) do
        get_internal
      end
      if response.is_a? Hash
        decorate_internal_response response
      else
        {}
      end
    end

  end
end
