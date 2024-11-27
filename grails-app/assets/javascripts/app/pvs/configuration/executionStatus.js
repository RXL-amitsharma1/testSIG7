//= require app/pvs/configuration/milestoneTable.js

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
    const sanitizerAllowList = $.fn.tooltip.Constructor.DEFAULTS.whiteList;
    sanitizerAllowList.table = [];
    sanitizerAllowList.tr = [];
    sanitizerAllowList.th = [];
    sanitizerAllowList.td = [];
    sanitizerAllowList.tbody = [];
    sanitizerAllowList.thead = [];
    var perfEntries = performance.getEntriesByType('navigation');
    if (perfEntries.length && perfEntries[0].type === 'back_forward') {
        var executionStatus = getCookie('executionStatus');
        var alertType = getCookie('alertType');
        $('#alertType').val(alertType);
        $('#executionStatus').val(executionStatus).trigger('change');
    }

    var isSpotFireEnabled = true;
    if ($("#spotFireHidden").val() != undefined)
        isSpotFireEnabled = false;
    if($('#completedMileStones').val() != undefined) {
        $('#completedMileStones').append(populateMileStoneTable(JSON.parse(milestoneTableMap),JSON.parse(progressTrackerMap),highestExecutionLevel, true));
    }

    $(document).on('click', '.resumeAlert', function (event) {
        event.preventDefault();
        var selectedRowIndex = $(this).closest('tr').index();
        var executionStatusId = table.rows(selectedRowIndex).data()[0].executionStatusId;
        var resumeUrl=$(this).data('href')
        bootbox.confirm({
            title: ' Run Alert ',
            message: "Are you sure want to run this Alert?",
            buttons: {
                confirm: {
                    label: 'Run Alert',
                    className: 'btn-primary'
                },
                cancel: {
                    label: 'Cancel',
                    className: 'btn-default'
                }
            },
            callback: function (result) {
                if (result) {
                    $.ajax({
                        type: "POST",
                        data: {'executionStatus.id': executionStatusId},
                        url: resumeUrl,
                    })
                        .done( function (res) {
                            if (res.status) {
                                $('#rxTableReportsExecutionStatus').DataTable().draw();
                            }
                            if(res.message && res.status){
                                $.Notification.notify('success', 'top right', "Success", res.message, {autoHideDelay: 5000});
                            } else if(res.message){
                                $.Notification.notify('error', 'top right', "Error", res.message, {autoHideDelay: 5000});
                            }
                        });
                } else {
                    event.preventDefault();
                }
            }
        });
    });



    var relatedPage = $('#relatedResults').val();

    var relatedUrl = executionStatusUrl;
    var pageURL = $(location).attr("href");
    var currentUrl = new URL(pageURL);
    var pageAlertType = currentUrl.searchParams.get("alertType");
    var isEmailRoute = false
    var alertStatusFromMail = currentUrl.searchParams.get("alertStatus")
    if (alertStatusFromMail != null && alertStatusFromMail != "") {
        isEmailRoute = true
    }

    var isAdmin = ($('#isAdmin').val() === 'true');
    var table = $('#rxTableReportsExecutionStatus').DataTable({
        layout: {
            topStart: ["pageLength", { search: { placeholder: "Search" } }],
            topEnd: null,
            bottomStart: [ "info", {paging: {type: "full_numbers"}}],
            bottomEnd: null,
        },
        customProcessing: true,
        "language": {
            "search": '',
            "url": "../assets/i18n/dataTables_" + userLocale + ".json"
        },
        drawCallback: function (settings) {
            $('.reloaderBtn').removeClass('glyphicon-refresh-animate');
            colEllipsis();
            webUiPopInit();
        },
        initComplete: function () {
            initAlertTypeDropDown("rxTableReportsExecutionStatus", table);
            initReportExeuctionStatusDropDown("rxTableReportsExecutionStatus", table);
            getReloader($('.dt-length'), $("#rxTableReportsExecutionStatus"));
            var executionStatusDropDown = $("#executionStatus");
            executionStatusDropDown.on('change',function () {
                setCookie('executionStatus', $(this).val());
                setCookie('alertType', $('#alertType').val());
                checkStatus(this);
            });
            var alertTypeDropDown = $("#alertType");
            alertTypeDropDown.on('change',function () {
                setCookie('executionStatus', $('#executionStatus').val());
                setCookie('alertType', $(this).val());
                checkStatus(this);
            });
        },
        "ajax": {
            "url": relatedUrl,
            "dataSrc": "aaData",
            "data": function (d) {
                d.searchString = d.search.value;
                if(d.order.length >0){
                    d.direction = d.order[0].dir;
                    //Column header mData value extracting
                    d.sort = d.columns[d.order[0].column].data;
                }
                if (isEmailRoute) {
                    d.status = alertStatusFromMail;
                    $('select[name="executionStatus"]').val("ERROR").trigger('change');
                    isEmailRoute = false;
                } else if ($('select[name="executionStatus"]').length > 0) {
                    d.status = $('select[name="executionStatus"]').val();
                }
                if ($('select[name="alertType"]').length > 0) {
                    d.alertType = $('select[name="alertType"]').val() ? $('select[name="alertType"]').val(): pageAlertType;
                }
            }
        },
        "oLanguage": {
            "oPaginate": {
                "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
            },
        },
        "aaSorting": [],
        "order": [[ 3, "desc" ]],
        "bLengthChange": true,
        "iDisplayLength": 50,
        "serverSide": true,
        "aLengthMenu": [[50, 100, 200, 500], [50, 100, 200, 500]],
        "aoColumns": [
            {
                "mData": "name",
                "className":'col-min-200 col-max-200 cell-break',
                "mRender": function(data, type, row){
                    if(row.nodeName != null && isAdmin)
                        return '<span tabindex="0" data-container="body" data-placement="top"  data-toggle="popover" style="cursor:pointer; white-space: pre;" class="popoverMessage" data-content="Alert ran on: ' + row.nodeName + '">'+addEllipsisForDescriptionText((row.name))+'</span>';
                    else
                        return '<span style="cursor:pointer; white-space: pre;" >'+addEllipsisForDescriptionText((row.name))+'</span>';
                }
            },
            {"mData": "alertType",
                "sortable": false
            },
            {
                "mData": "frequency",
                "sortable": false,
                "className": 'col-min-150 col-max-150'
            },
            {
                "mData": "runDate",
                "className": 'col-min-150 col-max-150',
                "aTargets": ["runDate"],
                "mRender": function (data, type, row) {
                    return row.runDate;
                }
            },
            {
                "mData": "executionTime",
                "className": 'col-min-150 col-max-150',
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
                "className": 'col-min-150 col-max-150'
            },
            {
                "mData": "executionStatus",
                "className": "dataTableColumnCenter col-min-100 col-max-100 text-center",
                "aTargets": ["executionStatus"],
                "sortable": false,
                "mRender": function (data, type, row) {
                    return createStatusHtml(row.executionStatus, row, resumeAlertUrl, true)
                }
            },
            {
                "className": "dataTableColumnCenter col-min-100 col-max-100 text-center",
                "sortable": false,
                "mRender": function (data, type, row) {
                    return createStatusHtml(row.reportExecutionStatus, row, resumeReportUrl, false)
                }
            },
            {
                "className": "dataTableColumnCenter col-min-100 col-max-100 text-center",
                "visible": isSpotFireEnabled,
                "sortable": false,
                "mRender": function (data, type, row) {
                    return createStatusHtml(row.spotfireExecutionStatus, row, resumeSpotfireUrl, false)
                }
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

    $('#rxTableReportsExecutionStatus').on('mouseover', 'tr', function () {
        $('.popoverMessage').popover({
            trigger: 'hover focus',
            viewport: '#rxTableReportsExecutionStatus',
            html: true
        }).on("show.bs.popover", function(){ $(this).data("bs.popover").tip().css("maxWidth", "350px")});
    });

    actionButton('#rxTableReportsExecutionStatus');
    loadTableOption('#rxTableReportsExecutionStatus');

    $('.outside').hide();
});


function checkStatus(elem) {
    $('#rxTableReportsExecutionStatus').DataTable().order( [[ 3, 'desc' ]] ).draw();
}

function getReloader(toolbar, tableName) {
    var reloader =   '<span title="Refresh" class="glyphicon reloaderBtn glyphicon-refresh"></span>';
    reloader = $(reloader);
    $(toolbar).append(reloader);
    if(tableName != undefined) {
        $('.reloaderBtn').on('click', function() {
            $('.reloaderBtn').addClass('glyphicon-refresh-animate');
            $(tableName).DataTable().draw();
        });
    }
}

function initReportExeuctionStatusDropDown(tableId,table) {
    var searchDiv = $('.dt-search');
    searchDiv.find('select').css('height', '40');
    searchDiv.find('label').css('margin-bottom', '-2px');
    searchDiv.parent().addClass('searchToolbar');
    searchDiv.parent().find('.dt-length').addClass('col-xs-2');
    searchDiv.before($('#executionStatusDropDown'));
    $('#executionStatusDropDown').find('select').css('display', 'inline-block');
}
function initAlertTypeDropDown(tableId,table) {

    var searchDiv = $('.dt-search');
    searchDiv.find('select').css('height', '40');
    searchDiv.find('label').css('margin-bottom', '-2px');
    searchDiv.parent().addClass('searchToolbar');
    searchDiv.before($('#alertTypeDropDown'));
    $('#alertTypeDropDown').find('select').css('display', 'inline-block');
}

function createStatusHtml(executionStatus, row, resumeUrl, isAlert) {
    var hasAlertResumeAccess = true;
    if(typeof roleAccessMap !== "undefined"){
        hasAlertResumeAccess = roleAccessMap[$("#alertType").val()]
    }
    var html = '';
    switch (executionStatus) {
        case EXECUTION_STATUS_ENUM.SCHEDULED :
            html = '<a tabindex="0" data-container="body" data-placement="top" data-toggle="tooltip" class="fa fa-clock-o fa-lg es-scheduled popoverMessage" data-content="' + $.i18n._('Scheduled') + '">';
            break;
        case EXECUTION_STATUS_ENUM.GENERATING :
            if (isAlert) {
                var execPercentage;
                if (row.alertType === ALERT_CONFIG_TYPE.EVDAS_CONFIGURATION || row.alertType === ALERT_CONFIG_TYPE.LITERATURE_CONFIGURATION || row.alertType === ALERT_CONFIG_TYPE.QUANTITATIVE_CONFIGURATION) {
                    execPercentage = ALERT_PROGRESS_BAR_STATUS[row.executionLevel];
                    if (row.alertType === ALERT_CONFIG_TYPE.QUANTITATIVE_CONFIGURATION) {
                        execPercentage = row.progressPercentage
                    }
                } else if (row.alertType === ALERT_CONFIG_TYPE.QUALTITATIVE_CONFIGURATION) {
                    execPercentage = row.progressPercentage
                    if(typeof  execPercentage ==="undefined"){
                        execPercentage = QUALITATIVE_PROGRESS_BAR_STATUS[row.executionLevel];
                    }
                }
                var color = QUALITATIVE_PROGRESS_BAR_COLOR[row.executionLevel];
                html = '<div class="progress m-t-15 popoverMessage" data-container="body" data-placement="top" data-content="' + execPercentage + "%" + $.i18n._('Completed') +'">' +
                    '    <div class="progress-bar ' + color + '" role="progressbar"  aria-valuenow="' + execPercentage + '" aria-valuemin="0" aria-valuemax="100" style="width:' + execPercentage + '%"><span class="progress-value m-t-3">' + execPercentage + '%</span>' +
                    '    </div>' +
                    '  </div>'
            } else {
                html = '<div class="progress m-t-15 popoverMessage" data-container="body" data-placement="top" data-content="' + "0%" + $.i18n._('Completed') +'">' +
                    '    <div class="progress-bar" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100" style="width:0%"><span class="progress-value m-t-3">' + 0 + '%</span>' +
                    '    </div>' +
                    '  </div>'
            }
            break;
        case EXECUTION_STATUS_ENUM.DELIVERING :
            html = '<div class="progress m-t-15 popoverMessage" data-container="body" data-placement="top"> ' +
                '    <div class="progress-bar" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100" style="width:0%"><span class="progress-value m-t-3">' + 0 + '%</span>' +
                '    </div>' +
                '  </div>';
            break;
        case EXECUTION_STATUS_ENUM.COMPLETED :
            html = '<a tabindex="0"  data-toggle="tooltip" data-container="body" data-placement="top" class="fa fa-check-circle-o fa-lg es-completed popoverMessage" data-content="' + $.i18n._('completedSuccessfully') + '">';
            break;
        case EXECUTION_STATUS_ENUM.ERROR :
            if ((hasAlertResumeAccess && row.executionStatus !== EXECUTION_STATUS_ENUM.ERROR) || (isAlert && hasAlertResumeAccess && row.resumeAccess)) {
                if(isAlert){
                    html = '<a tabindex="0" data-html="true" data-container="body" data-placement="top"  data-toggle="popover" class="fa fa-exclamation-circle fa-lg es-error popoverMessage" data-content="' + populateMileStoneTable(row, row.progressTrackerMap,row.highestExecutionLevel, isAlert) + '" href="' + executionErrorUrl + '?id=' + row.executionStatusId + '">' +
                        '</a>&nbsp;<span tabindex="0" data-container="body" data-placement="top"  data-toggle="tooltip" style="cursor:pointer;" class="glyphicon glyphicon-play-circle fa-lg lg-icon popoverMessage resumeAlert" data-content="' + $.i18n._('resumeAlert') + '" data-href="' + resumeUrl + '"></span>'

                }else{
                    if(!row.isLatest){
                        html = '<span tabindex="0" data-container="body" data-placement="top"  data-toggle="popover" class="fa fa-exclamation-circle fa-lg es-error" href=""></span>'
                    } else {
                        html = '<span tabindex="0" data-container="body" data-placement="top"  data-toggle="popover" class="fa fa-exclamation-circle fa-lg es-error" href="">' +
                            '</span>&nbsp;<span tabindex="0" data-container="body" data-placement="top"  data-toggle="tooltip" style="cursor:pointer;" class="glyphicon glyphicon-play-circle fa-lg lg-icon popoverMessage resumeAlert" data-content="' + $.i18n._('resumeAlert') + '" data-href="' + resumeUrl + '"></span>'
                    }
                }
            } else {
                html = '<span tabindex="0" class="fa fa-exclamation-circle fa-lg es-error">'
            }
            break;
        case EXECUTION_STATUS_ENUM.WARN :
            if ($("#isAdmin").val() === "true") {
                return '<a tabindex="0" data-container="body" data-placement="top" data-toggle="tooltip"  class="fa fa-exclamation-circle fa-lg es-warn popoverMessage" title="' + message + '" data-content="' + details + '">'
            }
            else {
                return '<a tabindex="0" data-container="body" data-placement="top" data-toggle="tooltip" class="fa fa-exclamation-circle fa-lg es-warn">'
            }
            break;
        default :
            html = '-'
    }
    return html
}

function setCookie(key, value) {
    var expires = new Date();
    expires.setTime(expires.getTime() + (60 * 60 * 1000));
    document.cookie = key + '=' + value + ';expires=' + expires.toUTCString();
}

function getCookie(key) {
    var key = document.cookie.match('(^|;) ?' + key + '=([^;]*)(;|$)');
    return key ? key[2] : null;
}
