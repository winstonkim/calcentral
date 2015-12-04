'use strict';

var angular = require('angular');

/**
 * OEC Factory - Interface for 'OEC Control Panel' API endpoints
 */
angular.module('calcentral.factories').factory('oecFactory', function($http) {
  var getOecTasks = function() {
    return $http.get('/api/oec_tasks');
  };

  var oecTaskStatus = function(oecTaskId) {
    return $http.get('/api/oec_tasks/status/' + oecTaskId);
  };

  var runOecTask = function(taskName, parameters) {
    return $http.post('/api/oec_tasks/' + taskName, parameters);
  };

  return {
    getOecTasks: getOecTasks,
    oecTaskStatus: oecTaskStatus,
    runOecTask: runOecTask
  };
});
