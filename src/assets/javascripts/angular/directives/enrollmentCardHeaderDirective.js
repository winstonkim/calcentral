'use strict';

var angular = require('angular');

/**
 * Directive for the enrollment card header
 */
angular.module('calcentral.directives').directive('ccEnrollmentCardHeaderDirective', function() {
  return {
    templateUrl: 'widgets/enrollment/enrollment_header.html',
    scope: {
      count: '=',
      date: '@',
      dateImportant: '@',
      title: '='
    }
  };
});
