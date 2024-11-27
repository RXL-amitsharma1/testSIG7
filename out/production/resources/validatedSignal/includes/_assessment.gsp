<%@ page import="com.rxlogix.util.ViewHelper ;com.rxlogix.Constants ; grails.util.Holders" %>
<script>

    $(document).ready(function () {
        $(".refresh-charts").hide();
        $("#genSignalSummary").click(function () {
            $("#signal-summary").html($("#summary").html());
        });

        $(".causality-assessment").click(function () {
            var causalityModal = $("#causalityModal");
            causalityModal.modal("show");
        });




    });
    $('a#assessmentTab').click(function () {
        if (!$('#showProductSelectionAssessment').val()) {
            $('#showProductSelectionAssessment').html($('#showProductSelection').html());
        }
        if (!$('#showEventSelectionAssessment').val()) {
            $('#showEventSelectionAssessment').html($('#showEventSelection').html());
        }
        $(".generate-assessment-reports").click();
        $('a#assessmentTab').unbind("click");
    });



    var joinValuesInObjectArray = function (resultSet, prop) {
        return resultSet.reduce(function (a, b) {
            return a + ["", ", "][+!!a.length] + b[prop];
        }, "")
    }
</script>
<div class="rxmain-container panel-group pv-max-scrollable-table">
    <div class="panel panel-default rxmain-container rxmain-container-top">
        <div class="rxmain-container-row rxmain-container-header panel-heading pv-sec-heading">
            <label class="rxmain-container-header-label">
                <a data-toggle="collapse" href="#accordion-pvs-references">
                    ${message(code: 'signal.workflow.reference.modal.title')}
                </a>
            </label>
            <span class="pv-head-config configureFields ">
                <a href="javascript:void(0);" class="ic-sm action-search-btn hide" title="Search">
                    <i class="md md-search" aria-hidden="true"></i>
                </a>
                    <a href="#" class="pull-right ic-sm ${buttonClass}" id="addReference" title="Add reference">
                        <i class="md md-add" aria-hidden="true"></i>
                    </a>
                <a href="javascript:void(0);" title="Remove reference" id="removeReference" class="table-row-del hidden-ic hide ${buttonClass}">
                    <i class="mdi mdi-close" aria-hidden="true"></i>
                </a>
            </span>

        </div>

        <div class="panel-collapse rxmain-container-content rxmain-container-show collapse in pv-scrollable-dt" id="accordion-pvs-references">
            <table id="reference-table" class="dataTable table table-striped row-border hover no-footer">
                <thead>
                <tr>
                    <th class="col-md-1-half" data-field="type";>Reference Type<span class="required-indicator">*</span></th>
                    <th class="col-md-3" data-field="description" >Description</th>
                    <th class="col-md-3" data-field="link">File Name<span class="required-indicator">*</span></th>
                    <th class="col-md-1-half" data-field="modifiedBy">Added By</th>
                    <th class="col-md-2" data-field="timeStamp">Date</th>
                    <th class="col-md-1" ></th>
                </tr>
                </thead>
                <tfoot class="reference-table-foot hide">
                <tr role="row">
                    <td class="col-md-1-half">
                        <g:select name="signalStatus"  id="assessmentReferenceType" from="${referenceType}" optionKey="key"
                                  optionValue="value" value="Others" class="form-control status assessmentReferenceType"/>
                    </td>
                    <td class="col-md-3">
                        <div class="textarea-ext">
                            <textarea id="referencesTextArea" type="text" rows="1" class="form-control comment" maxlength="8000" style="width: 100%;min-height: 25px"></textarea>
                            <a class="btn-text-ext openStatusComment" href="javascript:void(0);" tabindex="0"
                               title="Open in extended form">
                                <i class="mdi mdi-arrow-expand font-20 blue-1"></i></a>
                        </div>
                    </td>
                    <td class="col-md-3">
                        <div class="file-uploader" data-provides="fileupload">
                            <input type="file" name="assessment-file" class="file" id="assessmentFile">
                            <input type="text" class="referenceName hide">
                            <input type="text" class="referenceLink hide">

                            <div class="input-group" style="width: 100%">
                                <input type="text" class="form-control attachmentFilePath" placeholder="Attach a file"
                                       id="attachmentFilePath" name="assessment-file" value="" title="" disabled>
                                <span class="input-group-btn ">
                                    <button class="btn btn-primary btn-file-upload allowEdit" type="button"
                                            data-toggle="modal" data-target="#attachmentFileModal">
                                        <i class="glyphicon glyphicon-search"></i>
                                    </button>
                                </span>

                            </div>
                        </div>
                    </td>
                    <td class="col-md-1-half">
                        <div>
                            ${currentUserFullName}
                        </div>
                    </td>
                    <td class="col-md-2" style="padding: 10px 5px 3px 20px">
                        <div class="currentDate">
                        </div>
                    </td>
                    <td class="col-md-1">
                        <a href="javascript:void(0);" title="Save" class="table-row-save hidden-ic pv-ic saveAttachment"
                           data-editing="false">
                            <i class="mdi mdi-check" aria-hidden="true"></i>
                        </a>
                    </td>
                </tr>
                </tfoot>
            </table>
        </div>
    </div>

    <div class="rxmain-container-inner panel panel-default rxmain-container rxmain-container-top">
        <div class="rxmain-container-row rxmain-container-header panel-heading pv-sec-heading">
            <label class="rxmain-container-header-label">
                <a data-toggle="collapse" href="#accordion-pvs-assessments">
                    Assessment Reports
                </a>
            </label>
        </div>

        <div class="panel-collapse rxmain-container-content rxmain-container-show collapse in" id="accordion-pvs-assessments">
            <div class="rxmain-container inner-table">
                <div class="rxmain-container-inner panel panel-default">
                    <div class="rxmain-container-row rxmain-container-header panel-heading" data-toggle="collapse"
                         href="#assessmentFilterPanel"
                         aria-expanded="false" aria-controls="assessmentFilterPanel">
                        <label class="rxmain-container-header-label">Assessment Filter Panel</label>
                    </div>

                    <div class="collapse in" id="assessmentFilterPanel">
                        <div class="rxmain-container-content ">
                            <div class="row">
                                %{--<div class="col-md-2">--}%
                                    %{--<label>Data Source:</label> <br/> ${Holders.config.signal.dataSource.safety.name}--}%
                                %{--<g:hiddenField name="dataSources" value="pva" class="selectedDatasource"/>--}%
                                %{--</div>--}%

                                <div class="col-md-2">
                                    <div class="col-xs-12">
                                        <label><g:message code="app.label.DateRange"/></label>
                                        <g:select name="dateRange"
                                                  from="${ViewHelper.getAssessmentFilterDateRange()}"
                                                  optionValue="display"
                                                  optionKey="name"
                                                  value="LAST_1_YEAR"
                                                  class="form-control" required="required"/>
                                    </div>
                                <div class="hide col-xs-12" id="assessmentCustomDateRange">
                                    <div class="fuelux">
                                        <div class="datepicker toolbarInline historyDatePicker">
                                            <div class="input-group">
                                                <input placeholder="Select Start Date" id="assessmentCustomStartDate"
                                                       class="form-control start-date"
                                                       name="Date" type="text"/>
                                                <g:render template="/includes/widgets/datePickerTemplate"/>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="fuelux">
                                        <div class="datepicker toolbarInline historyDatePicker">
                                            <div class="input-group">
                                                <input placeholder="Select End Date" id="assessmentCustomEndDate"
                                                       class="form-control end-date"
                                                       name="Date" type="text"/>
                                                <g:render template="/includes/widgets/datePickerTemplate"/>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                </div>

                                <div class="col-md-4">
                                    <label>
                                        <g:message code="app.label.productSelection"/>
                                        <span class="required-indicator">*</span>
                                    </label>

                                    <div class="wrapper">
                                        <div id="showProductSelectionAssessment" class="showDictionarySelection">
                                        </div>

                                        <div class="iconSearch">
                                            <a tabindex="0" id="searchProductsAssessment" data-toggle="modal" data-target="#productModalAssessment" class="productRadio">
                                                <i class="fa fa-search "></i>
                                            </a>
                                        </div>
                                    </div>
                                    <g:hiddenField name="productSelectionAssessment" value="${validatedSignal?.products}"/>
                                    <g:hiddenField name="productGroupSelectionAssessment" value="${validatedSignal?.productGroupSelection}"/>
                                </div>

                                <div class="col-md-4">
                                    <label><g:message code="app.label.eventSelection"/>
                                        <span class="required-indicator">*</span></label>

                                    <div class="wrapper">
                                        <div id="showEventSelectionAssessment" class="showDictionarySelection"></div>

                                        <div class="iconSearch">
                                            <a tabindex="0" id="searchEventsAssessment" data-toggle="modal" data-target="#eventModalAssessment">
                                                <i class="fa fa-search"></i>
                                            </a>
                                        </div>
                                    </div>
                                    <g:textField name="eventSelectionAssessment" value="${validatedSignal?.events}" hidden="hidden"/>
                                    <g:textField name="eventGroupSelectionAssessment" value="${validatedSignal?.eventGroupSelection}" hidden="hidden"/>
                                </div>
                                <input type="hidden" name="isMultiIngredient" id="multiIngredientCheck" value="true">

                                <div class="col-md-2">
                                    <g:if test="${isEnableSignalCharts}">
                                        <span class="generate-assessment-reports save btn btn-large btn-primary">Generate</span>
                                    </g:if>
                                    <g:if test="${isSpotfireEnabled}">
                                        <span class="data-analysis data-analysis-assessment btn btn-large btn-primary ${buttonClass}">Data Analysis</span>
                                    </g:if>
                                    <span id="report-generating hide" style=" font-size:20px; display: none;" class="fa fa-spinner fa-spin"></span>

                                </div>
                            </div>

                        </div>
                    </div>
                </div>
            </div>

            <g:if test="${isSpotfireEnabled}">
            <div class="rxmain-container inner-table">
                <div class="rxmain-container-inner panel panel-default">
                    <div class="rxmain-container-row rxmain-container-header panel-heading pv-sec-heading">
                        <label class="rxmain-container-header-label">
                            <a data-toggle="collapse" href="#analysisDetails" data-original-title="" title="">
                                Data Analysis
                            </a>
                        </label>
                        <span class="pv-head-config configureFields ">
                            <a href="#" class="pull-right ic-sm refresh-analysis-table" title="Refresh"
                               data-id="assessmentDetails">
                                <i class="md md-refresh "></i>
                            </a>
                        </span>
                    </div>
                    <div class="collapse in" id="analysisDetails">
                        <div class="rxmain-container-content pv-scrollable-dt">
                            <table id="signal-analysis-table" class="dataTable table table-striped row-border hover no-footer">
                                <thead>
                                <tr>
                                    <th class="col-md-3 sorting"><g:message code="signal.rmms.label.fileName"/></th>
                                    <th class="col-md-2 sorting"><g:message code="app.label.product"/></th>
                                    <th class="col-md-2 sorting"><g:message code="app.label.event"/></th>
                                    <th class="col-md-2 sorting"><g:message code="label.evdas.data.file.list.date.range"/></th>
                                    <th class="col-md-1-half sorting"><g:message code="app.label.generatedBy"/></th>
                                    <th class="col-md-1-half sorting"><g:message code="app.label.generatedOn"/></th>
                                </tr>
                                </thead>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
            </g:if>

            <g:if test="${isEnableSignalCharts}">
                <div class="rxmain-container inner-table" id="assessmentDetailsHide">
                    <div class="rxmain-container-inner panel panel-default">
                        <div class="rxmain-container-row rxmain-container-header panel-heading pv-sec-heading">
                            <label class="rxmain-container-header-label">
                                <a data-toggle="collapse" href="#assessmentDetails" data-original-title="" title="">
                                    Assessment Details
                                </a>
                            </label>
                            <span class="pv-head-config configureFields ">
                            <a href="#" class="pull-right ic-sm addReference ${buttonClass}" data-name="${Constants.ReferenceName.ASSESSMENT_DETAILS}" title="Add Assessment details" data-id="7483">
                                <i class="md md-add" aria-hidden="true"></i>
                            </a>
                            <a href="#" class="pull-right ic-sm refresh-table" title="Refresh" data-id="assessmentDetails">
                                <i class="md md-refresh "></i>
                            </a>
                            </span>
                        </div>
                        <div class="collapse in" id="assessmentDetails">
                            <div class="rxmain-container-content ">
                            </div>
                        </div>
                    </div>
                </div>
            </g:if>

            <!-- Graphs comes here. -->
            <g:if test="${isEnableSignalCharts}">
                <div class="rxmain-container inner-table" id="chartsHide">
                    <div class="rxmain-container-inner panel panel-default">
                        <div class="rxmain-container-row rxmain-container-header panel-heading pv-sec-heading">
                            <label class="rxmain-container-header-label">
                                <a data-toggle="collapse" href="#graphs" data-original-title="" title="">
                                    Charts
                                </a>
                            </label>
                            <span class="pv-head-config configureFields ">
                            <a href="#" class="pull-right ic-sm addReference ${buttonClass}" data-name="${Constants.ReferenceName.CHARTS}" title="Add Charts" data-id="7483">
                                <i class="md md-add" aria-hidden="true"></i>
                            </a>
                            <a href="#" class="pull-right ic-sm refresh-table" title="Refresh" data-id="assessmentDetails">
                                <i class="md md-refresh "></i>
                            </a>
                            </span>
                        </div>
                        <div class="collapse in" id="graphs">
                            <div class=" ">
                                <div class="row">
                                    <div class="col-md-4">
                                        <div class="panel panel-default">
                                            <div class="panel-heading panel-title">
                                                <div class="rxmain-container-header-label">
                                                    <label class="m-t-5">Seriousness Criteria Counts Over Time</label>
                                                    <span class="configureFields ">
                                                    <a href="#" class="pull-right ic-sm m-l-5 addReference ${buttonClass}" data-name="${Constants.ReferenceName.SERIOUSNESS_COUNTS_OVER_TIME}" title="Add Seriousness Criteria Counts Over Time">
                                                        <i class="md md-add" aria-hidden="true"></i>
                                                    </a>
                                                    <a href="#" class="pull-right ic-sm refresh-charts" title="Refresh" data-id="seriousness-over-time-chart">
                                                        <i class="md md-refresh "></i>
                                                    </a>
                                                    </span>
                                                </div>
                                            </div>
                                            <div class="panel-body">
                                                <div id="severity" class="maxWidth">
                                                    <div id="seriousness-over-time-chart" class="assessment-chart" style="height: 400px"></div>
                                                </div>
                                                <input type="hidden" class="executedId"/>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-sm-4">
                                        <div class="panel panel-default ">
                                            <div class="panel-heading panel-title">
                                                <div class="rxmain-container-header-label">
                                                    <label class="m-t-5">Distribution By Age Group Over Time</label>
                                                    <span class="configureFields ">
                                                    <a href="#" class="pull-right ic-sm m-l-5 addReference ${buttonClass}" data-name="${Constants.ReferenceName.AGE_GROUP_OVER_TIME}" title="Add Distribution by age group over time">
                                                        <i class="md md-add" aria-hidden="true"></i>
                                                    </a>
                                                    <a href="#" class="pull-right ic-sm refresh-charts" title="Refresh" data-id="age-grp-over-time-chart">
                                                        <i class="md md-refresh "></i>
                                                    </a>
                                                    </span>
                                                </div>
                                            </div>
                                            <div class="panel-body">
                                                <div id="ageGroup" class="maxWidth">
                                                    <div id="age-grp-over-time-chart" class="assessment-chart" style="height: 400px"></div>
                                                </div>
                                                <input type="hidden" class="executedId"/>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-md-4">
                                        <div class="panel panel-default ">
                                            <div class="panel-heading panel-title">
                                                <div class="rxmain-container-header-label">
                                                    <label class="m-t-5">Distribution By Gender Over Time</label>
                                                    <span class="configureFields ">
                                                    <a href="#" class="pull-right ic-sm m-l-5 addReference ${buttonClass}" data-name="${Constants.ReferenceName.GENDER_OVER_TIME}" title="Add Distribution by gender over time">
                                                        <i class="md md-add" aria-hidden="true"></i>
                                                    </a>
                                                    <a href="#" class="pull-right ic-sm refresh-charts" title="Refresh" data-id="gender-over-time-chart">
                                                        <i class="md md-refresh "></i>
                                                    </a>
                                                    </span>
                                                </div>
                                            </div>
                                            <div class="panel-body">
                                                <div id="gender" class="maxWidth">
                                                    <div id="gender-over-time-chart" class="assessment-chart" style="height: 400px"></div>
                                                </div>
                                                <input type="hidden" class="executedId"/>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="col-sm-4">
                                        <div class="panel panel-default ">
                                            <div class="panel-heading panel-title">
                                                <div class="rxmain-container-header-label">
                                                    <label class="m-t-5">Distribution By Country Over Time</label>
                                                    <span class="configureFields ">
                                                    <a href="#" class="pull-right ic-sm m-l-5 addReference ${buttonClass}" data-name="${Constants.ReferenceName.COUNTY_OVER_TIME}" title="Add Distribution by country over time">
                                                        <i class="md md-add" aria-hidden="true"></i>
                                                    </a>
                                                    <a href="#" class="pull-right ic-sm refresh-charts" title="Refresh" data-id="country-over-time-chart">
                                                        <i class="md md-refresh "></i>
                                                    </a>
                                                    </span>
                                                </div>
                                            </div>
                                            <div class="panel-body">
                                                <div id="country" class="maxWidth">
                                                    <div id="country-over-time-chart" class="assessment-chart" style="height: 400px"></div>
                                                </div>
                                                <input type="hidden" class="executedId"/>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-md-4">
                                        <div class="panel panel-default ">
                                            <div class="panel-heading panel-title">
                                                <div class="rxmain-container-header-label">
                                                    <label class="m-t-5">Distribution By Case Outcome</label>
                                                    <span class="configureFields ">
                                                    <a href="#" class="pull-right ic-sm m-l-5 addReference ${buttonClass}" data-name="${Constants.ReferenceName.CASE_OUTCOME}" title="Add Distribution by case outcome">
                                                        <i class="md md-add" aria-hidden="true"></i>
                                                    </a>
                                                    <a href="#" class="pull-right ic-sm refresh-charts" title="Refresh" data-id="outcome-over-time-chart">
                                                        <i class="md md-refresh "></i>
                                                    </a>
                                                    </span>
                                                </div>
                                            </div>
                                            <div class="panel-body">
                                                <div id="outcome" class="maxWidth">
                                                    <div id="outcome-over-time-chart" class="assessment-chart" style="height: 400px"></div>
                                                </div>
                                                <input type="hidden" class="executedId"/>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-md-4">
                                        <div class="panel panel-default ">
                                            <div class="panel-heading panel-title">
                                                <div class="rxmain-container-header-label">
                                                    <label class="m-t-5">Distribution of HCP/Non-HCP Cases Overtime </label>
                                                    <span class="configureFields ">
                                                    <a href="#" class="pull-right ic-sm m-l-5 addReference ${buttonClass}" data-name="${Constants.ReferenceName.SOURCE_OVER_TIME}" title="Add Distribution by source over time">
                                                        <i class="md md-add" aria-hidden="true"></i>
                                                    </a>
                                                    <a href="#" class="pull-right ic-sm refresh-charts" title="Refresh" data-id="seriousness-count-pie-chart">
                                                        <i class="md md-refresh "></i>
                                                    </a>
                                                    </span>
                                                </div>
                                            </div>
                                            <div class="panel-body">
                                                <div id="seriousness-count" class="maxWidth">
                                                    <div id="seriousness-count-pie-chart" class="assessment-chart" style="height: 400px"></div>
                                                </div>
                                                <input type="hidden" class="executedId"/>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div class="row soc_data">
                                    <div class="row col-lg-12">
                                        <div class="panel panel-default align-center">
                                            <div class="panel-heading panel-title">
                                                <div class="rxmain-container-header-label">
                                                    <label class="m-t-5">Distribution By System Organ Class </label>
                                                    <span class="configureFields ">
                                                    <a href="#" class="pull-right ic-sm refresh-charts" title="Refresh" data-id="system-organ-heat-map">
                                                        <i class="md md-refresh "></i>
                                                    </a>
                                                    </span>
                                                </div>
                                            </div>
                                            <div class="panel-body">
                                                <div id="heat-map-chart" class="maxWidth">
                                                    <g:render template="includes/heat_map" model="${[heatMap: [:]]}"/>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </g:if>
        </div>
    </div>
</div>


<input type="hidden" value="${chartCount}" id="addedChartsCount"/>

<g:render template="/includes/modals/causality_assessment_modal"/>
<g:render template="/includes/modals/previous_assessment_modal"/>
<g:render template="includes/assessment_product_selection_modal" model="['multiIngredientValue': strategy?.isMultiIngredient,isPVCM:isPVCM]"/>
<g:render template="includes/assessement_event_selection_modal"/>

<asset:javascript src="app/pvs/common/rx_common.js"/>
<asset:javascript src="app/pvs/validated_signal/signal_charts.js"/>
