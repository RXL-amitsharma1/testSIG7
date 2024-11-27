<%@ page contentType="text/html" %>
<html>
<head>
    <title>Case Assignment For Review</title>
</head>

<body>
<h4>Message:</h4>
<div>${emailMessage}</div>

<div>
    <ul>
        <li>Alert Name: ${alertName}</li>
        <li>Product Name: ${productName}</li>
        <li>Soc: ${soc}</li>
        <li>PT: ${pt}</li>
        <li>Workflow State: ${workflowState}</li>
        <li>Disposition: ${disposition}</li>
        <li>Algorithm: ${algorithm}</li>
        <li>Score: ${score}</li>
        <li>Justification: ${justification}</li>
    </ul>
    Review the alert using following link: <a href="${alertLink}">PV Signal</a>
</div>
</body>
</html>