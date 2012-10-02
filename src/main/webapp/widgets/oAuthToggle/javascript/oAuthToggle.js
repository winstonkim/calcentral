var calcentral = calcentral || {};
calcentral.Widgets = calcentral.Widgets || {};
calcentral.Widgets.oAuthToggle = function(tuid) {


	/////////////////////////////
	// Configuration variables //
	/////////////////////////////

	'use strict';

	var $rootel = $('#' + tuid);
	var $oAuthToggleList = $('.cc-widget-oAuthToggle-list', $rootel);

	var urlMap = {
		'canvas': '/api/canvas/canvasOAuthEnabled',
		'google': '/api/google/gappsOAuthEnabled'
	};


	////////////////////
	// Event Handlers //
	////////////////////

	/**
	 * Disable the token for an oAuth service
	 */
	var disableToken = function() {
		var service = $(this).attr('data-disable-service');

		$.ajax({
			'url': urlMap[service],
			'type': 'POST',
			'data': {
				'method': 'delete'
			},
			'success': function() {
				window.location = "/secure/preferences";
			}
		});
		return false;

	};

	var addBinding = function () {
		$('a[data-disable-service]', $oAuthToggleList).on('click', disableToken);
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
		addBinding();
	};

	init();
};
