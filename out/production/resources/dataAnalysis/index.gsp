<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.DataAnalysis.title"/></title>
    <asset:javascript src="handlebar/handlebars-v4.0.11.js"/>
    <asset:javascript src="jquery-ui/jquery-ui.min.js"/>
    <asset:javascript src="fuelux/fuelux.js"/>
    <asset:javascript src="app/utils/pvr-common-util.js"/>
    <asset:javascript src="app/pvs/common/rx_common.js"/>
    <script type="application/javascript">
        var spotfireFilesListUrl = "${createLink(controller: 'dataAnalysis', action:'list')}";
        var spotfireFileViewUrl = "${createLink(controller: 'dataAnalysis', action:'view',absolute: true, params:
                   [configurationBlock:setFilterString])}";
        var spotfireFileForFaersVaersDataSource = "${createLink(controller: 'dataAnalysis', action:'view',absolute: true, params:
                           [configurationBlock:''])}";
        var serverUrl = "${wp_url}";
        var tk = "${auth_token}";
        var libraryRoot = "${libraryRoot}";
        var cb_srv = "${callback_server}";
    </script>
    <asset:stylesheet src="fuelux.css"/>
</head>

<body>
<rx:container title="${message(code: "app.label.title.library.dataAnalysis", default: "Analysis File Library")}"
              options="${true}">

    <g:render template="/includes/layout/flashErrorsDivs"/>

    <div>
        <table id="rxTableSpoftfireFiles" class="row-border hover" width="100%">
            <thead>
            <tr>
                <th><g:message code="app.label.spotfire.fileName"/></th>
                <th>Generation Time</th>
                <th><g:message code="app.label.spotfire.dateCreated"/></th>
                <th><g:message code="app.label.spotfire.lastUpdated"/></th>
                <th><g:message code="app.label.spotfire.dateAccessed"/></th>
                <th style="width: 50px;"><g:message code="app.label.action"/></th>
            </tr>
            </thead>
        </table>
    </div>
</rx:container>
<asset:javascript src="app/pvs/dataAnalysis/dataAnalysisList.js"/>
<asset:javascript src="app/pvs/dataTablesActionButtons.js"/>
</body>
