'use strict';

var angular = require('angular');

/**
 * Withdrawal estimator controller
 */
angular.module('calcentral.controllers').controller('WithdrawalEstimatorController', function(withdrawalEstimatorFactory, $scope) {
  $scope.withdrawalEstimator = {
    'selectedDate': $scope.selectedDate || ''
  };

  $scope.showEstimate = function(date) {
    $scope.displayDate = $scope.withdrawalEstimator.selectedDate;
    withdrawalEstimatorFactory.getWithdrawalEstimate({
      date: date
    }).success(
      function(data) {
        angular.extend($scope, data);
      }
    );
  };
});
