/*global $ */
var calcentral = calcentral || {};
calcentral.Widgets = calcentral.Widgets || {};

/**
 * My calendar widget
 * Get's events and basic information from your bcal calendar.
 *
 * Good example is at http://www.google.com/ig/directory?type=gadgets&url=www.google.com/ig/modules/calendar3.xml
 *
 * @param {string} tuid Unique id for the widget
 */
calcentral.Widgets.mycalendar = function(tuid) {


	/////////////////////////////
	// Configuration variables //
	/////////////////////////////

	'use strict';

	var $rootel = $('#' + tuid);
	var $mycalendarContainer = $('.cc-widget-mycalendar-container', $rootel);
	var $mycalendarOverviewTemplate = $('.cc-widget-mycalendar-overview-template', $rootel);

	var currentTime = new Date();
	var startToday = new Date();
	startToday.setHours(0, 0, 0, 0);
	var endToday = new Date();
	endToday.setHours(23, 59, 59, 999);


	///////////////
	// Rendering //
	///////////////

	/**
	 * Scroll to the upcoming event
	 */
	var scrollToUpcoming = function() {
		var $mycalendarDateList = $('.cc-widget-mycalendar-datelist', $rootel);
		var $upcoming = $('.cc-widget-mycalendar-isupcoming', $mycalendarDateList);
		$mycalendarDateList.scrollTop($mycalendarDateList.scrollTop() + $upcoming.position().top);
	};

	/**
	 * Render the calendar overview
	 * @param {Object} data The data we want to pass through
	 */
	var renderCalendarOverview = function(data) {
		calcentral.Api.Util.renderTemplate({
			'container': $mycalendarContainer,
			'data': data,
			'template': $mycalendarOverviewTemplate
		});
		scrollToUpcoming();
	};


	/////////////
	// Parsing //
	/////////////

	/**
	 * Sort the events, we only want to move the full day events to the bottom
	 * @param {Object} a an event item
	 * @return {Integer} We return an integer to know whether we need to move it or not.
	 */
	var sortEvents = function(a) {

		// Only full day events have a date here
		if (a.end.date) {
			return 1;
		} else {
			return -1;
		}
	};

	/**
	 * Parse the events we get returned from the server
	 * @param {Object} data The data we get back from the server
	 * @return {Object} The data that we modified
	 */
	var parseEvents = function(data) {

		// If we don't get any items, we just exit this function
		if (!data.items) {
			return data;
		}

		var hasUpcomingSet = false;

		// Sort events
		data.items.sort(sortEvents);

		for (var i = 0; i < data.items.length; i++) {
			var item = data.items[i];

			// Add the hours, minutes and am/pm to every event
			if (item.start.dateTime) {
				var itemDate = new Date(item.start.dateTime);

				if (itemDate > currentTime && !hasUpcomingSet) {
					item.isUpcoming = true;
					hasUpcomingSet = true;
				}

				var amPm = 'AM';
				var hour = itemDate.getHours();
				var minutes = itemDate.getMinutes();
				if (hour >= 12) {
					hour = (hour === 12) ? hour : hour - 12;
					amPm = 'PM';
				}

				item.start = $.extend({
					'amPm': amPm,
					'hour': hour,
					// The length should always be 2
					'minutes': ('0' + minutes).slice(-2)
				}, item.start);
			}
			if (i === data.items.length - 1 && !hasUpcomingSet) {
				item.isUpcoming = true;
				hasUpcomingSet = true;
			}
		}
		return data;
	};


	///////////////////
	// Ajax Requests //
	///////////////////

	/**
	 * Load the events from the Google calendar API
	 */
	var loadEvents = function() {
		return $.ajax({
			'data': {
				// TODO specify the fields we actually want to select 'fields'
				'orderBy': 'startTime', // Order by the start time, default is unspecified
				'singleEvents': true, // We want multiple of the same events into one (e.g. 5000 events)
				'timeMin': startToday.toISOString(),
				'timeMax': endToday.toISOString()
			},
			'url': '/api/google/calendar/v3/calendars/primary/events'
		});
	};


	////////////////////
	// Initialisation //
	////////////////////

	// Initialise the calendar widget
	var doInit = function(){
		$.when(loadEvents()).pipe(parseEvents).done(renderCalendarOverview);
	};

	// Start the request
	doInit();
};
