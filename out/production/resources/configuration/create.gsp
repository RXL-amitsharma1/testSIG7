<%@ page import="com.rxlogix.util.ViewHelper; com.rxlogix.util.DateUtil; com.rxlogix.config.Tag;" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.config.alert.label"/></title>

    <g:javascript>
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

        var templateSearchUrl = "${createLink(controller: 'reportTemplateRest', action: 'getTemplateList')}";

        var stringOperatorsUrl =  "${createLink(controller: 'query', action: 'getStringOperators')}";
        var numOperatorsUrl =  "${createLink(controller: 'query', action: 'getNumOperators')}";
        var dateOperatorsUrl =  "${createLink(controller: 'query', action: 'getDateOperators')}";
        var valuelessOperatorsUrl = "${createLink(controller: 'query', action: 'getValuelessOperators')}";
        var keywordsUrl =  "${createLink(controller: 'query', action:'getAllKeywords')}";
        var fieldsValueUrl = "${createLink(controller: 'query', action: 'getFieldsValue')}";
        var allFieldsUrl = "${createLink(controller: 'query', action: 'getAllFields')}";
        var possibleValuesUrl = "${createLink(controller: 'query', action: 'possibleValues')}";

        var blankValuesForQueryUrl = "${createLink(controller: 'query', action: 'queryExpressionValuesForQuery')}";
        var customSQLValuesForQueryUrl = "${createLink(controller: 'query', action: 'customSQLValuesForQuery')}";
        var customSQLValuesForTemplateUrl = "${createLink(controller: 'template', action: 'customSQLValuesForTemplate')}";

        var LABELS = {
            labelShowAdavncedOptions : "${message(code:'reportCriteria.show.advanced.options')}",
            labelHideAdavncedOptions : "${message(code:'reportCriteria.hide.advanced.options')}"
        }
    </g:javascript>

    <asset:javascript src="fuelux/fuelux.js"/>
    <asset:stylesheet src="fuelux.css"/>
    <asset:javascript src="app/alert_utils/common_alert_utils.js"/>
    <asset:javascript src="app/alert_utils/alert_product_utils.js"/>
    <asset:javascript src="app/alert_utils/alert_study_utils.js"/>
    <asset:javascript src="app/alert_utils/alert_event_utils.js"/>
    <asset:javascript src="app/tags.js"/>
    <asset:javascript src="app/configuration/templateQueries.js"/>
    <asset:javascript src="app/scheduler.js"/>
    <asset:javascript src="app/configuration/configurationCommon.js"/>
    <asset:javascript src="app/configuration/deliveryOption.js"/>
    <asset:javascript src="app/configuration/dateRange.js"/>
    <asset:javascript src="app/configuration/blankParameters.js"/>
    <asset:javascript src="app/disableAutocomplete.js"/>
    <asset:stylesheet src="configuration.css"/>
    <asset:link rel="import" href="columnView/column-view.html"/>
</head>

<body>
    <g:set var="userService" bean="userService"/>

    <g:render template="/includes/layout/flashErrorsDivs" bean="${configurationInstance}" var="theInstance"/>

    <form id="configurationForm" name="configurationForm" method="post" autocomplete="off" onsubmit="return onFormSubmit()">

        <g:render template="form" model="[configurationInstance: configurationInstance]"/>

        <g:hiddenField name="schedulerFrom" id="schedulerFrom" value="createPage"/>

        %{--BEGIN: Button Bar  ==============================================================================================================--}%
        <div style="margin-top:15px;">
            <div style="text-align: right">

            <g:actionSubmit class="btn primaryButton btn-primary" id="saveRun" action="run"
                            type="submit"
                            value="${message(code: 'default.button.run.label')}"/>
            <g:actionSubmit class="btn btn-default pv-btn-grey" id="saveBtn" action="save"
                            type="submit"
                            value="${message(code: 'default.button.save.label')}"/>
            <a class="btn btn-default pv-btn-grey"
               href="${createLink(controller: 'configuration', action: 'index')}">${message(code: "default.button.cancel.label")}</a>
        </div>
        </div>
        %{--END: Button Bar  ================================================================================================================--}%

        <input name="blankValuesJSON" id="blankValuesJSON" value="${configurationInstance?.blankValuesJSON}" hidden="hidden"/>
    </form>

    <div>
        %{--<!-- Render the templateQuery template (_templateQuery.gsp) hidden so we can clone it -->--}%
        <g:render template='/templateQuery/templateQuery' model="['templateQueryInstance':null,'i':'_clone','hidden':true]"/>
        <div class="expression" hidden="hidden"><g:render template="/query/toAddContainerQEV"   model="[type:'qev']"/></div>
        <div class="expression" hidden="hidden"><g:render template="/query/customSQLValue"   model="[type:'qev']"/></div>
    </div>

</body>
