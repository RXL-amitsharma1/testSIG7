<style>
  #similarCaseModal .modal-content {
      margin-left: -200px;
      margin-right: -200px;
  }
</style>

<div class="modal fade" id="similarCaseModal" tabindex="-1" role="dialog" aria-hidden="true" >
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span></button>
                <label class="modal-title">Similar cases based on - <span id="eventType"
                                                                          style="text-transform:uppercase"></span> : <span
                        id="eventVal"></span></label>
            </button>
            </div>

            <div class="modal-body">
                <div class="event-outcome-pie"></div>
                <br/>
                <div id="similar-case-container" class="list">
                    <table class="table" id="similarCaseModalTable" style="width: 100%">
                        <thead>
                        <tr>
                            <th class="reportDescriptionColumn">Case(f/u#)</th>
                            <th>Product</th>
                            <th>Preferred Term</th>
                            <th>HCP</th>
                            <th>
                                <div class="stacked-cell-center-top">S/U/R</div>
                                <div class="stacked-cell-center-bottom">Outcome</div>
                            </th>
                            <th>Disposition</th>
                            <th>Priority</th>
                            <th>Assigned To</th>
                        </tr>
                        <thead>
                        <tbody id="similarCaseModalTableBody" class="tableModalBody"></tbody>
                    </table>
                </div>
                <input type="hidden" id="alertName" />
                <input type="hidden" id="caseNumberInfo" />
                <input type="hidden" id="caseCurrentVersion" />
                <input type="hidden" id="executedConfigId" />
                <input type="hidden" id="eventCode" />
                <input type="hidden" id="eventCodeVal" />


            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default similar-case-modal-close" data-dismiss="modal">
                    <g:message code="default.button.close.label"/>
                </button>
            </div>
        </div>
    </div>
</div>
