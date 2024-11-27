<%@ page contentType="text/html;charset=UTF-8" %>

<Style>
.modal-body {
    max-height: calc(100vh - 150px);
    overflow-y: auto;
    padding: 0;
}
.application_close_button{
    background-color: #dae6ec;
    color: #333;
    border-color: #dae6ec;
}
table.dataTable{
    min-width: 100%;
    border: 1px solid #eee;
    border-radius: 10px 10px 10px 15px;
}
.modal .modal-dialog .modal-content .modal-body {
    background-color: #d4e9ef;
    max-height: calc(100vh - 150px);
    overflow-y: auto;
}
.dss-modal-bg .pv-tab{
    margin-top: 0px!important;
}
.pv-tab .common-bg{
    background: url('http://10.100.22.108:8000/static/DSS/images/section-hd-bg.jpg')!important;
}
</Style>

<div class="modal fade modal-wide" id="dss-modal" tabindex="-1" role="dialog" aria-hidden="true" data-url="">
    <div class="modal-dialog modal-xl" style="width:80%!important;">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal"
                        aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <label class="modal-title">DSS Details</label>
            </div>

            <div class="modal-body dss-modal-bg">

                <div class="pv-tab">
                    <ul id="rationale-tab" class="nav nav-tabs rxmain-container-header-label common-bg" role="tablist">
                        <li role="presentation" class="active">
                            <a href="#rationale" aria-controls="rationale" role="tab" data-toggle="tab" accesskey="1">Rationale</a>
                        </li>
                        <li role="presentation">
                            <a href="#detailed_history" id ="detailed_history_tab" aria-controls="detailed_history" role="tab" data-toggle="tab" accesskey="2">Detailed History</a>
                        </li>
                    </ul>
                </div>
                <div class="tab-content">
                    <div role="tabpanel" class="dss-table-margin tab-pane active" id="rationale">
                        <div class="row">
                            <div class="col-md-12 dss-shadow pr-0 active">
                                <table id="dssRationaleTable" class="mt-0 row-border hover no-shadow dss-border active" width="100%">
                                    <thead>
                                    <tr class="gray-section-hd-bgtrip1 common-bg">
                                        <td id="expand-colapse-all-rationale" class="double_down"></td>
                                        <th class="text-left col-min-150">PV Concept</th>
                                        <th class="t-center col-min-150" style="text-align:center!important;">Potential Signal</th>
                                        <th class="t-center col-min-150" style="text-align:center!important;">Score Confidence</th>
                                        <th class="text-left col-min-150">Rationale</th>
                                    </tr>
                                    </thead>
                                    <tbody>

                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                    <div id="detailed_history" class="dss-table-margin tab-pane fade" role="tabpanel">
                        <div class="row">
                            <div class="col-md-12">

                                <table id="dssDetailsTable" class="row-border hover no-shadow dss-border" width="100%">
                                    <thead id="tableHeadersPlaceholder">
                                    <tr class="gray-strip1 common-bg">
                                        <td id="expand-colapse-all-history" class="double_down"></td>
                                        <th class="">PV Concept</th>
                                        <g:each in="${dssDateRange}" var="dateRange">
                                            <th class="" data-field="${dateRange}">
                                                <div class='stacked-cell-center-top'>
                                                    ${dateRange}
                                                </div>
                                            </th>

                                        </g:each>
                                    </tr>
                                    </thead>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="row" style="margin-top:10px">
                    <div class="col-md-4"><label class="theme-font" style="padding-left:7px" id="current-date-range">Current Period: ${currentDateRangeDss} </label></div>
                    <div class="col-md-2"><label class="theme-font" id="newCount"></label></div>
                    <div class="col-md-3"><label class="theme-font" id="confidence"></label></div>
                    <div class="col-md-3"><label class="theme-font" id="rationale-disposition"></label></div>
                </div>

            </div>

            <div class="modal-footer">
                <button type="button" class="application_close_button btn"  data-dismiss="modal">Close</button>
            </div>
        </div>
    </div>
</div>