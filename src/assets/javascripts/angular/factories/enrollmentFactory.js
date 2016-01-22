'use strict';

var angular = require('angular');

/**
 * Factory for the enrollment information
 */
angular.module('calcentral.factories').factory('enrollmentFactory', function(apiService) {
  var urlEnrollmentTerm = '/dummy/json/enrollment_term.json';
  var urlEnrollmentTerms = '/dummy/json/enrollment_terms.json';

  var getEnrollmentTerm = function(options) {
    return apiService.http.request(options, urlEnrollmentTerm + '?termId=' + options.termId);
  };
  var getEnrollmentTerms = function(options) {
    return apiService.http.request(options, urlEnrollmentTerms);
  };

  return {
    getEnrollmentTerm: getEnrollmentTerm,
    getEnrollmentTerms: getEnrollmentTerms
  };
});
