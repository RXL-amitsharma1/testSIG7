<%@ page contentType="text/html" %>
<html>
<head>
    <title>Action Due Reminder</title>
</head>

<body>
<h4>Message:</h4>

<div><g:message code='app.action.due.reminder.msg'/></div>

<div>
    <ul>
<g:if test="${alertInstance.topic}">
        <li>Topic Name: ${alertInstance.topic}</li>
</g:if>
<g:if test="${alertInstance.detectedBy}">
        <li>Detected by: ${alertInstance.detectedBy}</li>
</g:if>
<g:if test="${alertInstance.detectedDate}">
        <li>Detected date: ${com.rxlogix.util.DateUtil.toUSDateString(alertInstance.detectedDate,
                timezone)}</li>
</g:if>
<g:if test="${alertInstance.getClass().getSimpleName() != 'AdHocAlert'}">
     <g:if test="${alertInstance.workflowState}">
            <li>Workflow State: ${alertInstance.workflowState?.displayName}</li>
      </g:if>
</g:if>

<g:if test="${alertInstance.initialDataSource}">
        <li>Initial data source: ${alertInstance.initialDataSource}</li>
</g:if>
<g:if test="${alertInstance.description}">
        <li>Additional information:${alertInstance.description}</li>
</g:if>
<g:if test="${alertInstance.assignedTo}">
        <li>Assigned user: ${alertInstance.assignedTo.fullName}</li>
</g:if>
<g:if test="${alertInstance.disposition}">
        <li>Disposition: ${alertInstance.disposition?.displayName}</li>
</g:if>
<g:if test="${alertInstance.priority}">
        <li>Priority: ${alertInstance.priority?.displayName}</li>
</g:if>
<g:if test="${alertLink}">
        <li>Alert Detail Screen Hyperlink: <a href="${alertLink}">Link</a></li>
</g:if>
    </ul>
</div>
</body>
</html>