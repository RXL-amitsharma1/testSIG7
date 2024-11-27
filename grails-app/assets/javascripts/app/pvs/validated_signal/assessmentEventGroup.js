$(function () {
    $(document).on('click', '#saveEventGroupAssessment', function () {
        var eventGroupModal = $("#eventGroupModalAssessment");
        var dictionaryObj = getDictionaryObjectAssessment(EVENT_DICTIONARY);
        var object = {
            'pva': dictionaryObj.getValuesPva()
        };
        var params = {
            id: $("#eventGroupIdAssessment").val(),
            groupName: $("#eventGroupNameAssessment").val(),
            description: $("#eventGroupDescriptionAssessment").val(),
            sharedWith: $("#eventGroupShareWithAssessment").val() != null ? $("#eventGroupShareWithAssessment").val().join(';') : "",
            data: JSON.stringify(object)
        };
        if ($("#saveEventGroupAssessment").text() == "Update") {
            if(!dictionaryObj.getSelectedDicGroups().length) {
                params.copyGroups = dictionaryObj.getValuesDicGroup()[0].id
            }
        } else {
            //copy scenario
            var eventGrpList = [];
            if (dictionaryObj.getValuesDicGroup().length) {
                $.each(dictionaryObj.getValuesDicGroup(), function (k, v) {
                    if (dictionaryObj.getSelectedDicGroups().length) {
                        if (!dictionaryObj.isShowEnabled(v.id)) {
                            eventGrpList.push(v.id)
                        }
                    } else {
                        eventGrpList.push(v.id)
                    }
                });
                params.copyGroups = eventGrpList.join(',')
            }
        }
        if ($("#eventGroupNameAssessment").val() == "" || !$("#eventGroupShareWithAssessment").val()) {
            showErrorMessage("Please fill the required fields.", $('#eventGroupModalAssessment .modal-body'));
        }
        else {
            eventGroupModal.find('.btn-primary').each(function () {
                $(this).prop('disabled', true);
            });
            $.ajax({
                type: "POST",
                url: saveEventGroupUrl,
                data: params,
                dataType: "json",
            })
                .done(function (result) {
                    if (result.status == true) {
                        var successMsg = '<div class="alert alert-success alert-dismissible" role="alert"> ';
                        successMsg += '<button type="button" class="close" data-dismiss="alert"> ';
                        successMsg += '<span aria-hidden="true">&times;</span> ';
                        successMsg += '<span class="sr-only"><g:message code="default.button.close.label" /></span> ';
                        successMsg += '</button> ' + result.message;
                        successMsg += '</div>';
                        eventGroupModal.modal("hide");
                        $('#eventModalAssessment .modal-body').prepend(successMsg);
                        $("#eventGroupSelectAssessment").val('').trigger('change')
                        $("#eventSmqSelectAssessment").val(null).trigger('change');
                        clearDictionaryModal(EVENT_DICTIONARY);
                        dictionaryObj.addToValuesDicGroup({
                            name: result.name,
                            id: result.groupId
                        });
                        console.log(result)
                        dictionaryObj.setEventGroupUpdateText(result);
                        enableDisableEventGroupButtonsAssessment();
                    } else {
                        var errorMsg = '<div class="alert alert-danger alert-dismissible" role="alert"> ';
                        errorMsg += '<button type="button" class="close" data-dismiss="alert"> ';
                        errorMsg += '<span aria-hidden="true">&times;</span> ';
                        errorMsg += '<span class="sr-only"><g:message code="default.button.close.label" /></span> ';
                        errorMsg += '</button> ' + result.message;
                        errorMsg += '</div>';
                        eventGroupModal.modal("hide");
                        $('#eventModalAssessment .modal-body').prepend(errorMsg);
                    }
                    setTimeout(function () {
                        addHideClass($(".alert-success"));
                        addHideClass($(".alert-danger"));
                    }, 5000);
                })
                .fail(function (err) {
                    alert(err);
                    eventGroupModal.modal("hide");
                })
                .always(function(){
                    eventGroupModal.find('.btn-primary').each(function() {
                        $( this ).prop('disabled', false);
                    });
                });
        }
        resetEventGroupModal();
    });

    $("#deleteEventGroupAssessment").on('click', function () {
        var modal = $("#deleteEventGroupModalAssessment");
        console.log("modal event")
        modal.modal("show");
        $('#deleteDlgErrorDivEventGrpAssessment').hide();
        modal.find('#nameToDelete').text('Are you sure you want to delete the event group');
    });

    $("#deleteEventGrpButtonAssessment").on("click", function () {
        var eventGroupModal = $("#eventGroupModalAssessment");
        var dictionaryObj = getDictionaryObjectAssessment(EVENT_DICTIONARY);
        var modal = $("#deleteEventGroupModalAssessment");
        var params = {
            groupName: $("#eventGroupNameAssessment").val(),
            id: $("#eventGroupIdAssessment").val()
        };
        $.ajax({
            type: "POST",
            url: deleteEventGroupUrl,
            data: params,
            dataType: "json",
        })
            .done(function (result) {
                console.log("result", result);
                if (result.status == true) {
                    var successMsg = '<div class="alert alert-success alert-dismissible" role="alert"> ';
                    successMsg += '<button type="button" class="close" data-dismiss="alert"> ';
                    successMsg += '<span aria-hidden="true">&times;</span> ';
                    successMsg += '<span class="sr-only"><g:message code="default.button.close.label" /></span> ';
                    successMsg += '</button> ' + result.message;
                    successMsg += '</div>';
                    modal.modal("hide");
                    eventGroupModal.modal("hide");
                    $('#eventModalAssessment .modal-body').prepend(successMsg);
                    $("#eventGroupSelectAssessment").val('').trigger('change');
                    clearDictionaryModal(EVENT_DICTIONARY);
                    enableDisabledEventGroupButtonsAssessment(dictionaryObj.getValues());
                } else {
                    var errorMsg = '<div class="alert alert-danger alert-dismissible" role="alert"> ';
                    errorMsg += '<button type="button" class="close" data-dismiss="alert"> ';
                    errorMsg += '<span aria-hidden="true">&times;</span> ';
                    errorMsg += '<span class="sr-only"><g:message code="default.button.close.label" /></span> ';
                    errorMsg += '</button> ' + result.message;
                    errorMsg += '</div>';
                    eventGroupModal.modal("hide");
                    modal.modal("hide");
                    $('#eventModalAssessment .modal-body').prepend(errorMsg);
                }
                setTimeout(function () {
                    addHideClass($(".alert-success"));
                    addHideClass($(".alert-danger"));
                }, 5000);
            })
            .fail(function (err) {
                alert(err);
                modal.modal("hide");
                eventGroupModal.modal("hide");
            });
        resetEventGroupModal();
    });


    $(".closeDeleteModalAssessment").on('click', function () {
        $("#deleteModalAssessment").modal("hide");
    });

    var addHideClass = function (row) {
        row.remove();
    };

    $(document).on('click', '.closeEventGroupModalAssessment', function () {
        $("#eventGroupModalAssessment").modal("hide");
        resetEventGroupModal();
    });

    function resetEventGroupModal() {
        $('#eventGroupModalAssessment').on('hidden.bs.modal', function (e) {
            clear_messages();
            $(this).find("input[type=text]").val('').end();
            $(this).find("#eventGroupDescriptionAssessment").val('');
            $(this).find("#eventGroupShareWithAssessment").val(null).trigger('change');
            $(this).find("#eventGroupIdAssessment").val('');
            $(this).find("#eventGroupNameAssessment").val('');
            $(this).find("#eventGroupDescriptionAssessment").val('');
        });
    }

    var clear_messages = function () {
        $("#eventGroupModalAssessment").find('.alert').hide();
    };

    function showErrorMessage(message, modal) {
        var errorMsg = '<div class="alert alert-danger alert-dismissible" role="alert"> ';
        errorMsg += '<button type="button" class="close" data-dismiss="alert"> ';
        errorMsg += '<span aria-hidden="true">&times;</span> ';
        errorMsg += '<span class="sr-only"><g:message code="default.button.close.label" /></span> ';
        errorMsg += '</button> ' + message;
        errorMsg += '</div>';
        modal.prepend(errorMsg);
        setTimeout(function () {
            addHideClass($(".alert-danger"));
        }, 5000);
    }
});