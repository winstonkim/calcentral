var calcentral = calcentral || {};
calcentral.Widgets = calcentral.Widgets || {};
calcentral.Widgets.myclasses = function(tuid) {

	/////////////////////////////
	// Configuration variables //
	/////////////////////////////

	var $rootel = $('#' + tuid);
	var $myclassesList = $('.cc-widget-myclasses-list', $rootel);
	// Method to filter the bspaceFavorites feed based on categories. If the value is left empty or null, will assume "All sites"
	var categoryFilter = "Fall 2012";

	////////////////////
	// Event Handlers //
	////////////////////

	///////////////
	// Rendering //
	///////////////

	var renderClassesList = function(data) {
		if (!data.bSpace) {
			data.bSpace.body = '{}';
		}
		calcentral.Api.Util.renderTemplate({
			'container': $myclassesList,
			'data': data,
			'template': $('#cc-widget-myclasses-list-template', $rootel)
		});
	};

	////////////////////
	// Initialisation //
	////////////////////

	/**
	 * Initialise the classes widget, after functions from other widgets are resolved.
	 */
	var delayedInit = function(fnMap){
		$.when(fnMap.loadFavouritesList(), fnMap.loadCourses()).pipe(function(dataBSpace, dataCanvas) {
			return fnMap.filterOnCategory(dataBSpace, categoryFilter).pipe(function(data) {
				var $combineDataDef = $.Deferred();
				$combineDataDef.resolve({'bSpace': data, 'canvas': dataCanvas});
				return $combineDataDef.promise();
			});
		}).done(renderClassesList);
	};

	var $bSpaceFavoritesDef = calcentral.WidgetStatus.bspacefavourites || $.Deferred();
	var $canvasCoursesDef = calcentral.WidgetStatus.canvascourses || $.Deferred();

	$.when($bSpaceFavoritesDef, $canvasCoursesDef).done(function(favWidgetName, canvasWidgetName) {
		var fnMap = {};
		fnMap.filterOnCategory = calcentral.Widgets.bspacefavourites(favWidgetName).filterOnCategory;
		fnMap.loadFavouritesList = calcentral.Widgets.bspacefavourites(favWidgetName).loadFavouritesList;
		fnMap.loadCourses = calcentral.Widgets.canvascourses(canvasWidgetName).loadCourses;

		// Start the request, with resolved functions.
		delayedInit(fnMap);
	});
};