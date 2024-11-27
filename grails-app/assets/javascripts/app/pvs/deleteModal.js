$(function () {

    $('#deleteModal').on('show.bs.modal', function (event) {

        var button = $(event.relatedTarget); // Button that triggered the modal

        // Extract info from data-* attributes
        var name = button.data('instancename');
        var domainId = button.data('instanceid');
        var extramsg = button.data('extramessage');
        var isEvdas = button.data('isevdas');
        var isLiterature = button.data('isliterature');
        var instanceType = $.i18n._(button.data('instancetype'));

        var modal = $(this);

        //Make sure cancel and delete buttons are enabled
        $(".btn").removeAttr("disabled");

        modal.find('#deleteModalLabel').text("");
        modal.find('#deleteModalLabel').text("Delete " + instanceType);
        modal.find("#deleteComponent").html(instanceType);

        var nameToDeleteLabel = $.i18n._('deleteThis') + instanceType + "?";
        modal.find('#nameToDelete').text("");
        modal.find('#nameToDelete').text(nameToDeleteLabel);

        modal.find('.description').empty();
        modal.find('.description').text(name);

        modal.find('.extramessage').empty();
        if (extramsg) {
            modal.find('.extramessage').html(extramsg);
        }

        //create new action
        var ctx = window.location.pathname;

        var newAction = "/" + ctx.split("/")[1] + "/"  + ctx.split("/")[2] + "/delete/" + domainId;

        if (isEvdas) {
            newAction = "/" + ctx.split("/")[1] + "/" + ctx.split("/")[2] + "/deleteEvdas/" + domainId;
        }else if(isLiterature){
            newAction = "/" + ctx.split("/")[1] + "/" + ctx.split("/")[2] + "/deleteLiterature/" + domainId;
        }

        $("#deleteButton").closest("form").attr("action", newAction);
    });
});



