(function(angular) {
  'use strict';

  /**
   * Dining Common controller
   */
   angular.module('calcentral.controllers').controller('diningCommonController', function(diningCommonFactory, $scope) {
    $scope.filterOptions = {
      stores: [
      {id : 1, location : 'On-Campus' },      
      {id : 2, location : 'Off-Campus' },
      ]
    };

    $scope.filterItem = {
      store: $scope.filterOptions.stores[0]
    }

    $scope.customFilter = function (data) {
      if (data.rating === $scope.filterItem.store.location) {
        return true;
      } else if ($scope.filterItem.store.location === 6) {
        return true;
      } else {
        return false;
      }
    };  

    diningCommonFactory.getDiningCommons().success(
      function(data) {
        angular.extend($scope, data);
      }
      );

  });
 })(window.angular);
