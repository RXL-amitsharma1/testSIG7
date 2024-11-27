<div class="modal fade" data-backdrop="static" id="showAttachmentModal" tabindex="-1" role="dialog" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-width-lg">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span></button>
                <label class="modal-title">Attachments</label>
            </div>

            <div class="modal-body">
                <form id="attachmentForm" name="attachmentForm" method="post" enctype="multipart/form-data" autocomplete="off" class="form ${buttonClass}">
                    <div class="">
                        <div><g:hiddenField name="source" value=""/></div>
                        <input id="attachmentFormId" type="hidden" name="alertId" value=""/>
                    </div>
                    <div class="row">
                        <div class="col-sm-4 col-md-4">
                            <div class="form-group">
                                <label>Select file</label>
                                <input multiple class="multi clearFields attachment-file" type="file" name="attachments" style="width:100%"/>
                            </div>
                        </div>
                        <div class="col-sm-6 col-md-6">
                            <div class="form-group">
                                <label>Description</label>
                                <input class="form-control clearFields attachment-input" type="input" name="description" />
                            </div>
                        </div>
                        <div class="col-sm-2 col-md-2">
                            <div class="form-group m-t-20 pull-right">
                                <button class="btn btn-primary upload" disabled="true" type="submit">${message(code: "default.upload.label")}</button>
                                <button data-dismiss="modal" class="btn btn-default">${message(code: "default.button.cancel.label")}</button>
                            </div>
                        </div>
                    </div>
                </form>

                <div class="attachments">
                    <table id="attachment-table" class="row-border hover  no-footer" width="100%">
                        <thead>
                        <tr>
                            <th>${message(code: "app.file.name")}</th>
                            <th>${message(code: "app.label.description")}</th>
                            <th>${message(code: "app.label.timestamp")}</th>
                            <th>${message(code: "app.label.performed.by")}</th>
                            <th>${message(code: "app.label.action")}</th>
                        </tr>
                        </thead>
                        <tbody>

                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>
<script>
    $("#showAttachmentModal").on("hidden.bs.modal", function(){
        $(".clearFields").val("");
    });

    $(document).ready(function () {
        addCountBoxToInputField(8000, $('.attachment-input'));

    });
</script>
