<%@ page import="com.rxlogix.signal.SystemConfig; com.rxlogix.pvdictionary.config.PVDictionaryConfig; com.rxlogix.signal.SignalOutcome; com.rxlogix.enums.ReportFormat;grails.util.Holders; com.rxlogix.Constants;grails.converters.JSON; grails.plugin.springsecurity.SpringSecurityUtils; com.rxlogix.AlertAttributesService;com.rxlogix.config.Disposition" contentType="text/html;charset=UTF-8" defaultCodec="none" %>
<g:set var="grailsApplication" bean="grailsApplication"/>
<g:set var="alertAttributesService" bean="alertAttributesService"/>

<head>
    <meta name="layout" content="main"/>
    <title><g:message code="validated.signals.label"/></title>
    <asset:javascript src="app/pvs/validated_signal/assessment_dictionary.js"/>
    <g:javascript>
        var hasConfigurationEditorRole = "true";
        var isProductAssignment = false;
        var isEmerging = false;
        var referenceType = ${referenceTypeJson}
        var addAssessmentReportUrl = "${createLink(controller: 'validatedSignal', action: 'fetchSignalAssessmentReport', params: [signalId: signal.id, outputFormat: ReportFormat.PDF, reportType: Constants.SignalReportTypes.PEBER])}";
        var dataSourcesColorMapValues = '{"faers": "rgb(112, 193, 179)", "eudra": "rgb(157, 129, 137)", "vaers": "rgb(92, 184, 92)", "vigibase": "rgb(255, 87, 51)"}';
        var singleCaseUpdateJustificationUrl = "${createLink(controller: "caseHistory", action: 'updateJustification')}";
        var aggUpdateJustificationUrl = "${createLink(controller: "productEventHistory", action: 'updateJustification')}";
        var isArchived = false;
        var VALIDATED = {
            scaListUrl: "${createLink(controller: 'validatedSignal', action: 'singleCaseAlertList')}",
            acaListUrl: "${createLink(controller: 'validatedSignal', action: 'aggregateCaseAlertList')}",
            literatureAlertListUrl: "${createLink(controller: 'validatedSignal', action: 'literatureAlertList')}",
            adHocListUrl: "${createLink(controller: 'validatedSignal', action: 'adHocAlertList')}",
            assessmentFilterUrl: "${createLink(controller: 'validatedSignal', action: 'aggregateCaseAlertProductAndEventList')}"
        };
        var isCommentAdded = true;
        var saveWorkflowStateUrl = "${createLink(controller: "validatedSignal", action: "saveWorkflowState")}";
        var saveAssignedTo = "${createLink(controller: "validatedSignal", action: "saveAssignedTo")}";
        var template_list_url = "${createLink(controller: 'template', action: 'index')}";
        var generateCaseSeriesUrl = "${createLink(controller: 'aggregateCaseAlert', action: 'generateCaseSeries')}";
        var caseDetailUrl = "${createLink(controller: "caseInfo", action: 'caseDetail')}";
        var getWorkflowUrl = "${createLink(controller: "workflow", action: 'getWorkflowState')}";
        var caseHistoryUrl = "${createLink(controller: "caseHistory", action: 'listCaseHistory')}";
        var evdasHistoryUrl = "${createLink(controller: "evdasHistory", action: "listEvdasHistory")}";
        var caseHistorySuspectUrl = "${createLink(controller: "caseHistory", action: 'listSuspectProdCaseHistory')}";
        var productEventHistoryUrl = "${createLink(controller: "productEventHistory", action: 'listProductEventHistory')}";
        var getPriorityUrl = "${createLink(controller: "validatedSignal", action: 'getPriorities')}";
        var activityUrl = "${createLink(controller: "activity", action: 'activitiesBySignal', params: [id: signal.id])}";
        var previousAssessmentUrl = "${createLink(controller: "validatedSignal", action: 'listPreviousSignals',
            params: [id: signal.id])}";
        var graphReportRestUrl = "${createLink(controller: 'validatedSignal', action: 'graphReport')}";
        var dispositionData = JSON.parse('${dispositionData}')
        var uploadSignalAssessmentReportUrl = "${createLink(controller: 'validatedSignal', action: 'uploadSignalAssessmentReport')}";
        var fetchAttachmentsUrl = "${createLink(controller: 'validatedSignal', action: 'fetchAttachments')}";
        var fetchAnalysisDataUrl = "${createLink(controller: 'validatedSignal', action: 'fetchAnalysisData')}";
        var updateAttachmentUrl = "${createLink(controller: 'validatedSignal', action: 'updateAttachment')}";
        var updateReferenceUrl = "${createLink(controller: 'validatedSignal', action: 'updateReference')}";
        var uploadUrl = "${createLink(controller: 'validatedSignal', action: 'upload')}";
        var addReferenceUrl = "${createLink(controller: 'validatedSignal', action: 'addReference')}";
        var saveAssessmentNotesUrl = "${createLink(controller: 'validatedSignal', action: 'saveAssessmentNotes')}";
        var fetchAssessmentNotesUrl = "${createLink(controller: 'validatedSignal', action: 'fetchAssessmentNotes')}";
        var xAxis = ${heatMap.years};
        var yAxis = ${heatMap.socs};
        var chartData = ${heatMap.data};
        var required = ${summaryReportPreference.required};
        var ignored = ${summaryReportPreference.ignore};
        var callingScreen;
        var dmsFoldersUrl = "${createLink(controller: 'controlPanel', action: 'getDmsFolders')}";
        var fetchSignalStatusUrl = "${createLink(controller: "validatedSignal", action: "fetchSignalStatus", params: [id: signal.id])}";
        var createSingleCaseAlert = "${createLink(controller: 'singleCaseAlert', action: 'create')}";
        var pvrIntegrate = "${grailsApplication.config.pvreports.url ? true : false}";
        var spotfireIntegrate = "${grailsApplication.config.signal.spotfire.enabled}";
        var updateSignalUrl = "${createLink(controller: 'validatedSignal', action: 'update')}";
        var singleCaseDetailsUrl = "${createLink(controller: 'singleCaseAlert', action: 'details')}";
        var singleCaseOnDemandDetailsUrl = "${createLink(controller: 'singleOnDemandAlert', action: 'adhocDetails')}";
        var aggregateCaseDetailsUrl = "${createLink(controller: 'aggregateCaseAlert', action: 'details')}";
        var aggregateCaseOnDemandDetailsUrl = "${createLink(controller: 'aggregateOnDemandAlert', action: 'adhocDetails')}";
        var tagDetailsUrl = "${createLink(controller: 'aggregateCaseAlert', action: 'details')}";
        var evdasDetailsUrl = "${createLink(controller: 'evdasAlert', action: 'details')}";
        var evdasOnDemandDetailsUrl = "${createLink(controller: 'evdasOnDemandAlert', action: 'adhocDetails')}";
        var detailsUrl = "${createLink(controller: 'aggregateCaseAlert', action: 'details')}";
        var linkedConfigurationUrl = "${createLink(controller: 'validatedSignal', action: 'fetchLinkedConfiguration', params: [id: signal.id])}";
        var changePriorityUrl = "${createLink(controller: 'validatedSignal', action: 'changePriorityOfSignal')}";
        var changeDispositionUrl = "${createLink(controller: 'validatedSignal', action: 'changeDisposition')}";
        var revertDispositionUrl = "${createLink(controller: 'validatedSignal', action: 'revertDisposition')}";
        var reviewCompletedDispostionList = JSON.parse('${reviewCompletedDispostionList}');
        var dispositionIncomingOutgoingMap = JSON.parse('${dispositionIncomingOutgoingMap}');
        var forceJustification = ${forceJustification};
        var evdasCaseDetailUrl = "${createLink(controller: "caseInfo", action: 'evdasCaseDetail')}";
        var fetchDrillDownDataUrl = "${createLink(controller: "evdasAlert", action: "fetchCaseDrillDownData")}";
        var pubMedUrl = "${literatureArticleUrl}";
        var authUrl = "${createLink(controller: 'user', action: 'eAuthenticate')}";
        var getSmqDropdownListUrl = "${createLink(controller: 'eventDictionary', action: 'getSmqDropdownList')}";
        var options = { spinnerPath:"${assetPath(src: 'select2-spinner.gif')}" };
        var saveSignalStatusHistory = "${createLink(controller: 'validatedSignal', action: 'saveSignalStatusHistory')}";
        var refreshSignalHistory = "${createLink(controller: 'validatedSignal', action: 'refreshSignalHistory', params: [signalId: signal.id])}";
        var saveSignalRMMs = "${createLink(controller: 'validatedSignal', action: 'saveSignalRMMs')}";
        var deleteSignalRMMs = "${createLink(controller: 'validatedSignal', action: 'deleteSignalRMMs')}";
        var sentToListUrl = "${createLink(controller: 'user', action: 'usersEmailList')}";
        var sendMailUrl = "${createLink(controller: 'validatedSignal', action: 'sendEmailForRmms')}";
        var allowedProductsAsSafetyLead = "${allowedProductsAsSafetyLead}";
        var isProductSecurity = "${isProductSecurity}";
        var searchUserGroupListUrl = "${createLink(controller: 'user', action: 'searchUserGroupList')}";
        var generateSpotfireReportUrl = "${createLink(controller: 'validatedSignal', action: 'generateSpotfireReportForSignal')}";
        var enableSignalWorkflow = ${enableSignalWorkflow};
        var possibleDispositions = JSON.parse('${possibleDispositions}');
        var hasReviewerAccess = ${hasSignalReviewerAccess};
        var hasSignalCreationAccessAccess = ${hasSignalCreationAccessAccess};
        var hasSignalViewAccessAccess = ${hasSignalViewAccessAccess};
        var buttonClass = "${buttonClass}";
        var hasDataAnalysisRole = ${hasDataAnalysisRole};
        var rmmType = JSON.parse('${rmmType}');
        var communicationType = JSON.parse('${communicationType}');
        var rmmStatus = JSON.parse('${rmmStatus}');
        var dueIn = "${dueIn}";
        var validationDateSynchingEnabled="${grailsApplication.config.detectedDateAndValidationDate.synch.enabled}";
        var validatedDateDispositions = ${validatedDateDispositions as grails.converters.JSON};
        var isEnableSignalCharts = ${isEnableSignalCharts};
        var countrySpecificRMM = "${grailsApplication.config.signal.rmm.country}";
        var justificationObj = '${justificationJSON}';
        var ingredientLevel = ${PVDictionaryConfig.ingredientColumnIndex}
        var disabledValues = {
            initialDataSource: "${alertAttributesService.getDisabled('initialDataSource')}",
            detectedBy       : "${alertAttributesService.getDisabled('detectedBy')}",
            actionTaken      : "${alertAttributesService.getDisabled('actionsTaken')}",
            evaluationMethod : "${alertAttributesService.getDisabled('evaluationMethods')}",
            signalOutcome    : "${SignalOutcome.list().size() != 0 ? SignalOutcome.list().findAll { it.isDisabled }.collect { it.name } : alertAttributesService.getDisabled('signalOutcome')}",
            signalTypeList   : "${alertAttributesService.getDisabled('topicCategory')}",
            signalStatus     : "${alertAttributesService.getDisabled('signalHistoryStatus')}",
            haSignalStatus   : "${Disposition.findAllByDisplay(false)*.id}"
        }

        options.study = {
            levelNames: "${PVDictionaryConfig.StudyConfig.levels.join(",")}",
            dicColumnCount: ${PVDictionaryConfig.StudyConfig.columns.size()},
            selectUrl: "${createLink(controller: 'studyDictionary', action: 'getSelectedStudy')}",
            preLevelParentsUrl: "${createLink(controller: 'studyDictionary', action: 'getPreLevelStudyParents')}",
            searchUrl: "${createLink(controller: 'studyDictionary', action: 'searchStudies')}"
        };

         options.event = {
            levelNames: "${PVDictionaryConfig.EventConfig.levels.join(",")}",
            dicColumnCount: ${PVDictionaryConfig.EventConfig.columns.size()},
            selectUrl: "${createLink(controller: 'eventDictionary', action: 'getSelectedEvent')}",
            preLevelParentsUrl: "${createLink(controller: 'eventDictionary', action: 'getPreLevelEventParents')}",
            searchUrl: "${createLink(controller: 'eventDictionary', action: 'searchEvents')}"
        };

        options.product = {
            levelNames: "${PVDictionaryConfig.ProductConfig.views.collect { message(code: it.code) }.join(",")}",
            dicColumnCount: ${com.rxlogix.pvdictionary.config.PVDictionaryConfig.ProductConfig.columns.size()},
            selectUrl: "${createLink(controller: 'productDictionary', action: 'getSelectedItem')}",
            preLevelParentsUrl: "${createLink(controller: 'productDictionary', action: 'getPreLevelProductParents')}",
            searchUrl: "${createLink(controller: 'productDictionary', action: 'searchViews')}"
        };
        var availableAlertPriorityJustifications = "${availableAlertPriorityJustifications}";
        intializeDictionariesAssessment(options)
        var isPriorityEnabled = ${isPriorityEnabled};
    </g:javascript>

    <g:if test="${isTopicMigrated}">
        <script type="text/javascript">
            var text = "${signal?.name} has been upgraded to signal successfully";
            $(document).ready(function () {
                bootbox.alert(text);
                $('#haSignalStatusList').select2();
            });
        </script>
    </g:if>

    <dms:dmsJSLibrary/>
    <asset:javascript src="app/pvs/userGroupSelect.js"/>
    <asset:javascript src="highcharts.js"/>
    <asset:javascript src="vendorUi/highcharts/highcharts-more.js"/>
    <asset:javascript src="vendorUi/highcharts/highcharts-3d.js"/>
    <asset:javascript src="vendorUi/highcharts/themes/grid-rx.js"/>
    <asset:javascript src="yadcf/jquery.dataTables.yadcf.js"/>
    <asset:javascript src="app/pvs/dataTablesActionButtons.js"/>
    <asset:javascript src="app/pvs/caseHistory/caseHistoryTable.js"/>
    <asset:javascript src="app/pvs/validated_signal/validated_signal_charts.js"/>
    <asset:stylesheet src="yadcf/jquery.dataTables.yadcf.css"/>
    <asset:stylesheet src="components.css"/>
    <asset:stylesheet src="configuration.css"/>
    <asset:javascript src="app/pvs/validated_signal/validated_signal_create.js"/>
    <asset:javascript src="app/pvs/validated_signal/linked_configurations.js"/>
    <asset:javascript src="app/pvs/validated_signal/rmm_communication.js"/>
    <asset:javascript src="tinymce/tinymce.min.js"/>
    <asset:javascript src="app/bootstrap-modal-popover/bootstrap-modal-popover.js"/>
    <asset:javascript src="app/pvs/caseForm.js"/>
    <asset:javascript src="app/pvs/evdasHistory/evdasHistoryTable.js"/>
    <asset:javascript src="app/pvs/disposition/disposition-change.js"/>
    <asset:javascript src="app/pvs/Disassociation/disassociate.js"/>
    <asset:javascript src="app/pvs/priority/priority-change.js"/>
    <g:if test="${!Holders.config.pv.plugin.dictionary.enabled}">
        <asset:javascript src="app/pvs/alert_utils/alert_product_utils.js"/>
        <asset:javascript src="app/pvs/alert_utils/alert_event_utils.js"/>
        <asset:stylesheet src="dictionaries.css"/>
    </g:if>
    <g:else>
        <asset:javascript src="app/pvs/alert_utils/dictionary-utils.js"/>
    </g:else>
    <asset:javascript src="app/pvs/alert_utils/common_key_prevent.js"/>
    <asset:stylesheet src="vendorUi/popover/popover.min.css"/>
    <asset:javascript src="vendorUi/popover/popover.min.js"/>
    <asset:javascript src="app/pvs/configuration/copyPasteValues.js"/>
    <asset:javascript src="app/pvs/configuration/dictionaryMultiSearch.js"/>
    <asset:stylesheet src="copyPasteModal.css"/>

    <asset:stylesheet href="app/pvs/pvs_app_css.css"/>
    <asset:stylesheet src="app/pvs/pvs_list.css"/>
    <asset:stylesheet href="flag-icon/css/flag-icon.min.css"/>

    <style>

    .generate-assessment-reports, .data-analysis-assessment {
        width: 117px;
        margin-top: 25px;
    }

    #assessmentCustomDateRange .fuelux {
        padding: 6px 0px 0px 0px;
    }

    tfoot {
        color: #1e1e1e;
    }


    .dropdown-submenu {
        position: relative;
    }

    .dropdown-submenu > .dropdown-menu {
        top: 10px;
        left: -160px;
        margin-top: -6px;
        margin-left: -1px;
        -webkit-border-radius: 0 6px 6px 6px;
        -moz-border-radius: 0 6px 6px;
        border-radius: 0 6px 6px 6px;
    }

    .dropdown-submenu:hover > .dropdown-menu {
        display: block;
    }

    .dropdown-submenu > a:after {
        display: block;
        content: " ";
        float: right;
        width: 0;
        height: 0;
        border-color: transparent;
        border-style: solid;
        border-width: 5px 0 5px 5px;
        border-left-color: #ccc;
        margin-top: 5px;
        margin-right: -10px;
    }

    .dropdown-submenu:hover > a:after {
        border-left-color: #fff;
    }

    .dropdown-submenu.pull-left {
        float: none;
    }

    .dropdown-submenu.pull-left > .dropdown-menu {
        left: -100%;
        margin-left: 10px;
        -webkit-border-radius: 6px 0 6px 6px;
        -moz-border-radius: 6px 0 6px 6px;
        border-radius: 6px 0 6px 6px;
    }

    #disassociationJustificationPopover {
        left: 10px;
        top: 10px;
    }

    #eventGroupModalAssessment {
        text-align: center;
        padding: 0 !important;
    }

    #eventGroupModalAssessment:before {
        content: '';
        display: inline-block;
        height: 100%;
        vertical-align: middle;
        margin-right: -4px;
    }

    #topic-page, #adhoc-detail {
        margin-bottom: 0px;
    }

    #rxTableAggregateReview_wrapper .pt-15 {
        padding-top: 8px !important;
    }

    #signal-communication-table_wrapper .pt-15 {
        padding-top: 8px !important;
        width: auto
    }

    #signal-rmms-table_wrapper .pt-15 {
        padding-top: 8px !important;
        width: auto
    }

    #signal-analysis-table_info {
        padding-top: 0.2em !important;
    }

    #edit-boxDis {
        padding-bottom: 10px;
    }

    .countBox {
        padding-bottom: 10px;
    }

    .revert-icon {
        display: flex;
        flex-direction: row;
    }

    </style>
