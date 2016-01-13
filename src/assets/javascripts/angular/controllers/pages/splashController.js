'use strict';

var angular = require('angular');

/**
 * Splash controller
 */
angular.module('calcentral.controllers').controller('SplashController', function(apiService, serviceAlertsFactory, $filter, $scope) {
  apiService.util.setTitle('Home');

  serviceAlertsFactory.getAlerts().success(function(data) {
    if (data.alert && data.alert.title) {
      $scope.splashNote = data.alert;
    } else {
      $scope.splashNote = data.releaseNote;
    }
  });
});
