<div class="modal fade" id="signalHistoryModal" tabindex="-1" role="dialog" aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content" >

            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span></button>
                <label class="modal-title"><g:message code="app.label.signal.history"/></label>
            </button>
            </div>

            <div class="modal-body" >
                <div id="partner-container" class="list">

                    <table class="table" id="signalHistoryModalTable" style="width: 100%">
                        <thead>
                        <tr>
                            <th class="nowrap-text"><g:message code="app.label.disposition"/></th>
                            <th class="nowrap-text"><g:message code="app.label.priority"/></th>
                            <th class="nowrap-text"><g:message code="app.label.assigned.to"/></th>
                            <th class="nowrap-text"><g:message code="app.label.case.history.time.stamp"/></th>
                            <th class="nowrap-text"><g:message code="app.label.case.history.performaed.by"/></th>
                            <th class="nowrap-text"><g:message code="app.label.case.history.justification"/></th>
                        </tr>
                        <thead>
                        <tbody id="signalHistoryModalTableBody" class="tableModalBody"></tbody>
                    </table>

                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default previous-followUp" data-dismiss="modal">
                    Cancel
                </button>
            </div>
        </div>
    </div>
</div>
