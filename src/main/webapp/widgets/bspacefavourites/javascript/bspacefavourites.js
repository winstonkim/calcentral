var calcentral = calcentral || {};
calcentral.Widgets = calcentral.Widgets || {};
calcentral.Widgets.bspacefavourites = function(tuid) {

	/////////////////////////////
	// Configuration variables //
	/////////////////////////////

	var $rootel = $('#' + tuid);
	var $bspacefavouritesList = $('.cc-widget-bspacefavourites-list', $rootel);
	// Method to filter the bspaceFavorites feed based on categories. If the value is left empty or null, will assume "All sites"
	var categoryFilter = "Fall 2012"

	////////////////////
	// Event Handlers //
	////////////////////

	///////////////
	// Rendering //
	///////////////

	var renderFavouritesList = function(data) {
		if (!data) {
			console.log('bspacefavourites widget - renderFavouritesList: ' + data.statusText);
			data.body = '{}';
		}
		calcentral.Api.Util.renderTemplate({
			'container': $bspacefavouritesList,
			'data': data,
			'template': $('#cc-widget-bspacefavourites-list-template', $rootel)
		});
	};

	///////////////////
	// Ajax Requests //
	///////////////////

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
			'error': $ajaxWrapper.reject
		});
		return $ajaxWrapper.promise();
	};

	/**
	 * Applies a category filter on the feed of sites from /api/bspacefavorites.
	 * @param  {Object} data.categories JSON response from /api/bspacefavorites
	 * @param  {String} category name of category to filger on.
	 * @return {[type]}          [description]
	 */
	var filterOnCategory = function(data, filter) {
		var $sitesDeferred = $.Deferred();
		//Assume that someone wants "All Sites" if filter is falsy
		if (!filter) {
			filter = "All sites";
		}
		var sites = $.map(data, function(value, index) {
			if (filter === value.category) {
				return value.sites;
			}
		});
		$sitesDeferred.resolve({'sites' : sites});

		return $sitesDeferred.promise();
	}


	////////////////////
	// Initialisation //
	////////////////////

	/**
	 * Initialise the bspace favourites widget
	 */
	var doInit = function(){
		$.when(loadFavouritesList()).pipe(function(data) {
			return filterOnCategory(data, categoryFilter);
		}).done(renderFavouritesList);
	};

	// Start the request
	doInit();
};