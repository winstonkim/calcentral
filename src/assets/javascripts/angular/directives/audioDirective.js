'use strict';

var angular = require('angular');

angular.module('calcentral.directives').directive('ccAudioDirective', function(apiService, $compile) {
  return {
    restrict: 'ACE',
    replace: true,
    link: function(scope, elem, attrs) {
      scope.$watch(attrs.ccAudioDirective, function(audioUrl) {
        // Templates for the player
        var templates = {
          audio: '<audio controls><source data-ng-src="' + audioUrl + '"></source>Your browser does not support the audio element.</audio>',
          noAudio: '<div>Your browser does not support playing mp3 files.</div>'
        };

        var init = function() {
          elem.empty();

          var template = templates.audio;
          if (apiService.util.canPlayMp3()) {
            template = templates.audio;
          } else {
            template = templates.noAudio;
          }

          var el = angular.element(template);
          var compiled = $compile(el);
          elem.append(el);
          compiled(scope);
        };

        if (audioUrl) {
          init();
        }
      });
    }
  };
});
