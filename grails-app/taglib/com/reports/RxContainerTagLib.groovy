package com.reports

import grails.plugin.springsecurity.SpringSecurityUtils

class RxContainerTagLib {

    static  namespace = "rx"

    def container = { attrs, body ->
        def title = attrs.title ? attrs.title : ''
        def options = attrs.options
        def bean = attrs.bean ? attrs.bean : null
        def copySelection = attrs.copy
        def newAction = attrs.new_action
        def importFile = attrs.importFile
        def filters = attrs.filters
        def alertPreCheck = attrs.alertPreCheck
        def signalConfiguration = attrs .signalConfiguration
        def productTypeConfig = attrs.productTypeConfig
        def isAuditReport=attrs.isAuditReport

        out << buildContainer(title, options, body, bean,
                copySelection, newAction, importFile, filters, alertPreCheck,signalConfiguration, productTypeConfig,isAuditReport)
    }
    def buildContainer(title, options, body, bean, copy, newAction, importFile, filters, alertPreCheck,signalConfiguration, productTypeConfig,isAuditReport) {
        def content = '<div class="rxmain-container">' +
                '<div class="rxmain-container-inner">' +
                '<div class="rxmain-container-header header-alignment-boxes ico-menu">' +
                '<div class="dropdown">' +
                '<label class="rxmain-container-header-label header-alignment">'
        content += "${title}"
        content += '</label>'

        if(importFile) {
            content += '<i id="import-file" class="fa fa-upload fa-lg pull-right rxmain-dropdown-settings"></i>'
        }

        if (newAction) {
            content += '<i id="new-action" class="pull-right fa fa-bell-o fa-lg rxmain-dropdown-settings"></i>'
        }

        if(copy) {
            content += '<i id="copySelection" class="pull-right fa fa-files-o fa-lg rxmain-dropdown-settings"></i>'
        }

        if (signalConfiguration) {
            content += '<i id="newEndofReviewConfig" title="Add Disposition end of review milestone configuration" class="pull-right fa fa-plus fa-lg rxmain-dropdown-settings"></i>'
        }

        if (productTypeConfig) {
            content += '<span class="pull-right m-r-15 pos-rel" style="cursor: pointer">\n' +
                    '                <span class="dropdown-toggle exportPanel " data-toggle="dropdown" accesskey="x" tabindex="0" title="Export to" aria-expanded="false"><i class="mdi mdi-export font-24 rxmain-dropdown-settings"></i>\n' +
                    '                </span>\n' +
                    '                <ul class="dropdown-menu export-type-list" id="exportTypesTopic">\n' +
                    '                    <strong class="font-12">Export</strong>\n' +
                    '                    <li class="export_icon commentHistoryExport">\n' +
                    '                        <a href="/signal/productTypeConfiguration/exportProductRule?outputFormat=PDF" style="margin-right: 20px;text-transform: none;" class="exportCommentHistories" data-original-title="" title="">\n' +
                    '                            <img src="/signal/assets/pdf-icon.jpg" class="pdf-icon" height="16" width="16"> Save as PDF\n' +
                    '                        </a>\n' +
                    '                    </li>\n' +
                    '                    <li class="export_icon commentHistoryExport">\n' +
                    '                        <a href="/signal/productTypeConfiguration/exportProductRule?outputFormat=XLSX" style="margin-right: 20px;text-transform: none;" class="exportCommentHistories" data-original-title="" title="">\n' +
                    '                            <img src="/signal/assets/excel.gif" class="excel-icon" height="16" width="16"> Save as Excel\n' +
                    '                        </a>\n' +
                    '                    </li>\n' +
                    '                    <li class="export_icon commentHistoryExport">\n' +
                    '                        <a href="/signal/productTypeConfiguration/exportProductRule?outputFormat=DOCX" style="margin-right: 20px;text-transform: none;" class="exportCommentHistories" data-original-title="" title="">\n' +
                    '                            <img src="/signal/assets/word-icon.png" class="word-icon" height="16" width="16"> Save as Word\n' +
                    '                        </a>\n' +
                    '                    </li>\n' +
                    '                </ul>\n' +
                    '            </span>'
            content += '<i id="toggle-column-filters" title="Filters" class="pull-right mdi mdi-filter-outline font-24 rxmain-dropdown-settings" style="padding-right: 10px;"></i>'
            content += '<i id="newProductRoleConfig" title="Add Aggregate Alert Product Type Configuration" class="pull-right mdi mdi-plus-box font-24 rxmain-dropdown-settings" style="padding-right: 10px;"></i>'
        }
        if (isAuditReport) {
            content += '<span tabindex="0" class="pull-right pos-rel m-r-15" style="cursor: pointer" data-toggle="tooltip" data-title="Export to" data-placement="bottom">' +
                    '                    <span class="dropdown-toggle exportPanel" data-toggle="dropdown" >' +
                    '                        <i class="mdi mdi-export blue-1 font-22 lh-1"></i>' +
                    '                        <span class="caret hidden"></span>' +
                    '                    </span>' +
                    '                    <ul class="dropdown-menu export-type-list" id="auditReportPopUp" style="min-width: 150px ">' +
                    '                        <strong class="font-12 title-spacing">Excel</strong>' +
                    '                        <li>' +
                    '                            <a href="/signal/auditLogEvent/generateAuditLogReportFile" style="margin-right: 20px; text-transform: none;" class="exportAudit">' +
                    '                                <img src="/signal/assets/excel.gif" class="excel-icon" height="16" width="16"/> Audit Log Details' +
                    '                            </a>' +
                    '                        </li>' +
                    '                        <li>' +
                    '                            <a href="/signal/auditLogEvent/exportExcel" style="margin-right: 20px; text-transform: none;" class="exportAudit"' +
                    '                                  >' +
                    '                                <img src="/signal/assets/excel.gif" class="excel-icon" height="16" width="16"/> Audit Log Listing' +
                    '                            </a>' +
                    '                        </li>' +
                    '                        <strong class="font-12 title-spacing">PDF</strong>' +
                    '                        <li>' +
                    '                            <a href="/signal/auditLogEvent/exportPdf" style="margin-right: 20px; text-transform: none;" class="exportAudit">' +
                    '                                <img src="/signal/assets/pdf-icon.jpg" class="excel-icon" height="16" width="16"/> Audit Log Listing' +
                    '                            </a>' +
                    '                        </li>' +
                    '                    </ul>' +
                    '                </span>'

        }

        if(alertPreCheck){
            content = '<div class="rxmain-container">' +
                    '<div class="rxmain-container-inner">' +
                    '<div class="rxmain-container-header ico-menu" style="padding: 5px 10px 7px 10px;">' +
                    '<div class="dropdown pv-tab" style="height: 35px; margin-top: 0px !important;">' +
                    '<label class="rxmain-container-header-label">' +
                    '<ul id="detail-tabs" class="nav nav-tabs rxmain-container-header-label" style="background-color: transparent !important;background-image: none !important;border-bottom: none !important;box-shadow: none !important;" role="tablist">'
            if(SpringSecurityUtils.ifAnyGranted("ROLE_SINGLE_CASE_CONFIGURATION")){
                content +=  '<li role="presentation" class="active">' +
                                '<a href="#graphReport" class="alertTypeSelected" style="text-transform: none;" value="Single Case Alert" aria-controls="details" role="tab" data-toggle="tab">'+
                                message(code: 'app.label.single.case.alert.rule') +
                            '</a></li>'
            }
            if(SpringSecurityUtils.ifAnyGranted("ROLE_AGGREGATE_CASE_CONFIGURATION, ROLE_FAERS_CONFIGURATION, ROLE_VAERS_CONFIGURATION, ROLE_VIGIBASE_CONFIGURATION")){
                content +=  '<li role="presentation">' +
                                '<a href="#graphReport" class="alertTypeSelected" style="text-transform: none;" value="Aggregate Case Alert" aria-controls="details" role="tab" data-toggle="tab">'+
                                message(code: 'app.label.agg.alert.rule') +
                            '</a></li>'
            }
            if(SpringSecurityUtils.ifAnyGranted("ROLE_EVDAS_CASE_CONFIGURATION")){
                content +=  '<li role="presentation">' +
                                '<a href="#graphReport" class="alertTypeSelected" style="text-transform: none;" value="EVDAS Alert" aria-controls="details" role="tab" data-toggle="tab">'+
                                message(code: 'app.label.agg.evdas.rule') +
                            '</a></li>'
            }
            if(SpringSecurityUtils.ifAnyGranted("ROLE_LITERATURE_CASE_CONFIGURATION")){
                content +=  '<li role="presentation">' +
                                '<a href="#graphReport" class="alertTypeSelected" style="text-transform: none;" value="Literature Search Alert" aria-controls="details" role="tab" data-toggle="tab">'+
                                message(code: 'app.label.literature.alert') +
                            '</a></li>'
            }
            content += '</ul></label>'
        }

        if (options) {
            content += '<a tabindex="0" class="pull-right dropdown-toggle rxmain-dropdown-settings" data-toggle="dropdown" accesskey="c" id="dropdownMenu1" ><i class="mdi mdi-format-list-bulleted font-24" data-toggle="tooltip" data-placement="bottom" title="Add/Remove Columns"></i></a>' +
                    '<div class="pull-right dropdown-menu menu-large" aria-labelledby="dropdownMenu1">' +
                    '<div class="rxmain-container-dropdown">' +
                    '<div>' +
                    '<table id="tableColumns" class="table no-border">' +
                    '<thead>' +
                    '<tr>' +
                    '<th>'
            content += message(code: 'app.label.name')
            content += '</th>' +
                    '<th>'
            content += message(code: 'app.label.show')
            content += '</th>' +
                    '</tr>' +
                    '</thead>' +
                    '</table>' +
                    '</div>' +
                    '</div>' +
                    '</div>'
        }
        if(filters) {
            content += '<a tabindex="0" id="toggle-column-filters" ' +
                    ' data-toggle="tooltip"  data-placement="bottom" title="Filters" '+
                    'class="pull-right grid-menu-tooltip m-r-15"  accesskey="y">' +
                    '<i class="mdi mdi-filter-outline font-24 blue-1"></i>' +
                    '</a>'
        }
        if(alertPreCheck){
            content += '<a tabindex="0"  class="pull-right grid-menu-tooltip m-r-15"  accesskey="z">' +
                    '<i id="toggle-alert-pre-check"' +
                    ' data-toggle="tooltip"  data-placement="bottom" title="Alert Pre-Checks" '+
                    'class="mdi mdi-plus font-24 blue-1"></i>' +
                    '</a>'
        }
        content += '</div>' +
                '</div>' +
                '<div class="row rxmain-container-content">'
        content += "${body()}"
        content += '</div>' +
                '</div>' +
                '</div>'
    }


