<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.EditTemplate.title" /></title>
    <g:javascript>
        var createCategoryUrl = "${createLink(controller: 'template', action: 'addCategory')}";
        var editMessage = "${message(code: "app.onlyAdminCreateNewTags.message")}";
        var templateType = "${template.templateType}";
    </g:javascript>
    <asset:javascript src="fuelux/fuelux.js"/>
    <asset:stylesheet src="fuelux.css"/>
    <asset:javascript src="app/pvs/tags.js"/>
    <asset:javascript src="app/pvs/category.js"/>
    <asset:javascript src="app/pvs/template/editColumns.js"/>
    <asset:javascript src="app/pvs/template/editMeasures.js"/>
    <asset:javascript src="app/pvs/template/editColumnMeasure.js"/>
    <asset:javascript src="app/pvs/expandingTextarea.js"/>
    <asset:javascript src="app/pvs/disableAutocomplete.js"/>
    <asset:stylesheet src="template.css" />
    <asset:stylesheet src="expandingTextarea.css"/>
    <script>
        $(document).ready( function () {
            var counter = $('#selectedCount');
            $("#selectedColumns").change(function() {
                counter.text($("#selectedColumns :selected").length);
            });
            $("#category").select2();
        });
    </script>
</head>

<g:set var="editable" value="${Boolean.TRUE}"/>

<body>
    <rx:container title="${message(code: "app.label.editTemplate")}" bean="${template}">

        <g:render template="/includes/layout/flashErrorsDivs" bean="${template}" var="theInstance"/>

        <div class="container-fluid">
            <g:form controller="template" action="update" method="post" onsubmit="return submitForm()">
                <g:hiddenField name="id" value="${template.id}" />

                <g:render template="form" model="['reportTemplateInstance': template, 'edit': true]"/>

                <div class="row">
                    <div class="col-xs-12">
                        <div class="pull-right">
                            <g:hiddenField name="edit" id="edit" value="${isAdmin}" />
                            <g:hiddenField name="templateId" id="templateId" value="${template.id}" />
                            <g:actionSubmit class="btn primaryButton btn-primary" action="update" value="${message(code:'default.button.update.label')}" />
                            <a class="btn btn-default pv-btn-grey" href="${createLink(controller:'template', action:'index')}"><g:message code="default.button.cancel.label"/></a>
                        </div>
                    </div>
                </div>
            </g:form>
        </div>
    </rx:container>
</body>
