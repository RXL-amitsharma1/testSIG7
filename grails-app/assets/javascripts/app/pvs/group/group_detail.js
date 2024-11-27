$(function () {
    $("#addBulkGroups").on('click', function () {
        $.ajax({
            url: "/signal/group/addGroupsToReports",
        })
            .done(function (data) {
                if (data.status === 200) {
                    $.Notification.notify('success', 'top right', "Success", "User Groups added to PVR", {autoHideDelay: 5000});
                } else {
                    $.Notification.notify('error', 'top right', "Failed", "User Groups could not be added to PVR", {autoHideDelay: 5000});
                }
                $('#copyBulkGroupsModal').modal('hide');
            })
            .fail(function (data) {
                $.Notification.notify('error', 'top right', "Failed", "User Groups could not be added to PVR", {autoHideDelay: 5000});
                $('#copyBulkGroupsModal').modal('hide');
            });
    });

    $('.pv-switch input').each((ind, item) => {
        $(item).prop('checked', $(item).attr('data-value') === 'true')
    })

    $('#groupTable').DataTable({
        language: {"lengthMenu":"Show _MENU_ entries"},
        layout: {
            topStart: ['pageLength', 'search'],
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
        "aaSorting": [[3, "desc"]],
        "aoColumnDefs": [
            {"bSearchable": false, "aTargets": [6]},
            {"sortable": false, "aTargets": [6]},
            {
                "targets": '_all',
                orderSequence: ['desc', 'asc']
            }
        ]
    });


});