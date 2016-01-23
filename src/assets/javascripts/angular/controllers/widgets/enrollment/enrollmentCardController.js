'use strict';

var angular = require('angular');
var _ = require('lodash');

/**
 * Enrollment Card Controller
 * Main controller for the enrollment card on the My Academics page
 */
angular.module('calcentral.controllers').controller('EnrollmentCardController', function(apiService, enrollmentFactory, $scope, $q) {
  $scope.enrollment = {
    isLoading: true,
    terms: [],
    sections: [
      {
        id: 'meet_advisor',
        title: 'Meet Advisor'
      },
      {
        id: 'plan',
        title: 'Plan'
      },
      {
        id: 'explore',
        title: 'Explore'
      },
      {
        id: 'schedule',
        title: 'Schedule'
      },
      {
        id: 'decide',
        title: 'Decide',
        show: true
      },
      {
        id: 'adjust',
        title: 'Adjust'
      }
    ]
  };

  /**
   * Stop the main spinner.
   * This happens when
   * 	- the terms data has loaded
   * 	- or when there is no term data
   */
  var stopMainSpinner = function() {
    $scope.enrollment.isLoading = false;
  };

  /**
   * Set the data for a specific term
   */
  var setTermData = function(data) {
    var termIndex = _.indexOf(
      $scope.enrollment.terms,
      _.find($scope.enrollment.terms, {
        termId: data.termId
      })
    );

    data.isTermLoading = false;

    $scope.enrollment.terms.splice(termIndex, 1, data);
  };

  /**
   * Parse a certain enrollment term
   */
  var parseEnrollmentTerm = function(data) {
    var termData = _.get(data, 'data.enrollmentTerm');

    setTermData(termData);
  };

  /**
   * Create a promise for a specific enrollment term
   */
  var createEnrollmentPromise = function(enrollmentTerm) {
    return enrollmentFactory.getEnrollmentTerm({
      termId: enrollmentTerm.termId
    }).then(parseEnrollmentTerm);
  };

  /**
   * Create promises for all the enrollment terms
   */
  var createEnrollmentPromises = function(enrollmentTerms) {
    var promiseArray = _.map(enrollmentTerms, createEnrollmentPromise);
    return $q.all(promiseArray);
  };

  /**
   * Set the scope for the enrollment cards and set each one to loading = true
   */
  var setEnrollmentTerms = function(enrollmentTerms) {
    $scope.enrollment.terms = _.map(enrollmentTerms, function(enrollmentTerm) {
      enrollmentTerm.isTermLoading = true;
      return enrollmentTerm;
    });
  };

  /**
   * Parse all the terms and create an array of promises for each
   */
  var parseEnrollmentTerms = function(data) {
    if (!_.get(data, 'data.enrollmentTerms.length')) {
      return;
    }

    var enrollmentTerms = _.get(data, 'data.enrollmentTerms');
    setEnrollmentTerms(enrollmentTerms);
    stopMainSpinner();

    return createEnrollmentPromises(enrollmentTerms);
  };

  /**
   * Load the enrollment data and fire off subsequent events
   */
  var loadEnrollmentData = function() {
    enrollmentFactory.getEnrollmentTerms()
      .then(parseEnrollmentTerms)
      .then(stopMainSpinner);
  };

  /**
   * We should check the roles of the current person since we should only load
   * the enrollment card for students
   */
  var checkRoles = function(data) {
    if (_.get(data, 'student')) {
      loadEnrollmentData();
    }
  };

  $scope.$watch('api.user.profile.roles', checkRoles);
});
