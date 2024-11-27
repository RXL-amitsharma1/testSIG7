<div class="modal fade" data-backdrop="static" id="createAdvancedFilterModal" role="dialog" aria-hidden="true" tabindex="-1">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span></button>
                <label class="modal-title">Add New Filter</label>
            </div>

            <div class="modal-body">
                <div class="msgContainer" style="display:none">
                    <div class="alert alert-danger" role="alert">
                        <span class="message"></span>
                    </div>
                </div>
                <div class="date-info-advance-filter-modal" style="display:none">
                    <div class="alert alert-success" role="alert">
                    <button type="button" class="close" data-dismiss="alert">
                        <span aria-hidden="true">&times;</span>
                        <span class="sr-only"><g:message code="default.button.close.label" /></span>
                    </button>
                        <span class="message"></span>
                    </div>
                </div>
                <g:render template="/advancedFilters/includes/form" model="[isShareFilterViewAllowed: isShareFilterViewAllowed]"/>

            </div>
        </div>
    </div>
</div>