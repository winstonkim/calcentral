var calcentral = calcentral || {};
calcentral.Widgets = calcentral.Widgets || {};
calcentral.Widgets.mygroups = function(tuid) {

	/*global $, _, console*/


	/////////////////////////////
	// Configuration variables //
	/////////////////////////////

	'use strict';

	var $rootel = $('#' + tuid);
	var $mygroupsList = $('.cc-widget-mygroups-list', $rootel);

	// Method to filter the bspaceFavorites feed based on categories. If the value is left empty or null, will assume "All sites"
	var categoryFilter = 'Projects';


	///////////////
	// Rendering //
	///////////////

	/**
	 * Renders the groups data.
	 * @param  {Object} data jsonObject containing a groups array of groups json objects.
	 */
	var renderGroupsList = function(data) {
		calcentral.Api.Util.renderTemplate({
			'container': $mygroupsList,
			'data': data,
			'template': $('#cc-widget-mygroups-list-template', $rootel)
		});
	};


	////////////
	// bSpace //
	////////////

	/**
	 * Applies a category filter on the feed of sites from /api/bspacefavorites.
	 * @param {Object} data JSON response from /api/bspacefavorites
	 * @param {String} filter category name of category to filter on.
	 * @return {Object} JSON object with single element "sites", which contains an array of project sites.
	 */
	var filterOnCategory = function(data, filter) {
		var $sitesDeferred = $.Deferred();
		//Assume that someone wants "All Sites" if filter is falsy
		if (!filter) {
			filter = 'All sites';
		}
		var sites = [];
		if ($.isArray(data)) {
			sites = $.map(data, function(value) {
				if (filter === value.category) {
					return value.sites;
				}
			});
		}
		$sitesDeferred.resolve({
			'sites': sites
		});

		return $sitesDeferred.promise();
	};

	/**
	 * Returns a JSON Object of all the bSpace sites, in categories.
	 * @return {Array} Array of JSONObject categories of bSpace sites.
	 */
	var loadFavouritesList = function() {
		var $ajaxWrapper = $.Deferred();
		$.ajax({
			'cache': false,
			'url': '/api/bspacefavorites',
			'success': function(data) {
				//only care about the categories.
				return $ajaxWrapper.resolve(data.body.categories);
			},
			'error': function(jqXHR, textStatus, errorThrown) {
				console.log('mygroups -> loadFavouritesList: ' + errorThrown);
				return $ajaxWrapper.resolve();
			}
		});
		return $ajaxWrapper.promise();
	};


	////////////
	// Canvas //
	////////////

	/**
	 * Get the current user's canvas groups. If success, do some filtering on the results
	 * to only return the parts necesssary for rendering.
	 * @return {Object} Pair of 1) canvasRoot host, and 2) Array of JSON objects to pass off to the template renderer.
	 */
	var loadCanvasGroups = function() {
		var $ajaxWrapper = $.Deferred();
		$.ajax({
			'url': '/api/canvas/users/self/groups',
			'success': function(data) {
				//do some translation on the results. Expecting an array of groups JSON object.
				var result = $.map(data, function(value) {
					return {
						'id': value.id,
						'name': value.name
					};
				});
				$ajaxWrapper.resolve({
					'host': calcentral.Data.Env.canvasRoot,
					'groups': result
				});
			},
			'error': function(jqXHR, textStatus, errorThrown) {
				console.log('mygroups -> loadCanvasGroups: ' + errorThrown);
				return $ajaxWrapper.resolve();
			}
		});

		return $ajaxWrapper.promise();
	};


	//////////////////
	// Merging data //
	//////////////////

	/**
	 * Merge groups data between canvas and bspace.
	 * @param  {Object} data bspace data in json format. Should only contain project sites.
	 * @param  {Object} dataCanvas JSON object containing canvas groups and host.
	 * @return {Array} merged JSONArray of group objects for rendering.
	 */
	var mergeGroups = function(data, dataCanvas) {
		// Harmonize the bSpace and canvas data for display.
		var displayData = [];
		if (data && data.sites) {
			displayData = displayData.concat($.map(data.sites, function(value) {
				return {
					'name': value.title,
					'site_type': 'bspace',
					'url': value.url
				};
			}));
		}
		if (dataCanvas && dataCanvas.groups && dataCanvas.host) {
			displayData = displayData.concat($.map(dataCanvas.groups, function(value) {
				return {
					'name': value.name,
					'site_type': 'canvas',
					'url': dataCanvas.host + '/groups/' + value.id
				};
			}));
		}
		displayData = _.sortBy(displayData, function(value) {
			return value.name;
		});
		return displayData;
	};


	////////////////////
	// Initialisation //
	////////////////////

	/**
	 * Initialise the groups widget, after functions from other widgets are resolved.
	 */
	var init = function(){
		$.when(loadFavouritesList(), loadCanvasGroups()).pipe(function(dataBSpace, dataCanvas) {
			return filterOnCategory(dataBSpace, categoryFilter).pipe(function(data) {
				return {
					'groups': mergeGroups(data, dataCanvas)
				};
			});
		}).done(renderGroupsList);
	};

	// Start the request
	init();
};
