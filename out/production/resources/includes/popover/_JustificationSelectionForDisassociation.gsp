<div id="disassociationJustificationPopover" class="disposition popover justification dyn">
   %{-- <button type="button" class="close modalCloseBTN" >&times;</button>--}%
    <div class="arrow"></div>
    <h3 class="popover-title mh-35"></h3>

    <div class="popover-content">
        <ul class="text-list">
            <g:each in="${dispositionJustifications}" var="justificationVal">
                <li>

                    <a tabindex="0" href="javascript:void(0);" title="${justificationVal.justificationText}"
                       class="selectJustificationNew text">${justificationVal.justificationText}</a>
                    <a tabindex="0" href="javascript:void(0);" title="Edit" class="btn-edit"><i class="mdi mdi-pencil editIconDis"></i></a>
                </li>
            </g:each>
            <li>
                <div id="edit-boxDis">
                    <textarea class="form-control editedJustificationForDisassociation"
                               maxlength="255"></textarea>
                    <ol class="confirm-options">
                        <li><a tabindex="0" href="javascript:void(0);" title="Save"><i class="mdi mdi-checkbox-marked green-1" id="confirmJustificationForDisassociation"></i></a></li>
                        <li><a tabindex="0" href="javascript:void(0);" title="Close"><i class="mdi mdi-close-box red-1" id="cancelJustificationForDisassociation"></i></a>
                        </li>
                    </ol>
                </div>
                <a tabindex="0" href="javascript:void(0);" class="btn btn-primary addNewJustificationForDisassociation" id="addNewJustificationForDisassociation">Add New</a>
            </li>
        </ul>
    </div>
</div>