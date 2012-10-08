<nav class="cc-topnavigation"><!-- --></nav>

<script id="cc-topnavigation-template" type="text/x-handlebars-template">
	<ul class="cc-topnavigation-primary">
		<li class="cc-left"><a href="/secure/dashboard" {{#compare pathName "/secure/dashboard"}} class="cc-topnavigation-selected"{{/compare}}>My Dashboard</a></li>
		<li class="cc-left"><a href="/secure/campus"{{#compare pathName "/secure/campus"}} class="cc-topnavigation-selected"{{/compare}}{{#compare pathName "/secure/finances"}} class="cc-topnavigation-selected cc-topnavigation-selected-subnav"{{/compare}}>My Campus</a></li>
		<ul>
			<li class="cc-right">
				<input class="cc-input-search" type="text" placeholder="Search..." />
			</li>
			<li class="cc-right">
				<a href="/" aria-haspopup="true">Discover...</a>
				<div class="cc-topnavigation-dropdown" style="display:none">
					<div>
						<h3>Classes</h3>
						<ul>
							<li>
								<a href="/colleges-and-schools.jsp">by Colleges &amp; Schools</a>
							</li>
							<li>
								<a href="#">in My Department</a>
							</li>
							<li>
								<a href="#">in My Major</a>
							</li>
						</ul>
					</div>
					<div>
						<h3>People</h3>
						<ul>
							<li>
								<a href="#">by Colleges &amp; Schools</a>
							</li>
							<li>
								<a href="#">in My Department</a>
							</li>
							<li>
								<a href="#">in My Major</a>
							</li>
							<li>
								<a href="#">in My Groups</a>
							</li>
						</ul>
					</div>
					<div>
						<h3>Groups</h3>
						<ul>
							<li>
								<a href="#">by Colleges &amp; Schools</a>
							</li>
							<li>
								<a href="#">Academic Groups</a>
							</li>
							<li>
								<a href="#">Administrative Groups</a>
							</li>
							<li>
								<a href="#">Campus Life Groups</a>
							</li>
						</ul>
					</div>
				</div>
			</li>
		</ul>
	</ul>
	{{#compare pathName "/secure/finances"}}
	<ul class="cc-topnavigation-subnav">
		<li><a href="#">Advising</a></li>
		<li><a href="#">Academic Plan</a></li>
		<li><a href="#">Departments</a></li>
		<li><a href="#">Career</a></li>
		<li><a href="#" class="cc-topnavigation-selected">Finances</a></li>
		<li><a href="#">Getting Around</a></li>
		<li><a href="#">Food &amp; Housing</a></li>
		<li><a href="#">Health &amp; Safety</a></li>
		<li><a href="#">New?</a></li>
		<li><a href="#">Research &amp; Studying</a></li>
		<li><a href="#">Student Groups</a></li>
		<li><a href="#">Social &amp; Recreation</a></li>
	</ul>
	{{/compare}}
</script>