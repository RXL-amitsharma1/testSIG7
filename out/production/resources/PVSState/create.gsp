
<%@ page import="com.rxlogix.config.PVSState" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title><g:message code="PVSState.create" default="Create Workflow State" /></title>
    </head>
    <body>

    <rx:container title="Create Workflow State">
        <g:render template="/includes/layout/flashErrorsDivs" bean="${PVSStateInstance}" var="theInstance"/>
        <div class="nav bord-bt">
            <span class="menuButton"><g:link class="list btn btn-primary" action="index">
                <g:message code="PVSState.list" default="Workflow State List" /></g:link>
            </span>
        </div>
        <div class="body">
            <g:form action="save" method="post" >
                <g:render template="form1" bean="${PVSStateInstance}" />
            </g:form>
        </div>

    </rx:container>
    </body>
</html>
