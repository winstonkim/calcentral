<%@ taglib tagdir="/WEB-INF/tags" prefix="tags" %>

<tags:head/>

<body class="cc-page-profile">
<tags:header/>
<tags:topnavigation/>
<div class="cc-container-main" role="main">
	<!-- Page specific HTML -->
		<div class="cc-container-widgets">
			<jsp:include page="widgets/profile/profile.html" />
		</div>
	<!-- END Page specific HTML -->
	<br class="clearfix" />
</div>
<tags:footer/>
</body>
</html>