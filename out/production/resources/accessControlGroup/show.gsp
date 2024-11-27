<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'accessControlGroup.label')}"/>
    <title><g:message code="accessControlGroup.label"/></title>
</head>

<body>

<g:set var="column1Width" value="4"/>
<g:set var="column2Width" value="8"/>

<rx:container title="${message(code: "app.label.accessControlGroup")}">

    <g:link action="index"><< Access Control Groups List</g:link>

    <h1 class="page-header"><g:message code="accessControlGroup.label"/></h1>

    <g:render template="/includes/layout/flashErrorsDivs" bean="${acgInstance}" var="theInstance"/>

    <g:render template="/includes/widgets/buttonBarCRUD" bean="${acgInstance}" var="theInstance"
              model="[whatIsBeingDeleted: acgInstance.name]"/>

    <div class="row">
        <div class="col-md-12">

            <h3 class="sectionHeader">Access Control Group Details</h3>

            <div class="col-md-12">

                <div class="row">
                    <div class="col-md-${column1Width}"><label><g:message code="accessControlGroup.name.label"/></label></div>

                    <div class="col-md-${column2Width}">${acgInstance.name}</div>
                </div>

                <div class="row">
                    <div class="col-md-${column1Width}"><label><g:message code="accessControlGroup.ldapGroupName.label"/></label></div>

                    <div class="col-md-${column2Width}">${acgInstance.ldapGroupName}</div>
                </div>

                <div class="row">
                    <div class="col-md-${column1Width}"><label><g:message code="accessControlGroup.description.label"/></label></div>

                    <div class="col-md-${column2Width}">${acgInstance?.description}</div>
                </div>
            </div>
            
        </div>
    </div>



</rx:container>

</body>
</html>
