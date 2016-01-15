'use strict';

var _ = require('lodash');
var angular = require('angular');

/**
 * Controller gets student linked to delegate user account
 */
angular.module('calcentral.controllers').controller('DelegateStudentsController', function(apiService, adminFactory, delegateFactory, $scope) {
  $scope.delegateStudents = {
    isLoading: true
  };

  var getStudents = function() {
    return delegateFactory.getStudents().then(function(data) {
      angular.extend($scope, _.get(data, 'feed'));
      $scope.delegateStudents.isLoading = false;
    });
  };

  $scope.delegateStudents.actAs = function(uid) {
    return adminFactory.delegateActAs({
      uid: uid
    }).success(apiService.util.redirectToHome);
  };

  getStudents();
});
