<%@ taglib prefix="cc" uri="http://calcentral.berkeley.edu"%>

<footer>
	<div class="cc-left">Copyright &copy; 2012 The Regents of the University of California</div>
	<a href="#">Help</a>
	<a href="http://ets.berkeley.edu/calcentral-feedback">Feedback</a>
</footer>

<div id="cc-notreallink-container" style="display:none;"><!-- --></div>
<script id="cc-notreallink-template" type="text/x-handlebars-template">
	<a class="cc-icon cc-icon-close cc-right avgrund-close" href="#"><!-- --></a>
	<h3 class="cc-icon-info">Oops! You've found a link that isn't quite working yet.</h3>
	{{#compare messageId "details"}}
		<p>In the future this link will go to a Detail view for this item.</p>
	{{/compare}}
	{{#compare messageId "finaidchange"}}
		<p>This link would take you to the place to change this item in MyFinAid.</p>
	{{/compare}}
	{{#compare messageId "finaidcheck"}}
		<p>This link would take you to the place to check this item in MyFinAid.</p>
	{{/compare}}
	{{#compare messageId "standard"}}
		<p>This section displays potential future functionality for CalCentral.</p>
	{{/compare}}
</script>

<div id="cc-splash-container" style="display:none;">
	<img alt="Welcome to the CalCentral Pilot" src="/img/myb/calcentral_logo_big.png" />
	<p>This system is a work-in-progress proof of concept created by Educational Technology Services.
It is constantly evolving, so while not all functionality pictured here works yet, it expresses future ideas and planned directions.</p>

	<p>To make it clear what functionality isn't yet working we've provided messages about where these links will eventually go.</p>

	<p>Keep checking back for new functionality, as we are improving the system all the time!</p>

	<button class="cc-right cc-splash-button avgrund-close">Take me to CalCentral</button>
</div>

<script>
	var calcentral = calcentral || {};
	(function() {
		calcentral.Data = calcentral.Data || {};
		calcentral.Data.User = <cc:userJSON/>;
		calcentral.Data.Env = <cc:envJSON/>;
	})();
</script>

<!-- JavaScript at the bottom for fast page loading -->

<!-- Grab Google CDN's jQuery, with a protocol relative URL; fall back to local if offline -->
<script src="//ajax.googleapis.com/ajax/libs/jquery/1.8.2/jquery.min.js"></script>
<script>window.jQuery || document.write('<script src="/js/libs/jquery-1.8.2.min.js"><\/script>')</script>

<!-- scripts concatenated and minified via build script -->
<script src="/js/plugins.js"></script>
<script src="/js/script.js"></script>
<!-- end scripts -->
