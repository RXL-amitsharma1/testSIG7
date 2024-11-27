//= require app/pvs/common/rx_common
//= require app/pvs/common/rx_alert_utils

var sharedWithModalShow = false;
var emailToModalShow = false;

$(function () {
    var genAlertNameLink = function(value, id) {
        return function() {
            return '<a href="' + signal.utils.composeUrl('adHocAlert', 'details', {id: id, type: 'sca'}) + '">' + encodeToHTML(value) + '</a>'
        }
    };

    var initTable = function() {
        signal.alerts_utils.get_priorities();
        signal.alerts_utils.get_workflowStates();

        var table = $('#adhocCaseAlertTable').DataTable({

            "language": {
                "url": "../assets/i18n/dataTables_" + userLocale + ".json",
                "oPaginate": {
                    "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                    "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                    "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                    "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
                },
            },
            initComplete: function () {
                $('.dt-input').val("");
                var $divToolbar = $('<div class="toolbarDiv col-xs-9"></div>');
                var $rowDiv = $('<div class="row"></div>');
                $divToolbar.append($rowDiv);
                $('#singleCaseAlerts_filter').attr("class", "row");
                $($('#singleCaseAlerts_filter').children()[0]).attr("class", "col-xs-3 searchBar");
                $("#singleCaseAlerts_filter").prepend($divToolbar);
                getDateFilter($rowDiv);

                initializeDatePickers(table);
                $('#singleCaseAlerts tbody tr').each(function () {
                    $(this).find('td:eq(3)').attr('nowrap', 'nowrap');
                });
            },
            drawCallback: function () {
                var rowsDataAdhoc = $('#adhocCaseAlertTable').DataTable().rows().data();
                pageDictionary($('#adhocCaseAlertTable_wrapper'),rowsDataAdhoc.length);
                showTotalPage($('#adhocCaseAlertTable_wrapper'),rowsDataAdhoc.length);
                initPSGrid($('#adhocCaseAlertTable_wrapper'));
                enterKeyAlertDetail();
                closePopupOnScroll();
            },

            "ajax": {
                "url": "listConfig",
                "dataSrc": ""
            },
            "aaSorting": [[3, "desc"]],
            "bLengthChange": true,
            "iDisplayLength": 50,
            "aLengthMenu": [[50, 100, 200, 500], [50, 100, 200, 500]],
            "aoColumns": [
                {
                    "mData": "name",
                    "mRender": function(data, type, row) {
                        return genAlertNameLink(row.name, row.id)
                    }
                },
                {
                    "mData": 'version'
                },
                {
                    "mData": "description",
                    "mRender": function(data, type, row) {
                        return encodeToHTML(row.description)
                    }
                },
                {
                    "mData": "alertRmpRemsRef"
                }
            ],
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }]
        });

        return table
    };

    var table = initTable();

    $('#dateFilterFrom').on('changed.fu.datepicker', function () {
        table.draw();
    });
    $('#dateFilterTo').on('changed.fu.datepicker', function () {
        table.draw();
    });
    loadTableOption('#singleCaseAlerts');

    $('.outside').hide();

    // For shared with modal
    $('#shareWith').select2().on("change", function (e) {
        $('#shareWith').parent().removeClass('has-error');
    });
    $('#sharedWithModal').on('show.bs.modal', function(e) {
        var executedConfigId = e.relatedTarget.id;
        $('#executedConfigId').val(executedConfigId);
        $('#shareWith').val('');
        $('#shareWith').parent().removeClass('has-error');

        sharedWithModalShow = true;

    }).on('hide.bs.modal', function(e) {
        sharedWithModalShow = false;
    });

    $('#emailToModal').on('show.bs.modal', function(e) {
        var executedConfigId = e.relatedTarget.id;
        $('#executedConfigId').val(executedConfigId);
        $('#emailUsers').val('');
        $('#emailUsers').parent().removeClass('has-error');
        $('#formatError').hide();

        // clear checkbox for attachemnt formats
        $('.emailOption').prop("checked", false);

        emailToModalShow = true;

    }).on('hide.bs.modal', function(e) {
        emailToModalShow = false;
    });

    var initFilters = function() {
        $('.filterInput').on('keyup', function() { table.draw(); });
        $('#workflowStateFilter').on('change', function() {table.draw()})
    };

    initFilters();

    var init = function() {
        $('#import-file').on('click', function() {
            $('#import-modal').modal({
                show: true
            })
        })
    };

    init()
});

