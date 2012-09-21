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
	/**
	 * Initialise the classes widget, after functions from other widgets are resolved.
	 */
	var init = function() {
		renderToggles(calcentral.Data.User.oauth);
		actionBindings();
	};

	init();
};