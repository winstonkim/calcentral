var calcentral = calcentral || {};
calcentral.Widgets = calcentral.Widgets || {};
calcentral.Widgets.profile = function (tuid) {

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

	var renderProfile = function (data) {
		calcentral.Api.Util.renderTemplate({
			'container':$profileList,
			'data':calcentral.Data.User,
			'template':$('#cc-widget-profile-list-template', $rootel)
		});
		$preferredNameInput = $('#cc-widget-profile-preferredName', $rootel);
		$preferredNameInput.on("blur", saveProfile);
		$linkInput = $('#cc-widget-profile-link', $rootel);
		$linkInput.on("blur", saveProfile);
	};

	///////////////////
	// Ajax Requests //
	///////////////////

	var saveProfile = function () {
		newUserData = {
			"preferredName" : $preferredNameInput.val(),
			"link" : $linkInput.val(),
			"uid" : calcentral.Data.User.uid
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
	var doInit = function () {
		calcentral.Api.User.getCurrentUser(renderProfile);
	};

	// Start the request
	doInit();
};