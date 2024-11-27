DATE_FMT_TZ = "YYYY-MM-DD";
EXECUTION_STATUS_ENUM = {
    SCHEDULED: 'Scheduled',
    GENERATING: 'Generating',
    DELIVERING: 'Delivering',
    COMPLETED: 'Completed',
    ERROR: 'Error',
    WARN: 'Warn'
};

$(function () {

    var executionStatusTable = $('#testCaseExecutionStatus').DataTable({
        "ajax": {
            "url": executionStatusUrl,
            "dataSrc": "aaData",
        },
        "order": [[1, "desc"]],
        "aoColumns": [
            {
                "mData": "name",
                "className": 'col-min-150 col-max-500 cell-break',
                "mRender": function (data, type, row) {
                    if (row.nodeName != null)
                        return '<span tabindex="0" data-placement="right"  data-toggle="tooltip" style="cursor:pointer;" class="popoverMessage" data-content="Alert ran on: ' + row.nodeName + '">' + row.name + '</span>';
                    else
                        return row.name;
                }
            },
            {
                "mData": "alertType"
            },
            {
                "mData": "product",
                "className": 'col-min-150'
            },
            {
                "mData": "frequency",
                "orderable": false
            },
            {
                "mData": "runDate",
                "className": 'col-min-150',
                "aTargets": ["runDate"],
                "mRender": function (data, type, row) {
                    var runDate = new Date(data);
                    return moment(runDate).tz(userTimeZone).format('lll');
                }
            },
            {
                "mData": "executionTime",
                "className": 'col-min-150',
                "sortable": false,
                "aTargets": ["executionTime"],
                "mRender": function (data, type, row) {
                    if (type === 'display') {
                        var time = '';
                        time = getTimeInFormat(data)
                        return time
                    }
                    return data
                }


            },
            {
                "mData": "owner",
                "className": 'col-min-150'
            },
            {
                "mData": "executionStatus",
                "sClass": "dataTableColumnCenter  text-center",
                "aTargets": ["executionStatus"],
                "sortable": false,
                "mRender": function (data, type, row) {
                    return createStatusHtml(row.executionStatus, row, "", false)
                }
            },
            {
                "mData": "peCount",
                "className": 'col-min-150'
            }
        ],
        "scrollX": true,
        "scrollY": '50vh',
        columnDefs: [{
            "targets": '_all',
            "render": $.fn.dataTable.render.text(),
            orderSequence: ['desc', 'asc']
        }]
    });

    setInterval(function () {
        executionStatusTable.ajax.reload();
    }, 10000);

    $('#delete-test-alerts').on('click', function () {

        $.ajax({
            type: "POST",
            url: "/signal/testSignalRest/deleteTestAlerts",
        })
            .done(function (data) {
                console.log(data.message + " " + data.code)
            });
    });

    var passedMap = JSON.parse(passedCountMap);
    var failedMap = JSON.parse(failedCountMap);


    var chart = {
        type: 'column'
    };
    var title = {
        text: 'Test Case Status'
    };
    var subtitle = {
        text: ''
    };
    var xAxis = {
        categories: ['Aggregate Review', 'Individual Case Review', 'Aggregate Adhoc Review', 'Individual Adhoc Case Review'],
        title: {
            text: null
        }
    };
    var yAxis = {
        min: 0,
        title: {
            text: '',
            align: 'high'
        },
        labels: {
            overflow: 'justify'
        }
    };
    var tooltip = {
        valueSuffix: ''
    };
    var plotOptions = {
        bar: {
            dataLabels: {
                enabled: true
            }
        },
        series: {
            stacking: 'normal'
        }
    };
    var legend = {
        layout: 'vertical',
        align: 'right',
        verticalAlign: 'top',
        x: -40,
        y: 50,
        floating: true,
        borderWidth: 1,

        backgroundColor: (
            (Highcharts.theme && Highcharts.theme.legendBackgroundColor)
            || '#FFFFFF'),
        shadow: true
    };
    var credits = {
        enabled: false
    };
    var series = [
        {
            name: 'Test Passed',
            data: [passedMap.aggAlerts, passedMap.icrAlerts, passedMap.aggAdhocAlerts, passedMap.icrAdhocAlerts]
        },
        {
            name: 'Test Failed',
            data: [failedMap.aggAlerts - passedMap.aggAlerts, failedMap.icrAlerts - passedMap.icrAlerts, failedMap.aggAdhocAlerts - passedMap.aggAdhocAlerts, failedMap.icrAdhocAlerts - passedMap.icrAdhocAlerts]
        }
    ];

    var json = {};
    json.chart = chart;
    json.title = title;
    json.subtitle = subtitle;
    json.tooltip = tooltip;
    json.xAxis = xAxis;
    json.yAxis = yAxis;
    json.series = series;
    json.plotOptions = plotOptions;
    json.legend = legend;
    json.credits = credits;
    $('#chart-container').highcharts(json);

});