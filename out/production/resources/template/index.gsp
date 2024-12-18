<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.TemplateLibrary.title" /></title>
    <asset:javascript src="app/pvs/template/template.js"/>
    <asset:javascript src="app/pvs/dataTablesActionButtons.js"/>
    <asset:javascript src="app/dms/dmsConfiguration.js"/>
    <g:javascript>
        var filterList = "${filterList}"
        var filters = "${filters}"
        var isFilterRequest = "${isFilterRequest}"
        var advancedFilterId = "${advancedFilterId}"
        var TEMPLATE = {
            listUrl: "${createLink(controller: 'reportTemplateRest')}",
            runReport: "${createLink(controller: 'api', action: 'reportBuilder',params: [filterList:filterList?:null, advancedFilterId: advancedFilterId?:null,filters: filters?:null, isFilterRequest: isFilterRequest?:null])}",
            generatedReportsUrl : "${createLink(controller: 'template', action: 'getGeneratedReports')}",
            downloadReportUrl : "${createLink(controller: 'template', action: 'downloadSignalReport')}"
        }
        var dmsFoldersUrl = "${createLink(controller: 'controlPanel', action: 'getDmsFolders')}";
        var addDmsConfiguration = "${createLink(controller: "controlPanel", action: "addDmsConfiguration")}";
        var dmsDocTypeValue = "${com.rxlogix.Constants.DMSDocTypes.GENERATED_REPORT}";
        var isAggScreen = ${isAggScreen}
        var hasReviewerAccess = ${hasReviewerAccess}
        var isDMSEnabled = ${grailsApplication.config.dms.enabled}
    </g:javascript>
    <g:javascript>
        $(document).on('click', '.sendToDms', function () {
            $('#sendToDmsModal').modal();
            $('#docTypeValue').val($(this).data('doc-type'));
            $('#reportId').val($(this).data('id'));
        });
    </g:javascript>
    <style>
    .pvs-validate-tabpan .dataTables_filter { display: block; }
    </style>
</head>

<body>
<div class="pvs-validate-tabpan">
    <g:render template="/includes/layout/flashErrorsDivs"/>

    <rx:container title="Alert Details">

        <div class="row">
            <div class="col-sm-2">
                <label><g:message code="app.label.alert.name"/></label>

                <div>${alertName}</div>
            </div>

            <div class="col-sm-2">
                <label><g:message code="app.label.version"/></label>

                <div id = 'versionNumber' style="padding-left: 15px">${version}</div>
            </div>

        <div class="col-sm-2 m-r-15">
            <label><g:message code="app.label.description"/></label>
            <div class="col-container max-height-20">
                <div class="col-height">${description}
                    <a tabindex="0"
                       title="View All"
                       class="ico-dots ico-circle view-all alert-ellipsis"
                       style="display: inline-block"
                       more-data="${description}">
                        <i class="mdi mdi-dots-horizontal font-20 blue-1"></i></a>
                </div>
            </div>
         </div>

            <div class="col-sm-2 m-l-25 m-r-15">
                <label><g:message code="app.label.product"/></label>

                <div class="col-container max-height-20">
                    <div id='product' >${productNameList}
                        <a tabindex="0"
                           title="View All"
                           class="ico-dots ico-circle view-all alert-ellipsis"
                            style="display: inline-block"
                           more-data="${productNameList}">
                            <i class="mdi mdi-dots-horizontal font-20 blue-1"></i></a>
                    </div>
                </div>
            </div>

            <div class="col-sm-2 m-l-25">
                <label><g:message code="app.label.DateRange"/></label>

                <div>${dateRange}</div>
            </div>

            <div class="col-sm-1 col-sm-offset-1">

            </div>
        </div>
    </rx:container>

    <rx:container title="Generated Reports">

        <table id="signalReportTable" class="row-border hover" width="100%">
            <thead>
            <tr>
                <th>Report Name</th>
                <th>Product</th>
                <th>PT/SMQ/Event Group</th>
                <th>Count Type</th>
                <th>Generated By</th>
                <th>Generation Date</th>
                <th>Reports</th>
            </tr>
            </thead>
        </table>
    </rx:container>

    <rx:container title="Template Library">
        <div>
            <table id="rxTableTemplates" class="row-border hover" width="100%">
                <thead>
                <tr>
                    <th><g:message code="app.label.category" /></th>
                    <th><g:message code="app.label.name" /></th>
                    <th><g:message code="app.label.description" /></th>
                    <th><g:message code="app.label.modifiedDate" /></th>
                    <th><g:message code="app.label.owner" /></th>
                    <th><g:message code="app.label.createdDate" /></th>
                    <th style="width: 35px;"><g:message code="app.label.action"/></th>
                </tr>
                </thead>
            </table>
        </div>
    </rx:container>
</div>
<g:form controller="DMSIntegration" action="sendToDms">
    <g:hiddenField name="docTypeValue" value=""/>
    <input type="hidden" id="selectedCases" value="${selectedCases}"/>
    <g:hiddenField name="reportId" id="reportId" value=""/>
    <g:render plugin="pvdms" template="/dms/sendToDmsModal"/>
</g:form>
</body>
