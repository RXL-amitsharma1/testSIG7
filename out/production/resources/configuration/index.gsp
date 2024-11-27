<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.ReportLibrary.title"/></title>
    <asset:javascript src="app/pvs/configuration/configuration.js"/>
    <asset:javascript src="app/pvs/dataTablesActionButtons.js"/>
    <asset:javascript src="yadcf/jquery.dataTables.yadcf.js"/>
    <asset:stylesheet src="jquery-ui/jquery-ui.css"/>
    <asset:stylesheet src="yadcf/jquery.dataTables.yadcf.css"/>
    <asset:javascript src="app/pvs/bootbox.min.js"/>
    <g:javascript>
        var CONFIGURATION = {
                listUrl: "${createLink(controller: 'configurationRest', action: 'index')}",
                deleteUrl: "${createLink(controller: 'configuration', action: 'delete')}",
                editUrl: "${createLink(controller: 'configuration', action: 'edit')}",
                viewUrl: "${createLink(controller: 'configuration', action: 'view')}",
                copyUrl: "${createLink(controller: 'configuration', action: 'copy')}",
                runUrl:"${createLink(controller: 'configuration', action: 'runOnce')}",
                sca_list_url: "${createLink(controller: 'singleCaseAlert', action: 'index')}",
                sca_edit_url: "${createLink(controller: 'singleCaseAlert', action: 'edit')}",
                sca_view_url: "${createLink(controller: 'singleCaseAlert', action: 'view')}",
                sca_copy_url: "${createLink(controller: 'singleCaseAlert', action: 'copy')}",
                aga_list_url: "${createLink(controller: 'aggregateCaseAlert', action: 'list')}",
                aga_edit_url: "${createLink(controller: 'aggregateCaseAlert', action: 'edit')}",
                aga_view_url: "${createLink(controller: 'aggregateCaseAlert', action: 'view')}",
                aga_copy_url: "${createLink(controller: 'aggregateCaseAlert', action: 'copy')}",
                adha_list_url: "${createLink(controller: 'adHocAlert', action: 'list')}",
                adha_edit_url: "${createLink(controller: 'adHocAlert', action: 'edit')}",
                adha_view_url: "${createLink(controller: 'adHocAlert', action: 'view')}",
                adha_copy_url: "${createLink(controller: 'adHocAlert', action: 'copy')}",
                evdas_run_url: "${createLink(controller: 'evdasAlert', action: 'runOnce')}",
                evdas_edit_url: "${createLink(controller: 'evdasAlert', action: 'edit')}",
                evdas_view_url: "${createLink(controller: 'evdasAlert', action: 'view')}",
                evdas_copy_url: "${createLink(controller: 'evdasAlert', action: 'copy')}",
                evdas_delete_url: "${createLink(controller: 'evdasAlert', action: 'delete')}",
                literature_edit_url: "${createLink(controller: 'literatureAlert', action: 'edit')}",
                literature_view_url: "${createLink(controller: 'literatureAlert', action: 'view')}",
                literature_copy_url: "${createLink(controller: 'literatureAlert', action: 'copy')}",
                literature_delete_url: "${createLink(controller: 'literatureAlert', action: 'delete')}",
                literature_run_url: "${createLink(controller: 'literatureAlert', action: 'runOnce')}"
        }
    </g:javascript>
    <style>
/*this css needs to be handled in common css files as this issue can appear on any pages where more than 2 dialogue boxes are used*/

</style>

</head>

<body>
<rx:container title="${message(code: message(code: "app.ReportLibrary.label"))}" options="${true}" filters="${true}">
    <g:render template="/includes/layout/flashErrorsDivs"/>
        <div class="m-b-15">
            <sec:ifAnyGranted roles="ROLE_SINGLE_CASE_CONFIGURATION">
                <label class="no-bold add-cursor radio-container"><input class="m-r-5 viewAlertRadio"
                                                                         type="radio" name="relatedResults"
                                                                         value="Single Case Alert"
                                                                         /><g:message
                        code="app.new.single.case.alert"/></label>
            </sec:ifAnyGranted>

            <sec:ifAnyGranted roles="ROLE_AGGREGATE_CASE_CONFIGURATION, ROLE_FAERS_CONFIGURATION, ROLE_VAERS_CONFIGURATION, ROLE_VIGIBASE_CONFIGURATION,ROLE_JADER_CONFIGURATION">
                <label class="no-bold add-cursor radio-container"><input class="m-r-5 viewAlertRadio"
                                                                         type="radio" name="relatedResults"
                                                                         value="Aggregate Case Alert"
                                                                         /><g:message
                        code="app.new.aggregate.case.alert"/></label>
            </sec:ifAnyGranted>

            <sec:ifAnyGranted roles="ROLE_EVDAS_CASE_CONFIGURATION">
                <label class="no-bold add-cursor radio-container"><input class="m-r-5 viewAlertRadio"
                                                                         type="radio" name="relatedResults"
                                                                         value="EVDAS Alert"
                                                                         /><g:message
                        code="app.label.evdas.configuration"/></label>
            </sec:ifAnyGranted>

            <sec:ifAnyGranted roles="ROLE_LITERATURE_CASE_CONFIGURATION">
                <label class="no-bold add-cursor radio-container"><input class="m-r-5 viewAlertRadio"
                                                                         type="radio" name="relatedResults"
                                                                         value="Literature Search Alert"
                                                                         /><g:message
                        code="app.new.literature.search.alert"/></label>
            </sec:ifAnyGranted>

        </div>

            <table id="rxTableConfiguration" class="row-border hover" width="100%">
                <thead>
                <tr>
                    <th class="nameColumn">
                        <div class="th-label"><g:message code="app.label.name"/></div></th>
                    <th class="reportDescriptionColumn">
                        <div class="th-label">
                            <g:message code="app.label.description"/>
                        </div></th>
                    <th>
                        <div class="th-label"><g:message code="app.label.runTimes"/></div></th>
                    <th>
                        <div class="th-label">
                            <g:message code="app.label.dateCreated"/>
                        </div>
                    </th>
                    <th>
                        <div class="th-label">
                            <g:message code="app.label.dateModified"/>
                        </div>
                    </th>
                    <th>
                        <div class="th-label">
                            <g:message code="app.label.owner"/>
                        </div>
                    </th>
                    <th>
                        <div class="th-label">
                            <g:message code="app.label.action"/>
                        </div>
                    </th>
                </tr>
                </thead>
            </table>
        <g:form controller="${controller}" method="delete">
            <g:render template="/includes/widgets/deleteRecord"/>
        </g:form>
</rx:container>

</body>
