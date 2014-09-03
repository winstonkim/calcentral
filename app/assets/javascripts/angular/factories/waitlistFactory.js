(function(angular) {

  'use strict';

  /**
   * Wait list factory
   * @param {Object} $http The $http service from Angular
   */
  angular.module('calcentral.factories').factory('waitlistFactory', function($http) {

    var getWaitlist = function() {
      return $http.get('/dummy/json/waitlist.json');
      // return $http.get('/api/my/waitlist');
    };

    return {
      getWaitlist: getWaitlist
    };

  });

}(window.angular));
