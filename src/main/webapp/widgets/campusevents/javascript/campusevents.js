/*global $ */

var calcentral = calcentral || {};
calcentral.Widgets = calcentral.Widgets || {};
/**
 * @class campusevents
 *
 * @description
 * The 'campusevents' widget (i.e. Campus News) displays news from the Berkeley Newscenter (newscenter.b.e.)
 *
 * @version 0.0.1
 * @param {String} tuid Unique id of the widget
 */
calcentral.Widgets.campusevents = function(tuid) {

	'use strict';

	// VARIABLES
	var $campuseventsContainer = $('#' + tuid);
	var $campuseventsTemplate = $('#cc-widget-campusevents-template', $campuseventsContainer);

	/**
	 * Given a YQL RSS feed, parses dates to friendly format and appends properties
	 * @param {object} data RSS feed filtered fetched by YQL
	 */
	var parseCampusEvents = function(data) {
		data = data.query.results;

		$.each(data.item, function(index, value){
			// Campus events feed does not include a date property; appends date as URL param instead
			var urldate = calcentral.Api.Util.parseURI({
				'url': value.link
			}).queryKey.date.split('-');

			var eventDate = new Date();
			eventDate.setFullYear(urldate[0], parseInt(urldate[1], 10) - 1, urldate[2]);

			// #todo Another reason we need a good JS date lib
			var weekdays = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
			var shortDay = weekdays[eventDate.getDay()];
			var theMonth = parseInt(urldate[1], 10);
			data.item[index].friendlyDate = shortDay + ' ' + theMonth + '/' + parseInt(urldate[2], 10);
		});
		return data;
	};

	/**
	 * Given RSS data pre-processed by parsecampusevents, renders to template
	 * @param {object} pre-processed data
	 */
	var renderCampusEvents = function(data) {
		calcentral.Api.Util.renderTemplate({
			'container': $campuseventsContainer,
			'data': data,
			'template': $campuseventsTemplate
		});
	};

	/**
	 * Use YQL to fetch RSS from events.berkeley.edu/index.php/rss/sn/pubaff/type/day/tab/all_events.html
	 */
	var getEventsFeed = function() {
		var getFeed = $.Deferred();
		$.ajax({
			'dataType': 'jsonp',
			'url': '//query.yahooapis.com/v1/public/yql?q=select%20title%2C%20link%20from%20rss%20where%20url%3D%22http%3A%2F%2Fevents.berkeley.edu%2Findex.php%2Frss%2Fsn%2Fpubaff%2Ftype%2Fday%2Ftab%2Fall_events.html%22%20limit%204&format=json',
			'success': getFeed.resolve,
			'error': function() {
				getFeed.resolve({});
			}
		});
		return getFeed;
	};

	$.when(getEventsFeed()).pipe(parseCampusEvents).done(renderCampusEvents);
};
