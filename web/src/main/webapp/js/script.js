

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

  /**
 * Adds and removes classes to a list of links to allow keyboard accessibility
 *
 * @param string dropDownId
 * @param string hoverClass
 * @param int mouseOffDelay
 */
/*function dropdown(dropdownId, hoverClass, mouseOffDelay) {
  if(dropdown = document.getElementById(dropdownId)) {
    var listItems = dropdown.getElementsByTagName('li');
    for(var i = 0; i < listItems.length; i++) {
      listItems[i].onmouseover = function() { this.className = addClass(this); }
      listItems[i].onmouseout = function() {
        var that = this;
        setTimeout(function() { that.className = removeClass(that); }, mouseOffDelay);
        this.className = that.className;
      }
      
      var anchor = listItems[i].getElementsByTagName('a');
      anchor = anchor[0];
      anchor.onfocus = function() { tabOn(this.parentNode); }
      anchor.onblur = function() { tabOff(this.parentNode); }
    }
  }
  
  function tabOn(li) {
    if(li.nodeName == 'LI') {
      li.className = addClass(li);
      tabOn(li.parentNode.parentNode);
    }
  }
  
  function tabOff(li) {
    if(li.nodeName == 'LI') {
      li.className = removeClass(li);
      tabOff(li.parentNode.parentNode);
    }
  }
}*/

})();