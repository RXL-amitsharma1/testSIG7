<%@ page import="com.rxlogix.enums.ReportFormat" %>

<div class="panel panel-default rxmain-container rxmain-container-top">
    <div class="panel-heading pv-sec-heading m-b-10">
        <div class="row">
            <div class="col-md-7">
                <span class="panel-title">${message(code: 'app.label.action.management')}</span>
            </div>

            <div class="col-md-5">
                <span class="pull-right m-l-1" style="font-size: 16px; margin-right:15px; cursor: pointer">
                    <span class="dropdown-toggle exportPanel" data-toggle="dropdown">
                        <i class="mdi mdi-export font-22 theme-color" aria-hidden="true"></i>
                        <span class="caret hidden"></span></button>
                    </span>
                    <ul class="dropdown-menu export-type-list" id="exportTypes">
                        <strong class="font-12">Export</strong>
                        <li><g:link controller="action" action="exportActionsReport" class="m-r-30"
                                    params="${[outputFormat: ReportFormat.DOCX, alertId: alertInst.id, appType: appType, callingScreen: 'review']}">
                            <img src="/signal/assets/word-icon.png" class="m-r-10" height="16"
                                 width="16"/><g:message code="save.as.word"/>
                        </g:link>
                        </li>
                        <li><g:link controller="action" action="exportActionsReport" class="m-r-30"
                                    params="${[outputFormat: ReportFormat.XLSX, alertId: alertInst.id, appType: appType, callingScreen: 'review']}">
                            <img src="/signal/assets/excel.gif" class="m-r-10" height="16" width="16"/><g:message
                                    code="save.as.excel"/>
                        </g:link></li>
                        <li><g:link controller="action" action="exportActionsReport" class="m-r-30"
                                    params="${[outputFormat: ReportFormat.PDF, alertId: alertInst.id, appType: appType, callingScreen: 'review']}">
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
            <g:render template="/includes/widgets/action_list_panel"
                      model="[alertInst: alertInst, attributes: statusAttributes]"/>
        </div>
    </div>
</div>
<g:render template="/includes/modals/actionCreateModal"
          model="[alertId: alertInst.id, appType: 'Ad-Hoc Alert', userList: userList, actionConfigList: actionConfigList, isArchived: false,id:alertInst.id]"/>
<g:render template="/includes/modals/action_edit_modal" model="[alertInst: alertInst]"/>
