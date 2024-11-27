$(function () {
    $('#eventModal').on('shown.bs.modal', function () {
        if(!JSON.parse(showEventDic))
        {
            $("#eventGroupSelect").parent().parent().hide();
            $(".ulEventGroup").parent().hide();
            $(".createEventGroup,.updateEventGroup").hide();
            showEventDic=true;
        }
    });

    $("#eventModal").on('hidden.bs.modal', function () {
        const searchIcon = document.getElementsByClassName("iconSearch");
        if (searchIcon && searchIcon.length > 0) {
            searchIcon[0].addEventListener("focusout", (event) => {
                event.target.style.background = "";
            });
        }
    });

    $('#eventModal').on('hide.bs.modal', function () {
        showEventDic=true;
        $("#eventGroupSelect").parent().parent().show();
        $(".ulEventGroup").parent().show();
        $(".createEventGroup,.updateEventGroup").show();
    });
    $(document).on('click', '.searchEventDmv,.searchEventDmv1', function (evt) {
        showEventDic=false;
    });
    $(document).on('click', '.validate-copy-paste', function (evt) {
        var currentQEV = $(this).closest('.toAddContainerQEV')[0];
        var copyAndPasteModalId = $(currentQEV).find('.copyAndPasteModal').attr('id');
        var name=$(this).closest(".toAddContainerQEV").find(".expressionValueText").attr("name");
        if(typeof copyAndPasteModalId ==="undefined"){
            copyAndPasteModalId="copyAndPasteModal";
        }
        if(typeof currentQEV ==="undefined"){
            currentQEV=document;
        }

        var isDmvPopUp=$(this).parent().parent().parent().parent().parent().hasClass("dmvCopyAndPasteModal");
        var qevId;
        if(JSON.parse(isDmvPopUp)){
            qevId="dmv";
        }else{
            qevId =  name.replace(/[^0-9]/g,'');
        }
        var pasteContent = $(currentQEV).find('.copyPasteContent').val();
        var delimiter = getCopyAndPasteDelimiter(currentQEV);
        var selectedField = getCurrentSelectedField(currentQEV);

        if(typeof selectedField==="undefined"){
            selectedField=$("#dataMiningVariable").val();
        }
        if (delimiter != null) {
            if (delimiter == '|') {
                delimiter = '\\|';
            }
            pasteContent = pasteContent.replace(new RegExp('\\s*' + delimiter + '\\s*', 'g'), ';');
        }
        $.ajax({
            url: validateValue,
            type: "POST",
            mimeType: "multipart/form-data",
            data: {
                "selectedField": selectedField,
                "qevId": qevId,
                "values": pasteContent
            },
        })
            .done(function (resp) {
                var data=JSON.parse(resp);
                if (data.success) {
                    showImportedValues(false,currentQEV, data, qevId, true);
                }
            })
            .fail(function (e) {
                console.error(e);
            });
    });

    $(document).on('click', '.confirm-paste', function (evt) {
        evt.preventDefault();
        var currentQEV = $(this).closest('.toAddContainerQEV')[0];
        var dmvFlag=false;
        if(typeof currentQEV ==="undefined"){
            currentQEV=document;
            dmvFlag=true;
        }
        var editorField = getValueTextFromExpression(currentQEV);
        var pasteContent = $(currentQEV).find('.copyPasteContent').val();
        var delimiter = getCopyAndPasteDelimiter(currentQEV);
        var selectedField = getCurrentSelectedField(currentQEV);

        if (delimiter != null) {
            if (delimiter == '|') {
                delimiter = '\\|';
            }
            pasteContent = pasteContent.replace(new RegExp('\\s*' + delimiter + '\\s*', 'g'), ';');
        }

        if (pasteContent != null && !_.isEmpty(pasteContent)) {
            $(editorField).val(pasteContent);
            if(JSON.parse(dmvFlag)){
                $("#selectValue1").val(pasteContent).trigger("change");
                var dmvJson={
                    operator:$("#selectOperator1").val(),
                    value:pasteContent,
                    operatorDisplay: $("#selectOperator1 option:selected").text(),
                    delimiter:delimiter
                }
                $("#dataMiningVariableValue").val(JSON.stringify(dmvJson));
            }else{
                $(currentQEV).find(".qevValue").val(pasteContent)
            }
            $(currentQEV).find('.isFromCopyPaste').val('true');
            removeSelect2();
            updateAJAXValues(currentQEV);
        } else {
            $(editorField).val("");
        }
    });

    function getCurrentSelectedField(container) {
        return $(getFieldFromExpression(container)).val();
    }

    // helper functions for the GUI
    function getCopyAndPasteDelimiter (container) {
        var selectedValue = $(container).find('input:radio[name^=delimiter]:checked').val();
        if (selectedValue === 'none') {
            return null;
        } else if (selectedValue === 'others') {
            var value = $(container).find('.c_n_p_other_delimiter').val();
            if (_.isEmpty(value)) {
                return null;
            }
            else {
                return value;
            }
        } else {
            return selectedValue;
        }
    }

    function updateAJAXValues(container) {
        var field = $(getFieldFromExpression(container)).val();
        var selectValue = getValueSelectFromExpression(container);
        $(selectValue).empty();
            showHideValue(EDITOR_TYPE_TEXT, container);

    }

    $(document).on('click', '.cancel', function (evt) {
        evt.preventDefault();
        var currentQEV = $(this).closest('.toAddContainerQEV')[0];
        $(currentQEV).find('.confirm-paste').show();
        $(currentQEV).find('.import-values').hide();
    });

    $(document).on('click', '.import-values', function (evt) {
        evt.preventDefault();
        var currentQEV = $(this).closest('.toAddContainerQEV')[0];
        var copyAndPasteModalId = $(currentQEV).find('.copyAndPasteModal').attr('id');
        if (typeof copyAndPasteModalId === "undefined") {
            copyAndPasteModalId = "copyAndPasteModal"
        }
        if (typeof currentQEV === "undefined") {
            currentQEV = document
        }
        var qevId = copyAndPasteModalId.slice(copyAndPasteModalId.indexOf('templateQuery'));
        if ($(currentQEV).find(':file').val()) {
            var file = $(currentQEV).find('#file_input');
            var jForm = new FormData();
            jForm.append("file", file.get(0).files[0]);
            jForm.append("qevId", qevId);
            jForm.append("selectedField", $(getFieldFromExpression(currentQEV)).val());
            $.ajax({
                url: importExcel,
                type: "POST",
                data: jForm,
                mimeType: "multipart/form-data",
                contentType: false,
                cache: false,
                processData: false,
            })
                .done(function (data) {
                    $response = JSON.parse(data);
                    if ($response.success) {
                        showImportedValues(true, currentQEV, $response, qevId, true);
                    } else {
                        $(currentQEV).find('#noDataInExcel').show().text($response.message);
                        setTimeout(function () {
                            $(currentQEV).find("#noDataInExcel").hide();
                        }, 10000);
                    }
                });
        }
    });


    var showImportedValues = function (isImported, currentQEV, $response, qevId, validate) {
        if (validate) {
            if (isImported) {
                $(currentQEV).find('.copyPasteContent').val($response.uploadedValues);
            } else {
                $(currentQEV).find("#importValuesTemplateContainer").html($response.uploadedValues);
                var importValueModal = $('#importValueModal' + qevId);
                importValueModal.modal('hide');
                importValueModal.modal('show').css({'z-index': '3000'});
                var invalidValuesContainer = importValueModal.find('.invalidValuesContainer');
                confirmImportValues(currentQEV, importValueModal);
                importValueModal.find('#showWarnings').on('click', function () {
                    if (invalidValuesContainer.attr('hidden')) {
                        invalidValuesContainer.removeAttr('hidden');
                    } else {
                        invalidValuesContainer.attr('hidden', 'hidden');
                    }
                });
            }
        } else {
            $(currentQEV).find('.copyPasteContent').val($response.uploadedValues);
        }
        //Select comma(';) as a delimiter
        $(currentQEV).find('.copyAndPasteModal input:radio[name^=delimiter][value=' + '\\;' + ']').prop('checked', true);
        $(currentQEV).find('.confirm-paste').show();
        $(currentQEV).find('.import-values').hide();
    };

    var confirmImportValues = function (currentQEV, importValueModal) {
        importValueModal.off('click.import-values').on('click.import-values', '.confirm-import', function (evt) {
            evt.preventDefault();
            $(currentQEV).find('.copyPasteContent').val(validValues);

        });
    };
    addPencilIcon();
});

