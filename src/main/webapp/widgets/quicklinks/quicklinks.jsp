<div class="cc-container-widget quicklinks_widget" id="cc-widget-quicklinks">
	<div class="cc-widget-title">
		<h2>Quicklinks</h2>
	</div>
	<!-- MAIN VIEW -->
    <!-- MAIN PANEL: List of links and categories. -->
    <div class="quicklinks_link_list">
        <div id="quicklinks_accordion">__MSG__LOADING__<!-- filled by trimpath, HACK: don't remove 'Loading...', it is here to make IE8 think that the outer widget div is not empty --></div>

        <div id="quicklinks_accordion_template" style="display:none;"><!--
        {var idx = 0}
        {for section in sections}
            {if idx === activeSection}
                <div class="quicklinks_accordion_pane quicklinks_accordion_open" data-sectionid="${idx}">
            {else}
                <div class="quicklinks_accordion_pane" data-sectionid="${idx}">
            {/if}
            <div class="quicklinks_section_label">${section.label}</div>
            <div class="quicklinks_accordion_content{if section.isEditable} quicklinks_section{/if}">
            <ul>

            {var i = 0}

            {for link in section.links}
                {var popText = section.isEditable ? link.url : link.popup_description}
                <li class="link featuredcontent_content featuredcontent_content_medium">
                    <a href="${link.url}"
                        id="quicklinks_${link.id}"
                        target="_blank"
                        class="s3d-widget-links"
                        title="${popText}"
                        data-gatrack="true">${link.name}</a>
                    {if section.isEditable}
                        <div class="quicklinks_edit_buttons">
                            <div class="quicklinks_icon_button quicklinks_delete_icon quicklinks-delete-mylink" data-eltindex="${i}"><span class="s3d-aural-text">__MSG__DELETE_LINK__: ${link.name}</div>
                            <div class="quicklinks_icon_button quicklinks_edit_icon quicklinks-edit-mylink" data-eltindex="${i}"><span class="s3d-aural-text">__MSG__EDIT_LINK__: ${link.name}</div>
                        </div>
                    {/if}
                </li>

            {eval}
                i = i + 1
            {/eval}
            {forelse}
                <div class="quicklinks_no_links_message">__MSG__NO_LINKS_EXIST__</div>
            {/for}
            </ul>
            </div>
        </div>

        {eval}
            idx = idx + 1
        {/eval}
        {/for}
    --></div>


        <!-- ADD/EDIT LINK PANEL: Allows user to add a new link or edit an existing link. -->
        <div class="quicklinks_addedit_link_panel">
            <form id="quicklinks-form" class="quicklinks-link-form s3d-form-field-wrapper" action="" method="post">
                <h2 class="quicklinks_addedit_link_panel_title s3d-addlink-header">__MSG__ADD_LINK__</h2>
                <div class="quicklinks-link-form-element">
                        <label class="elm-label" for="quicklinks-link-title">__MSG__LINK_TITLE__:</label>
                        <input type="text" id="quicklinks-link-title" class="quicklinks-save-link-keydown required" name="quicklinks-link-title" aria-required="true" />
                </div>
                <div class="quicklinks-link-form-element">
                        <label class="elm-label" for="quicklinks-link-url">__MSG__LINK_URL__:</label>
                        <input type="text" id="quicklinks-link-url" name="quicklinks-link-url" aria-required="true" class="appendhttp url required quicklinks-save-link-keydown"/>
                </div>
            </form>

            <div class="quicklinks-button-list">
                <button id="quicklinks-cancel-button" class="s3d-link-button s3d-bold">__MSG__CANCEL__</button>
                <button id="quicklinks-addlink-button" class="s3d-button s3d-overlay-button quicklinks-save-link-click"><span class="s3d-button-inner s3d-button-link-2-state-inner">__MSG__ADD_LINK__</span></button>
                <button id="quicklinks-savelink-button" class="s3d-button s3d-overlay-button quicklinks-save-link-click"><span class="s3d-button-inner s3d-button-link-2-state-inner">__MSG__SAVE__</span></button>
            </div>
        </div>
    </div>

    <div class="cc-widget-footer">
        <button class="s3d-button s3d-overlay-button fl-force-right" id="quicklinks-add-link-mode">__MSG__ADD_A_LINK__</button>
    </div>
</div>
