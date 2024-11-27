<%@ page import="com.rxlogix.config.Disposition" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main"/>
    <title><g:message code="disposition.edit" default="Edit Disposition"/></title>
</head>
<rx:container title="Edit Disposition">
    <g:render template="/includes/layout/flashErrorsDivs" bean="${dispositionInstance}" var="theInstance"/>
    <div class="nav bord-bt">
        <span class="menuButton"><g:link class="list btn btn-default" action="list"><g:message code="disposition.list"
                                                                                               default="Disposition List"/></g:link></span>
        <sec:ifAnyGranted roles="ROLE_CONFIGURATION_CRUD">
            <span class="menuButton"><g:link class="create btn btn-primary" action="create"><g:message
                    code="disposition.new" default="New Disposition"/></g:link></span>
        </sec:ifAnyGranted>
    </div>

    <div class="body">
        <g:form method="post" name="dispositionForm">
            <g:render template="form" model="[dispositionInstance: dispositionInstance, edit: true]"/>
        </g:form>
    </div>
</rx:container>
</html>
