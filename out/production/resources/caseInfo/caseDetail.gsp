<%@ page import="com.rxlogix.Constants; grails.util.Holders; grails.converters.JSON; com.rxlogix.enums.ReportFormat; com.rxlogix.Constants" contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.caseInfo.caseDetail.title"/></title>
    <asset:javascript src="yadcf/jquery.dataTables.yadcf.js"/>
    <asset:javascript src="vendorUi/datatables/datatable.colsReorder.min.js"/>
    <asset:stylesheet src="yadcf/jquery.dataTables.yadcf.css"/>
    <asset:stylesheet src="app/pvs/updatedFixedColumn.css"/>
    <asset:stylesheet src="colReorder.dataTables.min.css"/>
    <asset:javascript src="app/pvs/bootbox.min.js"/>

    <asset:javascript src="app/pvs/alerts_review/case_diff.js"/>
    <asset:javascript src="app/pvs/alerts_review/case_detail.js"/>
    <asset:stylesheet src="text_diff.css"/>
    <asset:javascript src="app/pvs/caseForm.js"/>
    <asset:stylesheet src="jsTree/style.min.css"/>
    <asset:javascript src="vendorUi/jsTree/jstree.js"/>
    <asset:javascript src="app/pvs/alert_utils/common_key_prevent.js"/>

    <asset:javascript src="highcharts.js"/>
    <asset:javascript src="vendorUi/highcharts/highcharts-more.js"/>
    <asset:javascript src="vendorUi/highcharts/highcharts-3d.js"/>
    <asset:javascript src="vendorUi/highcharts/themes/grid-rx.js"/>
    <asset:javascript src="app/bootstrap-modal-popover/bootstrap-modal-popover.js"/>
    <asset:javascript src="app/pvs/disposition/disposition-change.js"/>
    <asset:javascript src="app/pvs/priority/priority-change.js"/>
    <asset:javascript src="app/pvs/caseHistory/caseHistoryJustification.js"/>

    <g:javascript>
        var type = null;
        <g:if test="${!isChildCase && caseDetail}">
            var getAttachmentUrl = "${createLink(controller: 'singleCaseAlert', action: 'fetchAttachment', params: [alertId: caseDetail.alertId,isArchived: isArchived])}";
            var alertId = "${caseDetail.alertId}";
            var productName = "${caseDetail.productName}";
            var option = new Option("${caseDetail.assignedTo.fullName}", "${caseDetail.assignedTo.id}", true, true);
            var deleteAttachmentUrl = "${createLink(controller: 'singleCaseAlert', action: 'deleteAttachment', params: [alertId: caseDetail.alertId])}";
            var fetchOutcomeUrl = "${createLink(controller: 'caseInfo', action: 'fetchOutcomeForVAERS')}";
    </g:if>
        <g:else>
            var getAttachmentUrl = null;
            var option = null;
            var alertId = null;
        </g:else>
        var isFaers = ${caseDetail ? "${isFaers}" : false};
        var isVaers = "${caseDetail ? "${isVaers}" : false}";
        var isVigibase = "${caseDetail ? "${isVigibase}" : false}";
        var isJader = "${caseDetail ? "${isJader}" : false}";
        var isArchived = "${isArchived}";
        var hasReviewerAccess = ${hasSingleReviewerAccess};
        var hasSignalCreationAccessAccess = ${hasSignalCreationAccessAccess};
        var hasSignalViewAccessAccess = ${hasSignalViewAccessAccess};
        var getUploadUrl = "${createLink(controller: 'singleCaseAlert', action: 'upload', params: [isArchived: isArchived])}";
        var getPriorityUrl = "${createLink(controller: "workflow", action: 'getPriority')}";
        var relatedCaseSeriesUrl = "${createLink(controller: "singleCaseAlert", action: 'fetchRelatedCaseSeries', params: [caseNumber: caseNumber])}";
        var detailsUrl = "${createLink(controller: 'singleCaseAlert', action: 'details', params: [callingScreen: 'review'])}";
        var caseDetailAssessmentUrl = "${createLink(controller: 'caseInfo', action: 'fetchAssessmentDetailData')}";
        var caseHistoryUrl = "${createLink(controller: "caseHistory", action: 'listCaseHistory')}";
        var caseHistorySuspectUrl = "${createLink(controller: "caseHistory", action: 'listSuspectProdCaseHistory')}";
        var treeNodesUrl = "${createLink(controller: "caseInfo", action: 'fetchTreeViewNodes', params: ['alertType': com.rxlogix.Constants.AlertConfigType.SINGLE_CASE_ALERT, 'alertId': alertId, 'isFaers': isFaers, isVaers: isVaers, isVigibase: isVigibase,isJader:isJader,
                                'isAdhocRun':isAdhocRun, 'isStandalone': isStandalone])}";
        var sectionNameList = JSON.parse('${sectionNameList}');
        var dispositionIncomingOutgoingMap = JSON.parse('${dispositionIncomingOutgoingMap}');
        var forceJustification = ${forceJustification};
        var singleCaseDetailsUrl = "${createLink(controller: 'singleCaseAlert', action: 'details')}";
        var signalDetailUrl = "${createLink(controller: 'validatedSignal', action: 'details')}";
        var updateJustificationUrl = "${createLink(controller: "caseHistory", action: 'updateJustification')}";
        var authUrl = "${createLink(controller: 'user', action: 'eAuthenticate')}";
        var availableSignalNameList = JSON.parse('${availableSignals.collect { it.name } as JSON}');
        var caseDetailUrl = "${createLink(controller: "caseInfo", action: 'caseDetail')}";
        var updateAutoRouteDispositionUrl = "${createLink(controller: 'singleCaseAlert', action: 'updateAutoRouteDisposition')}";
        var followUpName = "${Holders.config.caseDetail.followUp.name}";
        var changeAssignedToUrl = "${createLink(controller: 'singleCaseAlert', action: 'changeAssignedToGroup')}";
        var changePriorityUrl = "${createLink(controller: 'singleCaseAlert', action: 'changePriorityOfAlert')}";
        var changeDispositionUrl = "${createLink(controller: 'singleCaseAlert', action: 'changeDisposition')}";
         var fetchCommonTagsUrl = "${createLink(controller: 'commonTag', action: 'commonTagDetails')}";
          var fillCommonTagUrl="${createLink(controller: 'commonTag', action: 'fetchCategoriesByVersion')}";
          var privateEnabled = "${grailsApplication.config.categories.feature.private.enabled ? true : false}";
         var nonConfiguredEnabled = "${grailsApplication.config.categories.feature.nonConfigured.enabled ? true : false}";
         var saveCommonTagsUrl = "${createLink(controller: 'commonTag', action: 'saveAlertCategories')}";
         var getCatByVersionUrl="${createLink(controller: 'commonTag', action: 'fetchCategoriesMapByCase')}";
         var saveCategoryAccess = "${saveCategoryAccess}";
          var isPriorityEnabled = "${isPriorityEnabled}";
          var isChildCase = ${isChildCase}
    </g:javascript>
    <style>
        .modal-lg {
            width: 1250px; /* New width for large modal */
        }
         #createActionModal .actionClass, #action-edit-modal .actionClass{
                    width:900px;  /* Added for Action modal */
        }

        .odd {
            background-color: #EFEFEF;
            padding: 0;
        }
        #categories .mdi-pencil{
            display: none;
        }
        #categories:hover .mdi-pencil{
            display : block;
        }


        .tag-container {
            margin-right: 0px;
            max-width: 90%;
            /* min-width: 180px; */
        }
        .col-max-300 {
            max-width: 100%;
        }
    </style>
    <g:render template="/includes/widgets/actions/action_types"/>
