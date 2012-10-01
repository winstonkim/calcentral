<%@ taglib tagdir="/WEB-INF/tags" prefix="tags" %>

<tags:head/>

<body class="cc-page-classlist">
    <tags:header/>
    <tags:topnavigation/>
    <div class="cc-container-main" role="main">
        <!-- Page specific HTML -->
        <tags:lhclasslistnavigation/>
        <div class="cc-page-classpage-container cc-container-main-right">

        <div class="cc-page-classlist-container"><!-- --></div>

        <script id="cc-page-classlist-template" type="text/x-handlebars-template">
        <nav class="cc-page-classlist-breadcrumbs">
            <a href="/colleges-and-schools.jsp">All Colleges &amp; Schools</a> :: {{#if college}}<a href="/classlist.jsp?college={{college.id}}">{{college.title}}</a> ::{{/if}}
        </nav>

        <h1 class="cc-page-classlist-title">{{#if department_name}}{{department_name}}{{else}}{{college.title}}{{/if}}</h1>
        {{#if classes}}
            {{#each classes}}
                <div class="cc-container-widget cc-classlist-section">
                    <div class="cc-widget-title">
                        <h2><a href="/classpage.jsp?cid={{classId}}">{{department}} {{catalogid}}: {{#if classtitle}}{{classtitle}}{{else}}<em>Not available</em>{{/if}}</a></h2>
                    </div>
                    <div class="cc-widget-main">
                        {{>courseInfo}}
                    </div>
                </div>
            {{/each}}
        {{else}}
            <p>No classes found.</p>
        {{/if}}
        </script>

        <script id="cc-page-classlist-courseinfo-template" type="text/x-handlebars-template">
            <p>{{#if description}}{{description}}{{else}}<em>Not available</em>{{/if}}</p>
        </script>

        </div>
        <!-- END Page specific HTML -->
        <br class="clearfix" />
    </div>
    <tags:footer/>
</body>
</html>