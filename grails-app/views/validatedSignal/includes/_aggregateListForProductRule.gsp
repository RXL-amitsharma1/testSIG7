<%@ page import="com.rxlogix.Constants; com.rxlogix.util.DateUtil;" %>
<!doctype html>
<html>
<head>
    <g:set var="userService" bean="userService"/>
</head>

<body>
<table id="aggTable" class="table table-striped table-curved table-hover">
    <thead>
    <tr>
        <th>Alert Name</th>
        <th>Description</th>
        <th>Date Created</th>
        <th>Owner</th>
    </tr>
    </thead>
    <tbody>
    <g:if test="${aggData}">
        <%
            def userTimeZone = userService.getCurrentUserPreference().timeZone
        %>
        <g:each var="entry" in="${aggData}">
            <tr>
                <td>${entry.name}</td>
                <td>${entry.description ?: '-'}</td>
                <td>${com.rxlogix.util.DateUtil.toDateStringWithTimeInAmPmFormat(entry.dateCreated, userTimeZone)}</td>
                <td>${entry.owner}</td>
            </tr>
        </g:each>
    </g:if>
    <g:else>
        <tr>
            <td colspan="4">No data available.</td>
        </tr>
    </g:else>
    </tbody>

</table>
</body>
</html>
