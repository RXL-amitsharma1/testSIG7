<div class="modal fade" id="requestByModal" tabindex="-1" role="dialog" aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content" style="margin-left: 0;margin-right: 0">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">×</span></button>
                <h4 class="modal-title " style="font-weight: bold"> Requested By :
                    <span id="comment-meta-info"></span>
                </h4>
            </button>
            </div>
            <div class="modal-body" style="max-height: 600px; overflow-y: scroll;">
                <div id="alert-comment-container" class="list">
                    <g:textArea maxlength="4000" name="requestByBox" id="requestByBox"
                                class="form-control height-150"></g:textArea>
                </div>

                <span class="createdBy"></span>
                <button type="button" class="btn btn-primary add-requested-by pull-right m-t-10">
                    <g:message code="default.button.add.label"/>
                </button>

            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-default alert-comment-modal-close" data-dismiss="modal">
                    <g:message code="default.button.close.label"/>
                </button>
            </div>
        </div>

    </div>
</div>