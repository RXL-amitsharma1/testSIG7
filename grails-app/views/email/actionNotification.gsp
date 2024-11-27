<%@ page contentType="text/html" %>
<html>
<head>
    <title>Action Notification</title>
</head>

<body>
<h4>Message: </h4>
<span>${emailMessage}</span>
<br>
<br>

<div>
    <ul>
        <li>Alert Name : ${alertInstance}</li>
        <li>Product Name: ${productName}</li>
        <li>Action type: ${actionType}</li>
        <li>Action: ${action}</li>
        <li>Due Date: ${dueDate}</li>
        <li>Action Details: ${details}</li>
        <br>
    </ul>
        Alert Screen Hyperlink: <a href="${alertLink}">PV Signal</a>

</div>
</body>
</html>