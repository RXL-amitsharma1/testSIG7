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
        <div class="row rxDetailsBorder">
            <div class="col-xs-12">
                <label><g:message code="app.label.alert.criteria"/></label>
            </div>
        </div>

        <div class="row">
            <div class="col-xs-4">
                <g:if test="${alertInstance?.productSelection || alertInstance?.productGroupSelection}">
                    <label><g:message code="app.label.productSelection"/></label>
                    <g:if test="${alertInstance?.productSelection}">
                        <div>
                            ${alertInstance?.getNameFieldFromJson(alertInstance?.productSelection)}
                        </div>
                    </g:if>
                    <g:if test="${alertInstance?.productGroupSelection}">
                        <div>
                            ${alertInstance?.getGroupNameFieldFromJson(alertInstance?.productGroupSelection)}
                        </div>
                    </g:if>
                </g:if>
                <g:if test="${alertInstance?.studySelection}">
                    <label><g:message code="app.label.studySelection"/></label>

                    <div>
                        ${ViewHelper.getDictionaryValues(alertInstance?.studySelection, DictionaryTypeEnum.STUDY)}
                    </div>
                </g:if>
            </div>

            <div class="col-xs-4">
                <g:if test="${alertInstance?.eventSelection || alertInstance?.eventGroupSelection}">
                    <label><g:message code="app.label.eventSelection"/></label>
                    <g:if test="${alertInstance?.eventSelection}">
                        <div>
                            ${ViewHelper.getDictionaryValues(alertInstance?.eventSelection, DictionaryTypeEnum.EVENT)}
                        </div>
                    </g:if>
                    <g:if test="${alertInstance?.eventGroupSelection}">
                        <div>
                            ${ViewHelper.getDictionaryValues(alertInstance?.eventGroupSelection, DictionaryTypeEnum.EVENT_GROUP)}
                        </div>
                    </g:if>
                </g:if>
            </div>
        </div>


        <div class="row">
            <div class="col-xs-4">
                <label><g:message code="app.label.reportType"/></label>
                <div>${alertInstance?.reportType}</div>
            </div>

            <div class="col-xs-4">
                <label><g:message code="app.label.countryOfIncidence"/></label>

                <div><span class="prop-value">${alertInstance?.countryOfIncidence}</span></div>
            </div>

            <div class="col-xs-4">
                <label><g:message code="device.related.label"/></label>
                <div>${alertInstance.getAttr("deviceRelated")}</div>
            </div>
        </div>

        <div class="row rxDetailsBorder">
            <div class="col-xs-12">
                <label><g:message code="app.label.alert.workflow"/></label>
            </div>
        </div>

        <div class="row">

            <div class="col-xs-3">
                <label><g:message code="app.label.priority"/></label>
                <div>${alertInstance?.priority?.value}</div>
            </div>

            <div class="col-xs-3">
                <label><g:message code="app.label.disposition"/></label>
                <div>${alertInstance?.disposition?.displayName}</div>
            </div>

            <div class="col-xs-3">
                <label><g:message code="app.label.population.specific"/></label>
                <div>${alertInstance?.getAttr("populationSpecific")}</div>
            </div>


        </div>

        <div class="row rxDetailsBorder">
            <div class="col-xs-12">
                <label><g:message code="app.label.alert.details"/></label>
            </div>
        </div>

        <div class="row">
            <div class="col-xs-4">
                <div class="row">
                    <div class="col-xs-12">
                        <label><g:message code="app.label.alert.name"/></label>
                        <div>
                            <g:applyCodec encodeAs="HTML">
                                ${alertInstance?.name}
                            </g:applyCodec>
                        </div>
                    </div>
                </div>
                <div class="row">
                    <div class="col-xs-12">
                        <label><g:message code="app.label.comments"/></label>
                        <div>
                            <g:applyCodec encodeAs="HTML">
                                ${alertInstance?.notes}
                            </g:applyCodec>
                        </div>
                    </div>
                </div>
            </div>
            <g:if test="${Holders.config.alert.adhoc.custom.fields.enabled == true}">
                <div class="col-xs-4">

                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="app.label.evaluationType"/></label>

                            <div>${alertInstance?.getAttr("evaluationMethods")}</div>
                        </div>
                    </div>
                </div>
            </g:if>

            <div class="col-xs-4">

                <div class="row">
                    <div class="col-xs-12">
                        <label><g:message code="app.label.description"/></label>
                        <div>
                            <g:applyCodec encodeAs="HTML">
                                ${alertInstance?.description}
                            </g:applyCodec>
                        </div>
                    </div>
                </div>
            </div>
        </div>


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