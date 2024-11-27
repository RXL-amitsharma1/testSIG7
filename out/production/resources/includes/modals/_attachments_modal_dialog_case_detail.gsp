<!-- Modal -->
<script type="text/javascript">
    $(document).ready(function () {
        fetchAcceptedFileFormats();
    });
</script>
<div id="attachmentCaseDetailModal" class="modal fade" role="dialog">
    <div class="modal-dialog">

        <!-- Modal content-->
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <label class="modal-title"><g:message code="caseDetails.attachments"/></label>
            </div>

            <form id="attachmentForm" name="attachmentForm" method="post" enctype="multipart/form-data"
                  autocomplete="off" class="form">
                <div class="modal-body">
                    <div class="row">
                        <div><g:hiddenField name="source" value=""/></div>
                        <input id="attachmentFormId" type="hidden" name="alertId"
                               value="${alertId}"/>
                    </div>

                    <div class="row">
                        <div class="col-xs-12">
                            <input multiple type="file" class="attachment-file" name="attachments"/>
                        </div>
                    </div>

                    <div class="row" id="fileSizeMessage" hidden="hidden">
                        <div class="col-xs-6">
                            <p style="color: red">File size can't exceed 20MB.</p>
                        </div>
                    </div>

                    <div class="row" style="padding-top: 30px;">
                        <div class="col-xs-12">
                            <label><g:message code="caseDetails.description"/></label>
                            <input class="form-control" type="text" id="attachmentDescription" name="description"
                                   style="width: 85%;float: right;"/>
                        </div>
                    </div>
                </div>

                <div class="modal-footer">
                    <div>
                        <div class="text-right">
                            <g:actionSubmit class="btn btn-primary" action="upload"
                                            type="submit"
                                            value="${message(code: "default.upload.label")}" disabled="true"/>
                            <button data-dismiss="modal"
                                    class="btn btn-default">${message(code: "default.button.cancel.label")}</button>
                        </div>
                    </div>
                </div>
            </form>
        </div>
    </div>
</div>
