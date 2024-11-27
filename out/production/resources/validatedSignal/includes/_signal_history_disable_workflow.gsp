<%@ page import="com.rxlogix.util.DateUtil"%>
<div class="container-fluid attachments">
    <table id="signal-history-table" class="dataTable row-border hover no-footer">
        <thead>
        <tr>
            <th class="sorting_disabled"><g:message code="signal.history.label.status"/><span
                    class="required-indicator">*</span></th>
            <th class="sorting_disabled"><g:message code="signal.history.label.status.comment"/></th>
            <th class="sorting_disabled pvi-col-md"><g:message code="signal.history.label.performedBy"/></th>
            <th class="sorting_disabled"><g:message code="signal.history.label.date"/><span
                    class="required-indicator">*</span></th>
            <th class="sorting_disabled ${buttonClass}"></th>
        </tr>
        </thead>
        <tbody>
        <g:if test="${signalHistoryList}">
            <g:each status="i" in="${signalHistoryList}" var="signalHistory">
                <tr role="row" class="${(i % 2) == 0 ? 'odd' : 'even'} signalHistoryRow">
                    <input type="hidden" class="signalHistoryId" value="${signalHistory.id}"/>
                    <td class="col-md-2">
                        <g:if test="${!signalHistory.dispositionUpdated}">
                            <g:select name="signalStatus${i}" class="form-control status"
                                      from="${[signalHistory.signalStatus]}" noSelection="['': '-Select Status -']"
                                      value="${signalHistory.signalStatus}"/>

                        </g:if>
                        <g:else>
                            <g:select name="signalStatus" class="form-control status"
                                      from="${signalHistory.signalStatusList}" noSelection="['': '-Select Status -']"
                                      value="${signalHistory.signalStatus}"/>
                        </g:else>
                    </td>

                    <td class="col-md-5">
                        <div class="textarea-ext">
                            <g:textField id="SignalStatusComments" name="comment${i}" class="form-control comment ellipsis-comment"

                                         value="${signalHistory.statusComment == null ? "" : signalHistory.statusComment }" maxlength="8000"/>
                            <a class="btn-text-ext openStatusComment" href="" tabindex="0" title="Open in extended form">
                                <i class="mdi mdi-arrow-expand font-20 blue-1"></i></a>
                        </div>
                    </td>
                    <td class="col-md-2">
                        ${signalHistory.performedBy}
                    </td>

                    <td class="col-md-2">
                        <div class="fuelux">
                            <div class="datepicker toolbarInline ${signalHistory.signalStatus!=com.rxlogix.Constants.WorkFlowLog.DUE_DATE? 'historyDatePicker':'' } ">
                                <div class="input-group">

                <g:if test="${signalHistory.isAutoPopulate && signalHistory.dispositionUpdated}">
                    <g:textField name="date-created${i}" placeholder="Select Date"
                                                     class="form-control date-created"
                                                     value="${signalHistory.dateCreated ? (signalHistory.signalStatus!=com.rxlogix.Constants.WorkFlowLog.DUE_DATE?DateUtil.toDateString(signalHistory.dateCreated, timezone):DateUtil.toDateString(signalHistory.dateCreated)) : ""}"/>
                </g:if>
                <g:else>
                    <g:textField name="date-created${i}" placeholder="Select Date"
                                 class="form-control date-created"
                                 value="${signalHistory.dateCreated ? (DateUtil.toDateString(signalHistory.dateCreated)) : ""}"/>
                </g:else>
                                    <g:if test="${!signalHistory.dispositionUpdated}">
                                        <div class="input-group-btn">
                                            <g:render template="/includes/widgets/datePickerTemplate"/>
                                        </div>
                                    </g:if>
                                    <g:else>
                                        <g:render template="/includes/widgets/datePickerTemplate"/>
                                    </g:else>
                                </div>
                            </div>
                        </div>
                    </td>
                    <td class="icons-class col-md-1 text-right ${buttonClass}">

                        <a tabindex="0" href="#" title="Save" class="save-history"><i
                                class="mdi mdi-check grey-2"></i></a>
                        <g:if test="${i == signalHistoryList.size() - 1 && signalHistory.isAddRow}">
                            <a href="#" tabindex="0" title="Add row" class="btn-add-row"><i
                                    class="mdi mdi-plus-box font-24 blue-1"></i></a>
                        </g:if>
                    </td>
                </tr>
            </g:each>
        </g:if>
        <g:else>
            <tr role="row" class="odd signalHistoryRow">
                <td class="col-md-2">
                    <g:select name="signalStatus" class="form-control status"
                              from="${signalStatusList}" noSelection="['': '-Select Status -']"/>
                </td>
                <td class="col-md-5">
                    <div class="textarea-ext">
                        <textarea type="text" class="form-control comment ellipsis-comment" rows="1" style="min-height: 25px" maxlength="8000"></textarea>
                        <a class="btn-text-ext openStatusComment" href="" tabindex="0" title="Open in extended form">
                            <i class="mdi mdi-arrow-expand font-20 blue-1"></i></a>
                    </div>
                </td>
                <td class="col-md-1">
                    ${currentUserFullName}
                </td>
                <td class="col-md-2">
                    <div class="fuelux">
                        <div class="datepicker toolbarInline historyDatePicker">
                            <div class="input-group">
                                <input placeholder="Select Date"
                                       class="form-control date-created"
                                       name="Date" type="text" value=""/>
                                <g:render template="/includes/widgets/datePickerTemplate"/>
                            </div>
                        </div>
                    </div>
                </td>
                <td class="icons-class col-md-1 text-right">
                    <a tabindex="0" href="#" title="Save" class="save-history"><i
                            class="mdi mdi-check grey-2"></i></a>
                    <a href="#" tabindex="0" title="Add row" class="btn-add-row disabled"><i
                            class="mdi mdi-plus-box font-24 grey-1"></i></a>
                </td>
            </tr>
        </g:else>
        <tr role="row" class="hidden tr_clone signalHistoryRow">
            <td class="col-md-2">
                <g:select name="signalStatus" class="form-control status"
                          from="${signalStatusList}" noSelection="['': '-Select Status -']"/>
            </td>

            <td class="col-md-5">
                <div class="textarea-ext">
                    <textarea type="text" class="form-control comment ellipsis-comment" rows="1" style="min-height: 25px" maxlength="8000"></textarea>
                    <a class="btn-text-ext openStatusComment" href="" tabindex="0" title="Open in extended form">
                        <i class="mdi mdi-arrow-expand font-20 blue-1"></i></a>
                </div>
            </td>
            <td class="col-md-1">
                ${currentUserFullName}
            </td>
            <td class="col-md-2">
                <div class="fuelux">
                    <div class="datepicker toolbarInline historyDatePicker">
                        <div class="input-group">
                            <input placeholder="Select Date"
                                   class="form-control date-created"
                                   name="Date" type="text" value=""/>
                            <g:render template="/includes/widgets/datePickerTemplate"/>
                        </div>
                    </div>
                </div>
            </td>
            <td class="icons-class col-md-1 text-right ${buttonClass}">
                <a tabindex="0" href="#" title="Save" class="save-history"><i
                        class="mdi mdi-check grey-2"></i></a>
                <a href="#" tabindex="0" title="Add row" class="btn-add-row disabled"><i
                        class="mdi mdi-plus-box font-24 grey-1"></i></a>
            </td>
        </tr>
        </tbody>
    </table>
</div>
