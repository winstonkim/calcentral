(function(angular) {
  'use strict';

  /**
   * Loan Payment Estimate Factory
   */
  angular.module('calcentral.factories').factory('loanEstimateFactory', function($http) {
    var url = '/dummy/json/loan_estimate.json';

    var getLoanEstimate = function(options) {
      return $http.get(url);
    };

    return {
      getLoanEstimate: getLoanEstimate
    };
  });
}(window.angular));
