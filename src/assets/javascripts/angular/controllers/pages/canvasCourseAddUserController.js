'use strict';

var angular = require('angular');

/**
 * Canvas Add User to Course LTI app controller
 */
angular.module('calcentral.controllers').controller('CanvasCourseAddUserController', function(apiService, canvasCourseAddUserFactory, canvasSharedFactory, $routeParams, $scope) {
  apiService.util.setTitle('Find a Person to Add');
  $scope.accessibilityAnnounce = apiService.util.accessibilityAnnounce;

  // initialize maintenance notice settings
  $scope.courseActionVerb = 'user is added';
  $scope.maintenanceCollapsed = true;

  var resetSearchState = function() {
    $scope.selectedUser = null;
    $scope.showUsersArea = false;
    $scope.userSearchResultsCount = 0;
    $scope.userSearchResults = [];
    $scope.noSearchTextAlert = false;
    $scope.noSearchResultsNotice = false;
    $scope.noUserSelectedAlert = false;
  };

  var resetImportState = function() {
    $scope.userAdded = false;
    $scope.showAlerts = false;
    $scope.additionSuccessMessage = false;
    $scope.additionFailureMessage = false;
  };

  $scope.resetForm = function() {
    $scope.searchTextType = 'text';
    $scope.searchText = '';
    $scope.searchTypeNotice = '';
    $scope.showAlerts = false;
    resetSearchState();
    resetImportState();
  };

  var setSearchTypeNotice = function() {
    if ($scope.searchType === 'ldap_user_id') {
      $scope.searchTypeNotice = 'CalNet UIDs must be an exact match.';
    } else {
      $scope.searchTypeNotice = '';
    }
  };

  // Initialize upon load
  $scope.canvasCourseId = $routeParams.canvasCourseId || 'embedded';
  $scope.resetForm();
  $scope.searchType = 'name';

  var invalidSearchForm = function() {
    if ($scope.searchText === '') {
      $scope.showAlerts = true;
      $scope.noSearchTextAlert = true;
      $scope.isLoading = false;
      return true;
    }
    return false;
  };

  var invalidAddUserForm = function() {
    if ($scope.selectedUser === null) {
      $scope.noUserSelectedAlert = true;
      $scope.showAlerts = true;
      return true;
    }
    $scope.noUserSelectedAlert = false;
    return false;
  };

  $scope.updateSearchTextType = function() {
    $scope.searchTextType = (['ldap_user_id'].indexOf($scope.searchType) === -1) ? 'text' : 'number';
  };

  $scope.searchUsers = function() {
    resetSearchState();
    resetImportState();

    if (invalidSearchForm()) {
      return false;
    }
    $scope.accessibilityAnnounce('Loading user search results');
    $scope.showUsersArea = true;
    $scope.isLoading = true;

    canvasCourseAddUserFactory.searchUsers($scope.canvasCourseId, $scope.searchText, $scope.searchType).success(function(data) {
      $scope.userSearchResults = data.users;
      if (data.users.length > 0) {
        $scope.userSearchResultsCount = Math.floor(data.users[0].resultCount);
        $scope.selectedUser = data.users[0];
      } else {
        setSearchTypeNotice();
        $scope.userSearchResultsCount = 0;
        $scope.noSearchResultsNotice = true;
      }
      $scope.isLoading = false;
      $scope.showAlerts = true;
      $scope.searchResultsFocus = true;
    }).error(function(data) {
      $scope.showError = true;
      if (data.error) {
        $scope.errorStatus = data.error;
      } else {
        $scope.errorStatus = 'User search failed.';
      }
      $scope.isLoading = false;
      $scope.showAlerts = true;
    });
  };

  $scope.addUser = function() {
    if (invalidAddUserForm()) {
      return false;
    }
    apiService.util.iframeScrollToTop();
    $scope.showUsersArea = false;
    $scope.showSearchForm = false;
    $scope.accessibilityAnnounce('Adding user');
    $scope.isLoading = true;
    $scope.showAlerts = true;
    var submittedUser = $scope.selectedUser;
    var submittedSection = $scope.selectedSection;
    var submittedRole = $scope.selectedRole;

    canvasCourseAddUserFactory.addUser($scope.canvasCourseId, submittedUser.ldapUid, submittedSection.id, submittedRole).success(function(data) {
      $scope.userAdded = data.userAdded;
      $scope.userAdded.fullName = submittedUser.firstName + ' ' + submittedUser.lastName;
      $scope.userAdded.role = submittedRole;
      $scope.userAdded.sectionName = submittedSection.name;
      $scope.additionSuccessMessage = true;
      $scope.showSearchForm = true;
      $scope.isLoading = false;
      resetSearchState();
    }).error(function(data) {
      if (data.error) {
        $scope.errorStatus = data.error;
      } else {
        $scope.errorStatus = 'Request to add user failed';
      }
      $scope.showSearchForm = true;
      $scope.additionFailureMessage = true;
      $scope.isLoading = false;
      resetSearchState();
    });
  };

  $scope.noUserSelected = function() {
    return !$scope.selectedUser;
  };

  var checkAuthorization = function() {
    canvasSharedFactory.courseUserRoles($scope.canvasCourseId).success(function(data) {
      $scope.courseUserRoles = data.roles;
      $scope.courseUserRoleTypes = data.roleTypes;
      $scope.grantingRoles = data.grantingRoles;
      $scope.selectedRole = $scope.grantingRoles[0];

      $scope.userAuthorized = userIsAuthorized($scope.courseUserRoleTypes) || ($scope.courseUserRoles.indexOf('globalAdmin') > -1);
      if ($scope.userAuthorized) {
        getCourseSections();
        $scope.showSearchForm = true;
      } else {
        $scope.showError = true;
        $scope.errorStatus = 'You must be a teacher in this bCourses course to import users.';
      }
    }).error(function(data) {
      $scope.userAuthorized = false;
      $scope.showError = true;
      if (data.error) {
        $scope.errorStatus = data.error;
      } else {
        $scope.errorStatus = 'Authorization Check Failed';
      }
    });
  };

  var getCourseSections = function() {
    canvasCourseAddUserFactory.courseSections($scope.canvasCourseId).success(function(data) {
      $scope.courseSections = data.courseSections;
      $scope.selectedSection = $scope.courseSections[0];
    }).error(function(data) {
      $scope.showError = true;
      if (data.error) {
        $scope.errorStatus = data.error;
      } else {
        $scope.errorStatus = 'Course sections failed to load';
      }
    });
  };

  var userIsAuthorized = function(courseUserRoleTypes) {
    var authorizedTypes = ['TeacherEnrollment', 'TaEnrollment'];
    return authorizedTypes.some(function(authorizedType) {
      return courseUserRoleTypes.indexOf(authorizedType) > -1;
    });
  };

  checkAuthorization();
});
