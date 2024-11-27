<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.DataAnalysis.title"/></title>
    <g:javascript>
        var spotfireFilesListUrl = "${createLink(controller: 'dataAnalysis', action:'list')}";
        var spotfireFileViewUrl = "${createLink(controller: 'dataAnalysis', action:'view')}";
        var ajaxProductFamilySearchUrl = "${createLink(controller: 'dataAnalysis', action: 'getProductFamilyList')}"
    </g:javascript>
    <asset:javascript src="app/pvs/dataAnalysis/dataAnalysis.js"/>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
    <asset:stylesheet href="spotfire_integration.css"/>

    %{--todo:  Used only for code that toggle panel; move that code to a centralized/non-configuration specific file. - morett--}%
    <asset:javascript src="app/configuration/configurationCommon.js"/>
    <script type="application/javascript">
        $(function () {
            spotfire.init();
            spotfire.keepLive(${interval});
            spotfire.openSpotfireReport("${fileName}", "${libraryRoot}", "${cookie}", "${server_url}", "${auth_token}");
        });
    </script>
</head>

<body>
<rx:container title="${message(code: "app.label.dataAnalysis", default: "Data Analysis")} - ${fileName.encodeAsXML()}">
    <div id="spotfirePanel"><g:message code="app.spotfire.openingfile.message"/></div>
    <g:javascript>
            var finalURL =  "${finalUrl}"
    </g:javascript>
    </div>

</rx:container>
</body>
