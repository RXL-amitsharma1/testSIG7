<%@ page import="com.rxlogix.user.User" %>
<asset:javascript src="app/pvs/group/addRemoveUser.js"/>
<div class="modal fade" id="addRemoveUserModal" data-backdrop="static" tabindex="-1" role="dialog" aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header dropdown">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title"><g:message code="userGroup.add.remove.users.label"/></h4>
            </div>

            <div class="modal-body action-item-modal-body">
                <g:select id="allowedUsers" name="allowedUsers"
                          from="${allUserList}"
                          optionKey="id" optionValue="fullName"
                          multiple="true"
                          value="${allowedUsers*.id}">
                </g:select>
            </div>

            <div class="modal-footer">
                <div class="buttons creationButtons">
                    <input id="addRemoveUserButton" type="button" class="btn btn-primary" value="Select"
                           onclick="changeUser();">
                    <button type="button" data-dismiss="modal" class="btn pv-btn-grey" aria-label="Close"><g:message
                            code="app.button.close"/></button>
                </div>
            </div>
        </div>
    </div>
</div>

<style type="text/css">
.pickList {
    margin-left: 155px;
}
.modal-title {
    margin: 0;
    line-height: 1.42857143;
}
.modal .modal-dialog .modal-content .modal-body {
    /* padding: 20px !important; */
    max-height: calc(100vh - 150px);
    overflow-y: auto;
    min-height: 100px;
}
.pickList_listItem {
    text-align: center;
}
.ui-state-highlight, .ui-widget-content .ui-state-highlight, .ui-widget-header .ui-state-highlight {
    border: 1px solid #dad55e;
    background: #fffa90;
    color: #777620;
}
.pickList_selectedListItem {
    background-color: #a3c8f5;
}
.pickList_list {
    width: 250px;
    height: 280px;
    list-style-type: none;
    margin: 0;
    padding: 0;
    float: left;
    width: 150px;
    height: 75px;
    border: 1px inset #eee;
    overflow-y: auto;
    cursor: default;
}
.pickList_listLabel {
    text-align: left;
    font-size: 1em;
    font-weight: bold;
}

</style>