function getIsFromCopyPasteValue(container) {
    return $(container).find('.isFromCopyPaste').val() == 'true';
}

function getCopyPasteIcon(div) {
    return $(div).find('.copy-n-paste');
}

function addCopyPasteModal(toAdd, templateQueryNamePrefix, count) {
    var modalId = 'copyAndPasteModal' + templateQueryNamePrefix.replace(".", "") + 'qev' + count;
    $(toAdd).find('.copy-n-paste').attr('data-target', '#'+modalId).show();
    $(toAdd).find('#copyAndPasteModal').attr('id', modalId);
    $(toAdd).find('input:radio[id=delimiter_none]').attr('checked', 'checked');
    $(toAdd).find('input:radio[name^=delimiter]').attr('name', 'delimiter'+templateQueryNamePrefix.replace(".", "") + 'qev' + count);

    var currentModal = $('#' + modalId);
    fileChange(currentModal);
    fileSelect(modalId);
//Due to query/copyPasteModal.js had to put down off
    $(currentModal).off('show.bs.modal').on('show.bs.modal', function () {
        var currentQEV = $(this).closest('.toAddContainerQEV')[0];
        var valContainer = $(currentQEV).find('.expressionsNoPad:visible')[0];
        var pasteContent = $($(valContainer).find('#selectValue')).val(); // get text value
        if (getFieldIsValidatableFromExpression(currentQEV)) {
            currentModal.find('.validate-copy-paste').removeAttr('disabled');
        } else{
            $('.validate-copy-paste').attr('disabled', 'disabled');
        }
        if (pasteContent == undefined) {
            pasteContent = "";
            var list = $($(valContainer).find('#selectSelect')).val(); // get normal select value
            if (list == undefined) {
                // get auto complete value
                list = $($(this).closest('.queryBlankContainer').find('input.expressionValueSelectAuto')[0]).select2('val');
            }
            if (list != undefined) {
                $.each(list, function() {
                    pasteContent += this + ";"
                });
                pasteContent = pasteContent.substring(0, pasteContent.length-1);
            }
        }

        if (pasteContent != undefined && !_.isEmpty(pasteContent)) {
            $(this).find('input:radio[name^=delimiter][value=";"]').prop('checked', true);
        } else {
            $(this).find('input:radio[name^=delimiter][value="none"]').prop('checked', true);
        }
        $(currentQEV).find('.copyPasteContent').val(pasteContent);
    });
}

