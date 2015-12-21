'use strict';

var angular = require('angular');
var _ = require('lodash');

/**
 * Finaid Communications controller
 */
angular.module('calcentral.controllers').controller('FinaidCommunicationsController', function($q, $scope, activityFactory, finaidFactory, finaidService, tasksFactory) {
  $scope.communicationsInfo = {
    isLoading: true,
    aidYear: '',
    taskStatus: '!completed',
    taskStatusText: 'Show Completed'
  };

  $scope.toggleCompletedTasks = function() {
    var status = $scope.communicationsInfo.taskStatus;
    var text = $scope.communicationsInfo.taskStatusText;
    status = (status === 'completed' ? '!completed' : 'completed');
    $scope.communicationsInfo.taskStatus = status;
    text = 'Show ' + (status === 'completed' ? 'Uncompleted' : 'Completed');
    $scope.communicationsInfo.taskStatusText = text;
  };

  var getMyFinaidActivity = function(options) {
    $scope.activityInfo = {
      isLoading: true
    };
    return activityFactory.getFinaidActivity(options).then(function(data) {
      angular.extend($scope, data);
      $scope.activityInfo.isLoading = false;
    });
  };

  var getMyFinaidTasks = function(options) {
    return tasksFactory.getFinaidTasks(options).then(function(data) {
      angular.extend($scope, data);
    });
  };

  var getFinaidYearInfo = function(options) {
    return finaidFactory.getFinaidYearInfo({
      finaidYearId: options.finaidYearId
    }).success(function(data) {
      $scope.communicationsInfo.unsolicitedResourcesUrl = _.get(data, 'feed.communications.unsolicitedResourcesUrl');
    });
  };

  var loadCommunications = function() {
    $scope.communicationsInfo.aidYear = finaidService.options.finaidYear;
    var finaidYearId = finaidService.options.finaidYear.id;
    $q.all([
        getMyFinaidActivity({
          finaidYearId: finaidYearId
        }),
        getMyFinaidTasks({
          finaidYearId: finaidYearId
        }),
        getFinaidYearInfo({
          finaidYearId: finaidYearId
        })
      ])
      .then(function() {
        $scope.communicationsInfo.isLoading = false;
      }
    );
  };

  $scope.$on('calcentral.custom.api.finaid.finaidYear', loadCommunications);
});
