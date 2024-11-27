function showConceptWidget(isAdmin) {
    $("#concepts").select2({
        placeholder: "Select PV Concepts",
        allowClear: true,
        width: "100%",
        formatNoMatches: function(term) {
            var create = "";
            if (isAdmin) {
                create = "<a href=createTagUrl id='addNew' class='btn btn-success'> "+$.i18n._('create')+"</a>";
            } else {
                create = editMessage;
            }
            return "<input readonly='readonly' class='form-control' id='newTerm' value='" + term + "'>" + create;
        }
    }).parent().find(".select2-drop").on("click","#addNew",function(){
        var newTerm = $("#newTerm").val();
        if ($.trim(newTerm) != "" && isAdmin) {
            $("<option>" + newTerm + "</option>").appendTo("#concepts");
            var selectedItems = $("#concepts").select2("val");
            selectedItems.push(newTerm);
            $("#concepts").val(selectedItems);
            $("#concepts").select2("close");		// close the dropdown
        } else {
            $("#newTerm").val("");
            $("#concepts").select2("close");
        }
    });
}