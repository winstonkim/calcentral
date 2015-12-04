'use strict';

var angular = require('angular');

/**
 * Set the location configuration for CalCentral
 */
angular.module('calcentral.config').config(function($locationProvider) {
  // We set it to html5 mode so we don't have hash bang URLs
  $locationProvider.html5Mode(true).hashPrefix('!');
});
