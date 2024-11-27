<g:javascript>
    sharedWithListUrl = "${createLink(controller: 'user', action: 'searchUserGroupList')}";

    $(document).ready(function () {
        var allowClear = true;
        if(typeof hasReviewerAccess !== "undefined" && !hasReviewerAccess){
            allowClear = false
        }
        bindShareWith($('.action .assignedToValue'), sharedWithListUrl, '', null,true, false, allowClear);
        bindShareWith($('.treeview-content .assignedToValue'), sharedWithListUrl, '', null,false, false, allowClear);

    });
</g:javascript>

<div class="${isAction == true ? 'col-lg-4': 'col-lg-3'}">
    <div class="clearfix"></div>
    <label>
        <g:message code="app.label.action.item.assigned.to" default="Assigned To" />
        <span class="required-indicator">*</span>
    </label>
    <select id="assignedTo" name="assignedToValue" class="form-control select2 assignedToValue"></select>
</div>
