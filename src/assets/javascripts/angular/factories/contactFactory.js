'use strict';

var angular = require('angular');

/**
 * Contact Factory
 */
angular.module('calcentral.factories').factory('contactFactory', function(apiService) {
  var urlAddress = '/api/campus_solutions/address';
  var urlCountries = '/api/campus_solutions/country';
  var urlStates = '/api/campus_solutions/state';
  var urlContact = '/dummy/json/contact.json';

  var getAddress = function(options) {
    return apiService.http.request(options, urlAddress);
  };

  var getCountries = function(options) {
    return apiService.http.request(options, urlCountries);
  };

  var getStates = function(options) {
    return apiService.http.request(options, urlStates);
  };

  var getContact = function(options) {
    return apiService.http.request(options, urlContact);
  };

  return {
    getAddress: getAddress,
    getCountries: getCountries,
    getStates: getStates,
    getContact: getContact
  };
});
