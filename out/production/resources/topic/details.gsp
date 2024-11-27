<%@ page import="com.rxlogix.enums.ReportFormat" %>
<g:set var="grailsApplication" bean="grailsApplication"/>

<head>
    <meta name="layout" content="main"/>
    <title><g:message code="topic.label"/></title>
    <g:javascript>
            var VALIDATED = {
                 scaListUrl: "${createLink(controller: 'topic', action: 'singleCaseAlertList')}",
                 acaListUrl: "${createLink(controller: 'topic', action: 'aggregateCaseAlertList')}",
                 adHocListUrl: "${createLink(controller: 'topic', action: 'adHocAlertList')}",
                 assessmentFilterUrl: "${createLink(controller: 'topic', action: 'aggregateCaseAlertProductAndEventList')}"
            };
            // var pvrIntegrate = "${grailsApplication.config.pvreports.url ? true : false}";
            var template_list_url = "${createLink(controller: 'template', action: 'index')}";
            var generateCaseSeriesUrl = "${createLink(controller: 'aggregateCaseAlert', action: 'generateCaseSeries')}";
            var isCommentAdded = true;
            var caseHistoryUrl = "${createLink(controller: "caseHistory", action: 'listCaseHistory')}";
            var caseHistorySuspectUrl = "${createLink(controller: "caseHistory", action: 'listSuspectProdCaseHistory')}"
            var productEventHistoryUrl = "${createLink(controller: "productEventHistory", action: 'listProductEventHistory')}"
            var caseDetailUrl = "${createLink(controller: "caseInfo", action: 'caseDetail')}"
            var getPriorityUrl = "${createLink(controller: "topic", action: 'getPriorities')}"
            var changePriorityUrl = "${createLink(controller: "topic", action: 'changePriority')}"
            var getWorkflowUrl = "${createLink(controller: "workflow", action: 'getWorkflowState')}"
            var activityUrl = "${createLink(controller: "activity", action: 'activitiesByTopic', params: [id: topic.id])}"
            var topicSaveUrl = "${createLink(controller: 'topic', action: 'save')}"
            var topicListUrl = "${createLink(controller: 'topic', action: 'list')}";
            var graphReportRestUrl = "${createLink(controller: 'topic', action: 'graphReport')}";
            var fetchSignalStatusUrl = "${createLink(controller: 'topic', action: 'detail')}";
            var xAxis = JSON.parse("${heatMap.years}");
            var yAxis = JSON.parse("${heatMap.socs}");
            var chartData = JSON.parse("${heatMap.data}");
            var pvrIntegrate = "${grailsApplication.config.pvreports.url ? true : false}";

    </g:javascript>
    <asset:javascript src="highcharts.js"/>
    <asset:javascript src="vendorUi/highcharts/highcharts-3d.js"/>
    <asset:javascript src="vendorUi/highcharts/highcharts-more.js"/>
    <asset:javascript src="vendorUi/highcharts/themes/grid-rx.js"/>

    <asset:javascript src="yadcf/jquery.dataTables.yadcf.js"/>
    <asset:javascript src="app/pvs/dataTablesActionButtons.js"/>
    <asset:javascript src="app/pvs/caseHistory/caseHistoryTable.js"/>
    <asset:javascript src="app/pvs/topic/topic.js"/>
    <asset:javascript src="app/pvs/validated_signal/validated_signal_charts.js"/>
    %{--<asset:javascript src="app/pvs/topic/topic_charts.js"/>--}%

    <asset:stylesheet src="yadcf/jquery.dataTables.yadcf.css"/>
    <asset:stylesheet src="components.css"/>

    <script>
        $("#detail-tabs a").click(function (event) {
            setTimeout(function () {
                _.each($('#detail-tabs a'), function (ele) {
                    if ($(ele).parent().hasClass('active')) {
                        $(ele).css('background', "slategrey")
                        $(ele).parent().addClass('rx-main-tab')
                    } else {
                        $(ele).css('background', "darkgray")
                        $(ele).parent().removeClass('rx-main-tab')
                    }
                })
            }, 100);
        });
    </script>
</head>

