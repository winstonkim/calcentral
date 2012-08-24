<nav class="cc-topnavigation">
	<ul>
		<li class="cc-left"><a href="/colleges-and-schools.jsp">Colleges and Schools</a></li>
		<li class="cc-right"><a href="/secure/dashboard">Sign in</a></li>
	</ul>
</nav>

<script id="cc-topnavigation-template" type="text/x-handlebars-template" style="display:none;">
	<ul>
		<li class="cc-left"><a href="/colleges-and-schools.jsp">Colleges and Schools</a></li>
	{{#if anon}}
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
	{{else}}
		<li class="cc-right"><a href="/secure/dashboard">Sign in</a></li>
	{{/if}}
	</ul>
</script>