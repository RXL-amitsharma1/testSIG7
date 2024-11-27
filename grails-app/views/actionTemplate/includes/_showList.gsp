<div class="rxmain-container rxmain-container-top">
    <div class="rxmain-container-inner">
        <div class="rxmain-container-row rxmain-container-header">
            <label class="rxmain-container-header-label click">
                <g:message code="app.label.action.template.list" default="Action Template List"/>
            </label>
        </div>

        <div class="rxmain-container-content">
            <div class="row">
                <div class="col-md-12">
                    <table id="actionTemplateListTable" class="row-border hover" width="100%">
                        <thead>
                        <tr>
                            <th><g:message code="app.action.template.name" default="Name"/></th>
                            <th><g:message code="app.action.template.description" default="Description"/></th>
                            <th><g:message code="app.action.template.action.properties"
                                           default="Action Properties"/></th>
                            <th><g:message code="app.action.template.lastUpdated" default="Last Updated Date"/></th>
                            <th><g:message code="app.action.template.modifiedBy" default="Modified By"/></th>
                            <sec:ifAnyGranted roles="ROLE_CONFIGURATION_CRUD">
                                <th class="pvi-col-xs">Actions</th>
                            </sec:ifAnyGranted>

                        </tr>
                        </thead>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>