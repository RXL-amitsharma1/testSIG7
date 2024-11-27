<%@ page import="java.text.SimpleDateFormat" contentType="text/html"%>
<html>
<head>
    <title>Import Alert</title>
</head>

<body>
<h2>Message:</h2>

<div>Import Alert for ${com.rxlogix.util.DateUtil.toUSDateString(logInstance.startTime, timezone)}</div>
<div>
    <ul>
        <li>Start Time: ${new SimpleDateFormat("MM-dd-yyy 'at' HH:mm:ss z").format(logInstance.startTime)}</li>
        <li>End Time: ${new SimpleDateFormat("MM-dd-yyy 'at' HH:mm:ss z").format(logInstance.endTime)}</li>
    <li>Number succeeded: ${logInstance.numSucceeded}</li>
        <li>Number failed: ${logInstance.numFailed}</li>
    </ul>
    <g:if test="${logInstance.numFailed > 0 }">
    <div>Errors:</div>
        <table width="100%">
            <tr>
                <th style="text-align:left;"><g:message code="email.error.table.record" /></th>
                <th style="text-align:left;"><g:message code="email.error.table.slim.id" /></th>
                <th style="text-align:left;"><g:message code="email.error.table.errors" /></th>
            </tr>
            <g:each var="detail" in="${logInstance.details}">
                <tr>
                    <td width="2%" style="text-align:right;">${detail.recNum}</td>
                    <td>${detail.inputIdentifier}</td>
                    <td>${detail.message}</td>
                </tr>
            </g:each>
        </table>
    </div>
    </g:if>
</body>
</html>