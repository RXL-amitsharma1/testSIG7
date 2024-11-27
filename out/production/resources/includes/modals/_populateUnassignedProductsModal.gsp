<%@ page import="com.rxlogix.util.ViewHelper" %>
<div class="modal fade" id="populateUnassignedProductsModal">
    <div class="modal-dialog modal-md">
        <div class="modal-content">

            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span></button>
                <label class="modal-title">Populate Unassigned Products</label>
            </div>

            <form action="populateUnassignedProducts" id="populateUnassignedProductsForm" method="post">

                <div class="modal-body">
                <g:select name="hierarchy" class="form-control"
                          from="${['Product Group'] + com.rxlogix.util.ViewHelper.getHiearchyValues()}" noSelection="['': 'Select Product Hierarchy']"/>
            </div>

                <div class="modal-footer">
                    <input type="submit" class="btn primaryButton btn-primary" value="Ok"
                           id="submit-button">
                    <button type="button" class="btn btn-default" data-dismiss="modal">
                        Cancel
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>