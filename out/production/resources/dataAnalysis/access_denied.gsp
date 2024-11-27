<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.DataAnalysis.title" /></title>
</head>
<g:set var="spotfireService" bean="spotfireService"/>
<body>
<rx:container title="${message(code:"app.label.dataAnalysis", default: "Data Analysis")}" bean="${error}">
    <g:render template="/includes/layout/flashErrorsDivs" bean="${spotfireCommand}" var="theInstance"/>
</rx:container>
</body>
