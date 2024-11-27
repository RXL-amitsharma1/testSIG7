<div id="dispositionSignalPopover" class="disposition popover signal">
    <div class="arrow"></div>
    <h3 class="popover-title">Associate Signal</h3>
    <div class="popover-content">
        <div id="signal-search-box" class="pos-ab">
            <input id="signal-search" type="text" placeholder="Search Signal..." class="form-control">
        </div>
        <ul id="signal-list" class="text-list">
            <g:each in="${availableSignals}" var="signal">
                <g:set var="displayName" value="${signal.name.concat(" ").concat(signal.products).concat(" ").concat(signal.detectedDate.split(" ")[0]).concat(" ").concat(signal.disposition)}"></g:set>
                <g:if test="${signal.isClosed==true}">
                <li  signalName="${signal.name.encodeAsHTML()} ${signal.products.encodeAsHTML()}"  >
                    <g:if test="${forceJustification}">
                        <a tabindex="0" data-target="#dispositionJustificationPopover" data-html="true" role="button" class="selectSignal text grid-menu-tooltip" data-container="body"
                           data-toggle="modal-popover" isClosed="${signal.isClosed}" sid="${signal.id}" data-placement="${caseDetail?'right':'left'}" data-title="Closed Signal<br>Detected Date: ${signal.detectedDate.encodeAsHTML()}<br>Current Disposition: ${signal.disposition.encodeAsHTML()}" signalName="${signal.name.encodeAsHTML()}" data-signal-id="${signal.id}">
                            <p class="" > <span class='comments-space'>${displayName}</span> </p>
                        </a>
                    </g:if>
                    <g:else>
                        <a tabindex="0" href="javascript:void(0);" data-html="true" data-title="Closed Signal<br>Detected Date: ${signal.detectedDate.encodeAsHTML()}<br>Current Disposition: ${signal.disposition.encodeAsHTML()}" sid="${signal.id}" signalName="${signal.name.encodeAsHTML()}" data-signal-id="${signal.id}" class="selectSignal text grid-menu-tooltip"
                           class="attach-signal">
                            <p class=""  > <span class='comments-space'>${displayName}</span> </p>
                        </a>
                    </g:else>
                </li>
                </g:if>
                <g:else>
                    <li signalName="${signal.name.encodeAsHTML()} ${signal.products.encodeAsHTML()}">
                        <g:if test="${forceJustification&&!caseDetail}">
                            <a tabindex="0" data-target="#dispositionJustificationPopover" role="button" class="selectSignal text grid-menu-tooltip" data-container="body"
                               data-toggle="modal-popover" sid="${signal.id}" data-placement="left"  data-html="true" data-title="Detected Date: ${signal.detectedDate.encodeAsHTML()}<br>Current Disposition: ${signal.disposition.encodeAsHTML()}" signalName="${signal.name.encodeAsHTML()}" data-signal-id="${signal.id}">
                                <p> <span class='comments-space'>${displayName}</span> </p>
                            </a>
                        </g:if>
                        <g:elseif test="${caseDetail}">
                            <a tabindex="0" data-target="#dispositionJustificationPopover" role="button" class="selectSignal text grid-menu-tooltip" data-container="body"
                               data-toggle="modal-popover" sid="${signal.id}" data-placement="right"  data-html="true" data-title="Detected Date: ${signal.detectedDate.encodeAsHTML()}<br>Current Disposition: ${signal.disposition.encodeAsHTML()}" signalName="${signal.name.encodeAsHTML()}" data-signal-id="${signal.id}">
                                <p> <span class='comments-space'>${displayName}</span> </p>
                            </a>
                        </g:elseif>
                    <g:elseif test="${!forceJustification}">
                        <a tabindex="0"  sid="${signal.id}" href="javascript:void(0);" data-html="true" data-title="Detected Date: ${signal.detectedDate.encodeAsHTML()}<br>Current Disposition: ${signal.disposition.encodeAsHTML()}" signalName="${signal.name.encodeAsHTML()}" data-signal-id="${signal.id}" class="selectSignal text grid-menu-tooltip"
                           class="attach-signal">
                            <p> <span class='comments-space'>${displayName}</span> </p>
                        </a>
                    </g:elseif>
                        <g:else>
                            <a tabindex="0"  sid="${signal.id}" href="javascript:void(0);"  data-title="Detected Date: ${signal.detectedDate.encodeAsHTML()},Current Disposition: ${signal.disposition.encodeAsHTML()}"  title="${signal.name.encodeAsHTML()}" signalName="${signal.name.encodeAsHTML()}" data-signal-id="${signal.id}" class="selectSignal text"
                               class="attach-signal">
                                <p> <span class='comments-space'>${displayName}</span> </p>
                            </a>
                        </g:else>
                    </li>
                </g:else>
            </g:each>
            <li>
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
    </div>
</div>