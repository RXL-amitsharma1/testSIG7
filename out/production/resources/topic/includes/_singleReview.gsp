<div class="rxmain-container ">

    <div class="rxmain-container-inner">

        <div class="rxmain-container-row rxmain-container-header">
            <label class="rxmain-container-header-label">
                <g:message code="app.single.case.review" />
            </label>
        </div>

        <div class="rxmain-container-content">
            <table id="rxTableSingleReview" class="dataTable row-border hover table-hd-less-gap no-footer" width="100%">
                <thead>
                <tr>
                    <th style="width:13%">Alert Name</th>
                    <th style="width:5%">Priority</th>
                    <th style="width:9%">Case Number</th>
                    <th style="width:10%">Product Name</th>
                    <th>Event PT</th>
                    <th>S/U</th>
                    <th>Signal Names</th>
                    <th style="width:10%">Disposition</th>
                    <th>History</th>
                    <th></th>
                </tr>
                </thead>
            </table>

        </div>

    </div>

</div>
<g:render template="/includes/modals/case_history_modal" />
<g:render template="/includes/modals/alert_comment_modal" />