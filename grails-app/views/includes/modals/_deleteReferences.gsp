<div class="modal fade" id="deleteReferenceModal"  data-backdrop="static" tabindex="-1" role="dialog" aria-labelledby="deleteModalLabelEventGrp"
     aria-hidden="true" style="z-index: 9999;">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                <h4 class="modal-title" id="deleteModalLabelEventGrpAssessment">Delete Record</h4>
            </div>

            <div class="modal-body">
                <div class="alert alert-danger alert-dismissible forceLineWrap" role="alert" id="deleteDlgErrorDivEventGrpAssessment" style="display: none">
                    <button type="button" class="closeDeleteModalAssessment">
                        <span aria-hidden="true">&times;</span>
                        <span class="sr-only"><g:message code="default.button.close.label"/></span>
                    </button>
                </div>
                    <div id="nameToDelete"></div>
                <p></p>
            </div>

            <div class="modal-footer">
                <button id="deleteEventGrpButtonAssessment" data-dismiss="modal" type="button" class="btn btn-default  okReferenceModal">
                   Yes
                </button>
                <button type="button" data-dismiss="modal" class="btn btn-default closeReferenceModal">No</button>
            </div>
        </div>
    </div>
</div>


