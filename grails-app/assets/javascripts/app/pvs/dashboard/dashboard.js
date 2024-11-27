//= require vendorUi/highcharts/highcharts-10.3.3/highcharts
//= require vendorUi/highcharts/highcharts-10.3.3/highcharts-3d
//= require vendorUi/highcharts/highcharts-10.3.3/highcharts-more
//= require vendorUi/highcharts/themes/grid-rx
//= require app/pvs/common/rx_common
//= require app/pvs/common/rx_handlebar_ext
//= require app/pvs/dashboard/pvs-link-widget
//= require vendorUi/counterup/jquery.counterup.min
//= require vendorUi/jquery/jquery.nicescroll

var triggeredAlertTable;
var triggeredSignalTable;
var triggeredActionsTable;
$(function () {
    rowDraggable('#refrences');
    // Added to check if the event handler is already attached to fix PVS-63998
    if (!$._data(document, 'events') || !($._data(document, 'events')['click'] && clickHandlerExists())) {
        // Attach the event handler only if it's not already attached
        $(document).on("click", ".check-action-access", function (e) {
            if (typeof userViewAccessMap !== "undefined" && !userViewAccessMap[$(this).data("type")]) {
                e.preventDefault();
                $.Notification.notify('warning', 'top right', "Warning", "You don't have access to perform this action", {autoHideDelay: 5000});
            }
        });
    }

function clickHandlerExists() {
    if ($._data(document, 'events')['click']) {
        var handlers = $._data(document, 'events')['click'];
        for (var i = 0; i < handlers.length; i++) {
            if (handlers[i].selector === ".check-action-access") {
                return true;
            }
        }
    }
    return false;
}

    Highcharts.setOptions({
        lang: {
            noData: "No Data available"
        }
    });

    $(".nicescroll").niceScroll({cursorcolor: '#98a6ad', cursorwidth: '6px', cursorborderradius: '5px'});

    $(document).on('click', '.remove-alert', function (e) {
        e.preventDefault();
        var currRow = $(this).closest('tr');
        var currRowIndex = $(this).closest('tr').index();
        var currRowData = triggeredAlertTable.rows(currRow).data()[0];
        $.ajax({
            url: deleteTriggeredAlert,
            type: "POST",
            async:false,
            data: {id: currRowData.id, type: currRowData.type}
        }).done(function (data) {
            if (data.status) {
                $.Notification.notify('success', 'top right', "Success", "Alert removed successfully", {autoHideDelay: 5000});
                triggeredAlertTable.ajax.reload();
            } else {
                $.Notification.notify('error', 'top right', "Error", data.message, {autoHideDelay: 5000});
            }
        });
    });

    $(".close-widget-inbox, .pv-dash-inbox-icon").on("click", function () {
        var isVisible = $(".sidebarDashboard").is(":visible");

        if (isVisible) {
            $(".sidebarDashboard").hide();
            $(".pv-dash-inbox-icon").removeClass("hide");
            $(".sidebarDashboard").removeClass("col-sm-12");
        } else {
            $(".sidebarDashboard").show();
            $(".pv-dash-inbox-icon").addClass("hide");
            $(".sidebarDashboard").addClass("col-sm-12");
        }

        updateDashboardWidgetConfigForInbox();

        $('.chart-container').each(function () {
            var chartContainer = $(this);
            if (chartContainer.length) {
                if (typeof chartContainer.highcharts() != "undefined" && chartContainer.highcharts() != null) {
                    chartContainer.highcharts().setSize(chartContainer.innerWidth(), chartContainer.innerHeight());
                }
            }
        });
        persistDashboardWidgetConfig();
    });

    $(window).on("resize", function () {
        $(".highcharts-container").attr("width", "100%");
    });

    setFilterDropdown("alertDashboard", $('#alertsFilterDashboard'));
    $('#alertsFilterDashboard').on('change', function() {
        sessionStorage.setItem("alertDashboard", $('#alertsFilterDashboard').val());
        $("#assignedTriggeredAlerts").DataTable().ajax.reload();
    });
    if(!("signalDashboard" in sessionStorage)) {
        sessionStorage.setItem("signalDashboard", "AssignToMe_"+userId);
    }
    if(!("alertDashboard" in sessionStorage)) { // added as part of PVS-72948 to auto select  “Assign to Me"
        sessionStorage.setItem("alertDashboard", "AssignToMe_" + userId);
    }
    setFilterDropdown("signalDashboard", $('#signalsFilter'));
    setFilterDropdown("alertDashboard", $('#alertsFilterDashboard'));
    $('#signalsFilter').on('change', function() {
        sessionStorage.setItem("signalDashboard", $('#signalsFilter').val());
        $("#assignedSignalTable").DataTable().ajax.reload();
    });
    $('#alertsFilter').on('change', function() {
        sessionStorage.setItem("alertDashboard", $('#alertsFilterDashboard').val());
        $("#assignedTriggeredAlerts").DataTable().ajax.reload();
    });
    initDashboardWidgetsData();

    var _searchTimer = null
    $('#custom-search-alert-wd').on("keyup", function (){
        clearInterval(_searchTimer)
        _searchTimer = setInterval(function (){
            triggeredAlertTable.search($('#custom-search-alert-wd').val()).draw() ;
            clearInterval(_searchTimer)
        },1500)

    });
    $('#custom-search-signal-wd').on("keyup", function (){
        clearInterval(_searchTimer)
        _searchTimer = setInterval(function (){
            triggeredSignalTable.search($('#custom-search-signal-wd').val()).draw() ;
            clearInterval(_searchTimer)
        },1500)

    });


    $('#custom-search-action-item-wd').on("keyup", function (){
        triggeredActionsTable.search($(this).val()).draw();
    })

    triggerResizeForAll()

});

    var setFilterDropdown = async function (alertType, selector) {
    let savedValues = sessionStorage.getItem(alertType);
    let fetchUsersAndGroupsUrl = "user/searchUsersAndGroupsForFilterAlertsAndSignals";
    let retainedObject=[];
    if(savedValues!=null && savedValues!="null" && savedValues!="") {
        $.ajax({url: fetchUsersAndGroupsUrl,async:true}).done(function (result) {
            savedValues?.split(",").forEach(function (i) {
                let objectByID;
                if(i.includes("User_")) {
                    objectByID = Object.values(result[2].children).filter(it => it.id==i);
                }
                else if(i.includes("UserGroup_")) {
                    objectByID = Object.values(result[1].children).filter(it => it.id==i);
                }
                else {
                    objectByID = Object.values(result[0].children).filter(it => it.id==i);
                }
                if(typeof objectByID[0] !=="undefined")
                {
                    objectByID[0].name = objectByID[0].text;
                    retainedObject.push(objectByID[0]);
                }
                bindShareWith2WithData(selector, fetchUsersAndGroupsUrl, retainedObject,  false, false, alertType);
            })
        })
    }
    bindShareWith2WithData(selector, fetchUsersAndGroupsUrl, retainedObject,  false, false, alertType);
}

