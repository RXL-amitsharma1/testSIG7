//= require app/pvs/common/rx_common.js
//= require app/pvs/caseDrillDown/caseDrillDown
//= require app/pvs/alertComments/alertComments
//= require app/pvs/productEventHistory/productEventHistoryTable
//= require app/pvs/alertComments/alertComments
//= require app/pvs/common/rx_alert_utils
//= require app/pvs/alerts_review/alert_review
//= require app/pvs/topic/topic_signal_utils

DATE_FMT_TZ = "YYYY-MM-DD";
var index=16;
var dir='asc';
var applicationName = 'Signal Management';
const SUCCESS = 'SUCCESS';
const PRE_REQUISITE_FAIL = 'PRE_REQUISITE_FAIL';
var prev_page = [];

$(function () {
    $(document).on('fieldsRearranged', function (e) {
        $('.yadcf-filter-wrapper').remove();
        init_filter($('#alertsDetailsTable').DataTable())
    })
    var age_grp_over_time_chart;
    var gender_over_time_chart;
    var region_over_time_chart;
    var seriousness_over_time_chart;

    signal.alertReview.setSortOrder();

    sessionStorage.setItem('signalCheckedBox',JSON.stringify([]));
    sessionStorage.setItem('validatedSignalName', JSON.stringify([]));

    $('a[data-toggle="tab"]').on('show.bs.tab', function (e) {
        localStorage.setItem('activeTab', $(e.target).attr('href'));
    });

    function showPopup(data) {
        var evdasDataLogModal = $("#downloadReportModal");
        $(evdasDataLogModal).find("#productName").val(data);
        evdasDataLogModal.modal("show");
    }

    var genAlertNameLink = function (signalId, signalName) {
        return '<a class="word-wrap-break-word" href="' + 'details' + '?' + 'id=' + signalId + '">' + escapeHTML(signalName) + '</a>';
    };

    var getTableElement = function (toolbar) {
        var tableElement = '<div class="radio radio-primary radio-inline m-l-negative"><input class="signalFilterRadio" id="allSignals" type="radio" name="signalGrid" checked/><label class="no-bold" >' + $.i18n._('All') + '</label></div>'
        tableElement = tableElement + '<div class="radio radio-primary radio-inline m-l-negative"><input class="signalFilterRadio" id="dueIn15Signals" type="radio" name="signalGrid" /> <label class="no-bold" >' + $.i18n._('dueIn15') + '</label></div>'
        tableElement = tableElement + '<div class="radio radio-primary radio-inline m-l-negative"><input class="signalFilterRadio" id="openSignals" type="radio" name="signalGrid" /><label class="no-bold" >' + $.i18n._('openSignals') + '</label></div>'
        tableElement = tableElement + '<div class="radio radio-primary radio-inline m-l-negative"><input class="signalFilterRadio" id="nonValidSignals" type="radio" name="signalGrid" /> <label class="no-bold" >' + $.i18n._('nonValid') + '</label></div>'
        tableElement = tableElement + '<div class="radio radio-primary radio-inline m-l-negative"><input class="signalFilterRadio" id="closedSignals" type="radio" name="signalGrid" /><label class="no-bold" >' + $.i18n._('closed') + '</label></div>'
        tableElement = $(tableElement);
        $(toolbar).append(tableElement);
    };

    var getTableButton = function (toolbar) {
        var tableElement = '<a  href="create" class="create btn btn-primary create-signal" id="create-signal" style="float:right">Create Signal</a>';
        tableElement = $(tableElement);
        $(toolbar).append(tableElement);
    };
    var getFilterIcon = function () {
        var right_bar = $('#validatedSignalsTable_filter').parent();
        right_bar.append('<a href="#" id="toggle-column-filters" title="Enable/Disable Filters" class="pv-ic"><i class="fa fa-filter" aria-hidden="true"></i></a>');
    };
    var init = function () {
        $(document).on('click', 'input#select-all', function () {
            $(".copy-select").prop('checked', this.checked);
        });
    };
    init();
    signal.fieldManagement.populateColumnList(gridColumnsViewUrl, gridColumnsViewUpdateUrl);

    $(document).on('click', '.alert-check-box', function () {
        if(!this.checked){
            $('input#select-all').prop('checked', false);
        }
    });
    $(document).on('click', '#validatedSignalsTable_paginate', function () {
        isPagination = true;
        if ($('input#select-all').is(":checked") && !prev_page.includes($('li.active').text().split("\n")[4].trim())) {
            prev_page.push($('li.active').text().split("\n")[4].trim());
        }
        if ((!$('input#select-all').is(":checked") && prev_page.includes($('li.active').text().split("\n")[4].trim()))) {
            var position = prev_page.indexOf($('li.active').text().split("\n")[4].trim());
            prev_page.splice(position, 1);
        }
    });
    var signalListTable = $('#validatedSignalsTable').DataTable({
        language: {
            lengthMenu: 'Show _MENU_ entries'
        },
        layout: {
            topStart: "pageLength",
            topEnd: null,
            bottomStart: [
                "info",
                {
                    paging: {
                        type: "full_numbers",
                    },
                },
            ],
            bottomEnd: null,
        },
        customProcessing: true,
        drawCallback: function () {
            var current_page = $('li.active').text().split("\n")[4];
            if (typeof current_page !== "undefined") {
                current_page = current_page.trim();
            }
            if (typeof prev_page != 'undefined' && $.inArray(current_page, prev_page) == -1) {
                $("input#select-all").prop('checked', false);
            } else {
                $("input#select-all").prop('checked', true);
            }
            removeBorder($("#alertsFilter"));
            sortIconHandler();
            colEllipsis();
            webUiPopInit();

            $(".downloadMemoReport").on('click', function (event) {
                showPopup($(event.target).parent().data('id'));
            });

            closeInfoPopover();
            showInfoPopover();
        },

        initComplete: function () {
            clearCustomSearch();
            var $divToolbar = $('<div class="toolbarDiv col-xs-9" ></div>');
            var $rowDiv = $('<div class=""></div>');
            $divToolbar.append($rowDiv);
            $("#validatedSignalsTable_filter").attr("class", "");
            $($("#validatedSignalsTable_filter").children()[0]).attr("class", "col-xs-offset-0");
            $($("#validatedSignalsTable_filter").children()[0]).attr("style", "font-weight:normal;float:right;width:auto;padding:0");
            $("#validatedSignalsTable_filter").prepend($divToolbar);

            var theDataTable = $('#validatedSignalsTable').DataTable();


            $(".signalFilterRadio").on('click', function () {
                var newUrl = VALIDATED.signalListUrl + "?radioSelected=" + $(this).attr("id");
                theDataTable.clear();
                theDataTable.draw();
                theDataTable.ajax.url(newUrl).load()
            });
            $("#toggle-column-filters, #ic-toggle-column-filters").on('click', function () {
                selectedFilter=false;
                var ele = $("#validatedSignalTableContainer").find('.yadcf-filter-wrapper');
                var inputEle = $('.yadcf-filter');
                if (ele.is(':visible')) {
                    ele.css('display','none');
                } else {
                    ele.css('display','block');
                    inputEle.first().focus();
                }
                theDataTable.draw();

            });
            $('.yadcf-filter-wrapper').hide();
            theDataTable.draw();
            signal.alertReview.enableMenuTooltips();
            signal.alertReview.disableTooltips();
            addGridShortcuts('#validatedSignalsTable');
            signal.fieldManagement.init($('#validatedSignalsTable').DataTable(), '#validatedSignalsTable', 2, true);
            colEllipsis();
            webUiPopInit();
            showInfoPopover();
        },

        "ajax": {
            "url": signalListUrl(),
            "cache": false,
             data: function (args) {
                 let selectedData = $('#alertsFilter').val();
                 if(selectedData) {
                     if(callingScreen == CALLING_SCREEN.DASHBOARD){
                         args.selectedAlertsFilterForDashboard = JSON.stringify(selectedData);
                     }else{
                         args.selectedAlertsFilter = JSON.stringify(selectedData);
                     }
                 }
                 else if(callingScreen == CALLING_SCREEN.DASHBOARD){
                     let retainedData = $("#filterValsForDashboard").val();
                     if(retainedData=="" || retainedData=="null"){
                         retainedData = null;
                     }
                     else {
                         retainedData = JSON.parse(retainedData);
                     }
                     args.selectedAlertsFilterForDashboard = JSON.stringify(retainedData);

                 } else {
                     let retainedData = $("#filterVals").val();
                     if(retainedData=="" || retainedData=="null"){
                         retainedData = null;
                     }
                     else {
                         retainedData = JSON.parse(retainedData);
                     }
                     args.selectedAlertsFilter = JSON.stringify(retainedData);

                 }
                return {
                    "args": JSON.stringify(args)
                };
            },
        },
        serverSide: true,
        "aaSorting": [[0, "desc" ]],
        "bLengthChange": true,
        "iDisplayLength": 25,
        "aLengthMenu": [[25, 50, 100, 200, 500], [25, 50, 100, 200, 500]],
        "oLanguage": {
            "sZeroRecords": "No matching records found", "sEmptyTable": "No data available in table ",
            "oPaginate": {
                "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
            },
        },
        searching: true,
        "aoColumns": [
            {
                "mData": "selected",
                "mRender": function (data, type, row) {
                    var currentlySelectedSignals = JSON.parse(sessionStorage.getItem("signalCheckedBox"));
                    var flag = false;
                    for (var signalId = 0; signalId < currentlySelectedSignals.length; signalId++) {
                        if (row.signalId.toString() == currentlySelectedSignals[signalId]) {
                            flag = true;
                            break
                        }
                    }
                    if (flag) {
                        return '<input type="checkbox" class="alert-check-box editor-active copy-select" data-id=' + row.signalId + ' checked/>';
                    } else {
                        return '<input type="checkbox" class="alert-check-box editor-active copy-select" data-id=' + row.signalId + ' />';
                    }
                },
                "className": "col-min-50 col-max-50",
                "sortable": false,
                "visible": true
            },
            {
                "mData": "name",
                "name" : "name",
                "mRender": function (data, type, row) {
                    return genAlertNameLink(row.signalId, row.signalName)
                },
                'className': 'col-min-150 col-max-150 cell-break',
                "visible": true
            },
            {
                "mData": "productName",
                "name" : "products",
                'className':'col-min-150 col-max-150 cell-break ',
                "visible": signal.fieldManagement.visibleColumns('productName'),
                "mRender" : function(data, type, row) {
                    return addEllipsis(row.productName);

                }
            },
            {
                "mData": "eventName",
                "name" : "events",
                'className':'col-min-150 col-max-150 cell-break',
                "visible": signal.fieldManagement.visibleColumns('eventName'),
                "mRender" : function(data, type, row) {
                    return addEllipsis(row.eventName);
                }
            },
            {
                "mData": "noOfPec",
                "name":"noOfPec",
                "sortable": true,
                "className": "col-min-100 col-max-100",
                "visible": signal.fieldManagement.visibleColumns('noOfPec')
            },
            {
                "mData": "noOfCases",
                "name": "noOfCases",
                "sortable": true,
                "className": "col-min-100 col-max-100",
                "visible": signal.fieldManagement.visibleColumns('noOfCases')
            },
            {
                "mData": "monitoringStatus",
                "name" : "disposition.displayName",
                "className": "col-min-100 col-max-100",
                "visible": signal.fieldManagement.visibleColumns('monitoringStatus')
            },
            {
                "mData": "topicCategory",
                "name" : "topicCategory",
                "sortable": false,
                "className": "col-min-100 col-max-100",
                "visible": signal.fieldManagement.visibleColumns('topicCategory')
            },
            {
                "mData": "assignedTo",
                "name" : "assignedTo.fullName",
                'className':'col-min-150 col-max-150',
                "sortable": false,
                "visible": signal.fieldManagement.visibleColumns('assignedTo')
            },
            {
                "mData": "priority",
                "name" : "priority.displayName",
                "className": "col-min-100 col-max-100",
                "visible": signal.fieldManagement.visibleColumns('priority')
            },
            {
                "mData": "actions",
                "sortable": false,
                "className": "col-min-100 col-max-100",
                "visible": signal.fieldManagement.visibleColumns('actions')
            },
            {
                "mData": "detectedDate",
                "name" : "detectedDate",
                "sortable": true,
                "className": "col-min-150 col-max-150",
                "visible": signal.fieldManagement.visibleColumns('detectedDate')
            },
            {
                "mData": "status",
                "name" : "signalStatus",
                "sortable": true,
                "className": "col-min-100 col-max-100",
                "visible": signal.fieldManagement.visibleColumns('status')
            },
            {
                "mData": "dateClosed",
                "name" : "actualDateClosed",
                "sortable": true,
                'className':'col-min-150 col-max-150 cell-break',
                "visible": signal.fieldManagement.visibleColumns('dateClosed')
            },
            {
                "mData": "dueIn",
                "name" : "dueIn",
                "sortable": false,
                "className": "col-min-100 col-max-100",
                "visible": signal.fieldManagement.visibleColumns('dueIn')
            },
            {
                "mData": "signalSource",
                "name": "initialDataSource",
                "sortable": false,
                "className": "col-min-120 col-max-120",
                "visible": signal.fieldManagement.visibleColumns('signalSource')
            },
            {
                "mData": "actionTaken",
                "name": "actionTaken",
                "sortable": false,
                "className": "col-min-100 col-max-100",
                "visible": signal.fieldManagement.visibleColumns('actionTaken')
            },
            {
                "mData": "signalOutcome",
                "name": "signalOutcome",
                "sortable": false,
                "className": "col-min-120 col-max-120",
                "visible": signal.fieldManagement.visibleColumns('signalOutcome')
            },
            {
                "mData": "signalId",
                "name": "signalId",
                "sortable": true,
                "visible": signal.fieldManagement.visibleColumns('signalId')
            }, {
                "mData": null, "visible": isSignalManagement,"sortable": false, "aTargets": ["signalId"], "mRender": function (data, type, row) {
                    var actionButtonContent = signal.utils.render('signal_management_action_button', build_url_for_signal_management(row));
                    return actionButtonContent
                }, "className": "col-min-100 col-max-100"
            }
        ],
        scrollX: true,
        scrollY: "calc(100vh - 261px)",
        columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }]
    });

    setTimeout(function(){
        init_filter(signalListTable)
    },500)

    var build_url_for_signal_management = function (data) {
        var returnObj = {};
        returnObj.obj_id = data.signalId;
        returnObj.obj_name = data.signalName;
        returnObj.delete_signal_display = $.i18n._('Delete');
        returnObj.isAdmin = isAdmin;
        return returnObj;
    };


    actionButton('#validatedSignalsTable');
    var init_filter = function (data_table) {
        yadcf.init(data_table, [
            {column_number: 1, filter_type: 'text', filter_reset_button_text: false, filter_delay: 600, filter_default_label: ''},
            {column_number: 2, filter_type: "text", filter_reset_button_text: false, filter_delay: 600, filter_default_label: ''},
            {column_number: 3, filter_type: "text", filter_reset_button_text: false, filter_delay: 600, filter_default_label: ''},
            {column_number: 6, filter_type: "text", filter_reset_button_text: false, filter_delay: 600, filter_default_label: ''},
            {column_number: 7, filter_type: "text", filter_reset_button_text: false, filter_delay: 600, filter_default_label: ''},
            {column_number: 8, filter_type: "text", filter_reset_button_text: false, filter_delay: 600, filter_default_label: ''},
            {column_number: 9, filter_type: "text", filter_reset_button_text: false, filter_delay: 600, filter_default_label: ''},
            {column_number: 11, filter_type: "text", filter_reset_button_text: false, filter_delay: 600, filter_default_label: ''},
            {column_number: 12, filter_type: "text", filter_reset_button_text: false, filter_delay: 600, filter_default_label: ''},
            {column_number: 13, filter_type: "text", filter_reset_button_text: false, filter_delay: 600, filter_default_label: ''},
            {column_number: 15, filter_type: "text", filter_reset_button_text: false, filter_delay: 600, filter_default_label: ''},
            {column_number: 16, filter_type: "text", filter_reset_button_text: false, filter_delay: 600, filter_default_label: ''},
            {column_number: 17, filter_type: "text", filter_reset_button_text: false, filter_delay: 600, filter_default_label: ''}
        ]);
        $(".yadcf-filter").removeAttr('placeholder');
        $('.yadcf-filter-wrapper').hide();
    };


    function signalListUrl() {
        if(callingScreen == 'dashboard'){
            return VALIDATED.signalListUrl + "?callingScreen=dashboard";
        }else{
            return VALIDATED.signalListUrl;
        }
    }

    $('.export_icon_signal a, #ic-exportTypes').on('click', function () {
        var format = $(this).data('format');
        var table = $('#validatedSignalsTable').DataTable();
        var $selectedRows = JSON.parse(sessionStorage.getItem("signalCheckedBox"))
        var idList = [];
        if ($selectedRows.length) {
            $.each($selectedRows, function () {
                idList.push(this);
            });
        }
        var jsonObj = JSON.parse(table.ajax.params().args);
        delete jsonObj.selectedAlertsFilter;
        delete jsonObj.selectedAlertsFilterForDashboard;
        var exportUrl = VALIDATED.exportReportUrl + "?outputFormat=" + format + "&idList=" + idList + "&callingScreen="+callingScreen + "&dataTableSearchRequest=" +encodeURIComponent(JSON.stringify(jsonObj));
        $(this).attr('href', exportUrl).data(JSON.stringify(table.ajax.params().args));
    });
    if(iconSeq!="" && iconSeq!=null){
        $.each(jQuery.parseJSON(iconSeq), function (key, value) {
            if (value) {
                $(".pin-unpin-rotate[data-id='" + key + "']").attr('title', 'Unpin');
                $(".pin-unpin-rotate[data-id='" + key + "']").addClass("active-pin");
                $(key).show();
            }
        });
    }

    $(".pin-unpin-rotate").on('click', function () {
        $(this).toggleClass("active-pin");
        togglePinUnpin($(this));
    });
    $(".li-pin-width").on('click', function () {
        $(".ul-ddm").hide();
    });
    $("#reportIconMenu").on("mouseover", function () {
        $(".ul-ddm").show();
    });
    $("#reportIconMenu").on("mouseout", function() {
        $(".ul-ddm").hide();
    });

    $('#alertsFilter').on('change', function() {
        if($('#alertsFilter').val()==null) {
            if(callingScreen === CALLING_SCREEN.DASHBOARD) {
                $("#filterValsForDashboard").val("");
            }else {
                $("#filterVals").val("");
            }
        }
        $("#validatedSignalsTable").DataTable().ajax.reload();
    });
    var _searchTimer = null
    $('#custom-search').on('keyup', function (){
        clearInterval(_searchTimer)
        _searchTimer = setInterval(function (){
            signalListTable.search($('#custom-search').val()).draw() ;
            clearInterval(_searchTimer)
        },1500)

    });

});


