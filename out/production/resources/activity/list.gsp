<%@ page contentType="text/html;charset=UTF-8" %>

<head>
    <meta name="layout" content="main"/>
    <title>Activities</title>
    <asset:javascript src="app/pvs/configuration/deliveryOption.js"/>
    <asset:javascript src="app/pvs/activity/activity.js"/>
    <asset:javascript src="app/pvs/dataTablesActionButtons.js"/>
    <asset:javascript src="fuelux/fuelux.js"/>
    <asset:stylesheet src="fuelux.css" />
    <r:require module="export"/>
</head>
<body>
    <rx:container title="${message(code: message(code:"app.label.activities"))}" options="${true}">
        <g:render template="/includes/layout/flashErrorsDivs" bean="${executedConfiguration}" var="theInstance"/>

        <div id="alertIdHolder" data-alert-id="${alertId}" data-alert-type="${type}"></div>
        <table id="activitiesTable" class="row-border hover" width="100%">
            <thead>
                <tr>
                    <th class=""><g:message code="app.label.activity.type" /></th>
                    <th width="50%"><g:message code="app.label.description" /></th>
                    <th><g:message code="app.label.performed.by" /></th>
                    <th><g:message code="app.label.timestamp" /></th>
                </tr>
            </thead>
        </table>
    </rx:container>
</body>
