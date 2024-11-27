<%@ page import="com.rxlogix.util.ViewHelper" %>
<asset:stylesheet src="dictionaries.css"/>
<asset:javascript src="app/pvs/alert_utils/alert_product_utils.js"/>
<asset:javascript src="app/pvs/alert_utils/alert_event_utils.js"/>
<script>
    $(document).ready(function () {

        $("#genSignalSummary").click(function () {
            $("#signal-summary").html($("#summary").html());
        });

        $(".causality-assessment").click(function () {
            var causalityModal = $("#causalityModal");
            causalityModal.modal("show");
        });

        $(".generate-assessment-reports").click(function () {
            var data = {};
            var eventSelection = $('#eventSelection').val();
            var productSelection = $('#productSelection').val();
            data['dataSource'] = $('#dataSources').val();
            data['dateRange'] = $('#dateRange').val();
            data['productSelection'] = productSelection;
            data['eventSelection'] = eventSelection;
            data['topic.id'] = $("#topicIdPartner").val();
            $.ajax({
                type: "POST",
                url: graphReportRestUrl,
                data: data,
                success: function (result) {
                    age_group_data.options.series = result['age-grp-over-time-chart'];
                    age_group_data.options.xAxis[0].categories = getCategoriesFromData(result['age-grp-over-time-chart'][0]['data']);
                    seriousness_data.options.series = result['seriousness-over-time-chart'];
                    seriousness_data.options.xAxis[0].categories = getCategoriesFromData(result['seriousness-over-time-chart'][0]['data']);
                    country_data.options.series = result['country-over-time-chart'];
                    country_data.options.xAxis[0].categories = getCategoriesFromData(result['country-over-time-chart'][0]['data']);
                    gender_data.options.series = result['gender-over-time-chart'];
                    gender_data.options.xAxis[0].categories = getCategoriesFromData(result['gender-over-time-chart'][0]['data']);
                    outcome_data.options.series = result['outcome-over-time-chart'];
                    outcome_data.options.xAxis[0].categories = getCategoriesFromData(result['outcome-over-time-chart'][0]['data']);
                    pie_chart_data.options.series = result['seriousness-count-pie-chart'];
                    pie_chart_data.options.xAxis[0].categories = getCategoriesFromData(result['seriousness-count-pie-chart'][0]['data']);

                    var chartArray = ["severity", "ageGroup", "country", "gender"];

                    var addedChartsCount = $("#addedChartsCount").val();

                    signal.charts.direct_draw_bar_chart('age-grp-over-time-chart', age_group_data.options);

                    signal.charts.direct_draw_bar_chart('seriousness-over-time-chart', seriousness_data.options);

                    signal.charts.direct_draw_bar_chart('country-over-time-chart', country_data.options);

                    signal.charts.direct_draw_bar_chart('gender-over-time-chart', gender_data.options);

                    signal.charts.direct_draw_bar_chart('outcome-over-time-chart', outcome_data.options);

                    signal.charts.direct_draw_bar_chart('seriousness-count-pie-chart', pie_chart_data.options);

                    xAxis = result['systemOrganClass']['years'];
                    yAxis = result['systemOrganClass']['socs'];
                    chartData = result['systemOrganClass']['data'];
                    init_system_organ_heat_chart();

                    $(".generate-chart").click(function () {
                        var chartName = $(this).attr('data-id');
                        var renderingNotification = '<span style="margin-left: 40%; margin-top:30%">Rendering Chart....</span>'
                        $("#" + chartName).html(renderingNotification);
                        $("#" + chartName).css("height", 400);
                        initChartDataGen(topicId, chartName)
                    });

                    $(".refresh-charts").click(function () {
                        for (var index = 0; index < chartArray.length; index++) {
                            var chartName = chartArray[index];
                            initChartDataGen(topicId, chartName)
                        }
                    });
                }
            });
        });
    });
    $('a#assessmentTab').click(function () {
        $.ajax({
            url: VALIDATED.assessmentFilterUrl + "?id=" + $("#topicId").val(),
            success: function (result) {
                var element = '<div style="padding: 5px">';
                $('#productSelection').val('{"1":[],"2":[],"3":' + JSON.stringify(result.productList) + ',"4":[],"5":[]}');
                $('#eventSelection').val('{"1":[],"2":[],"4":' + JSON.stringify(result.eventList) + ',"3":[],"5":[],"6":[]}');
                $("#showProductSelection").html(element + joinValuesInObjectArray(result.productList, 'name') + "</div>");
                $("#showEventSelection").html(element + joinValuesInObjectArray(result.eventList, 'name') + "</div>");
                productSelectionModal.loadProducts();
                eventSelectionModal.loadEvents();
                $(".generate-assessment-reports").click();
            }
        });
        $('a#assessmentTab').unbind("click");
    });

    var joinValuesInObjectArray = function (resultSet, prop) {
        return resultSet.reduce(function (a, b) {
            return a + ["", ", "][+!!a.length] + b[prop];
        }, "")
    }
