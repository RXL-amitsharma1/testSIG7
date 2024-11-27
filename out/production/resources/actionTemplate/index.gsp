<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Action Template</title>
    <g:javascript>
        var actionTemplateUrl = "${createLink(controller: 'actionTemplate', action: 'list')}";
        var saveActionTemplateUrl = "${createLink(controller: 'actionTemplate', action: 'save')}";
        var deleteActionTemplateUrl = "${createLink(controller: 'actionTemplate', action: 'delete')}";
        var editActionTemplateUrl = "${createLink(controller: 'actionTemplate', action: 'edit')}";
        var updateActionTemplateUrl = "${createLink(controller: 'actionTemplate', action: 'update')}";
        var isAdmin = ${grails.plugin.springsecurity.SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_CONFIGURATION_CRUD")};
    </g:javascript>
    <asset:javascript src="fuelux/fuelux.js"/>
    <asset:stylesheet src="fuelux.css"/>
    <asset:javascript src="app/pvs/common/rx_common.js"/>
    <asset:javascript src="app/pvs/actions/actionTemplate.js"/>
    <asset:javascript src="app/pvs/dataTablesActionButtons.js"/>
    <asset:javascript src="app/pvs/bootbox.min.js"/>

</head>

<body>

<g:render template="/includes/layout/flashErrorsDivs" bean="${actionTemplate}" var="theInstance"/>
<div class="messageContainer"></div>

<div class="rxmain-container rxmain-container-top">
    <div class="rxmain-container-inner">
        <g:render template="includes/showList" />
    </div>
</div>

<g:render template="includes/actIonTemplateModal" />

</body>
</html>