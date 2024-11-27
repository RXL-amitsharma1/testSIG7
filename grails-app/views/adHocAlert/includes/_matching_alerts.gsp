<div class="modal fade" id="matched-alert-modal" tabindex="-1" role="dialog" aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <label class="modal-title">Matched Alerts</label>
            </div>
            <div class="modal-body">
                <table class="table" id="matched-alerts-table" style="width:100%;">
                    <thead>
                    <tr>
                        <th>Name</th>
                        <th>Product/Generic Name</th>
                        <th>Topic</th>
                        <th>Detected Date</th>
                        <th>Disposition</th>
                    </tr>
                    </thead>
                    <tbody id="matched-alerts-body" class="tableModalBody"></tbody>
                </table>
            </div>

            <div class="modal-footer">
                <div class="buttons ">
                    <button class="btn btn-primary" id="matched-alerts-update-bt" data-dismiss="modal">Issue Previously Tracked</button>
                    <button class="btn btn-default" id="matched-alerts-cancel-bt" data-dismiss="modal">Cancel</button>
                </div>
            </div>
        </div>
    </div>
</div>