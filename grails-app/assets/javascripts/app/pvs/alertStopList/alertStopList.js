$(function () {
    var alertStoplistTable

    var initalertStoplistTable = function () {
        alertStoplistTable = $('#stopListTable').DataTable({

            responsive: true,
            language: {
                "url": "../assets/i18n/dataTables_" + userLocale + ".json"
            },
            initComplete: function(){
                addGridShortcuts('#stopListTable');
            },
            drawCallback: function (data) {
                    $("[name='activated']").bootstrapSwitch({
                        size: "mini",
                        onColor: "success",
                        offColor: "danger",
                        disabled: !isAdmin
                    });
            },
            "ajax": {
                "url": stopListUrl,
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
            "aaSorting": [[2, "desc"]],
            "bLengthChange": true,
            "iDisplayLength": 10,
            "aLengthMenu": [[10, 50, 100, 200, -1], [10, 50, 100, 200, "All"]],
            "scrollY":"calc(100vh - 461px)",
            "aoColumns": [
                {
                    "mData": 'productName',
                    "mRender": function (data, type, row) {
                        var display = "";
                        for (var label in row.productName) {
                            display = display + "<div><b>" + label.toUpperCase() + " </b>:" + row.productName[label] + "</div>"
                        }
                        return display;
                    }
                },
                {
                    "mData": "eventName",
                    "mRender": function (data, type, row) {
                        var display = "";
                        for (var label in row.eventName) {
                            display = display + "<div><b>" + label.toUpperCase() + " </b>:" + row.eventName[label] + "</div>"
                        }
                        return display;
                    }
                },
                {
                    "mData": 'dateCreated'
                },
                {
                    "mData": "dateDeactivated"
                },
                {
                    "mData": "",
                    "mRender": function (data, type, row) {
                        if (row.activated) {
                            return '<input style="width:87px" data="' + row.activated + '" type="checkbox" class="status" name="activated" listId="' + row.id + '" checked>';
                        } else {
                            return '<input data="' + row.activated + '" type="checkbox"  class="status" name="activated" listId="' + row.id + '">';
                        }
                    }
                }
            ],
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }]
        });
        return alertStoplistTable
    };
    $('#stopListTable').on('switchChange.bootstrapSwitch', 'input[name="activated"]', function (event, state) {
        event.preventDefault();
        event.stopPropagation();
        var $this = $(this);
        var id = $this.attr('listId');
        var token = $("meta[name='_csrf']").attr("content");
        var header = $("meta[name='_csrf_header']").attr("content");
        var url = updateListUrl + "?id=" + id + "&activated=" + state
        $.ajax({
            url: url

        }).done(function (data) {
            alertStoplistTable.ajax.reload()
        }).fail(function (data) {
        })
    });

    initalertStoplistTable()
});

