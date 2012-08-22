<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="tags" %>

<tags:head/>

<body class="cc-dashboard">
	<tags:header/>
	<nav class="cc-topnavigation">
		<ul>
			<li class="cc-right">
				<a href="/" aria-haspopup="true"><c:out value="${uid}" /> User!</a>
				<div class="cc-topnavigation-dropdown" style="display:none">
					<ul>
						<li>
							<a href="/secure/logout">Sign out</a>
						</li>
					</ul>
				</div>
			</li>
		</ul>
	</nav>
	<div class="cc-container-main" role="main">
		<!-- Page specific HTML -->
		<div class="cc-container-main-left">
			<section class="cc-entity">
				<div class="cc-entity-name"><strong><c:out value="${uid}" /> User!</strong></div>
			</section>
			<nav class="cc-lefthandnavigation">
				<ul>
					<li><a href="#" class="cc-lefthandnavigation-item-selected">My dashboard</a></li>
					<li><a href="#">My profile</a></li>
				</ul>
			</nav>
		</div>
		<div class="cc-container-main-right">
			<div class="cc-container-widgets">
				<jsp:include page="widgets/walktime/walktime.jsp" />
			</div>
		</div>
		<!-- END Page specific HTML -->
		<hr class="clearfix" />
	</div>
	<tags:footer/>
</body>
</html>