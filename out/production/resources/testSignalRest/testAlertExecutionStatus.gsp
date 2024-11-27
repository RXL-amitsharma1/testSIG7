<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.rxlogix.enums.ReportFormat" %>
<html>
<head>
    <title>Test Case Execution Status</title>
    <meta name="layout" content="main">
    <link type="text/css" href="//gyrocode.github.io/jquery-datatables-checkboxes/1.2.12/css/dataTables.checkboxes.css"
          rel="stylesheet"/>
    <script type="text/javascript"
            src="//gyrocode.github.io/jquery-datatables-checkboxes/1.2.12/js/dataTables.checkboxes.min.js"></script>
    <asset:javascript src="highcharts.js"/>
    <asset:javascript src="testSignal/testExecutionStatus.js"/>
    <asset:javascript src="app/pvs/configuration/executionStatus.js"/>
    <asset:javascript src="app/pvs/dataTablesActionButtons.js"/>
    <asset:javascript src="fuelux/fuelux.js"/>
    <asset:stylesheet src="executionStatus.css"/>
    <asset:javascript src="fuelux/fuelux.js"/>
    <asset:stylesheet src="fuelux.css"/>
    <asset:stylesheet src="testSignalUI.css"/>
    <g:javascript>
    var executionStatusUrl ="${createLink(controller: "testSignalRest", action: 'testAlertExecutionStatus')}";
    var executionErrorUrl = "${createLink(controller: 'Configuration', action: 'signalExecutionError')}";
    var passedCountMap = "${passedCountMap}";
    var failedCountMap = "${failedCountMap}";
    </g:javascript>
</head>

<body>
<rx:container title="Test Case Analytics" options="${false}" bean="${error}">

    <a class="btn btn-default pv-btn-grey" tabindex="0" accesskey="c"
       href="${createLink(controller: 'testSignalRest', action: 'exportReport', params: [outputFormat: ReportFormat.PDF])}">Export Report</a>

    <div class="card-container">
        <div class="card pass-container">
            <div class="card-header">
                <h1>${countOfPassedTestCases}</h1>
            </div>

            <div class="container">
                <h4><b>Passed Test Cases</b></h4>
            </div>
        </div>

        <div class="card fail-container">
            <div class="card-header">
                <h1>${countOfTotatTestCases - countOfPassedTestCases}</h1>
            </div>

            <div class="container">
                <h4><b>Failed Test Cases</b></h4>
            </div>
        </div>

        <div id="chart-container" style="width: 600px; height: 300px; margin: 0 auto"></div>
    </div>

</rx:container>


<rx:container title="Test Case Execution Status" options="${true}" bean="${error}">

    <table id="testCaseExecutionStatus" class="row-border hover" width="100%">
        <thead>
        <tr>
            <th><g:message code="app.label.name"/></th>
            <th>Alert Type</th>
            <th>Product</th>
            <th><g:message code="app.label.frequency"/></th>
            <th><g:message code="app.label.runDate"/></th>
            <th><g:message code="app.label.runDuration"/></th>
            <th><g:message code="app.label.owner"/></th>
            <th><g:message code="app.label.executionStatus"/></th>
            <th>PEC Count</th>
        </tr>
        </thead>
    </table>
</rx:container>

<input type="submit" class="btn primaryButton btn-primary" value="Delete Test Alerts"
       id="delete-test-alerts">
</body>
</html>