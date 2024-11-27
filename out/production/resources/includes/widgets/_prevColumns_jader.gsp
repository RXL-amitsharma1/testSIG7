<g:each in="${jaderColumnList}" status="i" var="columnInfoMap">
    <g:if test="${columnInfoMap.type == 'countStacked'}">
        <th class="sorting_disabled" data-field="exe${j}${columnInfoMap.name}">
            <div class="th-label" data-field="exe${j}${columnInfoMap.name}">
                <div class='stacked-cell-center-top'>
                    Prev Period ${j + 1}&nbsp;${columnInfoMap?.display?.split('/')[0]}
                </div>

                <div class='stacked-cell-center-top'>
                    Prev Period ${j + 1}&nbsp;${columnInfoMap?.display?.split('/')[1]}
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
    <g:if test = "${columnInfoMap.type == 'count'}">
        <th class="sorting_disabled" data-field="exe${j}${columnInfoMap.name}">
            <div class='th-label' data-field="exe${j}${columnInfoMap.name}">
                <g:if test="${groupBySmq && columnInfoMap?.display.split("#OR").size() > 1}">
                    <div class='stacked-cell-center-top'>Prev Period ${j+1}&nbsp;${columnInfoMap?.display.split("#OR")[1]}</div>
                </g:if>
                <g:else>
                    <div class='stacked-cell-center-top'>Prev Period ${j+1}&nbsp;${columnInfoMap?.display.split("#OR")[0]}</div>
                </g:else>
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
