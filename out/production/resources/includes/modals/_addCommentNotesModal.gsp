<div class="modal fade" data-backdrop="static" id="commentsModal" tabindex="-1" role="dialog" aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content" style="margin-left: 50px;margin-right: 50px">

            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span></button>
                <label class="modal-title">Comment</label>
            </button>
            </div>

            <div class="modal-body">
                <div class="form-group"><textarea rows="8" style="width: 100%;" id="commentNotes"
                          placeholder="Add your comments here..."></textarea></div>
                <span class="createdBy"></span>
            </div>
            <div class="modal-footer">
                <div class="bulkOptionsSection pull-left m-l-5" style="/*display: none;*/">
                    <label class="labelBold m-r-15" for="current">
                        <input class="m-r-5" type="radio" name="bulkOptions" id="current" value="current" checked>
                        Current Observation
                    </label>
                    <label class="labelBold" for="allSelected">
                        <input class="m-r-5" type="radio" name="bulkOptions" id="allSelected" value="allSelected">
                        Selected Observations (<span class="count"></span>)
                    </label>
                </div>
                <button type="button"class="btn btn-primary addAdhocComment ${buttonClass}">
                    <g:message code="default.button.add.label"/>
                </button>

                <button type="button" class="btn btn-default previous-followUp" data-dismiss="modal">
                    <g:message code="default.button.close.label"/>
                </button>
            </div>
        </div>
    </div>
</div>

<script>
    $("#commentsModal").on('show.bs.modal', function () {
        $('div.bulkOptionsSection input#current').click();
    });
</script>