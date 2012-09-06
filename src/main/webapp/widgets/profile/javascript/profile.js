var calcentral = calcentral || {};
calcentral.Widgets = calcentral.Widgets || {};
calcentral.Widgets.profile = function(tuid) {

	/////////////////////////////
	// Configuration variables //
	/////////////////////////////

	var $rootel = $('#' + tuid);
	var $profileList = $('.cc-widget-profile-list', $rootel);
	var $preferredNameInput;
	var $linkInput;

	////////////////////
	// Event Handlers //
	////////////////////

	///////////////
	// Rendering //
	///////////////

	var renderProfile = function(success, data) {
		calcentral.Api.Util.renderTemplate({
			'container': $profileList,
			'data': data,
			'template': $('#cc-widget-profile-list-template', $rootel)
		});
		$preferredNameInput = $('#cc-widget-profile-preferredName', $rootel).on('blur', saveProfile);
		$linkInput = $('#cc-widget-profile-link', $rootel).on('blur', saveProfile);
	};

	///////////////////
	// Ajax Requests //
	///////////////////

	var saveProfile = function() {
		newUserData = {
			'preferredName': $preferredNameInput.val(),
			'link': $linkInput.val(),
			'uid': calcentral.Data.User.user.uid
		};
		console.log('Profile widget - Saving profile: ', newUserData);
		calcentral.Api.User.saveUser(newUserData, function(success, data) {
			console.log('Profile widget - Profile saved: ' + success);
		});
	};

	////////////////////
	// Initialisation //
	////////////////////

	/**
	 * Initialise the profile widget
	 */
	var doInit = function() {
		// We always want an updated version of the the current user on the profile page.
		calcentral.Api.User.getCurrentUser({
			'refresh': false
		}, renderProfile);
	};

	// Start the request
	doInit();
};
