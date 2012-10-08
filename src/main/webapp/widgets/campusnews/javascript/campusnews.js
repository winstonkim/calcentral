/*global $, console */

var calcentral = calcentral || {};
calcentral.Widgets = calcentral.Widgets || {};
/**
 * @class campusnews
 *
 * @description
 * The 'campusnews' widget (i.e. Campus News) displays news from the Berkeley Newscenter (newscenter.b.e.)
 *
 * @version 0.0.1
 * @param {String} tuid Unique id of the widget
 */
calcentral.Widgets.campusnews = function(tuid) {

	'use strict';

	// VARIABLES
	var $campusNewsContainer = $('#' + tuid);
	var $campusNewsTemplate = $('#cc-widget-campusnews-template', $campusNewsContainer);

	/**
	 * Given a YQL RSS feed, parses dates to friendly format and appends properties
	 * @param {object} data RSS feed filtered fetched by YQL
	 */
	var parseCampusNews = function(data) {
		data = data.query.results;

		$.each(data.item, function(index, value){
			var pubDate = new Date(value.pubDate);
			// #todo Another reason we need a good JS date lib
			var weekdays = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
			var shortDay = weekdays[pubDate.getDay()];
			var theMonth = pubDate.getMonth() + 1;
			data.item[index].friendlyDate = shortDay + ' ' + theMonth + '/' + pubDate.getDate();
		});
		return data;
	};

	/**
	 * Given RSS data pre-processed by parseCampusNews, renders to template
	 * @param {object} pre-processed data
	 */
	var renderCampusNews = function(data) {
		calcentral.Api.Util.renderTemplate({
			'container': $campusNewsContainer,
			'data': data,
			'template': $campusNewsTemplate
		});
	};

	/**
	 * Use YQL to fetch RSS from newscenter.berkeley.edu/category/news/feed/
	 */
	var getNewsCenterFeed = function() {
		var getFeed = $.Deferred();
		$.ajax({
			'dataType': 'json',
			'url': '//query.yahooapis.com/v1/public/yql?q=select%20pubDate%2C%20title%2C%20link%20from%20rss%20where%20url%3D%22http%3A%2F%2Fnewscenter.berkeley.edu%2Fcategory%2Fnews%2Ffeed%2F%22%20limit%204&format=json&callback=',
			'success': getFeed.resolve,
			'error': function() {
				getFeed.resolve({});
			}
		});
		return getFeed;
	};

	$.when(getNewsCenterFeed()).pipe(parseCampusNews).done(renderCampusNews);
};