$(document).on('change', '.copy-select', function () {
    var dataTable = $('#validatedSignalsTable').DataTable();
    var selectedRowIndex = $(this).closest('tr').index();
    if (this.checked) {
        var currentlySelectedSignals = JSON.parse(sessionStorage.getItem('signalCheckedBox'));
        var currentlySelectedSignalName = JSON.parse(sessionStorage.getItem('validatedSignalName'));
        currentlySelectedSignals.push($(this).attr("data-id"));
        currentlySelectedSignalName.push(dataTable.rows(selectedRowIndex).data()[0].signalName);
        sessionStorage.setItem('validatedSignalName',JSON.stringify(currentlySelectedSignalName))
        sessionStorage.setItem('signalCheckedBox', JSON.stringify(currentlySelectedSignals));
    } else {
        var currentlySelectedSignals = JSON.parse(sessionStorage.getItem('signalCheckedBox'));
        var currentlySelectedSignalName = JSON.parse(sessionStorage.getItem('validatedSignalName'));
        for (var signalId = 0; signalId < currentlySelectedSignals.length; signalId++) {
            if ($(this).attr("data-id") == currentlySelectedSignals[signalId]) {
                currentlySelectedSignals.splice(signalId, 1)
            }
            if (dataTable.rows(selectedRowIndex).data()[0].signalName == currentlySelectedSignalName[signalId]){
                currentlySelectedSignalName.splice(signalId,1)
            }
        }
        sessionStorage.setItem('validatedSignalName',JSON.stringify(currentlySelectedSignalName))
        sessionStorage.setItem('signalCheckedBox', JSON.stringify(currentlySelectedSignals))
    }
});