var getActionDetails = function(modalId) {
    return {
        "config": modalId.find("#config").val(),
        "type": modalId.find("#type").val(),
        "assignedToValue": modalId.find("#assignedTo").val(),
        "dueDate": modalId.find("#dueDate").val(),
        "completedDate": modalId.find("#completedDate").val(),
        "details": modalId.find("#details").val(),
        "comments": modalId.find("#comments").val(),
        "alertId": modalId.find("#alertId").val(),
        "appType": modalId.find("#appType").val(),
        "exeConfigId": modalId.find("#exeConfigId").val(),
        "meetingId": modalId.find("#meetingElement").val(),
        "actionStatus": modalId.find("#actionStatus").val(),
        "actionId": modalId.find("#actionId").val()
    }
};


var build_sca_by_status = function (chartId, container) {
    $.ajax({
        cache: false,
        type: 'GET',
        url: '/signal/dashboard/caseByStatus'
    }).done(function (result) {
        by_status(result, chartId, container, "Qualitative_Alert")
    });
};

var build_agg_by_status = function (chartId, container) {
    $.ajax({
        cache: false,
        type: 'GET',
        url: '/signal/dashboard/aggAlertByStatus'
    }).done(function (result) {
        by_status(result, chartId, container, "Quantitative Alert")
    });
};


