<%@ page import="grails.util.Holders; com.rxlogix.util.ViewHelper; com.rxlogix.util.DateUtil; com.rxlogix.config.Tag;" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.new.literature.search.alert"/></title>
    <asset:javascript src="app/pvs/groups.js"/>
    <g:if test="${!Holders.config.pv.plugin.dictionary.enabled}">
        <asset:javascript src="app/pvs/alert_utils/alert_product_utils.js"/>
        <asset:javascript src="app/pvs/alert_utils/alert_study_utils.js"/>
        <asset:javascript src="app/pvs/alert_utils/alert_event_utils.js"/>
    </g:if>
    <g:else>
        <asset:javascript src="app/pvs/alert_utils/dictionary-utils.js"/>
    </g:else>
    <asset:javascript src="app/pvs/scheduler.js"/>
    <asset:javascript src="app/pvs/configuration/configurationCommon.js"/>
    <asset:javascript src="app/pvs/literatureSearch/dateRangeLiterature.js"/>
    <asset:javascript src="app/pvs/disableAutocomplete.js"/>
    <asset:javascript src="app/pvs/configuration/copyPasteValues.js"/>
    <asset:javascript src="app/pvs/configuration/dictionaryMultiSearch.js"/>

    <asset:stylesheet src="copyPasteModal.css"/>
    <asset:stylesheet src="configuration.css"/>
    <g:if test="${!Holders.config.pv.plugin.dictionary.enabled}">
        <asset:stylesheet src="dictionaries.css"/>
    </g:if>


    <g:javascript>
        var editMessage = "${message(code: "app.onlyAdminCreateNewTags.message")}";
        var fetchAllowedUsersUrl = "${createLink(controller: 'configurationRest', action: 'fetchAllowedUsers')}";
        var isValidationError= ${validationError?:false};
        var LABELS = {
            labelShowAdavncedOptions : "${message(code: 'reportCriteria.show.advanced.options')}",
            labelHideAdavncedOptions : "${message(code: 'reportCriteria.hide.advanced.options')}"
        };
        var actionName = "${actionName}";
        var editAlert = "${action}";
        var isValidationError= ${validationError?:false};
        var isAlertScheduled = ${configurationInstance?.nextRunDate? true : false};
         $(document).ready(function () {
            $(".priority-List").find(".select2").select2();
        });
    </g:javascript>
</head>

<body>

<g:set var="userService" bean="userService"/>

<g:render template="/includes/layout/flashErrorsDivs" bean="${configurationInstance}" var="theInstance"/>

<form id="configurationForm" name="configurationForm" method="post" autocomplete="off"
      onsubmit="return onFormSubmit()">

    <g:render template="form"
              model="[configurationInstance: configurationInstance, priorityList: priorityList, userList: userList, action: action, dateMap: dateMap,
                      templateList         : templateList, productGroupList: productGroupList, literatureSearchConfiguration: literatureSearchConfiguration]"/>

    <g:hiddenField name="schedulerFrom" id="schedulerFrom" value="createPage"/>

    <div class="text-right m-t-15">
        <g:actionSubmit accesskey="r" class="btn btn-primary repeat" data-action="${createLink(controller: 'literatureAlert',action: 'run',params: [id:configurationInstance.id] )}" type="submit" value="${message(code: 'default.button.update.run.label')}"/>
        <g:actionSubmit accesskey="u" tabindex="0" class="btn primaryButton btn-primary repeat"
                        data-action="${createLink(controller: 'literatureAlert', action: 'update')}"
                        type="submit"
                        value="${message(code: 'default.button.update.label')}"/>
            <g:hiddenField name="id" id="id" value="${configurationInstance?.id}"/>

            <a aria-label="main navigation" accesskey="c" tabindex="0" class="btn btn-default pv-btn-grey"
               href="${createLink(controller: 'configuration', action: 'index')}">${message(code: "default.button.cancel.label")}</a>
    </div>
    <input type="hidden" id="selectedDatasource" class="selectedDatasource" value="pva"/>
    <input type="hidden" id="dataSource" name="dataSource"/>
    <input name="previousAction" id="previousAction" value="${action}" hidden="hidden"/>
</form>

</body>
