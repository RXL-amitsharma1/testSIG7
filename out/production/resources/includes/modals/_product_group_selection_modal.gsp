<%@ page import="com.rxlogix.config.ProductGroup" %>

<script>
    $(document).ready(function () {
        $("#productGroupModal .addProductValues").click(function () {
            var showProductGroupSelection = $("#showProductGroupSelection");
            var text = [];
            $.each($("#productGroups").select2("data"), function () {
                text.push(this.text);
            });
            showProductGroupSelection.html('<div style="padding: 5px">' + text.join(", ") + '</div>');
            var productGroupModal = $("#productGroupModal");
            productGroupModal.modal('hide');
        });

        $("#productGroupModal .clearProductValues").click(function () {
            $("#productGroups").val("").trigger("change");
        });
        $('#productGroups').select2({
            placeholder: "Select Product Groups"
        });
    });
</script>

<div class="modal fade" id="productGroupModal" tabindex="-1" role="dialog" aria-labelledby="productDictionaryLabel"
     aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title" id="productDictionaryLabel">
                    Product Group Dictionary
                    <span id="fetchingProducts" style="font-size:20px; display:none;"
                          class="fa fa-spinner fa-spin"></span>
                </h4>

            </div>

            <div class="modal-body">
                <div class="row">
                    <div class="col-md-12">
                        <g:select name="productGroups" id="productGroups"
                                  from="${productGroupList}"
                                  optionKey="id" multiple="multiple"
                                  optionValue="groupName"
                                  class="form-control"/>
                    </div>
                </div>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-default clearProductValues">
                    <g:message code="default.button.clear.label"/>
                </button>
                <button type="button" class="btn btn-primary addProductValues">
                    <g:message code="default.button.add.label"/>
                </button>
                <button type="button" class="btn btn-default addProductValues" data-dismiss="modal">
                    <g:message code="default.button.close.label"/>
                </button>
            </div>
        </div>
    </div>
</div>