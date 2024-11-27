
<!-- Modal -->
<div id="myMeetingModal" class="modal fade" role="dialog">
    <div class="modal-dialog">

        <!-- Modal content-->
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <label class="modal-title">Attachmentdddds</label>
            </div>
            <div class="modal-body">

                <button type="button" id="upload-attachment-button" class="btn btn-primary">${message(code: "default.upload.label")}</button>
                    <div class="row">
                        <div><g:hiddenField name="source" value="${source}"/></div>
                    </div>
                    <div class="row">
                        <div class="col-xs-12">
                            <input multiple class="multi" type="file" name="attachments" id="attachments"/>
                        </div>
                    </div>

            </div>
            <div class="modal-footer">
                <div style="margin-top:15px;">
                    <div style="text-align: right">
                        <button data-dismiss="modal" class="btn btn-default">${message(code: "default.button.cancel.label")}</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

