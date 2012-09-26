<%@ taglib tagdir="/WEB-INF/tags" prefix="tags" %>

<tags:head/>

<body class="cc-page-dashboard">
	<tags:header/>
	<tags:topnavigation/>
	<div class="cc-container-main" role="main">
		<!-- Page specific HTML -->
		<div class="cc-container-widgets">
			<div class="cc-page-dashboard-column-narrow cc-left">
				<jsp:include page="widgets/mycalendar/mycalendar.html" />
				<jsp:include page="widgets/myclasses/myclasses.html" />
				<jsp:include page="widgets/mygroups/mygroups.html" />
			</div>
			<div class="cc-page-dashboard-column-wide cc-left">
				<jsp:include page="widgets/tasks/tasks.html" />
			</div>
			<div class="cc-page-dashboard-column-wide cc-left">
				<jsp:include page="widgets/notifications/notifications.html" />
			</div>
			<jsp:include page="widgets/oAuthToggle/oAuthToggle.html" />
		</div>
		<!-- END Page specific HTML -->
		<br class="clearfix" />
	</div>
	<tags:footer/>
</body>
</html>