</head>

<body>
<g:render template="/includes/modals/common_tag_modal"/>
<div id="top-panel" class="container">
    <div class="row">
        <div id="detail-tabs" class="col-md-12 panel m-b-0">
            <div role="presentation" class="${(!isFaers && !isVaers && !isVigibase && !isJader)?"col-sm-5":"col-sm-12"}">
                <h4 aria-controls="details" style="float: left; margin-right: 20px;" role="tab">${isFaers ? "FAERS" : isVigibase ? "VigiBase" : isVaers ? "VAERS" : isJader ? "JADER" : "Safety DB"} - Case Number: ${caseNumber}
                <g:if test="${!isFaers && !isVaers && !isVigibase && !isJader}">
                   - <span id="followup"></span>${followUpSelectionValue}
                </g:if>
                    <input type="hidden" id="fullCaseList" value="${fullCaseListData?.fullCaseList}">
                    <input type="hidden" id="detailsParameters" value="${fullCaseListData?.detailsParameters}">
                    <input type="hidden" id="totalCount" value="${fullCaseListData?.totalCount}">
                    <input type="hidden" id="version" value="${fullCaseListData?.version}">
                    <input type="hidden" id="execConfigId" value="${fullCaseListData?.execConfigId}">
                    <input type="hidden" id="isSingleAlertScreen" value="${isSingleAlertScreen}">
                    <input type="hidden" id="currFollowUp" value="${followUp}">
                    <input type="hidden" id="isCaseSeries" value="${isCaseSeries}">
                    <g:if test="${fullCaseListData && fullCaseListData.previous}">
                        <a type="button" class="btn btn-default m-l-5 caseNumberRef"
                           data-caseNumber='${fullCaseListData.previous.caseNumber}'
                           data-caseVersion='${fullCaseListData.previous.caseVersion}'
                           data-followUpNumber='${fullCaseListData.previous.followUpNumber}'
                           data-alertId='${fullCaseListData.previous.alertId}'><g:message code="default.paginate.prev"/></a>
                    </g:if>
                    <g:if test="${fullCaseListData && fullCaseListData.next}">
                        <a type="button" class="btn btn-default caseNumberRef"
                           data-caseNumber='${fullCaseListData.next.caseNumber}'
                           data-caseVersion='${fullCaseListData.next.caseVersion}'
                           data-followUpNumber='${fullCaseListData.next.followUpNumber}'
                           data-alertId='${fullCaseListData.next.alertId}'><g:message code="default.paginate.next"/></a>
                    </g:if>
                </h4>
            </div>

            <g:if test="${!isFaers && !isVaers && !isVigibase && !isJader}">
                <div class="col-sm-7 m-t-8 ico-menu">
                    <div class="text-right form-inline">
                        <div class="form-group">

                            <select name="followUpNumber" id="followUpNumber" class="form-control m-r-10 col-max-17">
                                <option value>-Choose-</option>
                                <g:each in="${versionsList}" status="i" var="version">
                                    <option value="${version.followUpNum}" data-version="${version.versionNum}">${version.data}</option>
                                </g:each>
                            </select>

                        </div>
                        <span class="pos-rel m-r-10">
                            <span class="dropdown-toggle exportPanel" data-toggle="dropdown" title="Export to" tabindex="0">
                                <i class="mdi mdi-export font-24 blue-1 lh-1 valign-m" ></i>
                                <span class="caret hidden"></span></button>

                            </span>
                            <ul class="dropdown-menu export-type-list" id="exportTypesTopic">
                                <strong class="font-12"><g:message code="app.label.export"/></strong>
                                <li><g:link controller="caseInfo" action="exportCaseInfo" class="m-r-30"
                                            params="${[outputFormat: com.rxlogix.enums.ReportFormat.DOCX, isVersion:isVersion,
                                                       caseNumber: caseNumber, followUpNumber: followUpNumber, version: caseDetail.caseVersion?:version,
                                                       alertConfigId: "${caseDetail?.configId}", productFamily: "${caseDetail?.productFamily}",
                                                       alertId: alertId,isAdhocRun: isAdhocRun, isDuplicate: type, isArchived: isArchived,
                                                       exeConfigId: "${caseDetail?.execConfigId}", isVersion: isVersion, isChildCase: isChildCase, isStandalone: isStandalone ]}">
                                    <img src="/signal/assets/word-icon.png" class="m-r-10" height="16"
                                         width="16"/><g:message code="save.as.word"/>
                                </g:link>
                                </li>
                                <li><g:link controller="caseInfo" action="exportCaseInfo" class="m-r-30"
                                            params="${[outputFormat: ReportFormat.XLSX, isVersion:isVersion, caseNumber: caseNumber,
                                                       followUpNumber: followUpNumber, version: caseDetail.caseVersion?:version,
                                                       alertConfigId: "${caseDetail?.configId}", productFamily: "${caseDetail?.productFamily}",
                                                       alertId: alertId,isAdhocRun: isAdhocRun, isDuplicate: type, isArchived: isArchived,
                                                       exeConfigId: "${caseDetail?.execConfigId}", isVersion: isVersion,isChildCase: isChildCase, isStandalone: isStandalone]}">
                                    <img src="/signal/assets/excel.gif" class="m-r-10" height="16"
                                         width="16"/><g:message
                                            code="save.as.excel"/>
                                </g:link></li>
                                <li><g:link controller="caseInfo" action="exportCaseInfo" class="m-r-30"
                                            params="${[outputFormat: ReportFormat.PDF, isVersion:isVersion, caseNumber: caseNumber,
                                                       followUpNumber: followUpNumber, version: caseDetail.caseVersion?:version,
                                                       alertConfigId: "${caseDetail?.configId}", productFamily: "${caseDetail?.productFamily}",
                                                       alertId: alertId,isAdhocRun: isAdhocRun, isDuplicate: type, isArchived: isArchived,
                                                       exeConfigId: "${caseDetail?.execConfigId}", isVersion: isVersion, isChildCase: isChildCase, isStandalone: isStandalone]}">
                                    <img src="/signal/assets/pdf-icon.jpg" class="m-r-10" height="16"
                                         width="16"/><g:message
                                            code="save.as.pdf"/>
                                </g:link></li>
                            </ul>
                        </span>
                        <span class="menuButton m-r-10" tabindex="0">
                            <i class="mdi mdi-format-list-bulleted mr-10 font-24 lh-1  grid-menu-tooltip valign-m" id="relatedCaseSeries"
                               data-title="Linked Case Series"></i>
                        </span>
                        <g:if test="${availableVersions?.size() > 1}">
                            <span class="menuButton m-r-10" tabindex="0">
                                <i class="mdi mdi-compare mr-10 font-24 lh-1  grid-menu-tooltip valign-m "
                                   id="version-list"
                                   data-title="Compare Versions"></i>

                                <div class="tooltip fade top in hide tooltip-versions m-t-40" id="version-list-tooltip">
                                    <div class="tooltip-inner row font-15">
                                        <div class="col-xs-6 col-md-6 col-lg-6 t-a-left"><div class="tooltip-heading">Available Versions</div>
                                            <g:each in="${availableVersions}" status="i" var="version">
                                                <div class="version-available m-t-5" data-id="${version.versionNum}"
                                                     data-followUp="${version.followUpNum}">
                                                    ${version.data} (${version.receiptDate})
                                                </div>
                                            </g:each>
                                        </div>

                                        <div class="col-xs-6 col-md-6 col-lg-6 t-a-left"><div class="tooltip-heading">Versions to Compare</div>
                                            <g:each in="${availableVersions}" status="i" var="version">

                                                <div class="version-compare m-t-5" data-id="${version.versionNum}"
                                                     data-followUp="${version.followUpNum}">
                                                    <span>${version.data} (${version.receiptDate})</span>
                                                    <g:if test="${!isChildCase && version.lastVersion}">
                                                        <span class="green-2">*</span>
                                                    </g:if>
                                                </div>
                                            </g:each>
                                        </div>
                                        <g:if test="${!isChildCase && isLastVersionPresent}">
                                            <div class="m-t-5 green-2 t-a-left">
                                                * Last reviewed version from the previous period
                                            </div>
                                        </g:if>
                                    </div>
                                </div>
                            </span>
                        </g:if>
                        <g:if test="${availableVersions?.size() > 1 && followUpNumber!='0'}">
                            <g:if test="${isLastVersionPresent}">
                                <input type="hidden" id="previous-version" value="${previousVersion}"/>
                                <input type="hidden" id="previous-followUp" value="${previousFollowUp}"/>
                            </g:if>
                            <span class="menuButton m-r-10">
                                <a class="btn btn-primary compare-versions"><g:message
                                        code="app.compare.versions"/></a>
                            </span>
                        </g:if>
                        <g:if test="${showGenerateCioms}">
                            <span class="menuButton ${buttonClass}">
                                <a href='${ciomsReportUrl}' target="_blank"
                                   class="btn btn-primary generate-cioms"><g:message code="caseDetails.generate.cioms"/></a>
                            </span>
                        </g:if>
                    </div>
                </div>
            </g:if>
        </div>
    </div>