$(document).on('change', '#select-all', function () {
    var dataTable = $('#validatedSignalsTable').DataTable();
    var currentlySelectedSignals = JSON.parse(sessionStorage.getItem('signalCheckedBox')) || [];
    var currentlySelectedSignalName = JSON.parse(sessionStorage.getItem('validatedSignalName')) || [];
    if (this.checked) {
        $('.copy-select').each(function () {
            var selectedRowIndex = $(this).closest('tr').index();
            var signalId = $(this).attr("data-id");
            var signalName = dataTable.rows(selectedRowIndex).data()[0].signalName;
            if (!currentlySelectedSignals.includes(signalId)) {
                currentlySelectedSignals.push(signalId);
                currentlySelectedSignalName.push(signalName);
            }
        });
    } else {
        var indexSet = new (Set)
        $('.copy-select').each(function () {
            var signalId = $(this).attr("data-id");
            var index = currentlySelectedSignals.indexOf(signalId);
            if (index !== -1) {
                indexSet.add(index)
            }
        });
        currentlySelectedSignals = currentlySelectedSignals.filter((_, index) => !indexSet.has(index));
        currentlySelectedSignalName = currentlySelectedSignalName.filter((_, index) => !indexSet.has(index));
    }
    sessionStorage.setItem('validatedSignalName', JSON.stringify(currentlySelectedSignalName));
    sessionStorage.setItem('signalCheckedBox', JSON.stringify(currentlySelectedSignals));
});



