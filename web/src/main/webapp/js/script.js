

// Top navigation
(function(){

	var $topNavigation = $('.cc-topnavigation');
	var $topNavigationItemsWithDropdown = $('a[aria-haspopup="true"]', $topNavigation);

	window.log($topNavigationItemsWithDropdown);

	$topNavigationItemsWithDropdown.on('focus, mouseenter', function() {
		var $this = $(this);
		var $dropdown = $this.siblings('.cc-topnavigation-dropdown');
		var selectedItemPosition = $this.position();
		$dropdown.css('top', selectedItemPosition.top + $this.outerHeight()).show();
	});

	$topNavigationItemsWithDropdown.on('blur, mouseleave', function() {
		$(this).siblings('.cc-topnavigation-dropdown').hide();
		//console.log('test2');
	});

})();