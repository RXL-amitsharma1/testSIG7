$(function () {
    $("#addBulkUsers").on('click', function () {
        $.ajax({
            url: "/signal/user/addUsersToReports",
        })
            .done(function (data) {
                if (data.status === 200) {
                    $.Notification.notify('success', 'top right', "Success", "Users added to PVR", {autoHideDelay: 5000});
                } else {
                    $.Notification.notify('error', 'top right', "Failed", "Users could not be added to PVR", {autoHideDelay: 5000});
                }
                $('#copyBulkUsersModal').modal('hide');
            })
            .fail(function (data) {
                $.Notification.notify('error', 'top right', "Failed", "Users could not be added to PVR", {autoHideDelay: 5000});
                $('#copyBulkUsersModal').modal('hide');
            });
    });

    $('#userTable').DataTable({
        language: {"lengthMenu": "Show _MENU_ entries"},
        layout: {
            topStart: ['pageLength', { search: { placeholder: "Max 100 Characters" } }],
            topEnd: null,
            bottomStart: ['info', {paging: {type: "full_numbers"}}],
            bottomEnd: null,
        },
        customProcessing: true,
        "oLanguage": {
            "url": "../assets/i18n/dataTables_" + userLocale + ".json",
            "oPaginate": {
                "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
            },
        },
        "aLengthMenu": [[25, 50, 100, -1], [25, 50, 100, "All"]],
        "aaSorting": [[4, "desc"]],
        "aoColumnDefs": [
            {"sortable": false, "aTargets": [5,6,7]},
            {
                "targets": '_all',
                orderSequence: ['desc', 'asc']
            }
        ]
    });


});