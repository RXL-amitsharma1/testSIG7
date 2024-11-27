<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.ProductIngredientMapping.title"/></title>
    <asset:javascript src="app/pvs/productIngredientMapping/productIngredientListing.js"/>
    <asset:javascript src="app/pvs/dataTablesActionButtons.js"/>
    <asset:javascript src="app/pvs/bootbox.min.js"/>
    <asset:javascript src="jquery-ui/jquery-ui.min.js"/>
    <g:javascript>
        var listQueriesUrl = "${createLink(controller: 'productIngredientsMapping', action: 'list')}";
        var productIngredientMappingCreateUrl = "${createLink(controller: 'productIngredientsMapping', action: 'create')}";
        var productIngredientMappingEditUrl = "${createLink(controller: 'productIngredientsMapping', action: 'edit')}";
        var toggleEnableUrl = "${createLink(controller: 'productIngredientsMapping', action: 'toggleEnableRule')}";
        var isAdmin = ${grails.plugin.springsecurity.SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_CONFIGURATION_CRUD")};
    </g:javascript>
</head>

<body>
<rx:container title="Product/Ingredient Mapping List" options="${true}" bean="${error}">

    <g:render template="/includes/layout/flashErrorsDivs"/>
    <div class="messageContainer"></div>

    <div>
        <table id="rxTableQueries" class="row-border hover" width="100%">
            <thead>
            <tr>
                <th>Data Source</th>
                <th>Product/Ingredient</th>
                <th>Company Data</th>
                <th>Product Level</th>
                <th>Last Modified By</th>
                <th>Last Modified</th>
                <th>Enabled</th>
                <sec:ifAnyGranted roles="ROLE_CONFIGURATION_CRUD">
                    <th style="width: 30px;"><g:message code="app.label.actions"/></th>
                </sec:ifAnyGranted>
            </tr>
            </thead>
        </table>
    </div>
</rx:container>
</body>