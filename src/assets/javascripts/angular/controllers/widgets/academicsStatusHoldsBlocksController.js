'use strict';

var angular = require('angular');

/**
 * Academics status, holds & blocks controller
 */
angular.module('calcentral.controllers').controller('AcademicsStatusHoldsBlocksController', function($scope) {
  $scope.statusHoldsBlocks = {};

  $scope.$watchGroup(['studentInfo.regStatus.code', 'studentInfo.californiaResidency', 'api.user.profile.features.csHolds'], function(newValues) {
    var enabledSections = [];

    if (newValues[0] !== null || newValues[1]) {
      enabledSections.push('Status');
    }
    if (newValues[2]) {
      enabledSections.push('Holds');
    }
    enabledSections.push('Blocks');

    $scope.statusHoldsBlocks.enabledSections = enabledSections;
  });
});
