<div class="modal fade" id="commentHistoryModal" tabindex="-1" role="dialog" aria-hidden="true" style="max-height: 95%">
    <div class="modal-dialog modal-xl">
        <div class="modal-content" style="margin-left: 0;margin-right: 0">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">×</span></button>
                <h4 class="modal-title " style="font-weight: bold"> Comments History
                    <i class="isProcessing mdi mdi-spin mdi-loading" style="display: none;"></i>
                </h4>
            </button>
            </div>

            <div class="modal-body">

                <div id="comment-history-container" class="list">
                    <label class="modal-title m-b-15">Comments History</label>
                    <br/>
                    <table class="table commentHistoryModalTable" id="commentHistoryModalTable" style="width: 100%">
                        <thead>
                        <tr>
                            <th><g:message code="app.label.alert.name"/></th>
                            <th><g:if test="${isCaseVersion && !isFaers && !isVaers}">
                                <g:message code="${"app.label.qualitative.details.column.caseNumber.version"}"/>
                            </g:if><g:else>
                                <g:message code="${isVaers||isFaers||isVigibase ? "app.label.qualitative.details.column.caseNumber.vaers":"app.label.qualitative.details.column.caseNumber"}"/>
                            </g:else>
                            </th>
                            <th><g:message code="app.comment.history.old"/></th>
                            <th><g:message code="app.comment.history.new"/></th>
                            <th><g:message code="app.label.modifiedBy"/></th>
                            <th><g:message code="app.comment.history.date"/></th>
                        </tr>
                        <thead>
                        <tbody id="commentHistoryModalTableBody" class="tableModalBody"></tbody>
                    </table>
                </div>
                <br/>

            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default case-history-modal-close" data-dismiss="modal">
                    <g:message code="default.button.close.label"/>
                </button>
            </div>
        </div>

    </div>
</div>

<script>

</script>