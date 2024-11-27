$(function () {
    $('#scaTable #aggTable #evdasTable #literatureTable').DataTable({

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
        "aoColumnDefs": [{"bSearchable": false, "aTargets": [3, 4, 5, 6]}, {"sortable": false, "aTargets": [2]}]
    });

    var justificationModal = $("#disassociateJustificationPopover")

    $(document).on('click', '.disassociateSignals', function (event) {
        $('#confirmDisassociate').attr('configId', $(this).attr('configId'))
        $('#confirmDisassociate').attr('alertType', $(this).attr('alertType'))
        $('#confirmDisassociate').attr('deleteLatest', $(this).attr('deleteLatest'))
        $this = $(this)
        adjustPopoverPosition($this, justificationModal)
        justificationModal.show()
    });

    function adjustPopoverPosition(parent, div) {
        var position = parent.offset();
        div.css("left", position.left - 510);
        div.css("top", position.top);
    }

    $(document).on('click', '#confirmDisassociate', function (event) {
        event.preventDefault();
        justificationModal.hide()
        var configId = $(this).attr('configId');
        var alertType = $(this).attr('alertType');
        var deleteLatest = $(this).attr('deleteLatest');
        var justification = $('#justification').val();
        var data = {};
        data.configId = configId
        data.alertType = alertType
        data.justification = justification
        data.deleteLatest = deleteLatest
        var url = "/signal/validatedSignal/disassociateSignalsFromExConfig";

        $.ajax({
            type: "GET", url: url, data: data, dataType: 'json',
        })
            .done(function (response) {
                if (response.status) {
                    $.Notification.notify('success', 'top right', "Success", response.message, {autoHideDelay: 5000});
                    setTimeout(function () {
                        window.location.reload();
                    }, 2000);
                } else {
                    $.Notification.notify('error', 'top right', "Error", response.message, {autoHideDelay: 5000});
                }
            });
    });

    $(document).on('click', '#cancelDisassociate', function (event) {
        event.preventDefault();
        $("#justification").val('');
        justificationModal.hide()
    });

});