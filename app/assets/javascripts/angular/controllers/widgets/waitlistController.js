(function(angular) {
  'use strict';

  /**
   * Waitlist controller
   */
  angular.module('calcentral.controllers').controller('WaitlistController', function(waitlistFactory, $interval, $scope) {

    var getWaitlist = function() {
      waitlistFactory.getWaitlist().then(function(xhr) {
        angular.extend($scope, xhr.data);
      });
    };

    $interval(getWaitlist, 2000);
    getWaitlist();
  });

})(window.angular);
