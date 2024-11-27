// TODO: This file is not used. Delete it later.

$(function () {
    var colFields = [];
    var dateFields = [];
    var centerFields = [];
    var result = {};
    var executedTemplateId = $('#executedTemplateId').val();

    $.ajax({
        type: "GET",
        url: getSortOrderUrl + "?currentId=" + executedTemplateId,
        dataType: 'json',
    })
        .done(function (result) {
            initDataTable(result);
        });

    function initDataTable(result) {
        $.getJSON(REPORT.result, function (data) {
            $.each(data["columns"], function (colKey, colValue) {
                var obj = { "title": colValue.name, "data": colValue.name};
                var isDate = colValue.name.toLowerCase().indexOf("date");
                colFields.push(obj);
                if (isDate >= 0) {
                    dateFields.push(colFields.length - 1)
                }
            });

            var table = $('#reportTable').DataTable({
                "ajax": {
                    "url": REPORT.data,
                    "type": "GET"
                },

                initComplete: function () {
                    $('#rxTableCoolReport tbody tr').each(function () {
                        $(this).find('td:eq(3)').attr('nowrap', 'nowrap');
                    });

                    $('.dt-input').val("");
                },
                "aaSorting": result,
                "blengthChange": true,
                "iDisplayLength": 50,
                "aLengthMenu": [[50, 100, 200, 500], [50, 100, 200, 500]],
                "columnDefs": [
                    {
                        "render": function (data, type, full) {
                            var dateFormat = new Date(data);
                            return moment(dateFormat).tz(userTimeZone).format("lll");
                        },
                        "targets": dateFields,
                        "sClass": "dataTableColumnCenter"
                    }
                ]
            });

            loadTableOption('#reportTable');
        });
    }
});
