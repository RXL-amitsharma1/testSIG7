<%@ page import="grails.converters.JSON" %>
<g:set var="templateService" bean="templateService"/>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.View.Template" /></title>
    <g:javascript>
        var templateType = "${template.templateType}";
    </g:javascript>
    <asset:javascript src="fuelux/fuelux.js"/>
    <asset:stylesheet src="fuelux.css"/>
    <asset:javascript src="app/pvs/template/editColumns.js"/>
    <asset:javascript src="app/pvs/template/editMeasures.js"/>
    <asset:javascript src="app/pvs/template/editColumnMeasure.js"/>
    <asset:stylesheet src="template.css" />
    <asset:javascript src="app/pvs/expandingTextarea.js"/>
    <asset:stylesheet src="expandingTextarea.css"/>
</head>

<body>
    <rx:container title="${title}" bean="${template}">

        <g:render template="/includes/layout/flashErrorsDivs" bean="${template}" var="theInstance"/>

        <div class="container-fluid">

            <div class="row">
                <div class="col-xs-3">
                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="app.label.templateName" /></label>
                            <div>${template.name}</div>
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="app.label.description" /></label>
                            <g:if test="${template.description}">
                                <div>${template.description}</div>
                            </g:if>
                            <g:else>
                                <div><g:message code="app.label.none.parends"/></div>
                            </g:else>
                        </div>
                    </div>
                </div>

                <div class="col-xs-3">
                    <div class="row">
                        <div class="col-xs-5">
                            <label><g:message code="app.label.category" /></label>
                            <g:if test="${template.category?.name}">
                                <div>${template.category?.name}</div>
                            </g:if>
                            <g:else>
                                <div><g:message code="app.label.none" /></div>
                            </g:else>
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="app.label.tag"/></label>
                            <g:if test="${template.tags?.name}">
                                <g:each in="${template.tags.name}">
                                    <div>${it}</div>
                                </g:each>
                            </g:if>
                            <g:else>
                                <div><g:message code="app.label.none" /></div>
                            </g:else>
                        </div>
                    </div>
                </div>

                <div class="col-xs-3">
                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="app.label.modifiedBy" /></label>
                            <div>${template.createdBy}</div>
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="app.label.modifiedDate" /></label>
                            <div>
                                <g:render template="/includes/widgets/dateDisplayWithTimezone" model="[date:template.lastUpdated]"/>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-xs-3">
                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="app.label.privatePublic"/></label>
                            <div>
                                <g:if test="${template.isPublic}">
                                    <g:message code="app.label.public" />
                                </g:if>
                                <g:else>
                                    <g:message code="app.label.private"/>
                                </g:else>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="col-xs-12">
                    <g:render template="includes/templateType" model="['reportTemplateInstance': template, 'editable': editable]" />
                    <g:hiddenField name="templateId" id="templateId" value="${template.id}" />
                    <g:hiddenField name="editable" id="editable" value="false" />
                </div>
            </div>

            <g:if test="${isExecuted}">
                <div class="row">
                    <div class="col-xs-12">
                        <div class="pull-right">
                            <g:link controller="template" action="view" id="${currentTemplate.id}"><g:message code="template.see.current.template" /></g:link>
                        </div>
                    </div>
                </div>
            </g:if>
            <g:else>
                <div class="row">
                    <div class="col-xs-12">
                        <div class="pull-right">
                            <g:link controller="configuration" action="create"
                                    params="[selectedTemplate: template.id]" class="btn primaryButton btn-primary"><g:message
                                    code="default.button.run.label"/></g:link>
                            <g:link controller="template" action="edit" id="${params.id}" class="btn btn-default pv-btn-grey"><g:message code="default.button.edit.label" /></g:link>
                            <g:link controller="template" action="copy" id="${params.id}" class="btn btn-default pv-btn-grey"><g:message code="default.button.copy.label" /></g:link>
                            <g:link controller="template" action="delete" id="${params.id}" class="btn btn-default pv-btn-dark-grey" url="#" data-toggle="modal" data-target="#deleteModal"
                                    data-instanceid="${params.id}" data-instancename="${template.name}"><g:message code="default.button.delete.label" /></g:link>
                        </div>
                    </div>
                </div>
            </g:else>
            <sec:ifAllGranted roles="ROLE_DEV">
            <div>
                <g:textArea name="templateExport" value="${templateService.getTemplateAsJSON(template)}" style="width: 100%; height: 150px; margin-top: 20px"/>
            </div>
            </sec:ifAllGranted>
        </div>
    </rx:container>
<g:form controller="${controller}" method="delete">
    <g:render template="/includes/widgets/deleteRecord" model="[controller: 'template']"/>
</g:form>

</body>
