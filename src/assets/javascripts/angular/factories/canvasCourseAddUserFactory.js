'use strict';

var angular = require('angular');

/**
 * Canvas Course Add User Factory - Interface for 'Find a Person to Add' tool API endpoints
 */
angular.module('calcentral.factories').factory('canvasCourseAddUserFactory', function($http) {
  var searchUsers = function(canvasCourseId, searchText, searchType) {
    return $http.get('/api/academics/canvas/course_add_user/' + canvasCourseId + '/search_users', {
      params: {
        searchText: searchText,
        searchType: searchType
      }
    });
  };

  var courseSections = function(canvasCourseId) {
    return $http.get('/api/academics/canvas/course_add_user/' + canvasCourseId + '/course_sections');
  };

  var addUser = function(canvasCourseId, ldapUserId, sectionId, role) {
    return $http.post('/api/academics/canvas/course_add_user/' + canvasCourseId + '/add_user', {
      ldapUserId: ldapUserId,
      sectionId: sectionId,
      role: role
    });
  };

  return {
    searchUsers: searchUsers,
    courseSections: courseSections,
    addUser: addUser
  };
});
