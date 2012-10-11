<%@ taglib tagdir="/WEB-INF/tags" prefix="tags" %>

<tags:head/>

<body class="cc-page-campus">
	<tags:header/>
	<tags:topnavigation/>
	<div class="cc-container-main" role="main">
		<!-- Page specific HTML -->
		<div class="cc-page-campus-container-left cc-left">
			<img class="cc-page-campus-icon-needhelp cc-right" src="/img/myb/need_help.png" alt="Need help?" />
			<form data-notreal="standard">
				<input class="cc-input-search" type="text" placeholder="Ask a question..." />
			</form>

			<jsp:include page="widgets/campusevents/campusevents.html" />
			<jsp:include page="widgets/campusnews/campusnews.html" />

		</div>
		<div class="cc-page-campus-container-right cc-left">
			<ul class="cc-page-campus-overview">
				<li>
					<a class="cc-icon-advising" href="#" data-notreal="standard">Advising</a>
				</li>
				<li>
					<a class="cc-icon-academicplan" href="#" data-notreal="standard">Academic Plan</a>
				</li>
				<li>
					<a class="cc-icon-campusdepartments cc-page-campus-overview-multiline" href="#">Campus Departments</a>
				</li>
				<li>
					<a class="cc-icon-career" href="#" data-notreal="standard">Career</a>
				</li>
				<li>
					<a class="cc-icon-finances" href="/secure/finances">Finances</a>
				</li>
				<li>
					<a class="cc-icon-gettingaround" href="#" data-notreal="standard">Getting Around</a>
				</li>
				<li>
					<a class="cc-icon-foodhousing" href="#" data-notreal="standard">Food &amp; Housing</a>
				</li>
				<li>
					<a class="cc-icon-healthsafety" href="#" data-notreal="standard">Health &amp; Safety</a>
				</li>
				<li>
					<a class="cc-icon-newtocampus" href="#" data-notreal="standard">New to Campus?</a>
				</li>
				<li>
					<a class="cc-icon-researchstudying cc-page-campus-overview-multiline" href="#" data-notreal="standard">Research &amp; Studying</a>
				</li>
				<li>
					<a class="cc-icon-studentgroups" href="#" data-notreal="standard">Student Groups</a>
				</li>
				<li>
					<a class="cc-icon-socialrecreation" href="#" data-notreal="standard">Social &amp; Recreation</a>
				</li>
			</ul>
		</div>
		<!-- END Page specific HTML -->
	</div>
	<tags:footer/>
</body>
</html>