var build_aha_by_status = function (chartId, container) {
    $.ajax({
        cache: false,
        type: 'GET',
        url: '/signal/dashboard/ahaByStatus'
    }).done(function (result) {
        by_status(result, chartId, container, "Adhoc Alert")
    })
};

var build_alert_by_duedate = function (chartId) {

    $.ajax({
        cache: false,
        type: 'GET',
        url: '/signal/dashboard/alertByDueDate'
    }).done(function (result) {
        by_dueDate(chartId, result)
    });
};


var build_product_by_status = function (chartId, chartType) {
    $.ajax({
        cache: false,
        type: 'GET',
        url: '/signal/dashboard/getProductByStatus?type=' + chartType
    }).done(function (result) {
        by_product_state(chartId, result.productState, result.productList)
    });
};

var by_product_state = function (chartId, result, productList) {

    $(chartId).css('height', '100%');
    $(chartId).highcharts({
        chart: {
            type: 'column'
        },
        title: {
            text: ''
        },
        credits: {
            enabled: false
        },
        xAxis: {
            categories: productList
        },
        yAxis: {
            min: 0,
            title: {
                text: ''
            },
            stackLabels: {
                enabled: true,
                style: {
                    fontWeight: 'bold',
                    color: (Highcharts.theme && Highcharts.theme.textColor) || 'gray'
                }
            }
        },
        legend: {
            layout: 'horizontal',
            backgroundColor: Highcharts.theme.legendBackgroundColor || '#FFFFFF',
            align: 'center',
            verticalAlign: 'bottom',
            x: 0,
            y: 10,
            floating: false,
            shadow: true
        },
        tooltip: {
            headerFormat: '<b>{point.x}</b><br/>',
            pointFormat: '{series.name}: {point.y}<br/>Total: {point.stackTotal}'
        },
        plotOptions: {
            column: {
                stacking: 'normal',
                dataLabels: {
                    enabled: true,
                    color: (Highcharts.theme && Highcharts.theme.dataLabelsColor) || 'white',
                    style: {
                        textShadow: '0 0 3px black'
                    }
                }
            }
        },
        series: result
    });
};

var by_dueDate = function (chartId, result) {

    $(chartId).css("height", "100%");
    $(chartId).highcharts({
        lang: {
            noData: "No Data available"
        },
        chart: {
            type: 'column'
        },
        title: {
            text: ''
        },
        credits: {
            enabled: false
        },
        xAxis: {
            categories: ['Individual Case Alert', 'Aggregate Alert', 'Adhoc Alert']
        },
        yAxis: {
            min: 0,
            title: {
                text: ''
            },
            stackLabels: {
                enabled: true,
                style: {
                    fontWeight: 'bold',
                    color: (Highcharts.theme && Highcharts.theme.textColor) || 'gray'
                }
            }
        },
        legend: {
            layout: 'horizontal',
            backgroundColor: Highcharts.theme.legendBackgroundColor || '#FFFFFF',
            align: 'center',
            verticalAlign: 'bottom',
            x: 0,
            y: 10,
            floating: false,
            shadow: true
        },
        tooltip: {
            headerFormat: '<b>{point.x}</b><br/>',
            pointFormat: '{series.name}: {point.y}<br/>Total: {point.stackTotal}'
        },
        plotOptions: {
            column: {
                stacking: 'normal',
                dataLabels: {
                    enabled: true,
                    color: (Highcharts.theme && Highcharts.theme.dataLabelsColor) || 'white',
                    style: {
                        textShadow: '0 0 3px black'
                    }
                }
            }
        },
        series: result
    });
};

