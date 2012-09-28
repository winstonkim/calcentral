<%@ taglib prefix="cc" uri="http://calcentral.berkeley.edu"%>

<footer>
	<div class="cc-left">Copyright &copy; 2012 The Regents of the University of California</div>
	<a href="#">Help</a>
	<a href="http://ets.berkeley.edu/calcentral-feedback">Feedback</a>
</footer>

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
