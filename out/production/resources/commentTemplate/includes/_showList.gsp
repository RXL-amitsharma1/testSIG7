<div class="rxmain-container rxmain-container-top">
    <div class="rxmain-container-inner">
        <div class="rxmain-container-row rxmain-container-header">
            <label class="rxmain-container-header-label click">
                <g:message code="app.label.comment.template.list" default="Comment Template List"/>
            </label>
        </div>

        <div class="rxmain-container-content">
            <div class="row">
                <div class="col-md-12">
                    <table id="commentTemplateListTable" class="row-border hover" width="100%">
                        <thead>
                        <tr>
                            <th><g:message code="app.comment.template.name" default="Name"/></th>
                            <th><g:message code="app.comment.template.content"
                                           default="Template Content"/></th>
                            <th><g:message code="app.comment.template.dateModified" default="Date Modified"/></th>
                            <th><g:message code="app.comment.template.lastUpdated" default="Last Updated By"/></th>
                            <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_CONFIGURATION_CRUD">
                                <th class="pvi-col-xs sorting_disabled">Actions</th>
                            </sec:ifAnyGranted>
                        </tr>
                        </thead>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>