var sortIconHandler = function () {
    var thArray = $('#validatedSignalsTable').DataTable().columns().header();
    $.each(thArray, function (currentIndex, element) {
        if (element.classList.contains('dt-ordering-asc')) {
            element.classList.remove('dt-ordering-asc');
            element.classList.add("sorting");
        } else if (element.classList.contains('dt-ordering-desc')) {
            element.classList.remove('dt-ordering-desc');
            element.classList.add("sorting");
        }
        if (currentIndex == index && !element.classList.contains('sorting_disabled')) {
            if (dir == 'asc') {
                element.classList.remove('sorting');
                element.classList.add("dt-ordering-asc");
            } else if (dir == 'desc') {
                element.classList.remove('sorting');
                element.classList.add("dt-ordering-desc");
            }
        }
    });
};

var togglePinUnpin = function (e1) {  //e1 is dom of span here.
    var id = $(e1).attr("data-id");
    var fieldName = $(e1).attr("data-title");
    var isPinned = false;
    if (e1.hasClass("active-pin")) {
        e1.attr('title', 'Unpin');
        $(id).show();
        isPinned = true;
        fieldName+=" is Pinned";

    } else {
        e1.attr('title', 'Pin to top');
        $(id).hide();
        fieldName+=" is Unpinned";
    }

    $.ajax({
        url: pinUnpinUrl,
        type: "POST",
        dataType: "json",
        data: {fieldName: id, isPinned: isPinned},
    })
        .done(function (data) {
            $.Notification.notify('success', 'top right', "Success", fieldName, {autoHideDelay: 5000});
        });
};

