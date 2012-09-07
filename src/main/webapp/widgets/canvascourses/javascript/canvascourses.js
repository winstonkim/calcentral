var calcentral = calcentral || {};
calcentral.Widgets = calcentral.Widgets || {};
calcentral.Widgets.canvascourses = function(tuid) {

	/////////////////////////////
	// Configuration variables //
	/////////////////////////////

	var $rootel = $('#' + tuid);
	var $canvascoursesList = $('.cc-widget-canvascourses-list', $rootel);
	var dummy = false;
	var accountID = 90242;

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

	var getCanvasData = function(url) {
		return $.ajax({
			'url': url
		});
	}

	var loadDummyCourses = function() {
		return $.ajax({
			'cache': false,
			'url': '/widgets/canvascourses/dummy/canvascourses.json'
		});
	}

	var extractCourseNameAndId = function(user_enrollment, allCourses) {
		var courseIds = $.map(user_enrollment[0], function(value, index) {
			return value.course_id;
		});
		var renderData = $.map(allCourses[0], function(course, index) {
			if ($.inArray(course.id, courseIds) > -1) {
				return {
					'name' : course.course_code + ": " + course.name,
					'id': course.id
				};
			}
		});
		return renderData;
	}

	var loadCourses = function(data) {
		if (dummy) {
			return loadDummyCourses();
		} else {
			var $loadCoursesDeferred = $.Deferred();
			$.when(getCanvasData(data.enrollment_url), getCanvasData(data.courses_url)).done(function(user_enrollment, allCourses){
				var renderData = extractCourseNameAndId(user_enrollment, allCourses);
				$loadCoursesDeferred.resolve(renderData);
			}).fail(function() {
				loadDummyCourses().done(function(data) {
					$loadCoursesDeferred.resolve(data);
				});
			});
			return $loadCoursesDeferred.promise();
		}
	};

	////////////////////
	// Initialisation //
	////////////////////

	/**
	 * Initialise the canvas classes widget
	 */
	 var doInit = function(){
	 	var data = {
	 		'uid': calcentral.Data.User.user.uid,
	 		'enrollment_url': '/api/canvas/users/sis_user_id:' + calcentral.Data.User.user.uid + '/enrollments',
	 		'courses_url': '/api/canvas/accounts/' + accountID + '/courses'
	 	};
	 	$.when(loadCourses(data)).done(renderCourses);
	 };

	// Start the request
	doInit();
};