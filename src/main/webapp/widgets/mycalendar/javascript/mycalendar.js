/*global $, _, console */
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

	// Need two arrays for events - one for events that are both all-day and
	// happening today, and another for timestamped events (NOT all-day)
	var fullDayEventsToday = [], datedEvents = [];

	///////////////
	// Rendering //
	///////////////

	/**
	 * Scroll to the upcoming event
	 */
	var scrollToUpcoming = function() {
		var $mycalendarDateList = $('.cc-widget-mycalendar-datelist', $rootel);
		var $upcoming = $('.cc-widget-mycalendar-isupcoming', $mycalendarDateList);
		// If there is no upcoming highlight, we shouldn't do the scrolling
		if (!$upcoming.length) {
			return;
		}
		$mycalendarDateList.scrollTop($upcoming.position().top - $mycalendarDateList.position().top);
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
	 * Sort the events based on the date
	 * @param {Object} item an event item
	 * @return {Boolean} We return an integer to know whether we need to move it or not.
	 */
	var sortEventsDated = function(item) {
		return new Date(item.end.date);
	};

	/**
	 * Sort events alphabetically, we need to do this for full day events
	 * @param {Object} a The first event item
	 * @param {Object} b The second event item
	 */
	var sortEventsAlphabetically = function(a, b) {
		var summaryA = a.summary.toLowerCase();
		var summaryB = b.summary.toLowerCase();
		if (summaryA < summaryB) {
			return -1;
		}
		if (summaryA > summaryB) {
			return 1;
		}
		return 0;
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

		// Prevent conversion of UTC+TZ date
		var convertUTCDate = function(datestring) {
			var tempStartDate = new Date(datestring);
			var offset = tempStartDate.getTimezoneOffset();
			var hours = tempStartDate.getHours();
			return tempStartDate.setHours(hours + offset / 24);
		};

		var storeEventTypes = function() {
			$.each(data.items, function(index, value){
				// All-day events have start.date; other events have start.dateTime
				if (value.start.date) {
					// We know it's an all-day event, but is it happening today?
					var tempStartDate = new Date(convertUTCDate(value.start.date));
					if (startToday.getDate() === tempStartDate.getDate()) {
						fullDayEventsToday.push(value);
					}
				} else {
					// Item has a start.dateTime field, so it's a timestamped event
					datedEvents.push(value);
				}
			});
		};
		storeEventTypes();

		// Sort events
		_.sortBy(datedEvents, sortEventsDated);
		fullDayEventsToday.sort(sortEventsAlphabetically);
		data.items = datedEvents.concat(fullDayEventsToday);

		for (var i = 0; i < data.items.length; i++) {
			var item = data.items[i];

			// Add the hours, minutes and am/pm to every event
			if (item.start.dateTime) {
				var itemStartTime = new Date(item.start.dateTime);
				var itemEndTime = new Date(item.end.dateTime);

				if (((currentTime < itemEndTime && currentTime > itemStartTime)) || ((currentTime < itemStartTime) && !hasUpcomingSet)) {
					item.isUpcoming = true;
					hasUpcomingSet = true;
				}

				var amPm = 'AM';
				var hour = itemStartTime.getHours();
				var minutes = itemStartTime.getMinutes();
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