</head>

<body>
<input type="hidden" name="labelConfigJson" id="labelConfigJson" value="${labelConfigJson}">
<input type="hidden" name="labelConfigNew" id="labelConfigNew" value="${labelConfigNew}">
<input type="hidden" name="labelConfig" id="labelConfig" value="${labelConfig}">
<input type="hidden" name="labelConfigCopy" id="labelConfigCopy" value="${labelConfigCopy}">
<input type="hidden" name="labelConfigCopyJson" id="labelConfigCopyJson" value="${labelConfigCopy as grails.converters.JSON}">
<input type="hidden" name="labelConfigKeyId" id="labelConfigKeyId" value='${labelConfigKeyId}'>
<g:render template="/includes/modals/addCaseModal"
          model="[id: 0, justification: justification]"/>

<div id="topic-page">

    <div class="row">
        <div class="col-sm-12">
            <div class="page-title-box">
                <div class="fixed-page-head">
                    <div class="page-head-lt" style="width:100%">
                        <div class="col-md-12">
                            <span class="col-md-2">
                                <h5><g:message code="app.label.signal.name"/></h5>
                                <input type="hidden" id="signalId" value="${signal.id}">
                                <h4 class="ellipsis validatedSignalName" id="validatedSignalName"
                                    data-toggle="validatedSignalNameTooltip"
                                    data-placement="bottom" title="${signal.name.encodeAsHTML()}">${signal.name.encodeAsHTML()}</h4>
                            </span>
                            <span class="col-md-1">
                                <h5><g:message code="app.label.priority.details"/></h5>
                                <h4 class="signalHeaderPriority">
                                    <g:if test="${allowedProductsAsSafetyLead}">
                                        <a data-target="#priorityPopover" class="changePriority ico-circle"
                                           role="button"
                                           data-toggle="modal-popover" data-placement="right"
                                           title="${signal.priority?.value}" style="font-size: 24px!important">
                                            <i class="${signal.priority?.iconClass}"></i>
                                        </a>
                                    </g:if>
                                    <g:else>
                                        <a class="priorityChangeNotAllowed ico-circle" role="button"
                                           data-toggle="modal-popover" data-placement="right"
                                           title="${signal.priority?.value}" style="font-size: 24px!important">
                                            <i class="${signal.priority?.iconClass}"></i>
                                        </a>
                                    </g:else>
                                </h4>
                            </span>

                            <span class="col-md-2 ${!enableSignalWorkflow ? 'hide' : ''}">
                                <h5><g:message code="app.label.signal.workflow.state"/></h5>
                                <h4>
                                    <span class="css-truncate expandable">
                                        <span class="branch-ref css-truncate-target">
                                            <a href="#" id="workflowHeader" class="workflowLink ellipsis"
                                               data-placement="bottom"
                                               title="${signal.workflowState}">${signal.workflowState}</a>
                                            <g:select class="manageWorkflowStateHeader form-control select2"
                                                      name="manageWorkflowStateHeader"
                                                      from="${workflowStatesSignal}"/>
                                        </span>
                                    </span>
                                </h4>
                            </span>

                            <span class="col-md-2 currentDispositionHead">
                                <h5><g:message code="app.label.current.disposition"/></h5>

                                <div id="dialog" style="display:none" title=""></div>
                                <span class="revert-icon">
                                    <h4 class="ellipsis dispositionId" data-placement="bottom" style="width: auto"
                                        data-id="${signal.disposition?.id}"
                                        title="${signal.disposition?.displayName}">${signal.disposition?.displayName}
                                    </h4>
                                    <input type="hidden" id="showUndoIconButton"
                                           value="${isUndoEnabled && ((SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN")) || signal.dispPerformedBy == currUserName)}">
                                    <g:if test="${isUndoEnabled && ((SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN")) || signal.dispPerformedBy == currUserName)}">
                                        <span href="#" class="revertDisposition" id="revertDisposition" title = "Undo Disposition Change" style="margin-left: 6px; margin-top: 2px">
                                            <span class="md md-undo m-r-10"></span>
                                        </span>
                                    </g:if>
                                </span>
                            </span>

                            <span class="col-md-2 disposition">
                                <h5><g:message code="app.label.disposition.to"/></h5>
                                <h4 class="disposition">
                                    <span class="disposition currentDispOptions">
                                        <ul class="list-inline icon-list txt-ellipsis"
                                            data-current-disposition="${signal.disposition.displayName}">
                                            <g:if test="${forceJustification}">
                                                <g:each in="${currentDispositionOptions}">
                                                    <li>
                                                        <a data-target="#dispositionJustificationPopover" role="button"
                                                           class="changeDisposition"
                                                           data-validated-confirmed="${false}"
                                                           data-disposition-id="${it.id}"
                                                           data-auth-required="${it.isApprovalRequired}"
                                                           data-toggle="modal-popover"
                                                           data-placement="bottom"
                                                           title="${it.displayName}">
                                                            <i class="ico-circle"
                                                               style="background:${it.colorCode}">${it.abbreviation}</i>
                                                        </a>
                                                    </li>
                                                </g:each>
                                            </g:if>
                                            <g:else>
                                                <g:each in="${currentDispositionOptions}">
                                                    <li>
                                                        <a data-target="#" role="button" class="changeDisposition"
                                                           data-validated-confirmed="${false}"
                                                           data-disposition-id="${it.id}"
                                                           data-auth-required="${it.isApprovalRequired}"
                                                           title="${it.displayName}">
                                                            <i class="ico-circle"
                                                               style="background:${it.colorCode}">${it.abbreviation}</i>
                                                        </a>
                                                    </li>
                                                </g:each>
                                            </g:else>
                                        </ul>
                                    </span>
                                </h4>
                            </span>

                            <span class="${enableSignalWorkflow ? 'col-md-1' : 'col-md-2'}">
                                <h5><g:message code="app.label.assigned.to"/></h5>
                                <h4>
                                    <span class="css-truncate expandable">
                                        <span class="branch-ref css-truncate-target">
                                            <a href="#"
                                               id="assignedToAction" title="${signal.assignedTo?.name.encodeAsHTML() ?: signal.assignedToGroup.name.encodeAsHTML()}">${signal.assignedTo?.name.encodeAsHTML() ?: signal.assignedToGroup.name.encodeAsHTML()}</a>
                                            <span class="assigned">
                                                <g:initializeAssignToElement assignedToId="assignedToActionAndWorkflow"
                                                                             isLabel="false" bean="${signal}"/>
                                            </span>
                                        </span>
                                    </span>
                                </h4>
                            </span>


                            <g:if test="${SystemConfig.first().displayDueIn}">
                                <span class="col-md-2">
                                    <h5><g:message code="app.label.signal.details.column.dueIn"/></h5>
                                    <h4><span id="dueInHeader">${dueIn == null ? '-' : dueIn + " Days"}</span>
                                    %{-- <g:if test="${dueIn}">--}%
                                        <g:if test="${isEditDueDate}">

                                            <div class="fuelux display-inline" id="dueInEdit">
                                                <div class="datepicker toolbarInline due-date dueDatePicker dueDateClass" ${dueIn == null ? 'style="display: none;"' : ""}
                                                     id="dueDatePicker">
                                                    <div class="input-group">
                                                        <input id="dueDate" placeholder="Select Date"
                                                               class="form-control dueDatePicker"
                                                               name="dueDate" type="text"/>
                                                        <g:render id="myDueDate"
                                                                  template="/includes/widgets/datePickerTemplate"/>
                                                    </div>
                                                </div>
                                                <i class="mdi mdi-pencil editButtonEvent" ${dueIn == null ? 'style="display: none;"' : ""}></i>
                                            </div>
                                        </g:if>
                                    %{-- </g:if>--}%
                                    </h4>
                                </span>
                            </g:if>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <g:render template="/includes/layout/flashErrorsDivs" bean="${signal}" var="theInstance"/>

    <div class="row m-t-10" id="divPaddingId">
        <div class="col-sm-12 ${!currentDispositionOptions ? 'nav-space' : 'top-nav-space'}">
            <!-- Nav tabs -->
            <ul id="detail-tabs" class="validation-tab m-b-5 p-0">
                <li role="presentation" class="active">
                    <a href="#update" aria-controls="update" role="tab" data-toggle="tab" accesskey="1" id="signalInfoTab">
                        <g:message code="app.label.signal.information" default="Signal Information"/>
                    </a>
                </li>

                <li role="presentation">
                    <a href="#details" id="detailsTab" aria-controls="details" role="tab" data-toggle="tab"
                       accesskey="2">
                        <g:message code="app.label.signal.management.review" default="Validated Observations"/>
                    </a>
                </li>

                <g:if test="${grailsApplication.config.signalManagement.linkedConfigurations.enabled}">
                    <li role="presentation">
                        <a href="#linkedConfigurations" id="linkedConfigurationsTab"
                           aria-controls="linkedConfigurations"
                           role="tab" data-toggle="tab" accesskey="3">
                            <g:message code="app.label.linked.configurations" default="Linked Configurations"/>
                        </a>
                    </li>
                </g:if>

                <li role="presentation">
                    <a href="#assessments" id="assessmentTab" aria-controls="assessments" role="tab" data-toggle="tab"
                       accesskey="4">
                        <g:message code="app.label.signal.management.assessments.references"
                                   default="Assessment and References"/>
                    </a>
                </li>
                <li role="presentation">
                    <a href="#notifications" id="actionAndWorkflow" aria-controls="notifications" role="tab"
                       data-toggle="tab" accesskey="5">
                        <g:message code="app.label.signal.management.actions.and.workflow"
                                   default="Actions And Workflow"/>
                    </a>
                </li>

                <li role="presentation">
                    <a href="#rmmCommunication" aria-controls="rmmCommunication" role="tab" data-toggle="tab"
                       id="signalRmms" accesskey="8">
                        <g:message code="signal.rmms.commitments.label"/>
                    </a>
                </li>

                <g:if test="${grailsApplication.config.signalManagement.signalReports.enabled}">

                    <li role="presentation">
                        <a href="#docManagement" id="documentManagement" aria-controls="docManagement" role="tab"
                           data-toggle="tab" accesskey="6">
                            <g:message code="app.label.signal.management.document.management"
                                       default="Signal Reports"/>
                        </a>
                    </li>
                </g:if>
            </ul>
            <!-- Tab panes -->
            <div class="tab-content pvs-validate-tabpan">

                <div id="update" class="tab-pane active m-b-10" role="tabpanel">
                    <g:render template="edit"
                              model="[validatedSignal   : signal, initialDataSource: initialDataSource.findAll { it != null }, signalTypeList: signalTypeList,
                                      haSignalStatusList: haSignalStatusList, timezone: timezone, signalOutcomes: signalOutcomes, genericComment: genericComment,]"/>
                </div>

                <div id="details" class="tab-pane fade m-b-10" role="tabpanel">
                    <g:render template="review" model="[labelConfigJson : labelConfigJson, hyperlinkConfiguration: hyperlinkConfiguration,labelConfigNew: labelConfigNew,labelConfig:labelConfig,labelConfigCopy:labelConfigCopy,labelConfigCopyJson:labelConfigCopyJson,labelConfigKeyId:labelConfigKeyId, strategy: signal,validatedAggHelpMap:validatedAggHelpMap,
                    validatedSingleHelpMap:validatedSingleHelpMap,validatedLiteratureHelpMap:validatedLiteratureHelpMap,columnLabelForSCA: columnLabelForSCA]"/>
                </div>

                <div id="linkedConfigurations" class="tab-pane fade m-b-10" role="tabpanel">
                    <g:render template="linkedConfigurations"/>
                </div>

                <div id="assessments" class="tab-pane fade m-b-10" role="tabpanel">
                    <g:render template="includes/assessment"
                              model="[strategy   : signal, chartCount: chartCount, specialPEList: specialPEList, signal: signal,
                                      heatMap    : heatMap, emergingIssues: emergingIssues, conceptsMap: conceptsMap, isSpotfireEnabled: isSpotfireEnabled,
                                      datasources: datasources, signalAssessmentDateRangeEnum: signalAssessmentDateRangeEnum]"/>
                </div>

                <div id="notifications" class="tab-pane fade m-b-10" role="tabpanel">
                    <g:render template="actionsAndWorkflow"
                              model="[signal: signal, actionConfigList: actionConfigList, timezone: timezone, userList: userList]"/>
                </div>

                <div id="rmmCommunication" class="tab-pane fade m-b-10" role="tabpanel">
                    <g:render template="/validatedSignal/includes/rmmCommunication"
                              model="[rmmType: rmmType, communicationType: communicationType, rmmStatus: rmmStatus]"/>
                </div>


                <div id="docManagement" class="tab-pane fade m-b-10" role="tabpanel">
                    <g:render template="includes/documentManagement" model="[signal: signal]"/>
                </div>

            </div>
        </div>
    </div>
    <input type="hidden" id="selectDatasource" value="${selectedDatasource}"/>
    <input type="hidden" id="signalName" value="${signalName}"/>
    <input type="hidden" id="signalIdPartner" value="${signal.id}"/>
    <input type="hidden" id="caseCountArgus" value="${caseCount}"/>
    <input type="hidden" id="pecCountArgus" value="${pecCountArgus}"/>
    <input type="hidden" id="pecCountEvdas" value="${evdasCount}"/>
    <input type="hidden" id="isEvdasEnabled" value="${grailsApplication.config.signal.evdas.enabled}"/>
    <input type="hidden" id="screenWidth" value="${1920}">
    <script>
        var eventObj;
        $(document).ready(function () {
         $("#editActionModal").find(".action-type-list .select2").select2();
         $("#editActionModal").find("#config").select2();
            $('#detail-tabs a').click(function (e) {
                e.preventDefault();
            });
            $(".dueDateClass").hide();

            $("a[data-toggle=\"tab\"]").on("shown.bs.tab", function (e) {
                $('#disassociationJustificationPopover').hide();
                switch (e.currentTarget.id) {
                    case 'assessmentTab':
                        $('#reference-table').DataTable().columns.adjust().draw();
                        $('#signal-analysis-table').DataTable().columns.adjust().draw();
                        break;
                    case 'linkedConfigurationsTab':
                        $('#linkedConfigurationTable').DataTable().columns.adjust();
                        break;
                    case 'actionAndWorkflow':
                        $('#action-table').DataTable().columns.adjust();
                        $('#meeting-table').DataTable().columns.adjust();
                        initializeActivityTable.ajax.reload();
                        $('#signalActivityTable').DataTable().columns.adjust();
                        colEllipsis();
                        webUiPopInit();
                        disableDueDate()
                        updateSignalHistory()
                        break;
                    case 'detailsTab':
                        $('#rxTableAggregateReview').DataTable().columns.adjust();
                        $('#rxTableSingleReview').DataTable().columns.adjust();
                        $('#rxTableLiteratureReview').DataTable().columns.adjust();
                        $('#rxTableAdHocReview').DataTable().columns.adjust();
                        break;
                    case 'signalRmms':
                        $('#signal-rmms-table').DataTable().columns.adjust().draw();
                        $('#signal-communication-table').DataTable().columns.adjust().draw();
                        break;
                }
            });

            var initializeActivityTable = signal.activities_utils.init_activities_table("#signalActivityTable", activityUrl, applicationName);


            var applicationName = "Signal Management";


            var activeTab = localStorage.getItem('activeTab');

            if (activeTab && lastVisitedUrl === localStorage.getItem('lastVisitedURL')) {
                $('a[href="' + activeTab + '"]').tab('show');
            }

            $(document).on('click', '#detail-tabs a', function (event) {
                var activeTab = $(event.target).attr('href');
                if (activeTab == '#notifications') {
                    setTimeout(function () {
                        _.each($('#notifications .dataTables_scrollHead'), function (elem) {
                            var elemWidth = $(elem).width();
                            $(elem).find('table.row-border.hover.dataTable.no-footer').width(elemWidth);
                        });
                    }, 500);
                }

                setTimeout(function () {
                    _.each($('#detail-tabs a'), function (ele) {

                        if ($(ele).parent().hasClass('active')) {
                            $(ele).css('background', "slategrey");
                            $(ele).parent().addClass('rx-main-tab')
                        } else {
                            $(ele).css('background', "darkgray");
                            $(ele).parent().removeClass('rx-main-tab')
                        }
                    })
                }, 10);
            });

            if ($('#productName')[0].scrollWidth > $('#productName').innerWidth()) {
                $('[data-toggle="productNameTooltip"]').tooltip();
            }

            if ($('#validatedSignalName')[0].scrollWidth > $('#validatedSignalName').innerWidth()) {
                $('[data-toggle="validatedSignalNameTooltip"]').tooltip();
            }

            if ($("#aggStartDate").val()) {
                var aggStartDate = new Date($("#aggStartDate").val());
                $('#aggStartDatePicker').datepicker({'setDate': aggStartDate, allowPastDates: true});
            } else {
                $('#aggStartDatePicker').datepicker({date: null, allowPastDates: true});
            }
            $('#aggStartDate').focusout(function () {
                $(this).val(newSetDefaultDisplayDateFormat( $(this).val()));
                if($(this).val()=='Invalid date'){
                    $(this).val('');
                }
            });
            if ($("#aggEndDate").val()) {
                var aggEndDate = new Date($("#aggEndDate").val());
                $('#aggEndDatePicker').datepicker({'setDate': aggEndDate, allowPastDates: true});
            } else {
                $('#aggEndDatePicker').datepicker({date: null, allowPastDates: true});
            }
            $('#aggEndDate').focusout(function () {
                $(this).val(newSetDefaultDisplayDateFormat( $(this).val()));
                if($(this).val()=='Invalid date'){
                    $(this).val('');
                }
            });
            if ($("#lastDecisionDate").val()) {
                var lastDecisionDate = new Date($("#lastDecisionDate").val());
                $('#lastDecisionDatePicker').datepicker({'setDate': lastDecisionDate, allowPastDates: true});
            } else {
                $('#lastDecisionDatePicker').datepicker({date: null, allowPastDates: true});
            }
            if ($("#haDateClosed").val()) {
                var haDateClosed = new Date($("#haDateClosed").val());
                $('#haDateClosedDatePicker').datepicker({'setDate': haDateClosed, allowPastDates: true});
            } else {
                $('#haDateClosedDatePicker').datepicker({date: null, allowPastDates: true});
            }
            $('#haDateClosed').focusout(function () {
                $(this).val(newSetDefaultDisplayDateFormat( $(this).val()));
                if($(this).val()=='Invalid date'){
                    $(this).val('');
                }
            });
            $("#screenWidth").val(screen.width)
            $('.dueDatePicker').datepicker(
                {
                    date: null,
                    allowPastDates: false,
                    momentConfig: {
                        culture: userLocale,
                        tz: userTimeZone,
                        format: DEFAULT_DATE_DISPLAY_FORMAT
                    },
                }).on('changed.fu.datepicker dateClicked.fu.datepicker', function (evt, date) {
                $('#disassociationJustificationPopover .popover-title').html(' Justification ');
                var elementLeft = $(this).parent().position().left
                var topValue = eventObj.pageY - 50;
                var totalx = $('#disassociationJustificationPopover').width()
                var leftValue = eventObj.pageX - totalx - 110;
                var arr = [];
                arr = $(".text-list li")
                for (var i = 0; i < arr.length; i++) {
                    var child = $(arr[i]).children()[0];
                    if ($(child).attr('id') == 'edit-boxDis') {
                        break;
                    } else {
                        $(arr[i]).hide();
                    }
                }
                $("#disassociationJustificationPopover").css({
                        'left': (Math.round($(this).offset().left) - $("#disassociationJustificationPopover").width()) + $(this).width(),
                        'top': Math.round($(this).offset().top) + 30 // field height
                    });

                $("#disassociationJustificationPopover").show();
                $(".confirm-options").attr("style", "bottom: 7px;");
                $("#addNewJustificationForDisassociation").trigger('click');
            });
            $(document).find('.selectlist-sizer').remove();
        });
        $(".editButtonEvent").click(
            function (e) {
                eventObj = e;
                $("#divPaddingId").attr("style", "padding-top: 20px")
                $("#dueDatePicker").show();

                $(".editButtonEvent").hide();
            }
        );
        var saveAssessmentNotes = function () {
            $.ajax({
                type: "POST",
                data: {'validatedSignal.id': $("#signalIdPartner").val(), 'comment': $("#assessmentNotes").val()},
                url: saveAssessmentNotesUrl,
                success: function (result) {
                    if (result.success) {
                        $("#commentsModal").modal("hide");
                    }
                }
            });
        };
        disableDueDate()
    </script>
    <g:render template="/includes/modals/partnerModal"/>
    <g:render template="/includes/modals/signalHistoryModal"/>
    <g:render template="/includes/widgets/show_evdas_charts_modal"/>
    <g:render template="/includes/modals/validated_signal_dissociation_modal"/>
    <g:render template="/includes/modals/addAssessmentNotesModal"/>
    <g:render template="/includes/modals/addAttachmentFileModal"/>
    <g:render template="/includes/modals/signal_revert_justification_modal" model="[id: validatedSignal.id]"/>
    <g:render template="/includes/modals/rmmAttachmentFileModal"/>
    <g:render template="/includes/modals/case_series_modal"/>
    <g:render template="/includes/popover/dispositionJustificationSelect"/>
    <g:render template="/includes/popover/undoableJustificationPopover" model="[id: validatedSignal.id]"/>
    <g:render template="/includes/popover/JustificationSelectionForDisassociation"
              model="[dispositionJustifications: dispositionJustifications]"/>
    <g:render template="/includes/popover/priorityJustificationSelect"
              model="[availableAlertPriorityJustifications: availableAlertPriorityJustifications]"/>
    <g:render template="/includes/popover/prioritySelect"
              model="[availablePriorities: availablePriorities, forceJustification: forceJustification]"/>

    <asset:javascript src="app/pvs/validated_signal/prev_assessment.js"/>
    <g:render template="/includes/modals/evdas_case_drill_down"/>
    <g:if test="${Holders.config.pv.plugin.dictionary.enabled}">
        <g:render template="/configuration/copyPasteModal"/>
        <input type="hidden" id="editable" value="true">
        <g:render template="/plugin/dictionary/dictionaryModals" plugin="pv-dictionary"
                  model="[filtersMapList: Holders.config.product.dictionary.filtersMapList, viewsMapList: Holders.config.product.dictionary.viewsMapList, isPVCM:isPVCM,multiIngredientValue: signal?.isMultiIngredient]"/>
        <g:render template="/plugin/dictionary/dictWarningTemplate"/>
    </g:if>
    <g:else>
        <g:render template="/includes/modals/event_selection_modal" model="[sMQList: sMQList]"/>
        <g:render template="/includes/modals/product_selection_modal"/>
    </g:else>

    <g:render template="/validatedSignal/includes/extendedTextarea"/>

</div>

<div id="hiddenform"></div>
</body>