    def containerCollapsible = { attrs, body ->
        def title = attrs.title ? attrs.title : ''
        def options = attrs.options
        def bean = attrs.bean ? attrs.bean : null
        def copySelection = attrs.copy
        def newAction = attrs.new_action
        def importFile = attrs.importFile
        def filters = attrs.filters

        out << buildContainerCollapsible(title, options, body, bean,
                copySelection, newAction, importFile, filters)
    }

    def buildContainerCollapsible(title, options, body, bean, copy, newAction, importFile, filters) {
        String containerName = "${title?.toLowerCase()?.replaceAll("\\s", "_")}_container"
        containerName=containerName.replace("(","").replace(")","")
        def content = '<div class="rxmain-container">' +
                '<div class="rxmain-container-inner">' +
                '<a data-toggle="collapse"  data-target="#' + containerName + '"><div class="rxmain-container-header ico-menu">' +
                '<div class="dropdown">' +
                '<label class="rxmain-container-header-label">'
        content += "${title}"
        content += '</label>'

        if (importFile) {
            content += '<i id="import-file" class="fa fa-upload fa-lg pull-right rxmain-dropdown-settings"></i>'
        }

        if (newAction) {
            content += '<i id="new-action" class="pull-right fa fa-bell-o fa-lg rxmain-dropdown-settings"></i>'
        }

        if (copy) {
            content += '<i id="copySelection" class="pull-right fa fa-files-o fa-lg rxmain-dropdown-settings"></i>'
        }

        if (options) {
            content += '<span data-toggle="dropdown"><i class="pull-right dropdown-toggle rxmain-dropdown-settings" id="dropdownMenu1" data-toggle="tooltip" data-placement="bottom" title="Add/Remove Columns"></i></span>' +
                    '<div class="pull-right dropdown-menu" aria-labelledby="dropdownMenu1">' +
                    '<div class="rxmain-container-dropdown">' +
                    '<div>' +
                    '<table id="tableColumns" class="table table-condensed rxmain-dropdown-settings-table">' +
                    '<thead>' +
                    '<tr>' +
                    '<th>'
            content += message(code: 'app.label.name')
            content += '</th>' +
                    '<th>'
            content += message(code: 'app.label.show')
            content += '</th>' +
                    '</tr>' +
                    '</thead>' +
                    '</table>' +
                    '</div>' +
                    '</div>' +
                    '</div>'
        }
        if(filters) {
            content += '<span tabindex="0" id="toggle-column-filters" data-title="Filters"' +
                    'class="pull-right grid-menu-tooltip m-r-10" style="cursor: pointer">' +
                    '<i class="mdi mdi-filter-outline font-24 grey-1"></i>' +
                    '</span>'
        }
        content += '</div>' +
                '</div></a>' +
                '<div id="' + containerName + '" class="row rxmain-container-content panel-collapse collapse">'
        content += "${body()}"
        content += '</div>' +
                '</div>' +
                '</div>'
    }

    def search = { attrs, body ->
        def searchID = attrs.searchID ? attrs.searchID : ''
        def placeholder = attrs.placeholder
        def JSONUrl = attrs.JSONUrl

        out << """
            <input type="hidden" id="${searchID}_JSONUrl" value="${JSONUrl}"/>
            <input type="hidden" id="${searchID}.id" name="${searchID}.id"  />
            <div class="right-inner-addon">
                <i class="glyphicon glyphicon-search"></i>
                <input type="search" class="form-control" id="${searchID}" placeholder="${placeholder}"/>
            </div>
        """
    }


}
