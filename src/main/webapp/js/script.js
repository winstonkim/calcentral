// Global calcentral object
var calcentral = calcentral || {};

/**
 * Data
 */
(function() {
	calcentral.Data = calcentral.Data || {};
	calcentral.Data.User = calcentral.Data.User || {};
	calcentral.Data.User.userId = 300646;
})();

/**
 * API
 */
(function() {
	calcentral.Api = calcentral.Api || {};
})();

/**
 * API Users
 */
(function() {
	calcentral.Api.User = calcentral.Api.User || {};

	calcentral.Api.User.getUser = function(config, callback) {
		callback(true, {
			'userId': calcentral.Data.User.userId
		});
	};

	calcentral.Api.User.getCurrentUser = function(callback) {
		$.ajax({
			'success': function(data) {
				callback(true, data.currentUser);
			},
			'url': '/api/currentUser'
		});
	};

})();

(function() {
	var templateCache = [];
	calcentral.Api.Util = calcentral.Api.Util || {};

	calcentral.Api.Util.Forms = calcentral.Api.Util.Forms || {};

	calcentral.Api.Util.Forms.clearValidation = function($form) {
		$form.find("span.cc-error, span.cc-error-after").remove();
		$form.find(".cc-error").removeClass("cc-error");
		$form.find(".cc-error-after").removeClass("cc-error-after");
		$form.find("*[aria-invalid]").removeAttr("aria-invalid");
		$form.find("*[aria-describedby]").removeAttr("aria-describedby");
	};

	calcentral.Api.Util.Forms.validate = function($form, opts, insertAfterLabel) {
		var options = {
			onclick: false,
			onkeyup: false,
			onfocusout: false
		};

		// When you set onclick to true, you actually just don't set it
		// to false, because onclick is a handler function, not a boolean
		if (opts) {
			$.each(options, function(key,val) {
				if (opts.hasOwnProperty(key) && opts[key] === true) {
					delete opts[key];
					delete options[key];
				}
			});
		}
		options.errorElement = 'span';
		options.errorClass = insertAfterLabel ? 'cc-error-after' : 'cc-error';

		// We need to handle success and invalid in the framework first
		// then we can pass it to the caller
		var successCallback = false;
		var invalidCallback = false;

		if (opts) {
			if (opts.hasOwnProperty('success') && $.isFunction(opts.success)) {
				successCallback = opts.success;
				delete opts.success;
			}

			if (opts && opts.hasOwnProperty('invalidCallback') && $.isFunction(opts.invalidCallback)) {
				invalidCallback = opts.invalidCallback;
				delete opts.invalidCallback;
			}
		}

		// Don't allow spaces in the field
		$.validator.addMethod('nospaces', function(value, element) {
			return this.optional(element) || (value.indexOf(' ') === -1);
		}, 'No spaces are allowed');

		// Appends http:// or ftp:// or https:// when necessary
		$.validator.addMethod('appendhttp', function(value, element) {
			if (value.substring(0,7) !== 'http://' &&
				value.substring(0,6) !== 'ftp://' &&
				value.substring(0,8) !== 'https://' &&
				$.trim(value) !== '') {
					$(element).val('http://' + value);
			}
			return true;
		});

		// Add the methods that were being passed in
		if (opts && opts.hasOwnProperty('methods')) {
			$.each(opts.methods, function(key, value) {
				$.validator.addMethod(key, value.method, value.text);
			});
			delete opts.methods;
		}

		// Include the passed in options
		$.extend(true, options, opts);

		// Success is a callback on each individual field being successfully validated
		options.success = options.success || function($label) {
			// For autosuggest clearing, since we have to put the error on the ul instead of the element
			if (insertAfterLabel && $label.next('ul.as-selections').length) {
				$label.next('ul.as-selections').removeClass('cc-error');
			} else if ($label.prev('ul.as-selections').length) {
				$label.prev('ul.as-selections').removeClass('cc-error');
			}
			$label.remove();
			if ($.isFunction(successCallback)) {
				successCallback($label);
			}
		};

		options.errorPlacement = options.errorPlacement || function($error, $element) {
			if ($element.hasClass('cc-error-calculate')) {
				// special element with variable left margin
				// calculate left margin and width, set it directly on the error element
				$error.css({
					'margin-left': $element.position().left,
					'width': $element.width()
				});
			}
			// Get the closest-previous label in the DOM
			var $prevLabel = $form.find('label[for="' + $element.attr('id') + '"]');
			$error.attr('id', $element.attr('name') + '_error');
			$element.attr('aria-describedby', $element.attr('name') + '_error');
			if (insertAfterLabel) {
				$error.insertAfter($prevLabel);
			} else {
				$error.insertBefore($prevLabel);
			}
		};

		options.invalidHandler = options.invalidHandler || function($thisForm, validator) {
			$form.find('.cc-error').attr('aria-invalid', 'false');
			if ($.isFunction(invalidCallback)) {
				invalidCallback($thisForm, validator);
			}
		};

		options.showErrors = options.showErrors || function(errorMap, errorList) {
			if (errorList.length !== 0 && $.isFunction(options.error)) {
				options.error();
			}
			$.each(errorList, function(i,error) {
				$(error.element).attr('aria-invalid', 'true');
				// Handle errors on autosuggest
				if ($(error.element).hasClass('cc-error-autosuggest')) {
					$(error.element).parents('ul.as-selections').addClass('cc-error');
				}
			});
			this.defaultShowErrors();
			if ($.isFunction(options.errorsShown)) {
				options.errorsShown();
			}
		};

		// Set up the form with these options in jquery.validate
		$form.validate(options);
	};

	calcentral.Api.Util.renderTemplate = function(config) {

		if (!config || !config.template || !config.data || !config.container) {
			throw 'Please supply a config parameter with a template, some data and a container';
		}

		// Register the partials if you supply any:
		if (config.partials) {
			for (var i in config.partials) {
				if (config.partials.hasOwnProperty(i)) {
					Handlebars.registerPartial(i, config.partials[i]);
				}
			}
		}

		// HELPER: #each_object
		//
		// Usage: {{#each_object obj}} Key: {{key}} // Value: {{value}} {{/each_object}}
		//
		// Iterate over an object, setting 'key' and 'value' for each property in
		// the object.
		Handlebars.registerHelper('each_object', function(obj, fn) {
			var buffer = '',
				key;

			for (key in obj) {
				if (obj.hasOwnProperty(key)) {
					buffer += fn({key: key, value: obj[key]});
				}
			}

			return buffer;
		});

		// {{#each_with_index records}}
		//	<li class="legend_item{{index}}"><span></span>{{Name}}</li>
		// {{/each_with_index}}
		Handlebars.registerHelper("each_with_index", function(array, fn) {
			var buffer = "";
			for (var i = 0, j = array.length; i < j; i++) {
				var item = array[i];

				// stick an index property onto the item, starting with 1, may make configurable later
				item.index = i;

				// show the inside of the block
				buffer += fn(item);
			}

			// return the finished buffer
			return buffer;

		});

		// {{#compare "Test" "Test"}}
		// Default comparison of "==="
		// {{/compare}}
		Handlebars.registerHelper('compare', function (lvalue, operator, rvalue, options) {

			var operators, result;

			if (arguments.length < 3) {
				throw new Error("Handlerbars Helper 'compare' needs 2 parameters");
			}

			if (options === undefined) {
				options = rvalue;
				rvalue = operator;
				operator = "===";
			}

			operators = {
				'==': function (l, r) { return l == r; },
				'===': function (l, r) { return l === r; },
				'!=': function (l, r) { return l != r; },
				'!==': function (l, r) { return l !== r; },
				'<': function (l, r) { return l < r; },
				'>': function (l, r) { return l > r; },
				'<=': function (l, r) { return l <= r; },
				'>=': function (l, r) { return l >= r; },
				'typeof': function (l, r) { return typeof l == r; }
			};

			if (!operators[operator]) {
				throw new Error("Handlerbars Helper 'compare' doesn't know the operator " + operator);
			}

			result = operators[operator](lvalue, rvalue);

			if (result) {
				return options.fn(this);
			} else {
				return options.inverse(this);
			}

		});

		var source = config.template.html();
		var template = Handlebars.compile(source);
		config.container.html(template(config.data));
	};
})();

