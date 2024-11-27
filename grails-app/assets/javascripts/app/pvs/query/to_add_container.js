var openContainer
(function() {
    $("#toAddContainer").ready(function() {
        var copyAndPasteDialog = $('#copyAndPasteModal')

        // helper functions for the GUI
        var getCopyAndPasteDelimiter = function() {
            var selectedValue = $('#copyAndPasteModal input:radio[name=delimiter]:checked').val();

            if (selectedValue === "none") {
                return null
            } else if (selectedValue === "others" ) {
                var value = $('#copyAndPasteModal .c_n_p_other_delimiter').val()

                if (_.isEmpty(value))
                    return null
                else
                    return value
            } else
                return selectedValue
        }

        var resetDelimiterToNone = function() {
            $('#copyAndPasteModal input:radio[id=delimiter_none]').prop('checked', true)
        }

        function getCurrentPastedContent() {
           return $("#copyAndPasteModal #copyPasteContent").val()
        }

        function getCurrentSelectedField() {
            var container = document.getElementById("toAddContainer")
            return $(getFieldFromExpression(openContainer)).select2("val")
        }

        function setPasteContent(content) {
            $("#copyAndPasteModal #copyPasteContent").val(content)
        }

        var confirmCopyAndPasteDialog = function() {
            copyAndPasteDialog.off('click.confirm').on('click.confirm', '.confirm-paste', function(evt) {
                evt.preventDefault()

                var container = $('#toAddContainer')[0]
                var confirmButton = $(this)
                confirmButton.parentsUntil('.modal').parent().modal('hide')
                var pastedContent = getCurrentPastedContent()
                var selectedField = getCurrentSelectedField()
                if (selectedField !== 'Select Field') {
                    $(container).trigger("copyAndPaste:paste", [pastedContent])
                }
            })
        }

        var initialize = function() {
            var container = document.getElementById('toAddContainer')

            $("#toAddContainer").on("copyAndPaste:paste", function(evt, data) {
                var editorField = getValueTextFromExpression(container)
                var delimiter = getCopyAndPasteDelimiter()
                var selectedField = getCurrentSelectedField()

                if (delimiter != null) {
                    data = data.replace(new RegExp('\\s*' + delimiter + '\\s*', 'g'), ';')
                }

                if (data != null && !_.isEmpty(data)) {
                    copyAndPastesFields[selectedField] = {
                        field: selectedField,
                        content: data,
                        delimiter: delimiter
                    }
                    $(editorField).val(data)
                    $(container).trigger('query.builder.updateAJAXValues')
                }

            })



            $('.showValue.showSelect').popover({
                content:function() {
                    return $(this).find("#selectValue").val()
                },
                show: true, trigger: 'hover'})

            confirmCopyAndPasteDialog()

            $('#addContainerWithSubmit .modal-link').on('click', function(evt) {
                evt.preventDefault()
                var selectedField = getCurrentSelectedField()
                if (!(AJAXFieldMap[selectedField] === RF_TYPE_DATE ||
                    AJAXFieldMap[selectedField] === RF_TYPE_PART_DATE))
                    $('#copyAndPasteModal').modal('show')
            })

            copyAndPasteDialog.on('change', ':file', function (e) {
                var input = $(this);
                var numFiles = input.get(0).files ? input.get(0).files.length : 0;
                var label = input.val().replace(/\\/g, '/').replace(/.*\//, '');
                var validExts = new Array(".xlsx", ".xls");
                var fileExt = label.substring(label.lastIndexOf('.'));
                if (numFiles > 0) {
                    $('.confirm-paste').hide();
                    $('.import-values').show();
                    if (validExts.indexOf(fileExt.toLowerCase()) < 0) {
                        $('#fileFormatError').show();
                        $('.import-values').attr('disabled', 'disabled');
                    } else {
                        $('#fileFormatError').hide();
                        $('.import-values').removeAttr('disabled');
                    }
                } else {
                    $('#fileFormatError').hide();
                    $('.import-values').hide();
                    $('.confirm-paste').show();
                }
                input.trigger('fileselect', [numFiles, label]);
                e.preventDefault();
                e.stopPropagation();
                return false;
            });

            $(':file').on('fileselect', function (event, numFiles, label) {
                var input = $(this).parents('.input-group').find(':text');
                var log = numFiles > 0 ? label : "";

                if (input.length) {
                    input.val(log);
                }
            });
        }

        initialize();
        $('#copyAndPasteModal').on('show.bs.modal', function(e) {
            setPasteContent('')
            $('#copyAndPasteModal .c_n_p_other_delimiter').val('')
            var radiobtn = $('#copyAndPasteModal').find('input:radio[value="none"]')[0];
            radiobtn.checked = true;

        })
        $('#copyAndPasteModalFilter').on('show.bs.modal', function () {
            $("#createAdvancedFilterModal").css('z-index', 1000);
        });

        $('#copyAndPasteModalFilter').on('hidden.bs.modal', function () {
            $("#createAdvancedFilterModal").css('z-index', 1050);
        });

        $(document).on('click', '.validate-copy-paste', function (evt) {
            var pasteContent = $(".copyAndPasteModal").find('#copyPasteContent').val();
            var delimiter = getCopyAndPasteDelimiter();
            var selectedField = getCurrentSelectedField();

            if (delimiter != null) {
                if (delimiter == '|') {
                    delimiter = '\\|'
                }
                pasteContent = pasteContent.replace(new RegExp('\\s*' + delimiter + '\\s*', 'g'), ';');
            }

            $.ajax({
                url: "/signal/advancedFilter/validateValue",
                type: "POST",
                mimeType: "multipart/form-data",
                data: {
                    "selectedField": selectedField,
                    "values": pasteContent,
                    "executedConfigId": executedConfigId,
                    alertType:applicationName,
                },
            })
                .done(function (resp) {
                    var data=JSON.parse(resp);
                    if (data.success) {
                        showImportedValues( data,  true);
                    }
                })
                .fail(function (e) {
                    console.error(e);
                });
        });

        $(document).on('click', '.confirm-paste', function (evt) {
            evt.preventDefault();
            var currentQEV = $(this).closest('.toAddContainerQEV')[0];

            showHideValue(EDITOR_TYPE_TEXT , openContainer)
            var editorField = getValueTextFromExpression(openContainer);
            var pasteContent = getCurrentPastedContent();
            var delimiter = getCopyAndPasteDelimiter();
            var selectedField = getCurrentSelectedField();

            if (delimiter != null) {
                if (delimiter == '|') {
                    delimiter = '\\|'
                }
                pasteContent = pasteContent.replace(new RegExp('\\s*' + delimiter + '\\s*', 'g'), ';');
            }

            if (pasteContent != null && !_.isEmpty(pasteContent)) {
                $(editorField).val(pasteContent);
                backboneExpressions.at(openContainer.closest('.expression')[0].expressionIndex).set("value", pasteContent);
                $(currentQEV).find('.isFromCopyPaste').val('true');
                if ($(editorField).attr("name").indexOf("templateQuery") == -1) {
                    var nameParts = $($(editorField).parent().parent().find("[name^=templateQuery]")[0]).attr("name").split(".");
                    nameParts[2] = "copyPasteValue";
                    $(editorField).attr("name", nameParts.join("."));
                }
                updateAJAXValues(openContainer);
            } else {
                $(editorField).val("");
            }

        });

        var showImportedValues = function ($response, validate) {
            if (validate) {
                $("#importValuesTemplateContainer").html($response.uploadedValues);
                $(".importValueModal").modal('show');
                confirmImportValues();
                $('#showWarnings').on('click', function () {
                    if ($('.invalidValuesContainer').attr('hidden')) {
                        $('.invalidValuesContainer').removeAttr('hidden');
                    } else {
                        $('.invalidValuesContainer').attr('hidden', 'hidden');
                    }
                });
            }
            else {
                setPasteContent($response.uploadedValues);
            }
            //Select comma(',') as a delimiter
            $('#copyAndPasteModal input:radio[name^=delimiter][value=' + '\\;' + ']').prop('checked', true);
            $('.confirm-paste').show();
            $('.import-values').hide();
        };

        var confirmImportValues = function () {
            $('.importValueModal').off('click.import-values').on('click.import-values', '.confirm-import', function (evt) {
                evt.preventDefault();
                setPasteContent(validValues);
            });
        };


        var importValuesFromExcel = function () {
            copyAndPasteDialog.off('click.import').on('click.import', '.import-values', function (evt) {
                $(".modal-body").append('<div id="compare-screens-spinner"><div class="grid-loading spinner-compare"><img src="/signal/assets/spinner.gif" width="30" align="middle"/></div></div>')
                evt.preventDefault();
                if (copyAndPasteDialog.find(':file').val()) {
                    var file = $('#file_input_filter').val();
                    var jForm = new FormData();
                    jForm.append("file", $('#file_input_filter').get(0).files[0]);
                    jForm.append("selectedField", getCurrentSelectedField());
                    jForm.append("executedConfigId" , executedConfigId)
                    jForm.append("alertType" , applicationName)
                    $.ajax({
                        url: "/signal/advancedFilter/importExcel",
                        type: "POST",
                        data: jForm,
                        mimeType: "multipart/form-data",
                        contentType: false,
                        cache: false,
                        processData: false,
                    })
                        .done(function (resp) {
                            var data=JSON.parse(resp);
                            if (data.success) {
                                $(".modal-body").find("#compare-screens-spinner").remove()
                                showImportedValues(data , true);
                            }
                            else {
                                $(".modal-body").find("#compare-screens-spinner").remove()
                                $('#noDataInExcel,.noDataInExcel').show().text(data.message);
                                setTimeout(function () {
                                    $("#noDataInExcel,.noDataInExcel").hide();
                                }, 10000);
                            }
                        });
                }
            });
        };

        $(document).on('focusout','.expressionValueDateInput', function (evt) {
            $(this).val(newSetDefaultDisplayDateFormat2( $(this).val()));
            if($(this).val()=='Invalid date'){
                $(this).val('')
            }
        })
        importValuesFromExcel();
        modalOpener();

    })
})()