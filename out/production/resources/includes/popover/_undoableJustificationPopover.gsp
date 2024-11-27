<div id="undoableJustificationPopover" class="disposition popover justification dyn undo-popover">
    %{-- <button type="button" class="close modalCloseBTN" >&times;</button>--}%
    <div class="arrow"></div>
    <h3 class="popover-title mh-35">Undo Disposition Change</h3>

    <div class="popover-content">
                <div id="edit-boxDis" style="overflow-y:hidden">
                    <textarea class="form-control editedJustification"
                              maxlength="255" style="overflow-y:scroll"></textarea>
                    <ol class="confirm-options" id="revertConfirmOption">
                        <li><a tabindex="0" href="javascript:void(0);" title="Save"><i class="mdi mdi-checkbox-marked green-1" data-id="${id}" id="confirmUndoJustificationSignal"></i></a></li>
                        <li><a tabindex="0" href="javascript:void(0);" title="Close"><i class="mdi mdi-close-box red-1" id="cancelUndoJustificationSignal"></i></a>
                        </li>
                    </ol>
                </div>
%{--
                <a tabindex="0" href="javascript:void(0);" class="btn btn-primary addNewJustificationForDisassociation" id="addNewJustificationForDisassociation">Add New</a>
--}%
    </div>
</div>