<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Edit Rule</title>
    <asset:javascript src="fuelux/fuelux.js"/>
    <asset:javascript src="app/pvs/disableAutocomplete.js"/>
    <asset:stylesheet src="fuelux.css"/>
    <asset:javascript src="boostrap-switch.js"/>
    <asset:javascript src="spectrum/spectrum.js"/>
    <asset:stylesheet src="spectrum.css"/>
    <asset:javascript src="app/pvs/businessConfiguration/businessConfiguration.js"/>
    <asset:javascript src="backbone/underscore.js"/>
    <asset:javascript src="backbone/backbone.js"/>
    <asset:javascript src="app/pvs/query/businessConfigQueryBuilder.js"/>
    <asset:stylesheet src="query.css"/>
    <asset:stylesheet src="app/pvs/businessConfiguration.css"/>
    <asset:stylesheet src="yadcf/jquery.dataTables.yadcf.css"/>
    <asset:stylesheet src="colReorder.dataTables.min.css"/>

    <g:javascript>
        var queryType = "QUERY_BUILDER";
        var editable = true;
        var disableInactiveTabs = false;
        var justificationObj = JSON.parse("${justificationJSON}");
        var nonConfiguredEnabled = "${grailsApplication.config.categories.feature.nonConfigured.enabled ? true : false}";
        var fetchCommonTagsUrl = "${createLink(controller: 'commonTag', action: 'commonTagDetails')}";
        $(document).ready(function () {
            $('#row-container').find('.business-configuration-row').each(function (index) {
                enableNextSelectBox($(this).find('.expressionCategory'));
                updateOperatorFieldsForAll();
                updateThresholdFieldsForAll();
            });
        });

        $(document).ready(function(){
           $('#case-alert-type-container').hide();
        });
        $(window).load(function() {
            $('[data-toggle="tooltip"]').tooltip({ trigger: "hover" });
        });

    </g:javascript>
</head>

<body>
<g:hiddenField name="fetchSelectBoxValuesUrl"
               value="${createLink(controller: 'businessConfiguration', action: 'fetchSelectBoxValues')}"/>
<g:hiddenField name="saveRuleUrl"
               value="${createLink(controller: 'businessConfiguration', action: 'saveRule')}"/>
<input type="hidden" id="selectedDatasource" class="selectedDatasource" value="pva"/>
<g:render template="/includes/layout/flashErrorsDivs" bean="${ruleInformation}" var="theInstance"/>
<div class="rxmain-container rxmain-container-top" id="businessconfig">
    <div class="rxmain-container-inner">
        <div id="alertMessage" hidden="hidden" class="alert alert-danger"></div>
        <div class="rxmain-container-row rxmain-container-header">
            <label class="rxmain-container-header-label click">
                <g:message code="app.label.business.configuration.title"/>
            </label>
        </div>

        <div class="rxmain-container-content">

            <g:form controller="businessConfiguration" action="updateRule" id="${ruleInformation?.id}"
                    name="businessConfigurationForm">

                <g:render template="ruleFormContent"
                          model="[businessConfigurationId: businessConfigurationId, queries: queries, actionList: actionList,
                                  dispositionList: dispositionList, justificationList: justificationList, ruleInformation: ruleInformation]"/>

                <div class="row">
                    <div class="col-md-2">
                        <g:submitButton name="saveBusinessConfigButton" class="btn btn-primary" value="Update"/>
                        <g:link name="cancelButton" controller="businessConfiguration" action="index" class="btn btn-default">Cancel</g:link>
                    </div>
                </div>
            </g:form>
        </div>
    </div>
</div>
<g:render template="/includes/modals/format_business_config"/>
</body>
</html>