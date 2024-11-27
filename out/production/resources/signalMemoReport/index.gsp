<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils;grails.converters.JSON" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main"/>
    <title><g:message code="app.label.signal.memo.configuration"/></title>
    <g:javascript>
        var signalMemoConfigUrl = "${createLink(controller: 'signalMemoReport', action: 'fetchSignalMemoConfig')}";
        var saveSignalMemoConfigUrl = "${createLink(controller: 'signalMemoReport', action: 'saveSignalMemoConfig')}";
        var deleteSignalMemoConfigUrl = "${createLink(controller: 'signalMemoReport', action: 'deleteSignalMemoConfig')}";
        var configExistsUrl = "${createLink(controller: 'signalMemoReport', action: 'checkIfConfigExistsWithConfigName')}";
        var searchUserGroupListUrl = "${createLink(controller: 'user', action: 'searchUserGroupList')}";
        var signalSource = JSON.parse('${signalSource}');
        var triggerVariable = JSON.parse('${triggerVariable}');
        var signalOutcomes = JSON.parse('${signalOutcomes}');
        var signalOutcomesToBeDisabled = JSON.parse('${signalOutcomesToBeDisabled}');
        var actionsTaken = JSON.parse('${actionsTaken}');
    </g:javascript>
    <style>
        table.dataTable tfoot td {
            border-top: 1px solid #ece5e5;
        }
        #signalMemoReportTable_wrapper .dataTables_scrollBody{
            max-height: 65vh;
        }

        .email-class .select2-container--default {
            max-width: 150px !important;
        }

        .signal-source-class .select2-container--default {
            max-width: 198px !important;
        }

        .trigger-val-class .select2-container--default {
            max-width: 150px !important;
        }

        .dd-content {
            margin-left: -25px;
            position: relative;
            bottom: 7px;
         }
        /*.signal-memo-email {*/
        /*    width: 150px !important;*/
        /*}*/
    </style>
    <asset:javascript src="app/pvs/signalMemoReport/signal_memo_report.js"/>
</head>

<body>

<div class="panel panel-default rxmain-container rxmain-container-top m-b-0">

    <div class="rxmain-container-inner">
        <div class="rxmain-container-row rxmain-container-header panel-heading pv-sec-heading">
            <div class="row">
                <label class="rxmain-container-header-label m-t-5">${message(code: "app.label.signal.memo.configuration")}</label>

                <span class="pv-head-config configureFields ">
                    <a href="#" id="notificationNewRow" class="pull-right ic-sm"
                       data-toggle="tooltip" data-placement="bottom">
                        <i class="md md-add" aria-hidden="true"></i></a>
                </span>
            </div>
        </div>
    <g:render template="/includes/layout/flashErrorsDivs"/>
        <div class="collapse in" id="signalMemoContainer">
            <div class="rxmain-container-content">
            <table id="signalMemoReportTable" class="dataTable table table-striped row-border hover no-footer">
                <thead>
                <tr class="relative-position">
                    <th class="col-md-1-half"><g:message code="app.label.signal.memo.report.config.name"/><span class="required-indicator">*</span></th>
                    <th class="col-md-2"><g:message code="app.label.signal.memo.report.signal.source"/></th>
                    <th class="col-md-1-half"><g:message code="app.label.signal.memo.report.trigger.variable"/><span class="required-indicator">*</span></th>
                    <th class="col-md-1-half"><g:message code="app.label.signal.memo.report.trigger.value"/><span class="required-indicator">*</span></th>
                    <th class="col-md-1-half"><g:message code="app.label.signal.memo.report.email"/><span class="required-indicator">*</span></th>
                    <th class="col-md-1-half"><g:message code="app.label.signal.memo.report.email.subject"/><span class="required-indicator">*</span></th>
                    <th class="col-md-1-half"><g:message code="app.label.signal.memo.report.email.body"/><span class="required-indicator">*</span></th>
                    <th class="col-md-1"><g:message code="app.label.signal.memo.report.actions"/></th>
                </tr>
                </thead>
                <tbody></tbody>
                <tfoot class="signal-memo-new-row">
                <tr class="relative-position">
                    <td class="col-md-1-half">
                        <div class="textarea-ext">
                            <input type="text" class="form-control config-name" name="config-name" maxlength="255" style="width: 100%">
                        </div>
                    </td>
                    <td class="col-md-2">
                        <div class="signal-source signal-source-class">
                            <g:select name="signalSource" class="form-control signalSource"
                                      from="${JSON.parse(signalSource)}" multiple="multiple"/>
                        </div>
                    </td>
                    <td class="col-md-1-half">
                        <g:select name="triggerVariable" class="form-control triggerVariable"
                                  from="${JSON.parse(triggerVariable).sort({it.toUpperCase()})}" noSelection="['': 'Select']"/>
                    </td>
                    <td class="col-md-1-half">
                        <div class="textarea-ext trigger-val-class">
                            <input type="number" class="form-control trigger-value" name="trigger-value" min="0" step="1" style="width: 100%" pattern="[0-9]*" oninput="this.value = this.value.replace(/e/g, '')">
                            <g:select name="signalOutcomes" class="form-control signalOutcomes hide"
                                      from="${JSON.parse(signalOutcomes)}"/>
                            <g:select name="actionsTaken" class="form-control actionsTaken hide"
                                      from="${JSON.parse(actionsTaken)}"/>
                        </div>
                    </td>
                    <td class="col-md-1-half email-class">
                        <g:initializeEmailForSignalMemo assignedToId="email-addresses" isLabel="false" isTags="true" bean="${signalMemo}"/>
                    </td>
                    <td class="col-md-1-half">
                        <div class="textarea-ext">
                            <input type="text" class="form-control email-subject" name="email-subject" maxlength="255" style="width: 100%">
                        </div>
                    </td>
                    <td class="col-md-1-half">
                        <div class="textarea-ext">
                            <textarea type="text" class="form-control email-body" name="email-body" maxlength="4000" rows="1" style="width: 100%; min-height: 25px;"></textarea>
                             <a class="btn-text-ext openStatusComment" href="javascript:void(0);" tabindex="0" data-name = "emailBody"
                              title="Open in extended form">
                             <i class="mdi mdi-arrow-expand font-20 blue-1"></i></a>
                        </div>
                    </td>
                    <td class="col-md-1 text-center">
                        <a href="javascript:void(0);" title="Save" class="table-row-saved save-signal-memo hidden-ic pv-ic">
                            <i class="mdi mdi-check" aria-hidden="true"></i>
                        </a>
                        <a href='javascript:void(0);' title='Delete' class='table-row-del remove-signal-memo hidden-ic'>
                            <i class='mdi mdi-close' aria-hidden='true'></i>
                        </a>
                    </td>
                </tr>
                </tfoot>
            </table>
        </div>
    </div>
  </div>
</div>
<g:render template="/validatedSignal/includes/extendedTextarea"/>
</body>
</html>
