$(function () {
    var editingRow = undefined,cancelClicked=false;
    $("#closeAttachModal").on('click', function () {
        cancelClicked = true
    })
    $(document).on('click', '.btn-file-upload.allowEdit', function (e) {
        var attachmentRow = $(e.target).closest('tr');
        editingRow = $(this).closest("tr");
        if((editingRow.find(".attachmentFilePath").attr("data-inputtype")) == 'link')
        {
            $("#attachmentFileModal .file-uploader .browse").prop('disabled', true);
            $("#literatureFilePath").prop('disabled', false);
            $('#literatureFilePath').prop({
                placeholder: $.i18n._('placeholder.addReferenceLink')
            });
            $('#attachmentFileModal input[type=radio][value=link]').prop("checked", true);
            $("#attachmentTypeName").text('References');
        }
        else {
            $("#literatureFilePath").prop('disabled', true);
            $('#literatureFilePath').prop({
                placeholder: $.i18n._('placeholder.attachFile')
            });
            $("#attachmentFileModal .file-uploader .browse").prop('disabled', false);
            $("#attachmentTypeName").text('File Name');
            $('#attachmentFileModal input[type=radio][value=file]').prop("checked", true);
        }
        var attachmentId = attachmentRow.find('span[data-field="removeAttachment"]').attr("data-attachmentId");
        var currentAttachmentUrl = '/signal/validatedSignal/fetchCurrentAttachmentData?attachmentId=' + attachmentId;
        $.ajax({
            type: "POST",
            url: currentAttachmentUrl,
        })
            .done(function (result) {
                    $('#literatureFilePath').val(result.savedName);
                    $('#attachmentName').val(result.inputName);
            })
        editingRow.find(".referenceName").val("");
        editingRow.find(".referenceLink").val("");

        if (editingRow.find(".attachmentFilePath").val()) {
            $('#attachmentName').val(editingRow.find(".attachmentFilePath").val());
            $('#literatureFilePath').val(editingRow.find(".savedName").val());
        }

        $('#attachmentFileModal input[type=radio][name=filetype]').on('change', function () {
            if ($(this).val() === "file") {
                $("#literatureFilePath").prop('disabled', true);
                $("#literatureFilePath").val('');
                $('#literatureFilePath').prop({
                    placeholder : $.i18n._('placeholder.attachFile')
                });
                $("#attachmentFileModal .file-uploader .browse").prop('disabled', false);
                $("#attachmentTypeName").text('File Name');
            } else {
                $("#attachmentFileModal .file-uploader .browse").prop('disabled', true);
                $("#literatureFilePath").prop('disabled', false);
                //Added to fix PVS-38885
                $("#literatureFilePath").val('');
                $('#literatureFilePath').prop({
                    placeholder : $.i18n._('placeholder.addReferenceLink')
                });
                $("#literatureFilePath").attr('maxLength', 8000);
                $("#attachmentTypeName").text('References');
            }
        });
    });
    $(document).on('click', '#reference-table a.table-row-edit', function (e) {
        e.preventDefault();
        var currRow = $(this).closest('tr');
        currRow.find('.assessmentReferenceType').next(".select2-container").show();
        currRow.find(".reference-type").addClass("hide");
        currRow.find(".description").addClass("hide");
        currRow.find(".textarea-ext").removeClass("hide");
        currRow.find(".hyper-link").addClass("hide");
        currRow.find(".file-uploader").removeClass("hide");

        currRow.find(".editAttachment").addClass("hide");
        currRow.find(".deleteAttachment").addClass("hide");
        currRow.find(".updateAttachment").removeClass("hide");
        currRow.find(".cancelAttachment").removeClass("hide");
    });

    $(document).on('click','#reference-table tr .table-row-del.deleteAttachment', function (e) {
        e.preventDefault();
        var attachmentRow = $(e.target).closest('tr');
        var referenceType = attachmentRow.find('input[data-inputtype="file"]').length > 0? "Attachment":"Reference";
        var attachmentId = attachmentRow.find('span[data-field="removeAttachment"]').attr("data-attachmentId");
        var removeUrl = '/signal/validatedSignal/deleteAttachment?attachmentId=' + attachmentId;
        console.log(referenceType);
        bootbox.confirm({
            title: 'Delete '+referenceType,
            message: "Are you sure you want to delete this "+referenceType+"?",
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
                    $.ajax({
                        async: false,
                        type: "POST",
                        url: removeUrl,
                        data: {alertId: alertId},
                        beforeSend: function(){
                            $('.btn-danger').prop('disabled',true)
                        },
                    })
                        .done(function () {
                            $.Notification.notify('success', 'top right', "Success", "Record Deleted Successfully.", {autoHideDelay: 5000});
                            $('#reference-table').DataTable().clear().draw();
                        })
                        .fail(function () {
                            $.Notification.notify('error', 'top right', "Error", "Record deletion failed.", {autoHideDelay: 5000});
                        })
                        .always(function () {
                            $('#reference-table').DataTable().ajax.reload();
                        })
                }
            }
        });

    });

    var hideReferenceType = function () {
        $("#reference-table").DataTable().rows().every(function () {
            var row = $(this.node());
            if ($(row).find('.reference-type').is(':visible')) {
                $(row).find('.assessmentReferenceType').next(".select2-container").hide();
            } else {
                $(row).find('.assessmentReferenceType').next(".select2-container").show();
            }
        });
    }

    $(document).on('click', '#reference-table .cancelAttachment', function (e) {
        e.preventDefault();
        var currRow = $(this).closest('tr');
        currRow.find('.assessmentReferenceType').next(".select2-container").hide();
        currRow.find(".reference-type").removeClass("hide");
        currRow.find(".description").removeClass("hide");
        currRow.find(".textarea-ext").addClass("hide");
        currRow.find(".hyper-link").removeClass("hide");
        currRow.find(".file-uploader").addClass("hide");

        currRow.find(".editAttachment").removeClass("hide");
        currRow.find(".deleteAttachment").removeClass("hide");
        currRow.find(".updateAttachment").addClass("hide");
        currRow.find(".cancelAttachment").addClass("hide");
    });
    $(".assessmentReferenceType").select2();

    var alertId = $("#validatedSignalid").val()
    var referenceTable = $("#reference-table").DataTable({
        destroy: true,
        searching: false,

        responsive: false,
        "oLanguage": {
            "url": "../assets/i18n/dataTables_" + userLocale + ".json",
            "oPaginate": {
                "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
            },
            "sLengthMenu": "Show _MENU_",

            "sZeroRecords": "No data available in table", "sEmptyTable": "No data available in table"
        },
        "ajax": {
            "url": fetchAttachmentsUrl,
            "dataSrc": "",
            data:{'alertId':alertId}
        },
        "rowCallback": function (row, data, index) {
            $(row).find('.assessmentReferenceType').select2()
        },
        drawCallback: function (settings) {
            colEllipsis();
            webUiPopInit();
            hideReferenceType();
            if ($(this).find('tbody tr td').length === 1 && $('#removeReference').is(":visible")) {
                $('#reference-table td.dt-empty').hide();
            }
            var rowsDataAR = $('#reference-table').DataTable().rows().data();
            pageDictionary($('#reference-table_wrapper'),rowsDataAR.length);
            showTotalPage($('#reference-table_wrapper'),rowsDataAR.length);
        },
        "aoColumns": [
            {
                "mData": "referenceType",
                "className": "col-md-1-half cell-break word-break",
                "mRender": function (data, type, row) {
                    var select = '<select name="signalStatus" class="form-control status assessmentReferenceType hide" id="assessmentReferenceType">'
                    for (const [key, value] of Object.entries(referenceType)) {
                        if(key === data){
                            select +='<option value='+key+' selected>'+value+'</option>'
                        } else {
                            select +='<option value='+key+'>'+value+'</option>'
                        }
                    }
                    return '<div><span class="reference-type">' + referenceType[data] + '</span>' +
                        select+
                        '</div>'

                }
            },
            {
                "mData": "description",
                "className": "col-md-3 cell-break",
                "mRender": function (data, type, row) {
                    return '<div class="" style="white-space: pre-wrap"><span class="description">' + addEllipsisWithEscape(data) + '</span>' +
                        '</div>'+
                        '<div class="textarea-ext hide">' +
                        '<textarea type="text" class="form-control comment" rows="1" style="width: 100%;min-height: 25px; white-space:pre-wrap" maxlength="8000">' + escapeAllHTML(data) + '</textarea>' +
                        '<a class="btn-text-ext openStatusComment" href="javascript:void(0);" tabindex="0" title="Open in extended form">' +
                        '<i class="mdi mdi-arrow-expand font-20 blue-1"></i></a>\n' +
                        '</div>'
                }
            },
            {
                "mData": "link",
                "className": "col-md-3 cell-break",
                "mRender": function (data, type, row) {
                    var result;
                    var inputValue;
                    var attachmentType;
                    var savedName;
                    if (row.type === ATTACHMENT_TYPE_ENUM.ATTACHMENT) {
                        result = "<div class='hyper-link'><a class='hyper-link' href='/signal/attachmentable/download?type=assessment&&id=" + row.id + "'>" + addEllipsisWithEscape(row.link) + "</a></div>";
                        inputValue = escapeHTML(row.link);
                        savedName = row.savedName;
                        attachmentType = "file";
                    } else {
                        if (row.inputName === "undefined") {
                            result = "<div class='hyper-link'><a class='hyper-link'  href='" + row.link + "' target='_blank'>" + addEllipsisWithEscape(row.link) + "</a></div>";
                            inputValue = escapeHTML(row.link);
                        } else {
                            result = "<div class='hyper-link' style='width:375px' ><a class='hyper-link'  href='" + row.link + "' target='_blank'>" + addEllipsisWithEscape(row.inputName) + "</a></div>";
                            inputValue = escapeHTML(row.inputName);
                        }
                        savedName = row.link;
                        attachmentType = "link";
                    }
                    return result + '<div class="file-uploader hide" data-provides="fileupload">' +
                        '<input type="file" name="assessment-file" class="file">' +
                        '<input type="text" class="referenceName hide">' +
                        '<input type="text" class="referenceLink hide">' +
                        '<input type="text" class="savedName hide" value= "' + savedName + '">' +
                        '<div class="input-group" style="width: 100%">' +
                        '<input type="text" data-inputtype="'+attachmentType+'" class="form-control attachmentFilePath" placeholder="Attach a file" id="attachmentFilePath" name="assessment-file" value="'+inputValue+'" title="" disabled >' +
                        '<span class="input-group-btn ">' +
                        '<button class="btn btn-primary btn-file-upload allowEdit" type="button" data-toggle="modal" data-target="#attachmentFileModal">' +
                        '<i class="glyphicon glyphicon-search"></i>' +
                        '</button>' +
                        '</span>' +
                        '</div>' +
                        '</div>'

                }
            },
            {"mData": "modifiedBy",
            "className": "col-md-1-half cell-break",
                "mRender": function (data, type, row) {
                return "<span class='userName'>" + addEllipsis(data)+ "</span>";
            }},
            {"mData": "timeStamp",
            "className": "col-md-2"},
            {"mData": "id",
                "className": "col-md-1 text-center",
                "mRender": function (data, type, row) {
                    return '<span data-field="removeAttachment" class="hide" data-attachmentId=' + data + '></span>' +
                        '<a href="javascript:void(0);" title="Edit" class="table-row-edit pv-ic hidden-ic editAttachment">' +
                        '<i class="mdi mdi-pencil" aria-hidden="true"></i> </a>' +
                        '<a href="javascript:void(0);" title="Remove" class="table-row-del hidden-ic deleteAttachment">' +
                        '<i class="mdi mdi-close" aria-hidden="true"></i> </a>' +
                        '<a href="javascript:void(0);" title="Update" class="table-row-save hidden-ic pv-ic updateAttachment hide" data-editing="true">' +
                        '<i class="mdi mdi-check" aria-hidden="true"></i> </a>' +
                        '<a href="javascript:void(0);" title="Cancel" class="table-row-del hidden-ic cancelAttachment hide">' +
                        '<i class="mdi mdi-close" aria-hidden="true"></i> </a>'
                },
                "visible": setVisibilityForColumn()
            }
        ],
        "bAutoWidth": false,
        columnDefs: [{
            "targets": -1,
            "sortable": false,
            "render": $.fn.dataTable.render.text()
        }, {
            "targets": '_all',
            "render": $.fn.dataTable.render.text(),
            orderSequence: ['desc', 'asc']
        }]
    });
    $("#addReference").on('click', function (ev) {
        ev.preventDefault();
        if(!referenceTable.data().count()){
            $('#reference-table td.dt-empty').hide();
            $('#reference-table_wrapper .dt-scroll-body').hide();
        }
        $(".reference-table-foot").removeClass('hide');
        $("#removeReference").removeClass('hide');
        $("#addReference").addClass('hide');
        $('tfoot .status').Reset_List_To_Default_Value();
        $('tfoot #attachmentFilePath').Reset_List_To_Default_Value();
        $('tfoot .comment').Reset_List_To_Default_Value();
        $('#attachmentFileModal').find('tfoot .file').val('');
        $('.file-uploader #assessmentFile').Reset_List_To_Default_Value();
    });
    $.fn.Reset_List_To_Default_Value = function()
    {
        $.each($(this), function(index, el) {
            var Founded = false;

            $(this).find('option').each(function(i, opt) {
                if(opt.defaultSelected){
                    opt.selected = true;
                    Founded = true;
                }
            });
            if(!Founded)
            {
                if($(this).attr('multiple'))
                {
                    $(this).val([]);
                }
                else
                {
                    $(this).val("");
                }
            }
            $(this).trigger('change');
        });
    }
    $("#removeReference").on('click', function (ev) {
        ev.preventDefault();
        if (!referenceTable.data().count()) {
            $('#reference-table_wrapper .dt-scroll-body').show();
            $('#reference-table td.dt-empty').show();
        }
        $(".reference-table-foot").addClass('hide');
        $("#removeReference").addClass('hide');
        $("#addReference").removeClass('hide');
    });

    $(".currentDate").text(moment.utc(new Date()).tz(userTimeZone).format("DD-MMM-YYYY hh:mm:ss A"));

    var type = $('input[name="filetype"]:checked').val();
    $(document).on('click', '#attachmentFileModal .file-uploader .browse', function () {
        editingRow.find(".file-uploader input[type='file']").trigger('click');

    });

    // Handles the file input change event to ensure the selected file does not exceed the maximum upload limit.Done to fix PVS-65851
    function handleFileInputChange(fileInput) {
        if (!fileInput || !fileInput.files) {
            return;
        }

        var files = fileInput.files;
        var maxUploadLimitMB = maxUploadLimit / 1048576
        if (files.length > 0) {
            var file = files[0];
            if (file.size > maxUploadLimit) {
                $.Notification.notify('error', 'top right', "Failed", $.i18n._('fileUploadMaxSizeExceedError', maxUploadLimitMB), {autoHideDelay: 5000});
                $(fileInput).val('');
            }
        }
    }

    $(document).on('change', "#reference-table .file-uploader input[type='file'], .reference-table-foot .file-uploader input[type='file']", function () {
        var currentElement = $(this);
        handleFileInputChange(this);
        $("#literatureFilePath").val(currentElement.prop('files')[0].name);
    });
    $('#attachmentFileModal .modal-footer .btn-primary').on('click', function (evt) {
        var modal = $(this).closest("#attachmentFileModal");
        var attachmentType = modal.find("input[type=radio][name=filetype]:checked").val();
        editingRow.find(".attachmentFilePath").attr("data-inputtype",attachmentType);
        if (editingRow.find(".attachmentFilePath").attr("data-inputtype") === 'file') {
            if ($("#attachmentName").val()) {
                editingRow.find(".attachmentFilePath").val($("#attachmentName").val());
                editingRow.find(".referenceName").val($("#attachmentName").val());
            } else if (editingRow.find(".file-uploader input[type='file']").prop('files').length > 0) {
                editingRow.find(".attachmentFilePath").val(editingRow.find(".file-uploader input[type='file']").prop('files')[0].name);
            }
        } else {
            editingRow.find(".referenceLink").val($("#literatureFilePath").val());
            if ($("#attachmentName").val()) {
                editingRow.find(".attachmentFilePath").val($("#attachmentName").val());
                editingRow.find(".referenceName").val($("#attachmentName").val());
            } else {
                editingRow.find(".attachmentFilePath").val($("#literatureFilePath").val());
            }
        }
    });

    $('#attachmentFileModal .modal-footer .btn-default').on('click', function (evt) {
        var modal = $(this).closest("#attachmentFileModal");
        var attachmentType = modal.find("input[type=radio][name=filetype]:checked").val();
        if($("#literatureFilePath").val() && attachmentType==='file') {
            editingRow.find(".file-uploader input[type='file']").val("");
            if (!JSON.parse(cancelClicked)) {
                editingRow.find(".attachmentFilePath").val($("#attachmentFileModal #attachmentName").val());
                cancelClicked = false;
            }
        }
    });

    var formdata = new FormData();
    $(document).on('click', ".reference-table-foot .saveAttachment, #reference-table .updateAttachment", function () {
        $('#reference-table_wrapper .dt-scroll-body').show();
        var currentRow = $(this).closest("tr");
        var uploadType = currentRow.find(".attachmentFilePath").attr("data-inputtype");
        if (uploadType === 'file') {
            if (!currentRow.find(".file-uploader input[type='file']").prop('files').length && !currentRow.find(".savedName").val()) {
                if(!referenceTable.data().count()){
                    $('#reference-table_wrapper .dt-scroll-body').hide();
                }
                $.Notification.notify('error', 'top right', "Error", $.i18n._('saveSignalReference'), {autoHideDelay: 5000});
                return;
            }
        } else {
            if (!currentRow.find(".referenceLink").val() && !currentRow.find(".savedName").val()) {
                if(!referenceTable.data().count()){
                    $('#reference-table_wrapper .dt-scroll-body').hide();
                }
                $.Notification.notify('error', 'top right', "Error", $.i18n._('saveSignalReference'), {autoHideDelay: 5000}); //converted warning message to error message to sync up with PVS-47979
                return;
            }
        }
        $("#removeReference").addClass('hide');
        $("#addReference").removeClass('hide');
        formdata = new FormData();
        if (uploadType === 'file') {
            if (currentRow.find(".file-uploader input[type='file']").prop('files').length > 0) {
                var file = currentRow.find(".file-uploader input[type='file']").prop('files')[0];
                formdata.append("attachments", file);
                formdata.append("fileName", currentRow.find(".file-uploader input[type='file']").prop('files')[0].name);
            }
        } else {
            formdata.append("referenceLink", currentRow.find(".referenceLink").val());
        }
        formdata.append("attachmentType", currentRow.find("#assessmentReferenceType").val());
        var id = $("#validatedSignalid").val();
        formdata.append("id", id);

        $(".reference-table-foot").addClass('hide');
        formdata.delete('description');
        formdata.append('description', currentRow.find(".form-control.comment").val());
        formdata.append('inputName', currentRow.find(".attachmentFilePath").val());
        var isEditing = $(this).closest("a").attr('data-editing');
        var url;
        var isSuccess = true;
        if (isEditing === "true") {
            formdata.append("attachmentId", currentRow.find('span[data-field="removeAttachment"]').attr("data-attachmentId"));
            formdata.append("alertId", alertId);
            if (uploadType === "file") {
                url = updateAttachmentUrl;
            } else {
                url = updateReferenceUrl;
            }
            $.ajax({
                async: false,
                url: url,
                type: "POST",
                mimeType: "multipart/form-data",
                processData: false,
                contentType: false,
                data: formdata,
                beforeSend : function () {
                    $(".updateAttachment").prop('disabled',true);
                },
            })
                .done(function (data) {
                    if (uploadType === 'file') {
                        $.Notification.notify('success', 'top right', "Success", "Attachment Saved Successfully", {autoHideDelay: 5000});
                    } else {
                        $.Notification.notify('success', 'top right', "Success", "Reference Saved Successfully", {autoHideDelay: 5000});
                    }

                    $(".updateAttachment").prop('disabled',false);
                    $('#reference-table').DataTable().clear().draw();
                })
                .fail(function (data) {
                    if (uploadType === 'file' && data.status != 500) {
                        var contentType = data.getResponseHeader('content-type');
                        if (contentType && contentType.indexOf('application/json') !== -1) {
                             var response = JSON.parse(data.responseText);
                             $.Notification.notify('error', 'top right', "Error", response.message, {autoHideDelay: 5000});

                        } else {
                              var responseXml = data.responseText;
                              var parser = new DOMParser();
                              var xmlDoc = parser.parseFromString(responseXml, "text/xml");
                              var errorMessage = xmlDoc.querySelector("message").textContent;
                              $.Notification.notify('error', 'top right', "Error", errorMessage, {autoHideDelay: 5000});
                        }
                    } else {
                        $.Notification.notify('error', 'top right', "Error", "Signal reference save failed", {autoHideDelay: 5000});
                    }
                    $(".updateAttachment").prop('disabled',false);
                    isSuccess = false;
                })
                .always(function () {
                    if(isSuccess){
                        $('#reference-table').DataTable().ajax.reload();
                    }
                });
        } else {
            if (uploadType === "file") {
                url = uploadUrl;
            } else {
                url = addReferenceUrl;
            }
            $.ajax({
                async: false,
                url: url,
                type: "POST",
                mimeType: "multipart/form-data",
                processData: false,
                contentType: false,
                data: formdata,
                beforeSend : function () {
                    $(".saveAttachment").prop('disabled',true);
                },
            })
                .done(function (data) {
                    if (uploadType === 'file') {
                        $.Notification.notify('success', 'top right', "Success", "Attachment Saved Successfully", {autoHideDelay: 5000});
                    } else {
                        $.Notification.notify('success', 'top right', "Success", "Reference Saved Successfully", {autoHideDelay: 5000});
                    }

                    $(".saveAttachment").prop('disabled',false);
                    $('#reference-table').DataTable().clear().draw();

                })
                .fail(function (data) {
                    if (uploadType === 'file' && data.status != 500) {
                        var contentType = data.getResponseHeader('content-type');
                        if (contentType && contentType.indexOf('application/json') !== -1) {
                            var response = JSON.parse(data.responseText);
                            $.Notification.notify('error', 'top right', "Error", response.message, {autoHideDelay: 5000});
                        } else {
                            var responseXml = data.responseText;
                            var parser = new DOMParser();
                            var xmlDoc = parser.parseFromString(responseXml, "text/xml");
                            var errorMessage = xmlDoc.querySelector("message").textContent;
                            $.Notification.notify('error', 'top right', "Error", errorMessage, {autoHideDelay: 5000});
                        }
                    } else {
                        $.Notification.notify('error', 'top right', "Error", "Signal reference save failed", {autoHideDelay: 5000});
                    }
                    $(".saveAttachment").prop('disabled',false);
                    isSuccess = false;
                })
                .always(function () {
                    if(isSuccess){
                        $('#reference-table').DataTable().ajax.reload();
                    }
                });
        }
        $(".reference-table-foot .file-uploader input[type='text']").val('');
    });

    disableOptions(disabledValues);
    var detectedDate;
    if(JSON.parse(validationDateSynchingEnabled)){
       if($("#detectedDate").val()=="")
       {
           detectedDate= moment.utc(new Date()).tz(serverTimeZone).format("DD-MMM-YYYY")
       }else{
           detectedDate=$("#detectedDate").val()
       }
    }else{
        detectedDate=$("#detectedDate").val()
    }
    $('#detected-date-picker').datepicker({
        date: detectedDate,
        allowPastDates: true,
        momentConfig: {
            culture: userLocale,
            tz: serverTimeZone,
            format: DEFAULT_DATE_DISPLAY_FORMAT
        },
        restricted: [{from: TOMORROW , to: Infinity}]
    });
    $('#aggStartDatePicker').datepicker({
        date: $("#aggStartDate").val() ? $("#aggStartDate").val() : null,
        allowPastDates: true,
        momentConfig: {
            culture: userLocale,
            tz: userTimeZone,
            format: DEFAULT_DATE_DISPLAY_FORMAT
        }
    });
    $('#aggEndDatePicker').datepicker({
        date: $("#aggEndDate").val() ? $("#aggEndDate").val() : null,
        allowPastDates: true,
        momentConfig: {
            culture: userLocale,
            tz: userTimeZone,
            format: DEFAULT_DATE_DISPLAY_FORMAT
        }
    });
    $('#lastDecisionDatePicker').datepicker({
        date: $("#lastDecisionDate").val() ? new Date($("#lastDecisionDate").val()) : null,
        allowPastDates: true
    });
    $('#haDateClosedDatePicker').datepicker({
        date: $("#haDateClosed").val() ? $("#haDateClosed").val() : null,
        allowPastDates: true,
        momentConfig: {
            culture: userLocale,
            tz: userTimeZone,
            format: DEFAULT_DATE_DISPLAY_FORMAT
        }
    });
    $('#haDateClosedDatePi').datepicker({
        date: $("#haDateClosed").val() ? new Date($("#haDateClosed").val()) : null,
        allowPastDates: true
    });

    $('#ud-date-picker1').datepicker({
        date: $("#udDate1").val() ? new Date($("#udDate1").val()) : null,
        allowPastDates: true,
        momentConfig: {
            culture: userLocale,
            tz: serverTimeZone,
            format: DEFAULT_DATE_DISPLAY_FORMAT
        }
    });

    $('#ud-date-picker2').datepicker({
        date: $("#udDate2").val() ? new Date($("#udDate2").val()) : null,
        allowPastDates: true,
        momentConfig: {
            culture: userLocale,
            tz: serverTimeZone,
            format: DEFAULT_DATE_DISPLAY_FORMAT
        }
    });
    $('#udDate1').on('focusout', function(){
        $(this).val(newSetDefaultDisplayDateFormat( $(this).val()));
        if($(this).val()=='Invalid date'){
            $(this).val('');
        }
    });
    $('#udDate2').on('focusout', function(){
        $(this).val(newSetDefaultDisplayDateFormat( $(this).val()));
        if($(this).val()=='Invalid date'){
            $(this).val('');
        }
    });
    $('#detectedDate').on('focusout', function(){
        $(this).val(newSetDefaultDisplayDateFormat( $(this).val()));
        if($(this).val()==='Invalid date'){
            $(this).val('');
        }
    });

    $('#aggStartDate').on('focusout', function(){
        $(this).val(newSetDefaultDisplayDateFormat( $(this).val()));
        if($(this).val()==='Invalid date'){
            $(this).val('');
        }
    });

    $('#aggEndDate').on('focusout', function(){
        $(this).val(newSetDefaultDisplayDateFormat( $(this).val()));
        if($(this).val()==='Invalid date'){
            $(this).val('');
        }
    });

    $('#haDateClosed').on('focusout', function(){
        $(this).val(newSetDefaultDisplayDateFormat( $(this).val()));
        if($(this).val()==='Invalid date'){
            $(this).val('');
        }
    });

    $("#udDropdown1").select2({minimumResultsForSearch: Infinity});
    $("#udDropdown2").select2({minimumResultsForSearch: Infinity});

    $("#haSignalStatus").select2();
    $('#evaluationMethods').select2();

    var sourceArray = null;
    if ($("#signalActionTaken").val()) {
        sourceArray = $("#signalActionTaken").val().toString().replace("[", "").replace("]", "").split(',');
        for (var i = 0; i < sourceArray.length; i++) {
            sourceArray[i] = sourceArray[i].trim()
        }
        $("#actionTaken").select2().val(sourceArray).trigger('change')
    } else {
        $("#actionTaken").select2()
    }
    if ($("#signalEvaluationMethod").val()) {
        sourceArray = $("#signalEvaluationMethod").val().toString().replace("[", "").replace("]", "").split(',');
        for (var i = 0; i < sourceArray.length; i++) {
            sourceArray[i] = sourceArray[i].trim()
        }
        $("#evaluationMethod").select2().val(sourceArray).trigger('change')
    } else {
        $("#evaluationMethod").select2()
    }

    $(".signalStrategy").on('change', function () {
        var currentValue = $(this).val();
        if (currentValue != '') {
            //if the selected signal is not null then we show the product names
            $.ajax({
                url: searchStrategyProducts + "?id=" + currentValue,
            })
                .done(function (data) {
                    $("#productNames").val(data.productNames);
                });
            var comboLabel = $(".signalStrategy  option:selected").text();
            $("#signalName").val(comboLabel + "_signal");
        } else {
            $("#signalName").val('');
            $("#productNames").val('');
        }
    });

    $("#includeInAggregateReportChk").on('click', function () {
        var isChecked = $(this).is(":checked");
        if (isChecked) {
            $("#includeInAggregateReport").attr('value', true);
        } else {
            $("#includeInAggregateReport").attr('value', false);
        }
    });

    if ($("#signalSource").val()) {
        sourceArray = $("#signalSource").val().toString().replace("[", "").replace("]", "").split('##');
        for (var i = 0; i < sourceArray.length; i++) {
            sourceArray[i] = sourceArray[i].trim();
        }
        $("#initialDataSource").select2().val(sourceArray).trigger('change');
    } else {
        $("#initialDataSource").select2();
    }

    if ($("#topicCategoryList").val()) {
        sourceArray = $("#topicCategoryList").val().toString().replace("[", "").replace("]", "").split(',');
        for (var i = 0; i < sourceArray.length; i++) {
            sourceArray[i] = sourceArray[i].trim();
        }
        $("#signalTypeList").select2().val(sourceArray).trigger('change');
    } else {
        $("#signalTypeList").select2();
    }

    if ($("#udDropdown1Value").val()) {
        sourceArray = $("#udDropdown1Value").val().toString().split(',');
        for (var i = 0; i < sourceArray.length; i++) {
            sourceArray[i] = sourceArray[i].trim();
        }
        $("#udDropdown1").select2().val(sourceArray).trigger('change');
    } else {
        $("#udDropdown1").select2();
    }

    if ($("#udDropdown2Value").val()) {
        sourceArray = $("#udDropdown2Value").val().toString().split(',');
        for (var i = 0; i < sourceArray.length; i++) {
            sourceArray[i] = sourceArray[i].trim();
        }
        $("#udDropdown2").select2().val(sourceArray).trigger('change');
    } else {
        $("#udDropdown2").select2();
    }

    $("#linkedSignal").select2().data('select2').$dropdown.addClass('cell-break');;
    updateSignalOutcome(true);


   $('.textarea-ext .openTextArea').off().on('click', function (evt) {
        evt.preventDefault();
        var extTextAreaModal = $("#textarea-ext3");
        addCountBoxToInputField(8000, extTextAreaModal.find('textarea'));
        triggerChangesOnModalOpening(extTextAreaModal);
        var textArea = $(this).prev('textarea');
        extTextAreaModal.find('.textAreaValue').val(textArea.val());
        extTextAreaModal.find('.modal-title').text(textArea.prev('label').text().trim().replace("*", ''));
        extTextAreaModal.modal("show");
        updateTextAreaData2(textArea.prop('id'));
    });

    $('#genCommentModal').off().on('click', function (evt) {
        evt.preventDefault();
        var extTextAreaModal = $("#textarea-ext1");
        triggerChangesOnModalOpening(extTextAreaModal);
        var textArea = $(this).prev('textarea');
        extTextAreaModal.find('#referencesExpandedTextArea').val($('#genericComment').val())
        extTextAreaModal.find('.textAreaValue').val(textArea.val());
        extTextAreaModal.find('.modal-title').text(textArea.prev('label').text().trim().replace("*", ''));
        extTextAreaModal.find('.textAreaValue').prop('disabled', false);
        extTextAreaModal.find('.updateTextarea').prop('disabled', false);
        extTextAreaModal.modal("show");
        updateTextAreaData(textArea.prop('id'));
    });

    $('#resonForEvModal').off().on('click', function (evt) {
        evt.preventDefault();
        var extTextAreaModal = $("#textarea-ext2");
        addCountBoxToInputField(8000, $(this).find('textarea'));
        triggerChangesOnModalOpening(extTextAreaModal);
        extTextAreaModal.find('textarea').attr('maxlength', 8000);
        var textArea = $(this).prev('textarea');
        extTextAreaModal.find('.textAreaValue').val(textArea.val());
        extTextAreaModal.find('.modal-title').text(textArea.prev('label').text().trim().replace("*", ''));
        extTextAreaModal.modal("show");
        updateTextAreaData1(textArea.prop('id'));
    });

    function calculateCount(ibox, tareaMaxLength, lineBreaksCount) {
        var text = ibox.val();
        if (text.match(/\n/g)) {
            lineBreaksCount = text.match(/\n/g).length;
        }
        // console.log(id2);
        var length = text.length;
        var left = tareaMaxLength - (length + lineBreaksCount);
        if (left < 0) {
            length = length + left;
            var txt = text.substring(0, length);
            ibox.val(txt);
            left = 0;
        }
        tareaMaxLength = 4000;
        return {text:(length + lineBreaksCount) + ' / ' + tareaMaxLength, lineBreaksCount:lineBreaksCount};
    }
    function addCountBoxToInputField(maxLength, dataField) {
        var tareaMaxLength = maxLength;
        var inputField = dataField;

        inputField.parent().append('<div class="countBox"></div>');
        inputField.on('focus', function () {
            var ibox = $(this);
            var countBox = ibox.parent().find('.countBox');
            ibox.parent().css('position', 'relative');
            var lineBreaksCount = 0;
            ibox.on('keyup', function () {
                var result = calculateCount(ibox,tareaMaxLength,lineBreaksCount);
                lineBreaksCount = result.lineBreaksCount;
                countBox.text(result.text);
            });
            var firstResult;
            firstResult = calculateCount(ibox,tareaMaxLength,lineBreaksCount);
            lineBreaksCount = firstResult.lineBreaksCount;
            countBox.text(firstResult.text);
            countBox.show();
            ibox.on('focusout', function () {
                countBox.hide();
            });
        });
    }

    $('#resonForEvModal').off().on('click', function (evt) {
        evt.preventDefault();
        var extTextAreaModal = $("#textarea-ext2");
        addCountBoxToInputField(4000, $(this).find('textarea'));
        triggerChangesOnModalOpening(extTextAreaModal);
        extTextAreaModal.find('textarea').attr('maxlength', 8000);
        var textArea = $(this).prev('textarea');
        extTextAreaModal.find('.textAreaValue').val(textArea.val());
        extTextAreaModal.find('.modal-title').text(textArea.prev('label').text().trim().replace("*", ''));
        extTextAreaModal.modal("show");
        updateTextAreaData1(textArea.prop('id'));
    });

    function calculateCount(ibox, tareaMaxLength, lineBreaksCount) {
        var text = ibox.val();
        if (text.match(/\n/g)) {
            lineBreaksCount = text.match(/\n/g).length;
        }
        // console.log(id2);
        var length = text.length;
        var left = tareaMaxLength - (length + lineBreaksCount);
        if (left < 0) {
            length = length + left;
            var txt = text.substring(0, length);
            ibox.val(txt);
            left = 0;
        }
        return {text:(length + lineBreaksCount) + ' / ' + tareaMaxLength, lineBreaksCount:lineBreaksCount};
    }
    function addCountBoxToInputField(maxLength, dataField) {
        var tareaMaxLength = maxLength;
        var inputField = dataField;

        inputField.parent().append('<div class="countBox"></div>');
        inputField.on('focus', function () {
            var ibox = $(this);
            var countBox = ibox.parent().find('.countBox');
            ibox.parent().css('position', 'relative');
            var lineBreaksCount = 0;
            ibox.on('keyup', function () {
                var result = calculateCount(ibox,tareaMaxLength,lineBreaksCount);
                lineBreaksCount = result.lineBreaksCount;
                countBox.text(result.text);
            });
            var firstResult;
            firstResult = calculateCount(ibox,tareaMaxLength,lineBreaksCount);
            lineBreaksCount = firstResult.lineBreaksCount;
            countBox.text(firstResult.text);
            countBox.show();
            ibox.on('focusout', function () {
                countBox.hide();
            });
        });
    }

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

    function updateTextAreaData(containerId) {
        var extTextAreaModal = $("#textarea-ext1");
        $('#textarea-ext1 .updateTextarea').off().on('click', function (evt) {
            evt.preventDefault();
            $("#" + containerId).val(extTextAreaModal.find('textarea').val());
            extTextAreaModal.modal("hide");
        });
    }
    function updateTextAreaData1(containerId) {
        var extTextAreaModal = $("#textarea-ext2");
        $('#textarea-ext2 .updateTextarea').off().on('click', function (evt) {
            evt.preventDefault();
            $("#" + containerId).val(extTextAreaModal.find('textarea').val());
            extTextAreaModal.modal("hide");
        });
    }
    function updateTextAreaData2(containerId) {
        var extTextAreaModal = $("#textarea-ext3");
        $('#textarea-ext3 .updateTextarea').off().on('click', function (evt) {
            evt.preventDefault();
            $("#" + containerId).val(extTextAreaModal.find('textarea').val());
            extTextAreaModal.modal("hide");
        });
    }

    $(document).on('click', "#linkedSignalInfo", function () {
        var linkedSignalInfo = $("#linkedSignalInfo");
        $('.viewLinkedSignal').html("");
        if ($("#linkedSignal").val() && $("#linkedSignal").val().length > 0) {
            var htmlCode = '';
            $("#linkedSignal option:selected").each(function () {
                var $this = $(this);
                htmlCode += '<li><a href="' + linkedSignalDetailUrl + '?id=' + $this.val() + '" target="_blank">' + $this.text() + '</a></li>';
            });
            $('.viewLinkedSignal').append(htmlCode);
            linkedSignalInfo.show();
        } else {
            $('.viewLinkedSignal').html("");
            linkedSignalInfo.hide();
        }
    });

    $(document).on('change', "#linkedSignal", function () {
        var linkedSignalInfo = $("#linkedSignalInfo");
        if ($(this).val() && $(this).val().length > 0) {
            linkedSignalInfo.show();
        } else {
            linkedSignalInfo.hide();
        }
    });

    $(document).on('submit', '#signalEditForm', function (event) {
        //enabling disabled values before ajax call in order to get the values of that field
        $('option').prop("disabled", false);
        $('#updateSignal').prop("disabled", true);
        event.preventDefault();
        var data = $(this).serialize();
        $.ajax({
            type: "POST",
            url: updateSignalUrl,
            data: data,
            dataType: 'json',
        })
            .done(saveJustificationSuccessCallback)
            .fail(function (err) {
                $('#updateSignal').prop("disabled", false);
            });
        disableOptions(disabledValues);
      if(JSON.parse(validationDateSynchingEnabled))
      {
          $('#signal-history-table select option[value="'+defaultValidatedDate+'"]:selected').parent().parent().parent().find(".date-created").val($("#detectedDate").val())
      }
    });
    // added for PVS-71111
    $(document).on('submit', '#saveSignal', function (event) {
        event.preventDefault();
        $('#saveSignalBtn').prop('disabled', true);
        $('#cancelSignalBtn').prop('disabled', true);
        let mandatoryFieldsFilled = true;
        const mandatoryFields = ['signalName', 'detectedDate', 'initialDataSource'];

        // Check if mandatory fields are filled
        mandatoryFieldsFilled = mandatoryFields.every((field) => {
            const fieldValue = $(`#${field}`).val();
            if (field === 'initialDataSource') {
                return Array.isArray(fieldValue) ? fieldValue.length > 0 : false;
            }
            return !!fieldValue;
        });

        if ($('#productSelection').val() === '' && ($('#productGroupSelection').val() === '' || $('#productGroupSelection').val() === '[]')) {
            mandatoryFieldsFilled = false;
        }

        if (!mandatoryFieldsFilled) {
            showErrorModal({data: 'Please provide information for all the mandatory fields which are marked with an asterisk (*)'});
        } else {
            if ($('#eventSelection').val() === '' && ($('#eventGroupSelection').val() === '' || $('#eventGroupSelection').val() === '[]')) {
                bootbox.confirm({
                    title: 'Warning',
                    message: "Do you want to proceed and save the signal without selecting an event?",
                    buttons: {
                        confirm: {
                            label: 'Ok',
                            className: 'btn-primary'
                        },
                        cancel: {
                            label: 'Cancel',
                            className: 'btn-default'
                        }
                    },
                    callback: function (result) {
                        if (result) {
                            submitForm();
                        }
                        $('#saveSignalBtn').prop('disabled', false);
                        $('#cancelSignalBtn').prop('disabled', false);
                    }
                })
            } else {
                submitForm();
            }
        }
    });

    var submitForm = function () {
        var data = $('#saveSignal').serialize();
        $.ajax({
            type: "POST",
            url: saveSignalUrl,
            data: data,
            dataType: 'json',
        })
            .done(function (response) {
                if (response.status) {
                    window.location.href = 'index';
                } else {
                    showErrorModal(response);
                }
            })
            .fail(function (err) {});
    }

    var showErrorModal = function (result) {
        $('#saveSignalBtn').prop('disabled', false);
        $('#cancelSignalBtn').prop('disabled', false);
        var errorDetails = "<ul>";
        errorDetails += "<li>" + result.data + "</li>";
        errorDetails += "</ul>";
        var errorMsg = '<div class="alert alert-danger alert-dismissible  edit-alert-danger" role="alert">';
        errorMsg += '<button type="button" class="close errorButton">';
        errorMsg += '<span aria-hidden="true">&times;</span>';
        errorMsg += '<span class="sr-only">Close</span> ';
        errorMsg += '</button> ';
        errorMsg +='<div class="error-message">';
        errorMsg +=errorDetails
        errorMsg += '</div>';
        errorMsg += '</div>';
        $('#errorElement').prepend(errorMsg);
    };
    $(document).on('click', '#cancelSignalBtn', function (event) {
        event.preventDefault();
        $('#saveSignalBtn').prop('disabled', true);
        window.location.href = 'index';
    })

    var saveJustificationSuccessCallback = function (result) {
        if (result.status) {
            if(result.value != "null" && result.value != null){
                $('#dueInHeader').html(result.value + " Days");
                $("#dueDatePicker").hide();
                $(".editButtonEvent").show();
            } else {
                $('#dueInHeader').html('-');
                $("#dueDatePicker").hide();
                $(".editButtonEvent").hide()
            }
            $(".alert-success").find('.success-message').text(result.data.message);
            if (!$(".alert-danger").hasClass('hide')) {
                $(".alert-danger").addClass('hide')
            }
            $(".alert-success").removeClass('hide');
            setTimeout(function () {
                addHideClass($(".alert-success"))
            }, 5000);
            $('#signal-history-table select option[value="Due Date"]:selected').parent().parent().parent().find(".date-created").val(result.data.dueDate)
            $('#productSelectionAssessment').val($('#productSelection').val());
            $('#eventSelectionAssessment').val($('#eventSelection').val());
            $("#showProductSelectionAssessment").html($("#showProductSelection").html());
            $("#showEventSelectionAssessment").html($("#showEventSelection").html());
            $(".selectedEventDictionaryValueAssessment").html($(".selectedEventDictionaryValue").html());
            if(typeof assessmentDictionaryModal !=="undefined" )
            assessmentDictionaryModal.loadValuesToDictionary();
        } else {
            var $alert = $(".alert-danger");
            var errorDetails = "<ul>";
            if(typeof (result.data) == 'object' ) {
                errorDetails += "<li>" + result.data.message + "</li>";
            }
            else
                errorDetails += "<li>" + result.data + "</li>";
            errorDetails += "</ul>";
            $alert.find('.error-message').html(errorDetails);
            if (!$(".alert-success").hasClass('hide')) {
                $(".alert-success").addClass('hide')
            }
            $alert.removeClass('hide')
        }
        $("html, body").animate({scrollTop: 0}, "slow");
        $('#updateSignal').prop("disabled", false);
        disableOptions(disabledValues);
        updateSignalOutcome(false)
    };

    $(document).on('click', '.errorButton', function (event) {
        event.preventDefault();
        $(".alert-danger").addClass('hide');
    });

    $(document).on('click', '.successButton', function (event) {
        event.preventDefault();
        $(".alert-success").addClass('hide');
    });

    $(document).on('submit', '#saveSignal', function (){
        $('#btnSave').prop('disabled', true);
    });

    $('#signalInfoTab').on('click', function () {
        $("#isAssessmentDicitionary").val("false");
    });

    $('#assessmentTab').on('click', function () {
        $("#isAssessmentDicitionary").val("true");
    });

    $('#eventModalAssessment').on('shown.bs.modal', function () {
        $('#eventSmqSelectAssessment').on('change', function () {
            if ($(this).val() !== null) {
                eventValues = {"1": [], "2": [], "3": [], "4": [], "5": [], "6": [], "7": [], "8": []};
            } else {
                $("#eventModalAssessment").find("input[type=text]").attr("disabled", false);
            }
        });
    });


    function getURLParameter(url, name) {
        return (RegExp(name + '=' + '(.+?)(&|$)').exec(url)||[,null])[1];
    }

    $('#assessmentToPdf, #assessmentToXlsx, #assessmentToDocx').each( function(){ $(this).on('click', function(event) {
        event.preventDefault();

        var outputParam = getURLParameter($(this).attr('href'), 'outputFormat')

        var newForm = jQuery('<form>', {
            'action': $(this).attr('href'),
            'target': '_top'
        }).append(jQuery('<input>', {
            'name': 'dateRange',
            'value': $('#dateRange').val(),
            'type': 'hidden'
        })).append(jQuery('<input>',{
            'name': 'productSelection',
            'value': $('#productSelectionAssessment').val(),
            'type': 'hidden'
        })).append(jQuery('<input>',{
            'name': 'productGroupSelection',
            'value': $('#productGroupSelectionAssessment').val(),
            'type': 'hidden'
        })).append(jQuery('<input>',{
            'name': 'eventSelection',
            'value': $('#eventSelectionAssessment').val(),
            'type': 'hidden'
        })).append(jQuery('<input>',{
            'name': 'eventGroupSelection',
            'value': $('#eventGroupSelectionAssessment').val(),
            'type': 'hidden'
        })).append(jQuery('<input>',{
            'name': 'validatedSignal.id',
            'value': $('#signalId').val(),
            'type': 'hidden'
        })).append(jQuery('<input>',{
            'name': 'outputFormat',
            'value': outputParam,
            'type': 'hidden'
        })).append(jQuery('<input>',{
            'name': 'reportType',
            'value': 'peber',
            'type': 'hidden'
        }));

        $('#hiddenform').append(newForm);
        newForm.submit();
    })

    });

    $('#assessmentFile').on('change', function () {
        if ($(this).val()) {
            if ($(this)[0].files[0].size > maxUploadLimit) {
                $.Notification.notify('error', 'top right', "Failed", $.i18n._('fileUploadMaxSizeExceedError', maxUploadLimit/1048576), {autoHideDelay: 5000});
                $(this).val('');
            }
        }
    });
});

var addHideClass = function(row) {
    row.addClass('hide');
};

var disableOptions = function (disabledValues) {
    var arrayValues = [];
    $.map(disabledValues, function (value, index) {
        if (value != "") {
            arrayValues = value.toString().replace("[", "").replace("]", "").split(',');
            for (var i = 0; i < arrayValues.length; i++) {
                if (index == 'signalStatus') {
                    $('select[name=signalStatus]').find('option[value="' + arrayValues[i].trim() + '"]').prop("disabled", true);
                } else {
                    $('#' + index).find('option[value="' + arrayValues[i].trim() + '"]').prop("disabled", true);
                }
            }
        }
    });
};

function setVisibilityForColumn(){
    var result = true;
    if(typeof hasReviewerAccess !=="undefined" && !hasReviewerAccess){
        result = false;
    }
    return result;
};
