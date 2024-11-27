<%@ page import="com.rxlogix.util.DateUtil" contentType="text/html" %>
<html>
<head>
    <title>Action Create</title>
</head>

<body>
<h2>Message:</h2>

<div><g:message code='app.email.action.create.msg' args='${[productName]}'/></div>

<div>
    <ul>
        <li>Product Name: ${productName}</li>
        <li>Topic Name: ${alertInstance.topic}</li>
        <li>Detected by: ${alertInstance.detectedBy}</li>
        <li>Detected date: ${com.rxlogix.util.DateUtil.toUSDateString(alertInstance.detectedDate,
                timezone)}</li>
        <li>Workflow State: ${alertInstance.workflowState?.displayName}</li>
        <li>Initial data source: ${alertInstance.initialDataSource}</li>
        <li>Additional information:${action?.details}</li>
        <li>Assigned user: ${alertInstance.assignedTo.fullName}</li>
        <li>Disposition: ${alertInstance.disposition?.displayName}</li>
        <li>Priority: ${alertInstance.priority?.displayName}</li>
        <li>Action: ${action?.config.displayName}</li>
        <li>Targeted completion date: ${com.rxlogix.util.DateUtil.toUSDateString(
                action?.dueDate, timezone)}</li>
        <li>Alert Detail Screen Hyperlink: <a href="${alertLink}">Link</a></li>
    </ul>
</div>
</body>
</html>