<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils; com.rxlogix.config.AlertStopList" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Alert Stop List</title>
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

        var getSelectedGenericUrl = "${createLink(controller: 'configurationRest', action: 'getSelectedGenericNames')}";
        var searchGenericsUrl = "${createLink(controller: 'configurationRest', action: 'searchGenerics')}";
        var stopListUrl = "${createLink(controller: 'alertStopList', action: 'list')}";
        var saveStopListUrl = "${createLink(controller: 'alertStopList', action: 'saveStopList')}";
        var updateListUrl = "${createLink(controller: 'alertStopList', action: 'update')}";
        var isAdmin = ${grails.plugin.springsecurity.SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_CONFIGURATION_CRUD")};

    </g:javascript>
    <asset:stylesheet src="configuration.css"/>
    <sec:ifAnyGranted roles="ROLE_CONFIGURATION_CRUD">
        <asset:javascript src="app/pvs/alert_utils/alert_product_utils.js"/>
        <asset:javascript src="app/pvs/alert_utils/alert_event_utils.js"/>
    </sec:ifAnyGranted>
    <asset:javascript src="app/pvs/alertStopList/alertStopList.js"/>
    <asset:javascript src="fuelux/fuelux.js"/>
    <asset:javascript src="app/pvs/disableAutocomplete.js"/>
    <asset:stylesheet src="fuelux.css"/>
    <asset:javascript src="app/pvs/users/user_edit.js"/>
    <asset:javascript src="boostrap-switch.js"/>
    <asset:stylesheet src="configuration.css"/>
    <asset:stylesheet src="dictionaries.css"/>
    <style>
    .bootstrap-switch-container {
        white-space: nowrap;
    }
    ul.productDictionaryColWidth {
        width: calc(100%/5);
    }
    </style>
</head>

<body>
<g:render template="/includes/layout/flashErrorsDivs" bean="${alertStopList}" var="theInstance"/>
<!-- For the single case alert we are going to keep the selected datasource as pva -->
<input type="hidden" id="selectedDatasource" class="selectedDatasource" value="pva"/>
<sec:ifAnyGranted roles="ROLE_CONFIGURATION_CRUD">
    <g:render template="add_alert_list" model="[alertStopList: alertStopList, controller: controllerName]"/>
</sec:ifAnyGranted>
<g:render template="show_alert_list" model="[alertStopList: alertStopList]"/>

</body>
</html>