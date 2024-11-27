<%@ page import="com.rxlogix.Constants" %>
<div class="row">
    <div class="panel-heading pv-sec-heading m-b-10">
        <div class="row">
            <div class="col-md-7">
                <span class="panel-title cell-break">${name}</span>
            </div>
        </div>
    </div>
</div>

<table id="archivedAlertsTable" class="dataTable" width="100%">
    <thead>
    <tr>
        <th><g:message code="app.label.alert.name"/></th>
        <th><g:message code="app.label.version"/></th>
        <th><g:message
                code="${type == Constants.AlertConfigType.EVDAS_ALERT  ? 'app.label.evdas.details.column.substance' : 'app.label.product'}"/></th>
        <th><g:message code="app.label.description"/></th>
        <th><g:message
                code="${type == Constants.AlertConfigType.EVDAS_ALERT || type == Constants.AlertConfigType.AGGREGATE_CASE_ALERT ? 'app.label.pec.count' : 'app.label.case.count'}"/></th>
        <th><g:message
                code="${type == Constants.AlertConfigType.EVDAS_ALERT || type == Constants.AlertConfigType.AGGREGATE_CASE_ALERT ? 'app.label.review.pec' : 'app.label.review.cases'}"/></th>
        <th><g:message code="app.label.DateRange"/></th>
        <th><g:message code="app.alert.last.modified"/></th>
    </tr>
    </thead>
</table>