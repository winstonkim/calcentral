'use strict';

var angular = require('angular');

/**
 * Hookup Google Reminder controller
 */
angular.module('calcentral.controllers').controller('HookupGoogleReminderController', function(apiService, googleFactory, $scope) {
  $scope.dismissReminder = function() {
    googleFactory.dismissReminder().success(function() {
      apiService.analytics.sendEvent('Preferences', 'Dismiss bConnected reminder card');
      $scope.showReminderCard = false;
      // Force the user.profile to refresh since status has changed.
      $scope.api.user.fetch({
        refreshCache: true
      });
    });
  };

  $scope.showReminderCard = true;

  $scope.$watch('api.user.profile.hasGoogleAccessToken + \',\' + api.user.profile.isGoogleReminderDismissed', function(newTokenTuple) {
    var dismissReminderFields = newTokenTuple.split(',');
    if (dismissReminderFields[0] === 'true' || dismissReminderFields[1] === 'true') {
      $scope.showReminderCard = false;
    }
  });
});