</script>

<div class="rxmain-container ">

    <div class="rxmain-container-inner">
        <div class="rxmain-container-row rxmain-container-header">
            <label class="rxmain-container-header-label">
                Topic Assessment
            </label>
        </div>

        <div class="row hide">
            <div class="col-sm-12">
                <div class="rxmain-container">
                    <div class="rxmain-container-inner">
                        <div class="rxmain-container-row rxmain-container-header" data-toggle="collapse"
                             href="#assessmentFilterPanel"
                             aria-expanded="false" aria-controls="assessmentFilterPanel">
                            <label class="rxmain-container-header-label">Assessment Filter Panel</label>
                        </div>

                        <div class="collapse in" id="assessmentFilterPanel">
                            <div class="rxmain-container-content ">
                                <div class="col-md-2">
                                    <label>Data Source</label>
                                    <g:select name="dataSources" from="${datasources}" value="${"PVA"}"
                                              class="form-control" required="required"/>
                                    <input type="hidden" id="selectedDatasource" class="selectedDatasource"
                                           value="pva"/>
                                </div>

                                <div class="col-md-2">
                                    <div class="col-xs-12">
                                        <label><g:message code="app.label.DateRange"/></label>
                                        <g:select name="dateRange"
                                                  from="${com.rxlogix.util.ViewHelper.getAssessmentFilterDateRange()}"
                                                  optionValue="display"
                                                  optionKey="name"
                                                  value="LAST_1_YEAR"
                                                  class="form-control" required="required"/>
                                    </div>
                                </div>

                                <div class="col-md-3">
                                    <label>
                                        <g:message code="app.label.productSelection"/>
                                        <span class="required-indicator">*</span>
                                    </label>

                                    <div class="wrapper">
                                        <div id="showProductSelection" class="showDictionarySelection">
                                        </div>

                                        <div class="iconSearch">
                                            <i class="fa fa-search productRadio" data-toggle="modal"
                                               data-target="#productModal"></i>
                                        </div>
                                    </div>
                                    <g:hiddenField name="productSelection" class="productSelection"/>
                                    <g:hiddenField name="isMultiIngredient" class="multiIngredient"/>
                                </div>

                                <div class="col-md-3">
                                    <label><g:message code="app.label.eventSelection"/>
                                        <span class="required-indicator">*</span></label>

                                    <div class="wrapper">
                                        <div id="showEventSelection" class="showDictionarySelection"></div>

                                        <div class="iconSearch">
                                            <i class="fa fa-search" id="searchEvents" data-toggle="modal"
                                               data-target="#eventModal"></i>
                                        </div>
                                    </div>
                                    <g:hiddenField name="eventSelection" class="eventSelection"/>
                                </div>

                                <div class="col-md-2">
                                    <span class="generate-assessment-reports save btn btn-primary">Generate</span>
                                </div>
                            </div>
                        </div>
                    </div>

                </div>
            </div>
        </div>

        <div class="rxmain-container-content">

            <div class="row">
                <div class="col-sm-12">
                    <div class="rxmain-container">

                        <div class="rxmain-container-inner">
                            <div class="rxmain-container-row rxmain-container-header" data-toggle="collapse"
                                 href="#summayPanel"
                                 aria-expanded="false" aria-controls="summayPanel">
                                <label class="rxmain-container-header-label">Summary Detail</label>
                            </div>

                            <div class="collapse in" id="summayPanel">
                                <div class="rxmain-container-content ">
                                    <g:render template="/validatedSignal/includes/summaryDetail"
                                              modal="[signal: topic]"/>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row" style="margin-bottom:2%">
                <div class="col-md-6">
                    <span class="save btn btn-primary">
                        <a style="color: white;" target="_blank"
                           href="showTrendAnalysis?id=${topic.id}">Trend Analysis</a>
                    </span>
                </div>
            </div>

            <div class="row">
                <div class="col-sm-12">
                    <div class="rxmain-container">

                        <div class="rxmain-container-inner">

                            <div class="rxmain-container-row rxmain-container-header" data-toggle="collapse"
                                 href="#conceptsPanel"
                                 aria-expanded="false" aria-controls="coneptsPanel">
                                <label class="rxmain-container-header-label">Medical Concept Distribution</label>
                            </div>

                            <div class="collapse in" id="conceptsPanel">
                                <div class="rxmain-container-content ">
                                    <g:render template="/includes/widgets/medical-concepts"
                                              modal="[conceptsMap: conceptsMap]"/>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="col-sm-12">
                    <div class="rxmain-container">

                        <div class="rxmain-container-inner">
                            <div class="rxmain-container-row rxmain-container-header" data-toggle="collapse"
                                 href="#pePanel"
                                 aria-expanded="false" aria-controls="pePanel">
                                <label class="rxmain-container-header-label">PEC Analysis</label>
                            </div>

                            <div class="collapse in" id="pePanel">
                                <div class="rxmain-container-content ">
                                    <g:render template="/validatedSignal/includes/peAnalysis"
                                              model="[signalId: topic.id, isTopic: true]"/>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="col-sm-12">
                    <div class="rxmain-container">

                        <div class="rxmain-container-inner">
                            <div class="rxmain-container-row rxmain-container-header" data-toggle="collapse"
                                 href="#assessmentDetails"
                                 aria-expanded="false" aria-controls="pePanel">
                                <div class="rxmain-container-header-label">Assessment Details
                                    <div class="dropdown pull-right">
                                        <i style="cursor: pointer;" class="fa fa-caret-square-o-down dropdown-toggle "
                                           data-toggle="dropdown">
                                        </i>
                                        <ul class="dropdown-menu">
                                            <li><a href="#">PVA</a></li>
                                            <li><a href="#">FAERS</a></li>
                                            <li><a href="#">EVDAS</a></li>
                                        </ul>
                                    </div>
                                </div>
                            </div>

                            <div class="collapse in" id="assessmentDetails">
                                <div class="rxmain-container-content ">
                                    <g:render template="/validatedSignal/includes/assessmentDetails"
                                              modal="[assessmentDetails: assessmentDetails]"/>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Graphs comes here. -->
            <div class="row">
                <div class="col-md-4">
                    <div class="panel panel-default">
                        <div class="panel-heading panel-title">
                            <div class="rxmain-container-header-label">
                                Distribution By Seriousness Over Time
                                <i class="fa fa-refresh generate-chart pull-right"
                                   style="cursor:pointer; float: right"
                                   data-id="severity"></i>
                            </div>
                        </div>

                        <div class="panel-body">
                            <div id="severity" class="maxWidth">
                                <div id="seriousness-over-time-chart" style="height: 400px"></div>
                            </div>
                            <input type="hidden" class="executedId"/>
                        </div>
                    </div>
                </div>

                <div class="col-sm-4">
                    <div class="panel panel-default ">
                        <div class="panel-heading panel-title">
                            <div class="rxmain-container-header-label">
                                Distribution By Age Group Over Time
                                <i class="fa fa-refresh generate-chart" style="cursor:pointer; float: right"
                                   data-id="ageGroup"></i>
                            </div>
                        </div>

                        <div class="panel-body">
                            <div id="ageGroup" class="maxWidth">
                                <div id="age-grp-over-time-chart" style="height: 400px"></div>
                            </div>
                            <input type="hidden" class="executedId"/>
                        </div>
                    </div>
                </div>

                <div class="col-sm-4">
                    <div class="panel panel-default ">
                        <div class="panel-heading panel-title">
                            <div class="rxmain-container-header-label">
                                Distribution By Country Over Time
                                <i class="fa fa-refresh generate-chart" style="cursor:pointer; float: right"
                                   data-id="country"></i>
                            </div>
                        </div>

                        <div class="panel-body">
                            <div id="country" class="maxWidth">
                                <div id="country-over-time-chart" style="height: 400px"></div>
                            </div>
                            <input type="hidden" class="executedId"/>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="col-md-4">
                    <div class="panel panel-default ">
                        <div class="panel-heading panel-title">
                            <div class="rxmain-container-header-label">
                                Distribution By Gender Over Time
                                <i class="fa fa-refresh generate-chart" style="cursor:pointer; float: right"
                                   data-id="gender"></i>
                            </div>
                        </div>

                        <div class="panel-body">
                            <div id="gender" class="maxWidth">
                                <div id="gender-over-time-chart" style="height: 400px"></div>
                            </div>
                            <input type="hidden" class="executedId"/>
                        </div>
                    </div>
                </div>

                <div class="col-md-4">
                    <div class="panel panel-default ">
                        <div class="panel-heading panel-title">
                            <div class="rxmain-container-header-label">
                                Distribution By Outcome
                                <i class="fa fa-refresh generate-chart" style="cursor:pointer; float: right"
                                   data-id="gender"></i>
                            </div>
                        </div>

                        <div class="panel-body">
                            <div id="outcome" class="maxWidth">
                                <div id="outcome-over-time-chart" style="height: 400px"></div>
                            </div>
                            <input type="hidden" class="executedId"/>
                        </div>
                    </div>
                </div>

                <div class="col-md-4">
                    <div class="panel panel-default ">
                        <div class="panel-heading panel-title">
                            <div class="rxmain-container-header-label">
                                Distribution By Seriousness
                                <i class="fa fa-refresh generate-chart" style="cursor:pointer; float: right"></i>
                            </div>
                        </div>

                        <div class="panel-body">
                            <div id="seriousness-count" class="maxWidth">
                                <div id="seriousness-count-pie-chart" style="height: 400px"></div>
                            </div>
                            <input type="hidden" class="executedId"/>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="row col-lg-12">
            <div class="col-lg-12 panel panel-default align-center">
                <div class="panel-heading panel-title">
                    <div class="rxmain-container-header-label">
                        Distribution By System Organ Class
                        <i class="fa fa-refresh generate-chart pull-right" style="cursor:pointer; float: right"
                           data-id="heat-map"></i>
                    </div>
                </div>

                <div class="panel-body">
                    <div id="heat-map-chart" class="maxWidth">
                        <g:render template="/validatedSignal/includes/heat_map" model="${[heatMap: [:]]}"/>
                    </div>
                </div>
            </div>
        </div>

    </div>
</div>

<pre id="summary" style="display:none">
    Number of Cases: 10
    Positive Re-challenge: 2
    Causality Assessment: Related
    Signal Priority: Medium
    Proposed Action: Label Update
    Distribution by Region: USA: 14, EMA: 5, Japan: 3
    Average EB05 score for current period: 4.52
    Literature References: Yes
    Does this relate to any emerging safety issue: No
</pre>

<input type="hidden" value="${chartCount}" id="addedChartsCount"/>

<g:render template="/includes/modals/causality_assessment_modal" />
<g:render template="/includes/modals/previous_assessment_modal"/>
<asset:javascript src="app/pvs/validated_signal/signal_charts.js"/>