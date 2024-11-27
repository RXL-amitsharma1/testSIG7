<g:if test="${isVigibaseEnabled}">
    <g:if test="${labelConfigCopy."exe${j}newSeriousCountVigibase"}">
    <th class="" data-field="exe${j}newSeriousCountVigibase">
        <div class="th-label" data-field="exe${j}newSeriousCountVigibase">
            <div class='stacked-cell-center-top'>
                ${labelConfig."exe${j}newSeriousCountVigibase".split("/")[0]}
            </div>

            <div class='stacked-cell-center-top'>
                ${labelConfig."exe${j}newSeriousCountVigibase".split("/")[1]}
            </div>
            <g:if test="${listDateRange[j]}">
                <div class='stacked-cell-center-top'>${listDateRange[j]}</div>
            </g:if>
            <g:else>
                <div class='stacked-cell-center-top'>Prev Period ${j + 1}</div>
            </g:else>
        </div>
    </th></g:if>
        <g:if test="${labelConfigCopy."exe${j}newFatalCountVigibase"}">
    <th class="" data-field="exe${j}newFatalCountVigibase">
        <div class="th-label" data-field="exe${j}newFatalCountVigibase">
            <div class='stacked-cell-center-top'>
                ${labelConfig."exe${j}newFatalCountVigibase".split("/")[0]}
            </div>

            <div class='stacked-cell-center-top'>
                ${labelConfig."exe${j}newFatalCountVigibase".split("/")[1]}
            </div>
            <g:if test="${listDateRange[j]}">
                <div class='stacked-cell-center-top'>${listDateRange[j]}</div>
            </g:if>
            <g:else>
                <div class='stacked-cell-center-top'>Prev Period ${j + 1}</div>
            </g:else>
        </div>
    </th></g:if>
            <g:if test="${labelConfigCopy."exe${j}newCountVigibase"}">
    <th class="" data-field="exe${j}newCountVigibase">
        <div class="th-label" data-field="exe${j}newCountVigibase">
            <div class='stacked-cell-center-top'>
                ${labelConfig."exe${j}newCountVigibase".split("/")[0]}
            </div>

            <div class='stacked-cell-center-top'>
                ${labelConfig."exe${j}newCountVigibase".split("/")[1]}
            </div>
            <g:if test="${listDateRange[j]}">
                <div class='stacked-cell-center-top'>${listDateRange[j]}</div>
            </g:if>
            <g:else>
                <div class='stacked-cell-center-top'>Prev Period ${j + 1}</div>
            </g:else>
        </div>
    </th></g:if>
                <g:if test="${labelConfigCopy."exe${j}newPediatricCountVigibase"}">
    <th class="" data-field="exe${j}newPediatricCountVigibase">
        <div class="th-label" data-field="exe${j}newPediatricCountVigibase">
            <div class='stacked-cell-center-top'>
                ${labelConfig."exe${j}newPediatricCountVigibase".split("/")[0]}
            </div>

            <div class='stacked-cell-center-top'>
                ${labelConfig."exe${j}newPediatricCountVigibase".split("/")[1]}
            </div>
            <g:if test="${listDateRange[j]}">
                <div class='stacked-cell-center-top'>${listDateRange[j]}</div>
            </g:if>
            <g:else>
                <div class='stacked-cell-center-top'>Prev Period ${j + 1}</div>
            </g:else>
        </div>
    </th></g:if>
                    <g:if test="${labelConfigCopy."exe${j}newGeriatricCountVigibase"}">
    <th class="" data-field="exe${j}newGeriatricCountVigibase">
        <div class="th-label" data-field="exe${j}newGeriatricCountVigibase">
            <div class='stacked-cell-center-top'>
                ${labelConfig."exe${j}newGeriatricCountVigibase".split("/")[0]}
            </div>

            <div class='stacked-cell-center-top'>
                ${labelConfig."exe${j}newGeriatricCountVigibase".split("/")[1]}
            </div>
            <g:if test="${listDateRange[j]}">
                <div class='stacked-cell-center-top'>${listDateRange[j]}</div>
            </g:if>
            <g:else>
                <div class='stacked-cell-center-top'>Prev Period ${j + 1}</div>
            </g:else>
        </div>
    </th></g:if>
    <g:if test="${showPrrVigibase}">
        <g:if test="${labelConfigCopy."exe${j}prrValueVigibase"}">
        <th data-field="exe${j}prrValueVigibase">
            <div class='th-label' data-field="exe${j}prrValueVigibase">
                <div class='stacked-cell-center-top'>
                    ${labelConfig."exe${j}prrValueVigibase"}
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
            <g:if test="${labelConfigCopy."exe${j}prrLCIVigibase"}">
        <th class="" data-field="exe${j}prrLCIVigibase">
            <div class="th-label" data-field="exe${j}prrLCIVigibase">
                <div class='stacked-cell-center-top'>
                    ${labelConfig."exe${j}prrLCIVigibase".split("/")[0]}
                </div>

                <div class='stacked-cell-center-top'>
                    ${labelConfig."exe${j}prrLCIVigibase".split("/")[1]}
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
    </g:if>
    <g:if test="${showRorVigibase}">
        <g:if test="${labelConfigCopy."exe${j}rorValueVigibase"}">
        <th class="" data-field="exe${j}rorValueVigibase">
            <div class='th-label' data-field="exe${j}rorValueVaers">
                <div class='stacked-cell-center-top'>
                    ${labelConfig."exe${j}rorValueVigibase"}
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
            <g:if test="${labelConfigCopy."exe${j}rorLCIVigibase"}">
        <th class="" data-field="exe${j}rorLCIVigibase">
            <div class="th-label" data-field="exe${j}rorLCIVigibase">
                <div class="stacked-cell-center-top">${labelConfig."exe${j}rorLCIVigibase".split("/")[0]}</div>

                <div class="stacked-cell-center-top">${labelConfig."exe${j}rorLCIVigibase".split("/")[1]}</div>
                <g:if test="${listDateRange[j]}">
                    <div class='stacked-cell-center-top'>${listDateRange[j]}</div>
                </g:if>
                <g:else>
                    <div class='stacked-cell-center-top'>Prev Period ${j + 1}</div>
                </g:else>
            </div>
        </th>
            </g:if>
    </g:if>
    <g:if test="${showEbgmVigibase}">
        <g:if test="${labelConfigCopy."exe${j}ebgmVigibase"}">
        <th class="" data-field="exe${j}ebgmVigibase">
            <div class='th-label' data-field="exe${j}ebgmVigibase">
                <div class='stacked-cell-center-top'>
                    ${labelConfig."exe${j}ebgmVigibase"}
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
            <g:if test="${labelConfigCopy."exe${j}eb05Vigibase"}">
        <th class="" data-field="exe${j}eb05Vigibase">
            <div class="th-label" data-field="exe${j}eb05Vigibase">
                <div class='stacked-cell-center-top'>
                    ${labelConfig."exe${j}eb05Vigibase".split("/")[0]}
                </div>

                <div class='stacked-cell-center-top'>
                    ${labelConfig."exe${j}eb05Vigibase".split("/")[1]}
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
    </g:if>
    <g:if test="${showPrrVigibase && showRorVigibase}">
        <g:if test="${labelConfigCopy."exe${j}chiSquareVigibase"}">
        <th class="" data-field="exe${j}chiSquareVigibase">
            <div class='th-label' data-field="exe${j}chiSquareVigibase">
                <div class='stacked-cell-center-top'>
                    ${labelConfig."exe${j}chiSquareVigibase"}
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
    </g:if>
</g:if>