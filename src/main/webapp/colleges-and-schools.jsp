<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="tags" %>

<tags:head/>

<body class="cc-page-colleges-and-schools">
	<tags:header/>
	<nav class="cc-topnavigation">
		<ul>
			<li class="cc-right"><a href="/secure/dashboard">Sign in</a></li>
		</ul>
	</nav>
	<div class="cc-container-main cc-container-main-full" role="main">
		<!-- Page specific HTML -->
		<h1>Colleges and Schools</h1>
		<div class="cc-page-colleges-and-schools-container"><!-- --></div>

		<script id="cc-page-colleges-and-schools-template" type="text/x-handlebars-template" style="display:none;">
		<ul>
        {{#each_object collegesandschools}}
        	<li><a {{#if value.cssclass}} class="cc-page-colleges-and-schools-item-{{value.cssclass}}"{{/if}} href="#">{{value.title_prefix}}<br /><span class="cc-page-colleges-and-schools-title">{{value.title}}</span></a></li>
        {{/each_object}}
        </ul>
        </script>

		<!-- END Page specific HTML -->
		<br class="clearfix" />
	</div>
	<tags:footer/>
</body>
</html>