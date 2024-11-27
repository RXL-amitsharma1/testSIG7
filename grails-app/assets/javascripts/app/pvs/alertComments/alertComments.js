var signal = signal || {}


signal.alertComments = (function () {

    var commentHistoryModalTable = null;

    var listComments = function (caseJson) {

        //Modal object
        var commentModal = $('#commentModal');

        //Populate the existing comments and bind events to them.
        populateComments(commentModal, caseJson);

        //Set values to the modal elements.
        var commentMetaInfo = ""
        if (caseJson.alertType == "Aggregate Case Alert") {
            commentMetaInfo = '<span id="productName">' + caseJson.productName + '</span> - <span id="eventName">' + caseJson.eventName + '</span>';
        } else {
            commentMetaInfo = '<span id="caseNumber">' + caseJson.caseNumber + '</span> - <span id="productFamily">' + caseJson.productFamily + '</span>';
        }
        commentModal.find("#comment-meta-info").html(commentMetaInfo);
        commentModal.find('.comment-history-icon').show();
        commentModal.find("#application").html(caseJson.alertType);
        commentModal.modal('show');
        bindSaveComment(commentModal);
    };

    var populateComments = function (commentModal, caseJson, onSignalScreen) {
        if (caseJson.alertType !== ALERT_CONFIG_TYPE.SINGLE_CASE_ALERT && caseJson.alertType !== ALERT_CONFIG_TYPE.AGGREGATE_CASE_ALERT) {
            addCountBoxToInputField(4000, $('#commentbox'));
        }
        var updatedComments = $("#commentbox").val();
        if (applicationName === ALERT_CONFIG_TYPE.SIGNAL_MANAGEMENT) {
            commentModal.find('#commentbox').prop("disabled", true);
            commentModal.find(".add-comments").hide()
        }
        if (applicationName === ALERT_CONFIG_TYPE.AGGREGATE_CASE_ALERT && selectedDatasource !== 'vigibase') {
            $.ajax({
                url: "/signal/commentTemplate/listOption",
            })
                .done(function (response) {
                    $selectElement = document.getElementById('commentTemplateSelect');
                    // get currently selected value
                    var value = $('#commentTemplateSelect').val();
                    var i, L = $selectElement.options.length - 1;
                    for (i = L; i >= 0; i--) {
                        $selectElement.remove(i);
                    }
                    var opt = document.createElement("option");
                    opt.value = "none";
                    opt.innerHTML = "Select an Option"; // whatever property it has

                    // then append it to the select element
                    $selectElement.appendChild(opt);
                    for (let element in response) {
                        var opt = document.createElement("option");
                        //get properties and add it to option element
                        opt.value = response[element].id;
                        opt.innerHTML = response[element].name;

                        // then append it to the select element
                        $selectElement.appendChild(opt);
                    }
                    $('#commentTemplateSelect').val(value);
                    $("#commentTemplateSelect").select2({
                        dropdownParent: $('#commentModal'),
                        value: value
                    });
                    $('#commentTemplateSelect').data('select2').$container.addClass('templateList')
                })
                .fail(function (response) {
                    console.log("Request for listOption failed with status" + response.status);
                });
        }
        $.ajax({
            url: "/signal/alertComment/listComments",
            data: caseJson,
            type: "POST",
        })
            .done(function (result) {
                var commentHistoryDataTable = $('#commentHistoryTable').DataTable();
                var comment = result.comment
                var commentHistoryList = result.commentHistoryList

                if (comment) {
                    commentModal.find("#no-comments").addClass("hide");
                    commentModal.find('#commentbox').removeClass("hide");
                    commentModal.find('.add-comments').html("Update");
                    if(updatedComments.length >= comment?.comments?.length){
                        commentModal.find("#commentbox").val(updatedComments);
                    }else{
                        commentModal.find("#commentbox").val(comment.comments);
                    }
                    commentModal.find("#commentId").val(comment.id);
                    var value = (comment.commentTemplateId && $('#commentTemplateSelect option[value="' + comment.commentTemplateId + '"]').length > 0) ?comment.commentTemplateId:"none"
                    $('#commentTemplateSelect').val(value).trigger("change.select2");
                    if (comment.comments != null && comment.comments !== "") {
                        if(applicationName !== ALERT_CONFIG_TYPE.AGGREGATE_CASE_ALERT) {
                            commentModal.find(".createdBy").text("Last Modified by " + comment.modifiedBy + " on " + moment.utc(comment.dateUpdated).format('DD-MMM-YYYY hh:mm:ss A'));
                        }
                    } else {
                        commentModal.find(".createdBy").text("");
                        commentModal.find('.add-comments').html("Add");
                    }

                    if ((applicationName ===  ALERT_CONFIG_TYPE.EVENT_DETAIL) || (commentModal.find('.add-comments').html() !== 'Update' && commentModal.find('#commentbox').val().length === 0 && commentModal.find('#commentbox').val() === "" && commentModal.find("#commentId").val() === "")) {
                        commentModal.find('.add-comments').prop("disabled", true);
                    }

                    if (applicationName === ALERT_CONFIG_TYPE.AGGREGATE_CASE_ALERT && commentHistoryList) {
                        commentHistoryDataTable.clear();
                        commentHistoryDataTable.rows.add(commentHistoryList);
                        commentHistoryDataTable.columns.adjust().draw();
                    }

                }else if(typeof isCommentAdded != 'undefined' && isCommentAdded && onSignalScreen){
                    commentModal.find("#commentId").val('');
                    commentModal.find('.add-comments').html("Add");
                    commentModal.find("#commentbox").val('');
                    commentModal.find("#no-comments").removeClass("hide");
                    if (commentModal.find('.add-comments').html() !== 'Update' && commentModal.find('#commentbox').val().length === 0 && commentModal.find('#commentbox').val() === "" && commentModal.find("#commentId").val() === "") {
                        commentModal.find('.add-comments').prop("disabled", true);
                    }
                    commentModal.find(".createdBy").text('');

                }else if(typeof isCommentAdded != 'undefined' && isCommentAdded){
                    commentModal.find("#commentId").val('');
                    commentModal.find('.add-comments').html("Add");
                    commentModal.find("#commentbox").val('');
                    commentModal.find("#no-comments").removeClass("hide");
                    if (commentModal.find('.add-comments').html() !== 'Update' && commentModal.find('#commentbox').val().length === 0 && commentModal.find('#commentbox').val() === "" && commentModal.find("#commentId").val() === "") {
                        commentModal.find('.add-comments').prop("disabled", true);
                    }
                    commentModal.find('#commentbox').addClass("hide");
                    commentModal.find(".createdBy").text('');
                }else{
                    commentModal.find("#commentId").val('');
                    commentModal.find(".createdBy").text('');
                    commentModal.find('.add-comments').html("Add");
                    commentModal.find('.add-comments').prop("disabled", true);
                    $('#commentTemplateSelect').val("none");

                    if (applicationName === ALERT_CONFIG_TYPE.AGGREGATE_CASE_ALERT && commentHistoryList) {
                        commentHistoryDataTable.clear();
                        commentHistoryDataTable.rows.add(commentHistoryList);
                        commentHistoryDataTable.columns.adjust().draw();
                    }
                }

                commentModal.find("#caseId").val(comment&&comment.caseId ? comment.caseId : caseJson.caseId);
                commentModal.find("#versionNum").val(caseJson.caseVersion);
                commentModal.find('#commentbox').on("keyup", function () {
                    toggleCommentButton(commentModal);
                });

                toggleCommentButton(commentModal);

                function toggleCommentButton(commentModal) {
                    const commentId = commentModal.find("#commentId").val();
                    const commentBoxValue = commentModal.find('#commentbox').val();
                    const addCommentsButton = commentModal.find('.add-comments');

                    if (commentId === "" && commentBoxValue === "") {
                        if (addCommentsButton.html() !== 'Update') {
                            addCommentsButton.prop("disabled", true);
                        }
                    } else if (comment?.comments?.length > 0 && (comment?.comments == commentBoxValue) && commentBoxValue !== "") {
                        addCommentsButton.prop("disabled", true);
                    } else {
                        addCommentsButton.prop("disabled", false);
                    }
                }

                $(document).on('click', '.ico-clone', function (event) {
                    if(!this.firstElementChild.classList.contains('fa-check')) {
                        commentHistoryDataTable.rows().iterator('row', function (context, index) {
                            var node = $(this.row(index).node());
                            node.find('.fa-clone, .fa-check').removeClass('fa-check').addClass('fa-clone');
                            //node.context is element of tr generated by jQuery DataTables.
                        });
                        const index = $('#commentbox').prop("selectionStart");
                        var comment = $('#commentbox').val();
                        comment = comment.slice(0, index) + decodeFromHTML(this.parentElement.parentElement.lastElementChild.firstElementChild.getAttribute('more-data')) + comment.slice(index);
                        $("#commentbox").val(comment);
                        commentModal.find('.add-comments').prop("disabled", false);
                        $(this).children().removeClass('fa-clone').addClass('fa-check');
                    }
                });

                $(document).on('click', '.ico-dots-modal', function(event) {
                    event.stopImmediatePropagation();
                    bootbox.dialog({
                        title: "Comment",
                        message: escapeHTML(decodeFromHTML(this.getAttribute('more-data'))).replace(/(?:\r\n|\r|\n)/g, '<br>'),
                        buttons: {
                            cancel: {
                                label: "Close",
                                className: 'btn btn-default'
                            }
                        },
                        onHide: function (){
                            if($(".modal").hasClass('in')){
                                $('body').addClass('modal-open');
                            }
                        }
                    }).find("div.modal-body").addClass("height-300");
                });
            })
            .fail(function () {
                console.log("Unable to load previous comments.");
            });
    };

    var bindSaveComment = function (commentModal) {

        //Add click event to the add comment button.
        commentModal.find(".add-comments").off().on('click', function () {
            var caseJsonObjArray = [];
            var appType = $("#application").html();
            var validatedSignalId = $("#validatedSignalId").html();
            var topicId = $("#topicId").html();

            if (appType == '') {
                appType = commentModal.find("#application").html();
                validatedSignalId = commentModal.find("#validatedSignalId").html();
                topicId = commentModal.find("#topicId").html()
            }

            var caseJsonObj;
            var url;
            var data;
            var isFaers = $('#isFaers').val() == "true" ? true: false;
            var isVaers = $('#isVaers').val() == "true" ? true: false;
            var isVigibase = $('#isVigibase').val() == "true" ? true: false;

            //Case JSON.
            caseJsonObj = {
                "alertType": appType,
                "productName": commentModal.find("#productName").html(),
                "eventName": commentModal.find("#eventName").html(),
                "comments": commentModal.find("#commentbox").val(),
                "validatedSignalId": validatedSignalId,
                "assignedTo": commentModal.find("#assignedTo").html(),
                "executedConfigId": commentModal.find("#executedConfigId").html(),
                "configId" : commentModal.find("#configId").html(),
                "topicId": commentModal.find("#topicId").html(),
                "productFamily": commentModal.find("#prodFamily").html(),
                "caseNumber": commentModal.find("#caseNo").html(),
                "productId": parseInt(commentModal.find("#productId").html()),
                "ptCode": parseInt(commentModal.find("#ptCode").html()),
                "alertName": commentModal.find("#alertName").html(),
                "caseId": commentModal.find("#caseId").html()!==undefined?commentModal.find("#caseId").html():commentModal.find("#alertId").html(),
                "caseVersion": commentModal.find("#versionNum").html(),
                "versionNum": commentModal.find("#versionNum").html(),
                "followUpNumber": commentModal.find("#followUpNumber").html(),
                "isFaers": isFaers,
                "isVaers": isVaers,
                "isVigibase": isVigibase,
                "commentTemplateId": commentModal.find("#commentTemplateSelect").val()
            };
            caseJsonObjArray.push(caseJsonObj);
            if(commentModal.find("#commentId").val()){
                url = "/signal/alertComment/updateComment";
                data = {
                    "alertType": appType,
                    "comment": commentModal.find("#commentbox").val(),
                    "id": commentModal.find("#commentId").val(),
                    "executedConfigId": commentModal.find("#executedConfigId").html(),
                    "validatedSignalId": validatedSignalId,
                    "alertName": commentModal.find("#alertName").html(),
                    "caseId": commentModal.find("#caseId").html(),
                    "caseNumber": commentModal.find("#caseNumber").html(),
                    "caseVersion": commentModal.find("#versionNum").html(),
                    "versionNum": commentModal.find("#versionNum").html(),
                    "followUpNumber": commentModal.find("#followUpNumber").html(),
                    "configId": commentModal.find("#configId").html(),
                    "isFaers": isFaers,
                    "isVaers": isVaers,
                    "isVigibase": isVigibase
                };
            }else{
                url = "/signal/alertComment/saveComment";
                data = {"caseJsonObjArray": JSON.stringify(caseJsonObjArray)}
            }

            $.ajax({
                url: url,
                data: data,
                type: "POST",
                async: true,
            })
                .done(function (result) {
                    if(result.success) {
                        commentModal.find("#isUpdated").val("true");
                        populateComments(commentModal, caseJsonObjArray[0]);
                        $('#commentbox').val('').blur()
                        if(data.comment=="" && commentModal.find('.add-comments').html() === 'Update'){
                            $.Notification.notify('success', 'top right', "Success", "Comment(s) removed successfully.", {autoHideDelay: 5000});
                        }
                        else if(commentModal.find('.add-comments').html() === 'Add'){
                            $.Notification.notify('success', 'top right', "Success", "Comment(s) added successfully.", {autoHideDelay: 5000});                        }
                        else {
                            $.Notification.notify('success', 'top right', "Success", result.message, {autoHideDelay: 5000});
                        }
                    }
                })
                .fail(function (result) {
                    $.Notification.notify('error', 'top right', "Error", result.message, {autoHideDelay: 5000});
                });
        });
    };

    var bindEditComment = function (commentModal) {

        commentModal.find(".previous-comments").find('.edit-comment').on('click', function () {
            $(this).parent().find(".update-comment").removeClass("hidden");
            $(this).parent().find(".comment-area").attr('disabled', false);
            $(this).addClass("hidden")
        });
    };

    var bindDeleteComment = function (commentModal, caseJson) {

        commentModal.find(".previous-comments").find('.delete-comment').on('click', function () {
            var id = {
                "id": $(this).parent().find(".commentId").val(),
                "validatedSignalId": $("#validatedSignalId").text(),
                "adhocAlertId" : caseJson.adhocAlertId
            };
            $.ajax({
                url: "/signal/alertComment/deleteComment",
                data: id,
                async: false,
            })
                .done(function (result) {
                    populateComments(commentModal, caseJson);
                    commentModal.find(".createdBy").text('');
                })
                .fail(function () {});
        });
    };

    var bindUpdateComment = function (commentModal, caseJson) {


        commentModal.find(".previous-comments").find('.update-comment').on('click', function () {

            $(this).parent().find(".glyphicon-pencil").removeClass("hidden");
            var commentData = {
                "comment": $(this).parent().find(".comment-area").val(),
                "id": $(this).parent().find(".commentId").val(),
                "validatedSignalId": $("#validatedSignalId").text(),
                "topicId": $("#topicId").html(),
                "executedConfigId": caseJson.executedConfigId,
                "adhocAlertId" : caseJson.adhocAlertId
            };

            $.ajax({
                url: "/signal/alertComment/updateComment",
                data: commentData,
                async: false
            })
                .done(function (result) {
                    populateComments(commentModal, caseJson);
                })
                .fail(function () {});
        });
    };

    var populateCommentsHistory = function (commentModal, caseJson, onSignalScreen) {

        var isFaers = $('#isFaers').val() == "true" ? true: false;
        var isVaers = $('#isVaers').val() == "true" ? true: false;
        var isVigibase = $('#isVigibase').val() == "true" ? true: false;

        var commentId = commentModal.find('#commentId').val();
        var dataUrl = "/signal/alertComment/listCommentsHistory" + "?caseNumber=" + commentModal.find("#caseNumber").html() + "&productFamily=" + encodeURIComponent(commentModal.find("#productFamily").html())
            + "&isFaers=" + isFaers + "&isVaers=" + isVaers + "&isVigibase=" + isVigibase
            + "&versionNum=" + commentModal.find("#versionNum").val() + "&caseId=" + commentModal.find("#caseId").val();


        if (typeof commentHistoryModalTable != "undefined" && commentHistoryModalTable != null) {
            commentHistoryModalTable.destroy()
        }


        //Data table for the document modal window.
        commentHistoryModalTable = $("#commentHistoryModalTable").DataTable({
            "language": {
                "url": "../assets/i18n/dataTables_" + userLocale + ".json"
            },
            initComplete: function () {
                $('[data-toggle="tooltip"]').tooltip()
                colEllipsis();
            },
            drawCallback: function(){
                colEllipsis();
                commentUiPopInit();
            },
            "ajax": {
                "url": dataUrl,
                "dataSrc": "",
                "type": "POST",
            },
            searching: false,
            "bLengthChange": true,
            "iDisplayLength": 5,
            "aaSorting": [[5,"desc"]],
            "aLengthMenu": [[5, 10, 20, 50, -1], [5, 10, 20, 50, "All"]],
            "aoColumns": [
                {
                    "mData": "alertName",
                    'className': 'col-min-120 col-max-150 dt-center new-word-break',
                    "mRender": function (data, type, row) {
                        if(row.alertName) {
                            var cumm="CUMM_";
                            var indexOfCumCount=row.alertName.indexOf(cumm);
                            if(indexOfCumCount){
                                return row.alertName.replace(cumm,"CUM_");
                            }
                            else{
                                return row.alertName;
                            }
                        }
                        else{
                            return '-';
                        }
                    },
                    "sWidth": "20%",
                    "sortable": false,
                },
                {
                    "mData": "caseNumber",
                    'className': 'col-min-80 col-max-120 dt-center',
                    "mRender": function (data, type, row) {
                        var tooltipMsg = "Version:" + row.caseVersion;
                        var followUpString = "";
                        if(!isVaers && !isFaers && !isVigibase){
                            followUpString =  '(' + row.followUpNumber + ')';
                        }
                        if (row.caseNumber != null && row.followUpNumber != null) {
                            var caseNum = "<span data-toggle='tooltip' data-placement='right' title='" + tooltipMsg + "'>";
                            caseNum+= row.caseNumber + followUpString;
                            caseNum+="</span>";
                            return caseNum;
                        } else {
                            return '';
                        }
                    },
                    "sortable": false,
                },
                {
                    "mData": "oldCommentTxt",
                    'className': 'col-min-150 col-max-250 cell-break',
                    "mRender": function (data, type, row) {
                    if (row.oldCommentTxt) {
                        return "<span class='commentOldPopup' data='"+ escapeAllHTML(row.oldCommentTxt)+"'>" +addCommentEllipsis(row.oldCommentTxt) + "</span>";
                    } else {
                        return "-"
                    }
                    },
                    "sortable": false,
                },
                {
                    "mData": "newCommentTxt",
                    'className': 'col-min-200 col-max-300 cell-break',
                    "mRender": function (data, type, row) {
                        if (row.newCommentTxt) {
                            return  "<span class='commentNewPopup' data='"+(escapeAllHTML(row.newCommentTxt))+"'>" + addCommentEllipsis(row.newCommentTxt) + "</span>";
                        } else {
                            return "-"
                        }
                    },
                    "sortable": false,

                },
                {
                    "mData" : "updatedBy",
                    'className' : "col-min-80 col-max-120 dt-center",
                    "sortable": false,
                },
                {
                    "mData": "updatedDate",
                    "sWidth": "15%",
                    'className': 'col-min-80 col-max-120 dt-center',
                    "mRender": function (data, type, row) {
                        if(row.updatedDate=="null" || row.updatedDate==null){
                            return '' ;
                        } else {
                            return moment.utc(row.updatedDate).format('DD-MMM-YYYY hh:mm:ss A');
                        }
                    }
                }
            ],
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }]
        });
        return commentHistoryModalTable
    };

    return {
        list_comments: listComments,
        populate_comments: populateComments,
        populate_comments_history: populateCommentsHistory,
        save_comment: bindSaveComment
    }
})();

var addCommentEllipsis = function (rowValue) {
    var colElement = '';
    if (rowValue) {
        var colElement = '<div class="col-container"><div class="col-height">';
        colElement +=  escapeAllHTML(rowValue).replace(/(?:\r\n|\r|\n)/g, '<br />')
        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + escapeAllHTML(rowValue) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
        colElement += '</div></div>';
        return colElement
    }
    return colElement
};

var commentUiPopInit = function(){
    var anchor = $(".view-all");
    anchor.webuiPopover({
        html: true,
        trigger: 'hover',
        content: function () {
            return encodeToHTML($(this).attr('more-data')).replace(/(?:\r\n|\r|\n)/g, '<br />')
        }
    });
    anchor.on("keypress",function( event ){
        if(event.keyCode ===13){
            $(this).webuiPopover('show');
        }
    });
    anchor.on("focusout",function( event ){
        $(this).webuiPopover('hide');
    });
};
