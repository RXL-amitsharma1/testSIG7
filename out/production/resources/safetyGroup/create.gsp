<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:javascript>var getProductURL = "${createLink(controller: 'group', action: 'getProducts')}"</g:javascript>
    <g:set var="entityName" value="${message(code: 'safety.group.label')}"/>
    <asset:stylesheet src="jquery-picklist.css"/>
    <asset:javascript src="jquery/jquery.js" />
    <asset:javascript src="jquery-ui/jquery-ui.min.js" />
    <asset:javascript src="jquery/jquery-picklist.js" />
    <asset:javascript src="app/pvs/safetyGroup/safetyGroup.js" />
    <title><g:message code="safety.groups.label" /></title>
    <asset:javascript src="app/pvs/productSelection.js"/>
</head>
<body>
<rx:container title="${message(code: "product.safety.lead.groups")}" >

    <g:render template="/includes/layout/flashErrorsDivs" bean="${safetyGroupInstance}" var="theInstance"/>

    <g:form method="post" action="save" class="" name="safetyGroupForm">
        <g:render template="form" model="[roleInstance: safetyGroupInstance, lmProducts: lmProducts, allowedProductsList: allowedProductsList]"/>

        <div class="buttonBar m-t-10">
            <button name="edit" class="btn btn-primary">
                %{--<span class="glyphicon glyphicon-ok icon-white"></span>--}%
                ${message(code: 'default.button.save.label')}
            </button>
            <g:link class="cancelLink btn btn-default" action="index">Cancel</g:link>
        </div>
    </g:form>

</rx:container>

</body>
</html>
