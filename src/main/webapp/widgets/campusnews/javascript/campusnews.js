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

	/** VARIABLES. **/

	var $campusNewsContainer = $('#cc-widget-campusnews');
	var $campusNewsTemplate = $('#cc-widget-campusnews-template', $campusNewsContainer);

	/**
	 * Given a parsed RSS feed, renders simple list of linked stories.
	 * @param {object} data RSS feed filtered through YQL to generate JSON
	 */
	var renderCampusNews = function(data) {
		data = data.query.results;

		$.each(data.item, function(index, value){
			var pubDate = new Date(value.pubDate);

			// #todo Another reason we need a good JS date lib
			var weekday=new Array(7);
			weekday[0]="Sun";
			weekday[1]="Mon";
			weekday[2]="Tues";
			weekday[3]="Wed";
			weekday[4]="Thur";
			weekday[5]="Fri";
			weekday[6]="Sat";

			var shortDay = weekday[pubDate.getDay()];
			var theMonth = pubDate.getMonth() + 1;
			data.item[index].friendlyDate = shortDay + ' ' + theMonth + '/' + pubDate.getDate();

		});

		calcentral.Api.Util.renderTemplate({
			'container': $campusNewsContainer,
			'data': data,
			'template': $campusNewsTemplate
		});
	};

	var getNewsCenterFeed = function() {
		// Use YQL to fetch RSS from newscenter.berkeley.edu/category/news/feed/
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

	$.when(getNewsCenterFeed()).done(renderCampusNews);
};
