'use strict';

var angular = require('angular');

/**
 * Directive for the enrollment links
 */
angular.module('calcentral.directives').directive('ccEnrollmentLinkDirective', function() {
  return {
    templateUrl: 'widgets/enrollment/enrollment_link.html',
    scope: {
      link: '=',
      text: '@'
    }
  };
});
