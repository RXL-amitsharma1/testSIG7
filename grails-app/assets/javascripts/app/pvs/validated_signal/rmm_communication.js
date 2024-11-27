//= require app/pvs/common/rx_common.js
//= require app/pvs/common/rx_handlebar_ext.js
//= require app/pvs/common/rx_alert_utils.js
//= require app/pvs/common/rx_list_utils.js
//= require app/pvs/actions/actions.js
//= require app/pvs/meeting/meeting.js

var rmmTable;
var communicationTable;
var rmmTypeSelector;
var rmmAttachMap ={};
$(function () {

    if(typeof hasReviewerAccess !=="undefined" && !hasReviewerAccess){
        $(".changePriority").removeAttr("data-target");
        $(".changeDisposition").removeAttr("data-target");
    }
    if(typeof hasSignalCreationAccessAccess !== "undefined" && !hasSignalCreationAccessAccess) {
        $(".changeDisposition[data-validated-confirmed=true]").removeAttr("data-target");
    }
    // Browse file upload Start
    $(document).on('click', '#rmmAttachmentFileModal .file-uploader .browse,#emailGenerationModal .file-uploader .browse', function () {
        var fileUploaderElement = $(this).closest('.file-uploader');
        var file = fileUploaderElement.find('.file');
        var fileName = fileUploaderElement.find('.form-control').val();
        file.trigger('click');
    });

    $(document).on('change', '#rmmAttachmentFileModal .file-uploader .file,#emailGenerationModal .file-uploader .browse', function () {
        var currentElement = $(this);
        var inputBox = currentElement.parent('.file-uploader').find('.form-control');
        if(!_.isEmpty(currentElement.val())){
            inputBox.val(currentElement.val().replace(/C:\\fakepath\\/i, ''));//todo remove replace method
        }
        inputBox.trigger('change');
    });

    $(document).on('click', '.editRecord', function (e) {
        e.preventDefault();
        var currRow = $(this).closest('tr');
        currRow.removeClass('readonly');
        currRow.find('.form-control').removeAttr('disabled');
        currRow.find('.select2').next(".select2-container").show();
        currRow.find('.rmmAssignedTo').addClass('hide');
        currRow.find('.remove-edit').removeClass('hide');
        currRow.find('.table-row-edit, .deleteRecord, .table-row-mail').addClass('hide');
        currRow.find('.table-row-saved').removeClass('hide').addClass('hidden-ic');
        currRow.find('.file-uploader .word-wrap-break-word').addClass('hide');
        currRow.find('.file-uploader .input-group').removeClass('hide');
        currRow.find('.file-uploader .form-control').prop('disabled', true).prop('placeholder', 'Attach a File');
        currRow.find('td').eq(0).find('.country').addClass('hide');
        currRow.find('td').eq(0).find('.rmmType').addClass('editRMMType');
        currRow.find('.rmmDescriptionInput,.comment-on-edit').removeClass('hide');
        currRow.find('.rmmDescriptionText,.rmmAttachmentText').toggle();
        currRow.find('.rmmCountryText,.rmmCountryTextInput').toggle();
        currRow.find('.rmmCountryTextInput').removeClass('hide');
        currRow.find('.flag-icon-text').addClass('hide')
        currRow.find('.flag-icon-input').removeClass('hide');
        currRow.find('.due-date').attr('placeholder', 'Select Date');
       if( currRow.find('#CountryTextInputId').val()=="undefined"){
           currRow.find('#CountryTextInputId').val('')
           currRow.find('#CountryTextInputId').removeAttr('title');
       }
        if(!currRow.find('.rmmAssignedTo').text()){
            currRow.find('.assignedToSelect').val('').trigger('change')
        }
        if(!currRow.find('.status').val()){
            currRow.find('.status').prepend($('<option>').val('').text('Select'));
        }
        var currentTableId = currRow.attr('data-id');
        var isRmmTable = currentTableId === 'signal-rmms-table';
        var table = isRmmTable ? rmmTable : communicationTable;

    });

    $(document).on('click', '.remove-edit', function (e) {
        e.preventDefault();
        var currRow = $(this).closest('tr');
        currRow.addClass('readonly');
        currRow.find('.form-control').prop('disabled', true);
        currRow.find('.select2').next(".select2-container").hide();
        currRow.find('.rmmAssignedTo').removeClass('hide');
        currRow.find('.remove-edit').addClass('hide');
        currRow.find('.table-row-edit, .deleteRecord, .table-row-mail').removeClass('hide');
        currRow.find('.table-row-saved').addClass('hide').removeClass('hidden-ic');
        currRow.find('.file-uploader .word-wrap-break-word').removeClass('hide');
        currRow.find('.file-uploader .input-group').addClass('hide');
        currRow.find('.file-uploader .form-control').prop('disabled', false);
        currRow.find('td').eq(0).find('.country').removeClass('hide');
        currRow.find('td').eq(0).find('.rmmType').removeClass('editRMMType');
        currRow.find('.rmmDescriptionInput,.comment-on-edit').addClass('hide');
        currRow.find('.rmmDescriptionText,.rmmAttachmentText').toggle();
        currRow.find('.rmmCountryText,.rmmCountryTextInput').toggle();
        currRow.find('.rmmCountryTextInput').addClass('hide');
        currRow.find('.flag-icon-text').removeClass('hide')
        currRow.find('.flag-icon-input').addClass('hide')
        currRow.find('.due-date').attr('placeholder', '');
        if(!currRow.find('.status').val()){``
            currRow.find('.status').find("option[value='']").remove();
            currRow.find('.status').val('');
        }
        $('#signal-rmms-table').DataTable().ajax.reload();
        $('#signal-communication-table').DataTable().ajax.reload();
    });

    $(document).on('change', '.editRMMType', function () {
        if (countrySpecificRMM.includes($(this).val())) {
            rmmTypeSelector = $(this).closest('.rmm-type');
            $('#countrySelectModal').modal({show: true});
        } else {
            $(this).closest('tr').find('#CountryTextInputId').val("")
            rmmTypeSelector = $(this).closest('.rmm-type');
            rmmTypeSelector.find('.col-md-2').remove();
            rmmTypeSelector.closest('tr').find('.flag-icon-input').remove()
            if($(this).val() === 'Signal Memo'){
                $.Notification.notify('warning', 'top right', "Warning", "Please fill in all the relevant fields except file name, and save the current row. The signal memo report will be generated and attached with this communication.", {autoHideDelay: 5000});
            }
        }
    });

    $('#countrySelectModal').on('hidden.bs.modal', function (e) {
        if ($('#country-select').val()) {
            rmmTypeSelector.find('.country').removeClass('hide')
        }
    });

    $('#attachmentFilePathRmm,#attachmentFilePathComm').prop('disabled', true);
    var currentRow
    $(document).on('click', '.allow-rmm-edit', function (e) {
        currentRow = $(this).closest('tr');
        var rmmRow = $(e.target).closest('tr');
        var attachmentId = rmmRow.find('span[class="newAttachment hide"]').attr("data-id");
        if ((currentRow.find("#attachmentFilePathRmm").attr("data-inputtype")) == 'link' || (currentRow.find("#attachmentFilePathComm").attr("data-inputtype")) == 'link') {
            $("#rmmAttachmentFileModal .file-uploader .browse").prop('disabled', true);
            $("#literature-file-path").prop('disabled', false);
            $('#rmmAttachmentFileModal input[type=radio][value=link]').prop("checked", true);
            $('#literature-file-path').prop({
                placeholder: $.i18n._('placeholder.addReferenceLink')
            });
            $("#attachment-type-name").text('References');
        } else {
            $("#literature-file-path").prop('disabled', true);
            $('#literature-file-path').prop({
                placeholder: $.i18n._('placeholder.attachFile')
            });
            $("#rmmAttachmentFileModal .file-uploader .browse").prop('disabled', false);
            $("#attachment-type-name").text('File Name');
            $('#rmmAttachmentFileModal input[type=radio][value=file]').prop("checked", true);
        }
        var currentAttachmentUrl = '/signal/validatedSignal/fetchCurrentRmmData?attachmentId=' + attachmentId;
        $.ajax({
            type: "POST",
            url: currentAttachmentUrl,
        })
            .done(function (result) {
                if(rmmAttachMap[attachmentId]===undefined){
                    $('#literature-file-path').val(result.savedName);
                    $('#attachment-name').val(result.inputName);
                    $('#currRmmIdForAttachModal').val(attachmentId);
                    rmmAttachMap[attachmentId] = {inputName: result.inputName, savedName: result.savedName};
                }else{
                    $('#literature-file-path').val(rmmAttachMap[attachmentId].savedName);
                    $('#attachment-name').val(rmmAttachMap[attachmentId].inputName);
                    $('#currRmmIdForAttachModal').val(attachmentId);
                }
            })
    });
    $(document).on('click', '#add-file', function () {
        //error message removed as part of PVS-70960
        if ($('#attachment-name').val()) {
            currentRow.find('#attachmentFilePathRmm,#attachmentFilePathComm').val($('#attachment-name').val());
        } else {
            currentRow.find('#attachmentFilePathRmm,#attachmentFilePathComm').val($('#literature-file-path').val());
        }
        var attachmentId = $('#currRmmIdForAttachModal').val();
        var inputName = $('#attachment-name').val();
        var savedName = $('#literature-file-path').val();
        rmmAttachMap[attachmentId] = {inputName: inputName, savedName: savedName};
    });

    var type = $('input[name="file-type"]:checked').val();
    if (type === 'file') {
        $("#literature-file-path").prop('disabled', true);
        $("#rmmAttachmentFileModal .file-uploader .browse").prop('disabled', false);
    } else if (type === 'link') {
        $("#rmmAttachmentFileModal .file-uploader .browse").prop('disabled', true);
        $("#literature-file-path").prop('disabled', false);
    }
    $('input[type=radio][name=file-type]').on('change', function () {
        $("#literature-file-path").val('');
        $("#attachment-name").val('');
        type = $('input[name="file-type"]:checked').val();
        if (type === 'link') {
            $("#rmmAttachmentFileModal .file-uploader .browse").prop('disabled', true);
            $("#literature-file-path").prop('disabled', false);
            $('#literature-file-path').attr('placeholder', 'Add reference link');
            $("#attachment-type-name").text('References')
        } else if (type === 'file') {
            $("#rmmAttachmentFileModal .file-uploader .browse").prop('disabled', false);
            $("#literature-file-path").prop('disabled', true);
            $('#literature-file-path').attr('placeholder', 'Attach a file');
            $(".file-uploader").show();
            $("#attachment-type-name").text('File Name')
        }
    });

    $(document).on("change","#rmmAttachmentFileModal .modal-body .form-group",function(){
        if($("#literature-file-path").val()){
            $("#add-file").attr("data-dismiss", "modal");
        }
        else{
            $("#add-file").attr("data-dismiss", "");
        }
    })

    function getCountryName(countryId){
        var countryName;
        for(var i = 0;i < isoCountries.length;i++){
            if(isoCountries[i].id === countryId){
                countryName = isoCountries[i].text;
            }
        }
        return countryName;
    }

        $('.save-country-modal').on('click', function () {
            var countrySelectVal = $('#country-select').val();
            if(countrySelectVal) {
                if($('.newRowCommunication #rmmType').val()!=''){
                    $('#addNewCountryComm').val(getCountryName(countrySelectVal))
                } else {
                    $('#addNewCountry').val(getCountryName(countrySelectVal))
                }
                rmmTypeSelector.closest('tr').find('#CountryTextInputId').val(getCountryName(countrySelectVal))
                rmmTypeSelector.closest('tr').find('#CountryTextInputId').attr('title',(getCountryName(countrySelectVal)))
                rmmTypeSelector.find('.flag-icon-input').remove();
                rmmTypeSelector.closest('tr').find('.rmmCountryTextInput').append('<span class=" flag-icon flag-icon-input flag-icon-' + countrySelectVal.toLowerCase() + ' flag-icon-squared" title="' + getCountryName(countrySelectVal) +  '" data-country="' + countrySelectVal + '"></span>') //for edit row
                rmmTypeSelector.closest('tr').find('.addCountry').append('<span class=" flag-icon flag-icon-input flag-icon-' + countrySelectVal.toLowerCase() + ' flag-icon-squared" title="' + getCountryName(countrySelectVal) + '" data-country="' + countrySelectVal + '"></span>')  //for new rowFbatch
                rmmTypeSelector.find('.col-md-2').remove();
                rmmTypeSelector.append('<div class="col-md-2 country hide" data-country="' + countrySelectVal + '"></div>');

            }
    });

    $(document).on('click', '.saveRecord', function (e) {
        e.preventDefault();
        var currRow = $(this).closest('tr');
        var currentTableId = currRow.attr('data-id');
        var isRmmTable = currentTableId === 'signal-rmms-table';
        var isCommunicationTable = currentTableId === 'signal-communication-table';
        var table = isRmmTable ? rmmTable : communicationTable;
        var formdata = new FormData();
        if (!currRow.parent().is('tfoot')) {
            var currRowIndex = $(this).closest('tr').index();
            currRowIndex += table.page.info().start;
            formdata.append("signalRmmId", table.rows(currRow).data()[0].id);
            formdata.append("attachmentId", currRow.find('.newAttachment').attr('data-id'))
        }
        var type = currRow.find('.rmmType').val();
        var rmmResp = currRow.find('.assignedToSelect').val();
        var status = currRow.find('.status').val();
        var description = currRow.find('.description').val();
        var dueDate = currRow.find('.due-date').val();
        var invalidValues = [null, undefined, "null", "undefined"];
        var country = (invalidValues.includes(currRow.find('.flag-icon-input:last-child').attr('data-country'))) ? currRow.find('.country').attr('data-country') : currRow.find('.flag-icon-input:last-child').attr('data-country');
        var $attachmentName = isRmmTable ? currRow.find("#attachmentFilePathRmm") : currRow.find("#attachmentFilePathComm");
        if ($attachmentName.val() !== "") {
            formdata.append("inputName", $attachmentName.val());
            $("#rmmAttachmentFileModal .file-uploader").show();
            if($("#rmmAttachmentFileModal input:radio:checked").val() == "file"){
                if ($("#rmmAttachmentFileModal .file-uploader .file")[0].files.length > 0) {
                    var file = $("#rmmAttachmentFileModal .file-uploader .file")[0].files[0];
                    formdata.append("attachments", file);
                }
            } else {
                formdata.append("referenceLink", $("#literature-file-path").val());
            }
        }
        formdata.append("dueDate", dueDate);
        formdata.append("description", description);
        formdata.append("type", type);
        formdata.append("rmmResp", rmmResp);
        formdata.append("status", status);
        if (countrySpecificRMM.includes(type)) {
            formdata.append("country", country);
        } else {
            formdata.append("country", null);
        }
        if (currentTableId === 'signal-communication-table') {
            formdata.append("communicationType", "communication");
        } else {
            formdata.append("communicationType", "rmmType");
        }
        formdata.append("signalId", $("#signalIdPartner").val());
        if ((isRmmTable && (!type || !rmmResp || !status || !dueDate)) || (!isRmmTable && !type)) {
            $.Notification.notify('error', 'top right', "Error", $.i18n._('rmmCommunicationSaveWarning'), {autoHideDelay: 5000});//converted warning message to error message to sync up with PVS-47979
        } else if( (isRmmTable || isCommunicationTable) && ((new Date(dueDate).addDays(1)) <= (new Date())) ){
            $.Notification.notify('warning', 'top right', "Warning", "Due date shall be a future date.", {autoHideDelay: 5000});
        } else {
            $.ajax({
                url: saveSignalRMMs,
                type: "POST",
                mimeType: "multipart/form-data",
                processData: false,
                contentType: false,
                data: formdata,
                beforeSend : function () {
                    $(".saveRecord").prop('disabled',true);
                }
            })
                .done(function (data) {
                    $response = JSON.parse(data);
                    if ($response.status) {
                        $.Notification.notify('success', 'top right', "Success", $.i18n._('rmmCommunicationSaveSuccess'), {autoHideDelay: 5000});
                        table.ajax.reload();
                        if (currRow.parent().is('tfoot')) {
                            if(isRmmTable){
                                $(".newRow").find('.country').remove();
                                $(".newRow").toggle()
                            } else {
                                $(".newRowCommunication").find('.country').remove();
                                $(".newRowCommunication").toggle()
                            }
                        }
                        if (!isRmmTable) {
                            $('#emailGenerationModal').find('#sentTo').val('').trigger('change').prop("disabled", false);
                            $('#emailGenerationModal').find('#subject').val('').prop("disabled", false);
                            $('#emailGenerationModal').find('.file').val('');
                            $('#emailGenerationModal').find('.fileName').val('');
                            $('#emailGenerationModal').find('.btn-file-upload').prop("disabled", false);
                            $('#emailGenerationModal').find('.add-attachment').show();
                            $('#emailGenerationModal').find('.delete-attachment').show();
                            tinyMCE.get('emailContentMessage').setContent('');
                            $('#sendMessage').prop('disabled', false);
                            $('#emailGenResetBtn').prop('disabled',false);
                        }
                    } else {
                        console.log($response.message)
                        $.Notification.notify('error', 'top right', "Error", $response.message, {autoHideDelay: 5000});
                    }
                    $(".saveRecord").prop('disabled',false);
                })
                .fail(function () {
                    $(".saveRecord").prop('disabled',false);
                });
        }
    });

    $(document).on('click', '.deleteRecord', function (e) {
        $(".deleteRecord").prop("disabled",true);
        e.preventDefault();
        var currRow = $(this).closest('tr');
        var currentTableId = currRow.attr('data-id');
        var isRmmTable = currentTableId === 'signal-rmms-table';
        var table = isRmmTable ? rmmTable : communicationTable;
        var rmmType = isRmmTable ? "RMM" : "Communication";
        var formdata = new FormData();
        bootbox.confirm({
            title: 'Delete '+rmmType,
            message: "Are you sure you want to delete this "+rmmType+"?",
            buttons: {
                confirm: {
                    label:'<span class="glyphicon glyphicon-trash icon-white"></span> Delete',
                    className: 'btn-danger'
                },
                cancel: {
                    label: 'Cancel',
                    className: 'btn-default'
                }
            },
            callback: function (result) {
                if (result) {
                    if (currRow.parent().is('tfoot')) {
                        isRmmTable ? $(".newRow").toggle() : $(".newRowCommunication").toggle();
                    } else {
                        var currRowIndex = currRow.index();
                        currRowIndex += table.page.info().start;
                        formdata.append("signalRmmId", table.rows(currRow).data()[0].id)
                    }
                    formdata.append("signalId", $("#signalIdPartner").val());
                    $.ajax({
                        url: deleteSignalRMMs,
                        type: "POST",
                        mimeType: "multipart/form-data",
                        processData: false,
                        contentType: false,
                        data: formdata,
                    })
                        .done(function (data) {
                            $response = JSON.parse(data);
                            if ($response.status) {
                                $.Notification.notify('success', 'top right', "Success", $.i18n._('rmmCommunicationDeleteSuccess'), {autoHideDelay: 5000});
                                table.ajax.reload();
                            } else {
                                $.Notification.notify('error', 'top right', "Error", $response.message, {autoHideDelay: 5000});
                            }
                        })
                }
            },
            complete : function () {
                $(".deleteRecord").prop("disabled",false);
            }
        });
        $(".deleteRecord").prop("disabled",false);

    });

    $(document).on('click', '.add-attachment', function () {
        var section = $("#attachmentSection-followUp");
        var clone = section.children().not("[data-id]").last().clone(true);
        clone.find('input.fileName').val('');
        section.append(clone);
        updateIndexInAttachmentSection(section);
    });

    $(document).on('click', '.delete-attachment', function () {
        var section = $("#attachmentSection-followUp");
        if (section.find('tr').not(".hide").length > 1) {
            var target = $(this);
            var trElement = target.closest('tr.pv-section-record');
            if (trElement.attr("data-id") && !trElement.hasClass("deleted")) {
                trElement.find(".attachmentRow").data("deleted", true);
                trElement.addClass("hide");
            }
            else {
                trElement.remove();
            }
        }
        else {
            section.find('tr').not("[data-id]").find('input').val('');
        }
        updateIndexInAttachmentSection(section);
    });

    function tinymce_updateCharCounter(el, len) {
        if(len>8000){
            var maxLimitedText = tinymce.get(tinymce.activeEditor.id).contentDocument.body.innerText.substr(0,8000)
            tinymce.get(tinymce.activeEditor.id).setContent(maxLimitedText)
        }
        $('#' + el.id).prev().find('.char_count').text(len + '/' + el.settings.max_chars);
    }

    function tinymce_getContentLength() {
        return tinymce.get(tinymce.activeEditor.id).getContent().length;
    }

    bindMultipleSelect2WithUrl($('#sentTo'), sentToListUrl);
    var tinyMCEparams = {
        selector: 'textarea#emailContentMessage',
        height: 300,
        branding: false,
        plugins: 'table link code ',
        menubar: 'edit insert format table tools',
        max_chars: 8000,
        setup: function (editor) {
            //stackover flow link :https://stackoverflow.com/questions/11342921/limit-the-number-of-character-in-tinymce for character counter part
            var allowedKeys = [8, 37, 38, 39, 40, 46]; // backspace, delete and cursor keys
            editor.on('keydown', function (e) {
                if (allowedKeys.indexOf(e.keyCode) !== -1) return true;
                if (tinymce_getContentLength()  >= this.settings.max_chars) {
                    e.preventDefault();
                    e.stopPropagation();
                    return false;
                }
                return true;
            });
            editor.on('keyup', function (e) {
                tinymce_updateCharCounter(this, tinymce_getContentLength());
            });
            editor.on('init', function () {

            });
        },
        init_instance_callback: function () { // initialize counter div
            $('#' + this.id).prev().append('<div class="char_count" style="text-align:right"></div>');
            tinymce_updateCharCounter(this, tinymce_getContentLength());
        }
    };
    tinyMCE.init(tinyMCEparams);

    function updateAttachments(attachments){
        _.each(attachments, function (attachment,index) {
            attachments[index].remove();
        });
    }

    const BYTES_IN_MB = 1048576;

    function handleFileInputChange(fileInput) {
        if (!fileInput || !fileInput.files) {
            return;
        }
        var files = fileInput.files;
        if (files.length > 0) {
            var maxUploadLimitMB = maxUploadLimit / BYTES_IN_MB;
            var file = files[0];
            if (file.size > maxUploadLimit) {
                $.Notification.notify('error', 'top right', "Failed", $.i18n._('fileUploadMaxSizeExceedError', maxUploadLimitMB), {autoHideDelay: 5000});
                $(fileInput).val('');
            }
        }
    }

    $('#emailGenerationModal .file').on('change', function () {
        handleFileInputChange(this);
    });

    $(document).on('click', '.table-row-mail', function () {
        var section = $("#attachmentSection-followUp");
        if (section.find('tr').not(".hide").length > 1) {
            var attachments = section.find("tr").not(".hide");
            updateAttachments(attachments.slice(1,attachments.length));
        }
        var currRow = $(this).closest('tr').index();
        var status = $(this).closest('tr').find('.status').val();
        var currentTableId = $(this).closest('tr').attr('data-id');
        var isCommunicationTable = currentTableId === 'signal-communication-table';
        var emailGenerationModal = $('#emailGenerationModal');
        if (currentTableId === 'signal-communication-table') {
            emailGenerationModal.find('.modal-title').text('Communications');
            currRow += communicationTable.page.info().start;
            var emailLog = communicationTable.rows().data()[currRow].emailLog;
            var emailAttachmentName = communicationTable.rows().data()[currRow].emailAttachmentName;
            var emailAttachmentId = communicationTable.rows().data()[currRow].emailAttachmentId;
            var emailReferenceLink = communicationTable.rows().data()[currRow].emailReferenceLink;
            var isEmailReferenceLink = communicationTable.rows().data()[currRow].isEmailReferenceLink;
            $('#sendMessage').prop('disabled',true);
            $('#emailGenResetBtn').prop('disabled',true);
            emailGenerationModal.find('.btn-file-upload').prop("disabled", true);
            emailGenerationModal.find('.add-attachment').hide();
            emailGenerationModal.find('.delete-attachment').hide();
            emailGenerationModal.find('.attached-file').addClass('hide');
            emailGenerationModal.find('.file-attach').removeClass('hide');
            emailGenerationModal.find('.first-index').removeClass('hide');
            emailGenerationModal.find('.first-header').removeClass('hide');
            if (emailLog) {
                var $sentTo = emailGenerationModal.find('#sentTo');
                $sentTo.find('option').remove();
                $sentTo.val(null).trigger("change");
                var assignedTo = emailLog.assignedTo.split(';');
                $.each(assignedTo, function (i, data) {
                    var option = new Option(data, data, true, true);
                    $sentTo.append(option).trigger('change.select2');
                });
                $sentTo.prop('disabled', true);
                emailGenerationModal.find('#subject').val(emailLog.subject).prop('disabled', true);
                tinyMCE.get('emailContentMessage').setContent(emailLog.body);
                tinyMCE.get('emailContentMessage').setMode('readonly');
                emailGenerationModal.find('.attached-file').removeClass('hide').html('');
                emailGenerationModal.find('.file-attach').addClass('hide');
                emailGenerationModal.find('.first-index').addClass('hide');
                emailGenerationModal.find('.first-header').addClass('hide');
                if(emailAttachmentName){
                    var attachedFile = ''
                    if(emailAttachmentId.includes(',')) {
                        var splitEmailAttachmentId = String(emailAttachmentId).split(',');
                        var splitEmailAttachmentName = String(emailAttachmentName).split(',');
                        for(var i = 0; i<splitEmailAttachmentId.length; i++){
                            attachedFile = attachedFile + "<a class='word-wrap-break-word' href='/signal/attachmentable/download?id=" + splitEmailAttachmentId[i] + "'>" + splitEmailAttachmentName[i] + "</a>" + "<br>"
                        }
                    } else {
                        attachedFile = "<a class='word-wrap-break-word' href='/signal/attachmentable/download?id=" + emailAttachmentId + "'>" + emailAttachmentName + "</a>"
                    }
                    emailGenerationModal.find('.attached-file').removeClass('hide').html(attachedFile);
                    emailGenerationModal.find('.file-attach').addClass('hide')
                }
                if(emailReferenceLink){
                    var link = "<a target='_blank' class='word-wrap-break-word' href='" + isEmailReferenceLink + "'>" + emailReferenceLink + "</a>";
                    tinyMCE.get('emailContentMessage').setContent(emailLog.body + '\n' + link);
                }
            } else {
                emailGenerationModal.find('#signalRmmId').val(communicationTable.rows().data()[currRow].id);
                emailGenerationModal.find('#sentTo').val('').trigger('change').prop("disabled",false);
                emailGenerationModal.find('#subject').val('').prop("disabled",false);
                tinyMCE.get('emailContentMessage').setContent('');
                tinyMCE.get('emailContentMessage').setMode('design');
                var currentCommRowFileName = communicationTable.rows().data()[currRow].fileName;
                emailGenerationModal.find('.file').val('');
                if(currentCommRowFileName){
                    emailGenerationModal.find('.fileName').val(currentCommRowFileName);
                } else {
                    emailGenerationModal.find('.fileName').val('');
                }
                emailGenerationModal.find('#emailGenResetBtn').prop('disabled',false);
                emailGenerationModal.find('.btn-file-upload').prop("disabled", false);
                emailGenerationModal.find('.add-attachment').show();
                emailGenerationModal.find('.delete-attachment').show();
                $('#sendMessage').prop('disabled',false);
                $('#emailGenResetBtn').prop('disabled',false);
                tinyMCE.get('emailContentMessage').setContent('');
            }
        } else {
            emailGenerationModal.find('.modal-title').text('Risk Minimization Measures');
            currRow += rmmTable.page.info().start;
            emailGenerationModal.find('.attached-file').addClass('hide');
            emailGenerationModal.find('.first-index').removeClass('hide');
            emailGenerationModal.find('.first-header').removeClass('hide');
            emailGenerationModal.find('.file-attach').removeClass('hide')
            emailGenerationModal.find('#signalRmmId').val(rmmTable.rows().data()[currRow].id);
            emailGenerationModal.find('#sentTo').val(null).trigger('change').prop('disabled', false);
            emailGenerationModal.find('#subject').val('').prop('disabled', false);
            var currentRowFileName = rmmTable.rows().data()[currRow].fileName;
            emailGenerationModal.find('.file').val('');
            if(currentRowFileName){
                emailGenerationModal.find('.fileName').val(currentRowFileName);
            } else {
                emailGenerationModal.find('.fileName').val('');
            }
            emailGenerationModal.find('#emailGenResetBtn').prop('disabled',false);
            emailGenerationModal.find('.btn-file-upload').prop("disabled", false);
            emailGenerationModal.find('.add-attachment').show();
            emailGenerationModal.find('.delete-attachment').show();
            tinyMCE.get('emailContentMessage').setContent('');
            $('#sendMessage').prop('disabled',false);
        }
        if (!isCommunicationTable && (status !== "Approved" && status !== "Completed")) {
            $.Notification.notify('warning', 'top right', "Warning", $.i18n._('mailAccessDenied'), {autoHideDelay: 5000});
        } else {
            tinymce_updateCharCounter(tinyMCE.get('emailContentMessage'), tinymce_getContentLength());
            emailGenerationModal.modal({show: true});
            emailGenerationModal.on('shown.bs.modal', function() {
                $('.select2-container--default .select2-selection--multiple .select2-selection__rendered').css('width', 'auto');
            });
        }

    });

    $(document).on('click', '#emailGenResetBtn', function () {
        $('#emailGenerationModal').find('#sentTo').val('').trigger('change');
        $('#emailGenerationModal').find('#subject').val('');
        tinyMCE.get('emailContentMessage').setContent('');
        $('#emailGenerationModal').find('.file').val('');
        $('#emailGenerationModal').find('.fileName').val('');
    });

    $('#sentTo').on('change', function () {
        var emailsArray = $(this).val() === null ? [] : $(this).val();
        if (emailsArray.length > 0) {
            if (!EMAIL_REGEX.test(emailsArray[emailsArray.length - 1])) {
                $.Notification.notify('warning', 'top right', "Warning", "Entered email address(s) is not valid.", {autoHideDelay: 5000});
            }
        }
    });

    // validate emails and show error message in case there are invalid emails
    function isEnteredEmailsAreValid() {
        var emailsArray = $('#sentTo').val() === null ? [] : $('#sentTo').val();
        var invalidEmails = "";

        $.each(emailsArray, function (index, value) {
            if (!EMAIL_REGEX.test(value)) {
                invalidEmails += value + ','
            }
        });

        if (invalidEmails.length > 0) {
            $.Notification.notify('error', 'top right', "Error",
                "Entered email address(s) " + invalidEmails.slice(0, -1) + " is not valid.", {autoHideDelay: 5000});
            return false;
        }

        return true;
    }

    $("form#emailGenerationForm").off('submit').on('submit', function (evt) {
        $("#sendMessage").attr("disabled", "disabled");
        evt.preventDefault();
        var formData = new FormData(this);
        formData.append('body', decodeFromHTML(tinyMCE.get('emailContentMessage').getContent()));
        formData.append("signalId", $("#signalIdPartner").val());
        if (!formData.get("sentTo") || !formData.get("subject") || !formData.get("body")) {
            var errorMsg = "<div class='alert alert-danger'>Please fill the required Fields</div>";
            $('#emailGenerationModal .modal-body').prepend(errorMsg);
            setTimeout(function () {
                addHideClass($(".alert-danger"));
                $("#sendMessage").removeAttr("disabled");
            }, 5000);
        } else if (isEnteredEmailsAreValid()) {
            $.ajax({
                url: sendMailUrl,
                type: "POST",
                mimeType: "multipart/form-data",
                processData: false,
                contentType: false,
                data: formData,
            })
                .done(function (data) {
                    $response = JSON.parse(data);
                    if ($response.status) {
                        $.Notification.notify('success', 'top right', "Success", $.i18n._('emailSuccess'), {autoHideDelay: 5000});
                        $('#signal-communication-table').DataTable().ajax.reload();
                        var emailGenerationModal = $('#emailGenerationModal');
                        $('form#emailGenerationForm').trigger('reset');
                        emailGenerationModal.modal('toggle');
                    } else {
                        $.Notification.notify('error', 'top right', "Error", $response.message, {autoHideDelay: 5000});
                        $("#sendMessage").removeAttr("disabled");
                    }
                })
        } else {
            $("#sendMessage").removeAttr("disabled");
        }
    });

    var updateIndexInAttachmentSection = function (section) {
        var index = 1;
        var attachments = section.find("tr").not(".hide");
        _.each(attachments, function (attachment) {
            $(attachment).find('td.new-index').text(index);
            index = index + 1;
        });
    };

    $('.rmmsDatePicker').datepicker({
        allowPastDates: false,
        momentConfig: {
            culture: userLocale,
            tz: userTimeZone,
            format: DEFAULT_DATE_DISPLAY_FORMAT
        }
    });

    $('.due-date').on('focusout', function(){
        $(this).val(newSetDefaultDisplayDateFormat( $(this).val()));
        if($(this).val()=='Invalid date'){
            $(this).val('')
        }
    });


    $('.communicationDatePicker').datepicker({
        allowPastDates: false,
        momentConfig: {
            culture: userLocale,
            tz: userTimeZone,
            format: DEFAULT_DATE_DISPLAY_FORMAT
        }
    });

    $('.rmmType').select2({
        tags : true,
    });
    $('.newRow').hide();
    $('.newRowCommunication').hide();


    communicationTable = $("#signal-communication-table").DataTable({
        layout: {
            topStart: null,
            topEnd: null,
            bottomStart: ['pageLength', 'info', {paging: {type: "full_numbers"}}],
            bottomEnd: null,
        },
        customProcessing: true,
        destroy: true,
        searching: false,
        sPaginationType: "full_numbers",
        responsive: false,
        "aaSorting": [],
        "order": [[ 5, "desc" ]],
        "aLengthMenu": [[10, 20, 50, -1], [10, 20, 50, "All"]],
        "iTotalDisplayRecords": 10,
        serverSide: true,
        language: {
            "url": "../assets/i18n/dataTables_" + userLocale + ".json"
        },
        "ajax": {
            "url": '/signal/validatedSignal/fetchRmms',
            "dataSrc": "aaData",
            data: {'signalId': $("#signalIdPartner").val(), 'type': 'communication'}
        },
        "oLanguage": {
            "oPaginate": {
                "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
            },
            "sLengthMenu": "Show _MENU_",

            "sZeroRecords": "No data available in table", "sEmptyTable": "No data available in table"
        },
        "createdRow": function (row, data, dataIndex) {
            $(row).addClass('readonly');
            $(row).attr('data-id','signal-communication-table');
            var $dateCell = $(row).find('td:eq(5)');
            $dateCell.attr('data-order', data.dueDateSort)
        },

        "drawCallback": function (settings) {
            if($(".newRowCommunication").is(":visible")){
                $("#signal-communication-table .dt-empty").hide();
            }
            colEllipsis();
            webUiPopInit();
            var rowsDataAR = $('#signal-communication-table').DataTable().rows().data();
            if(settings.json != undefined) {
                pageDictionary($('#signal-communication-table_wrapper'), settings.json.recordsFiltered);
                showTotalPage($('#signal-communication-table_wrapper'), settings.json.recordsFiltered);
            }else {
                pageDictionary($('#signal-communication-table_wrapper'), rowsDataAR.length);
                showTotalPage($('#signal-communication-table_wrapper'), rowsDataAR.length);
            }
        },

        "rowCallback": function (row, data, index) {
            //Bind AssignedTo Select Box
            if (data.emailAddress) {
                signal.user_group_utils.bind_assignTo_to_rmm_row($(row), searchUserGroupListUrl, {
                    name: data.emailAddress,
                    id: data.emailAddress
                });
            } else {
                signal.user_group_utils.bind_assignTo_to_rmm_row($(row), searchUserGroupListUrl, {
                    name: data.assignedToFullName,
                    id: (data.isAssignedTo ? "User_" : 'UserGroup_') + data.assignedToId
                });

            }
            $(row).find('.assignedToSelect').next(".select2-container").hide();
            $(row).find('.historyDatePicker').datepicker({
                date: data.dueDate,
                allowPastDates: false,
                momentConfig: {
                    culture: userLocale,
                    tz: serverTimeZone,
                    format: DEFAULT_DATE_DISPLAY_FORMAT
                }
            });
            $(row).find('.rmmType').select2({
                tags : true
            });

            if(!data.emailSent && (data.colorDueDate === 'orange' || data.colorDueDate === 'red')) {
                $(row).find('.due-date').css('color', data.colorDueDate);
            }
            if(!data.dueDate){
                $(row).find('.due-date').attr('placeholder', '');
            }
            if(!data.status){
                $(row).find('.status').val('');
            }
        },

        "bAutoWidth": false,
        "aoColumns": [
            {
                "mData": "type",
                "orderDataType": "dom-select",
                "className":"cell-break",
                "mRender": function (data, type, row) {
                    var $select = $("<select></select>", {
                        "value": row.type,
                        "class": "form-control rmmType",
                        "disabled": "true"
                    });
                    var $noSelection = $("<option></option>", {
                        "text": '',
                        "value": ''
                    });
                    $select.append($noSelection);
                    $.each(communicationType, function (k, v) {
                        var $option = $("<option></option>", {
                            "text": v,
                            "value": v
                        });
                        if (row.type === v) {
                            $option.attr("selected", "selected")
                        }
                        $select.append($option);
                    });

                    if($.inArray(row.type, communicationType) === -1){
                        var $option = $("<option></option>", {
                            "text": row.type,
                            "value": row.type
                        });
                        $option.attr("selected", "selected");
                        $select.append($option);
                    }

                    var type = '<div class="rmm-type row"><div>' + $select.prop("outerHTML") + '</div>';
                   /* if (row.country) {
                        type += '<div class="col-md-2 country" data-country="' + row.country + '"><span class="flag-icon flag-icon-' + row.country.toLowerCase() + ' flag-icon-squared" title="' + getCountryName(row.country) + '"></span></div>';
                    }*/
                    type += "</div>";
                    return type
                }
            },
            {
                "mData": "country",
                "className":"cell-break",
                "mRender": function (data, type, row) {
                    var country='<span  class="rmmCountryText" style="white-space: pre-wrap">'+getCountryName(row.country)+'&nbsp'+'</span>'
                    var hideCountryVal=''
                    if (!row.country || row.country=='undefined'||row.country=='null') {
                        country = '';
                    }else{
                        hideCountryVal=row.country.toLowerCase()
                        country += '<span class="flag-icon flag-icon-text flag-icon-' + row.country.toLowerCase() + ' flag-icon-squared" title="' + getCountryName(row.country) + '"></span>';
                    }
                    country+="<span class='rmmCountryTextInput hide'>" +
                        "<input type='text' id='CountryTextInputId' value='" + getCountryName(row.country) + "' title='" +getCountryName(row.country)+ "' class='CountryTextInputClass' maxlength='4000' style='width: 80%' disabled>"
                        +'<span class=\"flag-icon flag-icon-input flag-icon-' + hideCountryVal + ' flag-icon-squared\" title=\"' + getCountryName(row.country) + ' "data-country="' + row.country + '\"></span>'+"</span>"
                    return country
                }
            },
            {
                "mData": "description",
                "className": "cell-break",
                "mRender": function (data, type, row) {
                    if (!row.description) {
                        row.description = '';
                    }
                        var description =  '<div class="pre-wrap"><span class="rmmDescriptionText">' + addEllipsisWithEscape(row.description)+ '</span>' + '</div>';

                        description +="<div class='textarea-ext rmmDescriptionInput hide'><span class='css-truncate expandable'><span class='branch-ref css-truncate-target'><textarea type='text' value='" + row.description + "' title='" + row.description + "' class='form-control description comment ellipsis min-height' maxlength='8000' rows='1' disabled>" + row.description + "</textarea> \
                         <a class='btn-text-ext openStatusComment comment-on-edit hide' href='' tabindex='0' title='Open in extended form'> \
                                   <i class='mdi mdi-arrow-expand font-20 blue-1'></i></a></span></span></div>"
                        return description
                }
            },
            {
                "mData": "fileName",
                "className": " cell-break",
                "mRender": function (data, type, row) {
                    var attachments,attachmentName,attachmentType;
                    if (row.fileName) {
                        attachmentName = escapeAllHTML(row.fileName);
                        attachments = "<div class='file-uploader col-width-250' data-provides='fileupload'>\
                            <a class='word-wrap-break-word' href='/signal/attachmentable/download?id=" + row.attachmentId + "'>" + escapeAllHTML(row.fileName) + "</a>\
                            <span class='newAttachment hide' data-id = " + row.attachmentId + "></span>"
                        attachmentType = 'file';
                    } else {
                        if (row.isReferenceLinkUrl.toLowerCase().startsWith("http")) {
                            attachmentName = escapeAllHTML(row.referenceLink);
                            attachments = "<div class='file-uploader col-width-250' data-provides='fileupload'>\
                                <a target='_blank' class='word-wrap-break-word' href='" + row.isReferenceLinkUrl + "'>" + escapeAllHTML(row.referenceLink) + "</a>\
                                <span class='newAttachment hide' data-id = " + row.referenceId + "></span>"
                        } else {
                            attachmentName = escapeAllHTML(row.referenceLink);
                            attachments = "<div class='file-uploader col-width-250' data-provides='fileupload'>\
                                <span class='word-wrap-break-word'>" + escapeAllHTML(row.referenceLink) + "</span>\
                                <span class='newAttachment hide' data-id = " + row.referenceId + "></span>"
                        }
                        attachmentType = 'link';
                    }
                    return attachments + "<div class='input-group hide' style='width: 100%'>\
                            <input type='text' class='form-control' data-inputtype = '"+attachmentType+"' disabled id='attachmentFilePathComm'\
                                                                    name='assessment-file'\
                                                                    value='" + handleSingleQuotesForJson(attachmentName) + "'> \
                                             <span class='input-group-btn '>\
                                                     <button class='browse btn btn-primary btn-file-upload allow-rmm-edit' type='button'\
                                                             data-toggle='modal' data-target='#rmmAttachmentFileModal'>\
                                                         <i class='glyphicon glyphicon-search'></i>\
                                                     </button>\
                                                 </span>\
                                              </div>"
                }
            },
            {
                "mData": "assignedToFullName",
                "className":"col-md-2-half cell-break",
                "mRender": function (data, type, row) {
                    var assignedTo = signal.list_utils.assigned_to_comp(row.id, row.assignedTo);
                    assignedTo += "<div class='rmmAssignedTo'>" + escapeAllHTML(row.assignedToFullName) + "</div>";
                    return assignedTo
                }
            },
            {
                "mData": "status",
                "orderDataType": "dom-select",
                "mRender": function (data, type, row) {
                    var $select = $("<select></select>", {
                        "value": row.status,
                        "class": "form-control status",
                        "disabled": "true"
                    });
                    $.each(rmmStatus, function (k, v) {
                        var $option = $("<option></option>", {
                            "text": v,
                            "value": v
                        });
                        if (row.status === v) {
                            $option.attr("selected", "selected")
                        }
                        $select.append($option);
                    });
                    return $select.prop("outerHTML");
                }
            },
            {
                "mData": "dueDate",
                "orderDataType": "dom-input-date",
                "mRender": function (data, type, row) {
                    return signal.utils.render('date_picker', {})
                }
            },
            {
                "mData": "emailSent",
                "className": "col-md-1-half",
                "mRender": function (data, type, row) {
                    return row.emailSent
                }
            },
            {
                "mData": "",
                "sortable": false,
                "mRender": function (data, type, row) {
                    var actionButton
                    if(row.emailSent === ''){
                        actionButton = "<a href='javascript:void(0);' title='Edit' class='table-row-edit editRecord pv-ic hidden-ic'>" +
                            "<i class='mdi mdi-pencil' aria-hidden='true'></i>\</a>" +
                            "<a href='javascript:void(0);' title='Delete' class='table-row-del deleteRecord hidden-ic'> " +
                            "<i class='mdi mdi-close' aria-hidden='true'></i> \</a> " +
                            "<a href='javascript:void(0);' title='Delete' class='table-row-del remove-edit hide hidden-ic'> " +
                            "<i class='mdi mdi-close' aria-hidden='true'></i> \</a> "
                    } else {
                        actionButton = ''
                    }
                    return "<a href='javascript:void(0);' title='Save' class='saveRecord table-row-saved hide pv-ic'> " +
                        "<i class='mdi mdi-check' aria-hidden='true'></i> </a>" +
                        "<span class='signalRmmId hide' data-signalRmmId=" + row.id + " ></span>" +
                        actionButton +
                        "<a href='#' title='Mail' class='table-row-mail hidden-ic'> " +
                        "<i class='mdi mdi-email-outline' aria-hidden='true'></i> </a>"
                },
                "className": 'text-center',
                "visible":isVisible()
            }
        ],
        columnDefs: [
            { "targets": '_all', orderSequence: ['desc', 'asc'] },
            { "render": $.fn.dataTable.render.text() }
        ]
    });

    rmmTable = $("#signal-rmms-table").DataTable({
        layout: {
            topStart: null,
            topEnd: null,
            bottomStart: ['pageLength', 'info', {paging: {type: "full_numbers"}}],
            bottomEnd: null,
        },
        customProcessing: true,
        destroy: true,
        searching: false,
        sPaginationType: "full_numbers",
        responsive: false,
        serverSide: true,
        "aaSorting": [],
        "order": [[ 5, "desc" ]],
        "aLengthMenu": [[10, 20, 50, -1], [10, 20, 50, "All"]],
        "iTotalDisplayRecords":10,
        language: {
            "url": "../assets/i18n/dataTables_" + userLocale + ".json"
        },
        "ajax": {
            "url": '/signal/validatedSignal/fetchRmms',
            "dataSrc": "aaData",
            data: {'signalId': $("#signalIdPartner").val(), 'type': 'rmmType'}
        },
        "oLanguage": {
            "oPaginate": {
                "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
            },
            "sLengthMenu": "Show _MENU_",

            "sZeroRecords": "No data available in table", "sEmptyTable": "No data available in table"
        },
        "createdRow": function (row, data, dataIndex) {
            $(row).addClass('readonly');
            $(row).attr('data-id','signal-rmms-table');
            var $dateCell = $(row).find('td:eq(5)');
            $dateCell.attr('data-order', data.dueDateSort)
        },

        "drawCallback": function (settings) {
            rmmAttachMap = {};
            if($(".newRow").is(":visible")){
                $("#signal-rmms-table .dt-empty").hide();
            }
            colEllipsis();
            webUiPopInit();
            var rowsDataAR = $('#signal-rmms-table').DataTable().rows().data();

            if(settings.json !== undefined) {
                pageDictionary($('#signal-rmms-table_wrapper'), settings.json.recordsFiltered);
                showTotalPage($('#signal-rmms-table_wrapper'), settings.json.recordsFiltered);
            }else {
                pageDictionary($('#signal-rmms-table_wrapper'), rowsDataAR.length);
                showTotalPage($('#signal-rmms-table_wrapper'), rowsDataAR.length);
            }
        },

        "rowCallback": function (row, data, index) {
            //Bind AssignedTo Select Box
            if (data.emailAddress) {
                signal.user_group_utils.bind_assignTo_to_rmm_row($(row), searchUserGroupListUrl, {
                    name: data.emailAddress,
                    id: data.emailAddress
                });
            } else {
                signal.user_group_utils.bind_assignTo_to_rmm_row($(row), searchUserGroupListUrl, {
                    name: data.assignedToFullName,
                    id: (data.isAssignedTo ? "User_" : 'UserGroup_') + data.assignedToId
                });

            }
            $(row).find('.assignedToSelect').next(".select2-container").hide();
            $(row).find('.historyDatePicker').datepicker({
                date: data.dueDate,
                allowPastDates: false,
                momentConfig: {
                    culture: userLocale,
                    tz: serverTimeZone,
                    format: DEFAULT_DATE_DISPLAY_FORMAT
                }
            });
            $(row).find('.rmmType').select2({
                tags : true
            });

            if(data.colorDueDate === 'orange' || data.colorDueDate === 'red') {
                $(row).find('.due-date').css('color', data.colorDueDate);
            }
        },

        "aoColumns": [
            {
                "mData": "type",
                "orderDataType": "dom-select",
                "className":"cell-break",
                "mRender": function (data, type, row) {
                    var $select = $("<select></select>", {
                        "value": row.type,
                        "class": "form-control rmmType",
                        "disabled": "true"
                    });
                    $.each(rmmType, function (k, v) {
                        var $option = $("<option></option>", {
                            "text": v,
                            "value": v
                        });
                        if (row.type === v) {
                            $option.attr("selected", "selected")
                        }
                        $select.append($option);
                    });
                    if($.inArray(row.type, rmmType) === -1){
                        var $option = $("<option></option>", {
                            "text": row.type,
                            "value": row.type
                        });
                        $option.attr("selected", "selected");
                        $select.append($option);
                    }
                    var type = '<div class="rmm-type row"><div>' + $select.prop("outerHTML") + '</div>';
                   /* if (row.country) {
                        type += '<div class="col-md-2 country" data-country="' + row.country + '"><span class="flag-icon flag-icon-' + row.country.toLowerCase() + ' flag-icon-squared" title="' + getCountryName(row.country) + '"></span></div>';
                    }*/
                    type += "</div>";
                    return type
                }
            },
            {
                "mData": "country",
                "className":"cell-break",
                "mRender": function (data, type, row) {
                    var country='<span  class="rmmCountryText" style="white-space: pre-wrap">'+getCountryName(row.country)+'&nbsp'+'</span>';
                    var hideCountryVal=''
                    if (!row.country || row.country=='undefined'||row.country=='null') {
                        country = '';
                    }else{
                        hideCountryVal=row.country.toLowerCase()
                        country += '<span class="flag-icon flag-icon-text flag-icon-' + row.country.toLowerCase() + ' flag-icon-squared" title="' + getCountryName(row.country) + '"></span>';
                    }
                    country+="<span class='rmmCountryTextInput hide'>" +
                        "<input type='text' id='CountryTextInputId' value='" + escapeAllHTML(getCountryName(row.country)) + "' title='" +escapeAllHTML(getCountryName(row.country))+ "' class='CountryTextInputClass' maxlength='4000' style='width: 80%' disabled>"
                        +'<span class=\"flag-icon flag-icon-input flag-icon-' + hideCountryVal + ' flag-icon-squared\" title=\"' + escapeAllHTML(getCountryName(row.country)) + ' "data-country="' + row.country + '\"></span>'+"</span>"
                    return country
                }
            },
            {
                "mData": "description",
                "className":"cell-break",
                "mRender": function (data, type, row) {
                    if (!row.description) {
                        row.description = '';
                    }
                    var description =  '<div class="pre-wrap"><span class="rmmDescriptionText">' + addEllipsisWithEscape(row.description)+ "</span></div>";

                    description +="<div class='textarea-ext rmmDescriptionInput hide'><span class='css-truncate expandable'><span class='branch-ref css-truncate-target'><textarea type='text' value='" + row.description + "' title='" + row.description + "' class='form-control description comment ellipsis min-height' maxlength='8000' rows='1' disabled>"+row.description+"</textarea> \
                     <a class='btn-text-ext openStatusComment comment-on-edit hide' href='' tabindex='0' title='Open in extended form'> \
                               <i class='mdi mdi-arrow-expand font-20 blue-1'></i></a></span></span></div>"
                    return description
                }
            },
            {
                "mData": "fileName",
                "className":" cell-break",
                "mRender": function (data, type, row) {
                    var attachments, attachmentName, attachmentType;
                    if (row.fileName) {
                        attachmentName = escapeAllHTML(row.fileName);
                        attachments = "<div class='file-uploader col-width-250' data-provides='fileupload'>\
                            <a class='word-wrap-break-word' href='/signal/attachmentable/download?type=rmm&&id=" + row.attachmentId + "'>" + escapeAllHTML(row.fileName) + "</a>\
                            <span class='newAttachment hide' data-id = " + row.attachmentId + "></span>"
                        attachmentType = 'file';
                    } else {
                        if (row.isReferenceLinkUrl.toLowerCase().startsWith("http")) {
                            attachmentName = escapeAllHTML(row.referenceLink);
                            attachments = "<div class='file-uploader col-width-250' data-provides='fileupload'>\
                                <a target='_blank' class='word-wrap-break-word' href='" + row.isReferenceLinkUrl + "'>" + escapeAllHTML(row.referenceLink) + "</a>\
                                <span class='newAttachment hide' data-id = " + row.referenceId + "></span>"
                        } else {
                            attachmentName = escapeAllHTML(row.referenceLink);
                            attachments = "<div class='file-uploader col-width-250' data-provides='fileupload'>\
                                <span class='word-wrap-break-word'>" + escapeAllHTML(row.referenceLink) + "</span>\
                                <span class='newAttachment hide' data-id = " + row.referenceId + "></span>"
                        }
                        attachmentType = 'link'
                    }
                    return attachments + "<div class='input-group hide' style='width: 100%'>\
                            <input type='text' data-inputtype = '"+attachmentType+"' class='form-control' disabled id='attachmentFilePathRmm'\
                                                                    name='assessment-file'\
                                                                    value='" + handleSingleQuotesForJson(attachmentName) + "'> \
                                             <span class='input-group-btn '>\
                                                     <button class='browse btn btn-primary btn-file-upload allow-rmm-edit' type='button'\
                                                             data-toggle='modal' data-target='#rmmAttachmentFileModal'>\
                                                         <i class='glyphicon glyphicon-search'></i>\
                                                     </button>\
                                                 </span>\
                                              </div>"
                }
            },
            {
                "mData": "assignedToFullName",
                "className":"cell-break",
                "mRender": function (data, type, row) {
                    var assignedTo = signal.list_utils.assigned_to_comp(row.id, row.assignedTo);
                    assignedTo += "<div class='rmmAssignedTo'>" + escapeAllHTML(row.assignedToFullName) + "</div>";
                    return assignedTo
                }
            },
            {
                "mData": "status",
                "orderDataType": "dom-select",
                "mRender": function (data, type, row) {
                    var $select = $("<select></select>", {
                        "value": row.status,
                        "class": "form-control status",
                        "disabled": "true"
                    });
                    $.each(rmmStatus, function (k, v) {
                        var $option = $("<option></option>", {
                            "text": v,
                            "value": v
                        });
                        if (row.status === v) {
                            $option.attr("selected", "selected")
                        }
                        $select.append($option);
                    });
                    return $select.prop("outerHTML");
                }
            },
            {
                "mData": "dueDate",
                "orderDataType": "dom-input-date",
                "mRender": function (data, type, row) {
                    return signal.utils.render('date_picker', {})
                }
            },
            {
                "mData": "",
                "sortable": false,
                "mRender": function (data, type, row) {
                    return "<a href='javascript:void(0);' title='Save' class='saveRecord table-row-saved hide pv-ic'> " +
                        "<i class='mdi mdi-check' aria-hidden='true'></i> </a>" +
                        "<span class='signalRmmId hide' data-signalRmmId=" + row.id + " ></span>" +
                        "<a href='javascript:void(0);' title='Edit' class='table-row-edit editRecord pv-ic hidden-ic'>" +
                        "<i class='mdi mdi-pencil' aria-hidden='true'></i>\</a>" +
                        "<a href='javascript:void(0);' title='Delete' class='table-row-del deleteRecord hidden-ic'> " +
                        "<i class='mdi mdi-close' aria-hidden='true'></i> \</a> " +
                        "<a href='javascript:void(0);' title='Delete' class='table-row-del remove-edit hide hidden-ic'> " +
                        "<i class='mdi mdi-close' aria-hidden='true'></i> \</a> " +
                        "<a href='#' title='Mail' class='table-row-mail hidden-ic'> " +
                        "<i class='mdi mdi-email-outline' aria-hidden='true'></i> </a>"
                },
                "className": 'text-center',
                "visible": isVisible()
            }
        ],
        columnDefs: [
            { "targets": '_all', orderSequence: ['desc', 'asc'] },
            { "render": $.fn.dataTable.render.text() }
        ]
    });

    function isVisible() {
        var isVisible = true;
        if(typeof hasReviewerAccess !== "undefined" && !hasReviewerAccess) {
            isVisible = false;
        }
        return isVisible
    }

    $(document).on('click', ".remove-rmm-row", function () {
        if (!rmmTable.data().count()) {
            $('#signal-rmms-table td.dt-empty').toggle();
        }
        $(".newRow").toggle();
    });

    $(document).on('click', ".remove-comm-row", function () {
        if (!communicationTable.data().count()) {
            $('#signal-communication-table td.dt-empty').toggle();
        }
        $(".newRowCommunication").toggle();
    });

    $(document).on('click', ".add-new-row", function () {
        var attrId = $(this).attr('id');
        if (attrId === 'addRMMSRow') {
            if (!rmmTable.data().count()) {
                $('#signal-rmms-table td.dt-empty').toggle();
            }
            $(".newRow").toggle();
        } else {
            if (!communicationTable.data().count()) {
                $('#signal-communication-table td.dt-empty').toggle();
            }
            $(".newRowCommunication").toggle();
        }

        $('#signal-rmms-table_wrapper tfoot .description').val('');
        $('#signal-communication-table_wrapper tfoot .description').val('');
        $(".newRow").find('#attachmentFilePathRmm').Reset_List_To_Default_Value();
        $(".newRowCommunication").find('#attachmentFilePathComm').Reset_List_To_Default_Value();
        $('#rmmResp').Reset_List_To_Default_Value();
        $('#addNewCountry').val('');
        $('#addNewCountryComm').val('');
        $('.addCountry').find('.flag-icon-input').remove();
        $(".newRow").find('#rmmType').Reset_List_To_Default_Value();
        $(".newRowCommunication").find('#rmmType').Reset_List_To_Default_Value();
        $('#communicationResp').Reset_List_To_Default_Value();
        $('.status').Reset_List_To_Default_Value();
        $('#rmmAttachmentFileModal').find('.file').val('');
        $('.rmmsDatePicker').datepicker('setDate', new Date());
        $('.communicationDatePicker').datepicker('setDate', new Date());
    });

    // Resetting all select values to their default values
    $.fn.Reset_List_To_Default_Value = function () {
        $.each($(this), function (index, el) {
            var Founded = false;

            $(this).find('option').each(function (i, opt) {
                if (opt.defaultSelected) {
                    opt.selected = true;
                    Founded = true;
                }
            });
            if (!Founded) {
                if ($(this).attr('multiple')) {
                    $(this).val([]);
                }
                else {
                    $(this).val("");
                }
            }
            $(this).trigger('change');
        });
    }

    $('.rmmType').on('change', function () {
        if(communicationType.includes($(this).val()) ){
            if (countrySpecificRMM.includes($(this).val())) {
                rmmTypeSelector = $(this).closest('.rmm-type');
                $('#country-select').val(null).trigger('change.select2');
                $('#countrySelectModal').modal({show: true});
            } else {
                $('#addNewCountry').val("")
                $('#addNewCountryComm').val("")
                rmmTypeSelector = $(this).closest('.rmm-type');
                rmmTypeSelector.find('.col-md-2').remove();
                if($(this).val() === 'Signal Memo'){
                    $.Notification.notify('warning', 'top right', "Warning", "Please fill in all the relevant fields except file name, and save the current row. The signal memo report will be generated and attached with this communication.", {autoHideDelay: 5000});
                }
            }
        }
    });

    $('.close-country-modal').on('click', function () {
        $('#countrySelectModal').modal({show: false});
    })

    //Country List must be synced with the list in ValidatedSignalService.groovy
    var isoCountries = [{
        id: 'AF',
        text: 'Afghanistan'
    },
        {
            id: 'AX',
            text: 'Aland Islands'
        },
        {
            id: 'AL',
            text: 'Albania'
        },
        {
            id: 'DZ',
            text: 'Algeria'
        },
        {
            id: 'AS',
            text: 'American Samoa'
        },
        {
            id: 'AD',
            text: 'Andorra'
        },
        {
            id: 'AO',
            text: 'Angola'
        },
        {
            id: 'AI',
            text: 'Anguilla'
        },
        {
            id: 'AQ',
            text: 'Antarctica'
        },
        {
            id: 'AG',
            text: 'Antigua And Barbuda'
        },
        {
            id: 'AR',
            text: 'Argentina'
        },
        {
            id: 'AM',
            text: 'Armenia'
        },
        {
            id: 'AW',
            text: 'Aruba'
        },
        {
            id: 'AU',
            text: 'Australia'
        },
        {
            id: 'AT',
            text: 'Austria'
        },
        {
            id: 'AZ',
            text: 'Azerbaijan'
        },
        {
            id: 'BS',
            text: 'Bahamas'
        },
        {
            id: 'BH',
            text: 'Bahrain'
        },
        {
            id: 'BD',
            text: 'Bangladesh'
        },
        {
            id: 'BB',
            text: 'Barbados'
        },
        {
            id: 'BY',
            text: 'Belarus'
        },
        {
            id: 'BE',
            text: 'Belgium'
        },
        {
            id: 'BZ',
            text: 'Belize'
        },
        {
            id: 'BJ',
            text: 'Benin'
        },
        {
            id: 'BM',
            text: 'Bermuda'
        },
        {
            id: 'BT',
            text: 'Bhutan'
        },
        {
            id: 'BO',
            text: 'Bolivia'
        },
        {
            id: 'BA',
            text: 'Bosnia And Herzegovina'
        },
        {
            id: 'BW',
            text: 'Botswana'
        },
        {
            id: 'BV',
            text: 'Bouvet Island'
        },
        {
            id: 'BR',
            text: 'Brazil'
        },
        {
            id: 'IO',
            text: 'British Indian Ocean Territory'
        },
        {
            id: 'BN',
            text: 'Brunei Darussalam'
        },
        {
            id: 'BG',
            text: 'Bulgaria'
        },
        {
            id: 'BF',
            text: 'Burkina Faso'
        },
        {
            id: 'BI',
            text: 'Burundi'
        },
        {
            id: 'KH',
            text: 'Cambodia'
        },
        {
            id: 'CM',
            text: 'Cameroon'
        },
        {
            id: 'CA',
            text: 'Canada'
        },
        {
            id: 'CV',
            text: 'Cape Verde'
        },
        {
            id: 'KY',
            text: 'Cayman Islands'
        },
        {
            id: 'CF',
            text: 'Central African Republic'
        },
        {
            id: 'TD',
            text: 'Chad'
        },
        {
            id: 'CL',
            text: 'Chile'
        },
        {
            id: 'CN',
            text: 'China'
        },
        {
            id: 'CX',
            text: 'Christmas Island'
        },
        {
            id: 'CC',
            text: 'Cocos (Keeling) Islands'
        },
        {
            id: 'CO',
            text: 'Colombia'
        },
        {
            id: 'KM',
            text: 'Comoros'
        },
        {
            id: 'CG',
            text: 'Congo'
        },
        {
            id: 'CD',
            text: 'Democratic Republic of the Congo'
        },
        {
            id: 'CK',
            text: 'Cook Islands'
        },
        {
            id: 'CR',
            text: 'Costa Rica'
        },
        {
            id: 'CI',
            text: 'Cote D\'Ivoire'
        },
        {
            id: 'HR',
            text: 'Croatia'
        },
        {
            id: 'CU',
            text: 'Cuba'
        },
        {
            id: 'CY',
            text: 'Cyprus'
        },
        {
            id: 'CZ',
            text: 'Czech Republic'
        },
        {
            id: 'DK',
            text: 'Denmark'
        },
        {
            id: 'DJ',
            text: 'Djibouti'
        },
        {
            id: 'DM',
            text: 'Dominica'
        },
        {
            id: 'DO',
            text: 'Dominican Republic'
        },
        {
            id: 'EC',
            text: 'Ecuador'
        },
        {
            id: 'EG',
            text: 'Egypt'
        },
        {
            id: 'SV',
            text: 'El Salvador'
        },
        {
            id: 'GQ',
            text: 'Equatorial Guinea'
        },
        {
            id: 'ER',
            text: 'Eritrea'
        },
        {
            id: 'EE',
            text: 'Estonia'
        },
        {
            id: 'ET',
            text: 'Ethiopia'
        },
        {
            id: 'FK',
            text: 'Falkland Islands (Malvinas)'
        },
        {
            id: 'FO',
            text: 'Faroe Islands'
        },
        {
            id: 'FJ',
            text: 'Fiji'
        },
        {
            id: 'FI',
            text: 'Finland'
        },
        {
            id: 'FR',
            text: 'France'
        },
        {
            id: 'GF',
            text: 'French Guiana'
        },
        {
            id: 'PF',
            text: 'French Polynesia'
        },
        {
            id: 'TF',
            text: 'French Southern Territories'
        },
        {
            id: 'GA',
            text: 'Gabon'
        },
        {
            id: 'GM',
            text: 'Gambia'
        },
        {
            id: 'GE',
            text: 'Georgia'
        },
        {
            id: 'DE',
            text: 'Germany'
        },
        {
            id: 'GH',
            text: 'Ghana'
        },
        {
            id: 'GI',
            text: 'Gibraltar'
        },
        {
            id: 'GR',
            text: 'Greece'
        },
        {
            id: 'GL',
            text: 'Greenland'
        },
        {
            id: 'GD',
            text: 'Grenada'
        },
        {
            id: 'GP',
            text: 'Guadeloupe'
        },
        {
            id: 'GU',
            text: 'Guam'
        },
        {
            id: 'GT',
            text: 'Guatemala'
        },
        {
            id: 'GG',
            text: 'Guernsey'
        },
        {
            id: 'GN',
            text: 'Guinea'
        },
        {
            id: 'GW',
            text: 'Guinea-Bissau'
        },
        {
            id: 'GY',
            text: 'Guyana'
        },
        {
            id: 'HT',
            text: 'Haiti'
        },
        {
            id: 'HM',
            text: 'Heard Island & Mcdonald Islands'
        },
        {
            id: 'VA',
            text: 'Holy See (Vatican City State)'
        },
        {
            id: 'HN',
            text: 'Honduras'
        },
        {
            id: 'HK',
            text: 'Hong Kong'
        },
        {
            id: 'HU',
            text: 'Hungary'
        },
        {
            id: 'IS',
            text: 'Iceland'
        },
        {
            id: 'IN',
            text: 'India'
        },
        {
            id: 'ID',
            text: 'Indonesia'
        },
        {
            id: 'IR',
            text: 'Islamic Republic Of Iran'
        },
        {
            id: 'IQ',
            text: 'Iraq'
        },
        {
            id: 'IE',
            text: 'Ireland'
        },
        {
            id: 'IM',
            text: 'Isle Of Man'
        },
        {
            id: 'IL',
            text: 'Israel'
        },
        {
            id: 'IT',
            text: 'Italy'
        },
        {
            id: 'JM',
            text: 'Jamaica'
        },
        {
            id: 'JP',
            text: 'Japan'
        },
        {
            id: 'JE',
            text: 'Jersey'
        },
        {
            id: 'JO',
            text: 'Jordan'
        },
        {
            id: 'KZ',
            text: 'Kazakhstan'
        },
        {
            id: 'KE',
            text: 'Kenya'
        },
        {
            id: 'KI',
            text: 'Kiribati'
        },
        {
            id: 'KR',
            text: 'Korea'
        },
        {
            id: 'KW',
            text: 'Kuwait'
        },
        {
            id: 'KG',
            text: 'Kyrgyzstan'
        },
        {
            id: 'LA',
            text: 'Lao People\'s Democratic Republic'
        },
        {
            id: 'LV',
            text: 'Latvia'
        },
        {
            id: 'LB',
            text: 'Lebanon'
        },
        {
            id: 'LS',
            text: 'Lesotho'
        },
        {
            id: 'LR',
            text: 'Liberia'
        },
        {
            id: 'LY',
            text: 'Libyan Arab Jamahiriya'
        },
        {
            id: 'LI',
            text: 'Liechtenstein'
        },
        {
            id: 'LT',
            text: 'Lithuania'
        },
        {
            id: 'LU',
            text: 'Luxembourg'
        },
        {
            id: 'MO',
            text: 'Macao'
        },
        {
            id: 'MK',
            text: 'Macedonia'
        },
        {
            id: 'MG',
            text: 'Madagascar'
        },
        {
            id: 'MW',
            text: 'Malawi'
        },
        {
            id: 'MY',
            text: 'Malaysia'
        },
        {
            id: 'MV',
            text: 'Maldives'
        },
        {
            id: 'ML',
            text: 'Mali'
        },
        {
            id: 'MT',
            text: 'Malta'
        },
        {
            id: 'MH',
            text: 'Marshall Islands'
        },
        {
            id: 'MQ',
            text: 'Martinique'
        },
        {
            id: 'MR',
            text: 'Mauritania'
        },
        {
            id: 'MU',
            text: 'Mauritius'
        },
        {
            id: 'YT',
            text: 'Mayotte'
        },
        {
            id: 'MX',
            text: 'Mexico'
        },
        {
            id: 'FM',
            text: 'Federated States Of Micronesia'
        },
        {
            id: 'MD',
            text: 'Moldova'
        },
        {
            id: 'MC',
            text: 'Monaco'
        },
        {
            id: 'MN',
            text: 'Mongolia'
        },
        {
            id: 'ME',
            text: 'Montenegro'
        },
        {
            id: 'MS',
            text: 'Montserrat'
        },
        {
            id: 'MA',
            text: 'Morocco'
        },
        {
            id: 'MZ',
            text: 'Mozambique'
        },
        {
            id: 'MM',
            text: 'Myanmar'
        },
        {
            id: 'NA',
            text: 'Namibia'
        },
        {
            id: 'NR',
            text: 'Nauru'
        },
        {
            id: 'NP',
            text: 'Nepal'
        },
        {
            id: 'NL',
            text: 'Netherlands'
        },
        {
            id: 'AN',
            text: 'Netherlands Antilles'
        },
        {
            id: 'NC',
            text: 'New Caledonia'
        },
        {
            id: 'NZ',
            text: 'New Zealand'
        },
        {
            id: 'NI',
            text: 'Nicaragua'
        },
        {
            id: 'NE',
            text: 'Niger'
        },
        {
            id: 'NG',
            text: 'Nigeria'
        },
        {
            id: 'NU',
            text: 'Niue'
        },
        {
            id: 'NF',
            text: 'Norfolk Island'
        },
        {
            id: 'MP',
            text: 'Northern Mariana Islands'
        },
        {
            id: 'NO',
            text: 'Norway'
        },
        {
            id: 'OM',
            text: 'Oman'
        },
        {
            id: 'PK',
            text: 'Pakistan'
        },
        {
            id: 'PW',
            text: 'Palau'
        },
        {
            id: 'PS',
            text: 'Occupied Palestinian Territory'
        },
        {
            id: 'PA',
            text: 'Panama'
        },
        {
            id: 'PG',
            text: 'Papua New Guinea'
        },
        {
            id: 'PY',
            text: 'Paraguay'
        },
        {
            id: 'PE',
            text: 'Peru'
        },
        {
            id: 'PH',
            text: 'Philippines'
        },
        {
            id: 'PN',
            text: 'Pitcairn'
        },
        {
            id: 'PL',
            text: 'Poland'
        },
        {
            id: 'PT',
            text: 'Portugal'
        },
        {
            id: 'PR',
            text: 'Puerto Rico'
        },
        {
            id: 'QA',
            text: 'Qatar'
        },
        {
            id: 'RE',
            text: 'Reunion'
        },
        {
            id: 'RO',
            text: 'Romania'
        },
        {
            id: 'RU',
            text: 'Russian Federation'
        },
        {
            id: 'RW',
            text: 'Rwanda'
        },
        {
            id: 'BL',
            text: 'Saint Barthelemy'
        },
        {
            id: 'SH',
            text: 'Saint Helena'
        },
        {
            id: 'KN',
            text: 'Saint Kitts And Nevis'
        },
        {
            id: 'LC',
            text: 'Saint Lucia'
        },
        {
            id: 'MF',
            text: 'Saint Martin'
        },
        {
            id: 'PM',
            text: 'Saint Pierre And Miquelon'
        },
        {
            id: 'VC',
            text: 'Saint Vincent And Grenadines'
        },
        {
            id: 'WS',
            text: 'Samoa'
        },
        {
            id: 'SM',
            text: 'San Marino'
        },
        {
            id: 'ST',
            text: 'Sao Tome And Principe'
        },
        {
            id: 'SA',
            text: 'Saudi Arabia'
        },
        {
            id: 'SN',
            text: 'Senegal'
        },
        {
            id: 'RS',
            text: 'Serbia'
        },
        {
            id: 'SC',
            text: 'Seychelles'
        },
        {
            id: 'SL',
            text: 'Sierra Leone'
        },
        {
            id: 'SG',
            text: 'Singapore'
        },
        {
            id: 'SK',
            text: 'Slovakia'
        },
        {
            id: 'SI',
            text: 'Slovenia'
        },
        {
            id: 'SB',
            text: 'Solomon Islands'
        },
        {
            id: 'SO',
            text: 'Somalia'
        },
        {
            id: 'ZA',
            text: 'South Africa'
        },
        {
            id: 'GS',
            text: 'South Georgia And Sandwich Isl.'
        },
        {
            id: 'ES',
            text: 'Spain'
        },
        {
            id: 'LK',
            text: 'Sri Lanka'
        },
        {
            id: 'SD',
            text: 'Sudan'
        },
        {
            id: 'SR',
            text: 'Suriname'
        },
        {
            id: 'SJ',
            text: 'Svalbard And Jan Mayen'
        },
        {
            id: 'SZ',
            text: 'Swaziland'
        },
        {
            id: 'SE',
            text: 'Sweden'
        },
        {
            id: 'CH',
            text: 'Switzerland'
        },
        {
            id: 'SY',
            text: 'Syrian Arab Republic'
        },
        {
            id: 'TW',
            text: 'Taiwan'
        },
        {
            id: 'TJ',
            text: 'Tajikistan'
        },
        {
            id: 'TZ',
            text: 'Tanzania'
        },
        {
            id: 'TH',
            text: 'Thailand'
        },
        {
            id: 'TL',
            text: 'Timor-Leste'
        },
        {
            id: 'TG',
            text: 'Togo'
        },
        {
            id: 'TK',
            text: 'Tokelau'
        },
        {
            id: 'TO',
            text: 'Tonga'
        },
        {
            id: 'TT',
            text: 'Trinidad And Tobago'
        },
        {
            id: 'TN',
            text: 'Tunisia'
        },
        {
            id: 'TR',
            text: 'Turkey'
        },
        {
            id: 'TM',
            text: 'Turkmenistan'
        },
        {
            id: 'TC',
            text: 'Turks And Caicos Islands'
        },
        {
            id: 'TV',
            text: 'Tuvalu'
        },
        {
            id: 'UG',
            text: 'Uganda'
        },
        {
            id: 'UA',
            text: 'Ukraine'
        },
        {
            id: 'AE',
            text: 'United Arab Emirates'
        },
        {
            id: 'GB',
            text: 'United Kingdom'
        },
        {
            id: 'US',
            text: 'United States'
        },
        {
            id: 'UM',
            text: 'United States Outlying Islands'
        },
        {
            id: 'UY',
            text: 'Uruguay'
        },
        {
            id: 'UZ',
            text: 'Uzbekistan'
        },
        {
            id: 'VU',
            text: 'Vanuatu'
        },
        {
            id: 'VE',
            text: 'Venezuela'
        },
        {
            id: 'VN',
            text: 'Viet Nam'
        },
        {
            id: 'VG',
            text: 'British Virgin Islands'
        },
        {
            id: 'VI',
            text: 'U.S. Virgin Islands'
        },
        {
            id: 'WF',
            text: 'Wallis And Futuna'
        },
        {
            id: 'EH',
            text: 'Western Sahara'
        },
        {
            id: 'YE',
            text: 'Yemen'
        },
        {
            id: 'ZM',
            text: 'Zambia'
        },
        {
            id: 'ZW',
            text: 'Zimbabwe'
        }
    ];

    var addEllipsisWithEscape = function (rowValue) {
        var colElement = '';
        if (rowValue) {
            colElement = '<div class="col-container"><div class="col-height word-break">';
            colElement += escapeAllHTML(rowValue);
            colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + escapeAllHTML(escapeAllHTML(rowValue)) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
            colElement += '</div></div>';
        }
        return colElement
    };

    function formatCountry(country) {
        if (!country.id) {
            return country.text;
        }
        var $country = $(
            '<span class="flag-icon flag-icon-' + country.id.toLowerCase() + ' flag-icon-squared"></span>' +
            '<span class="flag-text">' + country.text + "</span>"
        );
        return $country;
    };
    //Assuming you have a select element with name country
    // e.g. <select name="name"></select>

    $("[name='country']").select2({
        placeholder: "Select a Country",
        templateResult: formatCountry,
        data: isoCountries
    });

    $("#rmmTypeSelect, #communicationTypeSelect").on("click",function(){
        $('.select2-container').find('input').attr("maxlength","255")
    });



});
