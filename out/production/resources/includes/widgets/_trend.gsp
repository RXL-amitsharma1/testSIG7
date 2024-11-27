<div class="row hide trendNotification">

    <div class="alert alert-warning alert-dismissible" role="alert">
        <button type="button" class="close" data-dismiss="alert">
            <span aria-hidden="true">&times;</span>
            <span class="sr-only"><g:message code="default.button.close.label" /></span>
        </button>
        <span>There is not enough data to show trends.</span>
    </div>
</div>

<div class="row hide trendRow" id="trendChartRow">
    <div class="col-sm-12">
        <div class="rxmain-container">

            <div class="rxmain-container-inner">
                <div class="rxmain-container-row rxmain-container-header">
                    <label class="rxmain-container-header-label">
                        Trend Chart
                    </label>
                </div>
                <div class="rxmain-container-content">
                    <div id="stock_div" style="width: 100%"></div>
                </div>
            </div>
        </div>
    </div>
</div>
<br/>
<br/>
<div class="row hide trendRow" id="trendDataRow">
    <div class="col-sm-12">
        <div class="rxmain-container">

            <div class="rxmain-container-inner">
                <div class="rxmain-container-row rxmain-container-header">
                    <label class="rxmain-container-header-label">
                        Trend Data
                    </label>
                </div>
                <div class="rxmain-container-content">
                    <div id="trend-div" style="width: 100%"></div>

                </div>
            </div>
        </div>
    </div>
</div>
<div class="hide" id="trendChartLoadingIcon">
    <i class="fa fa-refresh fa-spin fa-2x fa-fw"></i>
    <span class="sr-only hide">Loading...</span>
</div>