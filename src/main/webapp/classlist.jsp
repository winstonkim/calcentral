<%@ taglib tagdir="/WEB-INF/tags" prefix="tags" %>

<tags:head/>

<body class="cc-page-classlist">
    <tags:header/>
    <tags:topnavigation/>
    <div class="cc-container-main" role="main">
        <!-- Page specific HTML -->
        <tags:lhclasslistnavigation/>
        <div class="cc-page-classpage-container cc-container-main-right">

        <h1>Class Page Listings in category: foo</h1>
        <div class="cc-page-classlist-container"><!-- --></div>

        <script id="cc-page-classlist-template" type="text/x-handlebars-template">
        {{#each classes}}
            <div class="cc-container-widget cc-classlist-section">
                <div class="cc-widget-title">
                    <h2><a href="/classpage.jsp?cid={{cid}}">{{dept}} {{catalog_id}}: {{#if title}}{{title}}{{else}}<em>Not available</em>{{/if}}</a></h2>
                </div>
                <div class="cc-widget-main">
                    {{>courseInfo}}
                </div>
            </div>
        {{/each}}
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