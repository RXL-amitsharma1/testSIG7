<%@ page import="grails.util.Holders; com.rxlogix.AlertAttributesService ;com.rxlogix.config.Disposition" contentType="text/html;charset=UTF-8" %>
<g:set var="alertAttributesService" bean="alertAttributesService"/>

<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.label.createsignal"/></title>
    <asset:stylesheet src="configuration.css"/>
    <asset:javascript src="fuelux/fuelux.js"/>
    <asset:stylesheet src="fuelux.css"/>
    <g:javascript>
        var validationDateSynchingEnabled="${grailsApplication.config.detectedDateAndValidationDate.synch.enabled}"
    </g:javascript>
    <asset:javascript src="app/pvs/validated_signal/validated_signal_create.js"/>
    <asset:javascript src="app/pvs/userGroupSelect.js"/>
    <g:if test="${!Holders.config.pv.plugin.dictionary.enabled}">
        <asset:javascript src="app/pvs/alert_utils/alert_product_utils.js"/>
        <asset:javascript src="app/pvs/alert_utils/alert_event_utils.js"/>
        <asset:stylesheet src="dictionaries.css"/>

    </g:if>
    <g:else>
        <asset:javascript src="app/pvs/alert_utils/dictionary-utils.js"/>
    </g:else>
    <asset:javascript src="app/pvs/configuration/copyPasteValues.js"/>
    <asset:javascript src="app/pvs/configuration/dictionaryMultiSearch.js"/>
    <asset:stylesheet src="copyPasteModal.css"/>
    <g:javascript>
        var fetchAttachmentsUrl = "${createLink(controller: 'validatedSignal', action: 'fetchAttachments')}";
        var searchStrategyProducts = "${createLink(controller: 'validatedSignal', action: 'searchStrategyProducts')}"
        var linkedSignalDetailUrl="${createLink(controller: 'validatedSignal', action: 'details')}";
        var disabledValues = {
            initialDataSource: "${alertAttributesService.getDisabled('initialDataSource')}",
            detectedBy       : "${alertAttributesService.getDisabled('detectedBy')}",
            actionTaken      : "${alertAttributesService.getDisabled('actionsTaken')}",
            evaluationMethod : "${alertAttributesService.getDisabled('evaluationMethods')}",
            signalOutcome    : "${alertAttributesService.getDisabled('signalOutcome')}",
            signalTypeList   : "${alertAttributesService.getDisabled('topicCategory')}",
            signalStatus     : "${alertAttributesService.getDisabled('signalHistoryStatus')}",
            haSignalStatus   : "${Disposition.findAllByDisplay(false)*.id}"
        }
    </g:javascript>
</head>

<body>
<g:render template="/includes/layout/flashErrorsDivs" bean="${validatedSignal}" var="theInstance"/>

<g:form method="post" autocomplete="off" action="save" name="saveSignal">

    <div class="rxmain-container ">
        <div class="rxmain-container-inner">
            <div class="rxmain-container-row rxmain-container-header">
                <label class="rxmain-container-header-label">
                    <g:message code="app.signal.summary"/>
                </label>
            </div>

            <div class="rxmain-container-content">
                <g:render template="signalConfiguration"
                          model="[validatedSignal: validatedSignal, initialDataSource: initialDataSource.findAll{ it!=null }, signalOutcomes: signalOutcomes,
                                  priorityList   : priorityList, userList: userList, actionTakenList: actionTakenList, signalTypeList: signalTypeList, linkedSignals: linkedSignals, timezone: timezone]"/>
            </div>
        </div>
    </div>
    <br/>

    <g:if test="${showDetectedBy || showTopicInformation || showAggregateDate || showShareWith}">
        <div class="rxmain-container ">
            <div class="rxmain-container-inner">
                <div class="rxmain-container-row rxmain-container-header">
                    <label class="rxmain-container-header-label">
                        <g:message code="signal.details.label"/>
                    </label>
                </div>

                <div class="rxmain-container-content">
                    <g:render template="signalDetails"
                              model="[validatedSignal: validatedSignal, timezone: timezone]"/>
                </div>
            </div>
        </div>
    </g:if>

    <g:if test="${showHealthAuthority}">
        <div class="rxmain-container ">
            <div class="rxmain-container-inner">
                <div class="rxmain-container-row rxmain-container-header">
                    <label class="rxmain-container-header-label">
                        <g:message code="app.header.label.ha.signal.status"/>
                    </label>
                </div>

                <div class="rxmain-container-content">
                    <g:render template="includes/ha_signal_status_create"
                              model="[validatedSignal: validatedSignal, haSignalStatusList: haSignalStatusList, timezone: timezone]"/>
                </div>
            </div>
        </div>
    </g:if>

    <div class="m-t-15 text-right">
        <g:actionSubmit class="btn  btn-primary m-r-15" action="save" type="submit" id="btnSave"
                        value="${message(code: 'default.button.save.label')}"/>
        <g:actionSubmit class="btn btn-default pv-btn-grey" action="index" type="submit"
                        value="${message(code: 'default.button.cancel.label')}"/>
    </div>
</g:form>
<g:if test="${Holders.config.pv.plugin.dictionary.enabled}">
    <g:render template="/configuration/copyPasteModal"/>
    <input type="hidden" id="editable" value="true">
    <g:render template="/plugin/dictionary/dictionaryModals" plugin="pv-dictionary"
              model="[filtersMapList: Holders.config.product.dictionary.filtersMapList, viewsMapList: Holders.config.product.dictionary.viewsMapList, isPVCM:isPVCM,multiIngredientValue: validatedSignal?.isMultiIngredient]"/>
</g:if>
<g:render template="/validatedSignal/includes/extendedTextarea"/>
</body>
</html>
