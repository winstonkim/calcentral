(function(angular) {
  'use strict';

  /**
   * Withdrawal Estimator Factory
   */
  angular.module('calcentral.factories').factory('withdrawalEstimatorFactory', function($http) {
    var url = '/dummy/json/withdrawal_estimate.json';

    var getLoanEstimate = function(options) {
      return $http.get(url);
    };

    return {
      getWithdrawalEstimate: getWithdrawalEstimate
    };
  });
}(window.angular));
