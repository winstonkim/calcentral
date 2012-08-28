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
		data = $.parseJSON(data);
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
			'url': '/widgets/bspacefavourites/dummy/bspacesites.json'
		});
	};

	////////////////////
	// Initialisation //
	////////////////////

	/**
	 * Initialise the bspace favourites widget
	 */
	var doInit = function(){
		$.when(loadFavouritesList()).done(renderFavouritesList);
	};

	// Start the request
	doInit();
};