<%@ taglib tagdir="/WEB-INF/tags" prefix="tags" %>

<tags:head/>

<body class="cc-page-campus">
	<tags:header/>
	<tags:topnavigation/>
	<div class="cc-container-main" role="main">
		<!-- Page specific HTML -->
		<div class="cc-page-campus-container-left cc-left">
			<img class="cc-page-campus-icon-needhelp cc-right" src="/img/myb/need_help.png" alt="Need help?" />
			<form method="GET">
				<input class="cc-input-search" type="text" placeholder="Ask a question..." />
			</form>

			<jsp:include page="widgets/campusevents/campusevents.html" />
			<jsp:include page="widgets/campusnews/campusnews.html" />

		</div>
		<div class="cc-page-campus-container-right cc-left">
			<ul class="cc-page-campus-overview">
				<li>
					<a class="cc-icon-advising" href="#">Advising</a>
				</li>
				<li>
					<a class="cc-icon-academicplan" href="#">Academic Plan</a>
				</li>
				<li>
					<a class="cc-icon-campusdepartments cc-page-campus-overview-multiline" href="#">Campus Departments</a>
				</li>
				<li>
					<a class="cc-icon-career" href="#">Career</a>
				</li>
				<li>
					<a class="cc-icon-finances" href="#">Finances</a>
				</li>
				<li>
					<a class="cc-icon-gettingaround" href="#">Getting Around</a>
				</li>
				<li>
					<a class="cc-icon-foodhousing" href="#">Food &amp; Housing</a>
				</li>
				<li>
					<a class="cc-icon-healthsafety" href="#">Health &amp; Safety</a>
				</li>
				<li>
					<a class="cc-icon-newtocampus" href="#">New to Campus?</a>
				</li>
				<li>
					<a class="cc-icon-researchstudying cc-page-campus-overview-multiline" href="#">Research &amp; Studying</a>
				</li>
				<li>
					<a class="cc-icon-studentgroups" href="#">Student Groups</a>
				</li>
				<li>
					<a class="cc-icon-socialrecreation" href="#">Social &amp; Recreation</a>
				</li>
			</ul>
		</div>
		<!-- END Page specific HTML -->
	</div>
	<tags:footer/>
</body>
</html>
