(function(angular) {
  'use strict';

  /**
   * Withdrawal estimator controller
   */
  angular.module('calcentral.controllers').controller('WithdrawalEstimatorController', function(withdrawalEstimatorFactory, $scope) {
    withdrawalEstimatorFactoryFactory.getWithdrawalEstimate().success(
      function(data) {
        angular.extend($scope, data);
      }
    );

    // $scope.addEditTask = {
    //   'title': $scope.task.title,
    //   'dueDate': $scope.task.dueDate || '',
    //   'notes': $scope.task.notes
    // };
  });
})(window.angular);
