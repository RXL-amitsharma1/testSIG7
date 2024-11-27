<style>
.popover { pointer-events: none; }
</style>
<div class="modal fade productGroupModalDict" id="productGroupModalDictAssessment" data-backdrop="static"  aria-labelledby="productGroupDictLabelAssessment" aria-hidden="true" role="dialog" tabindex="-1">
    <div class="modal-dialog modal-lg product-group-modal-dialog" style="max-width: 800px;display:block" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close closeProductGroupModalAssessment" aria-label="Close">
                    <span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="productGroupDictLabelAssessment"><g:message code="app.productDictionary.create.product.group" /></h4>
            </div>
            <div class="modal-body container-fluid">
                <div class="msgContainer" style="display:none">
                    <div class="alert alert-danger" role="alert">
                        <span class="message"></span>
                    </div>
                </div>
                <div class="row">
                    <div class="col-md-5">
                        <div class="form-group">
                            <label><g:message code="app.productDictionary.product.group.name"/><span class="required-indicator">*</span></label>
                            <input type="text" name="productGroupName" placeholder="${g.message(code: 'input.name.placeholder')}"
                                   class="form-control" id="productGroupNameAssessment" value="" required/>
                        </div>
                        <div class="form-group">
                            <label><g:message code="app.productDictionary.product.group.share.with"/><span class="required-indicator">*</span></label>
                            <g:select id="shareWithProductGroupAssessment"
                                      name="shareWithProductGroupAssessment"
                                      from=""
                                      optionKey="userName"
                                      optionValue="fullNameAndUserName"
                                      value=""
                                      class="form-control" multiple="true"/>
                        </div>
                    </div>
                    <div class="col-md-7">
                        <div class="form-group">
                            <div class="form-group description-wrapper">
                                <label><g:message code="app.productDictionary.product.group.description"/>
                                </label>
                                <g:textArea rows="5" cols="3" id="descriptionProductGroupAssessment" name="descriptionProductGroupAssessment" maxlength="4000" style="height: 110px;" value="" class="form-control "/>
                            </div>
                        </div>
                    </div>

                </div>
                <g:hiddenField name="productGroupId" id="productGroupIdAssessment" value=""/>
                <g:hiddenField name="productGroupIsMultiIngredient" id="productGroupIsMultiIngredientAssessment" value=""/>
            </div>

            <div class="modal-footer">
                <div class="modal-footer">
                    <button type="button" id="deleteProductGroupAssessment" class="btn btn-primary">
                        <g:message code="default.button.delete.label"/>
                    </button>
                    <button type="button" id="saveProductGroupAssessment" class="btn btn-primary">
                        <g:message code="default.button.save.label"/>
                    </button>
                    <button type="button" class="btn btn-default closeProductGroupModalAssessment">
                        <g:message code="default.button.close.label"/>
                    </button>
                </div>
            </div>
        </div>
    </div>
</div>
<g:render template="/includes/widgets/deleteRecordAssessment"/>