//= require app/pvs/common/rx_common.js

$(function () {

    var specialPETable
    var initSpecialPETable = function () {
        var columns = create_special_pe_table_columns();
        specialPETable = $('#specialPETable').DataTable({

            responsive: true,
            language: {
                "url": "../assets/i18n/dataTables_" + userLocale + ".json"
            },
            "ajax": {
                "url": specialPEUrl,
                "dataSrc": ""
            },
            "initComplete": function () {
                $(document).on("click", '.delete-record', function (event) {
                    event.preventDefault();
                    var url = $(this).attr('href');
                    bootbox.confirm({
                        message: $.i18n._('deleteThis'),
                        buttons: {
                            confirm: {
                                label: 'Yes',
                                className: 'btn-primary'
                            },
                            cancel: {
                                label: 'No',
                                className: 'btn-default'
                            }
                        },
                        callback: function (result) {
                            if (result) {
                                $.ajax({
                                    type: "GET",
                                    url: url,
                                    dataType: 'json'
                                })
                                    .done(function (data) {
                                        if (data.status) {
                                            window.location.href = createUrl
                                        }
                                });
                            }
                        }
                    });
                });
            },

            "aaSorting": [[2, "desc"]],
            "bLengthChange": true,
            "iDisplayLength": 10,
            "aLengthMenu": [[10, 50, 100, 200, -1], [10, 50, 100, 200, "All"]],
            "aoColumns": columns,
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }]
        });
        actionButton('#specialPETable');
        return specialPETable
    };
    var create_special_pe_table_columns = function () {
        var aoColumns = [
            {
                "mData": "specialProducts",
            },
            {
                "mData": 'specialEvents',
            },
            {
                "mData": 'lastUpdated',
                "mRender": function (data, type, full) {
                    var lastUpdated = new Date(data);
                    return moment(lastUpdated).tz(userTimeZone).format('lll');
                }
            },
            {
                "mData": 'modifiedBy'
            }
        ];
        if (isAdmin) {
            aoColumns.push.apply(aoColumns, [{
                "mData": "",
                "sortable": false,
                "sWidth": "5%",
                "mRender": function (data, type, row) {
                    var url = specialPEeditUrl + "/" + row.id;
                    var deleteUrl = specialPEdeleteUrl + "/" + row.id;
                    var actionButton = '<div class="btn-group dropdown dataTableHideCellContent" align="center"> \
                            <a class="btn btn-success btn-xs" href="' + url + '">Edit</a> \
                            <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                                <span class="caret"></span> \
                                <span class="sr-only">Toggle Dropdown</span> \
                            </button> \
                            <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                                <li role="presentation"><a role="menuitem" href="' + deleteUrl + '" class="delete-record">' + $.i18n._('delete') + '</a></li>';
                    actionButton = actionButton + "</ul></div>"
                    return actionButton;
                }
            }]);
        }
        return aoColumns
    };
        initSpecialPETable();
});