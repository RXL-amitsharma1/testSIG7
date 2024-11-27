<div class="modal fade" id="productModalMart" tabindex="-1" role="dialog" aria-labelledby="productDictionaryLabel"
     aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="productDictionaryLabel"><g:message
                        code="app.reportField.productDictionary"/></h4>
            </div>

            <div class="modal-body">
                <div class="messageContainerForDialog"></div>
                <g:render template="/includes/widgets/productDictionaryTemplateMart"/>
            </div>

            <div class="modal-footer">
                %{--<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>--}%
                <button type="button" class="btn btn-default clearProductValuesMart"><g:message
                        code="default.button.clear.label"/></button>
                <button type="button" class="btn btn-primary addProductValuesMart"><g:message
                        code="default.button.add.label"/></button>
                <button type="button" class="btn btn-default addAllProductsMart" data-dismiss="modal"><g:message
                        code="default.button.close.label"/></button>
            </div>
        </div>
    </div>
</div>