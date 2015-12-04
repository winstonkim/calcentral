'use strict';

var angular = require('angular');

/**
 * Profile Menu Serives - provide all the information for the profile menu
 */
angular.module('calcentral.services').factory('profileMenuService', function() {
  var navigation = [
    {
      'label': 'Profile',
      'categories': [
        {
          'id': 'basic',
          'name': 'Basic Information'
        },
        {
          'id': 'contact',
          'name': 'Contact Information'
        },
        {
          'id': 'emergency',
          'name': 'Emergency Contact'
        },
        {
          'id': 'demographic',
          'name': 'Demographic Information'
        }
      ]
    },
    /* TODO - On hold till we have designs for these & after the profile sprint
    {
      'label': 'Privacy & Permissions',
      'categories': [
        {
          'id': 'recordaccess',
          'name': 'Record Access'
        },
        {
          'id': 'ferpa',
          'name': 'FERPA Restrictions'
        },
        {
          'id': 'title4',
          'name': 'Title IV Release'
        }
      ]
    }*/
    {
      'label': 'Credentials',
      'categories': [
        {
          'id': 'languages',
          'name': 'Languages'
        }
        /* TODO - Work experience is currently not part of the EDO - see SISRP-7184 for progress
        {
          'id': 'work-experience',
          'name': 'Work Experience'
        }*/
      ]
    }
    /* TODO - After the profile sprint
    {
      'label': 'Awards',
      'categories': [
        {
          'id': 'honors-awards',
          'name': 'Academic Honors & Awards'
        }
        TODO - On hold till SISRP-7185 is resolved
        {
          'id': 'finaid-awards',
          'name': 'Financial Aid Awards'
        }
      ]
    },
    */
    /* TODO - After profile sprint - see SISRP-8835
    {
      'label': 'Alerts & Notifications',
      'categories': [
        {
          'id': 'bconnected',
          'name': 'bConnected'
        }
      ]
    }
    */
  ];

  return {
    navigation: navigation
  };
});
