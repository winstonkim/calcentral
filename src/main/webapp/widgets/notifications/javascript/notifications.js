var calcentral = calcentral || {};
calcentral.Widgets = calcentral.Widgets || {};
/**
 * @class notifications
 *
 * @description
 * The 'notifications' widget displays an aggregated set of notifications from central campus,
 * classroom administators, and other sources, divided into groups ("Alerts", "Today", "Recent").
 *
 * @version 0.0.1
 * @param {String} tuid Unique id of the widget
 */
calcentral.Widgets.notifications = function(tuid) {


	///////////////
	// Variables //
	///////////////

	'use strict';

	var $notificationsContainer = $('#' + tuid);
	var $notificationsList = $('.cc-notifications-list', $notificationsContainer);
	var $notificationsListTemplate = $('#cc-widget-notifications-template', $notificationsContainer);
	var $alertsLoopTemplate = $('#cc-widget-notifications-alerts-loop-template', $notificationsContainer);
	var $notificationsLoopTodayTemplate = $('#cc-widget-notifications-loop-today-template', $notificationsContainer);
	var $notificationsLoopRecentTemplate = $('#cc-widget-notifications-loop-recent-template', $notificationsContainer);

	/**
	 * Given data for notification/assignments, adds additional fields to each element,
	 * and outputs new data structure organized into time sections (alerts/today/recent).
	 * @param {object} data JSON from Canvas and bSpace
	 */
	var renderNotifications = function(data) {
		var currentTime = new Date();

		// Init an empty object into which we'll rewrite the data structure, separating notifications into alerts, today, recent.
		// We do some in-place modification of the data object first, then write the completed objects out to arrays within newData.
		var newData = {
			'alerts': [],
			'today': [],
			'recent': []
		};

		// Generate friendly dates, push objects into alerts/today/recent buckets
		// "notifications" is the name of the array in the object.
		$.each(data.notifications, function(index, value){

			// **** TODO: POC ONLY **** monkey-patch dates so we always have items for today and recent.
			// Ignore the stored timestamps and dynamically generate new ones at a variety of ranges.
			var theDateEpoch = currentTime.getTime() / 1000;

			if (index < 4) {
				data.notifications[index].notificationDate = theDateEpoch; // Today
			} else if (index < 6) {
				data.notifications[index].notificationDate = theDateEpoch + 86400; // Tomorrow
			} else {
				data.notifications[index].notificationDate = theDateEpoch + 1672800; // Far future
			}
			// END POC TEMPORARY

			var notificationDate = new Date(value.notificationDate * 1000);
			data.notifications[index].friendlyDate = notificationDate.getMonth() + '/' + notificationDate.getDate();

			// Set alert/today/recent properties.
			if (data.notifications[index].type === 'alert') { // Alert

				if (currentTime.toDateString() === notificationDate.toDateString()) {
					data.notifications[index].friendlyDate = 'today';
				}

				newData.alerts.push(data.notifications[index]);

			} else if (currentTime.toDateString() === notificationDate.toDateString()) {
				data.notifications[index].friendlyDate = '';
				newData.today.push(data.notifications[index]);

			} else { // Recent
				newData.recent.push(data.notifications[index]);
			}
		});

		// Sort each sub-array by notificationDate. In future, we may want to presort data
		// rather than post (but if we have a lot of past notifications it'll be inefficient)
		var sortDate = function(a, b) {
			return parseFloat(a.notificationDate - b.notificationDate);
		};

		newData.alerts = newData.alerts.sort(sortDate);
		newData.today = newData.today.sort(sortDate);
		newData.recent = newData.recent.sort(sortDate);

		var partials = {
			'alertsLoop': $alertsLoopTemplate.html(),
			'notificationsTodayLoop': $notificationsLoopTodayTemplate.html(),
			'notificationsRecentLoop': $notificationsLoopRecentTemplate.html()
		};

		calcentral.Api.Util.renderTemplate({
			'container': $notificationsList,
			'data': newData,
			'partials': partials,
			'template': $notificationsListTemplate
		});
	};

	var loadNotifications = function() {
		return $.ajax({
			'url': '/dummy/notifications.json'
		});
	};

	$.when(loadNotifications()).done(renderNotifications);

};