function submitForm() {
    var validInfo = true;
    if (sharedWithModalShow) {
        if ($('#shareWith').select2('val') == '') {
            validInfo = false;
            $('#shareWith').parent().addClass('has-error');
        }
    }

    if (emailToModalShow) {
        if ($('#emailUsers').select2('val') == '') {
            validInfo = false;
            $('#emailUsers').parent().addClass('has-error');
        }
        if ($(document).find('.emailOption:checked').length == 0) {
            validInfo = false;
            $('#formatError').show();
        }
    }

    return validInfo;
}

$.fn.dataTableExt.afnFiltering.push(
    function( oSettings, aData, iDataIndex ) {
        var fromDate = $('#dateFilterFrom').datepicker('getFormattedDate');
        var toDate = $('#dateFilterTo').datepicker('getFormattedDate');
        var colDate = moment(aData[7]).format(DATE_FMT_TZ);

        if ((fromDate == 'Invalid Date' && toDate == 'Invalid Date')||
            (fromDate.length === 0 && toDate.length === 0) ||
            (moment(colDate).isAfter(fromDate) && (toDate == 'Invalid Date' || toDate.length === 0)) ||
            ((fromDate.length === 0 || fromDate == 'Invalid Date') && moment(colDate).isBefore(toDate)) ||
            (moment(colDate).isBetween(fromDate, toDate))) {
            return true
        }
        return false;
    }
);

function getDateFilter(toolbar) {
    var $dateFilters = $('<div class="fuelux row"></div>');
    var $parentDiv =  $('<div class="col-xs-7"></div>');

    var $dateFilterFrom = $('#reportDatepickerFrom').clone();
    $dateFilterFrom.attr('class', 'datepicker col-xs-6');
    $dateFilterFrom.attr('id', 'dateFilterFrom');
    var $fromDateSelected = $($dateFilterFrom).find('#myDatepickerFrom');
    $fromDateSelected.attr('id', 'dateFrom');


    $($dateFilters).append($dateFilterFrom);
    var $dateFilterTo = $('#reportDatepickerTo').clone();
    $dateFilterTo.attr('id', 'dateFilterTo');
    $dateFilterTo.attr('class', 'datepicker col-xs-6');
    var $toDateSelected = $($dateFilterTo).find('#myDatepickerTo');
    $toDateSelected.attr('id', 'dateTo');

    $($dateFilters).append($dateFilterTo);
    $parentDiv.append($dateFilters);
    $(toolbar).append($parentDiv);
}

