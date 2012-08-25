<nav class="cc-topnavigation"><!-- --></nav>

<script id="cc-topnavigation-template" type="text/x-handlebars-template" style="display:none;">
	<ul>
		<li class="cc-left"><a href="/colleges-and-schools.jsp">Colleges and Schools</a></li>
	{{#if loggedIn}}
		<ul>
			<li class="cc-right">
				<a href="/" aria-haspopup="true">{{firstName}} {{lastName}}</a>
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