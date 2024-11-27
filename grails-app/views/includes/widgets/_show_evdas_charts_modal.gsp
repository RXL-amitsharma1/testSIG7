<div class="modal fade modal-xlg" data-backdrop="static" id="show-evdas-chart-modal" tabindex="-1" role="dialog" aria-hidden="true">
    <div class="modal-dialog modal-lg" role="document" style="width: 1200px">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
                <label class="modal-title">Trend Charts</label>
            </div>

            <div class="modal-body" style="height: 900px;">
                <div class="pv-loader-bg" id="chart-loader-1" style="display: none;">
                    <div class="loader">
                        <svg class="circular" viewBox="25 25 50 50">
                            <circle class="path" cx="50" cy="50" r="20" fill="none" stroke="#4f8ef2" stroke-width="2" stroke-miterlimit="10"/>
                        </svg>
                    </div>
                </div>
                <div class="row">

                        <div class="col-sm-6 col-md-6 col-lg-6">
                            <div id="evdas-count-by-status" class="chart-container"></div>
                        </div>
                        <div class="col-sm-6">
                            <div id="trend-div" style="width: 100%"></div>
                        </div>

                </div>
                <div class="row m-t-20">

                        <div class="col-sm-6 col-md-6 col-lg-6">
                            <div id="evdas-scores-by-status" class="chart-container"></div>
                        </div>
                        <div class="col-sm-6">
                            <div id="scores-div" style="width: 100%"></div>
                        </div>

                </div>
            </div>

            <div class="modal-footer">
                <div class="buttons ">
                    <button class="button btn btn-default" data-dismiss="modal" id="cancel-bt">Close</button>
                </div>
            </div>
        </div>
    </div>
</div>