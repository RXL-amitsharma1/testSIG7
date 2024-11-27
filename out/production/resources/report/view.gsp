<%@ page import="com.rxlogix.util.ViewHelper; com.rxlogix.util.DateUtil; com.rxlogix.config.Tag; com.rxlogix.pvdictionary.config.PVDictionaryConfig" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.report"/></title>
    <asset:stylesheet src="configuration.css"/>
    <asset:javascript src="app/pvs/configuration/dateRangeEvdas.js"/>
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
<div class="rxmain-container">
    <div class="rxmain-container-inner">
        <div class="rxmain-container-row rxmain-container-header">
            <label class="rxmain-container-header-label">
                ICSR By Case Characteristics
            </label>
        </div>

        <div class="rxmain-container-content">
            <div class="row">
                <div class="row">
                    <div class="col-md-4">
                        <div class="row">
                            <div class="col-md-12">
                                <div class="form-group">
                                    <label>Report Name:</label> ${reportHistory.reportName}
                                </div>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-md-12">
                                <div class="form-group">
                                    <label>Date Range:</label> ${DateUtil.toDateStringWithoutTimezone(reportHistory.startDate)} to ${DateUtil.toDateStringWithoutTimezone(reportHistory.endDate)}
                                </div>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-md-12">
                                <div class="form-group">
                                    <label>Data Source:</label> ${dataSource}
                                </div>
                            </div>
                        </div>

                    </div>

                    <div class="col-md-6">
                        <div class="row">
                            <div class="col-md-12">
                                <div class="form-group">
                                    <label>Product/Product Group(s):</label> ${reportHistory.productName}
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="col-md-2">
                        <div class="row">
                            <div class="col-md-12">
                                <a class="btn btn-default pull-right" href="/signal/report/index?showHistory=true">
                                    <i class="fa fa-long-arrow-left" aria-hidden="true"></i> Back</a>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="row">
                    <!-- Nav tabs -->
                    <ul id="detail-tabs" class="validation-tab m-b-5 p-0" role="tablist">
                        <li role="presentation" class="active">
                            <a href="#individualCases" aria-controls="details" role="tab"
                               data-toggle="tab">Number of Individual Cases</a>
                        </li>
                        <li role="presentation">
                            <a href="#individualCasesByReactionGroup" aria-controls="details" role="tab"
                               data-toggle="tab">Number of Individual Cases By Reaction Group</a>
                        </li>
                        <li role="presentation">
                            <a href="#reactionGroup" aria-controls="details" role="tab"
                               data-toggle="tab">Number of Cases by Reaction Group</a>
                        </li>
                        <li role="presentation">
                            <a href="#reaction" aria-controls="details" role="tab"
                               data-toggle="tab">Number of Cases by Reaction</a>
                        </li>
                    </ul>

                    <!-- Tab panes -->
                    <div class="tab-content">
                        <div role="tabpanel" class="tab-pane active" id="individualCases">
                            <g:render template="includes/individualCases"/>
                        </div>

                        <div role="tabpanel" class="tab-pane fade" id="individualCasesByReactionGroup">
                            <g:render template="includes/individualCasesByReactionGroup" model="[socList: socList]"/>
                        </div>

                        <div role="tabpanel" class="tab-pane fade" id="reactionGroup">
                            <g:render template="includes/viewReactionGroup" model="[socList: socList]"/>
                        </div>

                        <div role="tabpanel" class="tab-pane fade" id="reaction">
                            <g:render template="includes/viewReaction"
                                      model="[ptList: ptList, socList: socList, selectedSOCForReaction: selectedSOCForReaction]"/>
                        </div>

                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<asset:javascript src="app/pvs/report/graphReport.js"/>
<asset:javascript src="app/pvs/report/graph.js"/>

<g:javascript>
    var reportsData = JSON.parse("${historicData}");
    var ingredientLevel = ${PVDictionaryConfig.ingredientColumnIndex};
</g:javascript>
<script>
    $(document).ready(function () {
        signal.graph.showGraph(reportsData);
    });
</script>
</body>