var by_status = function (data, chartId, container, alertType) {

    $(chartId).css("height", "100%");
    dashboardCharts = $(chartId).highcharts({
        chart: {
            renderTo: container,
            defaultSeriesType: 'pie'
        },
        credits: {
            enabled: false
        },
        title: {
            text: '',
            align: 'left'
        },
        subtitle: {
            text: ''
        },
        xAxis: {
            labels: {
                style: {
                    color: 'purple'
                }
            },
            title: {
                text: 'Signals',
                style: {
                    color: 'purple'
                }
            }
        }
        ,
        yAxis: {
            title: {
                text: '',
                style: {
                    color: 'purple'
                }
            }
        },
        legend: {
            layout: 'horizontal',
            backgroundColor: Highcharts.theme.legendBackgroundColor || '#FFFFFF',
            align: 'center',
            verticalAlign: 'bottom',
            x: 0,
            y: 10,
            floating: false,
            shadow: true
        },
        tooltip: {
            animation: true,
            formatter: function () {
                var label
                if (alertType == "Qualitative_Alert") {
                    label = '' + this.y + ' Cases';
                } else if (alertType == "Quantitative Alert") {
                    label = '' + this.y + ' PEC';
                } else {
                    label = '' + this.y + ' Alerts';
                }
                return label
            }
        },
        plotOptions: {
            column: {
                animation: true,
                pointPadding: 0.2,
                borderWidth: 0
            },
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
            type: 'pie',
            name: 'Signals by Status',
            size: '80%',
            innerSize: '40%',
            data: data
        }]
    });
};

var build_sidebar_data = function () {
    build_sidebar_case_data('adhoc', true);
    build_sidebar_case_data('signals', true);
    build_sidebar_case_data('aggregate', true);
    build_sidebar_case_data('single', true);
    build_sidebar_case_data('evdas', true);
    build_sidebar_case_data('actionItems', false);

};
var build_sidebar_case_data = function (caseType, isAsync) {
    $.ajax({
        cache: false,
        type: 'GET',
        async: isAsync,
        url: '/signal/dashboard/sideBarByType?case=' + caseType
    }).done(function (result) {
        pvsDashboardCaseCounter(result, caseType);
    })
};
var dashBoardCountDataMap = new Map();

function pvsDashboardCaseCounter(countData, caseType) {
    dashBoardCountDataMap.set(caseType, countData[caseType]);
    if (dashBoardCountDataMap.get('adhoc') != null && dashBoardCountDataMap.get('actionItems') != null && dashBoardCountDataMap.get('signals') != null
        && dashBoardCountDataMap.get('aggregate') != null && dashBoardCountDataMap.get('evdas') != null && dashBoardCountDataMap.get('single') != null) {
        pvsDashboardMapCounter(dashBoardCountDataMap);
    }
}

function pvsDashboardMapCounter(countData) {
    console.log(countData);
    $("#my-single .counter").text(countData.get('single'));
    $("#my-aggregate .counter").text(countData.get('aggregate'));
    $("#my-adhoc .counter").text(countData.get('adhoc'));
    $("#actionItems .counter").text(countData.get('actionItems'));
    $("#evdasReview .counter").text(countData.get('evdas'));
    $("#signals .counter").text(countData.get('signals'));
    // Counter
    $('.counter').counterUp({
        delay: 100,
        time: 1200
    });
}

var build_sidebar_data_bkp = function () {
    $.ajax({
        cache: false,
        type: 'GET',
        url: '/signal/dashboard/sideBar'
    }).done(function (result) {
        pvsDashboardCounter(result);
    });
};

var genSignalAlertNameLink = function (signalId, signalName) {
    return '<a href="' + 'validatedSignal/details' + '?' + 'id=' + signalId + '">' + escapeHTML(signalName) + '</a>';
};

var genTopicAlertNameLink = function (topicId, topicName) {
    return '<a href="' + '/signal/topic/details' + '?' + 'id=' + topicId + '">' + escapeHTML(topicName) + '</a>';
};

var genAlertNameLink = function (value, id, type, dashboardFilter, colName) {
    var result = "";
    return function () {
        var linkUrl = "";
        switch (type) {
            case "Aggregate":
                linkUrl = aggDataUrl
                break
            case "ICR":
                linkUrl = singleDataUrl
                break
            case "EVDAS":
                linkUrl = evdasDataUrl
                break
            case "Aggregate (adhoc)":
                linkUrl = aggAdhocDataUrl
                break
            case "ICR (adhoc)":
                linkUrl = singleAdhocDataUrl
                break
            case "EVDAS (adhoc)":
                linkUrl = evdasAdhocDataUrl
                break
            case "Literature":
                linkUrl = litDataUrl
                break
        }
        if (colName === "requiresReview") {
            if (value !== "-") {
                result = '<div class="cell-center"><a href="' + (linkUrl + '?' + 'callingScreen=review&' + signal.utils.composeParams({
                    configId: id,
                    dashboardFilter: dashboardFilter
                })) + '">' + value + '</a></div>'
            } else {
                result = '<div class="cell-center">' + value + '</div>'
            }
        } else {
            result = '<a href="' + (linkUrl + '?' + 'callingScreen=review&' + signal.utils.composeParams({
                configId: id,
                dashboardFilter: dashboardFilter
            })) + '">' + value
        }
        return result
    }
};

