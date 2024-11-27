<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.help.title" /></title>
    <asset:javascript src="app/help/help.js"/>
</head>

<body>
    <rx:container title="${message(code: "app.label.help")}" bean="${error}">

        <g:render template="/includes/layout/flashErrorsDivs"/>

        <div>
            <g:message code="app.label.help" />
        </div>
    </rx:container>
</body>