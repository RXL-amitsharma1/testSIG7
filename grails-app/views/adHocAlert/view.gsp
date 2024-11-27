<%@ page import="com.rxlogix.config.EvaluationReferenceType; com.rxlogix.enums.DictionaryTypeEnum; com.rxlogix.util.ViewHelper; grails.util.Holders;" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.View.Alert.title" args="${[alertInstance.name]}"/></title>
    <asset:javascript src="fuelux/fuelux.js"/>
    <asset:stylesheet src="fuelux.css"/>
    <asset:javascript src="app/pvs/scheduler.js"/>
    <asset:javascript src="app/pvs/configuration/viewScheduler.js"/>
    <asset:javascript src="app/pvs/widgets/properties_panel.js" />
</head>

<body>
<g:set var="userService" bean="userService"/>
<g:set var="timeZone" value="${userService?.getUser()?.preference?.timeZone}"/>
<g:set var="timeZone" value="${userService?.getUser()?.preference?.timeZone}"/>
<g:set var="userService" bean="userService"/>

<rx:container title="Adhoc Evaluation">

    <g:render template="/includes/layout/flashErrorsDivs" bean="${alertInstance}" var="theInstance"/>

    <div class="container-fluid">
    <!-- Loop through each section in the fieldMap -->
        <g:each in="${sectionWiseFields}" var="sectionEntry">
            <!-- Render Section Header -->
            <div class="row rxDetailsBorder">
                <div class="col-xs-12">
                    <h4><label>${sectionEntry.key}</label></h4>
                </div>
            </div>
            <div class="row">
            <g:each in="${sectionEntry.value}" var="field" status="index">
                <!-- Start a new row every 3 fields -->
                <g:if test="${index % 3 == 0 && index > 0}">
                    </div><div class="row">
                </g:if>
                <g:if test="${alertInstance?.studySelection}">
                    <label><g:message code="app.label.studySelection"/></label>
                </g:if>
                <!-- Render each field in a 4-column layout -->
                <div class="col-xs-4">
                    <label>${field?.label}</label>
                    <g:set var="fieldName" value="${field?.name}" />
                    <div>
                        <!-- Custom Handling for Known Field Names -->
                        <g:if test="${field?.name == 'productSelection' || field?.name == 'productGroupSelection'}">
                            <g:if test="${alertInstance?.productSelection}">
                                ${alertInstance?.getNameFieldFromJson(alertInstance?.productSelection)}
                            </g:if>
                            <g:elseif test="${alertInstance?.productGroupSelection}">
                                ${alertInstance?.getGroupNameFieldFromJson(alertInstance?.productGroupSelection)}
                            </g:elseif>
                        </g:if>
                        <g:elseif test="${field?.name == 'studySelection' && alertInstance?.studySelection}">
                                ${ViewHelper.getDictionaryValues(alertInstance?.studySelection, DictionaryTypeEnum.STUDY)}
                        </g:elseif>
                        <g:elseif test="${field?.name == 'eventSelection' || field?.name == 'eventGroupSelection'}">
                            <g:if test="${alertInstance?.eventSelection}">
                                ${ViewHelper.getDictionaryValues(alertInstance?.eventSelection, DictionaryTypeEnum.EVENT)}
                            </g:if>
                            <g:elseif test="${alertInstance?.eventGroupSelection}">
                                ${ViewHelper.getDictionaryValues(alertInstance?.eventGroupSelection, DictionaryTypeEnum.EVENT_GROUP)}
                            </g:elseif>
                        </g:elseif>
                        <g:elseif test="${field?.name == 'evaluationMethods' && Holders.config.alert.adhoc.custom.fields.enabled == true}">
                            ${alertInstance?.getAttr("evaluationMethods")}
                        </g:elseif>
                        <g:elseif test="${field?.name == 'deviceRelated' || field?.name == 'populationSpecific'}">
                            ${alertInstance.getAttr("${fieldName}")}
                        </g:elseif>
                        <g:elseif test="${field?.name == 'priority'}">
                            ${alertInstance?.priority?.value}
                        </g:elseif>
                        <g:elseif test="${field?.name == 'disposition'}">
                            ${alertInstance?.disposition?.displayName}
                        </g:elseif>
                        <g:elseif test="${field?.name == 'name' || field?.name == 'notes' || field?.name == 'description'}">
                            <g:applyCodec encodeAs="HTML">
                                ${alertInstance?."${fieldName}"}
                            </g:applyCodec>
                        </g:elseif>
                        <g:else>
                            ${alertInstance?."${fieldName}"}
                        </g:else>
                    </div>
                </div>
            </g:each>
            </div>
        </g:each>
    </div>

</rx:container>
<rx:container title="${message(code: 'app.label.attachments')}">
    <g:render template="/includes/widgets/attachment_panel" model="[alertInst: alertInstance]"/>
</rx:container>

<div class="row">
    <div class="col-xs-12">
        <div class="pull-right m-t-15">
            <g:link controller="adHocAlert" action="alertDetail" id="${params.id}"
                    class="btn btn-default pv-btn-grey m-r-10"><g:message
                    code="default.button.view.label"/></g:link>
            <g:link controller="adHocAlert" action="edit" id="${params.id}"
                    class="btn btn-primary"><g:message
                    code="default.button.edit.label"/></g:link>
        </div>
    </div> 
</div>
</body>
</html>
