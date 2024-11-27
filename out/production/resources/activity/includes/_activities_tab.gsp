<rx:container>
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
    <g:render template="includes/export_panel"
              model="[controller:'activity',action:'exportActivitiesReport',
                      extraParams:[id:id]]"/>
</rx:container>
