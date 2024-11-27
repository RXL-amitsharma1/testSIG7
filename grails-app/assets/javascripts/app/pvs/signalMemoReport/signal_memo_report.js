//= require app/pvs/common/rx_common.js
//= require app/pvs/common/rx_handlebar_ext.js
//= require app/pvs/common/rx_alert_utils.js
//= require app/pvs/common/rx_list_utils.js
//= require app/pvs/actions/actions.js
//= require app/pvs/meeting/meeting.js
var notificationConfigTable;
var signalOutcomesToBeDisabled = `${signalOutcomesToBeDisabled}`;
var currentEditingRow;
var isEditable = false;

$(function () {

    $("#signalSource").select2({
        multiple: true,
        width: '100%'
    });
    $("#triggerVariable").select2();
    $("#signalOutcomes").select2({ multiple: true, width: '100%' });
    $("#actionsTaken").select2({ multiple: true, width: '100%' });
    $("#disposition").select2({ multiple: true, width: '100%' });

    $(document).on('click', '.edit-signal-memo', function (e) {
        e.preventDefault();
        var currRow = $(this).closest('tr');
        currRow.removeClass('readonly');
        currRow.find('.form-control').removeAttr('disabled');
        currRow.find('.select2').next(".select2-container").show();
        currRow.find('.remove-edit-memo').removeClass('hide');
        currRow.find('.table-row-edit').addClass('hide');
        currRow.find('.table-row-saved').removeClass('hide').addClass('hidden-ic');
        currRow.find('.trigger-value-drop, .email-body-text, .email-subject-text, .delete-signal-memo').addClass('hide');
        currRow.find('.memo-address, .trigger-val-text, .trigger-var-text, .signal-source, .config-name-text').addClass('hide');
        currRow.find('.email-body-input,.comment-on-edit').removeClass('hide');
        currRow.find('.email-subject-input, .email-body-input').removeClass('hide');
        currRow.find('.config-input, .triggerVariable').removeClass('hide');
        currRow.find('.triggerVariable').next(".select2-container").css('display','block');
        currRow.find('.triggerVariable').addClass('edit-trig-val');
        currRow.find('.signalSource').next(".select2-container").css('display','block')
        if(typeof currRow.find('.signal-source').text() != 'undefined' && !currRow.find('.signal-source').text()){
            currRow.find('.signalSource').val(null).trigger("change");
        }
        currRow.find('.assignedToSelect').next(".select2-container").css('display', 'block')
        if (currRow.find('.triggerVariable').val() == $.i18n._('signalOutcomeLabel')) {
            currRow.find('.signalOutcomes').next(".select2-container").css('display', 'block')
        } else if (currRow.find('.triggerVariable').val() == $.i18n._('actionTakenLabel')) {
            currRow.find('.actionsTaken').next(".select2-container").css('display', 'block')
        } else if (currRow.find('.triggerVariable').val() == $.i18n._('dispositionLabel')) {
            currRow.find('.disposition').next(".select2-container").css('display', 'block')
        } else {
            currRow.find('.trigger-val-input').removeClass('hide');
        }
        currRow.find("#isEditable").val(true);
    });

    $(document).on('click', '.remove-edit-memo', function (e) {
        e.preventDefault();
        var currRow = $(this).closest('tr');
        currRow.removeClass('readonly');
        currRow.find('.form-control').prop('disabled', true);
        currRow.find('.select2').next(".select2-container").hide();
        currRow.find('.remove-edit-memo').addClass('hide');
        currRow.find('.table-row-edit').removeClass('hide');
        currRow.find('.table-row-saved').addClass('hide').removeClass('hidden-ic');
        currRow.find('.trigger-value-drop, .email-body-text, .email-subject-text, .delete-signal-memo').removeClass('hide');
        currRow.find('.memo-address, .trigger-val-text, .trigger-var-text, .signal-source, .config-name-text').removeClass('hide');
        currRow.find('.email-body-input,.comment-on-edit').addClass('hide');
        currRow.find('.trigger-val-input, .email-subject-input, .email-body-input').addClass('hide');
        currRow.find('.config-input, .triggerVariable').addClass('hide');
        currRow.find('.triggerVariable').next(".select2-container").css('display','none');
        currRow.find('.triggerVariable').removeClass('edit-trig-val');
        currRow.find('.signalSource').next(".select2-container").css('display','none');
        currRow.find('.assignedToSelect').next(".select2-container").css('display','none');
        if (currRow.find('.triggerVariable').val() == $.i18n._('signalOutcomeLabel')) {
            currRow.find('.signalOutcomes').next(".select2-container").css('display', 'none');
        } else if (currRow.find('.triggerVariable').val() == $.i18n._('actionTakenLabel')) {
            currRow.find('.actionsTaken').next(".select2-container").css('display', 'none');
        } else if (currRow.find('.triggerVariable').val() == $.i18n._('dispositionLabel')) {
            currRow.find('.disposition').next(".select2-container").css('display', 'none');
        } else {
            currRow.find('.trigger-val-input').addClass('hide');
        }
        $('#signalMemoReportTable').DataTable().columns.adjust().draw();
    });

    $('#signalSource').next(".select2-container").css('display','block');
    $('#signalOutcomes').next(".select2-container").css('display','none');
    $('#actionsTaken').next(".select2-container").css('display','none');
    $('#disposition').next(".select2-container").css('display','none');
    $(document).on('change', '#triggerVariable, .edit-trig-val', function () {
        var currRow = $(this).closest('tr');
        if ($(this).val() == $.i18n._('signalOutcomeLabel')) {
            currRow.find('.signalOutcomes').next(".select2-container").css('display', 'block');
            currRow.find('.trigger-value').css('display', 'none');
            currRow.find('.actionsTaken').next(".select2-container").css('display', 'none');
            currRow.find('.disposition').next(".select2-container").css('display', 'none');
        } else if ($(this).val() == $.i18n._('actionTakenLabel')) {
            currRow.find('.actionsTaken').next(".select2-container").css('display', 'block');
            currRow.find('.signalOutcomes').next(".select2-container").css('display', 'none');
            currRow.find('.disposition').next(".select2-container").css('display', 'none');
            currRow.find('.trigger-value').hide();
        } else if ($(this).val() == $.i18n._('dispositionLabel')) {
            currRow.find('.disposition').next(".select2-container").css('display', 'block');
            currRow.find('.signalOutcomes').next(".select2-container").css('display', 'none');
            currRow.find('.actionsTaken').next(".select2-container").css('display', 'none');
            currRow.find('.trigger-value').hide();
        } else {
            currRow.find('.trigger-value').removeClass('hide').css('display', 'block');
            currRow.find('.trigger-val-input').removeClass('hide')
            currRow.find('.signalOutcomes').next(".select2-container").css('display', 'none');
            currRow.find('.actionsTaken').next(".select2-container").css('display', 'none');
            currRow.find('.disposition').next(".select2-container").css('display', 'none');
        }
    });

    function checkIfConfigExistsWithConfigName(configName, configId, triggerVariable, callback, $this){
        var id;
        if(typeof configId != 'undefined' && configId){
            id = configId;
        }
        var data = {
            configName: configName,
            triggerVariable: triggerVariable,
            configId: id,
        };
        $.ajax({
            url: configExistsUrl,
            type: "POST",
            data: data,
        })
            .done(function (data) {
                callback(data);
                $this.show();
            });
    }

    function isInt(value) {
        return !isNaN(value) && (function(x) { return (x | 0) === x; })(parseFloat(value))
    }

    $(document).on('click', '.save-signal-memo', function (e) {
        e.preventDefault();
        var $this = $(this);
        var currRow = $(this).closest('tr');
        $this.hide();
        var formData = new FormData();
        var dispositionIds = ""
        var configName = currRow.find('.config-name').val();
        var signalSource = currRow.find('.signalSource').val();
        var triggerVariable = currRow.find('.triggerVariable').val();
        var triggerValue;
        if (currRow.find('.triggerVariable').val() == $.i18n._('signalOutcomeLabel')) {
            triggerValue = currRow.find('.signalOutcomes').val();
        } else if (currRow.find('.triggerVariable').val() == $.i18n._('actionTakenLabel')) {
            triggerValue = currRow.find('.actionsTaken').val();
        } else if (currRow.find('.triggerVariable').val() == $.i18n._('dispositionLabel')) {
            var selectedValues = currRow.find('.disposition').select2('data');
            var displayValues = selectedValues.map(function(item) {
                return item.text;
            });
            triggerValue = displayValues;
            dispositionIds = currRow.find('.disposition').val();
        } else {
            triggerValue = currRow.find('.trigger-value').val();
        }
        var mailAddresses = currRow.find('#emailAddress').val();
        var configId;
        var emailSubject = currRow.find('#emailSubject').val();
        var emailBody = currRow.find('#emailBody').val();
        if (!currRow.parent().is('tfoot')) {
            formData.append("signalMemoId", notificationConfigTable.rows(currRow).data()[0].id)
            configId = notificationConfigTable.rows(currRow).data()[0].id;
        }
        formData.append("configName",configName);
        formData.append("signalSource",signalSource); noSelection="['': 'Select']"
        formData.append("triggerVariable",triggerVariable);
        formData.append("triggerValue",triggerValue);
        formData.append("dispositionIds",dispositionIds);
        formData.append("mailAddresses",mailAddresses);
        formData.append("emailSubject",emailSubject);
        formData.append("emailBody",emailBody);
        var mandatoryFields = false;
        checkIfConfigExistsWithConfigName(configName, configId, triggerVariable, statusOfConfigName, $this);

        function statusOfConfigName(data) {
            const mandatoryLabels = [
                $.i18n._('signalOutcomeLabel'),
                $.i18n._('actionTakenLabel'),
                $.i18n._('dispositionLabel')
            ];
            const isMandatoryLabel = mandatoryLabels.includes(triggerVariable);
            const mandatory = isMandatoryLabel ? Array.isArray(triggerValue) && triggerValue.length > 0 : Boolean(triggerValue);
            if (typeof data != 'undefined' && data.status) {
                mandatoryFields = configName && triggerVariable && mandatory && mailAddresses;
            } else {
                mandatoryFields = configName && triggerVariable && mandatory && mailAddresses && emailSubject && emailBody;
            }
            if (!mandatoryFields) {
                $.Notification.notify('error', 'top right', "Error", $.i18n._('memoErrorMessage'), {autoHideDelay: 5000});
            } else if (triggerVariable !== $.i18n._('signalOutcomeLabel') && triggerVariable !== $.i18n._('actionTakenLabel') && triggerVariable !== $.i18n._('dispositionLabel') && !isInt(triggerValue)) {
                $.Notification.notify('warning', 'top right', "Warning", $.i18n._('memoDateTriggerErrorMessage'), {autoHideDelay: 5000});
            } else if (data.status && data.data === triggerVariable) {
                $.Notification.notify('warning', 'top right', "Warning", $.i18n._('memoTriggerErrorMessage'), {autoHideDelay: 5000});
            } else {
                $.ajax({
                    url: saveSignalMemoConfigUrl,
                    type: "POST",
                    mimeType: "multipart/form-data",
                    processData: false,
                    contentType: false,
                    data: formData,
                })
                    .done(function (data) {
                        $response = JSON.parse(data);
                        if ($response.status) {
                            $.Notification.notify('success', 'top right', "Success", "Record saved successfully", {autoHideDelay: 5000});
                            notificationConfigTable.ajax.reload();
                            if (!notificationConfigTable.data().count()) {
                                $(".dt-scroll-body #signalMemoReportTable").toggle();
                            }
                            if (currRow.parent().is('tfoot')) {
                                $(".signal-memo-new-row").hide();
                            }
                        } else {
                            $.Notification.notify('error', 'top right', "Error", $response.message, {autoHideDelay: 5000});
                        }
                    });
            }
        }
    });
    var emailGenerationModal = $('#emailGenerationModal');

    $(document).on('click', '#sendEmail', function (e) {
        e.preventDefault();
        currentEditingRow = $(this).closest("tr");
        var data = getRowData(currentEditingRow);
        populateModal(data);
        emailGenerationModal.modal({show: true});
    });

    function getRowData(row) {
        var signalMemoId = notificationConfigTable.rows(row).data()[0] ? notificationConfigTable.rows(row).data()[0].id : null;
        var mailAddresses = row.find('#selectedAddress').val() ? JSON.parse(row.find('#selectedAddress').val()) : [];
        var emailSubject = row.find('#emailSubject').val() || $.i18n._('preDraftedEmailSubject');
        var emailBody = row.find('#emailBody').val() || $.i18n._('preDraftedemailBody');
        var isEditable = row.find("#isEditable").val();
        return {
            signalMemoId: signalMemoId,
            mailAddresses: mailAddresses,
            emailSubject: emailSubject,
            emailBody: emailBody,
            isEditable: isEditable
        };
    }

    function populateModal(data) {
        var $sentTo = emailGenerationModal.find('.assignedToSelect');
        $sentTo.find('option').remove();
        $sentTo.val(null).trigger("change");
        if (data.mailAddresses.length > 0) {
            $.each(data.mailAddresses, function (i, address) {
                if (address.name && address.id) {
                    var option = new Option(address.name, address.id, true, true);
                    $sentTo.append(option).trigger('change.select2');
                }
            });
        }

        var editor = tinyMCE.get('emailContentMessage');
        emailGenerationModal.find('#signalMemoId').val(data.signalMemoId);
        var isEditable = data.isEditable === "true";

        emailGenerationModal.find('#subject').val(data.emailSubject);
        editor.setContent(data.emailBody);

        if (isEditable) {
            editor.setMode('design');
            $sentTo.prop('disabled', false);
            emailGenerationModal.find('#emailGenResetBtn').prop('disabled', false);
            emailGenerationModal.find('#saveEmail').prop('disabled', false);
            emailGenerationModal.find('#cancelButton').prop('disabled', false);
            emailGenerationModal.find('#subject').prop('disabled', false);
        } else {
            editor.setMode('readonly');
            $sentTo.prop('disabled', true);
            emailGenerationModal.find('#emailGenResetBtn').prop('disabled', true);
            emailGenerationModal.find('#saveEmail').prop('disabled', true);
            emailGenerationModal.find('#cancelButton').prop('disabled', true);
            emailGenerationModal.find('#subject').prop('disabled', true);
        }
        tinymce_updateCharCounter(editor, tinymce_getContentLength());
    }

    $(document).on('click', '#emailGenerationModal #saveEmail', function (evt) {
        evt.preventDefault();
        var mailAddresses = emailGenerationModal.find('.assignedToSelect').val();
        var selectedAddress = emailGenerationModal.find('.assignedToSelect').select2('data');
        var selectedEmailAddress = [];
        $.each(selectedAddress, function (id, data) {
            var selected = {"name": data.text, "id": data.id};
            selectedEmailAddress.push(selected);
        });
        var emailSubject = emailGenerationModal.find('#subject').val();
        var emailBody = decodeFromHTML(tinyMCE.get('emailContentMessage').getContent());

        if (!validateEmailData(mailAddresses, emailSubject, emailBody)) {
            return;
        }

        if (!validateEmailAddresses(mailAddresses)) {
            displayErrorMessage($.i18n._('memoInvalidEmailErrorMessage'));
            return;
        }

        saveEmailData(mailAddresses, emailSubject, emailBody, selectedEmailAddress);
    });

    function validateEmailData(mailAddresses, emailSubject, emailBody) {
        const isMailAddressesValid = Array.isArray(mailAddresses) && mailAddresses.length > 0;

        const isEmailSubjectValid = Boolean(emailSubject);
        const isEmailBodyValid = Boolean(emailBody);

        if (!isMailAddressesValid || !isEmailSubjectValid || !isEmailBodyValid) {
            displayErrorMessage($.i18n._('memoErrorMessage'));
            return false;
        }
        const hasSpecialCharacters = emailBody.includes('&lt;') || emailBody.includes('&gt;');
        if (hasSpecialCharacters) {
            displayErrorMessage($.i18n._('specialCharacterErrorMessage'));
            return false;
        }

        return true;
    }


    function saveEmailData(mailAddresses, emailSubject, emailBody, selectedEmailAddress) {
        currentEditingRow.find('#emailAddress').val(mailAddresses).trigger("change");
        currentEditingRow.find('#emailSubject').val(emailSubject).trigger("change");
        currentEditingRow.find('#emailBody').val(emailBody).trigger("change");
        currentEditingRow.find('#selectedAddress').val(JSON.stringify(selectedEmailAddress)).trigger("change");
        emailGenerationModal.modal("hide");
    }

    function displayErrorMessage(message) {
        var errorMsg = "<div class='alert alert-danger'>" + escapeAllHTML(message) + "</div>";
        $('#emailGenerationModal .modal-body').prepend(errorMsg);
        setTimeout(function () {
            addHideClass($(".alert-danger"));
        }, 5000);
    }

    var addHideClass = function (row) {
        row.addClass('hide');
    };
    $(document).on('click', '#emailGenResetBtn', function () {
        var editor = tinyMCE.get('emailContentMessage');
        emailGenerationModal.find('.assignedToSelect').val(null).trigger('change');
        emailGenerationModal.find('#subject').val($.i18n._('preDraftedEmailSubject'));
        var preDraftedemailBody = $.i18n._('preDraftedemailBody')
        editor.setContent(preDraftedemailBody);
        tinymce_updateCharCounter(editor, tinymce_getContentLength());
    });

    function validateEmailAddresses(emailAddresses) {
        let isEmailValid = true;

        const USER_TOKEN = "User_";
        const USER_GROUP_TOKEN = "UserGroup_";
        const recipients = emailAddresses;

        for (let recipient of recipients) {
            recipient = recipient.trim();
            if (recipient.includes(USER_TOKEN) || recipient.includes(USER_GROUP_TOKEN)) {
                continue;
            } else if (EMAIL_REGEX.test(recipient)) {
                continue;
            } else {
                isEmailValid = false;
            }
        }
        return isEmailValid;
    }



    $(document).on('click', '.delete-signal-memo', function (e) {
        e.preventDefault();
        $(this).hide();
        var currRow = $(this).closest('tr');
        var formData = new FormData();
        formData.append("signalMemoId", notificationConfigTable.rows(currRow).data()[0].id)
        $.ajax({
            url: deleteSignalMemoConfigUrl,
            type: "POST",
            mimeType: "multipart/form-data",
            processData: false,
            contentType: false,
            data: formData,
        })
            .done(function (data) {
                $response = JSON.parse(data);
                if ($response.status) {
                    $.Notification.notify('success', 'top right', "Success", "Record deleted successfully", {autoHideDelay: 5000});
                    notificationConfigTable.ajax.reload();
                    if (!$response.data) {
                        $('#signalMemoReportTable td.dt-empty').toggle();
                    }
                    $(".signal-memo-new-row").hide();
                } else {
                    $.Notification.notify('error', 'top right', "Error", $response.message, {autoHideDelay: 5000});
                }
            })
    });

    if(signalOutcomesToBeDisabled.length > 0){
        console.log(signalOutcomesToBeDisabled)
        for (var i = 0; i < signalOutcomesToBeDisabled.length; i++) {
            var data = $('#signalOutcomes').select2('data')
            if(typeof data !== 'undefined' && data[0].text === signalOutcomesToBeDisabled[i].trim()){
                data[0].disabled = true
            }
            $('#signalOutcomes').find('option[value="' + signalOutcomesToBeDisabled[i].trim() + '"]').prop("disabled", true).trigger('change');
        }
    }

    var initSignalMemoReportTable = function () {
        var columns = create_signal_memo_report_table_columns();
        notificationConfigTable = $('#signalMemoReportTable').DataTable({
            responsive: true,
            processing: true,
            serverSide: true,
            language: {
                "url": "../assets/i18n/dataTables_" + userLocale + ".json",
                search: 'Search:'
            },
            "ajax": {
                "url": signalMemoConfigUrl,
                "dataSrc": "aaData",
            },
            layout: {
                topStart: null,
                topEnd: 'search',
                bottomStart: ['pageLength', 'info', {paging: {type: "full_numbers"}}],
                bottomEnd: null,
            },
            customProcessing: true,
            drawCallback: function (settings) {
                scrollOff();
                colEllipsis();
                webUiPopInit();
                var rowsSignalMemoReport = $('#signalMemoReportTable').DataTable().rows().data();
                pageDictionary($('#signalMemoReportTable_wrapper'), settings.json.recordsFiltered);
                showTotalPage($('#signalMemoReportTable_wrapper'), settings.json.recordsFiltered);
                if(rowsSignalMemoReport.length > 10)
                    $('.dt-scroll-body').css("overflow-y","scroll");
                else
                    $('.dt-scroll-body').css("overflow-y","none");
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
            "rowCallback": function (row, data, index) {
                signal.user_group_utils.bind_assignTo_to_signal_memo_row($(row), searchUserGroupListUrl, data.mailAddresses);
                $(row).find('.signalSource').select2({ multiple: true });
                $(row).find('.signalOutcomes').select2({ multiple: true });
                $(row).find('.actionsTaken').select2({ multiple: true });
                $(row).find('.disposition').select2({ multiple: true });
                $(row).find('.triggerVariable').select2();
                $(row).find('.assignedToSelect').next(".select2-container").css('display','none');
                $(row).find('.signalSource').next(".select2-container").css('display','none');
                $(row).find('.signalOutcomes').next(".select2-container").css('display','none');
                $(row).find('.actionsTaken').next(".select2-container").css('display','none');
                $(row).find('.disposition').next(".select2-container").css('display','none');
                $(row).find('.triggerVariable').next(".select2-container").css('display','none');
                bindValuesToSelect2($(row).find('.signalSource'), data.signalSource, signalSource);
                bindValuesToSelect2($(row).find('.signalOutcomes'), data.triggerValue, signalOutcomes);
                bindValuesToSelect2($(row).find('.actionsTaken'), data.triggerValue, actionsTaken);
                bindValuesToSelect2Disposition($(row).find('.disposition'), data.dispositionTriggerValue, dispositionList);
                bindValuesToSelect2($(row).find('.triggerVariable'), data.triggerVariable, triggerVariable);
                $(row).find("#emailBody").val(data.emailBody).trigger("change");
                $(row).find("#emailSubject").val(data.emailSubject).trigger("change");
                let  names = data.mailAddresses.map(item => item.id);
                $(row).find("#emailAddress").val(names).trigger("change");
                $(row).find("#selectedAddress").val(JSON.stringify(data.mailAddresses)).trigger("change");
                $(row).find('#isEditable').val(false);
            },
            "aaSorting": [],
            "bLengthChange": true,
            "iDisplayLength": 10,
            "aLengthMenu": [[10, 20, 50, -1], [10, 20, 50, "All"]],
            "pagination": true,
            "aoColumns": columns,
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }]
        });
    };

    var create_signal_memo_report_table_columns = function () {
        var aoColumns = [
            {
                "mData": "configName",
                "className":"col-md-1-half cell-break",
                "mRender": function (data, type, row) {
                    if (!row.configName) {
                        row.configName = '';
                    }
                    var configName =  "<span class='config-name-text'>" + addEllipsisWithEscape(row.configName)+ "</span>";

                    configName +="<div class='textarea-ext config-input hide'><span class='css-truncate expandable'><span class='branch-ref css-truncate-target'><input type='text' value='" + escapeAllHTML(row.configName) + "' title='" + escapeAllHTML(row.configName) + "' class='form-control config-name comment ellipsis' maxlength='255' style='width: 100%' disabled></span></span></div>"
                    return configName
                }
            },
            {
                "mData": "signalSource",
                "className":"col-md-2 signal-source-class cell-break",
                "mRender": function (data, type, row) {
                    let signalSources = ''
                    if(row.signalSource == 'null' || !row.signalSource){
                        signalSources = '';
                    } else {
                        var signalSourceValues = row.signalSource.split(',');
                        _.each(signalSourceValues, function (obj, index) {
                            if (index == signalSourceValues.length - 1) {
                                signalSources += obj;
                            } else {
                                signalSources += obj + ", ";
                            }
                        });
                    }
                    var $select = $("<select></select>", {
                        "value": row.signalSource,
                        "class": "form-control signalSource hide",
                        "disabled": "true"
                    });
                    var signal_source = $select.prop("outerHTML");
                    signal_source += '<div class="signal-source">' + addEllipsis(signalSources) + '</div>'
                    return signal_source;
                }
            },
            {
                "mData":"triggerVariable",
                "className":"col-md-1-half cell-break text-left",
                "mRender": function (data, type, row) {
                    if (!row.triggerVariable) {
                        row.triggerVariable = '';
                    }
                    var $select = $("<select></select>", {
                        "value": row.triggerVariable,
                        "class": "form-control triggerVariable hide",
                        "disabled": "true"
                    });
                    var trigger_variable = $select.prop("outerHTML");
                    trigger_variable +=  "<span class='trigger-var-text'>" + addEllipsis(row.triggerVariable)+ "</span>";
                    return trigger_variable;
                }
            },
            {
                "mData": "triggerValue",
                "className":"col-md-1-half trigger-val-class cell-break",
                "mRender": function (data, type, row) {
                    let triggerValues = '';
                    var trigger_value = '';
                    if (!row.triggerValue || row.triggerValue == 'null') {
                        return triggerValues;
                    }
                    if (row.triggerVariable == $.i18n._('signalOutcomeLabel') || $.i18n._('actionTakenLabel') || $.i18n._('dispositionLabel')) {
                        var triggerDropDownValues = row.triggerValue.split(',');
                        _.each(triggerDropDownValues, function (obj, index) {
                            if (index == triggerDropDownValues.length - 1) {
                                triggerValues += obj;
                            } else {
                                triggerValues += obj + ", ";
                            }
                        });
                    }
                    var $selectOutcome = $("<select></select>", {
                        "value": row.triggerValue,
                        "class": "form-control signalOutcomes hide",
                        "disabled": "true"
                    });
                    var $selectAction = $("<select></select>", {
                        "value": row.triggerValue,
                        "class": "form-control actionsTaken hide",
                        "disabled": "true"
                    });
                    var $selectDisposition = $("<select></select>", {
                        "value": row.triggerValue,
                        "class": "form-control disposition hide",
                        "disabled": "true"
                    });

                    if (row.triggerVariable == $.i18n._('signalOutcomeLabel') || $.i18n._('actionTakenLabel') || $.i18n._('dispositionLabel')) {
                        trigger_value = '<div class="trigger-value-drop">' + addEllipsis(triggerValues) + '</div>';
                    } else {
                        trigger_value = "<span class='trigger-val-text'>" + row.triggerValue + "</span>";
                    }
                    trigger_value += "<div class='textarea-ext trigger-val-input hide'><span class='css-truncate expandable'><span class='branch-ref css-truncate-target'><input type='number' value='" + row.triggerValue + "' title='" + row.triggerValue + "' class='form-control trigger-value comment ellipsis' min=\"0\" step=\"1\" style='width: 100%' disabled></span></span></div>"
                    trigger_value += $selectAction.prop("outerHTML");
                    trigger_value += $selectOutcome.prop("outerHTML");
                    trigger_value += $selectDisposition.prop("outerHTML");
                    return trigger_value;
                }
            },
            {
                "mData": "mailAddresses",
                "sortable": false,
                "className":"col-md-1-half text-center email-class cell-break",
                "mRender": function (data, type, row) {
                    return '<input type="hidden" name="emailAddress" id="emailAddress" value=""/>' +
                        '<input type="hidden" name="selectedAddress" id="selectedAddress" value=""/>' +
                        '<input type="hidden" name="emailSubject" id="emailSubject" value=""/>' +
                        '<input type="hidden" name="emailBody" id="emailBody" value=""/>' +
                        '<input type="hidden" name="isEditable" id="isEditable" value=""/>' +
                        '<a data-toggle="modal" id="sendEmail" data-target="#emailGenerationModal" tabindex="0" data-toggle="tooltip" title="Mail"  class="productRadio">' +
                        '<i class="mdi mdi-email-outline"></i></a>';
                }
            },
            {
                "mData": "",
                "sortable": false,
                "mRender": function (data, type, row) {
                    return "<a href='javascript:void(0);' title='Save' class='save-signal-memo table-row-saved hide pv-ic'> " +
                        "<i class='mdi mdi-check' aria-hidden='true'></i> </a>" +
                        "<span class='signalMemoId hide' data-signalMemoId=" + row.id + " ></span>" +
                        "<a href='javascript:void(0);' title='Edit' class='table-row-edit edit-signal-memo pv-ic hidden-ic'>" +
                        "<i class='mdi mdi-pencil' aria-hidden='true'></i>\</a>" +
                        "<a href='javascript:void(0);' title='Delete' class='table-row-del delete-signal-memo hidden-ic'> " +
                        "<i class='mdi mdi-close' aria-hidden='true'></i> \</a> " +
                        "<a href='javascript:void(0);' title='Delete' class='table-row-del remove-edit-memo hide hidden-ic'> " +
                        "<i class='mdi mdi-close' aria-hidden='true'></i> \</a> "
                },
                "className": 'col-md-1 text-center',
            }
        ];

        return aoColumns
    };

    initSignalMemoReportTable();

    $(".signal-memo-new-row").hide();
    $(document).on('click', "#notificationNewRow", function () {
        if (!notificationConfigTable.data().count()) {
            $('#signalMemoReportTable td.dt-empty').toggle();
            $(".dt-scroll-body #signalMemoReportTable").toggle();
        }
        $(".signal-memo-new-row").toggle();

        $(".signal-memo-new-row").find('.config-name').Reset_List_To_Default_Value();
        $(".signal-memo-new-row").find('.signalSource').Reset_List_To_Default_Value();
        $(".signal-memo-new-row").find('.signalOutcomes').Reset_List_To_Default_Value();
        $(".signal-memo-new-row").find('.actionsTaken').Reset_List_To_Default_Value();
        $(".signal-memo-new-row").find('.disposition').Reset_List_To_Default_Value();
        $(".signal-memo-new-row").find('.triggerVariable').Reset_List_To_Default_Value();
        $(".signal-memo-new-row").find('.trigger-value').Reset_List_To_Default_Value();
        $(".signal-memo-new-row").find('#emailAddress').val("");
        $(".signal-memo-new-row").find('#selectedAddress').val("");
        $(".signal-memo-new-row").find('#emailSubject').val("");
        $(".signal-memo-new-row").find('#emailBody').val("");
        $(".signal-memo-new-row").find('#sendEmail').removeClass('disabled-link');
        $(".signal-memo-new-row").find("#isEditable").val(true);
    });

    $("#notificationNewRow").hover(function(){
        $(this).attr("title", "Add Notification Configurations");
    });

    $(document).on('click', ".remove-signal-memo", function () {
        if (!notificationConfigTable.data().count()) {
            $('#signalMemoReportTable td.dt-empty').toggle();
            $(".dt-scroll-body #signalMemoReportTable").toggle();
        }
        $(".signal-memo-new-row").toggle();
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
    bindMultipleSelect2WithUrl(emailGenerationModal.find('.assignedToSelect'), searchUserGroupListUrl);
    var tinyMCEparams = {
        selector: 'textarea#emailContentMessage',
        height: 300,
        branding: false,
        plugins: 'table link code ',
        menubar: 'edit insert format table tools',
        max_chars: 8000,
        setup: function (editor) {
            //stackover flow link :https://stackoverflow.com/questions/11342921/limit-the-number-of-character-in-tinymce for character counter part
            const allowedKeys = [
                BACKSPACE_KEY,
                LEFT_ARROW_KEY,
                UP_ARROW_KEY,               // backspace, delete and cursor keys
                RIGHT_ARROW_KEY,
                DOWN_ARROW_KEY,
                DELETE_KEY
            ];
            editor.on('keydown', function (e) {
                if (allowedKeys.indexOf(e.keyCode) !== -1) return true;
                if (tinymce_getContentLength() >= this.settings.max_chars) {
                    e.preventDefault();
                    e.stopPropagation();
                    return false;
                }
                return true;
            });
            editor.on('keyup', function (e) {
                tinymce_updateCharCounter(this, tinymce_getContentLength());
            });
        },
        init_instance_callback: function () { // initialize counter div
            $('#' + this.id).prev().append('<div class="char_count" style="text-align:right"></div>');
            tinymce_updateCharCounter(this, tinymce_getContentLength());
        }
    };
    tinyMCE.init(tinyMCEparams);

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

    var currentRow
    $(document).on('click', '.textarea-ext .openStatusComment', function(evt) {
        evt.preventDefault();
        currentRow = $(this).closest('tr');
        var headerName = $(this).attr('data-name');
        var extTextAreaModal = $("#textarea-ext4");
        triggerChangesOnModalOpening(extTextAreaModal);
        var textArea = currentRow.find('.email-body');
        extTextAreaModal.find('.textAreaValue').val(textArea.val());
        extTextAreaModal.find('.modal-title').text('Email Body');
        extTextAreaModal.modal("show");
    });
    $(document).on('click', '.textarea-ext',function(){
       $(this).find('.countBox').addClass('hide');
    });

    function triggerChangesOnModalOpening(extTextAreaModal) {
        extTextAreaModal.on('shown.bs.modal', function () {
            $('textarea').trigger('keyup');
            //Change button label.
            if(extTextAreaModal.find('.textAreaValue').val()){
                extTextAreaModal.find(".updateTextarea").html($.i18n._('labelUpdate'));
            } else {
                extTextAreaModal.find(".updateTextarea").html($.i18n._('labelAdd'));
            }
        });
    }

    var extTextAreaModal = $("#textarea-ext4");
    $(document).on('click', '#textarea-ext4 .updateTextarea', function(evt) {
        evt.preventDefault();
        currentRow.find(".email-body").val(extTextAreaModal.find('textarea').val());
        extTextAreaModal.modal("hide");
    });

    function isValueInSelect($select, data_value){
        return $($select).children('option').map(function(index, opt){
            return opt.value;
        }).get().includes(data_value);
    }

    function bindValuesToSelect2(selector, data, dataList){
        if(data != 'null' && data){
            var values = data.split(',');
        }
        var $option;
        $.each(dataList, function (k, v) {
            _.each(values, function (obj, index) {
                if (obj === v) {
                    $option = new Option(v, v, true, true);
                    if(!isValueInSelect(selector, v)){
                        selector.append($option).trigger('change');
                    }
                }
            });
            $option = new Option(v, v, false, false);
            if(!isValueInSelect(selector, v)){
                selector.append($option).trigger('change');
            }
            if(signalOutcomesToBeDisabled.length > 0){
                for (var i = 0; i < signalOutcomesToBeDisabled.length; i++) {
                    if($option.value ===  signalOutcomesToBeDisabled[i].trim()){
                        selector.find($option).prop('disabled', true);
                    }
                }
            }
        });
    }
    function bindValuesToSelect2Disposition(selector, data, dataList) {
        var values = [];
        if (data && data !== 'null') {
            values = data.split(',').map(Number);
        }
        selector.empty();
        $.each(dataList, function (index, item) {
            var isSelected = values.includes(item.id);
            var $option = new Option(item.name, item.id, isSelected, isSelected);
            selector.append($option);
        });
        selector.trigger('change');
    }
});
