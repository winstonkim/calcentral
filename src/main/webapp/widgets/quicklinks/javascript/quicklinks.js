var calcentral = calcentral || {};
calcentral.Widgets = calcentral.Widgets || {};
/**
 * @class quicklinks
 *
 * @description
 * The 'quicklinks' widget displays "Quicklinks" a set of campus links
 * and allows the user to add thier own links to the My Links pane
 *
 * @version 0.0.1
 * @param {String} tuid Unique id of the widget
 */
calcentral.Widgets.quicklinks = function(tuid) {

	/** VARIABLES. **/

	/** DOM elements. Configurable but be aware of validator dependencies. */
	var $widgetContainer =  $('#cc-widget-quicklinks');
	var $linkTitleInput = $('#quicklinks-link-title', $widgetContainer); //value attached to validator fn, remember to match.
	var $linkUrlInput = $('#quicklinks-link-url', $widgetContainer); //value attached to validator fn, remember to match.
	var $saveLinkKeydownClass = $('.quicklinks-save-link-keydown', $widgetContainer);
	var $addEditPanel = $('.quicklinks_addedit_link_panel', $widgetContainer);
	var $addEditPanelTitle = $('.quicklinks_addedit_link_panel_title', $widgetContainer);
	var $saveLinkButton = $('#quicklinks-savelink-button', $widgetContainer);
	var $addLinkButton = $('#quicklinks-addlink-button', $widgetContainer);
	var $saveLinkClickClass = $('.quicklinks-save-link-click', $widgetContainer);
	var $accordionContainer = $('#quicklinks_accordion', $widgetContainer);
	var $addLinkModeBtn = $('#quicklinks-add-link-mode', $widgetContainer);
	var $cancelButton = $('#quicklinks-cancel-button', $widgetContainer);
	var $myLinksFormId = $('#quicklinks-form', $widgetContainer);

	/** js keydown code for the ENTER key. */
	var ENTER_KEY = 13;

	/** js keydown code for the ESC key. */
	var ESC_KEY = 27;

	/** the user's own links ("My Links"). Specified by default-links.js JSON source. **/
	var userLinkData;
	var defaultLinks;
	var configObj = {
		'defaultConfiguration': {
			'roleAware': false,
			'roleLookupScheme': 'testing',
			'roleMappings': {
				'Faculty': [
					'academic_related_staff',
					'academic_staff',
					'assistant_staff'
				],
				'Students': [
					'graduate_student',
					'undergraduate_student',
					'postgraduate_student'
				],
				'Staff': [
					'non_academic_staff',
					'research_staff'
				],
				'Guests': [
					'other'
				],
				'ALL': [
					'ALL'
				]
			}
		}
	};

	/** END VARIABLES. **/


	/** Utility Functions. **/

	/**
	 * Role information could be stored in various places in the user object. This allows
	 * flexible definitions of how to reference the roles stored in the system.
	 * @param {String} lookupStrategy name of which lookup strategy is loaded.
	 * @param {Function} callback callback function to pass the list of retrieved roles off to.
	 */
	var getMyRoles = function(lookupStrategy, callback) {
		var listedRoles = [ 'ALL' ];

		var strategies = {
			'default': function (callback) {
				calcentral.Api.User.getUser('', function(success, data) {
					//for now, seems like OAE only supports 1 role per person.
					var exists = data && data.basic && data.basic.elements && data.basic.elements.role &&
								data.basic.elements.role.value;
					var systemRole = (exists && data.basic.elements.role.value) || '';
					listedRoles.push(systemRole);
					callback(listedRoles);
				});
			},

			'testing': function (callback) {
				listedRoles.push('graduate_student', 'undergraduate_student', 'postgraduate_student');
				callback(listedRoles);
			},

			'berkeley': function (callback) {
				calcentral.Api.User.getUser('', function(success, data) {
					$.each(data.institutional.elements.role, function(key, value) {
						if (key === 'value') {
							listedRoles.push(value);
						}
					});
					callback(listedRoles);
				});
			}
		};

		if (strategies[lookupStrategy]) {
			return strategies[lookupStrategy](callback);
		} else {
			return strategies['default'](callback);
		}
	};

	/**
	 * Will overwrite the filterLinksList function with a new definition that
	 * will filter the links in the json data object based on roles. Will run
	 * only once upon initialization if the widget is to be roleAware.
	 *
	 * @param {Array} data an array of role strings that the current user has.
	 */
	var setFilterLinksList = function(data) {
		filterLinksList = function(linkDataObj) {
			var linkRoles = [];
			var whiteListArr = data;
			var ignoreIndex = defaultLinks.userSectionIndex || '-1';
			var linkSections = linkDataObj.sections || {};

			$.each(linkSections, function(index, value) {
				if (index !== ignoreIndex) {
					var linksArray = value.links || [];
					var valuesToRetain = [];
					$.each(linksArray, function(sub_index, sub_value){
						linkRoles = sub_value.roles || [];
						if (!_.isEmpty(_.intersection(whiteListArr, linkRoles))) {
							valuesToRetain.push(sub_value);
						}
					});

					value.links = valuesToRetain;
				}
			});
			return linkDataObj;
		};
	};

	/**
	 * Default definition of filterLinksList. This could potentially be overwritten
	 * if the widget is to be made role aware (by setFilterLinksList). In the non-role aware
	 * state, the filter does a simple passthrough with the links data object.
	 *
	 * @param {Object} linkDataObj JSON object that needs to have a filter applied to it.
	 * @return {Object} JSON object of the links data that could of been filtered on based on roles.
	 */
	var filterLinksList = function(linkDataObj) {
		return linkDataObj;
	};

	/**
	 * Remaps the roles assigned by the system to the roles defined, mapped, and used
	 * in the default-links.js JSON data structure. This allows for custom definitions of
	 * "roles", role groupings, etc. If there are no matches between any listed systemRole
	 * and the rolesMappingsInLinks JSON structure, the system roles are still retained.
	 *
	 * @param {Array} systemRoles array of role strings returned by the system.
	 * @param {Object} roleMappingsInLinks role mappings one expects to find in the default-links.js
	 *      definition.
	 * @return {Array} an array of roles translated for use against default-links.js
	 */
	var remapRoleGroupings = function(systemRoles, roleMappingsInLinks) {
		var returnArray = [];
		//check each one of the role buckets to see if it contains my systemAssignedRole.
		$.each(roleMappingsInLinks, function(mappingIndex, mappingValue) {
			if (!_.isEmpty(_.intersection(mappingValue, systemRoles))) {
				returnArray.push(mappingIndex);
			}
		});

		returnArray = _.union(returnArray, systemRoles);
		return returnArray;
	};

	/**
	 * Loading data for display of the widget. This will initially load the config.json file
	 * to check if the widget should be role aware before it deals with the json data stores of links.
	 * It also determines how and where to lookup the roles information.
	 * This is only called once upon widget load.
	 *
	 * @return {undefined} initialized quicklinks widget with data ready for consumption.
	 */
	var loadUserData = function() {
		var rolesEnabled = configObj.defaultConfiguration.roleAware || false;
		var roleDefinitions = configObj.defaultConfiguration.roleMappings || {};
		var roleLookupScheme = configObj.defaultConfiguration.roleLookupScheme || 'default';
		//only loading the roles handling module if roles are enabled in config.json
		if (rolesEnabled) {
			getMyRoles(roleLookupScheme, function(data) {
				data = remapRoleGroupings(data, roleDefinitions);
				setFilterLinksList(data);
				loadUserList();
			});
		} else {
			loadUserList();
		}
	};

	/** Main View Functions. **/
	/**
	 * Load user's list if there is one, and merge it with the default links. If no user's list, just use
	 * the default ones. Will also initialize and save a default user list on the first load to prevent continued
	 * warning messages from loading for subsequent reloads.
	 * @return {undefined} modifies the defaultLinks object by merging in the user link data. Also manipulates the dom
	 * by trigging renderLinkList to show the updated defaultLinks object.
	 */
	var loadUserList = function() {
		calcentral.Api.Widgets.loadWidgetData({
			id: tuid
		}, function (success, data) {
			var renderAllLinks = function (userData) {
				// merge the user's links with the default links
				$.extend(userLinkData, userData, {"label": "My Links"});
				// if userLinkData is not an array then load it with an empty array
				if (!$.isArray(userLinkData.links)) {
					userLinkData.links = [];
				}
				defaultLinks.sections[defaultLinks.userSectionIndex] = userLinkData;
				defaultLinks.activeSection = parseInt(userLinkData.activeSection || 0, 10);
				renderLinkList(defaultLinks);
			};

			if (success) {
				renderAllLinks(data);
			} else {
				renderAllLinks(userLinkData);
			}
		});
	};


	/**
	 * Add a new link OR edit an existing link to/on the user's "My Links" pane.
	 *
	 * @return {undefined} modified userLinkData json object with new link updates. Also refreshes the pane list.
	 */
	var saveLink = function() {
		if ($addLinkButton.attr('disabled') === 'true') {
			return;
		}
		$myLinksFormId.trigger('submit');
	};

	/** Event Handlers. */

	/**
	 * Runs once upon widget initialization. Will set the default common button and keystroke events
	 * for the widget.
	 *
	 * @return {undefined} buttons and keys common to the widget are bound to event handlers and their respective functions.
	 */
	var setupEventHandlers = function() {
		$addLinkModeBtn.on('click', enterAddMode);

		$cancelButton.on('click', cancelEditMode);

		$saveLinkClickClass.on('click', saveLink);

		$saveLinkKeydownClass.on('keydown', function (event) {
			if (event.keyCode === ENTER_KEY) {
				event.preventDefault();
				saveLink();
			} else if (event.keyCode === ESC_KEY) {
				event.preventDefault();
				cancelEditMode();
			}
		});


		/**
		 * Stores new links to db. This function should only be called after form fields have been validated.
		 */
		var storeNewLink = function() {
			$saveLinkClickClass.attr('disabled', true);

			var index = $myLinksFormId.attr('data-eltindex');

			index = index || userLinkData.links.length;

			userLinkData.links[index] = {
				'name': $linkTitleInput.val(),
				'url': $linkUrlInput.val(),
				'popup_description': $linkTitleInput.val()
			};
			defaultLinks.sections[defaultLinks.userSectionIndex] = userLinkData;

			calcentral.Api.Widgets.saveWidgetData({
				id: tuid,
				data: userLinkData
			}, function(success) {
				if (success) {
					cancelEditMode();
					renderLinkList(defaultLinks);
				}
			});
		};

		/**
		 * Call OAE's wrappper around the jquery Validator plugin function.
		 * Links are permanently stored only after passing validation.
		 *
		 * @type Validator object (http://docs.jquery.com/Plugins/Validation#Validator).
		 */
		calcentral.Api.Util.Forms.validate($myLinksFormId, {
			submitHandler: storeNewLink
		}, true);

	};

	/**
	 * Called when the quicklinks widget needs to be rendered or reloaded with new data.
	 *
	 * @param {Object} data defaultLinks json object with possible custom user links.
	 * @return {undefined} rendered quicklinks accordion div.
	 */
	var renderLinkList = function(data) {
		data = filterLinksList(data);
		calcentral.Api.Util.renderTemplate({
			'container': $accordionContainer,
			'data': data,
			'template': $('#quicklinks_accordion_template')
		});
		setupAccordion();
		setupEditIcons();
	};

	/**
	 * Modifies the title for the add/edit link form.
	 *
	 * @param {String} title string to set the title to.
	 */
	var setAddEditLinkTitle = function(title) {
		$addEditPanelTitle.text(title);
	};


	/**
	 * Cancels the editing mode and returns to the accordion panes.
	 *
	 * @return {undefined} return to accordion panes
	 */
	var cancelEditMode = function() {
		calcentral.Api.Util.Forms.clearValidation($myLinksFormId);
		$addEditPanel.hide();
		$myLinksFormId.attr('data-eltindex', '');
		$('label.error', $widgetContainer).hide();
		$saveLinkClickClass.attr('disabled', false);
	};

	/**
	 * Enters the mode to add a new link.
	 * @return {undefined} New modal div that will allow a user to add a new link.
	 */
	var enterAddMode = function() {
		showPane($('.quicklinks_accordion_pane:last'));
		setAddEditLinkTitle('Add Link');
		$myLinksFormId.attr('data-eltindex', '');
		$addEditPanel.show();
		$addLinkButton.show();
		$saveLinkButton.hide();
		$linkTitleInput.focus();
	};

	/**
	 * Enters the mode to edit an existing link.
	 * @param {int} index index of the link in the user list of links.
	 * @return {undefined} New modal div that will allow a user to modify an existing link.
	 */
	var enterEditMode = function(index) {
		var link = userLinkData.links[index];
		setAddEditLinkTitle('Edit Link');
		$linkTitleInput.val(link.name);
		$linkUrlInput.val(link.url);
		$myLinksFormId.attr('data-eltindex', index);
		$addEditPanel.show();
		$addLinkButton.hide();
		$saveLinkButton.show();
		$linkTitleInput.focus();
	};


	/**
	 * Iterates through all the existing my links to append the edit link and delete link buttons.
	 * Called whenever the accordion menu has to be rendered.
	 *
	 * @return {undefined} edit and delete buttons on each link are attached to event handlers and their respective functions.
	 */
	var setupEditIcons = function() {
		$('.quicklinks-edit-mylink', $widgetContainer).on('click', function() {
			var idx = $(this).attr('data-eltindex');
			idx = parseInt(idx, 10);
			enterEditMode(idx);
		});

		$('.quicklinks-delete-mylink', $widgetContainer).on('click', function() {
			var idx = $(this).attr('data-eltindex');
			idx = parseInt(idx, 10);
			userLinkData.links.splice(idx, 1);
			defaultLinks.sections[defaultLinks.userSectionIndex] = userLinkData;
			calcentral.Api.Widgets.saveWidgetData({
				id: tuid,
				data: userLinkData
			}, function(success) {
				if (success) {
					renderLinkList(defaultLinks);
				}
			}, true);
		});
	};

	/**
	 * Show a specific pane
	 * @param {Object} $pane jquery selector for a specific pane to show.
	 * @return {undefined} opens up the pane specified by the $pane argument.
	 */
	var showPane = function ($pane) {
		if (!$pane.hasClass('quicklinks_accordion_open')) {
			closePanes();
			$pane.addClass('quicklinks_accordion_open');
		}
		$pane.children('.quicklinks_accordion_content').slideDown(0, function() {
			$pane.children('.quicklinks_accordion_content').css('overflow', 'auto');
		});

		var previousActiveSection = userLinkData.activeSection;
		if ($pane.length) {
			userLinkData.activeSection = parseInt($pane.attr('data-sectionid'), 10);
		} else {
			userLinkData.activeSection = 0;
		}
		defaultLinks.activeSection = userLinkData.activeSection;
		if (previousActiveSection !== userLinkData.activeSection) {
			// save active section only if it's different
			calcentral.Api.Widgets.saveWidgetData({
				id: tuid,
				data: userLinkData
			});
		}
	};

	/**
	 * Hide a specific pane
	 * @param {Object} $pane jquery selector for a specific pane to hide.
	 * @return {undefined} collapses a pane that was open.
	 */
	var hidePane = function ($pane) {
		$pane.removeClass('quicklinks_accordion_open');
		$pane.children('.quicklinks_accordion_content').slideUp(0);
	};

	/**
	 * Iterates through all the accordion container panes and closes them all.
	 * @return {undefined} closes all the panes.
	 */
	var closePanes = function () {
		$('.quicklinks_accordion_pane', $accordionContainer).each(function () {
			hidePane($(this));
		});
	};

	/**
	 * Setup the event handlers on the accordion container titles, as well as
	 * tracking analytics on the individual link elements.
	 *
	 * @return {undefined} accordion menu with click handlers configured.
	 */
	var setupAccordion = function() {
		$('.quicklinks_section_label', $accordionContainer).on('click', function() {
			showPane($(this).parent());
		});

		if ($accordionContainer.width() > 250) {
			$accordionContainer.addClass('quicklinks_link_grid');
		}

		showPane($('.quicklinks_accordion_open', $accordionContainer));
	};

	var loadDefaultLinks = function(callback) {
		$.ajax({
			'cache': false,
			'url': '/widgets/quicklinks/default-links.json',
			'success': function(data) {
				defaultLinks = $.extend(true, {}, data);
				userLinkData = defaultLinks.sections[defaultLinks.userSectionIndex];
				callback();
			}
		});
	};

	/**
	 * Initialization Function.
	 */
	var doInit = function() {
		loadDefaultLinks(function(){
			loadUserData();
			setupEventHandlers();
		});
	};


	// run doInit() function when sakai_global.quicklinks object loads
	doInit();
};