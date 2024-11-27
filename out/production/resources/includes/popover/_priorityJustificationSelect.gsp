<div id="priorityJustificationPopover" class="disposition popover priority">
    <div class="arrow"></div>

    <h3 class="popover-title mh-35"></h3>

    <div class="popover-content">
        <ul class="text-list">
            <g:each in="${availableAlertPriorityJustifications}" var="justification">
                <li>
                    <a  tabindex="0" href="javascript:void(0);" title="${justification.justificationText.replace("<br>","\n")}"
                       class="selectJustification text" style="white-space: pre-wrap;">${raw(justification.justificationText)}</a>
                    <a tabindex="0" href="javascript:void(0);" title="Edit" class="btn-edit"><i class="mdi mdi-pencil editIcon"></i></a>
                </li>
            </g:each>
            <li>
                <div id="edit-box">
                    <textarea class="form-control editedJustification"
                              title="Edit Justification" maxlength="255"></textarea>
                    <ol class="confirm-options">
                        <li><a tabindex="0" href="javascript:void(0);" title="Save"><i class="mdi mdi-checkbox-marked green-1" id="confirmJustification"></i></a></li>
                        <li><a tabindex="0" href="javascript:void(0);" title="Close"><i class="mdi mdi-close-box red-1" id="cancelJustification"></i></a>
                        </li>
                    </ol>
                </div>
                <a tabindex="0" href="javascript:void(0);" title="Add new Justification" class="btn btn-primary" id="addNewJustification">Add New</a>
            </li>
        </ul>
    </div>
</div>