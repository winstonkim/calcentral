'use strict';

var angular = require('angular');

/**
 * Budgeting Tool Factory
 */
angular.module('calcentral.factories').factory('budgetingToolFactory', function(apiService) {
  var urlBudgetingTool = '/dummy/json/budgeting_tool.json';

  var getBudgetingTool = function(options) {
    return apiService.http.request(options, urlBudgetingTool);
  };

  return {
    getBudgetingTool: getBudgetingTool
  };
});
