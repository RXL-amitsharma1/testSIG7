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
The following System Health Checks are failed.
<br>
<br>
<div>
    <table style="width:100%">
        <tr>
            <th style="text-align:left;">Pre-Check</th>
            <th style="text-align:left;">Information</th>
        </tr>
        <g:each var="data" in="${preCheckData}">
            <tr>
                <td>${data?.name?.encodeAsRaw()}</td>
                <td>${data?.reason?.encodeAsRaw()}</td>
            </tr>
        </g:each>
    </table>
    <br>Note: Please take appropriate action for the successful execution of the alert.
    <br>
    <br>
</div>
</body>
</html>