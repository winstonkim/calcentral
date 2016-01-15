'use strict';

var angular = require('angular');

/**
 * 'Acting as' controller
 */
angular.module('calcentral.controllers').controller('ActingAsController', function(adminFactory, apiService, $scope) {
  $scope.admin = {};

  /**
   * Stop acting as someone else
   */
  $scope.admin.stopActAs = function() {
    adminFactory.stopActAs().then(apiService.util.redirectToToolbox);
  };
});
