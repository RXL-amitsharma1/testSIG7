$(function () {

    var signalId = $("#signalId").val();
    var isTopic = $('#isTopic').val();

    var peAnalysisTable = null;

    var evdas_chart = function (chartId, categories, series, yAxisTitle, chartTitle) {
        $(chartId).highcharts({
            chart: {
                type: 'line'
            },
            xAxis: {
                categories: categories,
            },
            yAxis: {
                plotLines: [{
                    value: 0,
                    width: 1,
                    color: '#808080'
                }],
                title: {
                    text: yAxisTitle
                }
            },
            series: series,
            credits: {
                enabled: false
            },
            title: {
                text: chartTitle
            }
        });
    }

    var ptBadge = function (ime, dme, ei, sm) {
        var badgeSpan = "";
        if (ime == "true") {
            badgeSpan += '<span style="width:37px" class="badge badge-info">IME</span>'
        }
        if (dme == "true") {
            badgeSpan += '<span style="width:37px" class="badge badge-purple">DME</span>'
        }
        if (ei == "true") {
            badgeSpan += '<span style="width:37px" class="badge badge-success">EI</span>'
        }
        if (sm == "true") {
            badgeSpan += '<span style="width:37px" class="badge badge-warning">SM</span>'
        }
        return badgeSpan
    };
    var trends = function (trends) {
        if (trends == "Positive") {
            return "<span style = 'color:red'>(+)</span>";
        } else if(trends == "Negative") {
            return "<span style = 'color:green'>(-)</span>";
        }else
            return "<span>-</span>";
    };

    var init_peAnalysis_table = function () {

        if (typeof peAnalysisTable != "undefined" && peAnalysisTable != null) {
            peAnalysisTable.destroy()
        }


        //Data table for the document modal window.
        var peAnalysisTable = $("#peAnalysisTable").DataTable({
            "language": {
                "url": "../assets/i18n/dataTables_" + userLocale + ".json"
            },
            initComplete: function () {
                $('.show-chart-icon').on('click', function () {
                    var alertId = $(this).attr("data-value");
                    var showEvdasChart = $("#show-evdas-chart-modal");
                    showEvdasChart.modal("show");

                    $.ajax({
                        url: '/signal/aggregateCaseAlert/showCharts?alertId=' + alertId,
                    })
                        .done(function (result) {

                            evdas_chart("#evdas-count-by-status", result.xAxisTitle,
                                [{
                                    name: "Study",
                                    data: result.studyCount
                                }, {
                                    name: "Spon",
                                    data: result.sponCount
                                }, {
                                    name: "Serious",
                                    data: result.seriousCount
                                }, {
                                    name: "Fatal",
                                    data: result.fatalCount
                                }],
                                "Counts", 'Counts'
                            );

                            evdas_chart("#evdas-scores-by-status", result.xAxisTitle,
                                [{
                                    name: "PRR",
                                    data: result.prrValue
                                }, {
                                    name: "ROR",
                                    data: result.rorValue
                                }, {
                                    name: "EBGM",
                                    data: result.ebgmValue
                                }],
                                "Scores", 'Scores'
                            );
                            var countTable = [];
                            for (var index = 0; index < result.xAxisTitle.length; index++) {
                                if( isVaersEnabled && !isPva && !isEvdas){
                                    obj = {
                                        "xAxisTitle": result.xAxisTitle[index],
                                        "seriousCount": result.seriousCount[index],
                                        "fatalCount": result.fatalCount[index]
                                    };
                                }else{
                                    obj = {
                                        "xAxisTitle": result.xAxisTitle[index],
                                        "studyCount": result.studyCount[index],
                                        "sponCount": result.sponCount[index],
                                        "seriousCount": result.seriousCount[index],
                                        "fatalCount": result.fatalCount[index]
                                    };
                                }
                                countTable.push(obj);
                            }
                            var trend_data_content = signal.utils.render('counts_table_61', {trendData: countTable});
                            $("#trend-div").html(trend_data_content);

                            var scoresTable = [];
                            for (var index = 0; index < result.xAxisTitle.length; index++) {
                                var obj = {
                                    "xAxisTitle": result.xAxisTitle[index],
                                    "prrValue": result.prrValue[index],
                                    "rorValue": result.rorValue[index],
                                    "ebgmValue": result.ebgmValue[index]
                                };
                                scoresTable.push(obj);
                            }
                            var score_data_content = signal.utils.render('scoresTable', {trendData: scoresTable});
                            $("#scores-div").html(score_data_content);
                        })

                });


            },
            "ajax": {
                "url": '/signal/validatedSignal/peAnalysis?id=' + signalId + '&isTopic=' + isTopic,
                "dataSrc": ""
            },
            searching: false,
            "bLengthChange": false,
            "bPaginate": false,
            "bInfo": false,
            "aoColumns": [
                {
                    "mData": "",
                    "mRender": function (data, type, row) {
                        return '<span>' + row.dataSource + '</span>';
                    }
                },
                {
                    "mData": "productName"
                },
                {
                    "mData": "preferredTerm"
                },
                {
                    "mData": "",
                    "mRender": function (data, type, row) {
                        return ptBadge(row.ime, row.dme, row.ei, row.sm);
                    }
                },
                {
                    "mData": "",
                    "mRender": function (data, type, row) {
                        return signal.utils.stacked('<span>' + row.newCount + '</span>', '<span>' + row.totalCount + '</span>');
                    }
                },
                {
                    "mData": "",
                    "mRender": function (data, type, row) {
                        return signal.utils.stacked('<span>' + row.newFatal + '</span>', '<span>' + row.totalFatal + '</span>');
                    }
                },
                {
                    "mData": "sdr"
                },
                {
                    "mData": "",
                    "mRender": function (data, type, row) {
                        return trends(row.trend);
                    }
                },
                {
                    "mData": "",
                    "mRender": function (data, type, row) {
                        return signal.utils.stacked('<span>' + row.listed + '</span>', '<span>' + row.serious + '</span>');
                    }
                },
                {
                    "mData": "priority"
                },
                {
                    "mData": "",
                    "mRender": function (data, type, row) {
                        return signal.utils.stacked(trends(row.prrTrend), trends(row.ebgmTrend));
                    }
                },
                {
                    "mData": "",
                    "mRender": function (data, type, row) {
                        return '<span tabindex="0" style="cursor: pointer;font-size: 125%;" class="show-chart-icon fa fa-line-chart" data-value="' + row.id + '"></span>';
                    }
                }
            ],

            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }]
        })
    };

    init_peAnalysis_table();
});