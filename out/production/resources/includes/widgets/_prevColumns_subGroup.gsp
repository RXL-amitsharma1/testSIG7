<g:each in="${subGroupsColumnList}" var="category">
    <g:if test="${labelConfigCopy."exe${j}ebgm${category}"}">
    <th class="" data-field="exe${j}ebgm${category}">
        <div class="th-label" data-field="exe${j}ebgm${category}">
            <div class='stacked-cell-center-top'>
                ${labelConfig."exe${j}ebgm${category}"}
            </div>

            <g:if test="${listDateRange[j]}">
                <div class='stacked-cell-center-top'>${listDateRange[j]}</div>
            </g:if>
            <g:else>
                <div class='stacked-cell-center-top'>Prev Period ${j+1}</div>
            </g:else>
        </div>
    </th>
    </g:if>
    <g:if test="${labelConfigCopy."exe${j}eb05${category}"}">
    <th class="" data-field="exe${j}eb05${category}">
        <div class="th-label" data-field="exe${j}eb05${category}">
            <div class='stacked-cell-center-top'>
                ${labelConfig."exe${j}eb05${category}"}
            </div>
            <g:if test="${listDateRange[j]}">
                <div class='stacked-cell-center-top'>${listDateRange[j]}</div>
            </g:if>
            <g:else>
                <div class='stacked-cell-center-top'>Prev Period ${j+1}</div>
            </g:else>
        </div>
    </th>
    </g:if>
    <g:if test="${labelConfigCopy."exe${j}eb95${category}"}">
        <th data-field="exe${j}eb95${category}">
        <div class="th-label" data-field="exe${j}eb95${category}">
            <div class='stacked-cell-center-top'>
                ${labelConfig."exe${j}eb95${category}"}
            </div>

            <g:if test="${listDateRange[j]}">
                <div class='stacked-cell-center-top'>${listDateRange[j]}</div>
            </g:if>
            <g:else>
                <div class='stacked-cell-center-top'>Prev Period ${j+1}</div>
            </g:else>
        </div>
    </th>
        </g:if>
</g:each>

<g:each in="${prrRorSubGroupMap}" var="index, value">
    <g:if test="${labelConfigCopy."exe${j}${value}"}">
        <th data-field="exe${j}${value}">
            <div class="th-label" data-field="exe${j}${value}">
                <div class='stacked-cell-center-top'>
                    Prev ${labelConfig."${value}"}
                </div>
                <g:if test="${listDateRange[j]}">
                    <div class='stacked-cell-center-top'>${listDateRange[j]}</div>
                </g:if>
                <g:else>
                    <div class='stacked-cell-center-top'>Prev Period ${j + 1}</div>
                </g:else>
            </div>
        </th>
    </g:if>
</g:each>
<g:each in="${relativeSubGroupMap}" var="index, value">
    <g:if test="${labelConfigCopy."exe${j}${value}"}">
        <th data-field="exe${j}${value}">
            <div class="th-label" data-field="exe${j}${value}">
                <div class='stacked-cell-center-top'>
                    Prev ${labelConfig."${value}"}
                </div>
                <g:if test="${listDateRange[j]}">
                    <div class='stacked-cell-center-top'>${listDateRange[j]}</div>
                </g:if>
                <g:else>
                    <div class='stacked-cell-center-top'>Prev Period ${j + 1}</div>
                </g:else>
            </div>
        </th>
    </g:if>
</g:each>
<g:if test="${isFaersEnabled}">
    <g:each in="${faersSubGroupsColumnList}" var="category">
        <g:if test="${labelConfigCopy."exe${j}ebgm${category}Faers"}">
        <th data-field="exe${j}ebgm${category}Faers">
            <div class="th-label" data-field="exe${j}ebgm${category}Faers">
                <div class='stacked-cell-center-top'>
                    ${labelConfig."exe${j}ebgm${category}Faers"}
                </div>

                <g:if test="${listDateRange[j]}">
                    <div class='stacked-cell-center-top'>${listDateRange[j]}</div>
                </g:if>
                <g:else>
                    <div class='stacked-cell-center-top'>Prev Period ${j+1}</div>
                </g:else>
            </div>
        </th>
        </g:if>
        <g:if test="${labelConfigCopy."exe${j}eb05${category}Faers"}">
        <th data-field="exe${j}eb05${category}Faers">
            <div class="th-label" data-field="exe${j}eb05${category}Faers">
                <div class='stacked-cell-center-top'>
                    ${labelConfig."exe${j}eb05${category}Faers"}
                </div>

                <g:if test="${prevFaersDate[j]}">
                    <div class='stacked-cell-center-top'>${prevFaersDate[j]}</div>
                </g:if>
                <g:else>
                    <div class='stacked-cell-center-top'>Prev Period ${j+1}</div>
                </g:else>
            </div>
        </th>
        </g:if>
        <g:if test="${labelConfigCopy."exe${j}eb95${category}Faers"}">
        <th data-field="exe${j}eb95${category}Faers">
            <div class="th-label" data-field="exe${j}eb95${category}Faers">
                <div class='stacked-cell-center-top'>
                    ${labelConfig."exe${j}eb95${category}Faers"}
                </div>

                <g:if test="${prevFaersDate[j]}">
                    <div class='stacked-cell-center-top'>${prevFaersDate[j]}</div>
                </g:if>
                <g:else>
                    <div class='stacked-cell-center-top'>Prev Period ${j+1}</div>
                </g:else>
            </div>
        </th>
        </g:if>
    </g:each>
</g:if>