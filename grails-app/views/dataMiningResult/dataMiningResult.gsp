<%--
  Created by IntelliJ IDEA.
  User: shivams
  Date: 16/09/24
  Time: 2:32 PM
--%>


<%@ page import="grails.util.Holders; com.rxlogix.Constants; com.rxlogix.util.DateUtil; grails.plugin.springsecurity.SpringSecurityUtils; com.rxlogix.util.ViewHelper;grails.converters.JSON" %>
<head>
    <asset:stylesheet src="query.css"/>
    <g:javascript>
        var fetchResultUrl = "${Holders.config.fetchDataMiningResult.url}"
        var stringOperatorsUrl =  "${createLink(controller: 'query', action: 'getStringOperators')}";
        var numOperatorsUrl =  "${createLink(controller: 'query', action: 'getNumOperators')}";
        var booleanOperatorsUrl =  "${createLink(controller: 'query', action: 'getBooleanOperators')}";
        var dateOperatorsUrl =  "${createLink(controller: 'query', action: 'getDateOperators')}";
        var valuelessOperatorsUrl = "${createLink(controller: 'query', action: 'getValuelessOperators')}";
        var keywordsUrl =  "${createLink(controller: 'query', action:'getAllKeywords')}";
        var fieldsValueUrl = "${createLink(controller: 'query', action: 'getFieldsValue')}";
        var allFieldsUrl = "${createLink(controller: 'aggregateOnDemandAlert', action: 'fetchAllFieldValues')}";
        var possibleValuesUrl = "${createLink(controller: 'aggregateOnDemandAlert', action: 'fetchPossibleValues')}";
    </g:javascript>
    <meta name="layout" content="main"/>
    <title>Data Mining Result</title>
</head>

<g:javascript>
        var delimiter = null
        var editMessage = "${message(code: "app.onlyAdminCreateNewTags.message")}";
        var isPVCM = ${isPVCM};
        var sharedWithUrl = "${createLink(controller: 'user', action: 'searchShareWithUserGroupList')}";
        var blankValuesForQueryUrl = "${createLink(controller: 'query', action: 'queryExpressionValuesForQuery')}";
        var customSQLValuesForQueryUrl = "${createLink(controller: 'query', action: 'customSQLValuesForQuery')}";
        var customSQLValuesForTemplateUrl = "${createLink(controller: 'template', action: 'customSQLValuesForTemplate')}";
        var queryViewUrl = "${createLink(controller: 'query', action: 'view')}";
        var blankValuesForQuerySetUrl = "${createLink(controller: 'query', action: 'queryExpressionValuesForQuerySet')}";
        var queryList = "${createLink(controller: 'query',action: 'queryList')}";
        var dataSheetList = "${createLink(controller: 'dataSheet',action: 'dataSheets')}";
        var fetchFreqNameUrl = "${createLink(controller:"aggregateCaseAlert", action: "fetchFreqName")}";
        var fetchAllowedUsersUrl = "${createLink(controller: 'configurationRest', action: 'fetchAllowedUsers')}";
        var selectAutoUrl = "${createLink(controller: 'query', action: 'ajaxReportFieldSearch')}";
        var importExcel = "${createLink(controller: 'query', action: 'importExcel')}";
        var isWhoDictionary = true;
</g:javascript>

<asset:javascript src="app/pvs/alert_utils/common_alert_utils.js"/>
<asset:javascript src="app/pvs/common/rx_common.js"/>
<asset:javascript src="app/pvs/userGroupSelect.js"/>
<asset:javascript src="app/pvs/alert_utils/alert_product_utils.js"/>
<asset:javascript src="app/pvs/alert_utils/alert_event_utils.js"/>
<asset:javascript src="app/pvs/alert_utils/multi_datasource_dictionary.js"/>
<asset:javascript src="app/pvs/alert_utils/dictionary-utils.js"/>
<asset:javascript src="app/pvs/groups.js"/>
<asset:javascript src="app/pvs/configuration/configurationCommon.js" asset-defer="defer"/>
<asset:javascript src="app/pvs/configuration/blankParameters.js"/>
<asset:javascript src="app/pvs/alert_utils/alert_query_utils.js"/>
<asset:javascript src="app/pvs/disableAutocomplete.js"/>
<asset:javascript src="app/pvs/bootbox.min.js"/>
<asset:javascript src="app/pvs/configuration/copyPasteValues.js"/>
<asset:javascript src="app/pvs/configuration/dictionaryMultiSearch.js"/>
<asset:stylesheet src="configuration.css"/>
<asset:stylesheet src="copyPasteModal.css"/>
<asset:stylesheet src="dictionaries.css"/>
<body>
    <g:render template="includes/dataMiningConfiguration" model="[fieldList:fieldList,isShareFilterViewAllowed: isShareFilterViewAllowed]"/>
    <g:render template="includes/dataMiningSearchResults"/>
</body>
</html>