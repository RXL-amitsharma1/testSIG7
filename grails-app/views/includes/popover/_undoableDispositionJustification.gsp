<div id="undoDispositionJustificationPopover"
     class="disposition popover justification dyn">%{--add class 'level2' when used under signal--}%
    <div class="arrow"></div>

    <h3 class="popover-title mh-35">Justification</h3>

    <div class="popover-content">
        <div id="edit-box" >
            <textarea class="form-control editedJustification"
                      title="Edit Justification" maxlength="255"></textarea>
            <ol class="confirm-options">
                <li><a tabindex="0" href="javascript:void(0);" title="Save"><i
                        class="mdi mdi-checkbox-marked green-1" data-id="${id}" id="confirmUndoJustificationSignal"></i></a></li>
                <li><a tabindex="0" href="javascript:void(0);" title="Close"><i class="mdi mdi-close-box red-1"
                                                                                id="cancelUndoJustificationSignal"></i>
                </a>
                </li>
            </ol>
        </div>
    </div>
</div>