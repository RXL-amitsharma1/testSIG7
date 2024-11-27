<%@ page contentType="text/html" %>
<html>
<head>
    <title>Document Assessment Reminder</title>
</head>

<body>
<h2>Message:</h2>

<div><g:message code='app.document.assessment.reminder.msg'/></div>

<div>
    <ul>
        <li>Produce/Generic Name: ${alertInstance?.buildProductNameList().toString()}</li>
        <li>Topic: ${alertInstance?.topic}</li>
        <li>Detected by: ${alertInstance?.detectedBy}</li>
        <li>Detected date: ${com.rxlogix.util.DateUtil.toUSDateString(alertInstance.detectedDate,
                timezone)}</li>
        <li>Workflow state: ${alertInstance?.workflowState?.displayName}</li>
        <li>Initial data source: ${alertInstance?.initialDateSource}</li>
        <li>Additional information:${alertInstance?.description}</li>
        <li>Assigned user: ${alertInstance?.assignedTo?.fullName}</li>
        <li>Disposition: ${alertInstance?.disposition?.displayName}</li>
        <li>Priority: ${alertInstance?.priority?.displayName}</li>
        <li>Target completion date: ${com.rxlogix.util.DateUtil.toUSDateString(document?.targetDate, timezone)}</li>
        <li>Document status: ${document?.documentStatus}</li>
        <li>Document status date: ${com.rxlogix.util.DateUtil.toUSDateString(document?.statusDate, timezone)}</li>
        <li>Alert Detail Screen Hyperlink: <a href="${alertLink}">Link</a></li>
    </ul>
</div>
</body>
</html>