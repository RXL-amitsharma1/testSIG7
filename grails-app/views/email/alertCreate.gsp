<%@ page contentType="text/html"%>
<html>
<head>
    <title>Alert Create</title>
</head>

<body>
<h2>Message:</h2>

<div><g:message code='app.email.alert.create.msg' args='${[alertInstance.initialDataSource]}' /></div>
<div>
    <ul>
        <li>Product Name: ${productName}</li>
        <li>Topic Name: ${alertInstance.topic}</li>
        <li>Detected by: ${alertInstance.detectedBy}</li>
        <li>Detected date:  ${com.rxlogix.util.DateUtil.toUSDateString(alertInstance.detectedDate,
                timezone)}</li>
        <li>Additional information:${alertInstance.description}</li>
        <li>Assigned user : ${alertInstance.assignedTo.fullName}</li>
        <li>Alert Detail Screen Hyperlink: <a href="${alertLink}">Link</a></li>
    </ul>
</div>
</body>
</html>