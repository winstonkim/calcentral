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
		if (!data.classes) {
			data.body = '{}';
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
				// Harmonize the bSpace and canvas data for display.
				var displayData = [];
				if (data && data.sites) {
					displayData = displayData.concat($.map(data.sites, function(value) {
						return {
							'site_type': 'bspace',
							'title': value.title,
							'url': value.url
						};
					}));
				}
				if (dataCanvas && dataCanvas.courses && dataCanvas.host) {
					displayData = displayData.concat($.map(dataCanvas.courses, function(value) {
						return {
							'site_type': 'canvas',
							'title' : value.name,
							'url': dataCanvas.host + '/courses/' + value.id
						};
					}));
				}
				displayData = _.sortBy(displayData, function(value) { return value.title; });
				$combineDataDef.resolve({'classes': displayData});
				return $combineDataDef.promise();
			});
		}).done(renderClassesList);
	};

	var $bSpaceFavoritesDeferred = calcentral.WidgetStatus.bspacefavourites || $.Deferred();
	var $canvasCoursesDeferred = calcentral.WidgetStatus.canvascourses || $.Deferred();

	$.when($bSpaceFavoritesDeferred, $canvasCoursesDeferred).done(function(favWidgetName, canvasWidgetName) {
		var functionMap = {
			'filterOnCategory': calcentral.Widgets.bspacefavourites(favWidgetName).filterOnCategory,
			'loadFavouritesList': calcentral.Widgets.bspacefavourites(favWidgetName).loadFavouritesList,
			'loadCourses': calcentral.Widgets.canvascourses(canvasWidgetName).loadCourses
		};
		// Start the request, with resolved functions.
		delayedInit(functionMap);
	});
};