</div>

<div class="info-widget">

    <div class="pv-fullcase-treeview">
        <div id="caseDetailTree" class="demo"></div>
    </div>

    <div class="treeview-content">
        <g:if test="${caseDetail && !isFaers && !isVaers && !isVigibase && !isJader && !isChildCase}">
            <rx:containerCollapsible class="section m-b-10" title="Workflow and Categories Management">
                <div class="row">
                    <div>
                        <span class="box-100">
                            <input data-field="productFamily" data-id="${caseDetail.productFamily}" type="hidden"
                                   value="${caseDetail.productFamily}"/>
                            <input data-field="primaryEvent" data-id="${caseDetail.primaryEvent}" type="hidden"
                                   value="${caseDetail.primaryEvent}"/>
                            <input class="alert-check-box" data-id="${caseDetail.alertId}" type="hidden"
                                   value="${caseDetail.alertId}"/>
                            <input class="statsId" data-id="${caseDetail.statsId}" type="hidden"
                                   value="${caseDetail.statsId}"/>
                            <span data-field="productName" data-id="${caseDetail.productName}"></span>
                            <span data-field='caseNumber' data-id="${caseDetail.caseNumber}"></span>
                            <span data-field='caseVersion' data-id="${caseDetail.caseVersion}"></span>
                            <span data-field='followUpNumber' data-id="${caseDetail.followUpNumber}"></span>
                            <span data-field="masterPrefTermAll" data-id="${caseDetail.pt}"></span>
                            <span data-field='assignedTo' data-info='row'
                                  data-current-user-id="${caseDetail.assignedTo.id}"
                                  data-current-user="${caseDetail.assignedTo.fullName}"></span>
                        </span>
                    </div>
                    <g:if test="${isPriorityEnabled}">
                        <div class="col-md-1 priority">
                            <label class="box-100"><g:message
                                    code="app.label.evdas.details.column.priorityAll"/></label>
                            <a class="font-24" title="${caseDetail.priority.value}"><i
                                    class="${caseDetail.priority.iconClass}"></i></a>
                        </div>
                    </g:if>

                    <div class="col-md-2 currentDisposition">
                        <label class="box-100">Current Disposition</label>
                        <h5 id = "caseDisposition">${caseDetail.disposition}</h5>
                    </div>

                    <div class="col-md-3">
                        <label>Disposition</label>
                        <span class="disposition">
                            <ul class="list-inline icon-list"
                                data-current-disposition="${caseDetail.disposition}">
                                <g:if test="${caseDetail.isValidationStateAchieved}">
                                    <li>
                                        <a data-target="${!caseDetail.isValidationStateAchieved?(hasSingleReviewerAccess?'#dispositionSignalPopover':''):
                                                (hasSignalCreationAccessAccess && hasSingleReviewerAccess?'#dispositionSignalPopover':'')}" role="button"
                                           class="changeDisposition"
                                           data-validated-confirmed="${caseDetail.isValidationStateAchieved}"
                                           data-disposition-id="${caseDetail.dispositionId}" data-toggle="modal-popover"
                                           data-placement="bottom"
                                           title="${caseDetail.disposition}"><span>Add Signal</span>
                                        </a>
                                    </li>
                                </g:if>
                                <g:else>
                                    <g:each in="${currentDispositionOptions}">
                                        <g:if test="${it.validatedConfirmed}">
                                            <li>
                                                <a data-target="${!it.validatedConfirmed?(hasSingleReviewerAccess?'#dispositionSignalPopover':''):
                                                        (hasSignalCreationAccessAccess && hasSingleReviewerAccess?'#dispositionSignalPopover':'')}" role="button"
                                                   class="changeDisposition"
                                                   data-validated-confirmed="${it.validatedConfirmed}"
                                                   data-auth-required="${it.isApprovalRequired}"
                                                   data-disposition-id="${it.id}" data-toggle="modal-popover"
                                                   data-placement="bottom" title="${it.displayName}">
                                                    <i class="ico-circle"
                                                       style="background:${it.colorCode}">${it.abbreviation}</i>
                                                </a>
                                            </li>
                                        </g:if>
                                        <g:else>
                                            <g:if test="${forceJustification}">
                                                <li>
                                                    <a data-target="${!it.validatedConfirmed?(hasSingleReviewerAccess?'#dispositionJustificationPopover':''):
                                                            (hasSignalCreationAccessAccess && hasSingleReviewerAccess?'#dispositionJustificationPopover':'')}" role="button"
                                                       class="changeDisposition"
                                                       data-validated-confirmed="${it.validatedConfirmed}"
                                                       data-auth-required="${it.isApprovalRequired}"
                                                       data-disposition-id="${it.id}"
                                                       data-toggle="modal-popover"
                                                       data-placement="bottom"
                                                       title="${it.displayName}">
                                                        <i class="ico-circle"
                                                           style="background:${it.colorCode}">${it.abbreviation}</i>
                                                    </a>
                                                </li>
                                            </g:if>
                                            <g:else>
                                                <li>
                                                    <a data-target="#" role="button" class="changeDisposition"
                                                       data-validated-confirmed="${it.validatedConfirmed}"
                                                       data-auth-required="${it.isApprovalRequired}"
                                                       data-disposition-id="${it.id}"
                                                       title="${it.displayName}">
                                                        <i class="ico-circle"
                                                           style="background:${it.colorCode}">${it.abbreviation}</i>
                                                    </a>
                                                </li>
                                            </g:else>

                                        </g:else>
                                    </g:each>
                                </g:else>
                            </ul>
                        </span>
                    </div>
                    <div class="col-md-3" id="categories" style="padding-right: 20px!important;">

                        <label style="margin-left: 8px;!important">Category</label>
                        <input type="hidden" class="catAlertId"  value="${alertId}" id="catAlertId">
                        <input type="hidden" id="isFoundAlertArchived" value="${isFoundAlertArchived}"/>
                        <input type="hidden" id="foundAlertId" value="${foundAlertId}"/>
                        <input type="hidden" id="isCategoryEditable" value="${isCategoryEditable}"/>
                        <input type="hidden" id="categoryList" value="${categoriesList}"/>
                        <i class="isCategoriesProcessing mdi mdi-spin mdi-loading" style="display: none;"></i>


                    </div>

                    <g:render template="/includes/widgets/assigned_to_Select"/>
                </div>
            </rx:containerCollapsible>
        </g:if>
        <g:if test="${(!caseDetail && !isFaers && !isVaers && !isVigibase && !isJader) || isChildCase}">
            <rx:containerCollapsible class="section m-b-10" title="Workflow and Categories Management">
                <div class="col-md-3" id="categories">

                    <label style="margin-left: 8px;!important">Category</label>
                    <input type="hidden" class="catAlertId"  value="${alertId}" id="catAlertId">
                    <input type="hidden" id="isFoundAlertArchived" value="${isFoundAlertArchived}"/>
                    <input type="hidden" id="adHocVersionNum" value="${versionNum}"/>
                    <input type="hidden" id="foundAlertId" value="${foundAlertId}"/>
                    <input type="hidden" id="isCategoryEditable" value="${isCategoryEditable}"/>
                    <input type="hidden" id="categoryList" value="${categoriesList}"/>
                    <i class="isCategoriesProcessing mdi mdi-spin mdi-loading" style="display: none;"></i>

                </div>
            </rx:containerCollapsible>
        </g:if>
        <rx:containerCollapsible title="Case Detail">
            <g:if test="${data}" >
                <g:each in="${data}" var="k, v" status="i">
                    <g:if test="${v?.containsValues?.any { it }}">
                        <g:if test="${(isVaers && k!="Narrative") || (isVigibase && k!="Narrative") || (!isVaers && !isVigibase && sectionRelatedInfo?."${k}".isFullText != true)}">
                            <rx:containerCollapsible title="${k}">
                                <div class="scroll-body">
                                    <g:if test="${!isVaers && !isVigibase && sectionRelatedInfo?."${k}".sectionKey == Constants.CaseDetailUniqueName.DEVICE_INFORMATION}">
                                        <table class="table table-condensed no-border m-t-0 m-b-0 text-left">
                                            <thead>
                                            <tr>
                                                <g:each in="${v[0]}" status="column" var="paramKey, paramValue">
                                                    <g:if test="${!(paramKey in [Constants.CaseDetailFields.CONTAINS_VALUES, Constants.CaseDetailFields.CHECK_SEQ_NUM, Constants.CaseDetailFields.CHECK_SEQ_NUM_TWO])}">
                                                        <g:if test="${column % 2 == 0}">
                                                            <th class="col-md-1-half">
                                                            <div class="stacked-cell-left-top">
                                                            ${paramKey}
                                                            </div>
                                                        </g:if>
                                                        <g:else>
                                                            <div class="stacked-cell-left-bottom">
                                                                ${paramKey}
                                                            </div>
                                                        </th>

                                                    </g:else>
                                                </g:if>
                                            </g:each>
                                        </tr>
                                        </thead>
                                        <tbody style="border-top: 1px #ccc !important">
                                        <g:each in="${v}" var="ki" status="column">
                                            <g:if test="${column % 2 == 0}">
                                                <tr class="odd">
                                            </g:if>
                                            <g:else>
                                                <tr class="even">
                                            </g:else>
                                                <g:each in="${v[column]}" var="paramKey, paramValue" status="columnValue">
                                                    <g:if test="${v[column][Constants.CaseDetailFields.CONTAINS_VALUES] && !(paramKey in [Constants.CaseDetailFields.CONTAINS_VALUES, Constants.CaseDetailFields.CHECK_SEQ_NUM, Constants.CaseDetailFields.CHECK_SEQ_NUM_TWO])}">
                                                        <g:if test="${columnValue % 2 == 0}">
                                                            <td class="col-md-1-half">

                                                                <div class="stacked-cell-left-top">
                                                            ${paramValue ?: '-'}
                                                            </div>

                                                        </g:if>
                                                        <g:else>
                                                            <div class="stacked-cell-left-top">
                                                                    ${paramValue ?: '-'}
                                                                </div>
                                                        </td>
                                                        </g:else>
                                                    </g:if>
                                                </g:each>
                                            </tr>
                                        </g:each>
                                        </tbody>
                                    </table>
                                </g:if>
                                <g:else>
                                <table class="table no-border m-t-0 m-b-0">
                                    <thead>
                                    <g:each in="${v[0]}" var="paramKey, paramValue">
                                        <g:if test="${!(paramKey in [Constants.CaseDetailFields.CONTAINS_VALUES, Constants.CaseDetailFields.CHECK_SEQ_NUM, Constants.CaseDetailFields.CHECK_SEQ_NUM_TWO])}">
                                            <th>${paramKey}</th>
                                        </g:if>
                                    </g:each>
                                    </thead>
                                    <tbody>
                                    <g:each in="${v}" var="ki" status="column">
                                        <g:if test="${column % 2 == 0}">
                                            <tr class="odd">
                                        </g:if>
                                        <g:else>
                                            <tr class="even">
                                        </g:else>
                                        <g:each in="${v[column]}" var="paramKey, paramValue" status="columnValue">
                                            <g:if test="${v[column][Constants.CaseDetailFields.CONTAINS_VALUES] && !(paramKey in [Constants.CaseDetailFields.CONTAINS_VALUES, Constants.CaseDetailFields.CHECK_SEQ_NUM, Constants.CaseDetailFields.CHECK_SEQ_NUM_TWO])}">
                                                <g:if test="${(isVaers || isVigibase) && paramKey == 'Outcome'}">
                                                    <td class="outcome-value">${paramValue ?: '-'}</td>
                                                </g:if>
                                                <g:else>
                                                <g:if test="${paramValue == grails.util.Holders.config.caseDetail.reference.type}">
                                                    <td>${paramValue ?: '-'}</td>
                                                    <g:set var="referenceNumberIsUrl" value="${true}"></g:set>
                                                </g:if>
                                                <g:else>
                                                <g:if test="${paramKey == "Age with units"}">
                                                    <td>${paramValue ? paramValue + " Years" : '-'}</td>
                                                </g:if>
                                                <g:else>
                                                    <g:if test="${paramKey == Constants.CaseDetailUniqueName.REFERENCE_NUMBER && referenceNumberIsUrl}">
                                                        <td class="referenceNumber">${paramValue ?: '-'}</td>
                                                        <g:set var="referenceNumberIsUrl" value="${false}"></g:set>
                                                    </g:if>
                                                    <g:elseif test="${!isVaers && !isVigibase && fieldRelatedInfo?."${k}"?."${paramKey}" == Constants.CaseDetailUniqueName.LINKED_CHILD_CASE}">
                                                        <g:if test = "${paramValue}">
                                                            <td><a href="${createLink(controller: 'caseInfo', action: 'caseDetail', params: [caseNumber: paramValue ?: '-', isChildCase: true, isFaers: isFaers])}"
                                                                   class="linked-child-case" action="caseDetail"
                                                                   target="_blank"
                                                                   data-casenumber="${paramValue}">${paramValue ?: '-'}</a>
                                                            </td>
                                                        </g:if>
                                                        <g:else>
                                                            <td>${paramValue ?: '-'}</td>
                                                        </g:else>
                                                    </g:elseif>
                                                    <g:else>
                                                        <g:if test="${columnValue % 2 != 0 && v[column][Constants.CaseDetailUniqueName.REFERENCE_TYPE] == Constants.CaseDetailUniqueName.DUPLICATE_CASES}">
                                                            <td class="duplicateCases">${paramValue ?: '-'}</td>
                                                        </g:if>
                                                        <g:else>
                                                            <td style="max-width: 250px;word-wrap: break-word !important; "><pre style="background-color: inherit;font-size: 14px!important;border: 0;padding: 0px;margin: 0px;font-family: 'Open Sans', sans-serif !important;word-break: normal;">${paramValue ?: '-'}</pre></td>
                                                        </g:else>
                                                    </g:else>
                                                </g:else>
                                                </g:else>
                                            </g:else>
                                            </g:if>
                                        </g:each>
                                        </tr>
                                    </g:each>
                                    </tbody>
                                </table>
                                </g:else>
                            </div>
                        </rx:containerCollapsible>
                        </g:if>
                        <g:else>
                            <div class="rxmain-container">
                                <div class="rxmain-container-inner">
                                    <a>
                                        <div class="rxmain-container-header ico-menu">
                                            <div class="dropdown col-md-9 col-xs-9" data-toggle="collapse" data-target="#${k?.toLowerCase()?.replaceAll("\\s", "_").replace("(","").replace(")","")}_container">
                                                <label class="rxmain-container-header-label">${k}</label>
                                            </div>
                                            <div class="dropdown col-md-3 col-xs-3">
                                                <div class="container">
                                                    <div class="col-md-2">

                                                    </div>
                                                    <div class="col-md-3 zoom-tab to-center bd-right">
                                                        <label class="m-t-3">Zoom</label>
                                                    </div>
                                                    <div class="col-md-5 zoom-tab to-center bd-left">
                                                        <label class="fl-left font-16 zoom-in-element">-</label>
                                                        <label class="m-t-3 zoom-viewer">100</label>
                                                        <label>%</label>
                                                        <label class="fl-right font-16 zoom-out-element">+</label>
                                                    </div>
                                                    <div class="col-md-1 m-l-15 zoom-tab" style="padding:0!important;">
                                                        <label class="font-20 refresh-zoom"><i class="mdi mdi-refresh"></i></label>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </a>
                                    <div id="${k?.toLowerCase()?.replaceAll("\\s", "_").replace("(","").replace(")","")}_container"
                                         class="row rxmain-container-content panel-collapse collapse free-text-container">
                                        <g:each in="${v[0]}" var="paramKey, paramValue"   status = "column">
                                            <g:if test="${!(paramKey in [Constants.CaseDetailFields.CONTAINS_VALUES, Constants.CaseDetailFields.CHECK_SEQ_NUM, Constants.CaseDetailFields.CHECK_SEQ_NUM_TWO])}">
                                                <g:if test="${column % 2 == 0}">
                                                    <div class="row odd">
                                                </g:if>
                                                <g:else>
                                                    <div class="row even">
                                                </g:else>
                                                    <div class="col-xs-2 break-word-hyphen">
                                                        <label>${paramKey}</label>
                                                    </div>

                                                    <div class="col-xs-10">
                                                        <pre style="background-color: inherit;font-size: 14px!important;border: 0;padding: inherit;font-family: 'Open Sans', sans-serif !important;word-break: normal;">${paramValue ?: '-'}</pre>
                                                    </div>
                                                </div>
                                            </g:if>
                                        </g:each>
                                    </div>
                                </div>
                            </div>
                        </g:else>
                    </g:if>
                </g:each>
            </g:if>
            <g:else>
                <div class="m-b-10">
                    <span><g:message code="app.case.data.not.found"/></span>
                </div>
            </g:else>
        </rx:containerCollapsible>
        <g:if test="${isChildCase && caseDetail && !isFaers && !isVaers && !isVigibase && !isJader}">
            <input type="hidden" id="caseVersion" value="${caseDetail.caseVersion}"/>
        </g:if>
        <g:if test = "${isAdhocRun}">
            <input type="hidden" id="caseVersionAdhoc" value="${caseVersionAdhoc}"/>
        </g:if>
        <g:if test = "${isStandalone}">
                    <input type="hidden" id="caseVersionIsStandalone" value="${caseVersionIsStandalone}"/>
        </g:if>
        <g:if test="${!isChildCase && caseDetail && !isFaers && !isVaers && !isVigibase && !isJader}">
            <rx:containerCollapsible class="section m-b-10" title="Attachments">
                <div class="col-sm-12">
                    <a class="btn btn-primary ${buttonClass}" href="#attachmentCaseDetailModal" data-toggle="modal">Add File</a>

                    <div class="attachments">
                        <table id="attachment-table" class="row-border hover  no-footer" width="100%">
                            <thead>
                            <tr>
                                <th><g:message code="caseDetails.link"/></th>
                                <th><g:message code="caseDetails.description"/></th>
                                <th><g:message code="caseDetails.timestamp"/></th>
                                <th><g:message code="caseDetails.modified.by"/></th>
                                <th class="sorting_disabled"><g:message code="caseDetails.action"/></th>
                            </tr>
                            </thead>
                            <tbody>

                            </tbody>
                        </table>
                    </div>
                </div>
            </rx:containerCollapsible>
            <rx:containerCollapsible class="section m-b-10" title="Comments">

                <div class="comment">
                    <div id="alert-comment-container">
                        <g:textArea maxlength="4000" name="commentbox" id="commentbox"
                                    placeholder="Please enter your comment here." class="form-control"/>
                    </div>

                    <span class="createdBy"></span>

                    <button type="button" class="btn btn-primary add-comments pull-right m-t-10 ${buttonClass}">
                        <g:message code="default.button.add.label"/>
                    </button>
                    <br>
                    <br>
                    <span class="hide" id="executedConfigId">${caseDetail.execConfigId}</span>
                    <span class="hide" id="exConfigId">${caseDetail.execConfigId}</span>
                    <span id="application"
                          class="hide">${com.rxlogix.Constants.AlertConfigType.SINGLE_CASE_ALERT}</span>
                    <input type="hidden" id="commentId"/>
                    <span class="hide" id="prodFamily">${caseDetail.productFamily}</span>
                    <span class="hide" id="caseNo">${caseDetail.caseNumber}</span>
                    <span class="hide" id="caseNumber">${caseDetail.caseNumber}</span>
                    <span class="hide" id="assignedTo">${caseDetail.assignedTo.id}</span>
                    <span class="hide" id="eventName">${caseDetail.pt}</span>
                    <span class="hide" id="productName">${caseDetail.productName}</span>
                    <span class="hide" id="configId">${caseDetail.configId}</span>
                    <span class="hide" id="alertName">${caseDetail.alertName}</span>
                    <span class="hide" id="caseId">${caseDetail.caseId}</span>
                    <span class="hide" id="versionNum">${caseDetail.caseVersion}</span>
                    <span class="hide" id="followUpNumber">${caseDetail.folowUpNumber}</span>
                    <span class="hide" id="isFaers">${isFaers}</span>
                </div>

            </rx:containerCollapsible>
            <rx:containerCollapsible class="section m-b-10" title="Actions">

                <div id="action-list-conainter" class="list">
                    <table id="action-table" class="row-border hover dataTable no-footer" style="width: 100%">
                        <thead>
                        <tr>
                            <th><g:message code="app.action.id" default="Type"/></th>
                            <th><g:message code="app.action.list.type.label" default="Type"/></th>
                            <th><g:message code="app.label.action" default="Action"/></th>
                            <th><g:message code="app.action.list.action.details.label" default="Details"/></th>
                            <th><g:message code="app.label.due.date" default="Due Date"/></th>
                            <th><g:message code="app.label.assigned.to" default="Assigned To"/></th>
                            <th><g:message code="app.action.list.status.label" default="Status"/></th>
                            <th><g:message code="app.action.list.completion.label" default="Completion Date"/></th>
                        </tr>
                        </thead>
                    </table>
                </div>
            </rx:containerCollapsible>
            <rx:containerCollapsible class="section m-b-10" title="History">
                <div id="caseHistoryModal">

                    <span id="caseNumber" style="display: none;">${caseDetail.caseNumber}</span>
                    <span id="productFamily" style="display: none;">${caseDetail.productFamily}</span>
                    <input type="hidden" id="caseVersion" value="${caseDetail.caseVersion}"/>
                    <input type="hidden" id="productName" value="${caseDetail.productName}"/>
                    <input type="hidden" id="alertConfigId" value="${caseDetail.configId}"/>
                    <input type="hidden" id="pt" value="${caseDetail.pt}"/>
                    <g:hiddenField name="exeConfigId" id="exeConfigId" value="${caseDetail.execConfigId}" />

                    <div id="case-history-container" class="list m-b-10">
                        <label class="modal-title m-b-15"><g:message code="caseDetails.review.history.current.alertProduct"/></label>
                        <br/>
                        <table class="table caseHistoryModalTable" id="caseHistoryModalTable" style="width: 100%">
                            <thead>
                            <tr>
                                <th><g:message code="app.label.alert.name"/></th>
                                <th><g:if test="${isCaseVersion && !isFaers && !isVigibase}">
                                    <g:message code="${"app.label.qualitative.details.column.caseNumber.version"}"/>
                                </g:if><g:else>
                                    <g:message code="app.label.qualitative.details.column.caseNumber"/>
                                </g:else>
                                </th>
                                <th><g:message code="app.label.disposition"/></th>
                                <th><g:message code="app.label.justification"/></th>
                                <th><g:message code="app.label.priority"/></th>
                                <th><g:message code="app.label.tag.column"/></th>
                                <th><g:message code="app.label.subTag.column"/></th>
                                <th><g:message code="app.label.performed.by"/></th>
                                <th><g:message code="app.label.date"/></th>
                            </tr>
                            <thead>
                            <tbody id="caseHistoryModalTableBody" class="tableModalBody"></tbody>
                        </table>
                    </div>

                    <div id="case-history-container_suspect" class="list m-b-10">
                        <label class="modal-title m-b-15"><g:message code="caseDetails.review.history.other.alert"/></label>
                        <br/>
                        <table class="table" id="caseHistoryModalTableSuspect" style="width: 100%">
                            <thead>
                            <tr>
                                <th><g:message code="app.label.alert.name"/></th>
                                <th><g:if test="${isCaseVersion && !isFaers && !isVigibase && !isJader}">
                                    <g:message code="${"app.label.qualitative.details.column.caseNumber.version"}"/>
                                </g:if><g:else>
                                    <g:message code="app.label.qualitative.details.column.caseNumber"/>
                                </g:else>
                                </th>
                                <th><g:message code="app.label.disposition"/></th>
                                <th><g:message code="app.label.justification"/></th>
                                <th><g:message code="app.label.priority"/></th>
                                <th><g:message code="app.label.tag.column"/></th>
                                <th><g:message code="app.label.subTag.column"/></th>
                                <th><g:message code="app.label.performed.by"/></th>
                                <th><g:message code="app.label.date"/></th>
                            </tr>
                            <thead>
                            <tbody id="caseHistoryModalTableBody2" class="tableModalBody"></tbody>
                        </table>
                    </div>
                </div>
            </rx:containerCollapsible>
        </g:if>
    </div>

