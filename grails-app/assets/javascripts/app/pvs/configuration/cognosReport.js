$(function () {
    var table = $('#rxTableCognosReport').DataTable({

        "ajax": {
            "url": cognosUrl,
            "dataSrc": ""
        },
        "aaSorting": [[0, "asc"]],
        "bLengthChange": true,
        "iDisplayLength": 50,
        "aLengthMenu": [[50, 100, 200, 500], [50, 100, 200, 500]],
        "aoColumns": [
            {
                "mData": "name",
                "aTargets": ["name"],
                "mRender": function (data, type, row) {
                    return '<a href=' + row.url + '>' + data + '</a>'
                }
            },
            {"mData": "description"}

        ],
        columnDefs: [{
            "targets": '_all',
            "render": $.fn.dataTable.render.text(),
            orderSequence: ['desc', 'asc']
        }]
    });
    actionButton('#rxTableCognosReport');
    loadTableOption('#rxTableCognosReport');
});

