var calcentral = calcentral || {};
calcentral.Widgets = calcentral.Widgets || {};
calcentral.Widgets.canvascourses = function(tuid) {

	/////////////////////////////
	// Configuration variables //
	/////////////////////////////

	var $rootel = $('#' + tuid);
	var $canvascoursesList = $('.cc-widget-canvascourses-list', $rootel);
	// Used to control whether or not to load the dummy feed.
	var dummy = false;

	////////////////////
	// Event Handlers //
	////////////////////

	///////////////
	// Rendering //
	///////////////

	var renderCourses = function(data) {
		calcentral.Api.Util.renderTemplate({
			'container': $canvascoursesList,
			'data': {
				'courses': data.courses,
				'host': data.host
			},
			'template': $('#cc-widget-canvascourses-list-template', $rootel)
		});
	};

	///////////////////
	// Ajax Requests //
	///////////////////

	/**
	 * Will always load the list of dummy courses hardcoded for the widget.
	 * @return {Object} Ajax object for fetching the dummy couress.
	 */
	var loadDummyCourses = function() {
		var $ajaxWrapper = $.Deferred();
		$.ajax({
			'cache': false,
			'url': '/widgets/canvascourses/dummy/canvascourses.json',
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

	// Initialise the canvas classes widget
	var doInit = function(){
		$.when(loadCourses()).done(renderCourses);
	};

	// Start the request
	doInit();

	return {
		'loadCourses': loadCourses
	};
};