</div>

<g:render template="/includes/modals/case_diff_modal"/>
<g:if test="${!isChildCase && caseDetail}">
    <g:render template="/includes/modals/attachments_modal_dialog_case_detail" model="[alertId: caseDetail.alertId]"/>
    <g:render template="/includes/modals/actionCreateModal"
              model="[alertId: caseDetail.alertId, id: caseDetail.execConfigId, appType: 'Single Case Alert', actionConfigList: actionConfigList, isArchived: isArchived]"/>
    <g:hiddenField class="execConfigId" name="execConfigId" value="${caseDetail.execConfigId}"/>
    <g:hiddenField class="configId" name="configId" value="${caseDetail.configId}"/>
    <g:hiddenField class="productName" name="productName" value="${caseDetail.productName}"/>
</g:if>

<g:render template="/includes/modals/related_case_series" model="[caseNumber: caseNumber]"/>
<g:render template="/includes/modals/message_box"/>
<g:render template="/includes/modals/action_edit_modal"/>
<g:render template="/includes/popover/dispositionJustificationSelect"/>
<g:render template="/includes/popover/dispositionSignalSelect"
          model="[availableSignals: availableSignals, forceJustification: forceJustification, caseDetail: true]"/>
<g:render template="/includes/popover/priorityJustificationSelect"
          model="[availableAlertPriorityJustifications: availableAlertPriorityJustifications]"/>
