<div class="cc-container-main-left"><!-- --></div>
<script id="cc-container-main-left-template" type="text/x-handlebars-template">
	<section class="cc-entity">

		<div class="cc-entity-name cc-lefthandnavigation">
			<strong><a href="/classlist.jsp?college={{college.slug}}" {{#compare college_url pathname}}class="cc-lefthandnavigation-item-selected"{{/compare}}>{{college.title}}</a></strong>
		</div>

	</section>
	<nav class="cc-lefthandnavigation">
		<ul>
			{{#each pages}}
				<li><a href="{{url}}"{{#compare url ../pathname}} class="cc-lefthandnavigation-item-selected"{{/compare}}>{{title}}</a></li>
			{{/each}}

		</ul>
	</nav>
</script>
