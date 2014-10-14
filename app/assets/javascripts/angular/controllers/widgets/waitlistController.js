(function(angular) {
  'use strict';

  /**
   * Waitlist controller
   */
  angular.module('calcentral.controllers').controller('WaitlistController', function(waitlistFactory, $interval, $http, $scope) {

    var currentWaitlistInfo = {};

    var checkNofitications = function(data) {

      if (currentWaitlistInfo &&
        currentWaitlistInfo.courses &&
        data &&
        data.courses &&
        data.courses.length === 0 &&
        currentWaitlistInfo.courses.length !== data.courses.length) {

        var message = 'Congrats Oski, you are now enrolled for ' + currentWaitlistInfo.courses[0].courseName + ' - ' + currentWaitlistInfo.courses[0].courseTitle;
        $http.get('https://twiltestberkeley.herokuapp.com/message/' + message);
      }

      currentWaitlistInfo = data;
    };

    var getWaitlist = function() {
      waitlistFactory.getWaitlist().then(function(xhr) {
        angular.extend($scope, xhr.data);

        if (xhr.data && xhr.data.waitlist) {
          checkNofitications(xhr.data.waitlist);
        }
      });
    };

    $interval(getWaitlist, 2000);
    getWaitlist();
  });

})(window.angular);
