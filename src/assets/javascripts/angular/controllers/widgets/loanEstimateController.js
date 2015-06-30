(function(angular) {
  'use strict';

  /**
   * Loan payment estimate controller
   */
  angular.module('calcentral.controllers').controller('LoanEstimateController', function(loanEstimateFactory, $scope) {
    loanEstimateFactory.getLoanEstimate().success(
      function(data) {
        angular.extend($scope, data);
      }
    );

    $scope.options = {
      // Manipulating how Y axis labels are displayed
      scaleLabel: '<%= \'$\' + value/1000 + \'k\' %>',

      // Boolean - Whether to show vertical lines (except Y axis)
      scaleShowVerticalLines: false,

      // Boolean - Whether to show a dot for each point
      pointDot: false,

      // Boolean - Determines whether to draw tooltips on the canvas or not
      showTooltips: false
    };
  });
})(window.angular);
