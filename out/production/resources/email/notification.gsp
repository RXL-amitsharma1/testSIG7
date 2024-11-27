<%@ page contentType="text/html" %>
<html>
<head>
    <title>Case Assignment For Review</title>
</head>

<body>
<h4>Message:</h4>
<g:if test="${emailMessage}">
    <span>${emailMessage}</span>
    <br>
    <br>
</g:if>

<div>
    <ul>
        <g:each in="${map}" var="k, v" status="i">
            <g:if test="${(k != "Meeting Agenda" || (k == "Meeting Agenda" && v == "-")) && k != "Report Link"}">
                <li>${k} : ${v}</li>
            </g:if>
            <g:if test="${(k == "Meeting Agenda" || k == "Report Link") && v != "-"}">
                <li>${k} :
                    <g:each in="${v.split(" ")}" var="words">
                        <g:if test="${words.contains("http") || words.contains("www")}">
                            <a href="${words}">${words}</a>
                        </g:if>
                        <g:else>
                            ${words}
                        </g:else>
                    </g:each>
                </li>
            </g:if>
        </g:each>
    </ul>
    <g:if test="${screenName}">
        You can review the ${screenName} using following hyperlink <a href="${alertLink}">PV Signal</a>
    </g:if>
    <g:else>
        <g:if test="${caseLink}">
            You can review this case using following hyperlink <a href="${caseLink}">${map["Case Number"]}</a>
            <br>
        </g:if>
        You can review this alert using following hyperlink <a href="${alertLink}">PV Signal</a>
    </g:else>
</div>

</body>
</html>