<div class="modal fade" id="importAssignmentFormatModal" tabindex="-1" role="dialog" aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span></button>
                <label class="modal-title">Import Assignment Format</label>
            </div>

            <div class="modal-body">
                <div>Import Values from a file:</div>
                <div>- Values will only be imported from the first worksheet of excel/CSV file</div>
                <div>- First row of excel/CSV file is the header</div>
                <div>- Order for columns are Products, Product Hierarchy, Assignment, Workflow Group, Import</div>
                <div>- <b>Import:</b> If the value is blank for the record then the record will be imported and if it is 0 then the record will be discarded.</div>
                <table id="importAssignmentFormatTable" class="row-border hover simple-alert-table" width="100%">
                    <thead>
                    <tr>
                        <th class="col-md-3">Product</th>
                        <th class="col-md-2">Product Hierarchy</th>
                        <th class="col-md-3">Assignment</th>
                        <th class="col-md-2">Workflow Group</th>
                        <th class="col-md-2">Import</th>
                    </tr>
                    </thead>
                    <tbody>
                    <td class="col-md-3">Paracetamol M Tablet</td>
                    <td class="col-md-2">Product Name</td>
                    <td class="col-md-3">pvs_admin,All Users</td>
                    <td class="col-md-2">Default</td>
                    <td class="col-md-2">0</td>
                    </tbody>
                </table>
            </div>

            <div class="modal-footer">
                <div class="buttons ">
                    <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
                </div>
            </div>
        </div>
    </div>
</div>