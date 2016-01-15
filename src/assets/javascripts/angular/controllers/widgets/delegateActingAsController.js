'use strict';

var angular = require('angular');

/**
 * Controller for delegate users in act-as mode.
 */
angular.module('calcentral.controllers').controller('DelegateActingAsController', function(adminFactory, apiService, $scope) {
  $scope.delegate = {};

  /**
   * Stop acting as someone else
   */
  $scope.delegate.stopActAs = function() {
    adminFactory.stopDelegateActAs().then(apiService.util.redirectToToolbox);
  };
});
