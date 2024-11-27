<%@ page import="grails.util.Holders; com.rxlogix.util.ViewHelper; com.rxlogix.util.DateUtil; com.rxlogix.config.Tag;" %>
<%@ page import="com.rxlogix.config.DateRangeValue" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.Edit.Alert.title" args="${[alertInstance.name]}"/></title>
    <g:javascript>
        var editMessage = "${message(code: "app.onlyAdminCreateNewTags.message")}";
        var searchGenericsUrl = "${createLink(controller: 'configurationRest', action: 'searchGenerics')}";
        var isAdhocAlert = true


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

<rx:container title="${message(code: 'app.label.editReport')}">

    <g:render template="/includes/layout/flashErrorsDivs" bean="${alertInstance}" var="theInstance"/>

    <form id="configurationForm" name="configurationForm" method="post" autocomplete="off" onsubmit="return onFormSubmit()">

        <g:render template="form" model="[alertInstance: alertInstance, alertAttributesService: alertAttributesService]"/>
        <g:hiddenField name="editable" id="editable" value="true"/>
        <g:hiddenField name="schedulerFrom" id="schedulerFrom" value="editPage"/>
        <g:hiddenField name="alertId" id="alertId" value="${alertInstance.id}"/>

        %{--BEGIN: Button Bar  ==============================================================================================================--}%
        <div class="pull-right rxmain-container-top">
            <g:actionSubmit accesskey="u" class="btn btn-primary" data-action="${createLink(controller: 'adHocAlert',action: 'update')}" type="submit" value="${message(code: 'default.button.update.label')}"/>
            <g:hiddenField name="id" id="id" value="${params.id}"/>
            <a class="btn btn-default pv-btn-grey" tabindex="0" accesskey="c"
               href="${createLink(controller: 'adHocAlert', action: 'alertDetail', params:[id: alertInstance.id])}">${message(code: "default.button.cancel.label")}</a>
        </div>
    </form>

    <g:render template="/includes/modals/attachments_modal_dialog" model="[alertInst: alertInstance, source: 'edit']"/>
</rx:container>
</body>

