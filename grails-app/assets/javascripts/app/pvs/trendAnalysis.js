//= require app/pvs/common/rx_common.js
//= require app/pvs/trend/trend

$(function () {

    var graph = null;

    var init_pe_table = function () {

        //Data table for the document modal window.
        var peTable = $("#peTable").DataTable({
            "language": {
                "url": "../assets/i18n/dataTables_" + userLocale + ".json"
            },
            initComplete: function (response) {
                showTrendCharts(response.aoData[0]._aData.id, response.aoData[0]._aData.dataSource, 1, "New Spontaneous Counts");

                $(".showTrend").on('click', function () {
                    var obj = $(this);
                    var alertId = obj.attr("data-id");
                    var alertType = obj.attr("data-type");
                    var chartType = obj.attr("data-chart-type");
                    var chartLabel = obj.html();
                    showTrendCharts(alertId, alertType, chartType, chartLabel);
                });
            },
            "ajax": {
                "url": peTableUrl,
                "dataSrc": ""
            },
            searching: true,
            "bLengthChange": true,
            "iDisplayLength": 10,
            "aaSorting": [[1, "desc"]],
            "aLengthMenu": [[10, 20, 50, -1], [10, 20, 50, "All"]],
            "aoColumns": [
                {
                    "mData": "dataSource",
                    "sWidth": "10%",
                    "mRender": function (data, type, row) {
                        if (row.dataSource == "EVDAS") {
                            return '<span>EVDAS</span>';
                        } else if (row.dataSource == "PVA") {
                            return '<span>PVA</span>';
                        } else if (row.dataSource == "FAERS") {
                            return '<span>FAERS</span>';
                        }else {
                            return '<span>-</span>';
                        }
                    }
                },
                {
                    "mData": "productName",
                    "sWidth": "20%"
                },
                {
                    "mData": "soc",
                    "sWidth": "10%"
                },
                {
                    "mData": "preferredTerm",
                    "sWidth": "20%"
                },
                {
                    "mData": "detectedDate",
                    "sWidth": "10%"
                },
                {
                    "mData" : "newSpon",
                    "mRender": function (data, type, row) {
                        if (row.dataSource == "eudra") {
                            return row.newSpon;
                        } else {
                            if (typeof row.newSponCount != "undefined" && row.newSponCount != null) {
                                return row.newSponCount;
                            } else {
                                return '-';
                            }
                        }
                    },
                    "sWidth": "15%",
                },
                {
                    "mData": "",
                    "sWidth": "15%",
                    "mRender": function (data, type, row) {
                        var actionButton = '<div class="btn-group dropdown" align="center"> \
                            <a class="btn btn-success btn-xs" href="#">Show Trend</a> \
                            <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                                <span class="caret"></span> \
                                <span class="sr-only">Toggle Dropdown</span> \
                            </button> \
                            <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                                <li role="presentation">\
                                    <a class="showTrend" role="menuitem" data-chart-type="1" data-id="' + row.id + '" data-type="' + row.dataSource + '" ' +
                            'href="#">New Spontaneous Counts</a>\
                    </li> \
                    <li role="presentation">\
                        <a class="showTrend" role="menuitem" data-chart-type="2" data-id="' + row.id + '" data-type="' + row.dataSource + '" ' +
                            'href="#">New Fatal Counts</a>\
                    </li> \
                    <li role="presentation">\
                        <a class="showTrend" role="menuitem" data-chart-type="3" data-id="' + row.id + '" data-type="' + row.dataSource + '" ' +
                            'href="#">New Serious Counts</a>\
                    </li> \
                    <li role="presentation">\
                        <a class="showTrend" role="menuitem" data-chart-type="4" data-id="' + row.id + '" data-type="' + row.dataSource + '" ' +
                            'href="#">New Literature Counts</a>\
                    </li> \
                </ul> \
            </div>';
                        return actionButton;
                    }
                }
            ],
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text()
            }]
        });
        //actionButton( '#peTable' );
    };

    init_pe_table();
    var showTrendCharts = function (alertId, alertType, chartType, chartLabel) {
        $("#trendChartLoadingIcon").removeClass("hide");
        $("#trendChartRow").addClass("hide");
        $("#trendDataRow").addClass("hide");
        signal.trend.showTrends(fetchTrendUrl, alertId, alertType, chartType, chartLabel, $("#trendFrequency").val(), $('#isFaers').val());
        $("#trendChartLoadingIcon").addClass("hide");
        $("#trendDataRow").removeClass("hide");
        $("#trendChartRow").removeClass("hide");
    };
});