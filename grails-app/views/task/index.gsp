<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.TaskLibrary.title" /></title>
    <asset:javascript src="app/pvs/task/task.js"/>
</head>

<body>
    <rx:container title="${message(code:"app.label.tasks")}" bean="${error}">

        <g:render template="/includes/layout/flashErrorsDivs"/>

        <div>
            <g:message code="app.label.tasks" />
        </div>
    </rx:container>
</body>