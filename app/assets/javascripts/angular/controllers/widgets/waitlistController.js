(function(angular) {
  'use strict';

  /**
   * Waitlist controller
   */
  angular.module('calcentral.controllers').controller('WaitlistController', function(waitlistFactory, $scope) {

    waitlistFactory.getWaitlist().then(function(xhr) {
      angular.extend($scope, xhr.data);
    });

  });

})(window.angular);
