'use strict';

var angular = require('angular');
var _ = require('lodash');

/**
 * Demographic veteran status controller
 */
angular.module('calcentral.controllers').controller('DemographicVeteranController', function(profileFactory, $scope, $q) {
  var parsePerson = function(data) {
    var person = data.data.feed.student;
    angular.extend($scope, {
      veteranStatus: {
        content: _.get(person, 'usaCountry.militaryStatus')
      }
    });
  };

  var getPerson = profileFactory.getPerson().then(parsePerson);

  var loadInformation = function() {
    $q.all(getPerson).then(function() {
      $scope.isLoading = false;
    });
  };

  loadInformation();
});
