// Global calcentral object
var calcentral = calcentral || {};

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

	calcentral.Api.User.getUser = function(callback) {

		callback({
			'userId': 12345
		});
	};
})();

/**
 * Widgets
 */
(function() {

	calcentral.Widgets = calcentral.Widgets || {};

	var widgetLocation = '/widgets/';
	var widgetPrefix = 'cc-widget-';
	var widgetsToLoad = ['quicklinks', 'walktime'];

	var loadCSS = function(widgetName) {
		var widgetCSSLocation = widgetLocation + widgetName + '/css/' + widgetName + '.css';
		$('<link rel="stylesheet" type="text/css" href="' + widgetCSSLocation + '"/>').appendTo('head');
	};

	var loadJavaScript = function(widgetName) {
		var widgetJavaScriptLocation = widgetLocation + widgetName + '/javascript/' + widgetName + '.js';
		$.getScript(widgetJavaScriptLocation, function(data, textStatus, jqxhr) {
			calcentral.Widgets[widgetName]();
		});
	};

	var loadWidget = function(widgetName){
		loadCSS(widgetName);
		loadJavaScript(widgetName);
	};

	var loadWidgets = function() {
		for (var i = 0; i < widgetsToLoad.length; i++) {
			var widgetName = widgetsToLoad[i];
			loadWidget(widgetName);
		}
	};
	loadWidgets();

})();

/**
 * Top Navigation
 */
(function() {

	var $openMenu = false;
	var $topNavigation = $('.cc-topnavigation');
	var $topNavigationItemsWithDropdown = $('a[aria-haspopup="true"]', $topNavigation);

	window.log($topNavigationItemsWithDropdown);

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

	$('.cc-topnavigation > ul > li').on('mouseleave', function(e) {
		closeMenu();
	});

})();