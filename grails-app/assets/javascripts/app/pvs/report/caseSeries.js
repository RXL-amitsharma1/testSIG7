$(function (event) {

    $("#clipBoardModel").on('show.bs.modal', function(event) {
        var button = $(event.relatedTarget);
        var modal = $(this);
        if(button.data('type') == "report") {
            $.ajax({
                url: "/signal/report/copyCaseNumbersResult",
                data: { "id": $('#elementId').val()},
                dataType: "json",
            })
                .done(function (result) {
                    var formattedResult = result.toString().replace(/,/g ,', ');
                    modal.find('.modal-body textArea').val(formattedResult)
                });

        } else {
            $.ajax({
                url: "/signal/report/copyCaseNumbersConfiguration",
                data: { "id": $('#elementId').val()},
                dataType: "json",
            })
                .done(function (result) {
                    var formattedResult = result.toString().replace(/,/g ,', ');
                    modal.find('.modal-body textArea').val(formattedResult)
                });
        }
    });

});