$(document).on('click', '.delete-signal-button', function (event) {
    event.preventDefault();
    var list = []
    list.push(JSON.parse(sessionStorage.getItem("signalCheckedBox")));
    var signalId = $(this).attr('data-instanceid');
    var signalIdList = list[0].length > 0 ? list.join() : signalId
    var $this = $(this);
    var signalNames = JSON.parse(sessionStorage.getItem("validatedSignalName"));
    var signalNamesHtml = signalNames.length >0 ? "" : $this.attr('signal-name') + "<br>";
    signalNames.forEach((signalName) => {
        signalNamesHtml += signalName + "<br>";
    });
    signalNamesHtml += "<br>";
    bootbox.confirm({
        title: 'Delete Signal',
        message: "Are you sure you want to delete the Signal(s)?"+"<p style='font-weight: bolder;'>"+signalNamesHtml+"Justification<p><textarea id='deleteSignalJustification' cols='60' rows='6' name='deleteSignalJustification' class ='deleteSignalJustification' style='height: 60px;" +
            "width: 539px;border-radius: 5px;'></textarea>",
        buttons: {
            confirm: {
                label:'<span class="glyphicon glyphicon-trash icon-white"></span> Delete', className: 'btn btn-danger'
            }, cancel: {
                label: 'Cancel', className: 'btn-default'
            }
        }, callback: function (result) {
            if (result) {
                var data = {};
                var justificationText =  $('#deleteSignalJustification').val()
                if(justificationText && justificationText !== "undefined") {
                    let trimmedJustificationText = justificationText.trim();
                    if (trimmedJustificationText.length === 0) {
                        showEmptyJustificationErrorMessage();
                        return
                    }
                }
                data.signalIdList = signalIdList
                data.justification = justificationText
                var url = VALIDATED.deleteSignalUrl;
                $.ajax({
                    type: "POST", url: url, data: data, dataType: 'json',
                })
                    .done(function (response) {
                        removeExistingMessageHolder();
                        if (response.status) {
                            if (response.data[SUCCESS]) {
                                var message = `${response.message} <br> ` + response.data[SUCCESS].toString()
                                $.Notification.notify('success', 'top right', "Success", message, {autoHideDelay: 5000});
                                var table = $('#validatedSignalsTable').DataTable();
                                sessionStorage.setItem('signalCheckedBox',JSON.stringify([]));
                                sessionStorage.setItem('validatedSignalName', JSON.stringify([]));
                                table.ajax.reload()
                            }
                            if(response.data[PRE_REQUISITE_FAIL]){
                                var discardedSignals = response.data[PRE_REQUISITE_FAIL]
                                for (let i = 0; i < discardedSignals.length; i++) {
                                    let signalId = discardedSignals[i]["id"]
                                    let signalName = discardedSignals[i]["signalName"]
                                    let validatedCaseCounts = discardedSignals[i]["validatedCaseCounts"]
                                    let signalDeletionMessage = discardedSignals[i]["signalDeletionMessage"]
                                    var table = $('#validatedSignalsTable').DataTable();
                                    var message =  signalDeletionMessage +` attached to the current Signal. <a href ="/signal/validatedSignal/validatedObservations?signalId=${signalId}" target="_blank"" class="linked-pec-info-icon" data-instanceid="${signalId}">See Details</a>`
                                    showErrorMsg(message)
                                    sessionStorage.setItem('signalCheckedBox',JSON.stringify([]));
                                    sessionStorage.setItem('validatedSignalName', JSON.stringify([]));
                                    table.ajax.reload();
                                }
                            }
                        } else {
                            if(response.code ==1) {
                                showErrorMsg(response.message)
                            }else if(response.code ==2) {
                                $.Notification.notify('error', 'top right', "Error", response.message, {autoHideDelay: 5000});
                            }
                        }
                    });
            } else {
                event.preventDefault();
            }
        }
    });
    addCountBoxToInputField(8000,$('.deleteSignalJustification'));
});

