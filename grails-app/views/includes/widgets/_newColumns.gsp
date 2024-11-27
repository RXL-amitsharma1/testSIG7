<g:each var="d" in="${labelConfigNew}" status="i">
    <g:if test="${d.display?.contains("/") && d.enabled && (d.name.contains("exe"))}">
        <th class="" data-field="${d.name}">
            <div class="th-label" data-field="${d.name}">
                <div class='stacked-cell-center-top'>
                    ${d.display.split("/")[0]}
                </div>

                <div class='stacked-cell-center-top'>
                    ${d.display.split("/")[1]}
                </div>
                <g:if test="${listDateRange[d.previousPeriodCounter]}">
                    <div class='stacked-cell-center-top'>${listDateRange[d.previousPeriodCounter]}</div>
                </g:if>
                <g:else>
                    <div class='stacked-cell-center-top'>Prev Period ${d.previousPeriodCounter + 1}</div>
                </g:else>
            </div>
        </th>
    </g:if>
</g:each>
