<%@ page import="grails.util.Holders" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <g:javascript>
        var getSelectedProductUrl = "${createLink(controller: 'configurationRest', action: 'getSelectedProduct')}";
        var searchProductsUrl = "${createLink(controller: 'configurationRest', action: 'searchProducts')}";
        var productGroupUrl = "${createLink(controller: 'productGroup', action: 'list')}";
        var editUrl = "${createLink(controller: 'productGroup', action: 'edit')}";
        var deleteUrl = "${createLink(controller: 'productGroup', action: 'delete')}";
        var isAdmin = ${grails.plugin.springsecurity.SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_CONFIGURATION_CRUD")};

    </g:javascript>
    <asset:stylesheet src="configuration.css"/>
    <sec:ifAnyGranted roles="ROLE_CONFIGURATION_CRUD">
        <g:if test="${!Holders.config.pv.plugin.dictionary.enabled}">
            <asset:javascript src="app/pvs/alert_utils/alert_product_utils.js"/>
        </g:if>
        <g:else>
            <asset:javascript src="app/pvs/alert_utils/dictionary-utils.js"/>
        </g:else>
    </sec:ifAnyGranted>
    <asset:javascript src="app/pvs/productGroup.js"/>
    <g:if test="${!Holders.config.pv.plugin.dictionary.enabled}">
        <asset:stylesheet src="dictionaries.css"/>
    </g:if>
    <asset:javascript src="app/pvs/configuration/copyPasteValues.js"/>
    <asset:javascript src="app/pvs/configuration/dictionaryMultiSearch.js"/>
    <asset:stylesheet src="copyPasteModal.css"/>
    <asset:javascript src="app/pvs/dataTablesActionButtons.js"/>
    <asset:javascript src="app/pvs/bootbox.min.js"/>
    <asset:javascript src="app/pvs/users/user_edit.js"/>

</head>

<body>

<g:render template="/includes/layout/flashErrorsDivs" bean="${productGroup}" var="theInstance"/>
<input type="hidden" id="selectedDatasource" class="selectedDatasource" value="pva"/>
<sec:ifAnyGranted roles="ROLE_CONFIGURATION_CRUD">
    <g:render template="add_product_group" model="[productGroup: productGroup]"/>
</sec:ifAnyGranted>
<g:render template="show_product_group_list"/>
</body>
</html>