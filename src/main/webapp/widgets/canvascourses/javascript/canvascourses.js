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
				'courses': data
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
	 	return $.ajax({
	 		'cache': false,
	 		'url': '/widgets/canvascourses/dummy/canvascourses.json'
	 	});
	 }

	 /**
	  * Get the current user's canvas courses. If success, do some filtering on the results
	  * to only return the parts necesssary for rendering.
	  * @return {Array} Array of JSON objects to pass off to the template renderer.
	  */
	 var getCanvasCourses = function() {
	 	$ajaxWrapper = $.Deferred();
	 	$.ajax({
	 		'url': '/api/canvas/courses',
	 		'success': function(data) {
	 			//do some translation on the results. Expecting an array of course JSON object.
	 			var result = $.map(data, function(value, index) {
	 				return {
	 					'id': value.id,
	 					'name': value.course_code + ": " + value.name
	 				};
	 			});
	 			$ajaxWrapper.resolve(result);
	 		},
	 		'error': $ajaxWrapper.reject
	 	});

	 	return $ajaxWrapper.promise();
	 }

	/**
	 * Fetch users's course data from canvas.
	 * @return {Object} Deferred promise object for a Deferrred chain, with a (data) param.
	 */
	 var loadCourses = function() {
	 	if (dummy) {
	 		return loadDummyCourses();
	 	} else {
	 		var $loadCoursesDeferred = $.Deferred();
	 		$.when(getCanvasCourses()).done($loadCoursesDeferred.resolve).fail(function() {
	 			loadDummyCourses().done($loadCoursesDeferred.resolve);
	 		});
	 		return $loadCoursesDeferred.promise();
	 	}
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
};