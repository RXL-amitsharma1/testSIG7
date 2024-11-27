<div class="modal fade" data-backdrop="static" id="case-form-name-modal" tabindex="-1" role="dialog" aria-hidden="true">
    <div class="modal-dialog modal-md">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title" id="">Case Form File Name</h4>
            </div>
            <div class="modal-body">
                <div class="alert alert-danger alert-dismissible hide" role="alert">
                    <button type="button" class="close alert-name-close">
                        <span aria-hidden="true">×</span>
                        <span class="sr-only">Close</span>
                    </button>
                    <ul id="form-name-error"></ul>
                </div>
                <div class="row">
                    <div class="col-md-12">
                        <label>File Name</label>
                        <input type="text" class="form-control" maxlength="255" id="case-form-file-name" />
                    </div>
                </div>
            </div>

            <div class="modal-footer">
                <div class="buttons ">
                    <button type="button" class="btn btn-primary save-case-form">
                        Ok
                    </button>
                    <button type="button" class="btn btn-default save-case-form-close" data-dismiss="modal">
                        Cancel
                    </button>
                </div>
            </div>
        </div>
    </div>
    <input type="hidden" name="caseFormUrl" id="case-form-url" />
</div>