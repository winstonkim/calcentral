(function(angular) {
  'use strict';

  angular.module('calcentral.directives').directive('ccBarDirective', function() {
    return {
      transclude: true,
      replace: true,
      template: '<div class="cc-bar-container">' +
                  '<div class="cc-bar-container-background" style="background-image: linear-gradient(to right, #487E96 0%, #487E96 {{percent}}%, #194A5A {{percent}}%, #194A5A 100%);">' +
                    '<div class="cc-bar-container-enrolled">{{enrolled}} Enrolled</div>' +
                    '<div class="cc-bar-container-available">{{available}}</div>' +
                  '</div>' +
                  '<div class="cc-bar-container-available-text">Available Seats</div>' +
                '</div>',
      link: function(scope, elem, attrs) {

        var createBar = function(enrolled, available, percent) {
          scope.enrolled = enrolled;
          scope.available = available;
          scope.percent = percent;
        };

        scope.watchBar = [attrs.ccBarEnrolled, attrs.ccBarAvailable];

        var createEnrolledPercent = function(enrolled, available) {
          var total = enrolled + available;
          var mutliplyBy = 100 / total;
          return Math.floor(mutliplyBy * enrolled);
        };

        scope.$watch('watchBar', function(watchValue) {
          if (!watchValue || !watchValue[0] || !watchValue[1]) {
            return;
          }
          var enrolled = parseInt(watchValue[0], 10);
          var available = parseInt(watchValue[1], 10);
          var enrolledPercent = createEnrolledPercent(enrolled, available);
          createBar(enrolled, available, enrolledPercent);
        });

      }
    };
  });

})(window.angular);
