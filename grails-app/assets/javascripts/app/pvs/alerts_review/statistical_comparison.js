//= require vendorUi/highcharts/highcharts-10.3.3/highcharts
//= require vendorUi/highcharts/highcharts-10.3.3/highcharts-3d
//= require vendorUi/highcharts/highcharts-10.3.3/highcharts-more
//= require vendorUi/highcharts/themes/grid-rx
// = require app/pvs/common/rx_common.js
//= require app/pvs/common/rx_alert_utils.js
//= require app/pvs/common/rx_list_utils.js
//= require app/pvs/activity/activities.js
//= require app/pvs/actions/actions.js
//= require app/pvs/common/rx_handlebar_ext.js
//= require app/pvs/caseDrillDown/caseDrillDown.js
//= require app/pvs/alertComments/alertComments.js

var applicationName = signal.utils.getQueryString("appName").replace(/\+/g,' ');
var applicationLabel = "Statistical Comparison";
var table;

$(function () {
    var detail_table;
    var activities_table;
    var executedIdList = [];

    var signals = $("#signals").val();

    $('#alert-list-filter-apply-bt').on('click', function (evt) {
        evt.preventDefault();
        var filterParams = $('#alert-list-filter').serialize();
        var theDataTable = $('#alertsDetailsTable').DataTable();
        var newUrl = dataUrl + "&" + filterParams + "&filterApplied=" + true;
        theDataTable.ajax.url(newUrl).load()
    });

    var init = function () {

        $(document).on('click', '.show-chart-icon', function () {
            var id = $(this).attr("data-value");
            var statsId = $(this).attr("data-id");
            var showEvdasChart = $("#show-evdas-chart-modal");
            showEvdasChart.modal("show");
            showEvdasChart.find('.modal-header').text("Trend Charts");

            $.ajax({
                url: 'showComparisionCharts?id=' + id + '&statsId=' + statsId + '&appName=' + appName,
            })
                .done(function (result) {
                    evdas_chart("#evdas-count-by-status", result.xAxisTitle,
                        [{
                            name: "Spon(PVA)",
                            data: result.sponCount
                        },{
                            name: "EV(EVDAS)",
                            data: result.newEv
                        }, {
                            name: "Serious(PVA)",
                            data: result.seriousCount
                        }, {
                            name: "Serious(EVDAS)",
                            data: result.seriousEv
                        }, {
                            name: "Fatal(PVA)",
                            data: result.fatalCount
                        }, {
                            name: "Fatal(EVDAS)",
                            data: result.evFatalCount
                        }],
                        "Counts", 'Counts'
                    );

                    evdas_chart("#evdas-scores-by-status", result.xAxisTitle,
                        [{
                            name: "PRR(PVA)",
                            data: result.prrValue
                        },{
                            name:"ROR(PVA)",
                            data: result.rorValue
                        }, {
                            name: "ROR(EVDAS)",
                            data: result.rorEv
                        }],
                        "Scores", 'Scores'
                    );

                    var statsCountTable = [];

                    var evEnabled = result.evEnabled
                    var countLength = result.sponCount.length == 0 ? result.newEv.length : result.sponCount.length;

                    for (var index = 0; index < countLength; index++) {
                        var obj = {
                            "xAxisTitle": result.xAxisTitle[index],
                            "sponCount": result.sponCount[index] ? result.sponCount[index] : 0,
                            "seriousCount": result.seriousCount[index] ? result.seriousCount[index] : 0,
                            "fatalCount": result.fatalCount[index] ? result.fatalCount[index] : 0
                        };

                        if (evEnabled) {
                            obj.newEV = result.newEv[index];
                            obj.evSerious = result.seriousEv[index];
                            obj.evFatal = result.evFatalCount[index];
                        }
                        statsCountTable.push(obj);
                    }
                    var trend_data_content = signal.utils.render('statsCountTable', {trendData: statsCountTable, evEnabled:evEnabled});
                    $("#trend-div").html(trend_data_content);

                    var statsScoresTable = [];
                    for (var index = 0; index < countLength; index++) {
                        var obj = {
                            "xAxisTitle": result.xAxisTitle[index],
                            "prrValue": result.prrValue[index] ? result.prrValue[index] : 0,
                            "rorValue": result.rorValue[index] ? result.rorValue[index] : 0
                        };

                        if(evEnabled){
                            obj.rorEv = result.rorEv[index];
                        }
                        statsScoresTable.push(obj);
                    }
                    var score_data_content = signal.utils.render('statsScoresTable', {trendData: statsScoresTable, evEnabled:evEnabled});
                    $("#scores-div").html(score_data_content);
                })
        });
        detail_table = init_details_table();

        $('i#copySelection').on('click', function () {
            showAllSelectedCaseNumbers()
        });

        $(document).on('click', 'input#select-all', function () {
            $(".copy-select").prop('checked', this.checked)
        })
    };

    function getRadios(toolbar) {
        var radios = '<div class="">';
        radios = radios + '<label class="no-bold add-cursor " white-space="nowrap"><input id="showAll" type="radio"  name="relatedResults" checked/> ' + $.i18n._('All') + '</label>';
        radios = radios + '&nbsp;&nbsp';
        // radios = radios + '<label class="no-bold add-cursor " white-space="nowrap">&nbsp<input id="showSpecialPE" type="radio"  name="relatedResults" />&nbsp&nbsp'+ $.i18n._('Special PE')+'</label>'
        radios = radios + '&nbsp;&nbsp;&nbsp;<a href="' + statComparisonUrl + '" target="_blank" class="create btn btn-success statistical-comparison" id="statistical-comparison" style="float:right">Statistical Comparison</a>';
        radios = $(radios);
        $(toolbar).append(radios);
    }


    var init_details_table = function () {
        table = $('#statsTable').DataTable({


            "language": {
                "url": "../assets/i18n/dataTables_" + userLocale + ".json"
            },
            "ajax": {
                "url": dataUrl,
                "dataSrc": function(result) {
                    if(result.argusEndDate) {
                        $('div.stacked-cell-center-bottom:contains("(ARGUS)")').html('(ARGUS)<div class="stacked-cell-center-bottom">(' + result.argusEndDate + ')</div>');
                    }
                    if(result.evdasEndDate) {
                        $('div.stacked-cell-center-bottom:contains("(EVDAS)")').html('(EVDAS)<div class="stacked-cell-center-bottom">(' + result.evdasEndDate + ')</div>');
                    }
                    return result.statsData
                }

            },
            "bLengthChange": true,
            "iDisplayLength": 50,
            "bProcessing": true,
            "bServerSide": true,
            "searching": false,
            "MSG_LOADING": '',
            "oLanguage": {
                "oPaginate": {
                    "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                    "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                    "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                    "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
                },
            },
            drawCallback:function (settings) {
                if(typeof hasReviewerAccess !== "undefined" && !hasReviewerAccess) {
                    $(".changeDisposition").removeAttr("data-target");
                    $(".changePriority").removeAttr("data-target");
                }
                if(typeof hasSignalCreationAccessAccess !== "undefined" && !hasSignalCreationAccessAccess) {
                    $(".changeDisposition[data-validated-confirmed=true]").removeAttr("data-target");
                }
            },
            "aLengthMenu": [[50, 100, 200, 500], [50, 100, 200, 500]],
            "aoColumns": [
                {
                    "mData": "productName",
                    "className": "pvi-col-md",
                    "mRender": function (data, type, row) {
                        if (row.isSpecialPE) {
                            return "<span title='Special Product | Events' style='color:red' data-field ='productName' data-id='" + row.productName + "'>" + (row.productName) + "</span>"
                        } else {
                            return "<span data-field ='productName' data-id='" + row.productName + "'>" + (row.productName) + "</span>"
                        }
                    },
                    "className": 'dt-center pvi-col-sm'
                },
                {
                    "mData": "soc",
                    "className": 'pvi-col-md'
                },
                {
                    "mData": "pt",
                    "mRender": function (data, type, row) {
                        if (row.isSpecialPE) {
                            return "<span title='Special Product | Events' style='color:red' data-field ='eventName' data-id='" + row.pt + "'>" + (row.pt) + "</span>"
                        } else {
                            return "<span data-field='eventName' data-id='" + row.pt + "'>" + (row.pt) + "</span>"
                        }
                    },
                    "className": 'dt-center pvi-col-md'
                },
                {
                    "mData": "",
                    "mRender": function(data, type, row) {
                        var pvaNewSponCount = row.pvaNewSponCount ? row.pvaNewSponCount : "0";
                        var pvaCumSponCount = row.pvaCumSponCount ? row.pvaCumSponCount : "0";
                        return signal.utils.stacked(pvaNewSponCount, pvaCumSponCount);
                    },
                    className: 'dt-center',
                    "sortable": false
                },
                {
                    "mData": "",
                    "mRender": function(data, type, row) {
                        var pvaNewSponCount = row.faersNewSponCount ? row.faersNewSponCount : "0";
                        var pvaCumSponCount = row.faersCumSponCount ? row.faersCumSponCount : "0";
                        return signal.utils.stacked(pvaNewSponCount, pvaCumSponCount);
                    },
                    className: 'dt-center',
                    "sortable": false
                },
                {
                    "mData": "",
                    "mRender": function (data, type, row) {
                        var evdasNewSponCount = row.newEv ? row.newEv : "0";
                        var evdasCumSponCount = row.totalEv ? row.totalEv : "0";
                        return signal.utils.stacked(evdasNewSponCount, evdasCumSponCount);
                    },
                    className: '',
                    "sortable": false
                },
                {
                    "mData": "",
                    "mRender": function(data, type, row) {
                        var pvaPrrValue = row.pvaPrrValue ? parseFloat(row.pvaPrrValue).toFixed(2) : "0";
                        var pvaRorrValue = row.pvaRorrValue ? parseFloat(row.pvaRorrValue).toFixed(2) : "0";
                        return signal.utils.stacked(pvaPrrValue, pvaRorrValue);
                    },
                    className: '',
                    "sortable": false
                },
                {
                    "mData": "",
                    "mRender": function(data, type, row) {
                        var pvaPrrValue = row.faersPrrValue ? parseFloat(row.faersPrrValue ).toFixed(2) : "0";
                        var pvaRorrValue = row.faersRorrValue ? parseFloat(row.faersRorrValue).toFixed(2) : "0";
                        return signal.utils.stacked(pvaPrrValue, pvaRorrValue);
                    },
                    className: 'dt-center',
                    "sortable": false
                },
                {
                    "mData": "",
                    "mRender": function (data, type, row) {
                        var evdasRorrValue = row.ror ? parseFloat(row.ror).toFixed(2) : "0";
                        return signal.utils.stacked("", evdasRorrValue);
                    },
                    className: '',
                    "sortable": false
                },
                {
                    "mData": "",
                    "mRender": function(data, type, row) {
                        var faersPrrValue = row.pvaEbo5Value ? row.pvaEbo5Value : "0";
                        return faersPrrValue;
                    },
                    className: '',
                    "sortable": false
                },
                {
                    "mData": "",
                    "mRender": function(data, type, row) {
                        var faersPrrValue = row.faersEbo5Value ? row.faersEbo5Value : "0";
                        return faersPrrValue;
                    },
                    className: 'dt-center',
                    "sortable": false
                },
                {
                    "mData": "currentDisposition",
                    "mRender": function (data, type, row) {
                        return signal.utils.render('disposition_dss3', {
                            allowedDisposition: dispositionIncomingOutgoingMap[row.disposition],
                            currentDisposition: row.disposition,
                            forceJustification: forceJustification,
                            isValidationStateAchieved: row.isValidationStateAchieved,
                            id:row.currentDispositionId
                        });
                    },
                    'className': 'col-max-300 dispositionAction',
                    "sortable": false
                },{
                    "mData": "",
                    "mRender": function (data, type, row) {
                        return '<span tabindex="0" style="cursor: pointer;font-size: 125%;" class="show-chart-icon fa fa-line-chart" data-value="' + row.id + '" data-id="' + row.statsId + '"></span>';
                    },
                    "sortable": false
                }

            ],
            scrollX:true,
            scrollY:'50vh',
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }]
        });

        return table
    };

    var showAllSelectedCaseNumbers = function () {
        $('#copyCaseNumberModel').modal({
            show: true
        });

        var numbers = _.map($('input.copy-select:checked').parent().parent().find('td:nth-child(5)'), function (it) {
            return $(it).text()
        });
        $("#caseNumbers").text(numbers);
    };

    $('#exportTypes a[href]').on('click', function (e) {
        var clickedURL = e.currentTarget.href;
        var ids = [];
        var newUrl;
        $('input.copy-select:checked').map(function () {
            ids.push($(this).attr('data-id'));
        });
        if (ids.length > 0) {
            newUrl = clickedURL + "&selectedCases=" + ids;
            window.location.href = newUrl;
            return false
        }
        if (ids.length === 0) {
            $('input.copy-select').map(function () {
                ids.push($(this).attr('data-id'));
            });
            newUrl = clickedURL + "&selectedCases=" + ids;
            window.location.href = newUrl;
            return false
        }
    });
    init();
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
});