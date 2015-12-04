'use strict';

var angular = require('angular');

angular.module('calcentral.directives').directive('ccImageLoadedDirective', function() {
  return {
    link: function(scope, elm, attrs) {
      elm.bind('load', function() {
        scope.$apply(function() {
          scope[attrs.ccImageLoadedDirective] = false;
        });
      });
    }
  };
});
