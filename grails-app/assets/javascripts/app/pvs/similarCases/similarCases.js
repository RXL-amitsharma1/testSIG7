//= require highcharts/themes/grid-rx

var signal = signal || {};

signal.similarCaseTable = (function() {

    var similarCaseTable, event_outcome_chart = null;

    var init_similar_case_table = function (url) {

        clear_similar_case_table();
        clear_event_outcome_chart();

        var dataUrl = url +"?caseNumber=" + $("#caseNumberInfo").val() +
            "&currentVersion=" + $("#caseCurrentVersion").val() +
            "&eventVal=" + $("#eventCodeVal").val() +
            "&eventType=" + $("#eventCode").val() +
            "&executedConfigId="+ $('#similarCaseModal').find("#executedConfigId").val();

        //Data table for the document modal window.
        similarCaseTable = $("#similarCaseModalTable").DataTable({
            language: {
                "search": "",
            },

            "ajax": {
                "url": dataUrl,
                "dataSrc": ""
            },
            searching: true,
            "bLengthChange": true,
            "iDisplayLength": 10,
            "aLengthMenu": [[10, 20, 50, -1], [10, 20, 50, "All"]],
            "aoColumns": [
                {
                    "mData": "",
                    "mRender": function (data, type, row) {
                        return '<a target="_blank" href="'+ (caseDetailUrl + '?' + signal.utils.composeParams({caseNumber: row.caseNumber, version: row.caseVersion,followUpNumber: null,
                                alertId: row.alertId}))+'">'+"<span data-field ='caseNumber' data-id='" + row.caseNumber + "'>" +
                            (row.caseNumber) + "</span><span data-field ='caseVersion' data-id='" + row.caseVersion + "'>" +
                            "</span>(<span data-field ='followUpNumber' data-id='" + row.followUpNumber + "'>" +
                            (row.followUpNumber) + "</span>)"+'</a>'
                    },
                    'className': 'dt-center'
                },
                {
                    "mData": "productName",
                    'className': 'dt-center'
                },
                {
                    "mData": "pt",
                    'className': 'dt-center'
                },
                {
                    "mData": "hcp",
                    "mRender": function(data, type, row) {
                        var hcpVal = "";
                        if (row.hcp == 1) {
                            hcpVal = "Yes"
                        } else if(row.hcp == 0) {
                            hcpVal = "No"
                        }
                        return "<span>"+hcpVal+"</span>"
                    },
                    'className': 'dt-center'
                },
                {
                    "mData": "",
                    "mRender": function(data, type, row) {
                        return signal.utils.stacked(
                            calculateSeriousness(row.seriousness)+"/"+calculateListedness(row.listedness)+"/"+calculateDetCausality(row.determinedCausality),
                            row.outcome ? row.outcome : "-")
                    },
                    'className': 'dt-center'
                },
                {
                    "mData" : "disposition",
                    'className': 'dt-center'
                },
                {
                    "mData" : "priority",
                    'className': 'dt-center'
                },
                {
                    "mData" : "assignedTo",
                    'className': 'dt-center'
                }
            ],
            "initComplete": function(oSettings, json) {
                if (json.length > 1) {
                    var eventOutcomeObj = {};
                    var eventOutcome = [];
                    for (var i = 0; i < json.length; i++) {
                        var object = json[i];
                        var outcome = object.outcome;
                        var outcomeFromEvent = eventOutcomeObj[outcome];
                        if (typeof outcomeFromEvent != "undefined") {
                            eventOutcomeObj[outcome] = outcomeFromEvent + 1
                        } else {
                            eventOutcomeObj[outcome] = 1;
                        }
                    }

                    for (var property in eventOutcomeObj) {
                        if (property != "null") {
                            eventOutcome.push({
                                name : property,
                                y : eventOutcomeObj[property]
                            });
                        } else {
                            eventOutcome.push({
                                name : "Unknown",
                                y : eventOutcomeObj[property]
                            });
                        }
                    }

                    if (eventOutcome.length > 0) {
                        create_event_outcome_chart(eventOutcome);
                    }
                }
            },
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }]
        });
        return similarCaseTable
    };

    var create_event_outcome_chart = function(eventOutcomeObj) {

        // Build the chart
        event_outcome_chart = $('.event-outcome-pie').highcharts({
            chart: {
                plotBackgroundColor: null,
                plotBorderWidth: null,
                plotShadow: false,
                type: 'pie'
            },
            credits: {
                enabled: false
            },
            title: {
                text: 'Cases By Event Outcome'
            },
            credits: {
                enabled: false
            },
            tooltip: {
                formatter: function() {
                    return ''+
                        this.y + ' cases';
                }
            },
            plotOptions: {
                pie: {
                    allowPointSelect: true,
                    cursor: 'pointer',
                    dataLabels: {
                        enabled: false
                    },
                    showInLegend: true
                }
            },
            series: [{
                name: 'Cases',
                colorByPoint: true,
                innerSize: '40%',
                data: eventOutcomeObj
            }]
        });

    };

    var clear_event_outcome_chart = function() {
        var chartObj = $('.event-outcome-pie').highcharts();
        if (typeof chartObj != "undefined" && chartObj != null) {
            chartObj.destroy()
        }
    };

    var clear_similar_case_table = function() {
        if (typeof similarCaseTable != "undefined" && similarCaseTable != null) {
            similarCaseTable.destroy()
        }
    };

    var calculateSeriousness = function(serVal) {
        var calculatedSeriousness = "";
        switch(serVal) {
            case "Serious":
                calculatedSeriousness = "Y";
                break;
            case "Non Serious":
                calculatedSeriousness = "N";
                break;
            case "Unknown":
                calculatedSeriousness = "U";
                break;
            default:
                calculatedSeriousness = "-"
        }
        return calculatedSeriousness
    };

    var calculateListedness = function(listVal) {
        var calculatedListedness = "";
        switch(listVal) {
            case "Listed":
                calculatedListedness = "N";
                break;
            case "Unlisted":
                calculatedListedness = "Y";
                break;
            case "Unknown":
                calculatedListedness = "U";
                break;
            default:
                calculatedListedness = "-"
        }
        return calculatedListedness
    };

    var calculateDetCausality = function(detCausalityVal) {
        var calculatedReportedNess = "";
        switch (detCausalityVal) {
            case "1":
                calculatedReportedNess = "Y";
                break;
            case "0":
                calculatedReportedNess = "N";
                break;
            default:
                calculatedReportedNess = "-"
        }
        return calculatedReportedNess
    };

    return {
        init_similar_case_table: init_similar_case_table
    }
})();
