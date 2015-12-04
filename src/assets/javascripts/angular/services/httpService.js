'use strict';

var angular = require('angular');

angular.module('calcentral.services').factory('httpService', function($cacheFactory, $http) {
  /**
   * Clear the cache for a specific URL
   * @param {Object} options list of options that are being passed through
   * @param {String} url URL where the cache needs to be cleared
   */
  var clearCache = function(options, url) {
    if (options && options.refreshCache) {
      $cacheFactory.get('$http').remove(url);
    }
  };

  /**
   * Request an endpoint
   * @param {Object} options list of options that are being passed through
   * @param {String} url URL where the cache needs to be cleared
   */
  var request = function(options, url) {
    url = url ? url : options.url;
    clearCache(options, url);
    return $http.get(url, {
      cache: true,
      params: options ? options.params : null
    });
  };

  return {
    clearCache: clearCache,
    request: request
  };
});
