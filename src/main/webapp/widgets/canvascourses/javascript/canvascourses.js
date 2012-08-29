var calcentral = calcentral || {};
calcentral.Widgets = calcentral.Widgets || {};
calcentral.Widgets.canvascourses = function(tuid) {

	/////////////////////////////
	// Configuration variables //
	/////////////////////////////

	var $rootel = $('#' + tuid);
	var $canvascoursesList = $('.cc-widget-canvascourses-list', $rootel);

	////////////////////
	// Event Handlers //
	////////////////////

	///////////////
	// Rendering //
	///////////////

	var renderCourses = function(data) {
		data = $.parseJSON(data);
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

	var loadCourses = function() {
		return $.ajax({
			'cache': false,
			'url': '/widgets/canvascourses/dummy/canvascourses.json'
		});
	};

	////////////////////
	// Initialisation //
	////////////////////

	/**
	 * Initialise the canvas classes widget
	 */
	var doInit = function(){
		$.when(loadCourses()).done(renderCourses);
	};

	// Start the request
	doInit();
};