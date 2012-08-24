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
		$bspacefavouritesList.html(calcentral.Api.Util.renderTemplate('cc-widget-bspacefavourites-list-template', data));
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