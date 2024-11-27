<div id="advancedFiltersPopover" class="popover disposition adfilter">
    <div class="arrow"></div>

    <h3 class="popover-title">Available Advanced Filters</h3>

    <div class="popover-content">
        <ul class="text-list">
            <g:each in="${availableAdvancedFilters}" var="advFilter">
                <li value="${advFilter.id}">
                    <a tabindex="0" href="javascript:void(0);" title="Edit" class="btn-edit editSelectedFilter" id="editSelectedFilter"><i class="mdi mdi-pencil editIcon"></i></a>
                    <a tabindex="0" href="javascript:void(0);" title="${advFilter.name}"
                       class="selectJustification" id="advanced-filter">${advFilter.name}</a>
                </li>
            </g:each>
            <li>
                <a tabindex="0" href="javascript:void(0);" title="Add New Filter" class="btn btn-primary" id="addNewAdvancedFilter">Add New</a>
            </li>
        </ul>
    </div>
</div>