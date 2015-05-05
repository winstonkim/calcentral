(function(angular) {
  'use strict';

  /**
   * Dining Commons Factory
   */
  angular.module('calcentral.factories').factory('diningCommonFactory', function($http) {
    var url = '/dummy/json/dining_commons.json';

    var getDiningCommons = function(options) {
      return $http.get(url);
    };

    return {
      getDiningCommons: getDiningCommons
    };
  });
}(window.angular));
