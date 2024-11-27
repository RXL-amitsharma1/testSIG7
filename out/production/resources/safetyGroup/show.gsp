<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'safety.group.label')}"/>
    <title><g:message code="safety.group.label"/></title>
</head>

<body>

<g:set var="column1Width" value="4"/>
<g:set var="column2Width" value="8"/>

<rx:container title="${message(code: "product.safety.lead.groups")}">

    <div class="row">
        <g:render template="/includes/layout/flashErrorsDivs" bean="${safetyGroupInstance}" var="theInstance"/>
    </div>
    <br/>
    <div class="row">
        <div class="col-md-12">
            <g:link action="index" class="btn btn-default"><i class="fa fa-long-arrow-left"></i> ${message(code: "safety.group.label")} List</g:link>
            <g:render template="/includes/widgets/buttonBarCRUD" bean="${safetyGroupInstance}" var="theInstance"
                      model="[showDeleteButton: false]"/>
        </div>
    </div>
    <br/>

    <div class="row">
        <div class="col-md-12">
            <div class="col-md-12">
                <div class="row">
                    <div class="col-md-${column1Width}"><label><g:message code="group.name.label"/></label></div>
                    <div class="col-md-${column2Width}"><g:message code="app.role.${safetyGroupInstance.name.encodeAsHTML()}" default="${safetyGroupInstance.name.encodeAsHTML()}"/></div>
                </div>
                </div>

                <div class="row">
                    <div class="col-md-${column1Width}"><label><g:message code="group.allowed.products"/></label></div>
                    <g:if test="${safetyGroupInstance.allowedProd!=null}">
                        <div class="col-md-${column2Width}  word-break">${safetyGroupInstance.allowedProd.split('#%#').join(',')}</div>
                    </g:if>
                </div>
            </div>
        </div>
    </div>



</rx:container>

</body>
</html>
