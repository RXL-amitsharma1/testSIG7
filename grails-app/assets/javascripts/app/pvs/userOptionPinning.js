
$(function(){
    if($(".pin-unpin-rotate") != "undefined") {
        $.ajax({
            url: "/signal/viewInstance/fetchPinnedConfs",
            dataType: "json"
        })
            .done(function (data) {
                var pinnedIcons = data.pinnedConfs
                $.each(pinnedIcons, function (key, value) {
                    if (value) {
                        $(".pin-unpin-rotate[data-title='" + value + "']").attr('title', 'Unpin');
                        $(".pin-unpin-rotate[data-title='" + value + "']").addClass("active-pin");
                        var targetId =  $(".pin-unpin-rotate[data-title='" + value + "']").attr("data-id");
                        $(targetId).show()
                    }
                });
        });
    }
    $(".pin-unpin-rotate").on('click', function(){
        $(this).toggleClass("active-pin");
        togglePinUnpin($(this));
    });
    $(".ul-ddm-hide").on('click', function() {
        $(".ul-ddm").hide();
    });
    $("#reportIconMenu").mouseover(function() {
        $(".ul-ddm").show();
    });
    $("#reportIconMenu").mouseout(function() {
        $(".ul-ddm").hide();
    });
});

var togglePinUnpin= function(e1){  //e1 is dom of span here.
    var id = $(e1).attr("data-id");
    var fieldName = $(e1).attr("data-title");
    var isPinned = false;
    var message  = fieldName
    if (e1.hasClass("active-pin")) {
        e1.attr('title', 'Unpin');
        $(id).show();
        isPinned = true;
        message += " is Pinned"

    } else {
        e1.attr('title', 'Pin to top');
        $(id).hide();
        message += " Unpinned"
    }

    $.ajax({
        url: "/signal/viewInstance/savePinConfigurationAlert",
        type: "POST",
        dataType: "json",
        data: {fieldName: fieldName, isPinned: isPinned}
    })
        .done(function (data) {
            $.Notification.notify('success', 'top right', "Success", message, {autoHideDelay: 5000});
    });

}