<div id="dispositionSignalPopover" class="disposition popover signal">
    <div class="arrow"></div>
    <h3 class="popover-title">Associate Signal</h3>
    <div class="popover-content">
        <div id="signal-search-box" class="pos-ab">
            <input id="signal-search" type="text" placeholder="Search Signal..." class="form-control">
        </div>
        <ul id="signal-list" class="text-list">
            <!-- dynamic list items will go here -->
            <li id="new-signal-box-li">
                <div id="new-signal-box">
                    <g:if test="${forceJustification}">
                        <a tabindex="0" data-target="#dispositionJustificationPopover" id="newSignalJustification" class="selectSignal text" data-container="body"
                           role="button" data-toggle="modal-popover" data-placement="${caseDetail?'right':'left'}"></a>
                    </g:if>
                    <g:else>
                        <a tabindex="0" href="javascript:void(0);" id="newSignalJustification" class="selectSignal text"></a>
                    </g:else>
                    <input type="text" id="newSignalName" style="width: 82%" class="form-control" title="New Signal Name"/>
                    <ol class="confirm-options">
                        <li><a tabindex="0" href="javascript:void(0);" title="Save"><i class="mdi mdi-checkbox-marked green-1" id="createSignal"></i></a></li>
                        <li><a tabindex="0" href="javascript:void(0);" title="Close"><i class="mdi mdi-close-box red-1" id="cancelSignal"></i></a>
                        </li>
                    </ol>
                </div>
                <a tabindex="0" href="javascript:void(0);" title="Add new Signal" class="btn btn-primary" id="addNewSignal">Add New</a>
            </li>
        </ul>
        <div id="signal-list-loader" class="text-center" style="display:none;">
            <img src="/signal/assets/spinner.gif" alt="Loading..." style="max-width: 10%;margin-bottom: 20%; margin-top: auto">
        </div>
    </div>
</div>