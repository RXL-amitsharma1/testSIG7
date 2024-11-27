<%@ page contentType="text/html" %>
<html>
<head>
    <title>Alert Changed</title>
</head>

<body>
<h2>Message:</h2>
    <g:if test="${!currentState.equals('none')}">
        <div><g:message code='app.workflow.state.changed.msg'/>&nbsp;<b>${currentState}</b> to <b>${newState}</b></div>
    </g:if>
    <g:if test="${!currentDisposition.equals('none')}">
        <div><g:message code='app.disposition.changed.msg'/>&nbsp;<b>${currentDisposition}</b> to <b>${newDisposition}</b></div>
    </g:if>
<div>
    <ul>
        <li>Topic Name: ${alertInstance.topic}</li>
        <li>Detected by: ${alertInstance.detectedBy}</li>
        <li>Detected date: ${com.rxlogix.util.DateUtil.toUSDateString(alertInstance.detectedDate,
                timezone)}</li>
        <li>Initial data source: ${alertInstance.initialDataSource}</li>
        <li>Additional information:${alertInstance.description}</li>
        <li>Assigned user : ${fullName}</li>
        <li>Alert Detail Screen Hyperlink: <a href="${alertLink}">Link</a></li>
    </ul>
</div>
</body>
</html>