var init_assigned_signal_table = function () {
    if (!$.fn.dataTable.isDataTable('#assignedSignalTable')) {
        triggeredSignalTable = $('#assignedSignalTable').DataTable({
            language: {
                "lengthMenu": 'Show _MENU_ entries',
                "search": "",
            },
            layout : {
                topStart: 'pageLength',
                topEnd: null
            },
            initComplete: function () {
            },

            "ajax": {
                "url": signalListUrl,
                "cache": false,
                "dataSrc": "aaData",
                "data": function (d) {
                    let selectedData = $('#signalsFilter').val();
                    if(selectedData || sessionStorage.getItem("signalDashboard")=="null") {
                        d.selectedAlertsFilter = JSON.stringify(selectedData);
                    }
                    else {
                        d.selectedAlertsFilter = '[' + JSON.stringify(sessionStorage.getItem("signalDashboard")) + ']';
                    }
                },
                "error": ajaxAuthroizationError,
            },
            "aaSorting": [],
            "iDisplayLength": 50,
            "aLengthMenu": [[50, 100, -1], [50, 100, "All"]],
            "pagingType": "simple_numbers",
            "aoColumns": [
                {
                    "mData": "name",
                    'className': '',
                    "mRender": function (data, type, row) {
                        return genSignalAlertNameLink(row.signalId, row.signalName)
                    }
                },
                {
                    "mData": "productName",
                    'className': 'ol-md-1-half cell-break',
                    "mRender": function (data, type, row) {

                        var type = '<span>' + addEllipsis(row.productName) + '</span>';
                        return type
                    }
                },
                {"mData": "disposition"}
            ],
            scrollX: true,
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }]
        });
    }
};


var init_assigned_topic_table = function () {
    if (!$.fn.dataTable.isDataTable('#assignedTopicTable')) {
        $('#assignedTopicTable').DataTable({

            language: { search: ''},
            initComplete: function () {
            },
            "ajax": {
                "url": topicListUrl,
                "cache": false,
                "dataSrc": ""
            },
            "layout": {
                topStart: null,
                topEnd: null,
                bottomStart: ['pageLength', 'info', {
                    paging: {
                        type: 'full_numbers'
                    }
                }],
                bottomEnd: null,
            },
            /*"oLanguage": {
                "oPaginate": {
                    "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                    "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                    "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                    "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
                },
            },*/
            "iDisplayLength": 50,
            "aLengthMenu": [[50, 100, -1], [50, 100, "All"]],
            "pagingType": "simple_numbers",
            "aoColumns": [
                {
                    "mData": "topicName",
                    'className': '',
                    "mRender": function (data, type, row) {
                        return genTopicAlertNameLink(row.topicId, row.topicName)
                    }
                },
                {"mData": "productName"},
                {"mData": "noOfPec"},
                {"mData": "noOfCases"},
                {"mData": "disposition"}
            ],
            scrollX: true,
            scrollY: dtscrollHeight(),
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }]
        });
    }
};

