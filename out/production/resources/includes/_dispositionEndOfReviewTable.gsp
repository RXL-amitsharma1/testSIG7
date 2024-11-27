<%@ page import="com.rxlogix.Constants; com.rxlogix.util.DateUtil"%>
<div id="dispositionEndOfReviewContainer" style="width: 99%; padding-top: 10px;" >
    <rx:container title="Disposition End of Review Milestone Configuration" signalConfiguration="${true}">
        <div id="dispositionEndPointContainer" class="container-fluid attachments">
            <table id="disposition-endOfReview-table" class="dataTable row-border hover no-footer">
                <thead>
                <tr>
                    <th class="sorting_disabled"><g:message code="controlPanel.signal.dispositions"/></th>
                    <th class="sorting_disabled"><g:message code="controlPanel.signal.status"/></th>
                    <th class="sorting_disabled ${buttonClass}"></th>
                </tr>
                </thead>
            </table>
        </div>

    </rx:container>

</div>
