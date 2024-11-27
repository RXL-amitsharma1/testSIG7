//= require app/pvs/common/rx_common.js
//= require app/pvs/common/rx_alert_utils.js
//= require app/pvs/common/rx_list_utils.js

$(function () {

    insertFilterDropDown($("#search-control"), $("#dropdownMenu1"));
    var genAlertNameLink = function(value, id) {
        return function() {
            return '<a href="' + (detailsUrl + '?' + 'callingScreen=review&'+ signal.utils.composeParams({configId: id})) + '">' + escapeHTML(value) + '</a>'
        }
    };

    var initTable = function(table_rest_url) {

        signal.alerts_utils.get_priorities();
        signal.alerts_utils.get_workflowStates();

        var table = $('#evdasAlerts').DataTable({
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
            "language": {
                "url": "../assets/i18n/dataTables_" + userLocale + ".json",
            },
            initComplete: function () {
                addGridShortcuts('#evdasAlerts');
                showInfoPopover();
            },
            drawCallback: function (settings) {
                removeBorder($("#alertsFilter"));
                colEllipsis();
                webUiPopInit();
                closeInfoPopover();
                showInfoPopover();
                closePopupOnScroll();
            },
            "ajax": {
                "url": table_rest_url,
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
                "dataSrc": ""
            },
            "oLanguage": {
                "oPaginate": {
                    "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                    "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                    "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                    "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
                },
            },
            "aaSorting": [[5, "desc"]],
            "bLengthChange": true,
            "iDisplayLength": 25,
            "aLengthMenu": [[25, 50, 100, 200, 500], [25, 50, 100, 200, 500]],
            "aoColumns": [
                {
                    "mData": "name",
                    "mRender": function(data, type, row) {
                        return genAlertNameLink(row.name, row.id)
                    }
                },
                {
                    "mData": "description",
                    'className': 'dt-center col-min-250 col-max-250 cell-break',
                    "mRender": function(data, type, row) {
                        return addEllipsis(row.description)
                    }
                },
                {
                    "mData": "productSelection",
                    'className': 'dt-center'
                },
                {
                    "mData" : "caseCount",
                    'className': 'dt-center',
                    "orderable": true,
                },
                {
                    "mData" : "dateRagne",
                    "className" : "dt-center",
                    "sortable": false,
                },
                {
                    "mData" : "lastModified",
                    "className" : ""
                },
                {
                    "mData" : "lastExecuted",
                    "className" : ""
                },
                {
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
                                <li role="presentation"><a role="menuitem" class="del-eod-alert"  href="#" data-instancetype="alert" data-toggle="modal" data-target="#deleteModal" data-instanceid="' + row.id + '">' + $.i18n._('labelDelete') + '</a></li> \
                            </ul> \
                        </div>';
                            return actionButton;
                        }
                        return '';
                    }
                }
            ],
            scrollX: true,
            scrollY:'calc(100vh - 260px)',
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }]
        });
        return table
    };


    var table = initTable(listConfigUrl);
    actionButton('#evdasAlerts');
    loadTableOption('#evdasAlerts');
    signal.alerts_utils.initializeShareWithSelect2();
    signal.alerts_utils.initializeShareWithValues();

    $('#alertsFilter').on('change', function() {
        if($('#alertsFilter').val()==null) {
            $("#filterVals").val("");
        }
        $("#evdasAlerts").DataTable().ajax.reload();
    });
    var _searchTimer = null
    $('#custom-search').on('keyup', function (){
        clearInterval(_searchTimer)
        _searchTimer = setInterval(function (){
            table.search($('#custom-search').val()).draw() ;
            clearInterval(_searchTimer)
        },1500)

    });
});

