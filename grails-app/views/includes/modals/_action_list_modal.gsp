<div class="modal fade" data-backdrop="static" id="action-modal" tabindex="-1" role="dialog" aria-labelledby="eventDictionaryLabel"
     aria-hidden="true">
    <div class="modal-dialog" style="width: 70%;">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <label class="modal-title" id="eventDictionaryLabel">
                    <g:message code="app.label.action.list" default="Action List"/></label>
            </div>

            <div class="modal-body">
                <g:render template="/includes/widgets/action_list_panel"/>
            </div>
        </div>
    </div>
</div>

<g:render template="/includes/modals/actionCreateModal" model="[alertId: alertId, isArchived: false]" />
<g:render template="/includes/modals/action_edit_modal" />