var fileChange = function (currentModal) {
    currentModal.on('change', ':file', function () {
        var input = $(this);
        var numFiles = input.get(0).files ? input.get(0).files.length : 0;
        var label = input.val().replace(/\\/g, '/').replace(/.*\//, '');
        var validExts = new Array(".xlsx", ".xls");
        var fileExt = label.substring(label.lastIndexOf('.'));
        if (numFiles > 0) {
            currentModal.find('.confirm-paste').hide();
            currentModal.find('.import-values').show();
            if (validExts.indexOf(fileExt.toLowerCase()) < 0) {
                currentModal.find('#fileFormatError').show();
                currentModal.find('.import-values').attr('disabled', 'disabled');
            } else {
                currentModal.find('#fileFormatError').hide();
                currentModal.find('.import-values').removeAttr('disabled');
                var currentQEV = $(this).closest('.toAddContainerQEV')[0];
            }
        } else {
            currentModal.find('#fileFormatError').hide();
            currentModal.find('.import-values').hide();
            currentModal.find('.confirm-paste').show();
        }
        input.trigger('fileselect', [numFiles, label]);
    });
};

var fileSelect = function (modalId) {
    $(modalId).on('fileselect', function (event, numFiles, label) {
        var input = $(this).parents('.input-group').find(':text');
        var log = numFiles > 0 ? label : "";
        if (input.length) {
            input.val(log);
        }
    });
};

var addPencilIcon = function(){
    var pencilIconCode =  '<i tbindex="0" class="fa fa-pencil-square-o copy-n-paste modal-link" data-toggle="modal" data-target="#copyAndPasteDicModal"></i>';
    if($('#eventSelectionDictionary').length) {
        $('#eventSelectionDictionary >div').children("label").append(pencilIconCode);
    }

    if($('#productDicIng').length){
        $('#productDicIng').append(pencilIconCode);
    }
    var hlt = $("label:contains('HLT')").children("i");
    if(hlt.hasClass("hidden")){
        hlt.removeClass('hidden');
    }
};
