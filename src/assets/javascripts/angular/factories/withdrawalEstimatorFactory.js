'use strict';

var angular = require('angular');

/**
 * Withdrawal Estimator Factory
 */
angular.module('calcentral.factories').factory('withdrawalEstimatorFactory', function($http) {
  var url = '/dummy/json/withdrawal_estimate.json';

  var getWithdrawalEstimate = function(options) {
    return $http.get(url + '?date=' + options.date);
  };

  return {
    getWithdrawalEstimate: getWithdrawalEstimate
  };
});