<g:render template="/includes/popover/prioritySelect"
          model="[availablePriorities: availablePriorities, forceJustification: forceJustification]"/>

<g:hiddenField class="caseNumber" name="caseNumber" value="${caseNumber}"/>
<g:hiddenField class="alertId" name="alertId" value="${alertId}"/>
<g:hiddenField class="versionNumber" name="versionNumber" value="${version}"/>
<g:hiddenField class="followUpNumber" name="followUpNumber" value="${followUpNumber}"/>
<g:hiddenField class="isFaers" name="isFaers" value="${isFaers}"/>
<g:hiddenField class="isVaers" name="isVaers" value="${isVaers}"/>
<g:hiddenField class="isVigibase" name="isVigibase" value="${isVigibase}"/>
<g:hiddenField class="isJader" name="isJader" value="${isJader}"/>
<g:hiddenField class="absentValue" name="absentValue" value="${absentValue}"/>
<g:hiddenField class="isAdhocRun" name="isAdhocRun" value="${isAdhocRun}"/>
<g:hiddenField class="isAggAdhoc" id = "isAggAdhoc" name="isAggAdhoc" value="${isAggAdhoc}"/>
<g:hiddenField id='isArchived' name='isArchived' value="${isArchived}"/>
<g:hiddenField id='isVersion' name='isVersion' value="${isVersion}"/>
<g:hiddenField id='isArgusDataSource' name='isArgusDataSource' value="${isArgusDataSource}"/>
<g:hiddenField id='oldFollowUp' name='oldFollowUp' value="${oldFollowUp}"/>
<g:hiddenField id='oldVersion' name='oldVersion' value="${oldVersion}"/>
<g:hiddenField id='isStandalone' name='isStandalone' value="${isStandalone}"/>

</body>
</html>
