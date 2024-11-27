var selectedId;
$(function () {
    $(document).on('click', '.history-justification .editIcon', function (e) {
        updateJustification(e);
    });

    var updateJustification = function (e) {
        var $currentJustificationParent = $(e.target).closest('td');
        if ($(e.target).hasClass('editIcon')) {
            $currentJustificationParent.find('.editedJustification').val($(e.target).closest('.history-justification').find('span').html());
        }
        $currentJustificationParent.find('.edit-justification-box').show();

        $currentJustificationParent.find(".history-justification #confirmHistoryJustification").off().on('click', function () {
            changeJustification($currentJustificationParent.find('.editedJustification').val(), $currentJustificationParent);
        });

        $currentJustificationParent.find('.history-justification #cancelHistoryJustification').off().on('click', function () {
            clearAndHideJustificationEditBox($currentJustificationParent);
        });
    };

    var changeJustification = function (newJustification, $currentJustification) {
        var index = $currentJustification.closest('tr').index();
        var tableId = $currentJustification.closest('table').prop('id');
        var table = $("#" + tableId).DataTable();
        var historyObj = table.rows(index).data()[0];
        if (newJustification && newJustification.trim() !== historyObj.justification.trim()) {
            var $currentJustificationParent = $currentJustification.closest('td');
            var html = $currentJustificationParent.html();
            var data = {id: historyObj.id, newJustification: newJustification.trim(), selectedAlertId: selectedId};
            $.ajax({
                url: updateJustificationUrl,
                type: "POST",
                data: data,
                dataType: 'json',
                beforeSend: function () {
                    $currentJustificationParent.html("<i id='justificationChangeProcessing' class='mdi mdi-spin mdi-loading'></i>");
                },
            })
                .done(function (response) {
                    if (response.status) {
                        historyObj.justification = newJustification;
                    } else {
                        historyObj.justification = '';
                    }
                    table.row(index).data(historyObj).invalidate(); // to update the cell data in the table array
                    clearAndHideJustificationEditBox($currentJustificationParent);
                })
                .fail(function () {
                    $.Notification.notify('error', 'top right', "Error", "Sorry we could not proceed with the request. Kindly contact the administrator.", {autoHideDelay: 5000});
                    table.row(index).data(historyObj).invalidate(); // to update the cell data in the table array
                    clearAndHideJustificationEditBox($currentJustificationParent);
                });
        }
    };

    var clearAndHideJustificationEditBox = function ($currentJustificationParent) {
        $currentJustificationParent.find(".history-justification .edit-justification-box").hide();
        $currentJustificationParent.find('.history-justification .editedJustification').val('');
    };
});
