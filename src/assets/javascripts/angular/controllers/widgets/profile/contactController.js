'use strict';

var angular = require('angular');

/**
 * Contact controller
 */
angular.module('calcentral.controllers').controller('ContactController', function(contactFactory, $scope, $q) {
  $scope.newForm = {
    open: false
  };
  $scope.addButton = {
    hide: false
  };
  var loadContactInformation = function() {
    $q.all([
      contactFactory.getAddress(),
      contactFactory.getStates(),
      contactFactory.getCountries(),
      contactFactory.getContact()
    ]).then(function(data) {
      for (var i = 0; i < data.length; i++) {
        if (data[i].data && data[i].data.feed) {
          angular.extend($scope, data[i].data.feed);
        }
      }
      $scope.isLoading = false;
    });
  };

  loadContactInformation();
});
