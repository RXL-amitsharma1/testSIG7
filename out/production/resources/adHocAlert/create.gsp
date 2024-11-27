<%@ page import="grails.util.Holders; com.rxlogix.util.ViewHelper; com.rxlogix.util.DateUtil; com.rxlogix.config.Tag;" %>
<head>
    <meta name="layout" content="main"/>
    <title>Ad-Hoc Alert</title>

    <g:javascript>
        var getSelectedGenericUrl = "${createLink(controller: 'configurationRest', action: 'getSelectedGenericNames')}";
        var searchGenericsUrl = "${createLink(controller: 'configurationRest', action: 'searchGenerics')}";
        var isAdhocAlert = true
         $(document).ready(function () {
            $("#dataSourcesProductDict").closest(".row").hide()
         });
    </g:javascript>
    <asset:javascript src="app/pvs/userGroupSelect.js"/>
    <asset:javascript src="fuelux/fuelux.js"/>
    <asset:stylesheet src="fuelux.css"/>
    <asset:javascript src="app/pvs/adhocEvaluation/adhoc.js"/>
    <asset:javascript src="app/pvs/alert_utils/common_alert_utils.js"/>
    <g:if test="${!Holders.config.pv.plugin.dictionary.enabled}">
        <asset:javascript src="app/pvs/alert_utils/alert_product_utils.js"/>
        <asset:javascript src="app/pvs/alert_utils/alert_study_utils.js"/>
        <asset:javascript src="app/pvs/alert_utils/alert_event_utils.js"/>
    </g:if>
    <g:else>
        <asset:javascript src="app/pvs/alert_utils/dictionary-utils.js"/>
    </g:else>
    <asset:javascript src="app/pvs/groups.js"/>
    <asset:javascript src="app/pvs/configuration/configurationCommon.js"/>
    <asset:javascript src="multiple-file-upload/jquery.MultiFile.js"/>
    <asset:javascript src="app/pvs/configuration/copyPasteValues.js"/>
    <asset:javascript src="app/pvs/configuration/dictionaryMultiSearch.js"/>
    <asset:stylesheet src="copyPasteModal.css"/>
    <asset:stylesheet src="configuration.css"/>
    <g:if test="${!Holders.config.pv.plugin.dictionary.enabled}">
        <asset:stylesheet src="dictionaries.css"/>
    </g:if>


</head>

<body>
    <g:set var="userService" bean="userService"/>

    <g:render template="/includes/layout/flashErrorsDivs" bean="${alertInstance}" var="theInstance"/>

    <g:form id="configurationForm" name="configurationForm" url="[action: 'save', controller: 'adHocAlert']"
            method="post" autocomplete="off" onsubmit="return onFormSubmit()">

        <g:render template="form" model="[alertInstance: alertInstance, alertAttributesService: alertAttributesService, safetyLeadSecurityService: safetyLeadSecurityService,
                                          userService:userService, formulations: formulations, lmReportTypes: lmReportTypes, countryNames: countryNames]"/>

        <g:hiddenField name="schedulerFrom" id="schedulerFrom" value="createPage"/>

            <div class="m-t-15 text-right">

            <g:actionSubmit class="btn btn-primary" id="saveBtn" data-action="save"
                            type="submit" accesskey="s"
                            value="${message(code: 'default.button.save.label')}"/>
            <a class="btn btn-default pv-btn-grey" tabindex="0" accesskey="c"
               href="/signal/">${message(code: "default.button.cancel.label")}</a>
            </div>
    </g:form>
</body>
