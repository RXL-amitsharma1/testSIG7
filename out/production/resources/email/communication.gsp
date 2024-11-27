<%@ page contentType="text/html" %>
<html>
<head>
    <title>Case Assignment For Review</title>
</head>

<body>
${raw(emailMessage)}

<div>
    <g:if test="${references}">
        <g:if test="${references[0].inputName}">
            <a href="${references[0].referenceName}">${references[0].inputName}</a>
        </g:if>
        <g:else>
            <a href="${references[0].referenceName}">${references[0].referenceName}</a>
        </g:else>
    </g:if>
</div>

</body>
</html>