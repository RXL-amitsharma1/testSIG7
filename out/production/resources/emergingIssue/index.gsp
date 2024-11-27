<%@ page import="grails.util.Holders; com.rxlogix.config.AlertStopList; com.rxlogix.pvdictionary.config.PVDictionaryConfig" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.impEvents.title"/></title>
    <asset:javascript src="app/pvs/validated_signal/assessment_dictionary.js"/>
    <g:render template="/includes/modals/extendedImportantEvents"/>
    <g:javascript>
        var isProductAssignment = false;
        var isEmerging = true;
        var isMultipleDataSource=true;
        var hasConfigurationEditorRole = "${hasConfigurationEditorRole}";
        var editMessage = "${message(code: "app.onlyAdminCreateNewTags.message")}";
        var getSelectedEventUrl = "${createLink(controller: 'configurationRest', action: 'getSelectedEvent')}";
        var getPreLevelEventParentsUrl = "${createLink(controller: 'configurationRest', action: 'getPreLevelEventParents')}";
        var searchEventsUrl = "${createLink(controller: 'configurationRest', action: 'searchEvents')}";

        var getSelectedProductUrl = "${createLink(controller: 'configurationRest', action: 'getSelectedProduct')}";
        var getPreLevelProductParentsUrl = "${createLink(controller: 'configurationRest', action: 'getPreLevelProductParents')}";
        var searchProductsUrl = "${createLink(controller: 'configurationRest', action: 'searchProducts')}";

        var getSelectedStudyUrl = "${createLink(controller: 'configurationRest', action: 'getSelectedStudy')}";
        var getPreLevelStudyParentsUrl = "${createLink(controller: 'configurationRest', action: 'getPreLevelStudyParents')}";
        var searchStudiesUrl = "${createLink(controller: 'configurationRest', action: 'searchStudies')}";

        var getSelectedGenericUrl = "${createLink(controller: 'configurationRest', action: 'getSelectedGenericNames')}";
        var searchGenericsUrl = "${createLink(controller: 'configurationRest', action: 'searchGenerics')}";
        var stopListUrl = "${createLink(controller: 'emergingIssue', action: 'list')}";
        var saveStopListUrl = "${createLink(controller: 'alertStopList', action: 'saveStopList')}";
        var updateListUrl = "${createLink(controller: 'emergingIssue', action: 'update')}";
        var indexUrl = "${createLink(controller: 'emergingIssue', action: 'index')}";
        var deleteImportantIssuesUrl = "${createLink(controller: 'emergingIssue', action: 'delete')}";
        var editImportantIssuesUrl = "${createLink(controller: 'emergingIssue', action: 'edit')}";
        var isAdmin = ${grails.plugin.springsecurity.SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_CONFIGURATION_CRUD")};
        var dataSourcesColorMapValues = '{"faers": "rgb(112, 193, 179)", "eudra": "rgb(157, 129, 137)", "vaers": "rgb(92, 184, 92)", "vigibase": "rgb(255, 87, 51)"}';
        var options = { spinnerPath:"${assetPath(src: 'select2-spinner.gif')}" };
        var isMultiIngredientCheck = ${emergingIusseList?.isMultiIngredient}
        options.product = {
            levelNames: "${PVDictionaryConfig.ProductConfig.views.collect { message(code: it.code) }.join(",")}",
            dicColumnCount: ${com.rxlogix.pvdictionary.config.PVDictionaryConfig.ProductConfig.columns.size()},
            selectUrl: "${createLink(controller: 'productDictionary', action: 'getSelectedItem')}",
            preLevelParentsUrl: "${createLink(controller: 'productDictionary', action: 'getPreLevelProductParents')}",
            searchUrl: "${createLink(controller: 'productDictionary', action: 'searchViews')}"
        };
        intializeDictionariesAssessment(options);

    </g:javascript>
    <asset:stylesheet src="configuration.css"/>
    <sec:ifAnyGranted roles="ROLE_CONFIGURATION_CRUD">
        <g:if test="${!Holders.config.pv.plugin.dictionary.enabled}">
            <asset:javascript src="app/pvs/alert_utils/alert_event_utils.js"/>
        </g:if>
        <g:else>
            <asset:javascript src="app/pvs/alert_utils/dictionary-utils.js"/>
        </g:else>
    </sec:ifAnyGranted>
    <asset:javascript src="app/pvs/emergingIssue/emergingIssue.js"/>
    <asset:javascript src="fuelux/fuelux.js"/>
    <asset:javascript src="app/pvs/disableAutocomplete.js"/>
    <asset:stylesheet src="fuelux.css"/>
    <asset:javascript src="app/pvs/users/user_edit.js"/>
    <asset:javascript src="app/pvs/configuration/copyPasteValues.js"/>
    <asset:javascript src="boostrap-switch.js"/>
    <asset:javascript src="app/pvs/dataTablesActionButtons.js"/>
    <asset:javascript src="app/pvs/configuration/dictionaryMultiSearch.js"/>
    <asset:javascript src="yadcf/jquery.dataTables.yadcf.js"/>
    <asset:stylesheet src="copyPasteModal.css"/>
    <g:if test="${!Holders.config.pv.plugin.dictionary.enabled}">
        <asset:stylesheet src="dictionaries.css"/>
    </g:if>
    <asset:javascript src="app/pvs/bootbox.min.js"/>

    <style>
    .bootstrap-switch-container {
        white-space: nowrap;
    }

    .row.display-flex {
        display: flex;
        flex-wrap: wrap;
    }

    .row.display-flex > [class*='col-'] {
        display: flex;
        flex-direction: column;
    }

    </style>
</head>

<body>
<g:render template="/includes/layout/flashErrorsDivs" bean="${alertStopList}" var="theInstance"/>
<input type="hidden" id="selectedDatasource" class="selectedDatasource" value="pva"/>
<sec:ifAnyGranted roles="ROLE_CONFIGURATION_CRUD">
<g:render template="add_alert_list"
          model="[emergingIusseList: emergingIusseList, controller: controllerName, callingScreen: callingScreen, isPVCM: isPVCM, multiIngredientValue: emergingIusseList?.isMultiIngredient]"/>
<g:render template="/configuration/copyPasteModal"/>
</sec:ifAnyGranted>
<g:render template="show_alert_list" model="[emergingIusseList: emergingIusseList]"/>

</body>
</html>