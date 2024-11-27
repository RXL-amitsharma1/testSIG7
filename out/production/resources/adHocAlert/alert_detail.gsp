<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.rxlogix.enums.ReportFormat;grails.util.Holders;" %>

<html>
<head>
    <g:javascript>
        var alertId = "${alertInst.id}"
        var userLocale = "en"
        var saveReasonUrl = "${createLink(controller: 'adHocAlert', action: 'saveDelayReason')}";
        var fetchCommentUrl = "${createLink(controller: 'adHocAlert', action: 'fetchComment')}";
        var saveCommentUrl = "${createLink(controller: 'adHocAlert', action: 'saveComment')}";
        var isArchived = false;
        var hasReviewerAccess = ${hasAdhocReviewerAccess};
    </g:javascript>

    <meta name="layout" content="main"/>
    <title>Alert Details</title>
    <asset:javascript src="fuelux/fuelux.js"/>
    <asset:javascript src="app/pvs/alerts_review/ad_hoc_alert_details_details.js"/>
    <asset:javascript src="multiple-file-upload/jquery.MultiFile.js"/>
    <asset:javascript src="app/pvs/configuration/configurationCommon.js"/>
    <asset:javascript src="app/pvs/documentManagement/document.js"/>
    <asset:javascript src="yadcf/jquery.dataTables.yadcf.js"/>
    <asset:javascript src="app/pvs/alert_utils/common_key_prevent.js"/>
    <asset:stylesheet src="fuelux.css" />
    <asset:stylesheet src="yadcf/jquery.dataTables.yadcf.css" />

    <g:render template="/includes/widgets/actions/action_types" />
    <g:render template="/includes/modals/addCommentNotesModal"/>

</head>
<body>
<div id="adhoc-detail">
    <g:if test="gobackUrl">
        <div class="m-b-10 text-right">
            <a class="btn btn-default  pv-btn-grey" href="/signal/adHocAlert/index"><i class="fa fa-long-arrow-left" aria-hidden="true"></i> Back</a>
        </div>
    </g:if>

    <rx:container title="${message(code: 'app.rxlogix.signal.alert.detail.label')}">
        <g:render template="/includes/widgets/properties_panel" model="[alertInst: alertInst,
                                                                          attributes: attributes]">
        </g:render>
    </rx:container>
    <div class="clearfix m-t-10"></div>
    <div class="row">
        <div class="col-md-12">
            <rx:container title="${message(code: 'app.rxlogix.signal.alert.detail.status.label')}">
                <g:render template="/includes/widgets/status_panel" model="[alertInst: alertInst,
                                                                            statusAttributes: statusAttributes, reasonForDelay: reasonForDelay, reviewDelay: reviewDelay]" >
                </g:render>
            </rx:container>
        </div>
      
    </div>
    <div class="clearfix m-t-10"></div>
    <!-- The code block for document management -->
    <rx:container title="${message(code: 'app.label.document.management')}">
        <g:render template="/includes/widgets/documentManagement" model="[alertInst: alertInst]"/>
        <g:render template="/includes/modals/documentModal" model="[singleDocumentObj: alertInst,
                                                                    productNames     : productNames, documentTypes: documentTypes]"/>
    </rx:container>
    <div class="clearfix m-t-10"></div>

    <g:render template="/includes/widgets/action_management_panel"
              model="[alertInst: alertInst, statusAttributes: statusAttributes, userList: userList, appType: 'Ad-Hoc Alert', actionConfigList: actionConfigList]"/>

    <div class="clearfix m-t-10"></div>
    <rx:container title="${message(code: 'app.label.attachments')}" attachFile="true">
        <g:render template="/includes/widgets/attachment_panel" model="[alertInst: alertInst, source: 'detail']"/>
        <g:render template="/includes/modals/attachments_modal_dialog" model="[alertInst: alertInst, source: 'detail']"/>
    </rx:container>
    <div class="clearfix m-t-10"></div>

    <div class="panel panel-default rxmain-container rxmain-container-top">
        <div class="panel-heading pv-sec-heading m-b-10">
            <div class="row">
                <div class="col-md-7">
                    <span class="panel-title">${message(code: 'app.label.activities')}</span>
                </div>

                <div class="col-md-5">
                    <span class="pull-right m-l-1" style="font-size: 16px; margin-right:15px; cursor: pointer">
                        <span class="dropdown-toggle exportPanel" data-toggle="dropdown">
                            <i class="mdi mdi-export font-22 theme-color" aria-hidden="true"></i>
                            <span class="caret hidden"></span></button>
                        </span>
                        <ul class="dropdown-menu export-type-list" id="exportTypes">
                            <strong class="font-12">Export</strong>
                            <li><g:link controller="activity" action="exportActivitiesReport" class="m-r-30"
                                        params="${[outputFormat: ReportFormat.DOCX, alertId: alertInst.id, appType: 'Ad-Hoc Alert', callingScreen: 'review']}">
                                <img src="/signal/assets/word-icon.png" class="m-r-10" height="16"
                                     width="16"/><g:message code="save.as.word"/>
                            </g:link>
                            </li>
                            <li><g:link controller="activity" action="exportActivitiesReport" class="m-r-30"
                                        params="${[outputFormat: ReportFormat.XLSX, alertId: alertInst.id, appType: 'Ad-Hoc Alert', callingScreen: 'review']}">
                                <img src="/signal/assets/excel.gif" class="m-r-10" height="16" width="16"/><g:message
                                        code="save.as.excel"/>
                            </g:link></li>
                            <li><g:link controller="activity" action="exportActivitiesReport" class="m-r-30"
                                        params="${[outputFormat: ReportFormat.PDF, alertId: alertInst.id, appType: 'Ad-Hoc Alert', callingScreen: 'review']}">
                                <img src="/signal/assets/pdf-icon.jpg" class="m-r-10" height="16" width="16"/><g:message
                                        code="save.as.pdf"/>
                            </g:link></li>
                        </ul>
                    </span>
                </div>
            </div>
        </div>

        <div class="row rxmain-container-content rxmain-container-show">
            <div class="col-md-12">
                <table id="activity_list" class="row-border hover no-shadow" width="100%">
                    <thead>
                    <tr>
                        <th class=""><g:message code="app.label.activity.type"/></th>
                        <th width="50%"><g:message code="app.label.description"/></th>
                        <th><g:message code="app.label.performed.by"/></th>
                        <th><g:message code="app.label.timestamp"/></th>
                    </tr>
                    </thead>
                </table>
            </div>
        </div>
    </div>

    <div class="clearfix m-t-10"></div>

    <div class="row">
        <div class="col-xs-12">
            <div class="pull-right m-r-10">
                <g:link controller="adHocAlert" action="copy" id="${params.id}"
                        class="btn btn-default pv-btn-grey ${buttonClass}"><g:message
                        code="default.button.copy.label"/></g:link>
                <g:link controller="adHocAlert" action="edit" id="${params.id}"
                        class="btn btn-primary ${buttonClass}"><g:message
                        code="default.button.edit.label"/></g:link>

            </div>
        </div> 
    </div>
</div>
</body>
</html>