// Global calcentral object
calcentral = {};


/**
 * Top Navigation
 */
(function(){

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