function getErrorMessageHtml(msg) {
    var alertHtml = '<div class="alert alert-danger alert-dismissible" role="alert"> '
        +
        '<button type="button" class="close" data-dismiss="alert"> ' +
        '<span aria-hidden="true">&times;</span> ' +
        '<span class="sr-only"><g:message code="default.button.close.label" /></span> ' +
        '</button> ' + msg;
    '</div>';
    return alertHtml;
}

function getSuccessMessageHtml(msg) {
    var alertHtml = '<div class="alert alert-success alert-dismissible" role="alert"> ' +
        '<button type="button" class="close" data-dismiss="alert"> ' +
        '<span aria-hidden="true">&times;</span> ' +
        '<span class="sr-only"><g:message code="default.button.close.label" /></span> ' +
        '</button> ' + msg;
    '</div>';
    return alertHtml;
}

function showErrorMsg(msg) {
    // removeExistingMessageHolder();
    var alertHtml = getErrorMessageHtml(msg);
    $('body .messageContainer').prepend(alertHtml);
    $('body').scrollTop(0);
}

function showSuccessMsg(msg) {
    // removeExistingMessageHolder();
    var alertHtml = getSuccessMessageHtml(msg);
    $('body .messageContainer').prepend(alertHtml);
    $('body').scrollTop(0);
}

function removeExistingMessageHolder() {
    $('.messageContainer').html("");
}
function showEmptyJustificationErrorMessage() {
    $.Notification.notify('warning','top right', "Warning", "Justification is mandatory.", {autoHideDelay: 5000});
}
// Function to clear custom search value
function clearCustomSearch() {
    $('#custom-search').val('');
    $("#validatedSignalsTable").DataTable().search('').draw() ;
}


