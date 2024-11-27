<div id="disassociateJustificationPopover"
     class="disassociate popover justification dyn" style="float: right;left: 960px">
    <div class="arrow"></div>

    <h3 class="popover-title mh-35">Justification</h3>

    <div class="popover-content">
        <div>
            <textarea id="justification" class="form-control editedJustification"
                      title="Edit Justification" maxlength="255"></textarea>
            <ol class="confirm-options" id="revertConfirmOption">
                <li><a tabindex="0" href="#" title="Save"><i
                        class="mdi mdi-checkbox-marked green-1" signalId="${signalId}"
                        alertType=""
                        id="confirmDisassociate"></i></a></li>
                <li><a tabindex="0" href="#" title="Close"><i
                        class=" mdi mdi-close-box red-1"
                        id="cancelDisassociate"></i>
                </a>
                </li>
            </ol>
        </div>
    </div>
</div>