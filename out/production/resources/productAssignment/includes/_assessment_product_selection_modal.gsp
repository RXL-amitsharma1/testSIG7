<%@ page import="com.rxlogix.pvdictionary.config.PVDictionaryConfig" %>
<div class="modal fade" id="productModalAssessment" data-backdrop="static" tabindex="-1" role="dialog"
     aria-labelledby="productDictionaryLabel"
     aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="productDictionaryLabel"><g:message
                        code="app.reportField.productDictionary.label"/></h4>
            </div>

            <div class="modal-body">
                <g:render template="/validatedSignal/includes/productDictionaryTemplateAssessment"
                          model="[column_names: PVDictionaryConfig.ProductConfig.columns, views: PVDictionaryConfig.ProductConfig.views.sort{it.index}, filters: PVDictionaryConfig.ProductConfig.filters.sort{it.index}, productColumnCode: PVDictionaryConfig.ProductColumnCode]"/>
            </div>

            <div class="modal-footer">
                <div class="loading" style="display:none"><asset:image src="select2-spinner.gif" height="16"
                                                                       width="16"/></div>
                <button type="button" class="btn btn-default clearProductValuesAssessment" accesskey="$"><g:message
                        code="default.button.clear.label"/></button>
                <button type="button" class="btn btn-primary addProductValuesAssessment" accesskey="!"><g:message
                        code="default.button.add.label"/></button>
                <button type="button" class="btn btn-primary addAllProductValuesAssessment" accesskey="@"><g:message
                        code="app.label.add.all"/></button>
                <button type="button" class="btn btn-primary createProductGroupAssessment" disabled accesskey="*"><g:message
                        code="app.button.product.group.create.label"/></button>
                <button type="button" class="btn btn-primary updateProductGroupAssessment" disabled accesskey="&"><g:message
                        code="app.button.product.group.update.label"/></button>
                <button type="button" class="btn btn-default addAllProductsAssessment" data-dismiss="modal" accesskey="#"><g:message
                        code="default.button.close.label"/></button>
            </div>
        </div>
    </div>
</div>
<g:render template="/includes/widgets/productGroupModalAssessment"/>
