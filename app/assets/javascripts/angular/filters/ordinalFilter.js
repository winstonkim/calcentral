(function(angular) {
  'use strict';

  angular.module('calcentral.filters').filter('ordinal', function() {
    return function(input) {
      // Only process numeric values.
      if (isNaN(input) || input === null) {
        return input;
      }

      var s = ['th', 'st', 'nd', 'rd'];
      var v = input%100;
      return input + (s[(v - 20)%10]||s[v]||s[0]);
    };
  });

}(window.angular));
