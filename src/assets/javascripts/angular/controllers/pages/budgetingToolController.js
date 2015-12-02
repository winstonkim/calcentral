'use strict';

var angular = require('angular');

/**
 * Budgeting Tool controller
 */
 angular.module('calcentral.controllers').controller('BudgetingToolController', function(budgetingToolFactory, $scope) {
  $scope.getData = function() {
    budgetingToolFactory.getBudgetingTool().success(
      function(data) {
        angular.extend($scope, data);
      }
    );
  }

  $scope.addForm = {
    open_1: false,
    open_2: false,
    open_3: false,
    open_4: false,
    open_5: false,
    open_6: false
  };

  $scope.income = {
    startingIncome: 12000
  };

  $scope.getData();
  $scope.workIncome = [];
  $scope.financialAid = [];
  $scope.oneTimeIncome = [];
  $scope.monthlyExpenses = [];
  $scope.semesterExpenses = [];
  $scope.oneTimeExpenses = [];
  $scope.toggleAdd = false;

  $scope.getSum = function(){
    var sum = 0;
    sum += $scope.income.startingIncome * 1;
    for(var i=0; i<$scope.workIncome.length; i++) {
      sum += $scope.workIncome[i].wages * $scope.workIncome[i].hours * 4 * 5;
    }
    for(var i=0; i<$scope.financialAid.length; i++) {
      sum += $scope.financialAid[i].amount * 1;
    }
    for(var i=0; i<$scope.oneTimeIncome.length; i++) {
      sum += $scope.oneTimeIncome[i].amount * 1;
    }
    return sum;
  };

  $scope.getTotalExpenses = function(){
    var sum = 0;
    for(var i=0; i<$scope.monthlyExpenses.length; i++) {
      sum += $scope.monthlyExpenses[i].amount * 5;
    }
    for(var i=0; i<$scope.semesterExpenses.length; i++) {
      sum += $scope.semesterExpenses[i].amount * 1;
    }
    for(var i=0; i<$scope.oneTimeExpenses.length; i++) {
      sum += $scope.oneTimeExpenses[i].amount * 1;
    }
    return sum;
  };

  $scope.add = function(){
    console.log($scope.newHours)
    var newIncome = {hours:$scope.newHours, type:$scope.newType, wages:$scope.newWages};
    $scope.workIncome.unshift(newIncome);
  }

  $scope.delete = function(index, array){
    if (array=='workIncome'){
      $scope.workIncome.splice(index,1);
    }
    else if (array=='financialAid') {
      $scope.financialAid.splice(index,1);
    }
    else if (array=='monthlyExpenses') {
      $scope.monthlyExpenses.splice(index,1);
    }
    else if (array=='semesterExpenses') {
      $scope.semesterExpenses.splice(index,1);
    }
    else if (array=='oneTimeExpenses') {
      $scope.oneTimeExpenses.splice(index,1);
    }
    else{
      $scope.oneTimeIncome.splice(index,1);
    }
  }

  $scope.jobsLoop = function(jobsArray) {
    var amount = 0;
    for (var i = 0; i < jobsArray.length; i++){
      amount += jobsArray[i].wages * jobsArray[i].hours * 4
    }
    return amount;
  }
  // $scope.graphData = [[$scope.income.startingIncome + $scope.jobsLoop(), 32000, 20000, 0, 5]];
  $scope.labels = ['Aug\'15', 'Sep\'15', 'Oct\'15', 'Nov\'15', 'Dec\'15'];
});