/**
 * API Widgets
 */

(function() {

	calcentral.Api.Widgets = calcentral.Api.Widgets || {};

	var createWidgetDataUrl = function(widgetId) {
		return '/api/user/' + calcentral.Data.User.userId + '/widgetData/' + widgetId;
	};

	calcentral.Api.Widgets.loadWidgetData = function(config, callback) {

		if (!config || !config.id) {
			window.log('calcentral.Api.Widgets.loadWidgetData - Please provide a config object with an id.');
		}

		$.ajax({
			'cache': false,
			'success': function(data) {
				if (data && data.widgetData && data.widgetData.data) {
					callback(true, $.parseJSON(data.widgetData.data));
				} else {
					callback(false);
				}
			},
			'url': createWidgetDataUrl(config.id)
		});

	};

	calcentral.Api.Widgets.saveWidgetData = function(config, callback) {

		if (!config || !config.id || !config.data) {
			window.log('calcentral.Api.Widgets.saveWidgetData - Please provide a config object with an id and data.');
		}

		$.ajax({
			'data': {
				'data': JSON.stringify(config.data)
			},
			'success': function(data) {
				if ($.isFunction(callback)) {
					callback(true, data);
				}
			},
			'type': 'POST',
			'url': createWidgetDataUrl(config.id)
		});
	};

})();

/**
 * Dashboard
 */
