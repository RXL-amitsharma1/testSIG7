<%@ page import="grails.converters.JSON; com.rxlogix.enums.DictionaryTypeEnum; com.rxlogix.enums.DateRangeEnum; com.rxlogix.util.DateUtil; com.rxlogix.config.ExecutedConfiguration; com.sun.xml.internal.bind.v2.TODO; com.rxlogix.util.ViewHelper; com.rxlogix.enums.DateRangeTypeCaseEnum;  com.rxlogix.config.DateRangeValue" %>
<%@ page import="com.rxlogix.enums.EvaluateCaseDateEnum" %>

<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.View.Alert.title" args="${[configurationInstance.name]}"/></title>
    <asset:javascript src="fuelux/fuelux.js"/>
    <asset:stylesheet src="fuelux.css"/>
    <asset:javascript src="app/pvs/scheduler.js"/>
    <asset:javascript src="app/pvs/configuration/viewScheduler.js"/>
    <asset:javascript src="app/pvs/alert_utils/alert_utils.js"/>
    <g:javascript>
    var isAlertScheduled = ${configurationInstance?.nextRunDate? true : false};
</g:javascript>
</head>

<body>

<g:set var="timeZone" value="${currentUser?.preference?.timeZone}"/>

<g:if test="${isExecuted}">
    <g:set var="title" value="${message(code: "app.label.alert.criteria", args: [configurationInstance.name])}"/>
</g:if>
<g:else>
    <g:set var="title" value="${message(code: "app.View.page.title", args: [configurationInstance.name])}"/>
