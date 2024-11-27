REPORT_STATUS_ENUM = {
    NEW: 'NEW',
    NON_REVIEWED: 'NON_REVIEWED',
    REVIEWED: 'REVIEWED'
};
DATE_FMT_TZ = "YYYY-MM-DD";
var sharedWithModalShow = false;
var emailToModalShow = false;

$(function () {

    var relatedPage = $('#relatedReports').val();
    var relatedUrl;
    if (relatedPage == "home") {
        relatedUrl = indexReportUrl
    } else if (relatedPage == "archived") {
        relatedUrl = archivedReportUrl
    } else if (relatedPage == "UnderReview") {
        relatedUrl = allReportUrl
    }
    var table = $('#rxTableReports').DataTable({

        "language": {
            "url": "../assets/i18n/dataTables_"+userLocale+".json"
        },
        initComplete: function() {
            $('.dt-input').val("");
            var $divToolbar = $('<div class="toolbarDiv col-xs-9"></div>');
            var $rowDiv = $('<div class="row"></div>');
            $divToolbar.append($rowDiv);
            $('#rxTableReports_filter').attr("class","row");
            $($('#rxTableReports_filter').children()[0]).attr("class","col-xs-3 searchBar");
            $("#rxTableReports_filter").prepend($divToolbar);
            //$("div.toolbar").append($divToolbar);
            //$("div.toolbar").attr('class','row');
            getRadios(relatedPage,$rowDiv);
            getDateFilter($rowDiv);

            initializeDatePickers(table);
            $('#rxTableReports tbody tr').each(function() {
                $(this).find('td:eq(3)').attr('nowrap', 'nowrap');
            });
        },

        "ajax": {
            "url": relatedUrl,
            "dataSrc": ""
        },

        "aaSorting": [[ 3, "desc" ]],
        "bLengthChange": true,
        "iDisplayLength": 50,
        "aLengthMenu": [[50, 100, 200, 500], [50, 100, 200, 500]],
        "aoColumns": [
            { "mData": "name",
                "aTargets": ["name"],
                "mRender": function(data, type, row) {
                    var link = showReportUrl + '/' + row.id;
                    return '<a href='+link+'>'+data+'</a>'
                }
            },
            { "mData": "description"},
            { "mData": "owner" },

            { "mData": "dateCreated",
                "aTargets": ["dateCreated"],
                "sClass": "dataTableColumnCenter",
                "mRender": function(data, type, full) {
                    var dateCreated = new Date(data);
                    return moment(dateCreated).tz(userTimeZone).format('lll')
               }
            },
            { "mData": "version",
                "aTargets": ["version"],
                "sClass": "dataTableColumnCenter"
            },
            { "mData": "status",
                "aTargets": ["status"],
                "mRender": function(data, type, row) {
                    var select = '<div><select class="form-control add-cursor" style="height: 30px;" onchange="updateStatus(this.value,'+ row.id +')">';
                    if (data.name === REPORT_STATUS_ENUM.NEW) {
                        select = select + '<option value="NEW" selected> ' +$.i18n._('labelNew')+'</option>'
                    } else {
                        select = select + '<option value="NEW">'+$.i18n._('labelNew')+'</option>'
                    }
                    if (data.name === REPORT_STATUS_ENUM.NON_REVIEWED) {
                        select = select + '<option value="NON_REVIEWED" selected>'+$.i18n._('labelNonReviewed')+'</option>'
                    } else {
                        select = select + '<option value="NON_REVIEWED">'+$.i18n._('labelNonReviewed')+'</option>'
                    }
                    if (data.name === REPORT_STATUS_ENUM.REVIEWED) {
                        select = select + '<option value="REVIEWED" selected>'+$.i18n._('labelReviewed')+'</option>'
                    } else {
                        select = select + '<option value="REVIEWED">'+$.i18n._('labelReviewed')+'</option>'
                    }
                    select = select + '</select></div>';
                    return select;
                }
            },
            { "mData": "dateCreated",
                "aTargets": ["dateCreated"],
                "bVisible": false, // this is a hack for date filter
                "mRender": function(data, type, full) {
                    var dateCreated = new Date(data);
                    return moment(dateCreated).tz(userTimeZone).format(DATE_FMT_TZ)
                }
            },
            { "mData": null,
                "sortable": false,
                "aTargets": ["id"],
                "mRender": function(data, type, row) {
                    var actionButton = '<div class=" hidden-btn btn-group dropdown dataTableHideCellContent" align="center"> \
                    <a class="btn btn-success btn-xs" href="' + showReportUrl + '/' + data["id"] + '">' +$.i18n._('view')+'</a> \
                            <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                            <span class="caret"></span> \
                            <span class="sr-only">Toggle Dropdown</span> \
                            </button> \
                            <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                                <li role="presentation"><a role="menuitem" href="" id="'+row.id+'" data-toggle="modal" data-target="#sharedWithModal">' + $.i18n._('labelShare') + '</a></li> \
                                <li role="presentation"><a role="menuitem" href="" id="'+row.id+'" data-toggle="modal" data-target="#emailToModal">' + $.i18n._('labelEmailTo') + '</a></li> \
                                <li role="presentation"><a role="menuitem" href="' + LINKS.toWord + '&id=' + row.id + '">' + $.i18n._('labelExportTo') + ' Word</a></li> \
                                <li role="presentation"><a role="menuitem" href="' + LINKS.toExcel + '&id=' + row.id + '">' + $.i18n._('labelExportTo') + ' Excel</a></li> \
                                <li role="presentation"><a role="menuitem" href="' + LINKS.toPDF + '&id=' + row.id + '">' + $.i18n._('labelExportTo') + ' PDF</a></li> \
                                <li role="presentation"><a role="menuitem" href="#" data-toggle="modal" \
                                        data-target="#deleteModal" data-instancetype="' + $ .i18n._('configuration') + '" data-instanceid="' + row.id + '" data-instancename="' + data["name"] + '">' + $.i18n._('labelDelete') + '</a></li> \
                                </ul> \
                        </div>';
                    return actionButton;
                }
            }],
        columnDefs: [{
            "targets": '_all',
            "render": $.fn.dataTable.render.text(),
            orderSequence: ['desc', 'asc']
        }]
        //"sDom": 'f<"toolbar">rtip'
    });

    $('#dateFilterFrom').on('changed.fu.datepicker', function () {
        console.log('does not work...This is the right way to do it.');
        table.draw();
    });
    $('#dateFilterTo').on('changed.fu.datepicker', function () {
        console.log('does not work...This is the right way to do it.');
        table.draw();
    });
//    $('#rxTableReports tbody').on('click', 'td:not(:last-child)', function () {
//        var id = table.row($(this).parent()).data().id;
//        var link = showReportUrl + '/' + id;
//        document.location.href = link;
//    });
    actionButton( '#rxTableReports' );
    loadTableOption('#rxTableReports');

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

        $.ajax({
            cache: false,
            type: 'GET',
            url: getSharedWith + '?id=' + executedConfigId,
        })
            .done(function(result) {
                var users = '';
                $.each(result, function() {
                    users += this.fullName +' (' + this.username + ') <br />'
                });
                $('#sharedWithList').html(users);
            });

        sharedWithModalShow = true;

    }).on('hide.bs.modal', function(e) {
        sharedWithModalShow = false;
    });


    // For email to modal
//    $('#emailUsers').select2().on("change", function (e) {
//        $('#emailUsers').parent().removeClass('has-error');
//    });
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
//
//        // Todo: Remove dash and then compare. Validation. Get date column dynamically.
//
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
        url: updateStatusUrl,
        data: {id:rowId, reportStatus:newStatus},
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
