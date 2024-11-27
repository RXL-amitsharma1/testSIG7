function LoadQueryTable() {
    var table = $('#rxTableQueries').DataTable({
        initComplete: function() {
            $('#rxTableQueries tbody tr').each(function(){
                $(this).find('td:eq(4)').attr('nowrap', 'nowrap');
                $(this).find('td:eq(3)').attr('nowrap', 'nowrap');
            });

            $('.dt-input').val("");
        },
        "ajax": {
            "url": "queries",
            "dataSrc": ""
        },
        "aaSorting": [[ 0, "asc" ]],
        "bLengthChange": true,
        "iDisplayLength": 50,
        "aLengthMenu": [[50, 100, 200, 500], [50, 100, 200, 500]],
        "aoColumns": [
            { "mData": "name" },
            { "mData": "description" },
            { "mData": "isPublic",
                "aTargets": ["isPublic"],
                "mRender": function(data, type, full) {
                    if (data == false) return '<div align="center"><img src="assets/lock.png" /></div>';
                    else return null;
                }
            },
            { "mData": "dateCreated" },
            { "mData": "lastUpdated" },
            { "mData": "tag.name" },
            { "mData": null,
                "aTargets": ["id"],
                "mRender": function(data, type, full) {
                    var strEdit = '<button class="btn btn-default btn-xs" type="button" onclick="window.location.href=\'#/templates/addedittemplate:templateId=' + data["id"] +  '\'" >Edit</button>';
                    return strEdit;
                }
            }
        ],
        columnDefs: [{
            "targets": '_all',
            "render": $.fn.dataTable.render.text(),
            orderSequence: ['desc', 'asc']
        }]
    });
}