</g:else>
<rx:container title="${title}">

    <g:render template="/includes/layout/flashErrorsDivs" bean="${configurationInstance}" var="theInstance"/>

    <div class="container-fluid">
        <div class="row rxDetailsBorder">
            <div class="col-xs-12">
                <label><g:message code="app.label.alert.information"/></label>
            </div>
        </div>

        <div class="row">
            <div class="col-xs-12">
                <div class="row">
                    <div class="col-xs-4">
                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.name"/></label>
                                <div class="wrap_text">
                                    <g:applyCodec encodeAs="HTML">
                                        ${configurationInstance.name}
                                    </g:applyCodec>
                                </div>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-xs-12 col-height word-break">
                                <label><g:message code="app.label.description"/></label>
                                <div>
                                    <g:applyCodec encodeAs="HTML">
                                        ${configurationInstance.description}
                                    </g:applyCodec>
                                </div>
                            </div>
                        </div>

                    </div>

                    <div class="col-xs-4">
                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.scheduledBy"/></label>

                                <div>${configurationInstance.owner.fullName}</div>
                            </div>
                            <div class="col-xs-12" ><label><g:message code="app.label.selected.datasheets"/></label>
                                <g:if test="${selectedDatasheets}">
                                    <div class="overflow-nowrap">
                                        ${selectedDatasheets}
                                    </div>
                                </g:if>
                            </div>
                        </div>

                    </div>

                    <div class="col-xs-4">
                        <div class="row">
                            <div class="col-xs-12 cell-break">
                                <g:if test="${configurationInstance.productSelection || configurationInstance.productGroupSelection}">
                                    <label><g:message code="app.reportField.productDictionary"/></label>

                                    <g:if test="${configurationInstance.productSelection}">
                                        <div id="showProductSelection"></div>
                                        ${ViewHelper.getProductDictionaryValues(configurationInstance, DictionaryTypeEnum.PRODUCT)}
                                    </g:if>
                                    <g:if test="${configurationInstance.productGroupSelection}">
                                        <div id="showProductGroupSelection"></div>
                                        ${ViewHelper.getDictionaryValues(configurationInstance, DictionaryTypeEnum.PRODUCT_GROUP)}
                                    </g:if>
                                </g:if>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-xs-12">
                                <g:if test="${configurationInstance.eventSelection || configurationInstance.eventGroupSelection}">
                                    <label><g:message code="app.reportField.eventDictId"/></label>

                                    <g:if test="${configurationInstance.eventSelection}">
                                        <div id="showEventSelection"></div>
                                        ${ViewHelper.getDictionaryValues(configurationInstance, DictionaryTypeEnum.EVENT)}
                                    </g:if>
                                    <g:if test="${configurationInstance.eventGroupSelection}">
                                        <div id="showEventGroupSelection"></div>
                                        ${ViewHelper.getDictionaryValues(configurationInstance, DictionaryTypeEnum.EVENT_GROUP)}
                                    </g:if>
                                </g:if>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="row rxDetailsBorder">
            <div class="col-xs-12">
                <label><g:message code="report.criteria.sections"/></label>
            </div>
        </div>

        <div class="row">
            <div class="col-xs-12">
                <div class="row">

                    <div class="col-xs-4">
                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.queryName"/></label>
                                <g:if test="${isExecuted}">
                                    <g:if test="${configurationInstance.executedQuery}">
                                        <div>
                                            <g:link controller="query" action="view" target="blank"
                                                    id="${configurationInstance.executedQuery}">${configurationInstance.executedQueryName}</g:link>
                                        </div>
                                    </g:if>
                                    <g:else>
                                        <div><g:message code="app.label.none"/></div>
                                    </g:else>
                                </g:if>
                                <g:else>
                                    <g:if test="${configurationInstance.query}">
                                        <div>
                                            <g:link controller="query" action="view" target="blank"
                                                    id="${configurationInstance.query}">${configurationInstance.queryName}</g:link>
                                        </div>
                                    </g:if>
                                    <g:else>
                                        <div><g:message code="app.label.none"/></div>
                                    </g:else>
                                </g:else>
                            </div>
                        </div>
                    </div>

                    <div class="col-xs-4">
                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.DateRange"/></label>

                                <div>
                                    <g:message
                                            code="${(configurationInstance?.dateRangeInformation?.dateRangeEnum?.i18nKey)}"/>
                                </div>
                            </div>
                        </div>

                        <g:if test="${configurationInstance?.dateRangeInformation?.dateRangeEnum == com.rxlogix.enums.DateRangeEnum.CUSTOM}">
                            <div>
                                <label><g:message code="app.label.start"/></label>

                                <div><g:renderShortDateFormat
                                        date="${configurationInstance?.dateRangeInformation?.dateRangeStartAbsolute}"/></div>
                                <label><g:message code="app.label.end"/></label>

                                <div><g:renderShortDateFormat
                                        date="${configurationInstance?.dateRangeInformation?.dateRangeEndAbsolute}"/></div>
                            </div>
                        </g:if>
                    </div>

                </div>

            </div>
        </div>



        <div class="row rxDetailsBorder">
            <div class="col-xs-12">
                <label><g:message code="app.signal.view.screen.details"/></label>
            </div>
        </div>

        <div class="row">

            <div class="col-xs-4">
                <label><g:message code="app.label.priority"/></label>
                <g:if test="${configurationInstance.priority && !configurationInstance.adhocRun}">
                    <div>${configurationInstance.priority}</div>
                </g:if>
                <g:else>
                    <div><g:message code="app.label.none"/></div>
                </g:else>
            </div>

            <div class="col-xs-4 word-wrap-break-word">
                <label><g:message code="app.label.assigned.to"/></label>
                <div><g:showAssignedToName bean="${configurationInstance}" /></div>
            </div>

            <div class="col-xs-4 word-wrap-break-word">
                <label><g:message code="app.label.sharedWith.user.groups"/></label>
                <div><g:showSharedWithName bean="${configurationInstance}"/></div>
            </div>
        </div>

    <g:if test="${!configurationInstance.adhocRun}">
        <div class="row rxDetailsBorder">
            <div class="col-xs-12">
                <label><g:message code="report.delivery.option.scheduler"/></label>
            </div>
        </div>

        <div class="row">
            <div class="col-xs-3 fuelux" id="disabledScheduler">
                <g:hiddenField name="isEnabled" id="isEnabled" value="${configurationInstance?.isEnabled}"/>
                <g:if test="${configurationInstance?.isEnabled}">
                    <g:render template="/includes/schedulerTemplate"/>
                    <g:hiddenField name="schedulerTime"
                                   value="${com.rxlogix.util.RelativeDateConverter.getCurrentTimeWRTTimeZone(currentUser)}"/>
                    <g:hiddenField name="scheduleDateJSON"
                                   value="${configurationInstance?.scheduleDateJSON ?: null}"/>
                    <input type="hidden" name="configSelectedTimeZone" id="configSelectedTimeZone"
                           value="${configurationInstance?.configSelectedTimeZone ?: timeZone}"/>
                    <input type="hidden" id="timezoneFromServer" name="timezone"
                           value="${DateUtil.getTimezone(currentUser)}"/>
                </g:if>
                <g:else>
                    <label>
                        <g:message code="app.reportNotScheduled.message"/>
                    </label>
                </g:else>
            </div>
        </div>
    </g:if>

        <g:if test="${!isExecuted}">
            <div class="row">
                <div class="col-xs-12">
                    <div class="pull-right">
                        <g:link controller="evdasAlert" action="edit" id="${params.id}"
                                class="btn btn-primary"><g:message
                                code="default.button.edit.label"/></g:link>
                        <g:link controller="configuration" action="index"
                                class="btn btn-default"><g:message code="default.button.cancel.label"/></g:link>
                    </div>
                </div> 
            </div>
        </g:if>
    </div>

    <g:if test="${viewSql}">
        <div>
            <h3><g:message code="app.executedSql"/></h3>
            <g:each in="${viewSql}">
                <pre>
                    ${viewSql.gttInserts}<br/>
                    ${viewSql.listednessProc}<br/>
                    ${viewSql.queryInserts}<br/>
                    ${viewSql.evdasSql}<br/>
                </pre>
            </g:each>
        </div>
    </g:if>
    <g:hiddenField name="editable" id="editable" value="false"/>

</rx:container>
<g:form controller="${controller}" method="delete">
    <g:render template="/includes/widgets/deleteRecord"/>
</g:form>

</body>
</html>