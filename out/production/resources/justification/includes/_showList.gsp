<div class="rxmain-container rxmain-container-top">
    <div class="rxmain-container-inner">
        <div class="rxmain-container-row rxmain-container-header">
            <label class="rxmain-container-header-label click">
                <g:message code="app.label.add.justification.list" default="Justification List"/>
            </label>
        </div>

        <div class="rxmain-container-content">
            <div class="row">
                <div class="col-md-12">
                    <table id="justificationListTable" class="row-border hover" width="100%">
                        <thead>
                        <tr>
                            <th><g:message code="app.justification.name" default="Name"/></th>
                            <th><g:message code="app.justification.text" default="Justification"/></th>
                            <th><g:message code="app.justification.feature" default="Features"/></th>
                            <th><g:message code="app.justification.lastUpdated" default="Last Updated Date"/></th>
                            <th><g:message code="app.justification.modifiedBy" default="Modified By"/></th>
                            <sec:ifAnyGranted roles="ROLE_CONFIGURATION_CRUD">
                                <th><g:message code="app.justification.action" default="Actions"/></th>
                            </sec:ifAnyGranted>

                        </tr>
                        </thead>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>