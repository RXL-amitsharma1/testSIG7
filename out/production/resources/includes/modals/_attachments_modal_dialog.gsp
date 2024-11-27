<script type="text/javascript">
    $(document).ready(function () {
        addCountBoxToInputField(8000, $('#description'));
        fetchAcceptedFileFormats();
    });
</script>
<!-- Modal -->
<div id="myModal" class="modal fade" role="dialog">
    <div class="modal-dialog">

        <!-- Modal content-->
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <label class="modal-title">Attachments</label>
            </div>
            <g:uploadForm id="attachmentForm" name="attachmentForm" url="[action: 'upload', controller: 'adHocAlert', id:alertInst.id]"
                method="post" enctype="multipart/form-data" autocomplete="off" >
            <div class="modal-body">
                    <div class="row">
                        <div><g:hiddenField name="source" value="${source}"/></div>
                    </div>
                    <div class="row">
                        <div class="col-xs-12">
                            <input multiple type="file" class="attachment-file" name="attachments"/>
                        </div>
                    </div>
                    <div class="row" style="padding-top: 30px;">
                        <div class="col-xs-12">
                            <label>Description</label>
                            <input class="form-control" type="input" name="description" id="description" style="width: 85%;float: right;"/>
                        </div>
                    </div>
            </div>
            <div class="modal-footer">
                <div>
                    <div class="text-right">
                        <button class="btn btn-primary" id="disableBtn" value="${message(code: "default.upload.label")}" form="attachmentForm" disabled="disabled">${message(code: "default.upload.label")}</button>
                        <button data-dismiss="modal" class="btn btn-default">${message(code: "default.button.cancel.label")}</button>
                    </div>
                </div>
            </div>
            </g:uploadForm>
        </div>
    </div>
</div>

