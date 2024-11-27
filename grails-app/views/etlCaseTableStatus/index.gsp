<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <title><g:message code="etlcase.Transformation.label"/></title>
</head>

<body>

<rx:container title="${message(code: "etlcase.Transformation.label")}">

    <g:link controller="etlSchedule" action="index"><< <g:message code="app.label.etlStatus" /></g:link>

    <h1></h1>

    <div class="table-responsive curvedBox">
        <table class="table table-striped table-bordered">
            <thead class="sectionsHeader">
            <tr>
                <th><g:message code="etlcase.tableName.label" /></th>
                <th><g:message code="etlcase.stageStartTime.label" /></th>
                <th><g:message code="etlcase.stageEndTime.label" /></th>
                <th><g:message code="etlcase.transformationStartTime.label" /></th>
                <th><g:message code="etlcase.transformationEndTime.label" /></th>
            </tr>
            </thead>
            <tbody>
            <g:if test="${etlCaseTableStatusInstanceTotal > 0}">
                <g:each in="${etlCaseTableStatusInstanceList}" var="etlCaseTableStatusInstance">
                    <tr>
                        <td>${etlCaseTableStatusInstance?.tableName}</td>
                        <td><g:render template="/includes/widgets/dateDisplayWithTimezone"
                                      model="[date: etlCaseTableStatusInstance?.stageEndTime]"/></td>
                        <td><g:render template="/includes/widgets/dateDisplayWithTimezone"
                                      model="[date: etlCaseTableStatusInstance?.transformationStartTime]"/></td>
                        <td><g:render template="/includes/widgets/dateDisplayWithTimezone"
                                      model="[date: etlCaseTableStatusInstance?.stageStartTime]"/></td>
                        <td><g:render template="/includes/widgets/dateDisplayWithTimezone"
                                      model="[date: etlCaseTableStatusInstance?.transformationEndTime]"/></td>
                    </tr>
                </g:each>
            </g:if>
            <g:else>
                <tr>
                    <td colspan="6">None</td>
                </tr>
            </g:else>
            </tbody>
        </table>
    </div>
    <g:render template="/includes/widgets/pagination"
              bean="${etlCaseTableStatusInstanceTotal}" var="theInstanceTotal"/>
</rx:container>


</body>
</html>
