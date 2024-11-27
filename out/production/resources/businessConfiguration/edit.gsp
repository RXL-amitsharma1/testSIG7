<%@ page import="grails.util.Holders" contentType="text/html;charset=UTF-8" %>

<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.label.business.configuration.title" /></title>
    <asset:stylesheet src="configuration.css"/>
    <asset:javascript src="app/pvs/common/rx_common.js"/>
    <asset:javascript src="fuelux/fuelux.js"/>
    <asset:javascript src="app/pvs/disableAutocomplete.js"/>
    <asset:stylesheet src="fuelux.css" />
    <asset:javascript src="boostrap-switch.js"/>
    <asset:javascript src="app/pvs/businessConfiguration/businessConfiguration.js" />
    <g:javascript>
        var faersDisabledColumnsIndexesUrl = "${createLink(controller: 'aggregateCaseAlert', action: 'fetchFaersDisabledColumnsIndexes')}";
    </g:javascript>
</head>

<body>
<g:render template="/includes/layout/flashErrorsDivs" bean="${businessConfiguration}" var="theInstance"/>
<div class="rxmain-container rxmain-container-top">
    <div class="rxmain-container-inner">
        <div class="rxmain-container-row rxmain-container-header">
            <label class="rxmain-container-header-label click"><g:message code="app.label.business.configuration.title" /></label>
        </div>
        <div class="rxmain-container-content">
            <div class="row">
                <g:render template="form" model="[businessConfiguration: businessConfiguration , mode: 'edit', workflowStates: workflowStates]"/>
            </div>
        </div>
    </div>
</div>
<g:if test="${Holders.config.pv.plugin.dictionary.enabled}">
<script>
    var pvaUrls= {
        selectUrl :options.product.selectUrl,
        preLevelParentsUrl: options.product.preLevelParentsUrl,
        searchUrl:options.product.searchUrl
    };
    var otherUrls= {
        selectUrl :"${createLink(controller: 'pvsProductDictionary', action: 'getSelectedProduct')}",
        preLevelParentsUrl: "${createLink(controller: 'pvsProductDictionary', action: 'getPreLevelProductParents')}",
        searchUrl:"${createLink(controller: 'pvsProductDictionary', action: 'searchProducts')}"
    };
    var faersDisabledColumnsIndexesUrl = "${createLink(controller: 'aggregateCaseAlert', action: 'fetchFaersDisabledColumnsIndexes')}";
    changeDataSource("${businessConfiguration?.dataSource?:'pva'}");
</script>
</g:if>
</body>
</html>