var calcentral = calcentral || {};
calcentral.Widgets = calcentral.Widgets || {};
calcentral.Widgets.oAuthToggle = function(tuid) {

	/////////////////////////////
	// Configuration variables //
	/////////////////////////////

	var $rootel = $('#' + tuid);
	var $oAuthToggleList = $('.cc-widget-oAuthToggle-list', $rootel);

	////////////////////
	// Event Handlers //
	////////////////////

	var actionBindings = function () {
		$('a.acquireGoogleToken', $oAuthToggleList).on('click', function() {
			window.location = "/api/google/gappsOAuthResponse";
		});

		$('a.acquireCanvasToken', $oAuthToggleList).on('click', function() {
			window.location = "/api/canvas/oAuthToken";
		});

		$('a.disableGoogleToken', $oAuthToggleList).on('click', function() {
			$.ajax({
				'url': '/api/google/gappsOAuthEnabled',
				'type': 'POST',
				'data': {
					'method': 'delete'
				},
				'success': function() {
					window.location = "/secure/dashboard";
				}
			});
		});

		$('a.disableCanvasToken', $oAuthToggleList).on('click', function() {
			$.ajax({
				'url': '/api/canvas/canvasOAuthEnabled',
				'type': 'POST',
				'data': {
					'method': 'delete'
				},
				'success': function() {
					window.location = "/secure/dashboard";
				}
			});
		});


	};
	///////////////
	// Rendering //
	///////////////

	var renderToggles = function(data) {
		calcentral.Api.Util.renderTemplate({
			'container': $oAuthToggleList,
			'data': data,
			'template': $('#cc-widget-oAuthToggle-list-template', $rootel)
		});
	};

	////////////////////
	// Initialisation //
	////////////////////

	var checkGoogleToken = function() {
		return $.ajax({
			'url': '/api/google/gappsOAuthEnabled'
		});
	};

	var checkCanvasToken = function() {
		return $.ajax({
			'url': '/api/canvas/canvasOAuthEnabled'
		});
	};

	/**
	 * Initialise the classes widget, after functions from other widgets are resolved.
	 */

	var init = function() {
		$.when(checkGoogleToken(), checkCanvasToken()).done(function(googleData, canvasData) {
			var mergeData = {};
			$.extend(mergeData, googleData[0], canvasData[0]);
			renderToggles(mergeData);
			actionBindings();
		});
	};

	init();
};