var init_triggered_alert_table = function () {
    $.fn.DataTable.ext.pager.numbers_length = 6;
    if (!$.fn.dataTable.isDataTable('#assignedTriggeredAlerts')) {
        triggeredAlertTable = $('#assignedTriggeredAlerts').DataTable({
            language: {
                "lengthMenu": 'Show _MENU_ entries'
            },
            layout : {
                topStart: 'pageLength',
                topEnd: null
            },
            drawCallback: function () {
                colEllipsis();
                webUiPopInit();
            },
            initComplete: function () {
            },
            "ajax": {
                "url": alertUrl,
                "cache": false,
                "error": ajaxAuthroizationError,
                "dataSrc": "aaData",
                "data": function (d) {
                    d.searchString = d.search.value;
                    if (d.order.length > 0) {
                        d.direction = d.order[0].dir;
                        //Column header mData value extracting
                        d.sort = d.columns[d.order[0].column].data;
                    }
                    let selectedData = $('#alertsFilterDashboard').val();
                    if(selectedData || sessionStorage.getItem("alertDashboard")=="null") {
                        d.selectedAlertsFilter = JSON.stringify(selectedData);
                    }
                    else {
                        d.selectedAlertsFilter = '[' + JSON.stringify(sessionStorage.getItem("alertDashboard")) + ']';
                    }
                }
            },
            "iDisplayLength": 25,
            "serverSide": true,
            "aLengthMenu": [[25, 50, 100, 200, 500], [25, 50, 100, 200, 500]],
            //"sPaginationType": "simple_numbers",
            "aoColumns": [
                {
                    "mData": "product",
                    "className": 'col-min-150 col-max-150 cell-break',
                    "mRender": function (data, type, row) {
                        return addEllipsis(row.product);
                    }
                },
                {
                    "mData": "type",
                    "className": 'col-min-150 col-max-150'
                },
                {
                    "mData": "name",
                    'className': 'col-min-150 col-max-150 dashboard-name',
                    "mRender": function (data, type, row) {
                        return genAlertNameLink(escapeHTML(row.name), row.id, row.type, '', "name")
                    }
                },
                {
                    "mData": "requiresReview",
                    "className": "col-min-150 col-max-150 dashboard-alerts",
                    "mRender": function (data, type, row) {
                        return genAlertNameLink(row.requiresReview, row.id, row.type, 'underReview', "requiresReview");
                    }
                },
                {
                    "mData": "dueIn",
                    "className": "col-min-150 col-max-150 text-center",
                    "mRender": function (data, type, row) {
                        return "<span>" + row.dueIn + "</span>";
                    }
                },
                {
                    "mData": "dateRange",
                    "className": "col-min-150 col-max-150 text-left",
                    "mRender": function (data, type, row) {
                        return "<span>" + row.dateRange + "</span>";
                    }
                },
                {
                    "mData": "",
                    "sortable": false,
                    "mRender": function (data, type, row) {
                        return "<a href='javascript:void(0);' title='Remove' class='table-row-del remove-alert hidden-ic'> " +
                            "<i class='mdi mdi-close' aria-hidden='true'></i> \</a>";
                    },
                    "className": 'col-min-150 col-max-150 text-center'
                }
            ],
            scrollX: true,
            scrollY: dtscrollHeight(),
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }],
            "aaSorting": []
        });
    }
};

