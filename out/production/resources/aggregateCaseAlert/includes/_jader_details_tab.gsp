<g:each in="${jaderColumnList}" status="i" var="columnInfoMap">
    <g:if test="${ columnInfoMap.name != 'priority' && (columnInfoMap.type == 'text' || columnInfoMap.type == 'count' || columnInfoMap.type == 'score')}">
        <th data-idx="${i + 5}" data-field="${columnInfoMap.name}">
            <g:if test="${groupBySmq && columnInfoMap?.display.split("#OR").size() > 1}">
                <div class="th-label" data-field="${columnInfoMap.name}">${columnInfoMap?.display.split("#OR")[1]}</div>
            </g:if>
            <g:else>
                <div class="th-label" data-field="${columnInfoMap.name}">${columnInfoMap?.display.split("#OR")[0]}</div>
            </g:else>
        </th>

    </g:if>
    <g:if test="${columnInfoMap.type == 'countStacked'}">
        <th data-idx="${i + 5}" data-field="${columnInfoMap.name}">
            <div class="th-label" data-field="${columnInfoMap.name}">
                <div class="stacked-cell-center-top">
                    ${columnInfoMap?.display?.split('/')[0]}
                </div>

                <div class="stacked-cell-center-bottom">
                    ${columnInfoMap?.display?.split('/')[1]}
                </div>

            </div>
        </th>
    </g:if>

</g:each>
<g:each in="${(0..<prevColCount)}" var="j">
    <g:render template="/includes/widgets/prevColumns_jader"
              model="[listDateRange: listDateRange, prevColCount: prevColCount, j: j, jaderColumnList: jaderColumnList,groupBySmq:groupBySmq]"/>

</g:each>