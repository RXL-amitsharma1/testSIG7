<%@ page contentType="text/html" %>
<html>
<head>
    <title>ALERT EXECUTION SKIPPED</title>
</head>
<style>
table, th, td {
    border: 1px solid black;
    border-collapse: collapse;
}
</style>

<body>
<g:if test="${pausedDueToPvrIssue}">
    <h4>The following alerts are disabled due to PVR inaccessibility.</h4>
</g:if>
<g:else>
    <h4>The following alerts are disabled due to ETL issue.</h4>
</g:else>


<div>

    <table style="width:100%">
        <tr>
            <th style="text-align:left;">Alert Type</th>
            <th style="text-align:left;">Alert Name</th>
            <th style="text-align:left;">Date Range</th>
        </tr>
        <g:each var="missedAlertInstance" in="${missedAlertInstances}">
            <tr>
                <td>${missedAlertInstance.alertType}</td>
                <td>${missedAlertInstance.alertName}</td>
                <td>${missedAlertInstance.dateRangeInformation}</td>
            </tr>
        </g:each>
</div>
</body>
</html>