function getRadios(relatedPage,toolbar) {
    var currentPage = window.location.pathname;
    var radios = '<div class="col-xs-5 radioButtonGroup">';
    //radios += '<label class="add-cursor"><input type="checkbox" name="isPeriodic" value="" id="isPeriodic"/> ' + LABELS.labelIncludePeriodicReportsOnly + '</label>';
    if (relatedPage == 'home') {
        radios = radios + '<label class="no-bold add-cursor"><input id="indexRadio" type="radio" name="relatedReports" onchange="checkStatus(this)" checked/> ' +$.i18n._('labelNew')+'</label>'
    } else {
        radios = radios + '<label class="no-bold add-cursor"><input id="indexRadio" type="radio" name="relatedReports" onchange="checkStatus(this)"/> ' +$.i18n._('labelNew')+'</label>'
    }
    if (relatedPage == 'archived') {
        radios = radios + '<label class="no-bold add-cursor radioAll"><input id="archivedRadio" type="radio" name="relatedReports" onchange="checkStatus(this)" checked/> ' +$.i18n._('labelReviewed')+'</label>'
    } else {
        radios = radios + '<label class="no-bold add-cursor radioAll"><input id="archivedRadio" type="radio" name="relatedReports" onchange="checkStatus(this)"/> ' +$.i18n._('labelReviewed')+'</label>'
    }
    if (relatedPage == 'UnderReview') {
        radios = radios + '<label class="no-bold add-cursor radioAll"><input id="listAllRadio" type="radio" name="relatedReports" onchange="checkStatus(this)" checked/> ' +$.i18n._('labelNonReviewed')+'</label>';
    } else {
        radios = radios + '<label class="no-bold add-cursor radioAll"><input id="listAllRadio" type="radio" name="relatedReports" onchange="checkStatus(this)"/> ' +$.i18n._('labelNonReviewed')+'</label>';
    }
    radios = $(radios);
    $(toolbar).append(radios);
}

function updateStatus(newStatus, rowId) {
    $.ajax({
        type: "GET",
        url: "changeState",
        data: {id: rowId, newState: newStatus},
    })
        .done(function(data) {
            console.log('Load was performed.');
        })
}

function updateDisposition(disposition, rowId) {
    $.ajax({
        type: "GET",
        url: "changeDisposition",
        data: {id: rowId, newDisposition: disposition},
    })
        .done(function(data) {
            console.log('Load was performed.');
        })
}

function updatePriority(priority, rowId) {
    $.ajax({
        type: "GET",
        url: "changePriority",
        data: {id: rowId, newStatus: priority}
    })
        .done(function(data) {
            console.log('Load was performed.');
        })
}

function checkStatus(elem) {
    var checkId = elem.id;
    if (checkId == "archivedRadio") {
        window.location.href = listArchivedUrl;
    } else if (checkId == "indexRadio") {
        window.location.href = listIndexUrl;
    } else if (checkId == "listAllRadio") {
        window.location.href = listAllUrl;
    }
}

var initializeDatePickers = function (table) {
    $('#dateFilterFrom').datepicker({
        allowPastDates: true,
        date: null,
        restricted: [ {from: moment().add(1, 'days'), to: Infinity} ],
        formatDate: function(date) {
            return moment(date).format(DATE_FMT_TZ);
        }
    }).on('click', function () { table.draw(); });

    $('#dateFilterTo').datepicker({
        allowPastDates: true,
        date: null,
        restricted: [ {from: moment().add(1, 'days'), to: Infinity} ],
        formatDate: function(date) {
            return moment(date).format(DATE_FMT_TZ);
        }
    }).on('click', function () { table.draw(); });
};

var filters = [
    function(data) {
        var alertName = data[1]
        var filterValue = $('#alertNameFilter').val()
        if (filterValue) {
            var regx = new RegExp('.*' + filterValue + '.*', 'i')
            return regx.test(alertName)
        }

        return true
    },

    function(data) {
        var detectedDate = data[6]
        var filterValue = $('#detectedDateFilter').val()
        if (filterValue) {
            var regex = new RegExp('.*' + filterValue + '.*', 'i')
            return regex.test(detectedDate)
        }

        return true
    },

    function(data) {
        return true
    }
]

$.fn.dataTable.ext.search.push(
    function( settings, data, dataIndex ) {
        var fv = true

        for(var i=0; i < filters.length; i ++) {
            var nextFv = filters[i](data)
            fv &= nextFv
        }
        return fv
    }
)

Handlebars.registerHelper('option', function(value, label, selectedValue) {
    var selectedProperty = value == selectedValue ? 'selected="selected"' : '';
    return new Handlebars.SafeString(
        '<option value="' + value + '"' +  selectedProperty + '>' + label + "</option>");
});