'use strict';

var _ = require('lodash');
var angular = require('angular');

/**
 * Profile Menu Serives - provide all the information for the profile menu
 */
angular.module('calcentral.services').factory('profileMenuService', function(apiService, $q) {
  var navigation = [
    {
      label: 'Profile',
      categories: [
        {
          id: 'basic',
          name: 'Basic Information'
        },
        {
          id: 'contact',
          name: 'Contact Information'
        },
        {
          id: 'emergency',
          name: 'Emergency Contact',
          featureFlag: 'csProfileEmergencyContacts'
        },
        {
          id: 'demographic',
          name: 'Demographic Information'
        }
      ]
    },
    {
      label: 'Privacy & Permissions',
      categories: [
        {
          id: 'delegate',
          name: 'Delegate Access',
          featureFlag: 'csDelegatedAccess',
          roles: {
            student: true
          }
        },
        {
          id: 'information-disclosure',
          name: 'Information Disclosure'
        },
        {
          id: 'title4',
          name: 'Title IV Release',
          featureFlag: 'csFinAid',
          roles: {
            student: true
          }
        }
      ]
    },
    {
      label: 'Credentials',
      categories: [
        {
          id: 'languages',
          name: 'Languages'
        },
        {
          id: 'work-experience',
          name: 'Work Experience',
          featureFlag: 'csProfileWorkExperience'
        }
      ]
    },
    {
      label: 'Alerts & Notifications',
      categories: [
        {
          id: 'bconnected',
          name: 'bConnected'
        }
      ]
    }
  ];

  /**
   * Wrap callbacks into a promise
   */
  var defer = function(navigation, callback) {
    var deferred = $q.defer();

    navigation = callback(navigation);

    deferred.resolve(navigation);
    return deferred.promise;
  };

  /**
   * Filter the categories inside of the navigation element
   */
  var filterCategories = function(navigation, callback) {
    return _.map(navigation, function(item) {
      item.categories = callback(item.categories);
      return item;
    });
  };

  var filterRolesInCategory = function(categories) {
    var userRoles = apiService.user.profile.roles;
    return _.filter(categories, function(category) {
      if (!category.roles) {
        return true;
      } else {
        return _.some(category.roles, function(value, key) {
          return userRoles[key] === value;
        });
      }
    });
  };

  var filterRolesInNavigation = function(navigation) {
    return filterCategories(navigation, filterRolesInCategory);
  };

  /**
   * Filter based on the roles
   * If there is no 'roles' definied, we assume everyone should see it
   */
  var filterRoles = function(navigation) {
    return defer(navigation, filterRolesInNavigation);
  };

  var filterFeatureFlagsInCategory = function(categories) {
    var featureFlags = apiService.user.profile.features;
    return _.filter(categories, function(category) {
      if (!category.featureFlag) {
        return true;
      } else {
        return !!featureFlags[category.featureFlag];
      }
    });
  };

  var filterFeatureFlagsInNavigation = function(navigation) {
    return filterCategories(navigation, filterFeatureFlagsInCategory);
  };

  var filterFeatureFlags = function(navigation) {
    return defer(navigation, filterFeatureFlagsInNavigation);
  };

  var filterEmptyInNavigation = function(navigation) {
    return _.filter(navigation, function(item) {
      return !!_.get(item, 'categories.length');
    });
  };

  /**
   * If we remove all the links in a certain section, we need to make sure we
   * don't show the heading
   */
  var filterEmpty = function(navigation) {
    return defer(navigation, filterEmptyInNavigation);
  };

  var initialNavigation = function() {
    return $q(function(resolve) {
      resolve(navigation);
    });
  };

  var getNavigation = function() {
    return apiService.user.fetch()
      .then(initialNavigation)
      .then(filterRoles)
      .then(filterFeatureFlags)
      .then(filterEmpty);
  };

  return {
    getNavigation: getNavigation
  };
});
