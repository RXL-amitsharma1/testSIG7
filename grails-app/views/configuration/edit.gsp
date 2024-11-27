<%@ page import="com.rxlogix.util.ViewHelper; com.rxlogix.util.DateUtil; com.rxlogix.config.Tag;" %>
<%@ page import="com.rxlogix.config.DateRangeValue" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.EditReport.title"/></title>
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
        var booleanOperatorsUrl =  "${createLink(controller: 'query', action: 'getBooleanOperators')}";
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
    <asset:javascript src="app/scheduler.js"/>
    <asset:javascript src="app/configuration/configurationCommon.js"/>
    <asset:javascript src="app/configuration/deliveryOption.js"/>
    <asset:javascript src="app/configuration/dateRange.js"/>
    <asset:javascript src="app/configuration/templateQueries.js"/>
    <asset:javascript src="app/configuration/blankParameters.js"/>
    <asset:javascript src="app/disableAutocomplete.js"/>
    <asset:stylesheet src="configuration.css"/>
    <asset:link rel="import" href="columnView/column-view.html"/>
</head>

<body>

<g:set var="userService" bean="userService"/>

<div class="row">
    <div class="col-sm-12">
        <div class="page-title-box">
            <div class="fixed-page-head">
                <div class="page-head-lt">
                    <h4>${message(code: 'app.label.editReport')}</h4>
                </div>
            </div>
        </div>
    </div>
</div>
<div class="row">
    <div class="col-sm-12">
        <g:render template="/includes/layout/flashErrorsDivs" bean="${configurationInstance}" var="theInstance"/>

        <form id="configurationForm" name="configurationForm" method="post" autocomplete="off" onsubmit="return onFormSubmit()">

            <g:render template="form" model="[configurationInstance: configurationInstance]"/>
            <g:hiddenField name="editable" id="editable" value="true"/>
            <g:hiddenField name="schedulerFrom" id="schedulerFrom" value="editPage"/>

            %{--BEGIN: Button Bar  ==============================================================================================================--}%
            <div class="pull-right rxmain-container-top">
                <g:if test="${configurationInstance?.isEnabled && configurationInstance?.nextRunDate}">
                    <g:actionSubmit class="btn btn-default"  action="disable" type="submit" value="${message(code:"default.button.unschedule.label")}"></g:actionSubmit>
                </g:if>
                <g:else>
                    <g:actionSubmit class="btn btn-primary" action="run" type="submit" value="${message(code: 'default.button.run.label')}"/>
                </g:else>
                <g:actionSubmit class="btn btn-default btn-primary" action="update" type="submit" value="${message(code: 'default.button.update.label')}"/>
                <a class="btn btn-default pv-btn-grey"
                   href="${createLink(controller: 'configuration', action: 'index')}">${message(code: "default.button.cancel.label")}</a>
            </div>
            %{--END: Button Bar  ================================================================================================================--}%
            <input name="blankValuesJSON" id="blankValuesJSON" value="${configurationInstance?.blankValuesJSON}" hidden="hidden"/>
        </form>

        <div>
            %{--<!-- Render the templateQuery template (_templateQuery.gsp) hidden so we can clone it -->--}%
            <g:render template='/templateQuery/templateQuery' model="['templateQueryInstance':null,'i':'_clone','hidden':true]"/>
            %{--</tbody>--}%

            <div class="expression" hidden="hidden"><g:render template="/query/toAddContainerQEV"   model="[type:'qev']"/></div>
            <div class="expression" hidden="hidden"><g:render template="/query/customSQLValue"   model="[type:'qev']"/></div>
        </div>
    </div>
</div>
%{--<rx:container title="${message(code: 'app.label.editReport')}">--}%
%{--</rx:container>--}%
</body>

