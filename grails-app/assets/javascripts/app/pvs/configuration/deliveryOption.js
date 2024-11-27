$(function () {
    var create = " ";

    $("#shareWith").select2({
        placeholder: "Select Users"
    });

    $("#emailUsers").select2({
        placeholder: "Share With Email",
        allowClear: true,
        width: "100%",
        formatNoMatches: function (term) {
            if (isEmail(term)) {
                create = "<button id='addEmail' class='btn btn-success'>Add</button>";
            } else {
                create = "Warn: Not a valid email address!";
            }
            return "<input readonly='readonly' class='form-control' id='newEmail' value='" + term + "'>" + create;
        }
    }).parent().find(".select2-drop").on("click", "#addEmail", function () {
        var newTerm = $("#newEmail").val();
        if (isEmail(newTerm)) {
            $("<option>" + newTerm + "</option>").appendTo("#emailUsers");
            var selectedItems = $("#emailUsers").select2("val");
            selectedItems.push(newTerm);
            $("#emailUsers").val(selectedItems);
            $("#emailUsers").select2("close");
        } else {
            $("#newEmail").val("");
            $("#emailUsers").select2("close");
        }
    });

    // TODO: The enter key doesn't work here.
    $("#s2id_autogen1").on('keyup', function (event) {
        if (event.keyCode == 13) {
            $("#addEmail").trigger('click');
        }
    });

});


function isEmail(email) {
    var regex = /\S+@\S+\.\S+/;
    return regex.test(email);
};