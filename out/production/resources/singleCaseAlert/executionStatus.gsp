<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.ExecutionStatus.title"/></title>
    <asset:javascript src="app/pvs/configuration/executionStatus.js"/>
    <asset:javascript src="app/pvs/dataTablesActionButtons.js"/>
    <asset:javascript src="app/pvs/bootbox.min.js"/>
    <asset:javascript src="fuelux/fuelux.js"/>
    <asset:stylesheet src="fuelux.css"/>
    <asset:stylesheet src="executionStatus.css"/>
    <g:javascript>
        var executionStatusUrl = "${createLink(controller: 'ConfigurationRest', action: 'executionStatus')}";
        var executionAllUrl = "${createLink(controller: 'ConfigurationRest', action: 'listAllResults')}";
        var ShowConfigUrl = "${createLink(controller: 'Configuration', action: 'viewConfig')}";
        var listExecutionStatusUrl = "${createLink(controller: 'Configuration', action: 'executionStatus')}";
        var listAllResultsUrl = "${createLink(controller: 'Configuration', action: 'listAllResults')}";
    </g:javascript>
</head>

<body>
<rx:container title="${message(code: "app.ExecutionStatus.title")}" options="${true}" bean="${error}">

    <g:render template="/includes/layout/flashErrorsDivs"/>

    <div class="fuelux outside row">
        <div class="datepicker col-xs-6" id="resultDatepickerFrom">
            <div class="row form-inline">
                <div class="col-xs-3 datePickerMargin labelMargin ">
                    <label class="no-bold"><g:message code="app.dateFilter.from"/> </label>
                </div>
                <div class="col-xs-9 datePickerMargin">
                    <div class="input-group">
                        <input placeholder="${message(code: 'select.start.date')}" class="form-control input-sm" id="myResultDatepickerFrom" type="text"/>
                        <g:render template="/includes/widgets/datePickerTemplate"/>
                    </div>
                 </div>
             </div>
        </div>
        <div class="datepicker col-xs-6" id="resultDatepickerTo">
            <div class="row form-inline">
                <div class="col-xs-2 datePickerMargin labelMargin ">
                    <label class="no-bold"><g:message code="app.dateFilter.to"/> </label>
                </div>
                <div class="col-xs-9 datePickerMargin">
                    <div class="input-group">
                        <input placeholder="${message(code:"select.end.date")}" class="form-control input-sm" id="myResultDatepickerTo" type="text"/>
                        <g:render template="/includes/widgets/datePickerTemplate"/>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <table id="rxTableReportsExecutionStatus" class="row-border hover" width="100%">
        <thead>
        <tr>
            <th><g:message code="app.label.name"/></th>
            <th><g:message code="app.label.version"/></th>
            <th><g:message code="app.label.frequency"/></th>
            <th><g:message code="app.label.runDate"/></th>
            <th><g:message code="app.label.runDuration"/></th>
            <th><g:message code="app.label.owner"/></th>
            <th><g:message code="app.label.executionStatus"/></th>
            <th><g:message code="app.label.sharedWith"/></th>
        </tr>
        </thead>
    </table>
    <g:hiddenField name="relatedResults" id="relatedResults" value="${related}"/>
    <g:hiddenField name="isAdmin" id="isAdmin" value="${isAdmin}"/>
</rx:container>
</body>