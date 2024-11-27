<div class="modal fade" id="previousAssessmentModal" class="previousAssesment" tabindex="-1" role="dialog"
     aria-hidden="true">

    <div class="modal-dialog modal-lg" role="document" style="width: 1000px">

        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span></button>
                <label class="modal-title">Previous Assessment</label>
                </button>
            </div>

            <div class="modal-body">
                <div class="row" style="margin-left: 0px;margin-right: 0px">
                    <div id="prev-assessment-container" class="list">
                        <table class="table" id="prev-assessment-table" style="width: 100%">
                            <thead>
                            <tr>
                                <th>Signal Name</th>
                                <th>Signal Term</th>
                                <th>Disposition</th>
                                <th>Date Closed</th>
                                <th>Last Review Date</th>
                                <th>Comments</th>
                                <th>Actions</th>
                            </tr>
                            <thead>
                            <tbody id="evdasHistoryModalTableBody" class="tableModalBody"></tbody>
                        </table>
                    </div>
                </div>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-default case-history-modal-close" data-dismiss="modal">
                    <g:message code="default.button.close.label"/>
                </button>
            </div>

        </div>
    </div>
</div>
