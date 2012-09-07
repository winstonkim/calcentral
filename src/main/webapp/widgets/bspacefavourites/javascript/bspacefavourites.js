var calcentral = calcentral || {};
calcentral.Widgets = calcentral.Widgets || {};
calcentral.Widgets.bspacefavourites = function(tuid) {

	/////////////////////////////
	// Configuration variables //
	/////////////////////////////

	var $rootel = $('#' + tuid);
	var $bspacefavouritesList = $('.cc-widget-bspacefavourites-list', $rootel);

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

	var loadFavouritesList = function() {
		return $.ajax({
			'cache': false,
			'url': '/api/bspacefavorites'
		});
	};

	var removeDuplicateSites = function(data, statusText, jqXHR) {
		var $sitesDeferred = $.Deferred();
		var filteredSites = {
			'urls' : [],
			'uniqueSites' : []
		};
		$.each(data.body.categories, function(index, value) {
			filteredSites.urls = filteredSites.urls.concat($.map(value.sites, function(site, index) {
				// doesn't seem like inArray works with js objects.
				if ($.inArray(site.url, filteredSites.urls) === -1) {
					filteredSites.uniqueSites.push(site);
					return site.url;
				}
			}));
		});

		$sitesDeferred.resolve({'sites' : filteredSites.uniqueSites});
		return $sitesDeferred.promise();
	}

	////////////////////
	// Initialisation //
	////////////////////

	/**
	 * Initialise the bspace favourites widget
	 */
	var doInit = function(){
		$.when(loadFavouritesList()).pipe(removeDuplicateSites).done(renderFavouritesList);
	};

	// Start the request
	doInit();
};