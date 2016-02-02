'use strict';

var _ = require('lodash');
var angular = require('angular');

/**
 * Controller gets student linked to delegate user account
 */
angular.module('calcentral.controllers').controller('DelegateStudentsController', function(apiService, adminFactory, delegateFactory, $scope) {
  $scope.delegateStudents = {
    isLoading: true
  };

  var linkablePrivileges = [
    'financial',
    'viewEnrollments',
    'viewGrades'
  ];

  /**
   * TEMPORARY LOGIC ~ TO BE REPLACED BY BACK-END PROPERTY SETTING.
   * setFakeStudentPhotoAPI() adds `profilePhoto` property as a fictious API for
   * retrieving a delegate student's profile photo.
   */
  var setFakeStudentPhotoAPI = function(student) {
    student.fakeProfilePhotoAPI = '/api/student/photo/' + student.uid;
  };

  /**
   * setStudentViewability() adds `viewable` property on student, if and only if student
   * has granted at least one delegate viewing privilege.
   */
  var setStudentViewability = function(student) {
    var viewable = _.some(linkablePrivileges, function(key) {
      return student.privileges[key];
    });
    student.viewable = viewable;
  };

  var getStudents = function() {
    return delegateFactory.getStudents().then(function(data) {
      angular.extend($scope, _.get(data, 'data.feed'));
      _.each($scope.students, setFakeStudentPhotoAPI);
      _.each($scope.students, setStudentViewability);
      $scope.delegateStudents.isLoading = false;
    });
  };

  $scope.delegateStudents.actAs = function(uid) {
    return adminFactory.delegateActAs({
      uid: uid
    }).success(apiService.util.redirectToHome);
  };

  getStudents();
});