<div id="topic-page">

    <div class="row">
        <div class="col-sm-12">
            <div class="page-title-box">
                <div class="fixed-page-head">
                    <div class="page-head-lt">
                        <div class="row">
                            <div class="col-md-6"><h5>${topic.name}</h5></div>

                            <div class="col-md-6"><h5>Product Name: ${topic.getProductNameList().join(',')}</h5></div>
                        </div>
                    </div>

                    <div class="page-head-rt">
                        <div class="row">
                            <div class="col-md-6">
                                <h5 class="m-t-5">Due Date: <g:formatDate type="date" style="MEDIUM"
                                                                          date="${topic.endDate}"/></h5>
                            </div>

                            <div class="col-md-6">
                                <a class="btn btn-default pull-right" href="/signal/validatedSignal/index">
                                    <i class="fa fa-long-arrow-left" aria-hidden="true"></i> Back</a>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>


    <div class="row">
        <div class="col-sm-12">
            <!-- Nav tabs -->
            <ul id="detail-tabs" class="validation-tab m-b-5 p-0">

                <li class="active" role="presentation">
                    <a href="#details" aria-controls="details" role="tab" data-toggle="tab">
                        <g:message code="app.label.signal.management.review" default="Validated Observations"/>
                    </a>
                </li>

                <li role="presentation">
                    <a href="#assessments" id="assessmentTab" aria-controls="assessments" role="tab" data-toggle="tab">
                        <g:message code="app.label.signal.management.assessments" default="Assessment and References"/>
                    </a>
                </li>
                <li role="presentation">
                    <a href="#notifications" aria-controls="notifications" role="tab" data-toggle="tab">
                        <g:message code="app.label.signal.management.actions.and.workflow"
                                   default="Actions And Workflow"/>
                    </a>
                </li>
                <li role="presentation">
                    <a href="#activities" aria-controls="activities" role="tab" data-toggle="tab" id="topicActivities">
                        <g:message code="app.label.signal.management.activity.log" default="Activity Log"/>
                    </a>
                </li>
            </ul>
            <!-- Tab panes -->
            <div class="tab-content pvs-validate-tabpan">

                <div id="details" class="tab-pane active m-b-10" role="tabpanel">
                    <g:render template="review" model="[topic: topic]"/>
                </div>

                <div id="assessments" class="tab-pane fade m-b-10" role="tabpanel">
                    <g:render template="includes/assessment"
                              model="[strategy: topic, emergingIssues: emergingIssues, chartCount: chartCount, specialPEList: specialPEList, conceptsMap: conceptsMap, heatMap: heatMap, assessmentDetails: assessmentDetails]"/>
                </div>

                <div id="notifications" class="tab-pane fade m-b-10" role="tabpanel">
                    <g:render template="actionsAndWorkflow" model="[topic: topic, actionConfigList: actionConfigList]"/>
                </div>

                <div id="activities" class="tab-pane fade m-b-10" role="tabpanel">
                    <g:render template="includes/activities"/>
                </div>
            </div>
        </div>
    </div>

    <input type="hidden" id="topicName" value="${topic.name}"/>
    <input type="hidden" id="topicIdPartner" value="${topic.id}"/>
    <input type="hidden" id="caseCountArgus" value="${caseCount}"/>
    <input type="hidden" id="pecCountArgus" value="${pecCountArgus}"/>
    <input type="hidden" id="isEvdasEnabled" value="${grailsApplication.config.signal.evdas.enabled}"/>
    <input type="hidden" id="pecCountEvdas" value="0"/>

    <script>

        $(document).ready(function () {

            $("a[data-toggle=\"tab\"]").on("shown.bs.tab", function (e) {
                $('#action-table').DataTable().columns.adjust();
                $('#meeting-table').DataTable().columns.adjust();
                $('#topicActivityTable').DataTable().columns.adjust();
            });
        });
    </script>
    <g:render template="/includes/modals/case_series_modal"/>
    <g:render template="/includes/modals/validated_signal_dissociation_modal"/>
    <asset:javascript src="app/pvs/validated_signal/prev_assessment.js"/>

</div>