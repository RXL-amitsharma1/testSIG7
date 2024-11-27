<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Comment Template</title>
    <g:javascript>
        var commentTemplateUrl = "${createLink(controller: 'commentTemplate', action: 'list')}";
        var saveCommentTemplateUrl = "${createLink(controller: 'commentTemplate', action: 'save')}";
        var deleteCommentTemplateUrl = "${createLink(controller: 'commentTemplate', action: 'delete')}";
        var editCommentTemplateUrl = "${createLink(controller: 'commentTemplate', action: 'edit')}";
        var updateCommentTemplateUrl = "${createLink(controller: 'commentTemplate', action: 'update')}";
        var isAdmin = ${grails.plugin.springsecurity.SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_CONFIGURATION_CRUD")};
        var list = [1,2,3,4,5,6,7,8];
    </g:javascript>
    <asset:javascript src="fuelux/fuelux.js"/>
    <asset:stylesheet src="fuelux.css"/>
    <asset:javascript src="app/pvs/common/rx_common.js"/>
    <asset:javascript src="app/pvs/comments/commentTemplate.js"/>
    <asset:javascript src="purify/purify.min.js" />
    <asset:javascript src="app/pvs/dataTablesActionButtons.js"/>
    <asset:javascript src="app/pvs/bootbox.min.js"/>
    <style>
        table.dataTable td {
            word-break: break-word;
        }
    </style>
</head>

<body>

<g:render template="/includes/layout/flashErrorsDivs" bean="${commentTemplate}" var="theInstance"/>
<div class="messageContainer"></div>

<div class="rxmain-container rxmain-container-top">
    <div class="rxmain-container-inner">
        <g:render template="includes/showList" />
    </div>
</div>

<g:render template="includes/commentTemplateModal" model="[labelConfig:labelConfig,commentCountList:commentCountList,commentScoresList:commentScoresList]" />

</body>
</html>