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
	var dummy = false;

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
		var sites = $.map(data, function(value) {
			if (filter === value.category) {
				return value.sites;
			}
		});
		$sitesDeferred.resolve({'sites' : sites});

		return $sitesDeferred.promise();
	};

	/**
	 * Will always load the list of dummy courses hardcoded for the widget.
	 * @return {Object} Ajax object for fetching the dummy couress.
	 */
	var loadDummyCourses = function() {
		var $ajaxWrapper = $.Deferred();
		$.ajax({
			'cache': false,
			'url': '/widgets/myclasses/dummy/canvascourses.json',
			'success': function(data) {
				$ajaxWrapper.resolve({'host': 'http://localhost:3000/', 'courses' : data});
			},
			'error': $ajaxWrapper.reject
		});
		return $ajaxWrapper.promise();
	};

	/**
	 * Get the current user's canvas courses. If success, do some filtering on the results
	 * to only return the parts necesssary for rendering.
	 * @return {Object} Pair of 1) canvasRoot host, and 2) Array of JSON objects to pass off to the template renderer.
	*/
	var getCanvasCourses = function() {
		var $ajaxWrapper = $.Deferred();
		$.ajax({
			'url': '/api/canvas/courses',
			'success': function(data) {
					//do some translation on the results. Expecting an array of course JSON object.
					var result = $.map(data.courses, function(value) {
						return {
							'id': value.id,
							'name': value.course_code + ": " + value.name
						};
					});
					$ajaxWrapper.resolve({'host':data.canvasRoot, 'courses': result});
				},
				'error': $ajaxWrapper.reject
			});

		return $ajaxWrapper.promise();
	};

	/**
	 * Fetch users's course data from canvas.
	 * @return {Object} Deferred promise object for a Deferrred chain, with a (data) param.
	 */
	var loadCourses = function() {
		var $loadCoursesDeferred = $.Deferred();
		if (dummy) {
			loadDummyCourses().done($loadCoursesDeferred.resolve);
		} else {
			$.when(getCanvasCourses()).done($loadCoursesDeferred.resolve).fail(function() {
				loadDummyCourses().done($loadCoursesDeferred.resolve);
			});
		}
		return $loadCoursesDeferred.promise();
	};


	////////////////////
	// Initialisation //
	////////////////////

	/**
	 * Initialise the bspace favourites widget
	 */
	var doInit = function(){
		$.when(loadFavouritesList(), loadCourses()).pipe(function(dataBSpace, dataCanvas) {
			/*
			 * Didn't want to rewrite the filterOnCategory to pass through another dataObject, thus
			 * I'm merging the data using a pipe.
			 */
			return filterOnCategory(dataBSpace, categoryFilter).pipe(function(data) {
				var $combineDataDef = $.Deferred();
				$combineDataDef.resolve({'bSpace': data, 'canvas': dataCanvas});
				return $combineDataDef.promise();
			});
		}).done(renderClassesList);
	};

	// Start the request
	doInit();
};