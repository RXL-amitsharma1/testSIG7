<%@ page contentType="text/html" %>
<html>
<head>
    <title>Assignment Change</title>
</head>

<body>
<h2>Message:</h2>
<div><g:message code="app.email.change.assignment.message" args='${[alertInstance.name]}' /></div>

<div>
    <ul>
        <li>Product/Generic name: ${productName}</li>
        <li>Topic Name: ${alertInstance.topic}</li>
        <li>Detected date:  ${com.rxlogix.util.DateUtil.toUSDateString(alertInstance.detectedDate,
                timezone)} </li>
        <li>Additional information:${alertInstance.description}</li>
        <li>Alert Detail Screen Hyperlink: <a href="${alertLink}">Link</a></li>
    </ul>
</div>
</body>
</html>