var init_action_list_table = function () {
    if (!$.fn.dataTable.isDataTable('#listAction')) {
        triggeredActionsTable = $('#listAction').DataTable({
            language: { "lengthMenu": 'Show _MENU_ entries'},
            drawCallback: function () {

                $('.edit-action-link').on("click", function (evt) {
                    var url = "/signal/action/getById?id=" + $(this).attr('data-id');
                    $.ajax({
                        url: url,
                        async: false,
                        cache: false
                    }).done(function (data) {
                        data.action_types = action_select_values.types;
                        data.action_configs = action_select_values.configs;
                        data.all_status = action_select_values.allStatus;
                        if (typeof alertId != "undefined") {
                            data.alertId = $(this).attr('data-id');
                        }
                        data.actionStatus = {name: data.actionStatus, value: "New"};
                        var html_content = signal.utils.render('action_editor_6.1_6_v1', data);
                        $('#action-editor-container').html(html_content);
                        $('#action-editor-container #due-date-picker').datepicker({
                            date: data.dueDate,
                            allowPastDates: true,
                            momentConfig: {
                                culture: userLocale,
                                tz: userTimeZone,
                                format: DEFAULT_DATE_DISPLAY_FORMAT
                            }
                        });
                        var dueDate = moment(data.dueDate, DEFAULT_DATE_DISPLAY_FORMAT).format(DEFAULT_DATE_DISPLAY_FORMAT);
                        $('#action-editor-container').find('#due-date-picker').find('#dueDate').val(dueDate);

                        $('#action-editor-container #completion-date-picker').datepicker({
                            date: data.completedDate?data.completedDate: '',
                            allowPastDates: true,
                            restricted: [{from: TOMORROW , to: Infinity}],
                            momentConfig: {
                                culture: userLocale,
                                tz: userTimeZone,
                                format: DEFAULT_DATE_DISPLAY_FORMAT
                            }
                        }).on('inputParsingFailed.fu.datepicker',function (e) {
                            $('#completedDate').val('');
                        });
                        if(data.meetingId && data.meetingId != "null" && data.alertType =="Signal Management" ){
                            var option = new Option(data.config, data.configObj.id, true, true);
                            $('#action-edit-modal').find(".action-config-list .selectBox").append(option);
                        }
                        var completedDate = data.completedDate?moment(data.completedDate, DEFAULT_DATE_DISPLAY_FORMAT).format(DEFAULT_DATE_DISPLAY_FORMAT):'';
                        $('#action-editor-container').find('#completion-date-picker').find('#completedDate').val(completedDate);

                        $('#action-editor-container #actionStatus').on("change", function () {
                            if ($(this).val() === 'Closed') {
                                var completedDate = moment.utc(new Date()).tz(serverTimeZone).format("DD-MMM-YYYY");
                                $('#action-editor-container #completion-date-picker').find('#completedDate').val(completedDate);
                            }
                        });

                        $('#action-editor-container').find('#appType').val(data.alertType);
                        var action_editor = $('#action-editor-container');
                        var editActionModal = $('#action-edit-modal');

                        if (typeof alertId != "undefined") {
                            editActionModal.find('.id-element').attr('data-id', $(this).attr('data-id'))
                        }
                        editActionModal.modal({});
                        $('#action-edit-modal #cancel-bt').on("click", handle_action_editor_cancel);
                        $('#action-edit-modal #update-bt').on("click", handle_action_editor_update);
                    })
                });
                colEllipsis();
                webUiPopInit();
            },
            initComplete: function () {
            },
            "ajax": {
                "url": actionListUrl,
                "dataSrc": "",
                "error": ajaxAuthroizationError,
            },
            "layout": {
                topStart: 'pageLength',
                topEnd: null,
                bottomStart: ['info', {
                    paging: {
                        type: 'full_numbers'
                    }
                }],
                bottomEnd: null,
            },
            "oLanguage": {
                "oPaginate": {
                    "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                    "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                    "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                    "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
                },
            },
            "bLengthChange": true,
            "iDisplayLength": 5,
            "aLengthMenu": [[5, 10, -1], [5, 10, "All"]],
            "pagingType": "simple_numbers",
            "aoColumns": [
                {
                    "mData": "id",
                    "mRender": function (data, type, row) {
                        return "<a href='#' class='edit-action-link' data-id='" +
                            row.id + "'>" + row.id + "<a>"
                    },
                    "className": 'col-min-100 col-max-100 cell-break'
                },
                {
                    "mData": "type",
                    "mRender": function (data, type, row) {
                        return DOMPurify.sanitize(addEllipsisWithEscape(row.type), {
                            ALLOWED_TAGS: ['div', 'a', 'i'],
                            ALLOWED_ATTR: ['class', 'tabindex', 'title', 'more-data'],
                            SAFE_FOR_TEMPLATES: true
                        })
                    },
                    "className": 'col-min-100 col-max-100 cell-break'
                },
                {
                    "mData": "config",
                    "mRender": function (data, type, row) {
                        return DOMPurify.sanitize(addEllipsisWithEscape(row.config), {
                            ALLOWED_TAGS: ['div', 'a', 'i'],
                            ALLOWED_ATTR: ['class', 'tabindex', 'title', 'more-data'],
                            SAFE_FOR_TEMPLATES: true
                        })
                    },
                    "className": 'col-min-150 col-max-150 cell-break'
                },
                {
                    "mData": "details",
                    "mRender": function (data, type, row) {
                        return DOMPurify.sanitize(addEllipsisWithEscape(row.details), {
                            ALLOWED_TAGS: ['div', 'a', 'i'],
                            ALLOWED_ATTR: ['class', 'tabindex', 'title', 'more-data'],
                            SAFE_FOR_TEMPLATES: true
                        })
                    },
                    "className": 'col-min-150 col-max-150 cell-break'
                },
                {
                    "mData": "alertName",
                    "mRender": function (data, type, row) {
                        return DOMPurify.sanitize(addEllipsis(row.alertName), {
                            ALLOWED_TAGS: ['div', 'a', 'i'],
                            ALLOWED_ATTR: ['class', 'tabindex', 'title', 'more-data'],
                            SAFE_FOR_TEMPLATES: true
                        })
                    },
                    "className": 'col-min-100 col-max-100 cell-break'
                },
                {"mData": "dueDate", 'className': 'col-min-100 col-max-100'}
            ],
            scrollX: true,
            aaSorting: [[0, "desc"]],
            scrollY: dtscrollHeight(),
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }]
        });
    }
};

