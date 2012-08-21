<%@ taglib tagdir="/WEB-INF/tags" prefix="tags" %>

<tags:head/>

<body class="cc-user">
	<tags:header/>
	<nav class="cc-topnavigation">
		<ul>
			<li class="cc-right">
				<a href="/" aria-haspopup="true">Oliver Heyer</a>
				<ul class="cc-topnavigation-dropdown" style="display:none">
					<li>
						<a href="/">Sign out</a>
					</li>
				</ul>
			</li>
		</ul>
	</nav>
	<div class="cc-container-main" role="main">
		<!-- Page specific HTML -->
		<div class="cc-mainleftcolumn">
			<section class="cc-entity">
				<div class="cc-entity-name"><strong>Oliver Heyer<strong></div>
			</section>
			<nav class="cc-lefthandnavigation">
				<ul>
					<li><a href="#" class="cc-lefthandnavigation-item-selected">My dashboard</a></li>
					<li><a href="#">My profile</a></li>
				</ul>
			</nav>
		</div>
		<!-- END Page specific HTML -->
	</div>
	<tags:footer/>
</body>
</html>