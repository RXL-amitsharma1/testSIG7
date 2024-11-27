<%@ page import="com.rxlogix.pvdictionary.config.PVDictionaryConfig; com.rxlogix.Constants; com.rxlogix.dto.SpotfireSettingsDTO; com.rxlogix.enums.ProductClassification; grails.converters.JSON; com.rxlogix.enums.DictionaryTypeEnum; com.rxlogix.enums.DateRangeEnum; com.rxlogix.util.DateUtil; com.rxlogix.config.ExecutedConfiguration; com.sun.xml.internal.bind.v2.TODO; com.rxlogix.util.ViewHelper; com.rxlogix.enums.DateRangeTypeCaseEnum;  com.rxlogix.config.DateRangeValue; com.rxlogix.enums.DrugTypeEnum ; grails.util.Holders" %>
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
<g:set var="cacheService" bean="cacheService"/>
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
                    <div class="col-xs-3">
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

                        <div class="row">
                            <div class="col-xs-12 cell-break">
                                <g:if test="${configurationInstance.productSelection || configurationInstance.productGroupSelection || configurationInstance.dataMiningVariable}">
                                    <label><g:message code="app.reportField.productDictionary"/></label>
                                    <g:if test="${configurationInstance.dataMiningVariable && configurationInstance.productSelection}">
                                        <div id="showProductSelection"></div>
                                        ${ViewHelper.getProductDictionaryValues(configurationInstance, DictionaryTypeEnum.PRODUCT)}
                                    </g:if>
                                    <g:elseif test="${configurationInstance.dataMiningVariable && configurationInstance.productGroupSelection}">
                                        <div id="showProductSelection"></div>
                                        ${ViewHelper.getDictionaryValues(configurationInstance, DictionaryTypeEnum.PRODUCT_GROUP)}
                                    </g:elseif>
                                    <g:elseif test="${configurationInstance.dataMiningVariable}">
                                        <div id="showProductSelection"></div>
                                        ${configurationInstance.dataMiningVariable}
                                    </g:elseif>
                                    <g:elseif test="${configurationInstance.productSelection}">
                                        <div id="showProductSelection"></div>
                                        ${ViewHelper.getProductDictionaryValues(configurationInstance, DictionaryTypeEnum.PRODUCT)}
                                    </g:elseif>
                                    <g:elseif test="${configurationInstance.productGroupSelection}">
                                        <div id="showProductGroupSelection"></div>
                                        ${ViewHelper.getDictionaryValues(configurationInstance, DictionaryTypeEnum.PRODUCT_GROUP)}
                                    </g:elseif>
                                </g:if>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-xs-12">
                                <g:if test="${isPVCM}">
                                    <label><g:message code="app.label.productDictionary.multi.substance"/></label>
                                </g:if>
                                <g:else>
                                    <label><g:message code="app.label.productDictionary.multi.ingredient"/></label>
                                </g:else>
                                <div id="isMultiSubstance"></div>

                                <g:formatBoolean boolean="${configurationInstance.isMultiIngredient}"
                                                 true="${message(code: "default.button.yes.label")}"
                                                 false="${message(code: "default.button.no.label")}"/>

                            </div>
                        </div>

                        <div class="row">
                            <div class="col-xs-12">
                                <g:if test="${configurationInstance.studySelection}">
                                    <label><g:message code="app.reportField.studyDictionary"/></label>

                                    <div id="showStudySelection"></div>
                                    ${ViewHelper.getDictionaryValues(configurationInstance, DictionaryTypeEnum.STUDY)}
                                </g:if>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="alert.view.label.nonValidatedCases"/></label>

                                <div>
                                    <g:formatBoolean boolean="${configurationInstance.excludeNonValidCases}"
                                                     true="${message(code: "default.button.yes.label")}"
                                                     false="${message(code: "default.button.no.label")}"/>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="col-xs-3">
                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.scheduledBy"/></label>

                                <div>${configurationInstance.owner.fullName}</div>
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

                    <div class="col-xs-3">
                        <div class="row"><label><g:message code="app.label.configuration.on.demand.run"/></label>

                            <div>
                                <g:formatBoolean boolean="${configurationInstance?.adhocRun}"
                                                 true="${message(code: "default.button.yes.label")}"
                                                 false="${message(code: "default.button.no.label")}"/>
                            </div>
                        </div>

                        <div class="row">
                            <label><g:message code="app.signal.data.source"/></label>

                            <div>${dataSource}</div>
                        </div>
                        <div class="row"><label><g:message code="reportCriteria.exclude.follow.up"/></label>

                            <div>
                                <g:formatBoolean boolean="${configurationInstance?.excludeFollowUp}"
                                                 true="${message(code: "default.button.yes.label")}"
                                                 false="${message(code: "default.button.no.label")}"/>
                            </div>
                        </div>

                        <div class="row"><label><g:message code="app.label.selected.datasheets"/></label>
                            <g:if test="${selectedDatasheets}">
                                <div id='selectedDatasheets' class="word-break">
                                    ${selectedDatasheets}
                                </div>
                            </g:if>
                            <g:else>
                               <div id='selectedDatasheets' class="word-break">
                                    -
                               </div>
                            </g:else>
                        </div>
                    </div>

                    <div class="col-xs-3">


                        <div class="row">
                            <div class="col-xs-12">
                                <label>Data Mining based on SMQ/Event group</label>

                                <div>
                                    <g:formatBoolean boolean="${configurationInstance.groupBySmq}"
                                                     true="${message(code: "default.button.yes.label")}"
                                                     false="${message(code: "default.button.no.label")}"/>
                                </div>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.configuration.missed.cases"/></label>

                                <div>
                                    <g:formatBoolean boolean="${configurationInstance.missedCases}"
                                                     true="${message(code: "default.button.yes.label")}"
                                                     false="${message(code: "default.button.no.label")}"/>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-xs-3">
                        <div class="row">
                            <div class="col-xs-12">

                                <label><g:message code="app.label.eventSelection.limit.primary.path"/></label>

                                <div><g:formatBoolean boolean="${configurationInstance?.limitPrimaryPath}"
                                                      true="${message(code: "default.button.yes.label")}"
                                                      false="${message(code: "default.button.no.label")}"/></div>

                            </div>
                        </div>
                    </div>

                    <g:if test="${configurationInstance.drugType}">
                        <div class="col-xs-3">
                            <div class="row">
                                <div class="col-xs-12">
                                    <label><g:message code="productType.signal.productType"/></label>
                                    <div style="word-break: break-all">
                                        <g:message code="${productRuleNames}"/>
                                    </div>
                                </div>
                            </div>
                            <g:if test="${Holders.config.configurations.include.locked.versions.enabled}">
                                <div class="row">
                                    <div class="col-xs-12">
                                    <label><g:message code="reportCriteria.include.locked.versions.only"/></label>

                                    <div><g:formatBoolean boolean="${configurationInstance.includeLockedVersion}"
                                                          true="${message(code: "default.button.yes.label")}"
                                                          false="${message(code: "default.button.no.label")}"/></div>
                                    </div>
                                </div>
                            </g:if>
                        </div>

                    </g:if>
                </div>
            </div>

        </div>


        <div class="row rxDetailsBorder">
            <div class="col-xs-12">
                <label><g:message code="report.criteria.sections"/></label>
            </div>
        </div>

        <div class="row">

            <div class="col-xs-2">
                <div class="row">
                    <div class="col-xs-12 row">
                        <label><g:message code="app.label.queryName"/></label>
                        <g:if test="${isExecuted}">
                            <g:if test="${configurationInstance.executedAlertQueryId}">
                                <div>
                                    <g:link controller="query" action="view" target="blank"
                                            id="${configurationInstance.executedAlertQueryId}">${configurationInstance.alertQueryName}</g:link>
                                </div>
                            </g:if>
                            <g:else>
                                <div><g:message code="app.label.none"/></div>
                            </g:else>
                        </g:if>
                        <g:else>
                            <g:if test="${configurationInstance.alertQueryId}">
                                <div>
                                    <g:link controller="query" action="view" target="blank"
                                            id="${configurationInstance.alertQueryId}">${configurationInstance.alertQueryName}</g:link>
                                </div>
                            </g:if>
                            <g:else>
                                <div><g:message code="app.label.none"/></div>
                            </g:else>
                        </g:else>
                    </div>
                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="app.label.DateRangeType"/></label>
                            <div>
                                <g:message code="${(DateRangeTypeCaseEnum.(configurationInstance.dateRangeType).i18nKey)}"/>
                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="app.label.DateRange"/></label>
                            <g:if test="${isExecuted}">
                                <div>
                                    <g:message
                                            code="${configurationInstance?.executedAlertDateRangeInformation?.dateRangeEnum?.i18nKey}"/>
                                    <input type="hidden" id="dateRangeValueRelative"
                                           value="${(configurationInstance?.executedAlertDateRangeInformation?.dateRangeEnum    )}"/>
                                    <g:if test="${(DateRangeEnum.getRelativeDateOperatorsWithX().contains(configurationInstance.executedAlertDateRangeInformation?.dateRangeEnum))}">
                                        <div id="relativeDateRangeValueX">where, X = ${(configurationInstance.executedAlertDateRangeInformation?.relativeDateRangeValue)}</div>
                                    </g:if>
                                </div>
                                <g:if test="${configurationInstance?.executedAlertDateRangeInformation?.dateRangeEnum == DateRangeEnum.CUSTOM}">
                                    <div>
                                        <label><g:message code="select.start.date"/></label>

                                        <div><g:renderShortDateFormat
                                                date="${configurationInstance?.executedAlertDateRangeInformation?.dateRangeStartAbsolute}"/></div>
                                        <label><g:message code="select.end.date"/></label>

                                        <div><g:renderShortDateFormat
                                                date="${configurationInstance?.executedAlertDateRangeInformation?.dateRangeEndAbsolute}"/></div>
                                    </div>
                                </g:if>
                            </g:if>
                            <g:else>
                                <div>
                                    <g:message
                                            code="${configurationInstance?.alertDateRangeInformation?.dateRangeEnum?.i18nKey}"/>
                                    <input type="hidden" id="dateRangeValueRelative"
                                           value="${(configurationInstance?.alertDateRangeInformation?.dateRangeEnum    )}"/>
                                    <g:if test="${(DateRangeEnum.getRelativeDateOperatorsWithX().contains(configurationInstance.alertDateRangeInformation?.dateRangeEnum))}">
                                        <div id="relativeDateRangeValueX">where, X = ${(configurationInstance.alertDateRangeInformation?.relativeDateRangeValue)}</div>
                                    </g:if>
                                </div>

                                <g:if test="${configurationInstance?.alertDateRangeInformation?.dateRangeEnum == DateRangeEnum.CUSTOM}">
                                    <div>
                                        <label><g:message code="select.start.date"/></label>

                                        <div><g:renderShortDateFormat
                                                date="${configurationInstance?.alertDateRangeInformation?.dateRangeStartAbsolute}"/></div>

                                        <label><g:message code="select.end.date"/></label>

                                        <div><g:renderShortDateFormat
                                                date="${configurationInstance?.alertDateRangeInformation?.dateRangeEndAbsolute}"/></div>
                                    </div>
                                </g:if>

                            </g:else>

                            <label><g:message code="evaluate.on.label"/></label>

                            <div id="evaluateCaseDate"><g:message
                                    code="${(EvaluateCaseDateEnum.(configurationInstance.evaluateDateAs).i18nKey)}"/>
                                <g:if test="${configurationInstance.evaluateDateAs == EvaluateCaseDateEnum.VERSION_ASOF}">
                                    <div>
                                        <g:renderShortDateFormat date="${configurationInstance.asOfVersionDate}"/>
                                    </div>
                                </g:if>

                            </div>
                        </div>
                    </div>

                </div>
            </div>

            <div class="col-xs-2">
                <div class="row">
                    <g:if test="${masterConfig}">
                        <div class="col-xs-12 row">
                                <label>Master Configuration</label>
                                <div>${masterConfig}</div>
                        </div>
                    </g:if>

                    <div class="col-xs-12 row">
                        <label><g:message code="app.label.parameters"/></label>
                        <g:if test="${isExecuted}">
                            <g:if test="${configurationInstance.executedAlertQueryValueLists}">
                                <div class="italic">
                                    <g:message code="app.label.query"/>
                                </div>
                                <g:each in="${configurationInstance.executedAlertQueryValueLists}">
                                    <div>
                                        ${it.queryName}
                                    </div>

                                    <div class="word-wrap-break-word">
                                        <g:each in="${it.parameterValues}">
                                            <div>
                                                <g:if test="${it.hasProperty('reportField')}">
                                                    <g:if test="${cacheService.getRptToUiLabelInfoPvrEn(it.reportField.name)}">
                                                        ${cacheService.getRptToUiLabelInfoPvrEn(it.reportField.name)}
                                                    </g:if>
                                                    <g:else>
                                                        <g:message code="app.reportField.${it.reportField.name}"/>
                                                    </g:else>
                                                    <g:message code="${it.operator.getI18nKey()}"/>
                                                    <g:if test="${it.value}">
                                                        ${it.value}
                                                    </g:if>
                                                    <g:else>
                                                        ${Constants.Commons.ALL.toLowerCase().substring(0, 1).toUpperCase() + Constants.Commons.ALL.toLowerCase().substring(1)}
                                                    </g:else>
                                                </g:if>
                                                <g:else>
                                                    ${it.key} : ${it.value}
                                                </g:else>
                                            </div>
                                        </g:each>
                                    </div>
                                </g:each>
                            </g:if>
                            <g:else>
                                <div><g:message code="app.label.none"/></div>
                            </g:else>
                        </g:if>
                        <g:else>
                            <g:if test="${configurationInstance.alertQueryValueLists}">
                                <div class="italic">
                                    <g:message code="app.label.query"/>
                                </div>
                                <g:each in="${configurationInstance.alertQueryValueLists}">
                                    <div>
                                        ${it.queryName}
                                    </div>

                                    <div class="word-wrap-break-word">
                                        <g:each in="${it.parameterValues}">
                                            <div>
                                                <g:if test="${it.hasProperty('reportField')}">
                                                    <g:if test="${cacheService.getRptToUiLabelInfoPvrEn(it.reportField.name)}">
                                                        ${cacheService.getRptToUiLabelInfoPvrEn(it.reportField.name)}
                                                    </g:if>
                                                    <g:else>
                                                        <g:message code="app.reportField.${it.reportField.name}"/>
                                                    </g:else>
                                                    <g:message code="${it.operator.getI18nKey()}"/>
                                                    <g:if test="${it.value}">
                                                        ${it.value}
                                                    </g:if>
                                                    <g:else>
                                                        ${Constants.Commons.ALL.toLowerCase().substring(0, 1).toUpperCase() + Constants.Commons.ALL.toLowerCase().substring(1)}
                                                    </g:else>
                                                </g:if>
                                                <g:else>
                                                    ${it.key} : ${it.value}
                                                </g:else>
                                            </div>
                                        </g:each>
                                    </div>
                                </g:each>
                            </g:if>
                            <g:else>
                                <div><g:message code="app.label.none"/></div>
                            </g:else>
                        </g:else>
                    </div>
                    <g:if test="${configurationInstance?.selectedDatasource == 'faers'}">
                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="productType.signal.productType"/></label>

                                <div>${productRuleNames}</div>
                            </div>
                        </div>

                    %{-- Removed story/PVS-57996- part for drug classification --}%
                    </g:if>
                    <g:if test="${configurationInstance?.selectedDatasource!='faers' && configurationInstance?.selectedDatasource!='vaers' && configurationInstance?.selectedDatasource!='eudra'}">
                        <g:if test="${isExecuted}">
                            <g:if test="${vaersDateRange}">
                                <div class="row">
                                    <div class="col-xs-12">
                                        <label><g:message code="app.label.vaers.dateRange"/></label>

                                        <div>${vaersDateRange}</div>
                                    </div>
                                </div>
                            </g:if>
                            <g:if test="${faersDateRange}">
                                <div class="row">
                                    <div class="col-xs-12">
                                        <label><g:message code="app.label.faers.dateRange"/></label>

                                        <div>${faersDateRange}</div>
                                    </div>
                                </div>
                            </g:if>
                            <g:if test="${configurationInstance.evdasDateRange}">
                                <div class="row">
                                    <div class="col-xs-12">
                                        <label><g:message code="app.label.evdas.dateRange"/></label>

                                        <div>${configurationInstance.evdasDateRange}</div>
                                    </div>
                                </div>
                            </g:if>
                            <g:if test="${vigibaseDateRange}">
                                <div class="row">
                                    <div class="col-xs-12">
                                        <label><g:message code="app.label.vigibase.dateRange"/></label>

                                        <div>${vigibaseDateRange}</div>
                                    </div>
                                </div>
                            </g:if>
                        </g:if>

                     <g:else>
                        <g:set var="executedConfig" value="${ExecutedConfiguration.findByConfigId(configurationInstance.id,[sort: 'id',order: 'desc'])}"/>
                        <g:if test="${executedConfig}">
                            <g:if test="${faersDateRange}">
                                <div class="row">
                                    <div class="col-xs-12">
                                        <label><g:message code="app.label.faers.dateRange"/></label>

                                        <div>${faersDateRange}</div>
                                    </div>
                                </div>
                            </g:if>
                            <g:if test="${vaersDateRange}">
                                <div class="row">
                                    <div class="col-xs-12">
                                        <label><g:message code="app.label.vaers.dateRange"/></label>

                                        <div>${vaersDateRange}</div>
                                    </div>
                                </div>
                            </g:if>
                            <g:if test="${executedConfig.evdasDateRange}">
                                <div class="row">
                                    <div class="col-xs-12">
                                        <label><g:message code="app.label.evdas.dateRange"/></label>

                                        <div>${executedConfig.evdasDateRange}</div>
                                    </div>
                                </div>
                            </g:if>
                            <g:if test="${vigibaseDateRange}">
                                <div class="row">
                                    <div class="col-xs-12">
                                        <label><g:message code="app.label.vigibase.dateRange"/></label>

                                        <div>${vigibaseDateRange}</div>
                                    </div>
                                </div>
                            </g:if>
                        </g:if>
                    </g:else>
                    </g:if>
                </div>
            </div>
            <g:render template="/includes/viewStratification"/>
        </div>
        <div class="row">
            <br>
            <div class="col-xs-2">
                <div class="row">
                    <div class="col-xs-12 row">
                        <label><g:message code="app.label.foreground.queryName"/></label>
                        <g:if test="${isExecuted}">
                            <g:if test="${configurationInstance.executedAlertForegroundQueryId}">
                                <div>
                                    <g:link controller="query" action="view" target="blank"
                                            id="${configurationInstance.executedAlertForegroundQueryId}">${configurationInstance.alertForegroundQueryName}</g:link>
                                </div>
                            </g:if>
                            <g:else>
                                <div><g:message code="app.label.none"/></div>
                            </g:else>
                        </g:if>
                        <g:else>
                            <g:if test="${configurationInstance.alertForegroundQueryId}">
                                <div>
                                    <g:link controller="query" action="view" target="blank"
                                            id="${configurationInstance.alertForegroundQueryId}">${configurationInstance.alertForegroundQueryName}</g:link>
                                </div>
                            </g:if>
                            <g:else>
                                <div><g:message code="app.label.none"/></div>
                            </g:else>
                        </g:else>
                    </div>
                   </div>
            </div>
            <div class="col-xs-2">
                <div class="row">

                    <div class="col-xs-12 row">
                        <label><g:message code="app.label.foreground.queryParameters"/></label>
                        <g:if test="${isExecuted}">
                            <g:if test="${configurationInstance.executedAlertForegroundQueryValueLists}">
                                <div class="italic">
                                    <g:message code="app.label.query"/>
                                </div>
                                <g:each in="${configurationInstance.executedAlertForegroundQueryValueLists}">
                                    <div>
                                        ${it.queryName}
                                    </div>

                                    <div class="word-wrap-break-word">
                                        <g:if test="${it.parameterValues}">
                                            <g:each in="${it.parameterValues}">
                                                <div>
                                                    <g:if test="${it.hasProperty('reportField')}">
                                                        <g:if test="${cacheService.getRptToUiLabelInfoPvrEn(it.reportField.name)}">
                                                            ${cacheService.getRptToUiLabelInfoPvrEn(it.reportField.name)}
                                                        </g:if>
                                                        <g:else>
                                                            <g:message code="app.reportField.${it.reportField.name}"/>
                                                        </g:else>
                                                        <g:message code="${it.operator.getI18nKey()}"/>
                                                        <g:if test="${it.value}">
                                                            ${it.value}
                                                        </g:if>
                                                        <g:else>
                                                            ${Constants.Commons.ALL.toLowerCase().substring(0, 1).toUpperCase() + Constants.Commons.ALL.toLowerCase().substring(1)}
                                                        </g:else>
                                                    </g:if>
                                                    <g:else>
                                                        ${it.key} : ${it.value}
                                                    </g:else>
                                                </div>
                                            </g:each>
                                            <g:if test="${configurationInstance.foregroundSearch}">
                                                <g:each in="${JSON.parse(configurationInstance.foregroundSearchAttr)}">
                                                    <g:if test="${it.val}">
                                                        ${it.label+Constants.Commons.SPACE+Constants.AdvancedFilter.EQUALS.toLowerCase().substring(0, 1).toUpperCase() + Constants.AdvancedFilter.EQUALS.toLowerCase().substring(1)+Constants.Commons.SPACE+it.text}<br>
                                                    </g:if>
                                                </g:each>
                                            </g:if>
                                        </g:if>
                                        <g:else>
                                                ${Constants.Commons.ALL.toLowerCase().substring(0, 1).toUpperCase() + Constants.Commons.ALL.toLowerCase().substring(1)}
                                        </g:else>
                                    </div>
                                </g:each>
                            </g:if>
                            <g:else>
                                <g:if test="${configurationInstance.foregroundSearch}">
                                    <g:each in="${JSON.parse(configurationInstance.foregroundSearchAttr)}">
                                        <g:if test="${it.val}">
                                            <div>${it.label+Constants.Commons.SPACE+Constants.AdvancedFilter.EQUALS.toLowerCase().substring(0, 1).toUpperCase() + Constants.AdvancedFilter.EQUALS.toLowerCase().substring(1)+Constants.Commons.SPACE+it.text}</div>
                                        </g:if>
                                    </g:each>
                                </g:if>
                                <g:else>
                                    <div><g:message code="app.label.none"/></div>
                                </g:else>
                            </g:else>
                        </g:if>
                        <g:else>
                            <g:if test="${configurationInstance.alertForegroundQueryValueLists}">
                                <div class="italic">
                                    <g:message code="app.label.query"/>
                                </div>
                                <g:each in="${configurationInstance.alertForegroundQueryValueLists}">
                                    <div>
                                        ${it.queryName}
                                    </div>
                                    <div class="word-wrap-break-word">
                                        <g:if test="${it.parameterValues}">
                                            <g:each in="${it.parameterValues}">
                                                <div>
                                                    <g:if test="${it.hasProperty('reportField')}">
                                                        <g:if test="${cacheService.getRptToUiLabelInfoPvrEn(it.reportField.name)}">
                                                            ${cacheService.getRptToUiLabelInfoPvrEn(it.reportField.name)}
                                                        </g:if>
                                                        <g:else>
                                                            <g:message code="app.reportField.${it.reportField.name}"/>
                                                        </g:else>
                                                        <g:message code="${it.operator.getI18nKey()}"/>
                                                        <g:if test="${it.value}">
                                                            ${it.value}
                                                        </g:if>
                                                        <g:else>
                                                            ${Constants.Commons.ALL.toLowerCase().substring(0, 1).toUpperCase() + Constants.Commons.ALL.toLowerCase().substring(1)}
                                                        </g:else>
                                                    </g:if>
                                                    <g:else>
                                                        ${it.key} : ${it.value}
                                                    </g:else>
                                                </div>
                                            </g:each>
                                            <g:if test="${configurationInstance.foregroundSearch}">
                                                <g:each in="${JSON.parse(configurationInstance.foregroundSearchAttr)}">
                                                    <g:if test="${it.val}">
                                                        ${it.label+Constants.Commons.SPACE+Constants.AdvancedFilter.EQUALS.toLowerCase().substring(0, 1).toUpperCase() + Constants.AdvancedFilter.EQUALS.toLowerCase().substring(1)+Constants.Commons.SPACE+it.text}<br>
                                                    </g:if>
                                                </g:each>
                                            </g:if>
                                        </g:if>
                                        <g:else>
                                            ${Constants.Commons.ALL.toLowerCase().substring(0, 1).toUpperCase() + Constants.Commons.ALL.toLowerCase().substring(1)}
                                        </g:else>
                                    </div>
                                </g:each>
                            </g:if>
                            <g:else>
                                <g:if test="${configurationInstance.foregroundSearch}">
                                    <g:each in="${JSON.parse(configurationInstance.foregroundSearchAttr)}">
                                        <g:if test="${it.val}">
                                            <div>${it.label+Constants.Commons.SPACE+Constants.AdvancedFilter.EQUALS.toLowerCase().substring(0, 1).toUpperCase() + Constants.AdvancedFilter.EQUALS.toLowerCase().substring(1)+Constants.Commons.SPACE+it.text}</div>
                                        </g:if>
                                    </g:each>
                                </g:if>
                                <g:else>
                                    <div><g:message code="app.label.none"/></div>
                                </g:else>
                            </g:else>
                        </g:else>
                    </div>
               </div>
            </div>

        </div>
        </div>
    <g:if test="${configurationInstance?.selectedDatasource != 'jader'}">
        <div class="row rxDetailsBorder">
        <div class="col-xs-12">
            <label><g:message code="app.label.advancedOptions"/></label>
        </div>
    </div>
            <g:if test="${configurationInstance.adhocRun && configurationInstance.dataMiningVariable!=null && !configurationInstance.dataMiningVariable.equals("")}">
    <div class="row">
        <div class="col-xs-2">
            <label><g:message code="app.label.data.mining.variables1"/></label>
            <g:if test="${configurationInstance.dataMiningVariable}">
                <div>${configurationInstance.dataMiningVariable}</div>
            </g:if>
            <g:else>
                <div><g:message code="app.label.all"/></div>
            </g:else>
        </div>

        <div class="col-xs-10 word-wrap-break-word">
            <label><g:message code="app.label.values"/></label>
            <g:set var="all"
                   value="${Constants.Commons.ALL.toLowerCase().substring(0, 1).toUpperCase() + Constants.Commons.ALL.toLowerCase().substring(1)}"/>
            <g:set var="equals"
                   value="${Constants.AdvancedFilter.EQUALS.toLowerCase().substring(0, 1).toUpperCase() + Constants.AdvancedFilter.EQUALS.toLowerCase().substring(1)}"/>
            <g:set var="dmv" value="${configurationInstance.dataMiningVariable}"></g:set>
            <g:set var="dmvValue" value="${configurationInstance.dataMiningVariableValue}"></g:set>
            <g:if test="${dmvValue}">
                <g:set var="operator" value="${JSON.parse(dmvValue)?.operatorDisplay}"></g:set>
                <g:set var="value" value="${JSON.parse(dmvValue)?.value}"></g:set>
            </g:if>

            <div>
                <g:if test="${dmv == null}">
                    ${all}
                </g:if>
                <g:elseif test="${dmv}">
                    <g:if test="${!value && operator}">
                        ${dmv + ' ' + operator + ' ' + all}
                    </g:if>
                    <g:elseif test="${!value && operator}">
                        ${dmv + ' ' + equals + ' ' + all}
                    </g:elseif>
                    <g:elseif test="${value && operator}">
                        ${dmv + ' ' + operator + ' ' + value}
                    </g:elseif>
                    <g:else>
                        ${dmv + ' ' + equals + ' ' + all}
                    </g:else>
                </g:elseif>
            </div>
        </div>
    </div>

    </g:if>

            <g:if test="${configurationInstance?.selectedDatasource != 'vigibase'}">
                <div class="row" style="margin-top: 5px;">
                    <div class="col-xs-12">
                        <label><g:message code="advanced.option.background.product"/></label>
                        <div>
                            <g:formatBoolean boolean="${configurationInstance.isProductMining}"
                                             true="${message(code: "default.button.yes.label")}"
                                             false="${message(code: "default.button.no.label")}"/>
                        </div>
                    </div>
                </div>
                <g:if test="${com.rxlogix.pvdictionary.config.PVDictionaryConfig.ProductConfig.filters.size() > 0}">
                    <div class="row" style="margin-top: 5px;">
                        <div class="col-xs-12">
                            <label><g:message code="advanced.option.foreground.product"/></label>

                            <div>
                                <g:if test="${configurationInstance.foregroundSearch == null}">
                                    <g:message code="default.button.no.label"/>
                                </g:if>
                                <g:else>
                                    <g:formatBoolean boolean="${configurationInstance.foregroundSearch}"
                                                     true="${message(code: "default.button.yes.label")}"
                                                     false="${message(code: "default.button.no.label")}"/>
                                </g:else>
                            </div>
                        </div>
                    </div>
                </g:if>
            </g:if>



    <g:if test="${templateQueries || configurationInstance?.spotfireSettings}">
        <div class="row rxDetailsBorder">
            <div class="col-xs-12">
                <label><g:message code="report.criteria.and.sections"/></label>
            </div>
        </div>
    </g:if>

        <g:each var="templateQuery" in="${templateQueries}">
            <div class="row">
                <div class="col-xs-3">
                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="app.label.templateName"/></label>

                            <div>
                                <div>
                                    <g:if test="${isExecuted}">
                                        <g:link controller="template" action="view"
                                                id="${templateQuery.executedTemplate}">${templateQuery.executedTemplateName}</g:link>
                                    </g:if>
                                    <g:else>
                                        <g:link controller="template" action="view"
                                                id="${templateQuery.template}">${templateQuery.templateName}</g:link>
                                    </g:else>
                                </div>
                            </div>
                        </div>
                    </div>
                    <g:if test="${templateQuery.privacyProtected}">
                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="templateQuery.privacyProtected.label"/></label>

                                <div>
                                    <g:formatBoolean boolean="${templateQuery.privacyProtected}" true="Yes"
                                                     false="No"/>
                                </div>
                            </div>
                        </div>
                    </g:if>
                    <g:if test="${templateQuery.blindProtected}">
                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="templateQuery.blindProtected.label"/></label>

                                <div>
                                    <g:formatBoolean boolean="${templateQuery.blindProtected}" true="Yes"
                                                     false="No"/>
                                </div>
                            </div>
                        </div>
                    </g:if>
                </div>

                <div class="col-xs-3">
                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="app.label.queryName"/></label>
                            <g:if test="${isExecuted}">
                                <g:if test="${templateQuery.executedQuery}">
                                    <div>
                                        <g:link controller="query" action="view"
                                                id="${templateQuery.executedQuery}">${templateQuery.executedQueryName}</g:link>
                                    </div>
                                </g:if>
                                <g:else>
                                    <div>
                                        <g:message code="app.label.none"/>
                                    </div>
                                </g:else>
                            </g:if>
                            <g:else>
                                <g:if test="${templateQuery.query}">
                                    <div>
                                        <g:link controller="query" action="view"
                                                id="${templateQuery.query}">${templateQuery.queryName}</g:link>
                                    </div>
                                </g:if>
                                <g:else>
                                    <div>
                                        <g:message code="app.label.none"/>
                                    </div>
                                </g:else>
                            </g:else>
                        </div>
                    </div>
                </div>

                <div class="col-xs-3">
                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="app.label.DateRange"/></label>
                            <g:if test="${isExecuted}">
                                <div>
                                    <g:message
                                            code="${(templateQuery?.executedDateRangeInformationForTemplateQuery?.dateRangeEnum?.i18nKey)}"/>
                                    <input type="hidden" id="dateRangeValueRelative"
                                           value="${(templateQuery?.executedDateRangeInformationForTemplateQuery?.dateRangeEnum)}"/>
                                    <g:if test="${(DateRangeEnum.getRelativeDateOperatorsWithX().contains(templateQuery?.executedDateRangeInformationForTemplateQuery?.dateRangeEnum))}">
                                        <div id="relativeDateRangeValueX">where, X = ${(templateQuery?.executedDateRangeInformationForTemplateQuery?.relativeDateRangeValue)}</div>
                                    </g:if>
                                </div>

                                <g:if test="${templateQuery.executedDateRangeInformationForTemplateQuery?.dateRangeStartAbsolute && templateQuery?.executedDateRangeInformationForTemplateQuery?.dateRangeEnum != com.rxlogix.enums.DateRangeEnum.CUMULATIVE}">
                                    <div>
                                        <label><g:message code="select.start.date"/></label>

                                        <div><g:renderShortFormattedDate
                                                date="${templateQuery.executedDateRangeInformationForTemplateQuery?.dateRangeStartAbsolute}"/></div>
                                        <label><g:message code="select.end.date"/></label>

                                        <div><g:renderShortFormattedDate
                                                date="${templateQuery.executedDateRangeInformationForTemplateQuery?.dateRangeEndAbsolute}"/></div>
                                    </div>
                                </g:if>
                            </g:if>
                            <g:else>
                                <div>
                                    <g:message
                                            code="${(templateQuery?.dateRangeInformationForTemplateQuery?.dateRangeEnum?.i18nKey)}"/>
                                    <input type="hidden" id="dateRangeValueRelative"
                                           value="${(templateQuery?.dateRangeInformationForTemplateQuery?.dateRangeEnum)}"/>
                                    <g:if test="${(DateRangeEnum.getRelativeDateOperatorsWithX().contains(templateQuery?.dateRangeInformationForTemplateQuery?.dateRangeEnum))}">
                                        <div id="relativeDateRangeValueX">where, X = ${(templateQuery?.dateRangeInformationForTemplateQuery?.relativeDateRangeValue)}</div>
                                    </g:if>
                                </div>

                                <g:if test="${templateQuery.dateRangeInformationForTemplateQuery?.dateRangeStartAbsolute}">
                                    <div>
                                        <label><g:message code="select.start.date"/></label>

                                        <div><g:renderShortFormattedDate
                                                date="${templateQuery.dateRangeInformationForTemplateQuery?.dateRangeStartAbsolute}"/></div>
                                        <label><g:message code="select.end.date"/></label>

                                        <div><g:renderShortFormattedDate
                                                date="${templateQuery.dateRangeInformationForTemplateQuery?.dateRangeEndAbsolute}"/></div>
                                    </div>
                                </g:if>
                            </g:else>
                            <label><g:message code="app.label.queryLevel"/></label>

                            <div id="queryLevel">
                                <div><g:message code="${templateQuery.queryLevel.i18nKey}"/></div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-xs-3">
                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="app.label.parameters"/></label>

                            <g:if test="${isExecuted}">
                                <g:if test="${templateQuery.executedTemplateValueLists}">
                                    <div class="italic">
                                        <g:message code="app.label.template"/>
                                    </div>
                                    <g:each in="${templateQuery.executedTemplateValueLists}">
                                        <div>
                                            ${it.templateName}
                                        </div>

                                        <div>
                                            <g:each in="${it.parameterValues}">
                                                <div class="left-indent">
                                                    ${it.key} = ${it.value}
                                                </div>
                                            </g:each>
                                        </div>
                                    </g:each>
                                </g:if>

                                <g:if test="${templateQuery.executedQueryValueLists}">
                                    <div class="italic">
                                        <g:message code="app.label.query"/>
                                    </div>
                                    <g:each in="${templateQuery.executedQueryValueLists}">
                                        <div>
                                            ${it.queryName}
                                        </div>

                                        <div>
                                            <g:each in="${it.parameterValues}">
                                                <div class="left-indent">
                                                    <g:if test="${it.hasProperty('reportField')}">
                                                        <g:if test="${cacheService.getRptToUiLabelInfoPvrEn(it.reportField.name)}">
                                                            ${cacheService.getRptToUiLabelInfoPvrEn(it.reportField.name)}
                                                        </g:if>
                                                        <g:else>
                                                            <g:message code="app.reportField.${it.reportField.name}"/>
                                                        </g:else>
                                                        <g:message code="${it.operator.getI18nKey()}"/>
                                                        ${it.value?.tokenize(';')?.join(', ')}
                                                    </g:if>
                                                    <g:else>
                                                        ${it.key} = ${it.value}
                                                    </g:else>
                                                </div>
                                            </g:each>
                                        </div>
                                    </g:each>
                                </g:if>

                                <g:if test="${!templateQuery.executedTemplateValueLists && !templateQuery.executedQueryValueLists}">
                                    <div><g:message code="app.label.none"/></div>
                                </g:if>
                            </g:if>

                            <g:else>

                                <g:if test="${templateQuery.templateValueLists}">
                                    <div class="italic">
                                        <g:message code="app.label.template"/>
                                    </div>
                                    <g:each in="${templateQuery.templateValueLists}">
                                        <div>
                                            ${it.templateName}
                                        </div>

                                        <div>
                                            <g:each in="${it.parameterValues}">
                                                <div>
                                                    ${it.key} : ${it.value}
                                                </div>
                                            </g:each>
                                        </div>
                                    </g:each>
                                </g:if>

                                <g:if test="${templateQuery.queryValueLists}">
                                    <div class="italic">
                                        <g:message code="app.label.query"/>
                                    </div>
                                    <g:each in="${templateQuery.queryValueLists}">
                                        <div>
                                            ${it.queryName}
                                        </div>

                                        <div>
                                            <g:each in="${it.parameterValues}">
                                                <div>
                                                    <g:if test="${it.hasProperty('reportField')}">
                                                        <g:if test="${cacheService.getRptToUiLabelInfoPvrEn(it.reportField.name)}">
                                                            ${cacheService.getRptToUiLabelInfoPvrEn(it.reportField.name)}
                                                        </g:if>
                                                        <g:else>
                                                            <g:message code="app.reportField.${it.reportField.name}"/>
                                                        </g:else>
                                                        <g:message code="${it.operator.getI18nKey()}"/>
                                                        ${it.value?.tokenize(';')?.join(', ')}
                                                    </g:if>
                                                    <g:else>
                                                        ${it.key} : ${it.value}
                                                    </g:else>
                                                </div>
                                            </g:each>
                                        </div>
                                    </g:each>
                                </g:if>
                                <g:if test="${!templateQuery.templateValueLists && !templateQuery.queryValueLists}">
                                    <div><g:message code="app.label.none"/></div>
                                </g:if>
                            </g:else>
                        </div>
                    </div>
                </div>
            </div>
        </g:each>
        <g:if test="${configurationInstance?.spotfireSettings}">
            <g:set var="spotfireSettings" value="${configurationInstance?.spotfireSettings?SpotfireSettingsDTO.fromJson(configurationInstance?.spotfireSettings):new SpotfireSettingsDTO()}"/>
            <div class="row spotfire">
                <div class="col-md-12 ">
                    <div class="row">
                        <div class="col-md-3">
                            <label>
                                <g:message code="app.label.spotfire.generateAnalysis"/>
                            </label>
                            <div>
                                <g:if test="${spotfireSettings?.type}">
                                    ${message(code: "default.button.yes.label")}
                                </g:if>
                            </div>
                        </div>

                        <div class="col-md-3 ">
                            <div class="row">
                                <label><g:message code="app.label.spotfire.type"/></label>

                                <div>
                                    <g:if test="${spotfireSettings?.type == ProductClassification.DRUG}">
                                        <g:message code="app.label.spotfire.type.drag"/>
                                    </g:if>
                                    <g:else>
                                        <g:message code="app.label.spotfire.type.vacc"/>
                                    </g:else>
                                </div>
                            </div>
                        </div>

                        <div class="col-md-3">
                            <div class="row">
                                <label><g:message code="app.label.DateRange"/></label>

                                <div>
                                    <g:set var="spotfireDateRange"
                                           value="${ViewHelper.getNewDateRangeReportSection().findAll {
                                               it.name in spotfireSettings?.rangeType
                                           }?.display}"/>
                                    ${spotfireDateRange.join(", ")}
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </g:if>
    </g:if>
        <div class="row rxDetailsBorder">
            <div class="col-xs-12">
                <label><g:message code="app.signal.view.screen.details"/></label>
            </div>
        </div>

        <div class="row">

            <div class="col-xs-3 word-wrap-break-word">
                <label><g:message code="app.label.priority"/></label>
                <g:if test="${configurationInstance.priority && !configurationInstance.adhocRun}">
                    <div>${configurationInstance.priority}</div>
                </g:if>
                <g:else>
                    <div><g:message code="app.label.none"/></div>
                </g:else>
            </div>

            <div class="col-xs-3 word-wrap-break-word">
                <label><g:message code="app.label.assigned.to" /></label><br/>
                <g:showAssignedToName bean="${configurationInstance}"/>
            </div>

            <div class="col-xs-3 word-wrap-break-word">
                <label><g:message code="app.label.sharedWith.user.groups"/></label>
                <div><g:showSharedWithName bean="${configurationInstance}"/></div>
            </div>
            <g:if test="${!configurationInstance.adhocRun}">
                <div class="col-xs-3">
                    <label><g:message code="app.label.templateAlert"/></label>
                    <div>
                        <g:formatBoolean boolean="${configurationInstance.isTemplateAlert}"
                                         true="${message(code: "default.button.yes.label")}"
                                         false="${message(code: "default.button.no.label")}"/>
                    </div>
                </div>
            </g:if>
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
                        <g:if test="${isEdit}">
                            <g:link controller="aggregateCaseAlert" action="edit" id="${params.id}"
                                    class="btn btn-primary"><g:message
                                    code="default.button.edit.label"/></g:link>
                        </g:if>
                        <g:link controller="configuration" action="index"
                                class="btn btn-default"><g:message code="default.button.cancel.label"/></g:link>
                        <g:if test="${isExecuted}">
                            <g:link controller="aggregateCaseAlert" action="details" id="${executedConfigId}"
                                    class="btn btn-default"><g:message code="default.button.view.label"/></g:link>

                        </g:if>

                    </div>
                </div> 
            </div>
        </g:if>

    </div>

    <g:if test="${viewSql}">
        <div>
            <g:each in="${viewSql}">
                <pre>
                    <div>${it.gttInsertSql}</div><br>

                    <div>${it.caseListInsertSql}</div><br>

                    <div>${it.gttInitializeMissedCasesSql}</div><br>

                    <div>${it.gttInitializeSql}</div><br>

                    <div>${it.stopListSql}</div><br>

                    <div>${it.listednessProc}</div><br>

                    <div>${it.caseSeriesSql}</div><br>

                    <div>${it.aggDataMiningSql}</div><br>

                    <div>${it.sqlResultSql}</div><br>
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
