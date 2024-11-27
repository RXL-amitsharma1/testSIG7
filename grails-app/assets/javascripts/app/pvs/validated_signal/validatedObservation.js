$(function () {
    $('#scaTable #aggTable #literatureTable #adhocTable').DataTable({
        //
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
        $('#confirmDisassociate').attr('alertType', $(this).attr('alertType'))
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
        var signalId = $(this).attr('signalId');
        var alertType = $(this).attr('alertType');
        var justification = $("#justification").val();
        var data = {};
        data.signalId = signalId
        data.alertType = alertType
        data.justification = justification
        var url = "/signal/validatedSignal/disassociateAlertsBySignalId";
        $('#disassociate-spinner').removeClass("hidden");
        $.ajax({
            type: "POST", url: url, data: data, dataType: 'json',
        })
            .done(function (response) {
                if (response.status) {
                    $.Notification.notify('success', 'top right', "Success", response.message, {autoHideDelay: 5000});
                    setTimeout(function () {
                        window.location.reload();
                    }, 1000);
                    $('#disassociate-spinner').addClass("hidden");
                } else {
                    $.Notification.notify('error', 'top right', "Error", response.message, {autoHideDelay: 5000});
                    $('#disassociate-spinner').addClass("hidden");
                }
            });
    });

    $(document).on('click', '#cancelDisassociate', function (event) {
        event.preventDefault();
        $("#justification").val('');
        justificationModal.hide()
    });
});