$(function() {
    var commentTemplateTable
    var unsavedChanges = false;
    var firstChange = true;

    $(document).on('click', "#addComment", function (event)  {
        var commentTemplateModal = $(".comment-template-modal");
        commentTemplateModal.modal("show");
        commentTemplateModal.modal({backdrop: 'static', keyboard: false})
        commentTemplateModal.find("#save-comment-template").text("Save");
        commentTemplateModal.find("#save-comment-template").off().on('click', function (evt) {
            addOrEditCommentTemplate(saveCommentTemplateUrl, commentTemplateModal, "");
        });
    });

    addCountBoxToInputField(32000, $('#comment-template'));

    var initCommentTemplateTable = function () {
        commentTemplateTable = $('#commentTemplateListTable').DataTable({

            layout: {
                topStart: ['pageLength', 'search'],
                topEnd: null,
                bottomStart: ['info', {paging: {type: "full_numbers"}}],
                bottomEnd: null,
            },
            regex: false,
            responsive: true,
            language: {
                "url": "../assets/i18n/dataTables_" + userLocale + ".json"
            },
            search: {
                smart: false
            },
            "ajax": {
                "url": commentTemplateUrl,
                "dataSrc": ""
            },
            "initComplete": function() {
                if(isAdmin) {
                    var buttonHtml = '<button type="button" class="btn btn-primary" id="addComment" data-toggle="modal">Add Comment Template</button>&nbsp;&nbsp;&nbsp;';
                    var $divToolbar = $('.dt-search');
                    $divToolbar.prepend(buttonHtml);
                }
                $(document).on("click", '.deleteCommentTemplate', function (event) {
                    event.preventDefault();
                    var url = $(this).parent().parent().parent().find('#deleteCommentTemplateUrl').val();
                    bootbox.confirm({
                        title: ' ',
                        message: "This will delete the record. Do you want to proceed?",
                        buttons: {
                            confirm: {
                                label: 'Yes',
                                className: 'btn-primary'
                            },
                            cancel: {
                                label: 'No',
                                className: 'btn-default'
                            }
                        },
                        callback: function (result) {
                            if (result) {
                                $.ajax({
                                    type: "GET",
                                    url: url,
                                    dataType: 'json',
                                })
                                    .done(function (data) {
                                        if (data.status) {
                                            commentTemplateTable.ajax.reload()
                                            showSuccessMsg(data.message)
                                        } else {
                                            showErrorMsg(data.message);
                                        }
                                    });
                            }
                        }
                    });
                });
                $(document).on('click', '.editCommentTemplate', function (event) {
                    event.preventDefault();
                    var url = $(this).parent().find('#editCommentTemplateUrl').val();
                    $.ajax({
                        type: "GET",
                        url: url,
                        dataType: 'json',
                    }).done(editSuccessCallback);
                });
                $(document).on('click', '.copyCommentTemplate', function (event) {
                    event.preventDefault();
                    var url = $(this).parent().parent().parent().find('#editCommentTemplateUrl').val();
                    $.ajax({
                        type: "GET",
                        url: url,
                        dataType: 'json',
                    }).done(copySuccessCallback);
                });
            },
            drawCallback: function (){
                colEllipsis();
                webUiPopInit();
            },
            "scrollX": true,
            "scrollY": true,
            "aaSorting": [[2, "desc"]],
            "bLengthChange": true,
            "iDisplayLength": 10,
            "aLengthMenu": [[10, 50, 100, 200, -1], [10, 50, 100, 200, "All"]],
            "aoColumns": aoColumns,
            columnDefs: [{
                "targets": '_all',
                orderSequence: ['desc', 'asc'],
            },
                {
                    "targets": 2,
                    "type": 'date'
                }]
        });
        return commentTemplateTable
    };
    var aoColumns = [
        {
            "mData": "name",
            "className": 'col-min-100 col-min-100',
            "mRender": function (data, type, row) {
                return encodeToHTML(data)
            }
        },
        {
            "mData": 'comments',
            "className": 'col-min-500 col-max-500',
            "mRender": function (data, type, row) {
                var colElement = '<div class="col-container"><div class="col-height">';
                colElement += encodeToHTML(row.comments);
                colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(escapeHTML(row.comments)) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                colElement += '</div></div>';
                return colElement
            }
        },
        {
            "mData": 'lastUpdated',
            "className": 'col-min-150 col-min-150',
            "mRender": function (data, type, row) {
                return encodeToHTML(data)
            }
        },
        {
            "mData": 'modifiedBy',
            "className": 'col-min-150 col-min-150',
            "mRender": function (data, type, row) {
                return encodeToHTML(data)
            }
        }];

    if (isAdmin) {
        aoColumns.push.apply(aoColumns, [{
            "mData": 'actions',
            "sortable": false,
            "className": 'col-min-75 col-min-75',
            "mRender": function (data, type, row) {
                var deleteUrl = deleteCommentTemplateUrl + "/" + row.id;
                var editCommentUrl = editCommentTemplateUrl + "/" + row.id;
                data = row;
                return "<div class='hidden-btn btn-group dropdown dataTableHideCellContent' align='center'> \
                            <a class='btn btn-success btn-xs editCommentTemplate'>" + 'Edit' + "</a> \
                            <button type='button' class='btn btn-default btn-xs dropdown-toggle' data-toggle='dropdown'> \
                                <span class='caret'></span> \
                                <span class='sr-only'>Toggle Dropdown</span> \
                            </button> \
                            \ <input type='hidden' id='editCommentTemplateUrl' value='" + editCommentUrl + "'/>\
                            \ <input type='hidden' id='deleteCommentTemplateUrl' value='" + deleteUrl + "'/>\
                            <ul class='dropdown-menu dropdown-menu-right' role='menu' style='min-width: 80px !important; font-size: 12px;'> \
                                <li role='presentation'><a class='copyCommentTemplate' data-name='" + encodeToHTML(row.name) + "'  data-version='" + row.version + "' data-id='" + row.id + "' " +
                    "role='menuitem'>" + 'Copy' + "</a></li>\
                                <li role='presentation'><a class='deleteCommentTemplate' data-name='" + encodeToHTML(row.name) + "'  data-version='" + row.version + "' data-id='" + row.id + "' " +
                    "role='menuitem' >" + 'Delete' + "</a></li>\
                             </ul></div>";
            }
        }]);
    }
    initCommentTemplateTable();
    function customSort(a, b) {
        if (a.text < b.text) {
            return -1;
        }
        if (a.text > b.text) {
            return 1;
        }
        return 0;
    }
    function sorter(data) {
        return data.sort(customSort);
    }
    $("#comment-template-counts").select2({
        multiple: true,
        sorter: sorter,
        placeholder: 'Select Counts',
        allowClear: true
    });
    $("#comment-template-counts").on("select2:select", function (evt) {
        var element = evt.params.data.element;
        var $element = $(element);

        $element.detach();
        $(this).append($element);
        $(this).trigger("change");
    });
    $("#comment-template-scores").select2({
        multiple: true,
        sorter: sorter,
        placeholder: 'Select Scores and other fields',
        allowClear: true
    });
    $("#comment-template-scores").on("select2:select", function (evt) {
        var element = evt.params.data.element;
        var $element = $(element);

        $element.detach();
        $(this).append($element);
        $(this).trigger("change");
    });

    $('.select2-search__field').css("width", "200px");
    var editSuccessCallback = function (response){
        if(response.status){
            var commentTemplateModal = $(".comment-template-modal");
            commentTemplateModal.modal("show");
            commentTemplateModal.find('#template-name').val(response.data.name);
            commentTemplateModal.find('#comment-template').val(response.data.comments);
            commentTemplateModal.find('#save-comment-template').text("Update");
            commentTemplateModal.find('#save-comment-template').off().on('click', function (evt){
                addOrEditCommentTemplate(updateCommentTemplateUrl, commentTemplateModal, response.data.id);
            });
        }
    }

    var copySuccessCallback = function (response){
        if(response.status){
            var commentTemplateModal = $(".comment-template-modal");
            firstChange= false;
            commentTemplateModal.modal("show");
            commentTemplateModal.find('#template-name').val("Copy of " + response.data.name);
            commentTemplateModal.find('#comment-template').val(response.data.comments);
            commentTemplateModal.find('#save-comment-template').text("Save");
            commentTemplateModal.find('#save-comment-template').off().on('click', function (evt){
                addOrEditCommentTemplate(saveCommentTemplateUrl, commentTemplateModal, "");
            });
        }
    }
    // Adding default comment when Add is clicked
    $("#populate-comment").on('click', function (){
        const counts = $('.comment-template-count .select2-selection__choice').map(function () {
            return this.title
        }).get();
        const scores = $('.comment-template-score .select2-selection__choice').map(function () {
            return this.title
        }).get();
        if(counts.length>0||scores.length>0){
            unsavedChanges = true;
        }
        $("#comment-template-counts").val(null).trigger('change');
        $("#comment-template-scores").val(null).trigger('change');
        const cursorPosition = $('#comment-template').prop("selectionStart");
        $("#comment-template").val(commentTemplateFromSelection(counts, scores, cursorPosition));
        if($('#save-comment-template').html()==="Save" && (counts.length>0||scores.length>0)){
            firstChange= false;
        }
    });

    var commentTemplateFromSelection = function(counts, scores, cursorIndex){
        let commentTemplate = "";
        var labelConfigNew = JSON.parse($("#labelConfig").val());
        if((cursorIndex===0&&$('#comment-template').val().trim().length===0)||
            $('#comment-template').val().trim().length===0||(firstChange&&$('#save-comment-template').html()==="Save")){
            commentTemplate+="The Product "+'<'+labelConfigNew.productName+'>' + " and Event " + '<'+ labelConfigNew.pt.split("#OR")[0] +'>/'+ '<'+labelConfigNew.pt.split("#OR")[1]+'>';
            counts.forEach(function (value, index, array){
                if(index===0){
                    commentTemplate+=" contain(s) "+'<'+value+'> '+value.replace("Count", "Cases");
                }else if(index===counts.length-1){
                    commentTemplate+=" and "+'<'+value+'> '+value.replace("Count", "Cases");
                }else{
                    commentTemplate+=" , "+'<'+value+'> '+value.replace("Count", "Cases");
                }
            });
            scores.forEach(function(value, index, array){
                if(index===0){
                    commentTemplate+=" , with "+ value +' = <'+value+'>';
                }else if(index===scores.length-1){
                    commentTemplate+=" and " + value +' = <'+value+'>';
                }else{
                    commentTemplate+=' , '+ value + ' = <'+value+'>';
                }
            });
            commentTemplate+='.';
        }else{
            commentTemplate= $('#comment-template').val();
            const preTemplate = commentTemplate.charAt(cursorIndex - 1) === '.' ? commentTemplate.slice(0, cursorIndex - 1) : commentTemplate.slice(0, cursorIndex);
            const postTemplate = commentTemplate.slice(cursorIndex, -1) + '.';
            let mainTemplate = "";
            counts.forEach(function (value, index, array){
                if(commentTemplate.substring(cursorIndex-10, cursorIndex) === "contain(s)"&&index===0){
                    mainTemplate+=" "+'<'+value+'> '+value.replace("Count", "Cases") +" , ";
                }else {
                    mainTemplate+=" , "+'<'+value+'> '+value.replace("Count", "Cases") +" ";
                }
            });
            scores.forEach(function(value, index, array){
                if(commentTemplate.substring(cursorIndex-4, cursorIndex) === "with"&&index===0){
                    mainTemplate+=" "+ value +' = <'+value+'> , ';
                }else{
                    mainTemplate+=' , '+ value + ' = <'+value+'> ';
                }
            });
            commentTemplate=preTemplate+mainTemplate+postTemplate;
        }
        return commentTemplate;
    }


    var addOrEditCommentTemplate = function (url, commentTemplateModal, commentTemplateId) {
        $('#save-comment-template').attr('disabled', true);
        const template_name = $("#template-name").val();
        const comment_template = $('#comment-template').val();
        $.ajax({
            url: url,
            type: "POST",
            data: {
                "id": commentTemplateId,
                "templateName": template_name,
                "comment": comment_template
            },
        })
            .done(function (data) {
                if (data.status) {
                    $('#save-comment-template').attr('disabled', false);
                    commentTemplateModal.modal("hide");
                    $('#template-name').val("");
                    $('#comment-template').val("");
                    $("#comment-template-counts").val(null).trigger('change');
                    $("#comment-template-scores").val(null).trigger('change');
                    unsavedChanges=false;
                    firstChange = true;
                    commentTemplateTable.ajax.reload();
                    showSuccessMsg(data.message)
                } else {
                    $('#save-comment-template').attr('disabled', false);
                    var $alert = $(".msgContainer").find('.alert');
                    $alert.find('.message').html(data.message);
                    $(".msgContainer").show();

                    $alert.alert();
                    $alert.fadeTo(2000, 1000).slideUp(1000, function () {
                        $alert.slideUp(1000);
                    });
                }
            })
    };

    $('#comment-template').on("keyup", function () {
        unsavedChanges = true;
    });

    $(document).on('click', '.close-comment-template', function (e) {
        if(unsavedChanges){
            bootbox.confirm({
                title: ' ',
                message: "There are unsaved changes in the screen. Do you want to proceed?",
                buttons: {
                    confirm: {
                        label: 'Yes',
                        className: 'btn-primary'
                    },
                    cancel: {
                        label: 'No',
                        className: 'btn-default'
                    }
                },
                callback: function (result) {
                    bootbox.hideAll();
                    if (result) {
                        unsavedChanges=false;
                        firstChange=true;
                        $("#comment-template-counts").val(null).trigger('change');
                        $("#comment-template-scores").val(null).trigger('change');
                        $('#template-name').val("");
                        $('#comment-template').val("");
                        $('.comment-template-modal').modal('hide');
                    }
                }
            });
        }else{
            unsavedChanges=false;
            firstChange=true;
            $("#comment-template-counts").val(null).trigger('change');
            $("#comment-template-scores").val(null).trigger('change');
            $('#template-name').val("");
            $('#comment-template').val("");
            $('.comment-template-modal').modal('hide');
        }
    });

    function getErrorMessageHtml(msg) {
        var alertHtml = '<div class="alert alert-danger alert-dismissible" role="alert"> '
            +
            '<button type="button" class="close" data-dismiss="alert"> ' +
            '<span aria-hidden="true">&times;</span> ' +
            '<span class="sr-only"><g:message code="default.button.close.label" /></span> ' +
            '</button> ' + msg;
        '</div>';
        return alertHtml;
    }

    function getSuccessMessageHtml(msg) {
        var alertHtml = '<div class="alert alert-success alert-dismissible" role="alert"> ' +
            '<button type="button" class="close" data-dismiss="alert"> ' +
            '<span aria-hidden="true">&times;</span> ' +
            '<span class="sr-only"><g:message code="default.button.close.label" /></span> ' +
            '</button> ' + msg;
        '</div>';
        return alertHtml;
    }

    function showErrorMsg(msg) {
        removeExistingMessageHolder();
        var alertHtml = getErrorMessageHtml(msg);
        $('body .messageContainer').prepend(alertHtml);
        $('body').scrollTop(0);
        $('.messageContainer').fadeTo(2000, 500).slideUp(500, function () {
            $('.messageContainer').slideUp(500);
        });
    }

    function showSuccessMsg(msg) {
        removeExistingMessageHolder();
        var alertHtml = getSuccessMessageHtml(msg);
        $('body .messageContainer').prepend(alertHtml);
        $('body').scrollTop(0);
        $('.messageContainer').fadeTo(2000, 500).slideUp(500, function () {
            $('.messageContainer').slideUp(500);
        });
    }

    function removeExistingMessageHolder() {
        $('.messageContainer').html("");
    }
});