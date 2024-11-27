$(function () {
    $("#encode-bt").on('click', function () {
        var passwordToBeEncoded = $("#passwordToBeEncoded").val();
        if (passwordToBeEncoded) {
            $.ajax({
                url: encodePasswordLink + "?passwordToBeEncoded=" + passwordToBeEncoded,
            })
                .done(function (data) {
                    if (data.success) {
                        $("#encodeTextBox").show();
                        $("#encodedPassword").val(data.encodedPassword);
                    } else {
                        $.Notification.notify('error', 'top right', "Failed", "Unable to encode password, please contact system admin", {autoHideDelay: 5000});
                    }

                })
                .fail(function () {
                    $.Notification.notify('error', 'top right', "Failed", "Unable to encode password, please contact system admin", {autoHideDelay: 5000});
                    $('#passwordEncoder').modal('hide');
                });
        }else {
            $.Notification.notify('error', 'top right', "Failed", "Enter Password", {autoHideDelay: 5000});
        }

    });
});


$(function () {
    $(".encode-close-bt").on('click', function () {
        $("#encodeTextBox").hide();
        $("#encodedPassword").val("");
        $("#passwordToBeEncoded").val("");
    });

});