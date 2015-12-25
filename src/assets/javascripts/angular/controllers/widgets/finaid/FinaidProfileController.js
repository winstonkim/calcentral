'use strict';

var _ = require('lodash');
var angular = require('angular');

/**
 * Financial Aid & Scholarships Profile controller
 */
angular.module('calcentral.controllers').controller('FinaidProfileController', function($scope, finaidFactory, finaidService) {
  $scope.finaidProfileInfo = {
    isLoading: true
  };
  $scope.finaidProfile = {};

  var loadProfile = function() {
    return finaidFactory.getFinaidYearInfo({
      finaidYearId: finaidService.options.finaidYear.id
    }).success(function(data) {
      angular.extend($scope.finaidProfile, _.get(data, 'feed.status'));
      $scope.finaidProfileInfo.errored = data.errored;
      $scope.finaidProfileInfo.isLoading = false;
    });
  };

  $scope.$on('calcentral.custom.api.finaid.finaidYear', loadProfile);
});