(function() {

	/*$('.cc-container-widgets').masonry({
		itemSelector: '.cc-container-widget',
		columnWidth: 348,
		gutterWidth: 20
	});*/

	calcentral.Widgets = calcentral.Widgets || {};

	var widgetLocation = '/widgets/';
	var widgetPrefix = 'cc-widget-';
	var widgetsToLoad = ['quicklinks', 'walktime', 'bspacefavourites', 'canvascourses'];

	var loadCSS = function(widgetName) {
		var widgetCSSLocation = widgetLocation + widgetName + '/css/' + widgetName + '.css';
		$('<link rel="stylesheet" type="text/css" href="' + widgetCSSLocation + '"/>').appendTo('head');
	};

	var loadJavaScript = function(widgetName, callback) {

		var widgetJavaScriptLocation = widgetLocation + widgetName + '/javascript/' + widgetName + '.js';

		var script = document.createElement("script");
		script.type = "text/javascript";

		if (script.readyState) { //IE
			script.onreadystatechange = function () {
				if (script.readyState == "loaded" || script.readyState == "complete") {
					script.onreadystatechange = null;
					callback();
				}
			};
		} else { //Others
			script.onload = function () {
				callback();
			};
		}

		script.src = widgetJavaScriptLocation;
		document.getElementsByTagName("head")[0].appendChild(script);
	};

	var loadWidget = function(widgetName){
		loadCSS(widgetName);
		loadJavaScript(widgetName, function() {
			calcentral.Widgets[widgetName](widgetPrefix + widgetName);
		});
	};

	var loadWidgets = function() {
		for (var i = 0; i < widgetsToLoad.length; i++) {
			var widgetName = widgetsToLoad[i];
			loadWidget(widgetName);
		}
	};

	var renderLeftHandNavigation = function(data) {
		calcentral.Api.Util.renderTemplate({
			'container': $('.cc-container-main-left'),
			'data': data,
			'template': $('#cc-container-main-left-template')
		});
	};

	var loadLeftHandNavigation = function() {
		calcentral.Api.User.getCurrentUser(function(success, data) {
			renderLeftHandNavigation(data);
		});
	};

	if ($('.cc-page-dashboard').length){
		loadWidgets();
		loadLeftHandNavigation();
	}

})();


/**
 * Clickable masthead - Logged in users go to dashboard, anon users go to "/"
 */
(function() {

	var $bannerTop = $('header');

	calcentral.Api.User.getCurrentUser(function(success, data) {
		$bannerTop.on('click', function() {
			window.location = data.loggedIn ? '/secure/dashboard' : '/';
		});
	});

})();


/**
 * Top Navigation
 */
(function() {

	var $topNavigation = $('.cc-topnavigation');

	var addBinding = function() {

		var $openMenu = false;
		var $topNavigationItemsWithDropdown = $('a[aria-haspopup="true"]', $topNavigation);

		var removeSelected = function() {
			$('.cc-topnavigation-selected').removeClass('cc-topnavigation-selected');
		};

		var closeMenu = function(){
			if ($openMenu.length) {
				$openMenu.hide();
				removeSelected();
			}
		};

		$topNavigationItemsWithDropdown.on('focus mouseenter', function() {
			var $this = $(this).addClass('cc-topnavigation-selected');
			$openMenu = $this.siblings('.cc-topnavigation-dropdown');
			var selectedItemPosition = $this.position();
			$openMenu.css({
				'top': selectedItemPosition.top + $this.outerHeight() - 2
			}).show();
		});

		$topNavigationItemsWithDropdown.parent().on('mouseleave', function(e) {
			closeMenu();
		});

	};

	if ($topNavigation.length) {
		calcentral.Api.User.getCurrentUser(function(success, data){
			calcentral.Api.Util.renderTemplate({
				'container': $topNavigation,
				'data': data,
				'template': $('#cc-topnavigation-template')
			});
			addBinding();
		});
	}

})();

/**
 * Colleges and schools
 */
(function() {
	var $collegesAndSchools = $('.cc-page-colleges-and-schools');

	var $collegesAndSchoolsContainer = $('.cc-page-colleges-and-schools-container', $collegesAndSchools);

	var renderCollegesAndSchools = function(data) {
		calcentral.Api.Util.renderTemplate({
			'container': $collegesAndSchoolsContainer,
			'data': $.parseJSON(data),
			'template': $('#cc-page-colleges-and-schools-template', $collegesAndSchools)
		});
	};

	var loadCollegesAndSchools = function() {
		return $.ajax({
			'url': '/dummy/colleges-and-schools.json'
		});
	};

	if($collegesAndSchools.length) {
		$.when(loadCollegesAndSchools()).then(renderCollegesAndSchools);
	}

})();

/**
 * ClassPage
 */
(function() {
	var $classPage = $('.cc-page-classpage');

	var $classPageContainer = $('.cc-page-classpage-container', $classPage);

	var renderClassPage = function(data) {
		var partials = {
			'courseInfo': $('#cc-page-classpage-courseinfo-template', $classPage).html(),
			'description': $('#cc-page-classpage-description-template', $classPage).html(),
			'instructor': $('#cc-page-classpage-instructor-template', $classPage).html()
		};

		calcentral.Api.Util.renderTemplate({
			'container': $classPageContainer,
			'data': $.parseJSON(data),
			'partials': partials,
			'template': $('#cc-page-classpage-template', $classPage)
		});
	};

	var loadClassPage = function(id) {
		return $.ajax({
			'url': '/dummy/classpage.json'
		});
	};

	if($classPage.length) {
		$.when(loadClassPage()).then(renderClassPage);
	}

})();