var handle_action_editor_update = function (evt) {
    evt.preventDefault();
    if (!evt.handled) {
        evt.handled = true;
        var json = $('#action-editor-container form#action-editor-form').serialize() + "&appType=" + $("#appType").val() + "&exeConfigId=" + $("#executedConfigId").val();
        $.ajax({
            url: '/signal/action/updateAction',
            method: 'POST',
            data: json,
            async: false,
            cache: false
        }).done(function (dat) {
            var action_list_table = $('#listAction').DataTable();
            action_list_table.ajax.reload();
            var containerId = "#" + signal.utils.localStorageUtil.getJSON("dashboardWidgetsConfig")['pvWidgetChart-6'].content.id;
            $(containerId).fullCalendar('destroy');
            init_calendar(containerId);
        });
        $('#action-edit-modal').modal('hide')
    }
};
var handle_action_editor_cancel = function (evt) {
    evt.preventDefault();
    $('#action-edit-modal').modal('hide')
};

var initDashboardWidgetsData = function () {

    //Side bar data.
    build_sidebar_data();

    //Triggered alert grid for Qualitative and Quantitative data.
    init_triggered_alert_table();
    if ($.fn.dataTable.isDataTable('#assignedTriggeredAlerts')) {
        loadTableOption('#assignedTriggeredAlerts');
    }

    //Assigned signal grid
    init_assigned_signal_table();

    //Assigned topic grid
    init_assigned_topic_table();

    //Assignedl Action list
    init_action_list_table();

    //Qualitative alert by status
    build_sca_by_status('#case-status-chart', "case-status-chart");

    //Quantitative alert by status
    build_agg_by_status('#aggregate-chart', "aggregate-chart");

    //Adhoc Alert By Status
    build_aha_by_status('#adhoc-chart', 'adhoc-chart');

    //Alert By Due date
    build_alert_by_duedate("#due-date-chart");

    //Qualitative alert by products by status
    var dashboardWidgetsConfig_local = signal.utils.localStorageUtil.getJSON("dashboardWidgetsConfig");
    if (typeof dashboardWidgetsConfig_local["pvWidgetChart-9"] != 'undefined' && dashboardWidgetsConfig_local["pvWidgetChart-9"]['visible']) {
        build_product_by_status("#qualitative-products-status", "Single Case Alert");
    }

    //Quantitative alert by products by status
    if (typeof dashboardWidgetsConfig_local["pvWidgetChart-10"] != 'undefined' && dashboardWidgetsConfig_local["pvWidgetChart-10"]['visible']) {
        build_product_by_status("#quantitative-products-status", "Aggregate Case Alert");
    }

};

function pvsDashboardCounter(countData) {
    $("#my-single .counter").text(countData.single);
    $("#my-aggregate .counter").text(countData.aggregate);
    $("#my-adhoc .counter").text(countData.adhoc);
    $("#actionItems .counter").text(countData.actionItems);
    $("#evdasReview .counter").text(countData.evdas);
    $("#signals .counter").text(countData.signals);
    // Counter
    $('.counter').counterUp({
        delay: 100,
        time: 1200
    });
}

function checkCount(e) {
    if (!parseInt($(e).find('span').html())) {
        return false;
    }
}
