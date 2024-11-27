<%@ page contentType="text/html" %>
<html>
<head>
    <title>Assignment Change</title>
</head>

<body>
<div><g:message code="app.email.change.assignment.message" args='${[alertInstance.name]}'/></div>

<div>
    <ul>
        <li><g:message code="app.label.product.name"/> :${productName}</li>
        <li><g:message code="app.topic.name"/> :${alertInstance.topic}</li>
        <li><g:message code="app.label.detected.date"/>  :${com.rxlogix.util.DateUtil.toUSDateString(alertInstance.detectedDate, timezone)} </li>
        <li><g:message code="app.label.additional.information"/> :${alertInstance.description}</li>
        <li><g:message code="app.label.alert.detail.screen.hyperlink"/> :<a href="${alertLink}">Link</a></li>
        <li><g:message code="app.label.assigned.to"/> :${alertInstance.owner}</li>`
    </ul>
</div>
</body>
</html>