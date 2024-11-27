<div id="priorityPopover" class="popover priority-icons">
    <div class="arrow"></div>
    <div class="popover-content priority-icon-list">
        <g:each in="${availablePriorities?.sort{it[2]}}">
            <g:if test="${forceJustification}">
                <a tabindex="0" data-target="#priorityJustificationPopover" data-id="${it[0]}" role="button" class="changeToPriority font-24" data-toggle="modal-popover"
                   data-placement="bottom" data-priority="${it[1]}" title="${it[1]}" data-days="${it[4]}">
                    <i class="${it[3]}"></i>
                </a>
            </g:if>
            <g:else>
                <a tabindex="0" href="javascript:void(0);" data-id="${it[0]}" class="changeToPriority font-24" data-priority="${it[1]}" title="${it[1]}" data-days="${it[4]}">
                    <i class="${it[3]}"></i>
                </a>
            </g:else>

        </g:each>
    </div>
</div>
