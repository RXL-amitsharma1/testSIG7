<div id="bulkDispositionPopover" class="popover priority-icons">
    <div class="arrow ico-circle-arrow-hide" ></div>
    <div class="popover-content priority-icon-list">
        <g:each in="${alertDispositionList}">
            <g:if test="${it.validatedConfirmed}">
                <a tabindex="0" data-target="#dispositionSignalPopover" role="button" class="changeDisposition tooltipDisabled font-24 disable"
                   data-validated-confirmed="${it.validatedConfirmed}" data-bulk-disp-update="true"
                   data-disposition-id="${it.id}" data-toggle="modal-popover" data-trigger="focus"
                   data-placement="bottom" title="${it.displayName}">
                    <i class="ico-circle" style="background:${it.colorCode}">${it.abbreviation}</i>
                </a>
            </g:if>
            <g:elseif test="${forceJustification}">
                <a tabindex="0" data-target="#dispositionJustificationPopover" role="button"
                   class="changeDisposition tooltipDisabled font-24 disable" data-bulk-disp-update="true"
                   data-validated-confirmed="${it.validatedConfirmed}"
                   data-disposition-id="${it.id}" data-toggle="modal-popover" data-trigger="focus"
                   data-placement="bottom" title="${it.displayName}">
                    <i class="ico-circle" style="background:${it.colorCode}">${it.abbreviation}</i>
                </a>
            </g:elseif>
            <g:else>
                <a tabindex="0" href="javascript:void(0);" role="button" class="changeDisposition tooltipDisabled font-24 disable"
                   data-validated-confirmed="${it.validatedConfirmed}" data-bulk-disp-update="true"
                   data-disposition-id="${it.id}" data-toggle="modal-popover" data-trigger="focus"
                   data-placement="bottom" title="${it.displayName}">
                    <i class="ico-circle" style="background:${it.colorCode}">${it.abbreviation}</i>
                </a>
            </g:else>
        </g:each>
    </div>
</div>