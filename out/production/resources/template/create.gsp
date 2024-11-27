<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.CreateTemplate.title" /></title>
    <g:javascript>
        var editMessage = "${message(code: "app.onlyAdminCreateNewTags.message")}";
        var createCategoryUrl = "${createLink(controller: 'template', action: 'addCategory')}";
        var templateType = "${error?.templateType ? error?.templateType : ""}";
    </g:javascript>
    <asset:javascript src="fuelux/fuelux.js"/>
    <asset:stylesheet src="fuelux.css"/>
    <asset:javascript src="app/pvs/tags.js"/>
    <asset:javascript src="app/pvs/category.js"/>
    <asset:javascript src="app/pvs/template/editColumns.js"/>
    <asset:javascript src="app/pvs/template/editColumnMeasure.js"/>
    <asset:javascript src="app/pvs/template/editMeasures.js"/>
    <asset:javascript src="app/pvs/expandingTextarea.js"/>
    <asset:javascript src="app/pvs/disableAutocomplete.js"/>
    <asset:stylesheet src="expandingTextarea.css"/>
    <asset:stylesheet src="template.css" />
</head>
<body>

<g:set var="userService" bean="userService"/>
<g:set var="editable" value="${Boolean.TRUE}"/>

<rx:container title="${message(code:"app.label.createTemplate")}">

    <g:render template="/includes/layout/flashErrorsDivs" bean="${reportTemplateInstance}" var="theInstance"/>

    <div class="container-fluid">
        <g:form controller="template" action="save" method="post" onsubmit="return submitForm()">
            <g:hiddenField name="owner" value="${userService.getUser().id}"/>

            <g:render template="form" model="['reportTemplateInstance': reportTemplateInstance]"/>

            <div class="row">
                <div class="col-xs-12">
                    <div class="text-right m-t-10">
                        <g:actionSubmit class="btn primaryButton btn-primary" action="save" value="${message(code:'default.button.save.label')}" />
                        <a class="btn btn-default pv-btn-grey" href="${createLink(controller:'template', action:'index')}"><g:message code="default.button.cancel.label"/></a>
                    </div>
                </div>
            </div>
        </g:form>
    </div>
</rx:container>
</body>
