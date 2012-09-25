<nav class="cc-topnavigation"><!-- --></nav>

<script id="cc-topnavigation-template" type="text/x-handlebars-template">
	<ul>
		<li class="cc-left"><a href="/secure/dashboard" {{#compare pathName "/secure/dashboard"}} class="cc-topnavigation-selected"{{/compare}}>My Dashboard</a></li>
		<li class="cc-left"><a href="#">My Campus</a></li>
		<ul>
			<li class="cc-right">
				<input type="text" placeholder="Search..." />
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
</script>