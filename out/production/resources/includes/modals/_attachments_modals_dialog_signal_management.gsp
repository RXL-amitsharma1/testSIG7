<!-- Modal -->
<script type="text/javascript">
    $(document).ready(function () {
        addCountBoxToInputField(4000,$('.attachmentDiscription'));
        fetchAcceptedFileFormats();
        $("#attachmentForm input[type=file]").bind("change", function () {
            var imgVal = $(this).val();
            var imgSize = $(this)[0].files[0].size;
            if (imgVal != '' && imgSize < 20000000) {
                $('#fileSizeMessage').hide()
                $("[name=_action_upload]").attr('disabled', false)
            } else if (imgVal != '') {
                $('#fileSizeMessage').show();
                $("[name=_action_upload]").attr('disabled', true);
            } else {
                $("[name=_action_upload]").attr('disabled', true);
            }
        })
    })
</script>
<div id="myModal" class="modal fade" role="dialog">
    <div class="modal-dialog">

        <!-- Modal content-->
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <label class="modal-title"><g:message code="app.label.attachments"/></label>
            </div>
            <g:form id="attachmentForm" name="attachmentForm"
                    method="post" enctype="multipart/form-data" autocomplete="off" >
                <div class="modal-body">
                    <div class="row">
                        <div><g:hiddenField name="source" value="${source}"/></div>
                        <input id="attachmentFormId" type="hidden" name="alertId"
                               value="${alertInst.id}"/>
                    </div>
                    <div class="row">
                        <div class="col-xs-12">
                            <input multiple class="multi attachment-file" type="file" name="attachments" value=""/>
                        </div>
                    </div>

                    <div class="row" id="fileSizeMessage" hidden="hidden">
                        <div class="col-xs-6">
                            <p style="color: red">File size can't exceed 20MB.</p>
                        </div>
                    </div>
                    <div class="row" style="padding-top: 30px;padding-bottom:10px">
                        <div class="col-xs-12">
                            <label><g:message code="app.label.description"/></label>
                            <input class="form-control attachmentDiscription" type="input" name="description" id="description" value="" style="width: 85%;float: right;"/>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <div class="text-right">

                            <g:actionSubmit class="btn btn-primary" action="upload"
                                            type="submit"
                                            value="${message(code: "default.upload.label")}" disabled="true"/>
                            <button data-dismiss="modal"
                                    class="btn btn-default">${message(code: "default.button.cancel.label")}</button>

                    </div>
                </div>
            </g:form>
        </div>
    </div>
</div>


