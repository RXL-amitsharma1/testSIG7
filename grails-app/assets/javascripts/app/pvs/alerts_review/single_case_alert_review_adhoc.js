//= require app/pvs/common/rx_common.js
//= require app/pvs/common/rx_alert_utils.js
//= require app/pvs/common/rx_list_utils.js

var sharedWithModalShow = false;
var emailToModalShow = false;

$(function () {

    insertFilterDropDown($("#search-control"), $("#dropdownMenu1"));
    var genAlertNameLink = function (value, id) {
        return function () {
            return '<a href="' + encodeURI(detailsUrl + '?' + 'callingScreen=review&' +
                signal.utils.composeParams({configId: id})) + '">' + escapeHTML(value) + '</a>'
        }
    };

    var initTable = function (table_rest_url) {
        signal.alerts_utils.get_priorities();
        signal.alerts_utils.get_workflowStates();

        var table = $('.simple-alert-table').DataTable({
            //
            "language": {
                "lengthMenu": 'Show _MENU_ entries',
                "url": "../assets/i18n/dataTables_" + userLocale + ".json",
                "oPaginate": {
                    "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                    "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                    "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                    "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
                },
            },
            layout: {
                topStart: "pageLength",
                topEnd: null,
                bottomStart: ["info", {paging: {type: "full_numbers"}}],
                bottomEnd: null,
            },
            customProcessing: true,
            drawCallback: function (settings){
                removeBorder($("#alertsFilter"));
                colEllipsis();
                webUiPopInit();
                closeInfoPopover();
                showInfoPopover();
            },
            initComplete: function () {

                $(".generate-case-series").off().on("click", function () {
                    var id = $(this).attr("data-id");
                    var name = $(this).attr("data-name");
                    var caseSeriesModal = $("#case-series-modal");
                    caseSeriesModal.modal("show");
                    caseSeriesModal.find("#case-series-name").val(name + id);
                    caseSeriesModal.find(".save-case-series").off().on("click", function () {
                        var seriesName = caseSeriesModal.find("#case-series-name").val();
                        $.ajax({
                            url: generateCaseSeriesUrl + "?id=" + id + "&seriesName=" + seriesName,
                        }).done(function (result) {
                            caseSeriesModal.modal("hide");
                        }).fail(function (err) {
                            alert("Case Series Saving Failed.");
                            caseSeriesModal.modal("hide");
                        })
                    })
                });
                addGridShortcuts('#simpleCaseAlerts');
                showInfoPopover();
            },

            ajax: {
                "url": table_rest_url,
                cache: false,
                data : function (d) {
                    let selectedData = $('#alertsFilter').val();
                    if((Array.isArray(selectedData) && selectedData.length > 0) || (selectedData && !Array.isArray(selectedData))) {
                        d.selectedAlertsFilter = JSON.stringify(selectedData);
                    }
                    else {
                        let retainedData = $("#filterVals").val();
                        if(retainedData=="" || retainedData=="null"){
                            retainedData = null;
                        }
                        else {
                            retainedData = JSON.parse(retainedData);
                        }
                        d.selectedAlertsFilter = JSON.stringify(retainedData);

                    }
                },
                "dataSrc": "aaData"
            },
            "bProcessing": true,
            "bServerSide": true,
            "aaSorting": [[6, "desc"]],
            "bLengthChange": true,
            "iDisplayLength": 25,
            "aLengthMenu": [[25, 50, 100, 200, 500], [25, 50, 100, 200, 500]],
            columns: alert_review_column_data(),
            scrollX: true,
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }]
        });
        return table
    }

    var alert_review_column_data = function () {
        var aoColumns = [
            {
                "mData": "name",
                "className" : 'col-min-200 col-max-200',
                "mRender": function (data, type, row) {
                    return genAlertNameLink(row.name, row.id)
                }
            },
            {
                "mData": "description",
                "mRender": function (data, type, row) {
                    if (row.description) {
                        return addEllipsis(row.description)
                    } else {
                        return "-"
                    }
                },
                "className": 'dt-center col-min-250 col-max-250 cell-break'
            },
            {
                "mData": "productSelection",
                "className": 'col-min-150 col-max-150 cell-break',
                "sortable": false,
                "mRender": function (data, type, row) {
                    return addEllipsis(row.productSelection);
                }
            },
            {
                "mData": "",
                "className": 'col-min-150 col-max-150',
                "mRender": function (data, type, row) {
                    return "<span>" + row.dataSource + "</span>"
                }
            }
        ];
        if (appType == 'Single Case Alert') {
            aoColumns.push({
                "mData": "caseCount",
                "sortable": true
            });
            aoColumns.push({
                "mData": "singleDateRange",
                "className": 'col-min-150 col-max-150',
                "sortable": false
            });
        } else if (appType == 'Aggregate Case Alert') {
            aoColumns.push({
                "mData": "pecCount",
                "sortable": true
            });
            aoColumns.push({
                "mData": "alertPriority",
                "sortable": false
            });
            aoColumns.push({
                "mData": "dateRagne",
                "sortable": false,
                "className": 'col-min-150 col-max-150'
            });

        }
        aoColumns.push({
            "mData": "lastModified",
            "className": 'col-min-150 col-max-150'
        });

        aoColumns.push({
            "mData": "lastExecuted",
            "className": 'col-min-150 col-max-150'
        });

        aoColumns.push({
            "sortable": false,
            "aTargets": ["id"],
            "className": 'col-min-150 col-max-150',
            "mRender": function (data, type, row) {
                if (row.IsShareWithAccess) {
                    var actionButton = '<div class="btn-group dropdown dataTableHideCellContent" align="center"> \
                            <a class="btn btn-success btn-xs" href="" id="' + row.id + '" data-toggle="modal" data-target="#sharedWithModal">' + $.i18n._('labelShare') + '</a> \
                            <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                                <span class="caret"></span> \
                                <span class="sr-only">Toggle Dropdown</span> \
                            </button> \
                            <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                                <li role="presentation"><a role="menuitem" class="del-sod-alert" href="#" data-instancetype="alert" data-toggle="modal" data-target="#deleteModal" data-instanceid="' + row.id + '">' + $.i18n._('labelDelete') + '</a></li> \
                            </ul> \
                        </div>';
                    return actionButton;
                }
                return '';
            }
        });
        return aoColumns
    };


    var table = initTable(listConfigUrl);
    actionButton('#simpleCaseAlerts');
    loadTableOption('#simpleCaseAlerts');
    signal.alerts_utils.initializeShareWithSelect2();
    signal.alerts_utils.initializeShareWithValues();
    $('.outside').hide();

    $('#alertsFilter').on('change', function() {
        if($('#alertsFilter').val()==null) {
            $("#filterVals").val("");
        }
        $(".simple-alert-table").DataTable().ajax.reload();
    });
    var _searchTimer = null
    $('#custom-search').on("keyup", function (){
        clearInterval(_searchTimer)
        _searchTimer = setInterval(function (){
            table.search($('#custom-search').val()).draw() ;
            clearInterval(_searchTimer)
        },1500)

    });
});

var filters = [
    function (data) {
        var alertName = data[1]
        var filterValue = $('#alertNameFilter').val()
        if (filterValue) {
            var regx = new RegExp('.*' + filterValue + '.*', 'i');
            return regx.test(alertName)
        }

        return true
    },

    function (data) {
        return true
    }
];


$.fn.dataTable.ext.search.push(
    function (settings, data, dataIndex) {
        var fv = true;

        for (var i = 0; i < filters.length; i++) {
            var nextFv = filters[i](data);
            fv &= nextFv
        }
        return fv
    }
);

Handlebars.registerHelper('option', function (value, label, selectedValue) {
    var selectedProperty = value == selectedValue ? 'selected="selected"' : '';
    return new Handlebars.SafeString(
        '<option value="' + value + '"' + selectedProperty + '>' + encodeToHTML(label) + "</option>");
});