<%@ taglib tagdir="/WEB-INF/tags" prefix="tags" %>

<tags:head/>

<body class="cc-page-classpage">
	<tags:header/>
	<tags:topnavigation/>
	<div class="cc-container-main cc-container-main-full" role="main">
		<!-- Page specific HTML -->
		<div class="cc-page-classpage-container"><!-- --></div>

        <ul style="position:relative;right:-20px;">
            <li><a href="/classpage.jsp?cid=2012D16442">One</a></li>
            <li><a href="/classpage.jsp?cid=2012D16487">Two</a></li>
            <li><a href="/classpage.jsp?cid=2012D26287">Three</a></li>
            <li><a href="/classpage.jsp?cid=2012D31021">Four</a></li>
            <li><a href="/classpage.jsp?cid=2012D32233">Five</a></li>
            <li><a href="/classpage.jsp?cid=2012D74058">Six</a></li>
        </ul>

		<script id="cc-page-classpage-template" type="text/x-handlebars-template">
            <div class="cc-container-widget cc-page-classpage-header">
                <div class="cc-widget-title">
                    <h2>UC Berkeley Course</h2>
                </div>
                <div class="cc-widget-main">
                    {{>header}}
                </div>
            </div>
			<div class="cc-container-widget cc-page-classpage-description">
				<div class="cc-widget-title">
					<h2>Course Catalog Description</h2>
				</div>
				<div class="cc-widget-main">
					{{>description}}
				</div>
			</div>
			<div class="cc-container-widget cc-page-classpage-courseinfo">
				<div class="cc-widget-title">
					<h2>Course Info</h2>
				</div>
				<div class="cc-widget-main">
					{{>courseInfo}}
				</div>
			</div>
			<div class="cc-container-widget cc-page-classpage-instructor">
				<div class="cc-widget-title">
					<h2>Instructor</h2>
				</div>
				<div class="cc-widget-main">
					{{>instructor}}
				</div>
			</div>
			<div class="cc-container-widget cc-page-classpage-sections">
				<div class="cc-widget-title">
					<h2>Lecture &amp; Section Details</h2>
				</div>
				<div class="cc-widget-main">
					{{>sections}}
				</div>
			</div>
		</script>
		<script id="cc-page-classpage-header-template" type="text/x-handlebars-template">
            <h2>{{classPage.classtitle}}</h2>
            <h3>{{classPage.courseinfo.department}} {{classPage.courseinfo.coursenum}} : {{classPage.courseinfo.term}} {{classPage.courseinfo.year}}</h3>
		</script>

		<script id="cc-page-classpage-description-template" type="text/x-handlebars-template">
			{{classPage.description}}
		</script>

		<script id="cc-page-classpage-courseinfo-template" type="text/x-handlebars-template">
			{{#with classPage.courseinfo}}
			<ul class="cc-page-classpage-list">
				<li><span>Format:</span><span>{{#if format}}{{format}}{{else}}<em>Not available</em>{{/if}}</span></li>
				<li><span>Units:</span><span>{{#if units}}{{units}}{{else}}<em>Not available</em>{{/if}}</span></li>
				<li><span>Semesters offered:</span><span>{{#if semesters_offered}}{{semesters_offered}}{{else}}<em>Not available</em>{{/if}}</span></li>
				<li><span>Requirements:</span><span>{{#if requirements}}{{requirements}}{{else}}<em>Not available</em>{{/if}}</span></li>
				<li><span>Grading:</span><span>{{#if grading}}{{grading}}{{else}}<em>Not available</em>{{/if}}</span></li>
				<li><span>Prerequisites:</span><span>{{#if prereqs}}{{prereqs}}{{else}}<em>Not available</em>{{/if}}</span></li>
			</ul>
			{{/with}}
		</script>

		<script id="cc-page-classpage-instructor-template" type="text/x-handlebars-template">
			<ul class="cc-page-classpage-instructor-item">

				{{#each classPage.instructors}}
					<li>
						{{#if img}}
							<img class="cc-page-classpage-instructor-profile" src="{{img}}" />
						{{/if}}
						<div class="cc-page-classpage-instructor-heading">
						{{#if name}}
							<a href="https://calnet.berkeley.edu/directory/details.pl?uid={{id}}">{{name}}</a>
						{{else}}
							<em>Instructor name is not available</em>
						{{/if}}
						{{#if title}}
							<div>{{title}}</div>
						{{/if}}
						</div>
						<ul class="cc-page-classpage-list">
							{{#if url}}<li><span>Website:</span><span><a href="{{url}}">{{url}}</a></span></li>{{/if}}
							{{#if phone}}<li><span>Phone #:</span><span>{{phone}}</span></li>{{/if}}
							{{#if office}}<li><span>Office:</span><span>{{office}}</span></li>{{/if}}
						</ul>
					</li>
				{{/each}}
			</ul>
		</script>

		<script id="cc-page-classpage-sections-template" type="text/x-handlebars-template">
        {{#if classPage.sections}}
        <span id="classpages_showhideall">
        	<button id="classpages_expandall" class="s3d-link-button">Expand all</button> |
        	<button id="classpages_collapseall" class="s3d-link-button">Collapse all</button>
        </span>
        <table id="classpages_section_results">
            <thead>
                <tr>
                    <th class="classpages_sections_col1">Class Meeting</th>
                    <th>CCN</th>
                    <th>Instructor</th>
                    <th>Enrolled</th>
                    <th>Waitlist</th>
                    <th class="classpages_sections_timecol">Time</th>
                    <th class="classpages_sections_loccol">Location</th>
                </tr>
            </thead>
            <tbody>
                {{#each classPage.sections}}
                <tr class="classpages_classrow">
                    <td>
                        <a href="#"><div class="classpages_sections_arrow" id="sectionarrow-{{ccn}}"></div></a>
                        <span class="classpages_section_title_warrow">
                            <strong>{{section}}</strong></span>
                        </span>
                    </td>

                    <td>
                        {{ccn}}
                    </td>

                    <td class="classpages_sections_instrnames">
	                    {{#each instructors}}
	                        {{#if id}}
	                            <a href="/~{{id}}">{{name}}</a>
	                        {{else}}
	                            {{name}}
	                        {{/if}}
	                    {{/each}}
                    </td>

                    <%-- Mustache doesnt allow tests like 'if this AND that' so we double nest the tests --%>
                    <td>{{#if enrolled_cur}}
	                    	{{#if enrolled_max}}
	                    		{{enrolled_cur}}/{{enrolled_max}}
	                    	{{/if}}
                    	{{/if}}
                    </td>

                    <td>
                    	{{#if waitlist}}
                    		Y
                    	{{/if}}
                    </td>

                    <td class="classpages_sections_timecol">
                        {{#if time }}
                            {{time}}
                        {{/if}}
                    </td>

                    <td class="classpages_sections_timecol">
                        {{#if location }}
                            {{location}}
                        {{/if}}
                    </td>
                </tr>

                <tr class="classpages_metadata">
                    <td colspan="5">
                        <table class="classpages_sections_table_inner">
                            {{#if note}}
                            <tr>
                                <td class="classpages_sections_col1 classpages_table_label">
                                    Note:
                                </td>

                                <td>
                                    {{note}}
                                </td>
                            </tr>
                            {{/if}}

                            <tr>
                                <td class="classpages_sections_col1 classpages_table_label">
                                    Final exam:
                                </td>

                                <td>
                                    {{#if final_datetime}}{{final_datetime}}
                                        {{#if final_coords}}
                                            <a href="http://maps.google.com/maps?daddr={{final_coords}}&l=en&dirflg=w&t=m&z=17" target="_blank">in {{final_location}}</a>
                                        {{else}}
                                            <em>Not available</em>
                                        {{/if}}
                                    {{else}}
                                            <em>Not available, location unknown</em>
                                    {{/if}}

                                    {{#if final_note}}
                                        <a href="#" class="show_note" data-note="{{final_note}}">[Note]</a>
                                    {{/if}}
                                </td>
                            </tr>

                            {{#if midterm_datetime}}
                            <tr>
                                <td class="classpages_sections_col1 classpages_table_label">
                                    Mid-term:
                                </td>

                                <td>
                                    {{#if midterm_datetime}}{{midterm_datetime}}
                                        {{#if midterm_coords}}
                                            <a href="http://maps.google.com/maps?daddr={{midterm_coords}}&l=en&dirflg=w&t=m&z=17" target="_blank">in {{midterm_location}}</a>
                                        {{/if}}
                                    {{else}}
                                        <em>Not available</em>
                                    {{/if}}
                                    {{#if midterm_note}}
                                        <a href="#" class="show_note" data-note="{{midterm_note}}">[Note]</a>
                                    {{/if}}
                                </td>
                            </tr>
                            {{/if}}

                            {{#if restrictions}}
                            <tr>
                                <td class="classpages_sections_col1 classpages_table_label">
                                    Restrictions:
                                </td>

                                <td>
                                    {{restrictions}}
                                </td>
                            </tr>
                            {{/if}}

                        </table>
                    </td>

                    <td colspan="2">
                        {{#if coords}}
                            <a href="http://maps.google.com/maps?daddr={{coords}}&l=en&dirflg=w&t=m&z=17" target="_blank"><img src="http://maps.googleapis.com/maps/api/staticmap?center={{coords}}&zoom=16&size=200x200&maptype=roadmap&markers=color:blue%7C{{coords}}&sensor=false" /></a>
                        {{else}}
                            <img src="/img/myb/classpages_map_not_available.png" />
                        {{/if}}
                    </td>
                </tr>

                {{/each}}
            </tbody>
        {{else}}
            <em>Not available</em>
        {{/if}}
		</script>

		<!-- END Page specific HTML -->
		<br class="clearfix" />
	</div>
	<tags:footer/>
</body>
</html>