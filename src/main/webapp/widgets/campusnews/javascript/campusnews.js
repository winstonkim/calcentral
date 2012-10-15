/*global $ */

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

	///////////////
	// Selectors //
	///////////////

	var $campusNewsContainer = $('#' + tuid);
	var $campusNewsTemplate = $('#cc-widget-campusnews-template', $campusNewsContainer);

	/**
	 * Given RSS data pre-processed by parseCampusNews, renders to template
	 * @param {object} pre-processed data
	 */
	var renderCampusNews = function(data) {
		calcentral.Api.Util.renderTemplate({
			'container': $campusNewsContainer,
			'data': data.query.results,
			'template': $campusNewsTemplate
		});
	};

	/**
	 * Use YQL to fetch RSS from newscenter.berkeley.edu/category/news/feed/
	 */
	var getNewsCenterFeed = function() {
		var getFeed = $.Deferred();
		$.ajax({
			'dataType': 'jsonp',
			'url': '//query.yahooapis.com/v1/public/yql?q=select%20pubDate%2C%20title%2C%20link%20from%20rss%20where%20url%3D%22http%3A%2F%2Fnewscenter.berkeley.edu%2Fcategory%2Fnews%2Ffeed%2F%22%20limit%204&format=json',
			'success': getFeed.resolve,
			'error': function() {
				getFeed.resolve({});
			}
		});
		return getFeed;
	};

	$.when(getNewsCenterFeed()).done(renderCampusNews);
};
