<%@ page import="com.rxlogix.config.CommentTemplate; com.rxlogix.Constants;  com.rxlogix.enums.ReportFormat" %>
<div class="modal fade" data-backdrop="static" id="commentModal" tabindex="-1" role="dialog" aria-hidden="true">
    <div class="modal-dialog modal-lg" style="width: 1000px">
        <div class="modal-content" style="margin-left: 0;margin-right: 0">
            <div class="modal-header">
                <g:if test="${appType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT}">
                    <button type="button" class="pull-right close" id="upper-close-comment" aria-label="Close">
                        <span aria-hidden="true"  style="font-size: 27px">×</span></button>
                </g:if>
                <g:else>
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">×</span></button>
                </g:else>
                <g:if test="${appType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT}">
                    <span class="pull-right m-r-15 pos-rel" style="cursor: pointer">
                        <span class="dropdown-toggle exportPanel " data-toggle="dropdown" accesskey="x" tabindex="0" title="Export to"><i class="mdi mdi-export blue-1 font-18 lh-1"></i>
                            <span class="caret hidden"></span>
                        </span>
                        <ul class="dropdown-menu export-type-list" id="exportTypesTopic">
                            <strong class="font-12">Export</strong>
                            <li class="export_icon commentHistoryExport">
                                <g:link controller="alertComment" action="exportCommentHistory" style="margin-right: 20px" class="exportCommentHistories"
                                         params="${[outputFormat: ReportFormat.PDF,isArchived:isArchived]}">
                                    <asset:image src="pdf-icon.jpg" class="pdf-icon" height="16" width="16"/> <g:message code="save.as.pdf" />
                                </g:link>
                            </li>
                            <li class="export_icon commentHistoryExport">
                                <g:link controller="alertComment" action="exportCommentHistory" style="margin-right: 20px" class="exportCommentHistories"
                                        params="${[outputFormat: ReportFormat.XLSX,isArchived:isArchived]}">
                                    <asset:image src="excel.gif" class="excel-icon" height="16" width="16"/> <g:message code="save.as.excel" />
                                </g:link>
                            </li>
                            <li class="export_icon commentHistoryExport">
                                <g:link controller="alertComment" action="exportCommentHistory" style="margin-right: 20px" class="exportCommentHistories"
                                        params="${[outputFormat: ReportFormat.DOCX,isArchived:isArchived]}">
                                    <asset:image src="word-icon.png" class="word-icon" height="16" width="16"/> <g:message code="save.as.word" />
                                </g:link>
                            </li>
                        </ul>
                    </span>
                </g:if>
                <h4 class="modal-title " style="font-weight: bold"> Comments :
                    <span id="comment-meta-info"></span>
                <g:if test="${appType == com.rxlogix.Constants.AlertConfigType.SINGLE_CASE_ALERT}">
                    <i style="cursor:pointer" class="fa fa-info-circle comment-history-icon f14" aria-hidden="true"></i>
                </g:if>
                    <i class="isProcessing mdi mdi-spin mdi-loading" style="display: none;"></i>
                </h4>
            </button>
            </div>
            <g:render template="/includes/layout/flashErrorsDivs" bean="${executedConfigId}" var="theInstance"/>
            <div class="modal-body" style="max-height: 450px; overflow-y: auto; padding: 15px !important;">
                <g:if test="${appType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT &&  (selectedDatasource!='jader' && selectedDatasource!='vigibase')}">
                    <div id="commentTemplateList" style="margin-bottom: 2px">
                        <label style="padding-right: 7px">Template </label>
                        <select class="form-control width-30" id="commentTemplateSelect" name="commentTemplateSelect" style="display: inline-block">
                            <option value="none" selected >Select an Option</option>
                            <g:each in="${CommentTemplate.list().sort({ it.name.toUpperCase()})}" var="commentTemplate">
                                <option value="${commentTemplate.id}">${commentTemplate.name}</option>
                            </g:each>
                        </select>
                    </div>
                </g:if>
                <div id="no-comments" class="hide">
                    <h4 class="text-center">There are no comments</h4>
                </div>
                <div id="alert-comment-container" class="list">
                    <div class="form-group"><g:textArea maxlength="4000" name="commentbox" id="commentbox" rows="12" placeholder="Please enter your comment here."  style="height: 150px !important;" class="height-150 form-control"/>
                    </div>
                </div>

                <span class="createdBy"></span>

                <input type="hidden" id="commentId"/>
                <input type="hidden" id="caseId"/>
                <input type="hidden" id="versionNum"/>

                <g:if test="${appType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT}">
                    <div>
                        <div class="row">
                            <div class="col-md-12">
                                <table id="commentHistoryTable" class="row-border hover" width="100%">
                                    <thead>
                                    <tr>
                                        <th class="col-md-2"><g:message code="app.label.comment.template.period"
                                                       default="Period"/></th>
                                        <th class="col-md-6"><g:message code="app.label.comment.template.comment" default="Comment"/></th>
                                        <th class="col-md-2"><g:message code="app.label.comment.template.modifiedBy" default="Modified By"/></th>
                                        <th class="col-md-2"><g:message code="app.label.comment.template.date" default="Date"/></th>
                                    </tr>
                                    </thead>
                                </table>
                            </div>
                        </div>
                    </div>
                </g:if>
            </div>

            <div class="modal-footer">
                <div class="bulkOptionsSection pull-left m-l-5" style="display: none;">
                    <label class="labelBold m-r-15" for="current">
                        <input class="m-r-5" type="radio" name="bulkOptions" id="current" value="current" checked>
                        Current <span class="alertTypeText"></span>
                    </label>
                    <label class="labelBold" for="allSelected">
                        <input class="m-r-5" type="radio" name="bulkOptions" id="allSelected" value="allSelected">
                        Selected <span class="alertTypeText"></span>s (<span class="count"></span>)
                    </label>
                </div>
                <button type="button" class="btn btn-primary add-comments ${buttonClass}">
                    <g:message code="default.button.add.label"/>
                </button>
                <g:if test="${appType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT}">
                    <button type="button" class="btn btn-default alert-comment-modal-close">
                        <g:message code="default.button.close.label"/>
                    </button>
                </g:if>
                <g:else>
                    <button type="button" class="btn btn-default alert-comment-modal-close" data-dismiss="modal">
                        <g:message code="default.button.close.label"/>
                    </button>
                </g:else>
            </div>
        </div>
        <div class="hidden">
            <span id="application"></span>
            <span id="validatedSignalId"></span>
            <span id="assignedTo"></span>
            <span id="executedConfigId"></span>
            <span id="configId"></span>
            <input type="hidden" id="isUpdated" value="false"/>

        </div>
    </div>
</div>

<script>
    $("#commentModal").on('show.bs.modal', function(){
        $('div.bulkOptionsSection input#current').click();
    });
</script>