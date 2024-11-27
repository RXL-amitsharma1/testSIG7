<%@ page import="grails.converters.JSON; com.rxlogix.enums.DictionaryTypeEnum; com.rxlogix.enums.DateRangeEnum; com.rxlogix.util.DateUtil; com.rxlogix.config.ExecutedConfiguration; com.sun.xml.internal.bind.v2.TODO; com.rxlogix.util.ViewHelper; com.rxlogix.enums.DateRangeTypeCaseEnum;  com.rxlogix.config.DateRangeValue" %>
<%@ page import="com.rxlogix.enums.EvaluateCaseDateEnum" %>
<g:set var="userService" bean="userService"/>

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

<g:set var="timeZone" value="${userService?.getUser()?.preference?.timeZone}"/>
<g:set var="userService" bean="userService"/>

<g:if test="${executedConfigId}">
    <g:set var="title" value="${message(code: "app.View.executed.page.title", args: [configurationInstance.name])}"/>
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
                    </div>

                    <div class="col-xs-4">
                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.scheduledBy"/></label>

                                <div>${configurationInstance.owner.fullName}</div>
                            </div>
                        </div>
                    </div>

                    <div class="col-xs-4">
                        <div class="row">
                            <div class="col-xs-12">
                                <g:if test="${configurationInstance.productSelection || configurationInstance.productGroupSelection}">
                                    <label><g:message code="app.reportField.productDictionary"/></label>

                                    <g:if test="${configurationInstance.productSelection}">
                                    <div id="showProductSelection"></div>
                                        ${ViewHelper.getProductDictionaryValues(configurationInstance, DictionaryTypeEnum.PRODUCT)}
                                    </g:if>
                                    <g:if test="${configurationInstance.productGroupSelection}">
                                    <div id="productGroupSelection"></div>
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
                                        <div id="showEventSelection"></div>
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
            <div class="col-xs-4">
                <div class="row">
                    <div class="col-xs-12">
                        <label><g:message code="app.label.searchString"/></label>

                        <div>
                            <g:message code="${(configurationInstance?.searchString.encodeAsHTML())}"/>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-xs-4">
                <div class="row">
                    <div class="col-xs-12">
                        <label><g:message code="app.label.DateRange"/></label>
                        <g:if test="${isExecuted}">
                            <g:if test="${configurationInstance?.dateRangeInformation?.dateRangeEnum}">
                                <div>
                                    <g:message
                                            code="${(configurationInstance?.dateRangeInformation?.dateRangeEnum?.i18nKey)}"/>

                                </div>
                                <g:if test="${configurationInstance?.dateRangeInformation?.dateRangeEnum == com.rxlogix.enums.DateRangeEnum.CUSTOM}">
                                    <g:if test="${configurationInstance?.dateRangeInformation?.dateRangeStartAbsolute}">
                                        <div>
                                            <label><g:message code="app.label.start"/></label>

                                            <div><g:formatDate style="Long"
                                                               date="${configurationInstance?.dateRangeInformation?.dateRangeStartAbsolute}"
                                                               type="datetime"/></div>
                                            <label><g:message code="app.label.end"/></label>

                                            <div><g:formatDate style="Long"
                                                               date="${configurationInstance?.dateRangeInformation?.dateRangeEndAbsolute}"
                                                               type="datetime"/></div>
                                        </div>
                                    </g:if>
                                </g:if>
                            </g:if>
                        </g:if>
                        <g:else>
                            <g:if test="${configurationInstance?.dateRangeInformation?.dateRangeEnum}">
                                <div>
                                    <g:message
                                            code="${(configurationInstance?.dateRangeInformation?.dateRangeEnum?.i18nKey)}"/>
                                    <input type="hidden" id="dateRangeValueRelative"
                                           value="${(configurationInstance?.dateRangeInformation?.dateRangeEnum)}"/>
                                    <g:if test="${(DateRangeEnum.getRelativeDateOperatorsWithX().contains(configurationInstance.dateRangeInformation?.dateRangeEnum))}">
                                        <div id="relativeDateRangeValueX">where, X = ${(configurationInstance.dateRangeInformation?.relativeDateRangeValue)}</div>
                                    </g:if>
                                </div>
                                <g:if test="${configurationInstance?.dateRangeInformation?.dateRangeStartAbsolute}">
                                    <div>
                                        <label><g:message code="app.label.start"/></label>

                                        <div><g:formatDate formatName="default.date.format.short"
                                                           date="${configurationInstance?.dateRangeInformation?.dateRangeStartAbsolute}"/></div>
                                        <label><g:message code="app.label.end"/></label>

                                        <div><g:formatDate formatName="default.date.format.short"
                                                           date="${configurationInstance?.dateRangeInformation?.dateRangeEndAbsolute}"/></div>
                                    </div>
                                </g:if>
                            </g:if>
                        </g:else>
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
                <g:if test="${configurationInstance.priority}">
                    <div>${configurationInstance.priority}</div>
                </g:if>
                <g:else>
                    <div><g:message code="app.label.none"/></div>
                </g:else>
            </div>

            <div class="col-xs-4 word-wrap-break-word">
                <label><g:message code="app.label.assigned.to"/></label>

                <div><g:showAssignedToName bean="${configurationInstance}"/></div>
            </div>

            <div class="col-xs-4 word-wrap-break-word">
                <label><g:message code="app.label.sharedWith.user.groups"/></label>

                <div><g:showSharedWithName bean="${configurationInstance}"/></div>
            </div>
        </div>

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
                                   value="${com.rxlogix.util.RelativeDateConverter.getCurrentTimeWRTTimeZone(userService.getUser())}"/>
                    <g:hiddenField name="scheduleDateJSON"
                                   value="${configurationInstance?.scheduleDateJSON ?: null}"/>
                    <input type="hidden" name="configSelectedTimeZone" id="configSelectedTimeZone"
                           value="${configurationInstance?.configSelectedTimeZone ?: userService.getUser?.preference?.timeZone}"/>
                    <input type="hidden" id="timezoneFromServer" name="timezone"
                           value="${DateUtil.getTimezone(userService.getUser())}"/>
                </g:if>
                <g:else>
                    <label>
                        <g:message code="app.reportNotScheduled.message"/>
                    </label>
                </g:else>
            </div>
        </div>


        <div class="row">
            <div class="col-xs-12">
                <div class="pull-right">
                    <g:if test="${isEdit}">
                        <g:link controller="literatureAlert" action="edit" id="${params.id}"
                                class="btn btn-primary"><g:message
                                code="default.button.edit.label"/></g:link>
                    </g:if>
                    <g:link controller="configuration" action="index"
                            class="btn btn-default"><g:message code="default.button.cancel.label"/></g:link>
                    <g:if test="${executedConfigId}">
                        <g:link controller="literatureAlert" action="details" id="${executedConfigId}"
                                class="btn btn-default"><g:message code="default.button.view.label"/></g:link>

                    </g:if>

                </div>
            </div> 
        </div>
    </div>
    <g:hiddenField name="editable" id="editable" value="false"/>

</rx:container>
<g:form controller="${controller}" method="delete">
    <g:render template="/includes/widgets/deleteRecord"/>
</g:form>
</div>

</body>
</html>