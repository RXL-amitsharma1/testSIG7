<%@ page import="com.rxlogix.util.ViewHelper; com.rxlogix.util.DateUtil; com.rxlogix.config.Tag; com.rxlogix.pvdictionary.config.PVDictionaryConfig" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.report"/></title>
    <asset:stylesheet src="configuration.css"/>
    <asset:stylesheet src="fab.css"/>
    <asset:javascript src="app/pvs/configuration/dateRangeEvdas.js"/>
    <asset:javascript src="app/pvs/configuration/copyPasteValues.js"/>
    <asset:javascript src="app/pvs/configuration/dictionaryMultiSearch.js"/>
    <asset:stylesheet src="copyPasteModal.css"/>

    <g:javascript>
        var reportHistoryUrl = "${createLink(controller: 'report', action: 'history')}";
        var fetchICSRsReportUrl = "${createLink(controller: 'report', action: 'prepareICSRsReport')}";
        var fetchReactionGroupReportUrl = "${createLink(controller: 'report', action: 'prepareReactionGroupReport')}";
        var fetchReactionReportUrl = "${createLink(controller: 'report', action: 'prepareReactionReport')}";
        var saveICSRsReportHistoryUrl = "${createLink(controller: 'report', action: 'saveICSRsReportHistory')}";
        var requestReportRestUrl = "${createLink(controller: 'report', action: 'requestReport')}";
        var downloadReportUrl = "${createLink(controller: 'report', action: 'downloadReport')}";
        var viewICSRReportUrl = "${createLink(controller: 'report', action: 'view')}";
        var faersDisabledColumnsIndexesUrl = "${createLink(controller: 'aggregateCaseAlert', action: 'fetchFaersDisabledColumnsIndexes')}";
        var substanceFrequencyPropertiesUrl = "${createLink(controller: 'evdasAlert', action: 'fetchSubstanceFrequencyProperties')}";
        var ingredientLevel = ${PVDictionaryConfig.ingredientColumnIndex};
        pageName = 'Reporting';
    </g:javascript>
    <style>
    .validation-tab li a:before {
        content: none;
    }

    .validation-tab li a:after {
        content: none;
    }

    .validation-tab li {
        margin: 1px 0 5px 0;
    }

    .validation-tab li a {
        font-size: 14px;
        padding: 10px;
        margin: 1px;
    }

    .panel-body {
        padding: 15px !important;
    }

    .fa-disabled {
        opacity: 0.6;
        cursor: not-allowed !important;
    }
</style>
</head>

<body>

<div id="notification"></div>
<br/>
<div class="pv-tab">

    <!-- Nav tabs -->
    <ul id="detail-tabs" class="nav nav-tabs rxmain-container-header-label" role="tablist">
        <li role="presentation" <g:if test="${!showHistory}">class="active"</g:if>>
            <a href="#graphReport" aria-controls="details" role="tab" data-toggle="tab">Signal Reports</a>
        </li>
        <li role="presentation" <g:if test="${showHistory}">class="active"</g:if>>
            <a href="#reportHistory" id="reportHistoryTab" aria-controls="history" role="tab"
               data-toggle="tab">Generated Reports</a>
        </li>
    </ul>

    <!-- Tab panes -->
    <div class="tab-content">
        <div role="tabpanel" class="tab-pane <g:if test="${!showHistory}">active</g:if>" id="graphReport">
            <g:hiddenField name="productGroupSelection" id="productGroupSelection" value="${preSelectedData['productGroupSelection']}"/>
            <g:render template="includes/graphReport"
                      model="[socList: socList, ptList: ptList, productGroups: productGroups, reportTypes: reportTypes, preSelectedData: preSelectedData, datasources: datasources,isPVCM:isPVCM]"/>
        </div>
        <g:render template="/configuration/copyPasteModal" />
        <div role="tabpanel" class="tab-pane <g:if test="${showHistory}">active</g:if>" id="reportHistory">
            <g:render template="includes/reportHistory"/>
        </div>
    </div>

</div>
</body>
