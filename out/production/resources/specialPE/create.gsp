<html>
<head>
    <meta name="layout" content="main"/>
    <g:javascript>
        var editMessage = "${message(code: "app.onlyAdminCreateNewTags.message")}";
        var getSelectedEventUrl = "${createLink(controller: 'configurationRest', action: 'getSelectedEvent')}";
        var getPreLevelEventParentsUrl = "${createLink(controller: 'configurationRest', action: 'getPreLevelEventParents')}";
        var searchEventsUrl = "${createLink(controller: 'configurationRest', action: 'searchEvents')}";
        var specialPEeditUrl = "${createLink(controller: 'specialPE', action: 'edit')}"
        var createUrl = "${createLink(controller: 'specialPE', action: 'create')}"
        var specialPEdeleteUrl = "${createLink(controller: 'specialPE', action: 'delete')}"

        var getSelectedProductUrl = "${createLink(controller: 'configurationRest', action: 'getSelectedProduct')}";
        var getPreLevelProductParentsUrl = "${createLink(controller: 'configurationRest', action: 'getPreLevelProductParents')}";
        var searchProductsUrl = "${createLink(controller: 'configurationRest', action: 'searchProducts')}";

        var getSelectedStudyUrl = "${createLink(controller: 'configurationRest', action: 'getSelectedStudy')}";
        var getPreLevelStudyParentsUrl = "${createLink(controller: 'configurationRest', action: 'getPreLevelStudyParents')}";
        var searchStudiesUrl = "${createLink(controller: 'configurationRest', action: 'searchStudies')}";

        var getSelectedGenericUrl = "${createLink(controller: 'configurationRest', action: 'getSelectedGenericNames')}";
        var searchGenericsUrl = "${createLink(controller: 'configurationRest', action: 'searchGenerics')}";
        var specialPEUrl = "${createLink(controller: 'specialPE', action: 'list')}";
        var isAdmin = ${grails.plugin.springsecurity.SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_CONFIGURATION_CRUD")};

    </g:javascript>
    <asset:stylesheet src="configuration.css"/>
    <sec:ifAnyGranted roles="ROLE_CONFIGURATION_CRUD">
        <asset:javascript src="app/pvs/alert_utils/alert_product_utils.js"/>
        <asset:javascript src="app/pvs/alert_utils/alert_event_utils.js"/>
    </sec:ifAnyGranted>
    <asset:javascript src="app/pvs/specialPE.js"/>
    <asset:javascript src="app/pvs/dataTablesActionButtons.js"/>
    <asset:javascript src="fuelux/fuelux.js"/>
    <asset:javascript src="app/pvs/disableAutocomplete.js"/>
    <asset:javascript src="app/pvs/users/user_edit.js"/>
    <asset:stylesheet src="fuelux.css"/>
    <asset:stylesheet src="dictionaries.css"/>
    <asset:javascript src="app/pvs/bootbox.min.js"/>

    <script>
        $(document).ready(function () {
            $(".searchProducts[level=1]").attr("disabled", true);
            $(".searchProducts[level=2]").attr("disabled", true);
            $(".searchProducts[level=4]").attr("disabled", true);
            $(".searchEvents[level=1]").attr("disabled", true);
            $(".searchEvents[level=2]").attr("disabled", true);
            $(".searchEvents[level=3]").attr("disabled", true);
            $(".searchEvents[level=5]").attr("disabled", true);
            $(".searchEvents[level=6]").attr("disabled", true);
        })
    </script>

</head>

<body>

<g:render template="/includes/layout/flashErrorsDivs" bean="${specialPE}" var="theInstance"/>
<div class="messageContainer"></div>
<input type="hidden" id="selectedDatasource" class="selectedDatasource" value="pva"/>
<sec:ifAnyGranted roles="ROLE_CONFIGURATION_CRUD">
    <g:render template="add_alert_list" model="[alertStopList: specialPE]"/>
</sec:ifAnyGranted>
<g:render template="show_alert_list" model="[alertStopList: specialPE]"/>
</body>
</html>