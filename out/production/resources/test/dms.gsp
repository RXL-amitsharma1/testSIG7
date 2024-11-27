<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'controlPanel.label')}"/>
    <title><g:message code="app.controlPanel.title"/></title>
    <asset:javascript src="dmsConfiguration.js"/>
    <g:javascript>
        var dmsFoldersUrl = "${createLink(controller: 'controlPanel', action: 'getDmsFolders')}";
        var addDmsConfiguration = "${createLink(controller: "controlPanel", action: "addDmsConfiguration")}";
    </g:javascript>
</head>

<body>
sd

<a href="#" class="sendToDms" role="menuitem" data-toggle="modal" data-target="#sendToDmsModal"><i data-toggle="tooltip" class="fa fa-upload font-16" title="Send to DMS"> </i></a>
<g:form controller="test" action="sendToDms" method="post" onsubmit="return submitForm()">
    <g:render plugin="pvdms" template="/dms/sendToDmsModal"/>
</g:form>
</body>
</html>
