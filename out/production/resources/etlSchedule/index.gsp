<%@ page import="java.text.SimpleDateFormat; com.rxlogix.util.DateUtil; grails.util.Environment; com.rxlogix.Constants; org.apache.commons.lang3.text.WordUtils" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'etlSchedule.label')}"/>
    <title><g:message code="app.etlStatus.title"/></title>
    <g:javascript>
        var etlStatusUrl = "${createLink(controller: 'etlSchedule', action: 'getEtlStatus')}";
        var etlResultUrl = "${createLink(controller: 'etlSchedule', action: 'getEtlScheduleResult')}";
        var refreshInterval = 300000;
    </g:javascript>
    <asset:javascript src="app/pvs/etlStatus.js"/>
    <asset:javascript src="app/pvs/etlResultList.js"/>
    <asset:javascript src="app/pvs/etlResult.js"/>

</head>

<body>
<rx:container title="${message(code: "app.etlStatus.label")}">
    <g:render template="/includes/layout/flashErrorsDivs" bean="${etlScheduleInstance}" var="theInstance"/>
    <g:render template="show"
              model="[etlScheduleInstance: etlScheduleInstance, etlStatus: etlStatus, lastRunDateTime: lastRunDateTime]"/>
    <div class="horizontalRuleFull"></div>

    <div class="m-t-15">
        <table id="etlScheduleResult" class="table table-striped table-curved table-hover etlScheduleResult">
            <thead class="sectionsHeader">
            <tr>
                <th><g:message code="etlMaster.stageKey.label"/></th>
                <th><g:message code="etlMaster.startTime.label"/></th>
                <th><g:message code="etlMaster.finishTime.label"/></th>
                <th width="40%"><g:message code="etlMaster.passStatus.label"/></th>
            </tr>
            </thead>
            <tbody>

            </tbody>
        </table>
    </div>

    <div class="col-md-12 hide">
        <g:link controller="etlCaseTableStatus" action="index" class="btn btn-primary"
                style="margin-left:-15px;"><g:message code="etlcase.Transformation.label"/></g:link>
    </div>
</rx:container>
</body>
</html>


