
<%@ page import="com.rxlogix.Constants; com.rxlogix.enums.ReportFormat; grails.util.Holders" %>
<g:if test="${callingScreen == Constants.Commons.REVIEW}">
    <g:each in="${(0..<prevColCount)}" var="j">
        <g:if test="${labelConfigCopy."exe${j}newSponCount"}">
            <th class="" data-field="exe${j}newSponCount">
                <div class="th-label" data-field="exe${j}newSponCount">
                    <div class='stacked-cell-center-top'>
                        ${labelConfig."exe${j}newSponCount".split("/")[0]}
                    </div>

                    <div class='stacked-cell-center-top'>
                        ${labelConfig."exe${j}newSponCount".split("/")[1]}
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
        <g:if test="${labelConfigCopy."exe${j}newSeriousCount"}">
            <th class="" data-field="exe${j}newSeriousCount">
                <div class="th-label" data-field="exe${j}newSeriousCount">
                    <div class='stacked-cell-center-top'>
                        ${labelConfig."exe${j}newSeriousCount".split("/")[0]}
                    </div>

                    <div class='stacked-cell-center-top'>
                        ${labelConfig."exe${j}newSeriousCount".split("/")[1]}
                    </div>
                    <g:if test="${listDateRange[j]}">
                        <div class='stacked-cell-center-top'>${listDateRange[j]}</div>
                    </g:if>
                    <g:else>
                        <div class='stacked-cell-center-top'>Prev Period ${j + 1}</div>
                    </g:else>
                </div>
            </th></g:if>

        <g:if test="${labelConfigCopy."exe${j}newFatalCount"}">
            <th class="" data-field="exe${j}newFatalCount">
                <div class="th-label" data-field="exe${j}newFatalCount">
                    <div class='stacked-cell-center-top'>
                        ${labelConfig."exe${j}newFatalCount".split("/")[0]}
                    </div>

                    <div class='stacked-cell-center-top'>
                        ${labelConfig."exe${j}newFatalCount".split("/")[1]}
                    </div>
                    <g:if test="${listDateRange[j]}">
                        <div class='stacked-cell-center-top'>${listDateRange[j]}</div>
                    </g:if>
                    <g:else>
                        <div class='stacked-cell-center-top'>Prev Period ${j + 1}</div>
                    </g:else>
                </div>
            </th></g:if>

        <g:if test="${labelConfigCopy."exe${j}newStudyCount"}">
            <th class="" data-field="exe${j}newStudyCount">
                <div class="th-label" data-field="exe${j}newStudyCount">
                    <div class='stacked-cell-center-top'>
                        ${labelConfig."exe${j}newStudyCount".split("/")[0]}
                    </div>

                    <div class='stacked-cell-center-top'>
                        ${labelConfig."exe${j}newStudyCount".split("/")[1]}
                    </div>
                    <g:if test="${listDateRange[j]}">
                        <div class='stacked-cell-center-top'>${listDateRange[j]}</div>
                    </g:if>
                    <g:else>
                        <div class='stacked-cell-center-top'>Prev Period ${j + 1}</div>
                    </g:else>
                </div>
            </th></g:if>

        <g:if test="${labelConfigCopy."exe${j}newCount"}">
            <th class="" data-field="exe${j}newCount">
                <div class="th-label" data-field="exe${j}newCount">
                    <div class='stacked-cell-center-top'>
                        ${labelConfig."exe${j}newCount".split("/")[0]}
                    </div>

                    <div class='stacked-cell-center-top'>
                        ${labelConfig."exe${j}newCount".split("/")[1]}
                    </div>
                    <g:if test="${listDateRange[j]}">
                        <div class='stacked-cell-center-top'>${listDateRange[j]}</div>
                    </g:if>
                    <g:else>
                        <div class='stacked-cell-center-top'>Prev Period ${j + 1}</div>
                    </g:else>
                </div>
            </th></g:if>

        <g:if test="${labelConfigCopy."exe${j}newPediatricCount"}">
            <th class="" data-field="exe${j}newPediatricCount">
                <div class="th-label" data-field="exe${j}newPediatricCount">
                    <div class='stacked-cell-center-top'>
                        ${labelConfig."exe${j}newPediatricCount".split("/")[0]}
                    </div>

                    <div class='stacked-cell-center-top'>
                        ${labelConfig."exe${j}newPediatricCount".split("/")[1]}
                    </div>
                    <g:if test="${listDateRange[j]}">
                        <div class='stacked-cell-center-top'>${listDateRange[j]}</div>
                    </g:if>
                    <g:else>
                        <div class='stacked-cell-center-top'>Prev Period ${j + 1}</div>
                    </g:else>
                </div>
            </th></g:if>

        <g:if test="${labelConfigCopy."exe${j}newInteractingCount"}">
            <th class="" data-field="exe${j}newInteractingCount">
                <div class="th-label" data-field="exe${j}newInteractingCount">
                    <div class='stacked-cell-center-top'>
                        ${labelConfig."exe${j}newInteractingCount".split("/")[0]}
                    </div>

                    <div class='stacked-cell-center-top'>
                        ${labelConfig."exe${j}newInteractingCount".split("/")[1]}
                    </div>
                    <g:if test="${listDateRange[j]}">
                        <div class='stacked-cell-center-top'>${listDateRange[j]}</div>
                    </g:if>
                    <g:else>
                        <div class='stacked-cell-center-top'>Prev Period ${j + 1}</div>
                    </g:else>
                </div>
            </th></g:if>
        <g:if test="${labelConfigCopy."exe${j}newGeriatricCount"}">
            <th class="" data-field="exe${j}newGeriatricCount">
                <div class="th-label" data-field="exe${j}newGeriatricCount">
                    <div class='stacked-cell-center-top'>
                        ${labelConfig."exe${j}newGeriatricCount".split("/")[0]}
                    </div>

                    <div class='stacked-cell-center-top'>
                        ${labelConfig."exe${j}newGeriatricCount".split("/")[1]}
                    </div>
                    <g:if test="${listDateRange[j]}">
                        <div class='stacked-cell-center-top'>${listDateRange[j]}</div>
                    </g:if>
                    <g:else>
                        <div class='stacked-cell-center-top'>Prev Period ${j + 1}</div>
                    </g:else>
                </div>
            </th></g:if>

        <g:if test="${labelConfigCopy."exe${j}newNonSerious"}">
            <th class="" data-field="exe${j}newNonSerious">
                <div class="th-label" data-field="exe${j}newNonSerious">
                    <div class='stacked-cell-center-top'>
                        ${labelConfig."exe${j}newNonSerious".split("/")[0]}
                    </div>

                    <div class='stacked-cell-center-top'>
                        ${labelConfig."exe${j}newNonSerious".split("/")[1]}
                    </div>
                    <g:if test="${listDateRange[j]}">
                        <div class='stacked-cell-center-top'>${listDateRange[j]}</div>
                    </g:if>
                    <g:else>
                        <div class='stacked-cell-center-top'>Prev Period ${j + 1}</div>
                    </g:else>
                </div>
            </th></g:if>

        <g:if test="${showPrr}">
            <g:if test="${labelConfigCopy."exe${j}prrValue"}">
                <th data-field="exe${j}prrValue">
                    <div class='th-label' data-field="exe${j}prrValue">
                        <div class='stacked-cell-center-top'>
                            ${labelConfig."exe${j}prrValue"}
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

            <g:if test="${labelConfigCopy."exe${j}prrLCI"}">
                <th class="" data-field="exe${j}prrLCI">
                    <div class="th-label" data-field="exe${j}prrLCI">
                        <div class='stacked-cell-center-top'>
                            ${labelConfig."exe${j}prrLCI".split("/")[0]}
                        </div>

                        <div class='stacked-cell-center-top'>
                            Prev Period ${labelConfig."exe${j}prrLCI".split("/")[1]}
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
        <g:if test="${showRor}">
            <g:if test="${labelConfigCopy."exe${j}rorValue"}">
                <th class="" data-field="exe${j}rorValue">
                    <div class='th-label' data-field="exe${j}rorValue">
                        <div class='stacked-cell-center-top'>
                            <g:if test="${isRor}">
                                ${labelConfig."exe${j}rorValue".split("#OR")[0]}
                            </g:if>
                            <g:else>
                                ${labelConfig."exe${j}rorValue".split("#OR")[1]}
                            </g:else>
                        </div>
                        <g:if test="${listDateRange[j]}">
                            <div class='stacked-cell-center-top'>${listDateRange[j]}</div>
                        </g:if>
                        <g:else>
                            <div class='stacked-cell-center-top'>Prev Period ${j + 1}</div>
                        </g:else>
                    </div>
                </th></g:if>

            <g:if test="${labelConfigCopy."exe${j}rorLCI"}">
                <th class="" data-field="exe${j}rorLCI">
                    <div class="th-label" data-field="exe${j}rorLCI">
                        <g:if test="${isRor}">
                            <div class="stacked-cell-center-top">${labelConfig."exe${j}rorLCI".split("#OR")[0].split("/")[0]}</div>

                            <div class="stacked-cell-center-top">Prev Period ${labelConfig."exe${j}rorLCI".split("#OR")[0].split("/")[1]}</div>
                        </g:if>
                        <g:else>
                            <div class="stacked-cell-center-top">${labelConfig."exe${j}rorLCI".split("#OR")[1].split("/")[0]}</div>

                            <div class="stacked-cell-center-top">Prev Period ${labelConfig."exe${j}rorLCI".split("#OR")[1].split("/")[1]}</div>
                        </g:else>
                        <g:if test="${listDateRange[j]}">
                            <div class='stacked-cell-center-top'>${listDateRange[j]}</div>
                        </g:if>
                        <g:else>
                            <div class='stacked-cell-center-top'>Prev Period ${j + 1}</div>
                        </g:else>
                    </div>
                </th></g:if>
        </g:if>
        <g:if test="${showEbgm}">
            <g:if test="${labelConfigCopy."exe${j}ebgm"}">
                <th class="" data-field="exe${j}ebgm">
                    <div class='th-label' data-field="exe${j}ebgm">
                        <div class='stacked-cell-center-top'>
                            ${labelConfig."exe${j}ebgm"}
                        </div>
                        <g:if test="${listDateRange[j]}">
                            <div class='stacked-cell-center-top'>${listDateRange[j]}</div>
                        </g:if>
                        <g:else>
                            <div class='stacked-cell-center-top'>Prev Period ${j + 1}</div>
                        </g:else>
                    </div>
                </th></g:if>

            <g:if test="${labelConfigCopy."exe${j}eb05"}">
                <th class="" data-field="exe${j}eb05">
                    <div class="th-label" data-field="exe${j}eb05">
                        <div class='stacked-cell-center-top'>
                            ${labelConfig."exe${j}eb05".split("/")[0]}
                        </div>

                        <div class='stacked-cell-center-top'>
                            ${labelConfig."exe${j}eb05".split("/")[1]}
                        </div>
                        <g:if test="${listDateRange[j]}">
                            <div class='stacked-cell-center-top'>${listDateRange[j]}</div>
                        </g:if>
                        <g:else>
                            <div class='stacked-cell-center-top'>Prev Period ${j + 1}</div>
                        </g:else>
                    </div>
                </th></g:if>
        </g:if>
        <g:if test="${showPrr && showRor}">
            <g:if test="${labelConfigCopy."exe${j}chiSquare"}">
                <th class="" data-field="exe${j}chiSquare">
                    <div class='th-label' data-field="exe${j}chiSquare">
                        <div class='stacked-cell-center-top'>
                            ${labelConfig."exe${j}chiSquare"}
                        </div>
                        <g:if test="${listDateRange[j]}">
                            <div class='stacked-cell-center-top'>${listDateRange[j]}</div>
                        </g:if>
                        <g:else>
                            <div class='stacked-cell-center-top'>Prev Period ${j + 1}</div>
                        </g:else>
                    </div>
                </th>
            </g:if></g:if>
        <g:if test="${showEbgm}">
            <g:if test="${labelConfigCopy."exe${j}rrValue"}">
                <th data-field="exe${j}rrValue">
                    <div class='th-label' data-field="exe${j}rrValue">
                        <div class='stacked-cell-center-top'>
                            ${labelConfig."exe${j}rrValue"}
                        </div>
                        <g:if test="${listDateRange[j]}">
                            <div class='stacked-cell-center-top'>${listDateRange[j]}</div>
                        </g:if>
                        <g:else>
                            <div class='stacked-cell-center-top'>Prev Period ${j + 1}</div>
                        </g:else>
                    </div>
                </th>
            </g:if></g:if>

        <g:if test="${isFaersEnabled}">
            <g:if test="${labelConfigCopy."exe${j}newSponCountFaers"}">
                <th class="" data-field="exe${j}newSponCountFaers">
                    <div class="th-label" data-field="exe${j}newSponCountFaers">
                        <div class='stacked-cell-center-top'>
                            ${labelConfig."exe${j}newSponCountFaers".split("/")[0]}
                        </div>

                        <div class='stacked-cell-center-top'>
                            ${labelConfig."exe${j}newSponCountFaers".split("/")[1]}
                        </div>

                        <g:if test="${prevFaersDate[j]}">
                            <div class='stacked-cell-center-top'>${prevFaersDate[j]}</div>
                        </g:if>
                        <g:else>
                            <div class='stacked-cell-center-top'>Prev Period ${j + 1}</div>
                        </g:else>
                    </div>
                </th></g:if>

            <g:if test="${labelConfigCopy."exe${j}newSeriousCountFaers"}">
                <th class="" data-field="exe${j}newSeriousCountFaers">
                    <div class="th-label" data-field="exe${j}newSeriousCountFaers">
                        <div class='stacked-cell-center-top'>
                            ${labelConfig."exe${j}newSeriousCountFaers".split("/")[0]}
                        </div>

                        <div class='stacked-cell-center-top'>
                            ${labelConfig."exe${j}newSeriousCountFaers".split("/")[1]}
                        </div>

                        <g:if test="${prevFaersDate[j]}">
                            <div class='stacked-cell-center-top'>${prevFaersDate[j]}</div>
                        </g:if>
                        <g:else>
                            <div class='stacked-cell-center-top'>Prev Period ${j + 1}</div>
                        </g:else>
                    </div>
                </th></g:if>

            <g:if test="${labelConfigCopy."exe${j}newFatalCountFaers"}">
                <th class="" data-field="exe${j}newFatalCountFaers">
                    <div class="th-label" data-field="exe${j}newFatalCountFaers">
                        <div class='stacked-cell-center-top'>
                            ${labelConfig."exe${j}newFatalCountFaers".split("/")[0]}
                        </div>

                        <div class='stacked-cell-center-top'>
                            ${labelConfig."exe${j}newFatalCountFaers".split("/")[1]}
                        </div>

                        <g:if test="${prevFaersDate[j]}">
                            <div class='stacked-cell-center-top'>${prevFaersDate[j]}</div>
                        </g:if>
                        <g:else>
                            <div class='stacked-cell-center-top'>Prev Period ${j + 1}</div>
                        </g:else>
                    </div>
                </th></g:if>

            <g:if test="${labelConfigCopy."exe${j}newStudyCountFaers"}">
                <th class="" data-field="exe${j}newStudyCountFaers">
                    <div class="th-label" data-field="exe${j}newStudyCountFaers">
                        <div class='stacked-cell-center-top'>
                            ${labelConfig."exe${j}newStudyCountFaers".split("/")[0]}
                        </div>

                        <div class='stacked-cell-center-top'>
                            ${labelConfig."exe${j}newStudyCountFaers".split("/")[1]}
                        </div>

                        <g:if test="${prevFaersDate[j]}">
                            <div class='stacked-cell-center-top'>${prevFaersDate[j]}</div>
                        </g:if>
                        <g:else>
                            <div class='stacked-cell-center-top'>Prev Period ${j + 1}</div>
                        </g:else>
                    </div>
                </th></g:if>

            <g:if test="${labelConfigCopy."exe${j}newCountFaers"}">
                <th class="" data-field="exe${j}newCountFaers">
                    <div class="th-label" data-field="exe${j}newCountFaers">
                        <div class='stacked-cell-center-top'>
                            ${labelConfig."exe${j}newCountFaers".split("/")[0]}
                        </div>

                        <div class='stacked-cell-center-top'>
                            ${labelConfig."exe${j}newCountFaers".split("/")[1]}
                        </div>
                        <g:if test="${prevFaersDate[j]}">
                            <div class='stacked-cell-center-top'>${prevFaersDate[j]}</div>
                        </g:if>
                        <g:else>
                            <div class='stacked-cell-center-top'>Prev Period ${j + 1}</div>
                        </g:else>
                    </div>
                </th></g:if>
            <g:if test="${labelConfigCopy."exe${j}newPediatricCountFaers"}">
                <th class="" data-field="exe${j}newPediatricCountFaers">
                    <div class="th-label" data-field="exe${j}newPediatricCountFaers">
                        <div class='stacked-cell-center-top'>
                            ${labelConfig."exe${j}newPediatricCountFaers".split("/")[0]}
                        </div>

                        <div class='stacked-cell-center-top'>
                            ${labelConfig."exe${j}newPediatricCountFaers".split("/")[1]}
                        </div>
                        <g:if test="${prevFaersDate[j]}">
                            <div class='stacked-cell-center-top'>${prevFaersDate[j]}</div>
                        </g:if>
                        <g:else>
                            <div class='stacked-cell-center-top'>Prev Period ${j + 1}</div>
                        </g:else>
                    </div>
                </th></g:if>
            <g:if test="${labelConfigCopy."exe${j}newInteractingCountFaers"}">
                <th class="" data-field="exe${j}newInteractingCountFaers">
                    <div class="th-label" data-field="exe${j}newInteractingCountFaers">
                        <div class='stacked-cell-center-top'>
                            ${labelConfig."exe${j}newInteractingCountFaers".split("/")[0]}
                        </div>

                        <div class='stacked-cell-center-top'>
                            ${labelConfig."exe${j}newInteractingCountFaers".split("/")[1]}
                        </div>
                        <g:if test="${prevFaersDate[j]}">
                            <div class='stacked-cell-center-top'>${prevFaersDate[j]}</div>
                        </g:if>
                        <g:else>
                            <div class='stacked-cell-center-top'>Prev Period ${j + 1}</div>
                        </g:else>
                    </div>
                </th></g:if>

            <g:if test="${labelConfigCopy."exe${j}newGeriatricCountFaers"}">
                <th class="" data-field="exe${j}newGeriatricCountFaers">
                    <div class="th-label" data-field="exe${j}newGeriatricCountFaers">
                        <div class='stacked-cell-center-top'>
                            ${labelConfig."exe${j}newGeriatricCountFaers".split("/")[0]}
                        </div>

                        <div class='stacked-cell-center-top'>
                            ${labelConfig."exe${j}newGeriatricCountFaers".split("/")[1]}
                        </div>
                        <g:if test="${prevFaersDate[j]}">
                            <div class='stacked-cell-center-top'>${prevFaersDate[j]}</div>
                        </g:if>
                        <g:else>
                            <div class='stacked-cell-center-top'>Prev Period ${j + 1}</div>
                        </g:else>
                    </div>
                </th>
            </g:if>
            <g:if test="${labelConfigCopy."exe${j}newNonSeriousFaers"}">
                <th class="" data-field="exe${j}newNonSeriousFaers">
                    <div class="th-label" data-field="exe${j}newNonSeriousFaers">
                        <div class='stacked-cell-center-top'>
                            ${labelConfig."exe${j}newNonSeriousFaers".split("/")[0]}
                        </div>

                        <div class='stacked-cell-center-top'>
                            ${labelConfig."exe${j}newNonSeriousFaers".split("/")[1]}
                        </div>
                        <g:if test="${prevFaersDate[j]}">
                            <div class='stacked-cell-center-top'>${prevFaersDate[j]}</div>
                        </g:if>
                        <g:else>
                            <div class='stacked-cell-center-top'>Prev Period ${j + 1}</div>
                        </g:else>
                    </div>
                </th></g:if>

            <g:if test="${showPrrFaers}">
                <g:if test="${labelConfigCopy."exe${j}prrValueFaers"}">
                    <th data-field="exe${j}prrValueFaers">
                        <div class='th-label' data-field="exe${j}prrValueFaers">
                            <div class='stacked-cell-center-top'>
                                ${labelConfig."exe${j}prrValueFaers"}
                            </div>

                            <g:if test="${prevFaersDate[j]}">
                                <div class='stacked-cell-center-top'>${prevFaersDate[j]}</div>
                            </g:if>
                            <g:else>
                                <div class='stacked-cell-center-top'>Prev Period ${j + 1}</div>
                            </g:else>
                        </div>
                    </th></g:if>
                <g:if test="${labelConfigCopy."exe${j}prrLCIFaers"}">
                    <th data-field="exe${j}prrLCIFaers">
                        <div class="th-label" data-field="exe${j}prrLCIFaers">
                            <div class='stacked-cell-center-top'>
                                ${labelConfig."exe${j}prrLCIFaers".split("/")[0]}
                            </div>

                            <div class='stacked-cell-center-top'>
                                ${labelConfig."exe${j}prrLCIFaers".split("/")[1]}
                            </div>

                            <g:if test="${prevFaersDate[j]}">
                                <div class='stacked-cell-center-top'>${prevFaersDate[j]}</div>
                            </g:if>
                            <g:else>
                                <div class='stacked-cell-center-top'>Prev Period ${j + 1}</div>
                            </g:else>
                        </div>
                    </th>
                </g:if></g:if>
            <g:if test="${showRorFaers}">
                <g:if test="${labelConfigCopy."exe${j}rorValueFaers"}">
                    <th class="" data-field="exe${j}rorValueFaers">
                        <div class='th-label' data-field="exe${j}rorValueFaers">
                            <div class='stacked-cell-center-top'>
                                ${labelConfig."exe${j}rorValueFaers"}
                            </div>

                            <g:if test="${prevFaersDate[j]}">
                                <div class='stacked-cell-center-top'>${prevFaersDate[j]}</div>
                            </g:if>
                            <g:else>
                                <div class='stacked-cell-center-top'>Prev Period ${j + 1}</div>
                            </g:else>
                        </div>
                    </th>
                </g:if>
                <g:if test="${labelConfigCopy."exe${j}rorLCIFaers"}">
                    <th class="" data-field="exe${j}rorLCIFaers">
                        <div class="th-label" data-field="exe${j}rorLCIFaers">
                            <div class='stacked-cell-center-top'>
                                ${labelConfig."exe${j}rorLCIFaers".split("/")[0]}
                            </div>

                            <div class='stacked-cell-center-top'>
                                ${labelConfig."exe${j}rorLCIFaers".split("/")[1]}
                            </div>
                            <g:if test="${prevFaersDate[j]}">
                                <div class='stacked-cell-center-top'>${prevFaersDate[j]}</div>
                            </g:if>
                            <g:else>
                                <div class='stacked-cell-center-top'>Prev Period ${j + 1}</div>
                            </g:else>
                        </div>
                    </th>
                </g:if></g:if>
            <g:if test="${showEbgmFaers}">
                <g:if test="${labelConfigCopy."exe${j}ebgmFaers"}">
                    <th class="" data-field="exe${j}ebgmFaers">
                        <div class='th-label' data-field="exe${j}ebgmFaers">
                            <div class='stacked-cell-center-top'>
                                ${labelConfig."exe${j}ebgmFaers"}
                            </div>

                            <g:if test="${prevFaersDate[j]}">
                                <div class='stacked-cell-center-top'>${prevFaersDate[j]}</div>
                            </g:if>
                            <g:else>
                                <div class='stacked-cell-center-top'>Prev Period ${j + 1}</div>
                            </g:else>
                        </div>
                    </th>
                </g:if>
                <g:if test="${labelConfigCopy."exe${j}eb05Faers"}">
                    <th class="" data-field="exe${j}eb05Faers">
                        <div class="th-label" data-field="exe${j}eb05Faers">
                            <div class='stacked-cell-center-top'>
                                ${labelConfig."exe${j}eb05Faers".split("/")[0]}
                            </div>

                            <div class='stacked-cell-center-top'>
                                ${labelConfig."exe${j}eb05Faers".split("/")[1]}
                            </div>

                            <g:if test="${prevFaersDate[j]}">
                                <div class='stacked-cell-center-top'>${prevFaersDate[j]}</div>
                            </g:if>
                            <g:else>
                                <div class='stacked-cell-center-top'>Prev Period ${j + 1}</div>
                            </g:else>
                        </div>
                    </th>
                </g:if></g:if>
            <g:if test="${showPrrFaers && showRorFaers}">
                <g:if test="${labelConfigCopy."exe${j}chiSquareFaers"}">
                    <th class="" data-field="exe${j}chiSquareFaers">
                        <div class='th-label' data-field="exe${j}chiSquareFaers">
                            <div class='stacked-cell-center-top'>
                                ${labelConfig."exe${j}chiSquareFaers"}
                            </div>
                            <g:if test="${listDateRange[j]}">
                                <div class='stacked-cell-center-top'>${listDateRange[j]}</div>
                            </g:if>
                            <g:else>
                                <div class='stacked-cell-center-top'>Prev Period ${j + 1}</div>
                            </g:else>
                        </div>
                    </th></g:if>
            </g:if>
        </g:if>

        <g:if test="${isVaersEnabled}">
            <g:if test="${labelConfigCopy."exe${j}newSeriousCountVaers"}">
                <th class="" data-field="exe${j}newSeriousCountVaers">
                    <div class="th-label" data-field="exe${j}newSeriousCountVaers">
                        <div class='stacked-cell-center-top'>
                            ${labelConfig."exe${j}newSeriousCountVaers".split("/")[0]}
                        </div>

                        <div class='stacked-cell-center-top'>
                            ${labelConfig."exe${j}newSeriousCountVaers".split("/")[1]}
                        </div>
                        <g:if test="${listDateRange[j]}">
                            <div class='stacked-cell-center-top'>${listDateRange[j]}</div>
                        </g:if>
                        <g:else>
                            <div class='stacked-cell-center-top'>Prev Period ${j + 1}</div>
                        </g:else>
                    </div>
                </th></g:if>

            <g:if test="${labelConfigCopy."exe${j}newFatalCountVaers"}">
                <th class="" data-field="exe${j}newFatalCountVaers">
                    <div class="th-label" data-field="exe${j}newFatalCountVaers">
                        <div class='stacked-cell-center-top'>
                            ${labelConfig."exe${j}newFatalCountVaers".split("/")[0]}
                        </div>

                        <div class='stacked-cell-center-top'>
                            ${labelConfig."exe${j}newFatalCountVaers".split("/")[1]}
                        </div>
                        <g:if test="${listDateRange[j]}">
                            <div class='stacked-cell-center-top'>${listDateRange[j]}</div>
                        </g:if>
                        <g:else>
                            <div class='stacked-cell-center-top'>Prev Period ${j + 1}</div>
                        </g:else>
                    </div>
                </th></g:if>

            <g:if test="${labelConfigCopy."exe${j}newCountVaers"}">
                <th class="" data-field="exe${j}newCountVaers">
                    <div class="th-label" data-field="exe${j}newCountVaers">
                        <div class='stacked-cell-center-top'>
                            ${labelConfig."exe${j}newCountVaers".split("/")[0]}
                        </div>

                        <div class='stacked-cell-center-top'>
                            ${labelConfig."exe${j}newCountVaers".split("/")[1]}
                        </div>
                        <g:if test="${listDateRange[j]}">
                            <div class='stacked-cell-center-top'>${listDateRange[j]}</div>
                        </g:if>
                        <g:else>
                            <div class='stacked-cell-center-top'>Prev Period ${j + 1}</div>
                        </g:else>
                    </div>
                </th></g:if>

            <g:if test="${labelConfigCopy."exe${j}newPediatricCountVaers"}">
                <th class="" data-field="exe${j}newPediatricCountVaers">
                    <div class="th-label" data-field="exe${j}newPediatricCountVaers">
                        <div class='stacked-cell-center-top'>
                            ${labelConfig."exe${j}newPediatricCountVaers".split("/")[0]}
                        </div>

                        <div class='stacked-cell-center-top'>
                            ${labelConfig."exe${j}newPediatricCountVaers".split("/")[1]}
                        </div>
                        <g:if test="${listDateRange[j]}">
                            <div class='stacked-cell-center-top'>${listDateRange[j]}</div>
                        </g:if>
                        <g:else>
                            <div class='stacked-cell-center-top'>Prev Period ${j + 1}</div>
                        </g:else>
                    </div>
                </th></g:if>

            <g:if test="${labelConfigCopy."exe${j}newGeriatricCountVaers"}">
                <th class="" data-field="exe${j}newGeriatricCountVaers">
                    <div class="th-label" data-field="exe${j}newGeriatricCountVaers">
                        <div class='stacked-cell-center-top'>
                            ${labelConfig."exe${j}newGeriatricCountVaers".split("/")[0]}
                        </div>

                        <div class='stacked-cell-center-top'>
                            ${labelConfig."exe${j}newGeriatricCountVaers".split("/")[1]}
                        </div>
                        <g:if test="${listDateRange[j]}">
                            <div class='stacked-cell-center-top'>${listDateRange[j]}</div>
                        </g:if>
                        <g:else>
                            <div class='stacked-cell-center-top'>Prev Period ${j + 1}</div>
                        </g:else>
                    </div>
                </th></g:if>
            <g:if test="${showPrrVaers}">
                <g:if test="${labelConfigCopy."exe${j}prrValueVaers"}">
                    <th data-field="exe${j}prrValueVaers">
                        <div class='th-label' data-field="exe${j}prrValueVaers">
                            <div class='stacked-cell-center-top'>
                                ${labelConfig."exe${j}prrValueVaers"}
                            </div>
                            <g:if test="${listDateRange[j]}">
                                <div class='stacked-cell-center-top'>${listDateRange[j]}</div>
                            </g:if>
                            <g:else>
                                <div class='stacked-cell-center-top'>Prev Period ${j + 1}</div>
                            </g:else>
                        </div>
                    </th></g:if>

                <g:if test="${labelConfigCopy."exe${j}prrLCIVaers"}">
                    <th class="" data-field="exe${j}prrLCIVaers">
                        <div class="th-label" data-field="exe${j}prrLCIVaers">
                            <div class='stacked-cell-center-top'>
                                ${labelConfig."exe${j}prrLCIVaers".split("/")[0]}
                            </div>

                            <div class='stacked-cell-center-top'>
                                ${labelConfig."exe${j}prrLCIVaers".split("/")[1]}
                            </div>
                            <g:if test="${listDateRange[j]}">
                                <div class='stacked-cell-center-top'>${listDateRange[j]}</div>
                            </g:if>
                            <g:else>
                                <div class='stacked-cell-center-top'>Prev Period ${j + 1}</div>
                            </g:else>
                        </div>
                    </th></g:if>
            </g:if>
            <g:if test="${showRorVaers}">
                <g:if test="${labelConfigCopy."exe${j}rorValueVaers"}">
                    <th class="" data-field="exe${j}rorValueVaers">
                        <div class='th-label' data-field="exe${j}rorValueVaers">
                            <div class='stacked-cell-center-top'>
                                ${labelConfig."exe${j}rorValueVaers"}
                            </div>
                            <g:if test="${listDateRange[j]}">
                                <div class='stacked-cell-center-top'>${listDateRange[j]}</div>
                            </g:if>
                            <g:else>
                                <div class='stacked-cell-center-top'>Prev Period ${j + 1}</div>
                            </g:else>
                        </div>
                    </th></g:if>
                <g:if test="${labelConfigCopy."exe${j}rorLCIVaers"}">
                    <th class="" data-field="exe${j}rorLCIVaers">
                        <div class="th-label" data-field="exe${j}rorLCIVaers">
                            <div class="stacked-cell-center-top">${labelConfig."exe${j}rorLCIVaers".split("/")[0]}</div>

                            <div class="stacked-cell-center-top">${labelConfig."exe${j}rorLCIVaers".split("/")[1]}</div>
                            <g:if test="${listDateRange[j]}">
                                <div class='stacked-cell-center-top'>${listDateRange[j]}</div>
                            </g:if>
                            <g:else>
                                <div class='stacked-cell-center-top'>Prev Period ${j + 1}</div>
                            </g:else>
                        </div>
                    </th></g:if>
            </g:if>

            <g:if test="${showEbgmVaers}">
                <g:if test="${labelConfigCopy."exe${j}ebgmVaers"}">
                    <th class="" data-field="exe${j}ebgmVaers">
                        <div class='th-label' data-field="exe${j}ebgmVaers">
                            <div class='stacked-cell-center-top'>
                                ${labelConfig."exe${j}ebgmVaers"}
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
                <g:if test="${labelConfigCopy."exe${j}eb05Vaers"}">
                    <th class="" data-field="exe${j}eb05Vaers">
                        <div class="th-label" data-field="exe${j}eb05Vaers">
                            <div class='stacked-cell-center-top'>
                                ${labelConfig."exe${j}eb05Vaers".split("/")[0]}
                            </div>

                            <div class='stacked-cell-center-top'>
                                ${labelConfig."exe${j}eb05Vaers".split("/")[1]}
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


            <g:if test="${showPrrVaers && showRorVaers && labelConfigCopy."exe${j}chiSquareVaers"}">
                <g:if test="${labelConfigCopy."exe${j}chiSquareVaers"}">
                    <th class="" data-field="exe${j}chiSquareVaers">
                        <div class='th-label' data-field="exe${j}chiSquareVaers">
                            <div class='stacked-cell-center-top'>
                                ${labelConfig."exe${j}chiSquareVaers"}
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
        </g:if>
        <g:render template="/includes/widgets/prevColumns_subGroup"
                  model="[callingScreen: callingScreen, isPVAEnabled: isPVAEnabled, isFaersEnabled: isFaersEnabled,relativeSubGroupMap:relativeSubGroupMap,prrRorSubGroupMap:prrRorSubGroupMap, prevFaersDate: prevFaersDate, listDateRange: listDateRange,prevColCount: prevColCount, prevColumns: prevColumns, isVaersEnabled: isVaersEnabled, isVigibaseEnabled: isVigibaseEnabled,
                          showPrr      : showPrr, showRor: showRor, showEbgm: showEbgm, j : j,faersSubGroupsColumnList:faersSubGroupsColumnList, subGroupsColumnList: subGroupsColumnList, labelConfig: labelConfig, labelConfigCopy: labelConfigCopy,labelConfigNew:labelConfigNew]"/>
    </g:each>
    <g:each in="${(0..<prevColCount)}" var="j">
        <g:render template="/includes/widgets/prevColumns1"
                  model="[callingScreen  : callingScreen, listDateRange: listDateRange, prevColCount: prevColCount, prevColumns: prevColumns, isVaersEnabled: isVaersEnabled, isVigibaseEnabled: isVigibaseEnabled,
                          showPrr        : showPrr, showRor: showRor, showEbgm: showEbgm, prevFaersDate: prevFaersDate, prevEvdasDate: prevEvdasDate, j: j,
                          showPrrVigibase: showPrrVigibase, showRorVigibase: showRorVigibase, showEbgmVigibase: showEbgmVigibase, labelConfig: labelConfig, labelConfigCopy: labelConfigCopy,labelConfigNew:labelConfigNew]"/>
    </g:each>

    <g:if test="${isEvdasEnabled}">
        <g:each in="${(0..<prevColCount)}" var="j">
            <g:each in="${prevColumns}" var="val">
                <g:if test="${labelConfigCopy."exe${j}${val.key}Evdas"}">
                <th data-field='exe${j}${val.key}Evdas'>
                    <div class='th-label dateRange' data-field='exe${j}${val.key}Evdas'></div>

                    <div class='stacked-cell-center-top'>Prev ${val.value + " (E)"}</div>

                    <g:if test="${prevEvdasDate[j]}">
                        <div class='stacked-cell-center-top'>${prevEvdasDate[j]}</div>
                    </g:if>
                    <g:else>
                        <div class='stacked-cell-center-top'>Prev Period ${j + 1}</div>
                    </g:else>
                </th>
                </g:if>
            </g:each>

        </g:each>
    </g:if>



</g:if>