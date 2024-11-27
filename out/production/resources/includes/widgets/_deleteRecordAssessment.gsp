<div class="modal fade" id="deleteModalAssessment"  data-backdrop="static" role="dialog" aria-labelledby="deleteModalLabelProdGrp"
     aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close closeDeleteModal" aria-hidden="true">&times;</button>
                <h4 class="modal-title" id="deleteModalLabelProdGrp"><g:message code="app.productDictionary.delete.product.group" /></h4>
            </div>

            <div class="modal-body">
                <div class="alert alert-danger alert-dismissible forceLineWrap" role="alert" id="deleteDlgErrorDivAssessment" style="display: none">
                    <button type="button" class="closeDeleteModalAssessment">
                        <span aria-hidden="true">&times;</span>
                        <span class="sr-only"><g:message code="default.button.close.label"/></span>
                    </button>
                </div>
                <div id="nameToDelete"></div>
                <p></p>
            </div>

            <div class="modal-footer">
                <button id="deleteProdGrpButtonAssessment" type="button" class="btn btn-danger">
                    <span class="glyphicon glyphicon-trash icon-white"></span>
                    ${message(code: 'default.button.deleteProdGrp.label', default: 'Delete')}
                </button>
                <button type="button" class="btn btn-default closeDeleteModalAssessment"><g:message code="default.button.cancel.label"/></button>
            </div>
        </div>
    </div>
</div>


