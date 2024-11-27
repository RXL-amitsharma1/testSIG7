//= require app/pvs/common/rx_common.js
//= require app/pvs/common/rx_list_utils.js
//= require app/pvs/common/rx_handlebar_ext.js
//= require app/pvs/topic/attachAlertToTopic.js

var signal = signal || {};

var topicData;
var columnSeq;
var isViewInstance = 1;
var signalAlertType = {
    SINGLE_CASE_ALERT: "Single Case Alert",
    AGGREGATE_ALERT: "Aggregate Case Alert",
    EVDAS_ALERT: "EVDAS Alert",
    ADHOC_ALERT: "Ad-Hoc Alert",
    LITERATURE_SEARCH_ALERT: "Literature Search Alert"
}
var filteredViews;
var dragged;
var value;
var index;
var indexDrop;
var list;
var caseJsonArrayInfo = [];
var advancedFilterChanged = true;
var isPagination = false;
main_disposition_filter=false

signal.alertReview = (function () {

    var ids = [];
    var rows = [];

    $(document).on('click', '.copy-select', function () {
        var alertType = applicationName;
        if ($(this).is(':checked')) {
            var selectedRowIndex = $(this).closest('tr').index();
            if (isAbstractViewOrCaseView(selectedRowIndex)) {
                selectedRowIndex = selectedRowIndex / 2
            }
            caseJsonArrayInfo.push(populateCommentDataFromGrid(selectedRowIndex,alertType));
        } else {
            var unselectedCheckboxId = $(this).attr("data-id");
            caseJsonArrayInfo.forEach(function (caseInfo, index) {
                if (caseInfo.id == unselectedCheckboxId) {
                    caseJsonArrayInfo.splice(index, 1);
                }
            })
        }
    });
    var isAbstractViewOrCaseView = function (selectedRowIndex) {
        if (typeof isCaseDetailView != 'undefined' && isCaseDetailView == 'true' && selectedRowIndex != 0) {
            return true
        } else if ($('#detailed-view-checkbox').length > 0 && $('#detailed-view-checkbox').prop("checked") == true && selectedRowIndex != 0) {
            return true
        }
        return false

    };

    $(document).on('click', 'input#select-all', function () {
        var alertType = applicationName;
        $(".copy-select").prop('checked', this.checked);
        $(".alert-select-all").prop('checked', this.checked);
        if (typeof isCaseDetailView !== "undefined" && isCaseDetailView == "true") {
            checkboxSelector = 'table#alertsDetailsTable .copy-select';
        } else {
            checkboxSelector = '.copy-select';
        }
        $.each($(checkboxSelector), function () {
            if($(this).is(':checked')){
                var selectedRowIndex = $(this).closest('tr').index();
                if (isAbstractViewOrCaseView(selectedRowIndex)) {
                    selectedRowIndex = selectedRowIndex / 2
                }
                if(selectedCases.indexOf($(this).attr("data-id")) == -1) {
                    caseJsonArrayInfo.push(populateCommentDataFromGrid(selectedRowIndex, alertType));
                }
            }
            else {
                caseJsonArrayInfo.splice($.inArray($(this).attr("data-id"), caseJsonArrayInfo), 1);
            }
        })
    });


    var applyBusinessRules = function (row, data) {
        //Apply the eudra rules.
        if (data.format) {
            var formats = JSON.parse(data.format);
            $.each(formats, function (indx, format) {
                var obj = format;
                var textObj;
                $.each(obj.text.tc, function (index, data) {
                    textObj = $(row).find('.' + data);
                    textObj.css('color', obj.text.color);
                    if (obj.text.bold) {
                        textObj.css('font-weight', '900');
                    }
                    if (obj.text.italic) {
                        textObj.css('font-style', 'italic');
                    }
                    if (obj.text.underline) {
                        textObj.css('text-decoration', 'underline');
                    }
                });
                $.each(obj.cell.tc, function (index, data) {
                    $(row).find('.' + data).parents('td').css('background-color', obj.cell.color);
                });
            });
        }
    };

    var enableMenuTooltips = function () {
        $(".grid-menu-tooltip").on("mouseover", function () {
            var $this = $(this);
            var tooltipText = $this.attr("data-title");
            $this.tooltip({
                title: tooltipText,
                placement: "auto"
            });
            $this.tooltip('show');
        });
    };

    var enableMenuTooltipsDynamicWidth = function () {
        $(".grid-menu-tooltip-dynamicWidth").on("mouseover", function () {
            var $this = $(this);
            var tooltipText = $this.attr("data-title");
            $this.tooltip({
                title: tooltipText,
                placement: "auto",
                template: '<div class="tooltip" style="width:max-content !important;min-width:50px !important"><div class="tooltip-arrow"></div><div class="tooltip-inner"></div></div>'
            });
            $this.tooltip('show');
        });
    };


    var disableTooltips = function () {
        $(".mdi-alpha-d-box, .mdi-filter-outline, .mdi-trending-up, .mdi-content-save, .mdi-export, .mdi-settings, mdi-plus-box, mdi-chart-bar").on('click', function () {
            $(".tooltip").hide();
        });
    };

    var sortIconHandler = function (viewSortIndex=null) {
        var thArray = $('#alertsDetailsTable').DataTable().columns().header();
        if (isViewInstance) {
            var columnName;
            var indexOfColumn = 0;
            var  sorting;
            var sortIndex;
            var columnName;
            if ($('#sortedColumn').val()) {
                sorting = JSON.parse($('#sortedColumn').val());
                sortIndex = parseInt(Object.keys(sorting)[0]);
                columnName = $('#alertsDetailsTable').find("th").eq(sortIndex).attr("data-field");
            }
            if($(this).hasClass("dt-ordering-asc"))
            {
            sorting="desc"
            }else if($(this).hasClass("dt-ordering-desc"))
            {
            sorting="asc"
            }else{
            sorting="asc"
            }
            $(".export-report").each(function (e) {
                var currentUrl = $(this).attr("href");
                currentUrl = typeof currentUrl !== 'undefined' ? updateQueryStringParameter(currentUrl, 'column', columnName) : "";
                currentUrl = typeof currentUrl !== 'undefined' ? updateQueryStringParameter(currentUrl, 'sorting', sorting) : "";
                $(this).attr("href", currentUrl);
            })
            columnSeq.every(function () {
                var seq = columnSeq[indexOfColumn].seq;
                if (index == seq) {
                    columnName = columnSeq[indexOfColumn].name;
                    return false;
                }
                indexOfColumn++;
                return true;
            });

            var columnIndex
            if(viewSortIndex!=null)
            {
                columnIndex=viewSortIndex
            }else {
                columnIndex = $('#alertsDetailsTable').find("th[data-field='" + columnName + "']").attr('data-column-index');
            }

        } else {
            columnIndex = index;
        }
    };
 function updateQueryStringParameter(uri, key, value) {
        var re = new RegExp("([?&])" + key + "=.*?(&|$)", "i");
        var separator = uri.indexOf('?') !== -1 ? "&" : "?";
        if (uri.match(re)) {
            return uri.replace(re, '$1' + key + "=" + value + '$2');
        }
        else {
            return uri + separator + key + "=" + value;
        }
    }
    /**
     * The reset modal screen function which resets the password div in the workflow modal screen.
     * This is internal function to be called internally in this object scope.
     */
    var resetModalScreen = function () {
        //Clean up the the dispositions fields.
        $('#edit-state-modal').find('#extra-value-select').html("<option></option>");
        //Hide the password
        $('#edit-state-modal').find('#passwordDiv').addClass('hide');
        $('#edit-state-modal').find('#isPasswordEnabled').val(0);
    }

    var showPasswordField = function () {
        $('#edit-state-modal').find('#passwordDiv').removeClass('hide');
        $('#edit-state-modal').find('#isPasswordEnabled').val(1);
    }

    var changeWorkflowEditScreen = function (availableValues, targetVal) {

        var availableValObj = _.findWhere(availableValues, {value: targetVal});

        if (typeof availableValObj != 'undefined' && availableValObj) {
            if (availableValObj.approvalRequired) {
                showPasswordField();
            } else {
                resetModalScreen();
            }

            //Clean up the the dispositions fields
            $('#edit-state-modal').find('#extra-value-select').html('')

            //Fill up the dispositions in the combo
            $('#edit-state-modal').find('#extra-value-select').html(getDispositionOptions(availableValObj))
        }
    }

    var getDispositionOptions = function (availableValObj) {
        var dispositionStr = "<option></option>";
        _.each(availableValObj.dispositions, function (disposition) {
            dispositionStr = dispositionStr +
                "<option value=\"" + disposition.value + "\">" + encodeToHTML(disposition.displayName) + "</option>"
        })
        return dispositionStr
    }

    var authenticateUser = function () {
        var returnVal = false
        var passwordJson = {
            "password": $('#edit-state-modal').find('#passwordAuthentication').val()
        }

        var authUrl = '/signal/user/authenticate'

        $.ajax({
            url: authUrl,
            data: passwordJson,
            async: false
        })
            .done(function (result) {
                if (result.authorized) {
                    returnVal = true
                } else {
                    returnVal = false
                }
            })
            .fail(function () {
                returnVal = false
            });
        return returnVal
    }

    var toggleErrorNotification = function (action) {
        if (action == 'hide') {
            $('#edit-state-modal .errorNotification').addClass('hide');
            $('#edit-state-modal').find('.errorMessage').html('');
        } else {
            $('#edit-state-modal .errorNotification').removeClass('hide')
            $('#edit-state-modal').find('.errorMessage').html(' Authentication Failed!');
        }
    };

    var openCaseHistoryModal = function () {

        //Bind the click event on the case history icon.
        $(document).on('click', '.case-history-icon', function (event) {
            event.preventDefault();
            var parent_row = $(event.target).closest('tr');
            var selectedRowIndex = $(this).closest('tr').index();
            if (isAbstractViewOrCaseView(selectedRowIndex)) {
                selectedRowIndex = selectedRowIndex / 2
            }
            var isSignal = $(this).data('signal');

            var rowObject = {};
            if (!isSignal) {
                rowObject = table.rows(selectedRowIndex).data()[0];
            } else {
                rowObject = tableSingleReview.row($(this).parents('tr')).data();
            }
            var isVaers = $('#isVaers').val() == "true" ? true: false;
            var isVigibase = $('#isVigibase').val() == "true" ? true: false;
            var isFaers = $('#isFaers').val() == "true" ? true: false;
            var caseNumber = rowObject.actualCaseNumber;
            var productFamily = rowObject.actualProductName;
            var caseVersion = rowObject.caseVersion;
            var productName = rowObject.actualProductName;
            var pt = rowObject.primaryEvent;
            var alertConfigId = rowObject.alertConfigId;

            var caseHistoryModal = $('#caseHistoryModal');
            caseHistoryModal.find("#caseNumber").html(rowObject.caseNumber);
            caseHistoryModal.find("#productFamily").html(rowObject.productName);
            caseHistoryModal.find("#caseVersion").val(caseVersion);
            caseHistoryModal.find("#productName").val(rowObject.productName);
            caseHistoryModal.find("#alertConfigId").val(alertConfigId);
            caseHistoryModal.find("#actualCaseNumber").val(caseNumber);
            caseHistoryModal.find("#actualProductFamily").val(productFamily);
            caseHistoryModal.find("#pt").val(pt);
            caseHistoryModal.modal('show');

            if(typeof singleCaseUpdateJustificationUrl != 'undefined')
                updateJustificationUrl = singleCaseUpdateJustificationUrl;

            signal.caseHistoryTable.init_case_history_table(caseHistoryUrl, isArchived);
            signal.caseHistoryTable.init_case_history_table_suspect(caseHistorySuspectUrl);
        });
    };

    var initiateCommentHistoryTable = function (commentModal) {
        return $('#commentHistoryTable').DataTable({
            responsive: true,
            language: {
                "search": "",
                "lengthMenu": 'Show _MENU_'
            },
            "ajax": {
                "url": "/signal/alertComment/listAggCommentsHistory",
                "dataSrc": ""
            },
            drawCallback: function (){
                colEllipsisModal();
                var rowsDataAction = $('#commentHistoryTable').DataTable().rows().data();
                pageDictionaryForFive($('#commentHistoryTable_wrapper'),rowsDataAction.length);
            },
            "aaSorting": [[3, "desc"]],
            "bLengthChange": true,
            "iDisplayLength": 5,
            "aLengthMenu": [[5, 10, 100, 200, -1], [5, 10, 100, 200, "All"]],
            "aoColumns": [
                {
                    "mData": 'period',
                    "className":"period-width-1",
                    "mRender": function (data, type, row) {
                        if (row.period == null) {
                            return "-"
                        } else {
                            return row.period
                        }

                    }
                },
                {
                    "mData": 'comments',
                    "className": 'col-min-150 col-max-600 col-md-7',
                    "mRender": function (data, type, row) {
                        var colElement = '<div class="col-container small-height-50 row"><div class="col-height col-md-11 commentDisplay" style="max-width: 100%; word-break: break-word;">';
                        colElement +=  encodeToHTML(row.comments);
                        colElement += '</div><div class="col-md-1"><div>';
                        colElement += (row.comments!==""&&row.comments!==null) ? "<a title='Copy' class='ico-clone'><i class='fa fa-clone fa-lg'></i></a></div><div>" : "</div><div>";
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots-modal view-all-modal" more-data="' + encodeToHTML(escapeHTML(row.comments)) + '"><i class="fa fa-ellipsis-h fa-lg"></i></a>';
                        colElement += '</div></div></div>';
                        return colElement
                    },
                    "visible": true
                },
                {
                    "mData": 'modifiedBy',
                    "className": "period-width-1"
                },
                {
                    "mData": 'lastUpdated',
                    "className": "period-width-1",
                    "mRender": function (data, type, row) {
                        return moment.utc(row.lastUpdated).format('DD-MMM-YYYY hh:mm:ss A');
                    }
                }
            ]
        })
    }

    var openAlertCommentModal = function (alertType, applicationName) {

        //Modal object
        var commentModal = $('#commentModal');
        $(document).on('click', '.comment-icon', function (event) {
            var $this = this;
            var dataInfo;
            var isCaseDetail;
            var appType;
            var validatedSignalId;
            var commentAlertType = $($this).data('name');
            var isSignal = $($this).data('signal');

            if (commentAlertType !== undefined) {
                alertType = commentAlertType;
            }
            event.preventDefault();
            var caseJsonArray = [];
            dataInfo = $(event.target).attr('data-info');
            isCaseDetail = $(event.target).attr('data-comment');

            appType = $("#application").html();
            if (appType === '') {
                appType = commentModal.find("#application").html();
                validatedSignalId = commentModal.find("#validatedSignalId").html()
            }
            commentModal.find('.dt-input').val("").keyup();

            var selectedRowCount = caseJsonArrayInfo.length;
            if (selectedRowCount > 1 && $($this).closest('tr').find(".copy-select").prop("checked")) {
                var textToDisplay;
                switch (applicationName) {
                    case 'Single Case Alert':
                        textToDisplay = 'Case';
                        break;
                    case 'Aggregate Case Alert':
                        textToDisplay = 'PEC';
                        break;
                    case 'EVDAS Alert':
                        textToDisplay = 'PEC';
                        break;
                    case 'Literature Search Alert':
                        textToDisplay = 'Article';
                        break;
                }
                //@TODO create ajax call to check if we can show bulk edit option

                if(applicationName==='Aggregate Case Alert'){
                    var data = createCaseJsonArray(data, commentModal, alertType, isCaseDetail, isSignal);
                    $.ajax({
                        type: "POST",
                        url: "/signal/alertComment/getBulkCheck",
                        data: data,
                    })
                        .done(function (result) {
                            if(result.data){
                                $(commentModal).find('div.bulkOptionsSection').show();
                                $(commentModal).find('div.bulkOptionsSection span.alertTypeText').html(textToDisplay);
                                $(commentModal).find('div.bulkOptionsSection span.count').html(selectedRowCount);
                                $('#allSelected').attr("disabled",true).parent().css({cursor:"not-allowed"});
                            }else{
                                $(commentModal).find('div.bulkOptionsSection').show();
                                $(commentModal).find('div.bulkOptionsSection span.alertTypeText').html(textToDisplay);
                                $(commentModal).find('div.bulkOptionsSection span.count').html(selectedRowCount);
                                $('#allSelected').attr("disabled",false).parent().css({cursor:"default"});
                            }
                        });
                }else{
                    $(commentModal).find('div.bulkOptionsSection').show();
                    $(commentModal).find('div.bulkOptionsSection span.alertTypeText').html(textToDisplay);
                    $(commentModal).find('div.bulkOptionsSection span.count').html(selectedRowCount);
                    $('#allSelected').attr("disabled",false).parent().css({cursor:"default"});
                }
                $('#commentTemplateList').show();
            } else {
                $('#commentTemplateList').show();
                $(commentModal).find('div.bulkOptionsSection').hide();
            }

            $('div.bulkOptionsSection input[name=bulkOptions]').off().on('change', function () {
                switch ($(this).val()) {
                    case 'allSelected':
                        caseJsonArray = [];
                        if($('#commentTemplateSelect').val()&&$('#commentTemplateSelect').val()!="none"){
                            $('#commentbox').val('');
                        }
                        $('#commentTemplateList').hide();
                        initiateSingleBulkRowCommentProcess(commentModal, alertType, isCaseDetail, isSignal);
                        caseJsonArray = caseJsonArrayInfo;
                        bindAddComments(commentModal, caseJsonArray, "bulk", alertType, validatedSignalId, isCaseDetail, $this, applicationName);
                        break;
                    case 'current':
                        caseJsonArray = [];
                        $('#commentTemplateList').show();
                        initiateSingleRowCommentProcess($this, caseJsonArray, commentModal, alertType, isCaseDetail, isSignal);
                        bindAddComments(commentModal, caseJsonArray, "row", alertType, validatedSignalId, isCaseDetail, $this, applicationName);
                        break;
                }
            });

            $('#allSelected').click(function() {
                $('#commentbox').val('');
            });
            if (dataInfo === "row") {
                initiateSingleRowCommentProcess($this, caseJsonArray, commentModal, alertType, isCaseDetail, isSignal);
            }

            // clear history data once before open popup as we have different ways to close it:
            // "Close" btn, x icon, Esc (is not processed by code)
            $('#commentHistoryTable').DataTable().clear().draw();

            commentModal.modal('show');
            $('div.bulkOptionsSection input[name=bulkOptions]').on('change', function () {
                if(commentModal.find('.add-comments').html() !== 'Update' && commentModal.find('#commentbox').val().length == 0 && commentModal.find('#commentbox').val() == "" && commentModal.find("#commentId").val() == "") {
                    commentModal.find('.add-comments').prop("disabled", true);
                }
            });
            bindAddComments(commentModal, caseJsonArray, dataInfo, alertType, validatedSignalId, isCaseDetail, $this, applicationName)
        })
    };
    var bindAddComments = function (commentModal, caseJsonArray, dataInfo, alertType, validatedSignalId, isCaseDetail, currentRow, applicationName) {
        commentModal.find(".add-comments").off().on('click', function () {
            commentModal.find('.add-comments').prop("disabled", true);
            var caseJson;
            var caseJsonObj;
            var data;
            var url;
            var $this = this;
            var caseJsonObjArray = [];
            var isFaers = $('#isFaers').val() === "true";
            var isVaers = $('#isVaers').val() === "true";
            var isVigibase = $('#isVigibase').val() === "true";
            var isNewComment = false;
            var isExistingComment = false;
            var commentsValue = commentModal.find("#commentbox").val();
            var alertTable = getPECsTable(applicationName);
            var updatedPECsIndexesArray = [];
            if (dataInfo === "row") {
                caseJson = caseJsonArray[0];

                if (commentModal.find("#commentId").val()) {
                    data = {
                        "alertType": alertType,
                        "comment": commentsValue,
                        "id": commentModal.find("#commentId").val(),
                        "validatedSignalId": validatedSignalId,
                        "topicId": $("#topicId").html(),
                        "executedConfigId": caseJson.executedConfigId,
                        "adhocAlertId": caseJson.adhocAlertId,
                        "configId": caseJson.configId,
                        "literatureAlertId": caseJson.literatureAlertId,
                        "alertName": caseJson.alertName,
                        "caseNumber": caseJson.caseNumber,
                        "caseId": caseJson.caseId,
                        "caseVersion": caseJson.caseVersion,
                        "followUpNumber": caseJson.followUpNumber,
                        "isFaers": isFaers,
                        "isVaers": isVaers,
                        "isVigibase": isVigibase,
                        "commentTemplateId": commentModal.find("#commentTemplateSelect").val(),
                        "dtIndex": caseJson.dtIndex
                    };
                    if (data.comment === "") {
                        url = "/signal/alertComment/deleteComment";
                    } else {
                        url = "/signal/alertComment/updateComment";
                    }
                } else {
                    url = "/signal/alertComment/saveComment";

                    caseJsonObj = {
                        "alertType": alertType,
                        "productName": caseJson.productName,
                        "eventName": caseJson.eventName,
                        "pt": caseJson.pt,
                        "comments": commentsValue,
                        "caseNumber": caseJson.caseNumber,
                        "productFamily": caseJson.productFamily,
                        "validatedSignalId": validatedSignalId,
                        "topicId": commentModal.find("#topicId").html(),
                        "assignedTo": caseJson.assignedTo,
                        "adhocAlertId": caseJson.adhocAlertId,
                        "executedConfigId": caseJson.executedConfigId,
                        "productId": caseJson.productId,
                        "ptCode": caseJson.ptCode,
                        "configId": caseJson.configId,
                        "literatureAlertId": caseJson.literatureAlertId,
                        "articleId": caseJson.articleId,
                        "alertName": caseJson.alertName,
                        "caseId": caseJson.caseId,
                        "caseVersion": caseJson.caseVersion,
                        "followUpNumber": caseJson.followUpNumber,
                        "isFaers": isFaers,
                        "isVaers": isVaers,
                        "commentTemplateId": commentModal.find("#commentTemplateSelect").val(),
                        "isVigibase": isVigibase,
                        "dtIndex": caseJson.dtIndex
                    };
                    caseJsonObjArray.push(caseJsonObj);
                    data = {
                        caseJsonObjArray: JSON.stringify(caseJsonObjArray)
                    }
                }
            } else {
                if (commentsValue === "" && alertType !== ALERT_CONFIG_TYPE.SINGLE_CASE_ALERT) {
                    // Bulk deletion added for Aggregate, Evdas, Literature and Adhoc
                    url = "/signal/alertComment/deleteBulkComments";
                } else {
                    url = "/signal/alertComment/saveComment";
                }
                var selectedRowCount = caseJsonArrayInfo.length;
                for (var index = 0; index < selectedRowCount; index++) {
                    validatedSignalId = $("#validatedSignalId").html();
                    caseJson = caseJsonArray[index];
                    var prevComments = caseJson.comments;
                    if (!(commentsValue === '' && !prevComments)) {
                        caseJsonObj = {
                            "alertType": alertType,
                            "productName": caseJson.productName,
                            "eventName": caseJson.eventName,
                            "pt": caseJson.pt,
                            "comments": commentsValue,
                            "commentId": caseJson.commentId,
                            "dtIndex": caseJson.dtIndex,
                            "caseNumber": caseJson.caseNumber,
                            "productFamily": caseJson.productFamily,
                            "validatedSignalId": validatedSignalId,
                            "topicId": commentModal.find("#topicId").html(),
                            "assignedTo": caseJson.assignedTo,
                            "adhocAlertId": caseJson.adhocAlertId,
                            "productId": caseJson.productId,
                            "ptCode": caseJson.ptCode,
                            "configId": caseJson.configId,
                            "literatureAlertId": caseJson.literatureAlertId,
                            "articleId": caseJson.articleId,
                            "executedConfigId": caseJson.executedConfigId,
                            "alertName": caseJson.alertName,
                            "caseId": caseJson.caseId,
                            "caseVersion": caseJson.caseVersion,
                            "followUpNumber": caseJson.followUpNumber,
                            "isFaers": isFaers,
                            "isVaers": isVaers,
                            "isVigibase": isVigibase
                        };

                        caseJsonObjArray.push(caseJsonObj);
                        updatedPECsIndexesArray.push(caseJson.dtIndex);

                        // for toast message definition
                        if (prevComments !== undefined && prevComments !== null && prevComments) {
                            isExistingComment = true
                        } else {
                            isNewComment = true
                        }
                    }
                }
                data = {
                    caseJsonObjArray: JSON.stringify(caseJsonObjArray)
                }
            }

            //Save comment call
            $.ajax({
                url: url,
                data: data,
                type: "POST",
                async: true,
                beforeSend: function () {
                    commentModal.find("i.isProcessing").show();
                },
            })
                .done(function (result) {
                    commentModal.find("i.isProcessing").hide();
                    //Populate the comments again if single comment is added.
                    if (typeof isCaseDetail !== 'undefined') {
                        window.location.reload();
                        return;
                    }
                    var isBulk = false;
                    if (dataInfo === "row") {
                        if (alertType === ALERT_CONFIG_TYPE.SINGLE_CASE_ALERT) {
                            alertTable[caseJson.dtIndex].comments = commentsValue === "" ? null : commentsValue;
                        } else {
                            alertTable[caseJson.dtIndex].comment = commentsValue === "" ? null : commentsValue;
                        }
                        signal.alertComments.populate_comments(commentModal, caseJson);
                        $('#commentbox').val('').trigger("blur");
                        if (data.comment !== "") {
                            showCommentIcon(currentRow) ;
                        }
                        else {
                            removeCommentIcon(currentRow) ;
                        }

                    } else {
                        isBulk = true;
                        if (alertType === ALERT_CONFIG_TYPE.SINGLE_CASE_ALERT) {
                            $.each(updatedPECsIndexesArray, function (index, value) {
                                alertTable[value].comments = commentsValue === "" ? null : commentsValue;
                            });
                        } else {
                            $.each(updatedPECsIndexesArray, function (index, value) {
                                alertTable[value].comment = commentsValue === "" ? null : commentsValue;
                            });
                        }
                        commentModal.modal("hide");
                        $('#commentTemplateSelect').prop('selectedIndex', 0);
                        $('body').removeClass('modal-open');
                        $('.modal-backdrop').remove();
                        var checkboxSelector;
                        if (typeof isCaseDetailView !== "undefined" && isCaseDetailView === "true") {
                            checkboxSelector = 'table#alertsDetailsTable .copy-select:checked';
                        } else {
                            checkboxSelector = '.copy-select:checked';
                        }
                        if (result.success) {
                            $.each($(checkboxSelector), function () {
                                showCommentIcon(this);
                            });
                        }
                        if (caseJsonObj.comments === "") {
                            if(result.success){
                                $.each($(checkboxSelector), function () {
                                    removeCommentIcon(this);
                                });
                            }
                        }
                    }
                    if(result.success === true && result.dtIndexIdMap) {
                        if ((alertType === ALERT_CONFIG_TYPE.AGGREGATE_CASE_ALERT || alertType === ALERT_CONFIG_TYPE.SINGLE_CASE_ALERT || alertType === ALERT_CONFIG_TYPE.EVDAS_ALERT || alertType === ALERT_CONFIG_TYPE.LITERATURE_SEARCH_ALERT || alertType === ALERT_CONFIG_TYPE.SINGLE_CASE_ALERT_ON_DEMAND || alertType === ALERT_CONFIG_TYPE.AGGREGATE_CASE_ALERT_ON_DEMAND) && !$($this).data('signal')) {
                            for (const [key, value] of Object.entries(result.dtIndexIdMap)) {
                                table.rows(key).data()[0].commentId = value
                            }
                        } else if (alertType === 'Aggregate Case Alert') {
                            for (const [key, value] of Object.entries(result.dtIndexIdMap)) {
                                tableAggReview.rows(key).data()[0].commentId = value
                            }
                        } else if (alertType !== 'Ad-Hoc Alert') {
                            for (const [key, value] of Object.entries(result.dtIndexIdMap)) {
                                tableSingleReview.rows(key).data()[0].commentId = value
                            }
                        }
                        caseJsonArrayInfo.forEach(function(map){
                            map.commentId=result.dtIndexIdMap[map.dtIndex];
                        })
                    }
                    // For Aggregate Case alert field name is comment
                    if(result.success === true && (['Single Case Alert','Aggregate Case Alert', 'EVDAS Alert', 'Literature Search Alert'].includes(applicationName) && (signal.fieldManagement.visibleColumns('comments') || signal.fieldManagement.visibleColumns('comment')))){
                        $('#alertsDetailsTable').DataTable().ajax.reload();
                    }
                    // checkbox should be removed and info arrays should be cleared after bulk change
                    caseJsonArrayInfo = [];
                    selectedCases = [];
                    selectedCasesInfo = [];
                    selectedCasesInfoSpotfire = [];
                    $(".copy-select").prop('checked', false);
                    prev_page = [];
                    // alertIdSet.clear();
                    $("input#select-all").prop('checked', false);
                    prev_page = [];
                    if (applicationName !== 'EVDAS Alert' && applicationName !== 'Ad-Hoc Alert'){
                        alertIdSet.clear();
                    }
                    const isDeleteUrl = url.split("/");
                    isDeleteUrl.reverse();
                    if (isDeleteUrl[0] === "deleteComment") {
                        commentModal.find('.add-comments').html("Add");
                    }
                    if (result.success && result.message === "Comment(s) updated successfully." && commentModal.find('.add-comments').html() === 'Update') {
                        $.Notification.notify('success', 'top right', "Success", "Comment(s) updated successfully.", {autoHideDelay: 5000});
                    } else if (result.success && isBulk === false && data.comment === "") {
                        $.Notification.notify('success', 'top right', "Success", "Comment(s) removed successfully.", {autoHideDelay: 5000});
                    } else if (result.success && isBulk === true && caseJsonObj.comments === "") {
                        $.Notification.notify('success', 'top right', "Success", "Comment(s) removed successfully.", {autoHideDelay: 5000});
                    } else if (result.success && isBulk === true && caseJsonObj.comments !== "") {
                        if (isNewComment && isExistingComment) {
                            $.Notification.notify('success', 'top right', "Success", "Comment(s) added/updated successfully.", {autoHideDelay: 5000});
                        } else if (isNewComment && !isExistingComment) {
                            $.Notification.notify('success', 'top right', "Success", "Comment(s) added successfully.", {autoHideDelay: 5000});
                        } else if (!isNewComment && isExistingComment) {
                            $.Notification.notify('success', 'top right', "Success", "Comment(s) updated successfully.", {autoHideDelay: 5000});
                        }
                    } else if (result.success) {
                        $.Notification.notify('success', 'top right', "Success", "Comment(s) added successfully.", {autoHideDelay: 5000});
                    } else {
                        $.Notification.notify('error', 'top right', "Error", "An Error occurred while Processing request.", {autoHideDelay: 5000});
                    }
                })
                .fail(function () {});
        })
    };

    var initiateSingleRowCommentProcess = function ($this, caseJsonArray, commentModal, alertType, isCaseDetail, isSignal) {
        var adhocAlertId;
        var literatureAlertId;
        var articleId;
        var caseNumber;
        var productName;
        var pt;
        var rowObject = {};
        if ((alertType === ALERT_CONFIG_TYPE.AGGREGATE_CASE_ALERT || alertType === ALERT_CONFIG_TYPE.SINGLE_CASE_ALERT || alertType === ALERT_CONFIG_TYPE.EVDAS_ALERT || alertType === ALERT_CONFIG_TYPE.LITERATURE_SEARCH_ALERT ||  alertType === ALERT_CONFIG_TYPE.SINGLE_CASE_ALERT_ON_DEMAND || alertType === ALERT_CONFIG_TYPE.AGGREGATE_CASE_ALERT_ON_DEMAND) && !isSignal) {
            var selectedRowIndex = $($this).closest('tr').index();
            if (isAbstractViewOrCaseView(selectedRowIndex)) {
                selectedRowIndex = selectedRowIndex / 2
            }
            rowObject = table.row(selectedRowIndex).data();
        } else if (alertType === 'Aggregate Case Alert' || alertType === 'EVDAS Alert') {
            rowObject = tableAggReview.row($($this).parents('tr')).data();
        } else if (alertType === 'Literature Search Alert') {
            rowObject = tableLiteratureReview.row($($this).parents('tr')).data();
        } else {
            rowObject = tableSingleReview.row($($this).parents('tr')).data();
        }

        if(alertType == ALERT_CONFIG_TYPE.SINGLE_CASE_ALERT){
            caseNumber = rowObject.actualCaseNumber;
            productName = rowObject.actualProductName;
            pt = rowObject.actualPt;
        } else{
            caseNumber = rowObject.caseNumber;
            productName = rowObject.productName;
            pt = rowObject.pt;
        }

        var productFamily = rowObject.productFamily;
        var caseVersion = rowObject.caseVersion;
        var eventName = rowObject.preferredTerm;
        var assignedTo = rowObject.assignedTo?.id;
        var assignedToUser = null;
        if(rowObject.assignedToUser != undefined) {
            assignedToUser = rowObject.assignedToUser.id;
        }
        if(alertType === 'EVDAS Alert'){
            var productId = rowObject.substanceId;
        }
        else{
            var productId = rowObject.productId;
        }
        var ptCode = rowObject.ptCode;
        var configId = rowObject.alertConfigId;
        var executedConfigId = rowObject.execConfigId;
        var alertName = rowObject.alertName;
        var caseId = rowObject.caseId? rowObject.caseId: rowObject.id;
        var caseVersion = rowObject.caseVersion;
        var followUpNumber = rowObject.followUpNumber;
        var commentTemplateId = commentModal.find("#commentTemplateSelect").val();
        var commentId = rowObject.commentId;
        var dtIndex = $($this).closest('tr').index();

        if (alertType === signalAlertType.ADHOC_ALERT) {
            adhocAlertId = rowObject.id;
        } else if (alertType === signalAlertType.LITERATURE_SEARCH_ALERT) {
            literatureAlertId = rowObject.id;
            articleId = rowObject.articleId
        }
        var caseVisibility = rowObject.caseNumber;
        var caseJson = {
            "alertType": alertType,
            "productFamily": productFamily,
            "caseNumber": caseNumber,
            "productName": productName,
            "eventName": eventName,
            "pt": pt,
            "ptCode": ptCode,
            "productId": productId,
            "assignedTo": assignedTo ? assignedTo : assignedToUser,
            "executedConfigId": executedConfigId,
            "configId": configId,
            "adhocAlertId": adhocAlertId,
            "literatureAlertId": literatureAlertId,
            "articleId": articleId,
            "alertName": alertName,
            "caseId": caseId,
            "caseVersion": caseVersion,
            "followUpNumber": followUpNumber,
            "isFaers": $('#isFaers').val() === "true",
            "isVaers": $('#isVaers').val() === "true",
            "commentTemplateId": commentTemplateId,
            "commentId": commentId,
            "dtIndex": dtIndex,
            "isVigibase": $('#isVigibase').val() === "true",
            "caseVisibility" : caseVisibility
        };

        caseJsonArray.push(caseJson);
        var commentMetaInfo = "";
        var safetyProductName = $('#safetyProductName').val()
        if (caseJson.alertType === signalAlertType.AGGREGATE_ALERT || caseJson.alertType === signalAlertType.EVDAS_ALERT) {
            if (typeof (caseJson.eventName) === "undefined" || typeof (caseJson.productName) === "undefined") {
                commentMetaInfo = ""
            } else {
                commentMetaInfo = '<span id="productName">' + caseJson.productName + '</span> - <span id="eventName">' + caseJson.eventName + '</span>' + '<span class="hidden" id="productId">' + caseJson.productId + '</span>' +
                    '<span class="hidden" id="ptCode">' + caseJson.ptCode + '</span>'
            }
        } else {
            if (typeof (caseJson.caseNumber) === "undefined" || typeof (caseJson.productFamily) === "undefined") {
                commentMetaInfo = ""
            } else {
                if($('#isVaers').val() == "true" || $('#isFaers').val() == "true" || $('#isVigibase').val() == "true" || $('#isCaseSeries').val() == "true"){
                    if(safetyProductName != "" && safetyProductName != undefined){
                        commentMetaInfo = '<span id="caseNumber">' + caseJson.caseVisibility + '</span> - <span id="productFamily">' + safetyProductName + '</span>'
                    } else{
                        commentMetaInfo = '<span id="caseNumber">' + caseJson.caseVisibility + '</span> - <span id="productFamily">' + caseJson.productName + '</span>'
                    }
                }else{
                    commentMetaInfo = '<span id="caseNumber">' + caseJson.caseVisibility + '</span>'
                }
            }
        }
        commentModal.find("#comment-meta-info").html(commentMetaInfo);
        commentModal.find('.comment-history-icon').show();
        commentModal.find("#assignedTo").html(assignedTo);
        commentModal.find("#configId").html(configId);
        commentModal.find("#application").html(caseJson.alertType);

        //Populate the existing comments and bind events to them in case of single comment.
        if (typeof isCaseDetail !== 'undefined') {
            $('#loadingComments').html('')
        } else {
            signal.alertComments.populate_comments(commentModal, caseJson);
        }
        $("#commentModal").on('shown.bs.modal', function(){
            $('#commentHistoryTable').DataTable().order( [[ 3, 'desc' ]] ).draw()
            colEllipsisModal();
        });
        $("#commentModal").on('hidden.bs.modal', function () {
            $('#commentbox').val('');
            $('.add-comments').html('Add');
            $(".createdBy").text('');
        })
    };

    var createCaseJsonArray = function(data, commentModal, alertType, isCaseDetail, isSignal) {
        var indexSet = new Set();
        var checkboxSelector;
        var caseJsonObjArray = [];
        if (typeof isCaseDetailView !== "undefined" && isCaseDetailView == "true") {
            checkboxSelector = 'table#alertsDetailsTable .copy-select:checked';
        } else {
            checkboxSelector = '.copy-select:checked';
        }
        $.each($(checkboxSelector), function () {
            var selectedRowIndex = $(this).closest('tr').index();
            if (isAbstractViewOrCaseView(selectedRowIndex))
                selectedRowIndex = selectedRowIndex / 2;
            indexSet.add((selectedRowIndex));
        });
        indexSet.forEach(function (index) {
            var adhocAlertId;
            var literatureAlertId;
            var articleId;
            var rowObject = {};
            if ((alertType === ALERT_CONFIG_TYPE.AGGREGATE_CASE_ALERT || alertType === ALERT_CONFIG_TYPE.SINGLE_CASE_ALERT || alertType === ALERT_CONFIG_TYPE.EVDAS_ALERT || alertType === ALERT_CONFIG_TYPE.LITERATURE_SEARCH_ALERT) && !isSignal) {
                rowObject = table.rows(index).data()[0];
            } else if (alertType === 'Aggregate Case Alert') {
                rowObject = tableAggReview.rows(index).data()[0];
            } else {
                rowObject = tableSingleReview.rows(index).data()[0];
            }


            var caseNumber = rowObject.caseNumber;
            var productFamily = rowObject.productFamily;
            var caseVersion = rowObject.caseVersion;
            var productName = rowObject.productName;
            var eventName = rowObject.preferredTerm;
            var pt = rowObject.pt;
            var assignedTo = rowObject.assignedTo.id;
            var productId = rowObject.productId;
            var ptCode = rowObject.ptCode;
            var configId = rowObject.alertConfigId;
            var executedConfigId = rowObject.execConfigId;
            var alertName = rowObject.alertName;
            var caseId = rowObject.caseId? rowObject.caseId: rowObject.id;
            var caseVersion = rowObject.caseVersion;
            var followUpNumber = rowObject.followUpNumber;
            var commentTemplateId = commentModal.find("#commentTemplateSelect").val();
            var commentId = rowObject.commentId;
            var dtIndex = index;

            if (alertType === signalAlertType.ADHOC_ALERT) {
                adhocAlertId = rowObject.id;
            } else if (alertType === signalAlertType.LITERATURE_SEARCH_ALERT) {
                literatureAlertId = rowObject.id;
                articleId = rowObject.articleId
            }
            var caseJson = {
                "alertType": alertType,
                "productFamily": productFamily,
                "caseNumber": caseNumber,
                "productName": productName,
                "eventName": eventName,
                "pt": pt,
                "ptCode": ptCode,
                "productId": productId,
                "assignedTo": assignedTo,
                "executedConfigId": executedConfigId,
                "configId": configId,
                "adhocAlertId": adhocAlertId,
                "literatureAlertId": literatureAlertId,
                "articleId": articleId,
                "alertName": alertName,
                "caseId": caseId,
                "caseVersion": caseVersion,
                "followUpNumber": followUpNumber,
                "isFaers": $('#isFaers').val() == "true" ? true: false,
                "isVaers": $('#isVaers').val() == "true" ? true: false,
                "commentTemplateId": commentTemplateId,
                "commentId": commentId,
                "dtIndex": dtIndex
            };
            caseJsonObjArray.push(caseJson)
        });
        data = {
            caseJsonObjArray: JSON.stringify(caseJsonObjArray)
        };
        return data;
    };

    var initiateSingleBulkRowCommentProcess = function (commentModal, alertType, isCaseDetail, isSignal) {

        //Set values to the modal elements.
        commentModal.find("#loadingComments").hide();
        commentModal.find("#comment-meta-info").html("");
        commentModal.find(".previous-comments").html("");
        commentModal.find("#commentId").val('');
        commentModal.find(".createdBy").text('');
        commentModal.find('.comment-history-icon').hide();
        if (commentModal.find('.add-comments').html() != 'Update') {
            commentModal.find('.add-comments').html("Add");
        }
        commentModal.find('#commentbox').on("keyup", function () {
            if (commentModal.find('#commentbox').val().length > 0) { //
                commentModal.find('.add-comments').prop("disabled", false);
            } else {
                if(commentModal.find('.add-comments').html() !== 'Update' && commentModal.find('#commentbox').val().length == 0 && commentModal.find('#commentbox').val() == "" && commentModal.find("#commentId").val() == "") {
                    commentModal.find('.add-comments').prop("disabled", true);
                }
            }
        });
    };

    var populateCommentDataFromGrid = function (index, alertType) {
        var adhocAlertId;
        var literatureAlertId;
        var articleId;
        var caseNumber;
        var productName;
        var pt;
        var rowObject = getPECsTable(alertType)[index];

        if (alertType === signalAlertType.ADHOC_ALERT) {
            adhocAlertId = rowObject.id;
        } else if (alertType === signalAlertType.LITERATURE_SEARCH_ALERT) {
            literatureAlertId = rowObject.id;
            articleId = rowObject.articleId
        }

        if(alertType === signalAlertType.SINGLE_CASE_ALERT){
          caseNumber = rowObject.actualCaseNumber;
          productName = rowObject.actualProductName;
          pt = rowObject.actualPt;
        } else{
            caseNumber = rowObject.caseNumber;
            productName = rowObject.productName;
            pt = rowObject.pt;
        }

        var caseJson = {
            "id": rowObject.id,
            "alertType": alertType,
            "productFamily": rowObject.productFamily,
            "caseNumber": caseNumber,
            "productName": productName,
            "eventName": rowObject.preferredTerm,
            "pt": pt,
            "ptCode": rowObject.ptCode,
            "productId": rowObject.productId,
            "assignedTo": rowObject.assignedTo?.id,
            "executedConfigId": rowObject.execConfigId,
            "configId": rowObject.alertConfigId,
            "adhocAlertId": adhocAlertId,
            "literatureAlertId": literatureAlertId,
            "articleId": articleId,
            "alertName": rowObject.alertName,
            "caseId": rowObject.caseId? rowObject.caseId: rowObject.id,
            "caseVersion": rowObject.caseVersion,
            "followUpNumber": rowObject.followUpNumber,
            "isFaers": $('#isFaers').val() === "true",
            "isVaers": $('#isVaers').val() === "true",
            "commentTemplateId": $('#commentModal').find("#commentTemplateSelect").val(),
            "commentId": rowObject.commentId,
            // for different alert types we have different properties "comment" or "comments"
            "comments": !rowObject.comments ? rowObject.comment : rowObject.comments,
            "dtIndex": index,
            "isVigibase": $('#isVigibase').val() === "true"
        };
        return caseJson;
    };

    var restartReview = function (caseReviewPreviousUrl) {

        //Event when review restart is clicked.
        $(document).on('click', '.case-restart-review', function (event) {
            event.preventDefault();
            var parent_row = $(event.target).closest('tr');
            var caseNumber = parent_row.find('span[data-field="caseNumber"]').attr("data-id");
            var followUpNumber = parent_row.find('span[data-field="followUpNumber"]').attr("data-id");
            var caseVersion = parent_row.find('span[data-field="caseVersion"]').attr("data-id");
            var productFamily = parent_row.find('input[data-field="productFamily"]').attr("data-id");
            var followUpModal = $('#followUpModal');
            followUpModal.modal('show');
            followUpModal.find(".previous-followUp").off().on('click', function () {
                var getPreviousState = caseReviewPreviousUrl + "?caseNumber=" + caseNumber +
                    "&caseVersion=" + caseVersion + "&productFamily=" + productFamily + "&followUpNumber=" + followUpNumber;
                $.ajax({
                    url: getPreviousState
                })
                    .done(function (result) {
                        parent_row.find('span[data-field="workflowState"]').text(result.previousState);
                        parent_row.find('span[data-field="disposition"]').text(result.previousDisposition);
                        parent_row.find('span[data-field="info-sign"]').addClass("hidden");
                        followUpModal.modal('destroy');
                    })
            })
        });
    };

    var openSimilarCasesModal = function (caseInfoUrl) {

        //Event triggered when similar cases count is clicked.
        $(document).on('click', '.similar-cases', function (event) {

            var similarCaseModal = $('#similarCaseModal');
            event.preventDefault();

            var parent_row = $(event.target).closest("tr");

            //Fetch the values of the event.
            var eventVal = $(event.target).attr('data-event-val');
            var eventType = $(event.target).attr('data-field');
            var executedConfigId = $(event.target).attr("data-id");
            var pt = $(event.target).attr("data-pt");
            var eventCode;
            if (eventType) {
                eventCode = (eventType.split("_code")[0]);
                eventType = eventType.split("_")[2];
            }

            var caseNumber = parent_row.find('span[data-field="caseNumber"]').attr("data-id");
            var caseVersion = parent_row.find('span[data-field="caseVersion"]').attr("data-id");

            //Show modal and set its values.
            similarCaseModal.modal('show');
            similarCaseModal.find("#eventType").html(eventType);
            similarCaseModal.find("#eventVal").html(eventVal);
            similarCaseModal.find("#caseNumberInfo").val(caseNumber);
            similarCaseModal.find("#executedConfigId").val(executedConfigId);
            similarCaseModal.find("#caseCurrentVersion").val(caseVersion);
            similarCaseModal.find("#eventCode").val(eventCode);
            similarCaseModal.find("#eventCodeVal").val(eventVal);
            //Make the modal table as datatable

            setTimeout(function () {
                signal.similarCaseTable.init_similar_case_table(caseInfoUrl)
            }, 100);
        });
    };

    var openAttachmentModal = function () {
        $(document).on('click', '.show-attachment-icon', function (event) {
            var $this = this;
            event.preventDefault();
            fetchAcceptedFileFormats();
            //var isArchived = $("#isArchived").val();
            var parent_row = $(event.target).closest('tr');
            var alertId = parent_row.find('a[data-field="attachment"]').attr("data-id");
            var caseController = parent_row.find('a[data-field="attachment"]').attr("data-controller");
            var url = "/signal/" + caseController + "/upload?isArchived="+isArchived;
            var getAttachmentUrl = "/signal/" + caseController + "/fetchAttachment?alertId=" + alertId + "&isArchived=" + isArchived;

            if (caseController === 'literatureAlert') {
                url += "&datasource=" + selectedDatasource;
                getAttachmentUrl += "&datasource=" + selectedDatasource;
            }
            $('#showAttachmentModal #attachmentForm #attachmentFormId').attr('value', alertId);
            var caseHistoryModal = $('#showAttachmentModal');
            caseHistoryModal.modal('show');
            function showErrorMessage(text) {
                var errorMsg = "<div class='alert alert-danger'>";
                if (text) {
                    errorMsg += text;
                }
                errorMsg += "</div>";
                setTimeout(function () {
                    $(".alert-danger").remove();
                }, 10000);

                return errorMsg;
            }

            function getSpecialChars(str) {
                const DEFAULT_CHARS = ['"', '<', '>', '!', '|', '/','\\', '#'];
                let specialChars = [];
                for (let i = 0; i < str.length; i++) {
                    if (DEFAULT_CHARS.includes(str[i]) && !specialChars.includes(str[i])) {
                        specialChars.push(str[i]);
                    }
                }
                return specialChars;
            }
            function handleDoubleQuotes(fileName) {
                var errorMsg;
                var specialChars = getSpecialChars(fileName);
                errorMsg = showErrorMessage(` ${specialChars.join(' , ')} special characters are not allowed in File Name `);
                $('#showAttachmentModal .modal-body').prepend(errorMsg);
            }
            $("#showAttachmentModal form#attachmentForm").off('submit').on('submit', function (e) {
                var formData = new FormData(this);
                $("#attachmentForm .upload").attr('disabled', true);
                var $this = $(this);
                if ($this.find('.attachment-file').val()) {
                    $this.find(".upload").attr("disabled", true);
                    clearFormInputsChangeFlag($this);
                    var fileName = $('.attachment-file').val().replace(/C:\\fakepath\\/i, '');
                    if (fileName.includes('"') || fileName.includes('<') || fileName.includes('>')|| fileName.includes('!')||
                        fileName.includes('|') || fileName.includes('/')|| fileName.includes('\\') || fileName.includes('#')) {
                        handleDoubleQuotes(fileName);
                        $(".clearFields").val("");
                        e.preventDefault();
                        return false;
                    }
                    $.ajax({
                        url: url,
                        type: "POST",
                        data: formData,
                        async: true,
                        beforeSend: function () {
                            document.body.style.cursor = 'wait';
                            $("#showAttachmentModal").css({
                                "pointer-events": "none",
                                "cursor": "not-allowed"
                            });
                        },
                        cache: false,
                        contentType: false,
                        processData: false
                    })
                        .done(function (data) {

                            if (data['success']) {
                                $this.find('.attachment-file').val('');
                                showAttachmentIcon(parent_row);
                                $('#showAttachmentModal #attachment-table').DataTable().ajax.reload();
                                $("#attachmentForm .upload").attr("disabled", true);
                                $('.attachment-input').val('');
                            } else {
                                $.Notification.notify('error', 'top right', "Failed", 'Something Went Wrong, Please Contact Your Administrator.', {autoHideDelay: 5000});
                            }
                        })
                        .always(function () {
                            document.body.style.cursor = 'default';
                            $("#showAttachmentModal").css("pointer-events", "");
                            $("#showAttachmentModal").css("cursor", "");
                        })
                        .fail(function () {
                            $("#attachmentForm .upload").attr('disabled', false);
                            $.Notification.notify('error', 'top right', "Error", "Sorry, This File Format Not Accepted", {autoHideDelay: 5000});
                        });
                }
                return false;
            });
            $('#showAttachmentModal #attachment-table').DataTable({
                layout: {
                    topStart: null,
                    topEnd: null,
                    bottomStart: ["pageLength", "info", {paging: {type: "full_numbers"}}],
                    bottomEnd: null,
                },
                customProcessing: true,
                destroy: true,
                searching: false,
                responsive: false,
                language: {
                    "search": "",
                    "lengthMenu": 'Show _MENU_ entries'
                },
                "ajax": {
                    "url": getAttachmentUrl,
                    "dataSrc": ""
                },
                drawCallback: function () {
                    var rowsDataAD = $('#attachment-table').DataTable().rows().data();
                    pageDictionary($('#attachment-table_wrapper'), rowsDataAD.length);
                    showTotalPage($('#attachment-table_wrapper'), rowsDataAD.length);
                    tagEllipsis($(table));
                    colEllipsis();
                    webUiPopInit();
                    closeInfoPopover();
                    showInfoPopover();
                    $('.remove-attachment').on('click', function (e) {
                        var attachmentRow = $(e.target).closest('tr');
                        var attachmentId = attachmentRow.find('span[data-field="removeAttachment"]').attr("data-attachmentId");
                        var removeUrl = '/signal/' + caseController + '/deleteAttachment?attachmentId=' + attachmentId + '&isArchived=' + isArchived;
                        if (caseController === 'literatureAlert') {
                            removeUrl += "&datasource=" + selectedDatasource;
                        }
                        $.ajax({
                            type: "POST",
                            url: removeUrl,
                            data: {alertId: alertId},
                            beforeSend: function () {
                                $('.remove-attachment').css('pointer-events', 'none'); // Disables the span element
                                $('.remove-attachment').css('opacity', '0.5');
                            },
                        })
                            .done(function (result) {
                                if(result.success) {
                                    if ($('.remove-attachment').length === 1) {
                                        removeAttachmentIcon($this)
                                    }
                                    $.Notification.notify('success', 'top right', "Success", "Attachment deleted successfully.", {autoHideDelay: 5000});
                                    $('#showAttachmentModal #attachment-table').DataTable().ajax.reload();
                                    setTimeout(function () {
                                        $('.remove-attachment').css('pointer-events', 'auto'); // Enables the span element
                                        $('.remove-attachment').css('opacity', '1');
                                    }, 10000);
                                }
                            })
                            .fail(function () {
                                $.Notification.notify('error', 'top right', "Error", "Attachment could not be deleted. Please try again after some time.", {autoHideDelay: 5000});
                                setTimeout(function () {
                                    $('.remove-attachment').css('pointer-events', 'auto'); // Enables the span element
                                    $('.remove-attachment').css('opacity', '1');
                                }, 10000);
                            })
                    })
                },
                initComplete:function () {
                    showInfoPopover();
                },
                aaSorting: [[2, "desc"]],
                "iDisplayLength": 10,
                "aLengthMenu": [[10, 50, 100, 200, -1], [10, 50, 100, 200, "All"]],
                "pagination": true,
                "aoColumns": [
                    {
                        "mData": "name",
                        "mRender": function (data, type, row) {
                            var url = "/signal/attachmentable/download?id=" + row.id;
                            return '<div class="attachFileName">' + createLinkWithEllipsis(url, row.name) + '</div>';
                        }
                    }, {
                        "mData": "description",
                        "mRender": function (data, type, row) {
                            if(row.description=="null" || row.description==null){
                                row.description=''
                            }
                            return "<span style='white-space:pre-wrap'><div class='attachDescription'>" + addEllipsisForDescriptionText(encodeToHTML(row.description))+ "</div></span>";
                        },
                        "className": 'cell-break',
                    }, {
                        "mData": "timeStamp",
                        "mRender": function (data, type, row) {
                            if(row.timeStamp=="null" || row.timeStamp==null){
                                return '' ;
                            } else {
                                return '<div class="attachTimestamp">' + moment.utc(row.timeStamp).format('DD-MMM-YYYY hh:mm:ss A') + '</div>';
                            }
                        }
                    }, {
                        "mData": "modifiedBy"
                    }, {
                        "mData": "id",
                        "mRender": function (data, type, row) {
                            return '<span tabindex="0" title="Remove Attachment"  style="cursor: pointer" class="glyphicon glyphicon-remove remove-attachment" data-field="removeAttachment" data-attachmentId=' + row.id + '></span>'
                        },
                        "visible": isVisible()
                    }
                ],
                "bLengthChange": true,
                columnDefs: [{
                    "targets": -1,
                    "sortable": false,
                    "render": $.fn.dataTable.render.text()
                }]
            });
            $("#attachmentForm .attachment-file").on("change", function () {
                var imgVal = $(this).val();
                var imgSize;
                if (imgVal != '') {
                    imgSize = $(this)[0].files[0].size;
                }
                if (imgVal != '' && imgSize < 20000000) {
                    $('#fileSizeMessage').hide()
                    $("#attachmentForm .upload").attr('disabled', false)
                } else if (imgVal != '') {
                    $('#fileSizeMessage').show();
                    $("#attachmentForm .upload").attr('disabled', true);
                } else {
                    $("#attachmentForm .upload").attr('disabled', true);
                }
            });
        });
        $('#showAttachmentModal').on('hidden.bs.modal', function (e) {
            $(this)
                .find("#attachmentForm .upload")
                .attr('disabled', true)
                .end();
            $("#fileSizeMessage").css({display:"none"});
        });

    };

    function isVisible() {
        var isVisible = true;
        if(typeof hasReviewerAccess !== "undefined" && !hasReviewerAccess) {
            isVisible = false;
        }
        return isVisible
    }

    var ajaxTimeout = null;
    var bindGridDynamicFilters = function (filtersData, prefix, id) {
        var dataTables_length = $('.disposition-ico');
        var dispositionButtons = signal.utils.render('disposition_filters1', {
            filtersData : filtersData,
            prefix : prefix,
            id : id,
        });
        dataTables_length.append(dispositionButtons)

        updateSelectAllState()

        function updateSelectAllState() {
            $('ul').each(function () {
                var allChecked = $(this).find('.dynamic-filters').length > 0 &&
                    $(this).find('.dynamic-filters:checked').length === $(this).find('.dynamic-filters').length;

                $(this).find('.select-all-checkbox').prop('checked', allChecked);
            });
        }

        $(document).on('click', '#selectAllDispositions', function () {
            var isChecked = $(this).is(':checked');

            $('.dynamic-filters').each(function () {
                if ($(this).prop('checked') !== isChecked) {
                    $(this).trigger('click');
                }
            });
        });

        $(document).on('click', '.dynamic-filters', function () {
            isPagination = false;
            var alertDetailsTable;
            var freqSelected = "";
            if ($("#frequencyNames")) {
                freqSelected = $("#frequencyNames").val();
            }
            var filterArray = {};
            var filterValues = [];
            var passedFilters = []
            $('.dynamic-filters').each(function (index) {
                if(passedFilters.indexOf($(this).val()) == -1) {
                   passedFilters.push($(this).val())
                    var id = escapeSpecialCharactersInId($(this).attr('id'));
                    if($(this).is(':checked')) {
                        filterValues.push($(this).val());
                        $('.dynamic-filters').closest("#" + id).prop('checked' , true)
                    } else{
                        $('.dynamic-filters').closest("#" + id).prop('checked' , false)
                    }
                    filterArray[$(this).val()]=$(this).is(':checked');
                }
            });
            var filteredFilterArray = {};//This will contain filtered values where closed = false of filterData
            var filteredFilterValues = filterValues.filter(function (value) {
                return filtersData.find(function (filter) {
                    return filter.value === value && !filter.closed;
                });
            });
            Object.keys(filterArray).forEach(function (key) {
                if (filteredFilterValues.includes(key)) {
                    filteredFilterArray[key] = filterArray[key];
                }
            });
            sessionStorage.setItem(prefix + "filters_store", JSON.stringify(filteredFilterArray));
            sessionStorage.setItem(prefix + "filters_value", JSON.stringify(filteredFilterValues));
            sessionStorage.setItem(prefix + "id", id);
            var url;
            if (applicationName == 'Literature Search Alert') {
                alertDetailsTable = $('#alertsDetailsTable').DataTable();
                url = listConfigUrl + "?filters=" + encodeURIComponent(JSON.stringify(filterValues)) + '&advancedFilterChanged=' + false
            } else {
                alertDetailsTable = $('#alertsDetailsTable').DataTable();
                url = listConfigUrl + "&frequency=" + freqSelected + '&advancedFilterChanged=' + false + "&isFilterRequest=true&filters=" + encodeURIComponent(JSON.stringify(filterValues));
                if (typeof isTempViewSelected !== "undefined" && isTempViewSelected != null && isTempViewSelected == true) {
                    url = url + "&tempViewId=" + tempViewPresent
                }
            }
            updateSelectAllState();
            // PVS-53683 change to call ajax with delay
            // Clear the previous timeout to cancel the previous call
            clearTimeout(ajaxTimeout);

            // Set a new timeout before making the Ajax call
            ajaxTimeout = setTimeout(function() {
                alertDetailsTable.ajax.url(url).load();
            }, 1500); // PVS-53683 change to call ajax with delay


        });
    };

    var isAlertPersistedInSessionStorage = function (prefix) {
        var c = signal.utils.getQueryString("configId");
        return c ? sessionStorage.getItem(prefix + "id") == c : false
    };

    var removeFiltersFromSessionStorage = function (prefix) {
        sessionStorage.removeItem(prefix + "filters_store");
        sessionStorage.removeItem(prefix + "filters_value");
        sessionStorage.removeItem(prefix + "id");
    };

    var openSaveViewModal = function (filterIndex, applicationName, viewId) {
        var systemDefault = ["System View"];
        var viewInfo;

        //saving new view
        $(".saveView").off('click');
        $('.saveView').on('click', function () {
            viewInfo = generateViewInfo(filterIndex);
            $("#view-modal-title").html("View Manager");
            $("#select-view-div").hide();

            $('#save-view-modal #view_name').val('');
            $('#viewSharedWith').empty()
            $('#save-view-modal #view_filters').val(JSON.stringify(viewInfo.filterMap));
            $('#save-view-modal #view_columnList').val(viewInfo.notVisibleColumn);
            $('#save-view-modal #view_alertType').val(applicationName);
            $('#save-view-modal #view_sorting').val(JSON.stringify(viewInfo.sortedColumn));
            $('#save-view-modal #view_advanced_filter').val(viewInfo.advancedFilter);
            $('#save-view-modal #current_view_id').val(viewId);
            $('#save-view-modal .save_buttons').show();
            $('#save-view-modal .edit_buttons').hide();
            $('#save-view-modal').modal('show');
        })

        //updating existing view
        $(".updateView").off('click');
        $('.updateView').on('click', function () {
            viewInfo = generateViewInfo(filterIndex);
            if ($.inArray($( ".boomarknav .active-bookmark" ).text().replace('(S)' , '').trim(), systemDefault) !== -1) {
                $("#view-modal-title").html("View Manager");
                $("#select-view-div").hide();
                $('#save-view-modal #view_filters').val(JSON.stringify(viewInfo.filterMap));
                $('#save-view-modal #view_columnList').val(viewInfo.notVisibleColumn);
                $('#save-view-modal #view_alertType').val(applicationName);
                $('#save-view-modal #view_sorting').val(JSON.stringify(viewInfo.sortedColumn));
                $('#save-view-modal #view_advanced_filter').val(viewInfo.advancedFilter);
                $('#save-view-modal #current_view_id').val(viewId);
                $('#save-view-modal .save_buttons').show();
                $('#save-view-modal .edit_buttons').hide();
                $('#save-view-modal').modal('show');
            } else {
                if ($("#isViewUpdateAllowed").val() == 'true') {
                    updateExistingView(viewInfo, applicationName)
                } else {
                    bootbox.confirm({
                        message: $.i18n._('defaultViewUpdateOnly'),
                        buttons: {
                            confirm: {
                                label: 'Continue',
                                className: 'btn-success'
                            },
                            cancel: {
                                label: 'Cancel',
                                className: 'btn-danger'
                            }
                        },
                        callback: function (result) {
                            if (result == true) {
                                updateExistingView(viewInfo, applicationName)
                            }
                        }
                    });
                }
            }
        });

        //selecting another view
        $('.viewSelect').off('click');
        $('.viewSelect').on('change', function () {
            var pageURL = $(location).attr("href");
            var selectedView = $('.viewSelect').val();
            sessionStorage.setItem('isViewCall', 'true');
            isUpdateTemp = false
            var preIndex = pageURL.indexOf('viewId')
            if (pageURL.indexOf("viewId") != -1 && (pageURL.indexOf("isVaers") != -1 || pageURL.indexOf("isFaers") != -1  || pageURL.indexOf("isVigibase") != -1 || pageURL.indexOf("isJader") != -1) && pageURL.indexOf('&' , preIndex ) != -1) {
                window.location.href = pageURL.slice(0, preIndex - 1) + pageURL.slice(pageURL.indexOf('&' , preIndex )) + "&viewId=" + selectedView
            } else if (pageURL.indexOf("viewId") != -1 && (pageURL.indexOf("isVaers") != -1 || pageURL.indexOf("isFaers") != -1 || pageURL.indexOf("isVigibase") != -1 ||  pageURL.indexOf("isVigibase") != -1)) {
                window.location.href = pageURL.slice(0, preIndex - 1) + "&viewId=" + selectedView
            } else if (pageURL.indexOf("viewId") != -1) {
                window.location.href = pageURL.slice(0, pageURL.indexOf("viewId") + 7) + selectedView
            } else {
                if (pageURL.indexOf("#") != -1) {
                    pageURL = pageURL.slice(0, pageURL.indexOf("#"))
                }
                window.location.href = pageURL + "&viewId=" + selectedView
            }
        });

        //deleting or editing selected view
        $('.editView').off('click');
        $('.editView').on('click', function () {
            viewInfo = generateViewInfo(filterIndex);

            if ($.inArray($( "#viewsListSelect option:selected" ).text().replace('(S)' , '').trim(), systemDefault) == -1) {
                $("#view-modal-title").html("View Manager");
                $("#select-view-div").show();
                var viewMap
                $.each(filteredViews,function(key , value){
                    if(value.id == $( "#viewsListSelect option:selected" ).val())
                        viewMap = value;
                });
                if (viewMap.isAdmin == true || ((viewMap.isShared == false) && (viewMap.viewUserId == viewMap.currentUserId))) {
                    $('.delete-view').attr('disabled', false);
                } else {
                    $('.delete-view').attr('disabled', true);
                }
                $('#save-view-modal #view_name').val($( "#viewsListSelect option:selected" ).text().replace("(S)" , ""));
                $('#save-view-modal #view_default').prop('checked', (viewMap.defaultView == '(default)'));
                $('#save-view-modal #view_id').val($('.viewSelect :selected').val());
                $('#save-view-modal #view_filters').val(JSON.stringify(viewInfo.filterMap));
                $('#save-view-modal #view_columnList').val(viewInfo.notVisibleColumn);
                $('#save-view-modal #view_sorting').val(JSON.stringify(viewInfo.sortedColumn));
                $('#save-view-modal #view_advanced_filter').val(viewInfo.advancedFilter);
                $('#save-view-modal #view_alertType').val(applicationName);
                $('#save-view-modal .save_buttons').hide();
                $('#save-view-modal .edit_buttons').show();
                $('#save-view-modal').modal('show');
            } else {
                $('#alert-view-modal').modal('show');
            }
            $('#viewsListSelect').on('change', function (e) {
                $("#view_name").val($( "#viewsListSelect option:selected" ).text().replace("(S)" , ""));
                var viewMap
                $.each(filteredViews,function(key , value){
                    if(value.id == $( "#viewsListSelect option:selected" ).val())
                        viewMap = value;
                });
                if (viewMap.isAdmin == true || ((viewMap.isShared == false) && (viewMap.viewUserId == viewMap.currentUserId))) {
                    $('.delete-view').attr('disabled', false);
                } else {
                    $('.delete-view').attr('disabled', true);
                }
                $("#isViewUpdateAllowed").val(viewMap.isViewUpdateAllowed);
                $('#save-view-modal #view_default').prop('checked', (viewMap.defaultView == '(default)'));
                $('#viewSharedWith').empty()
                bindShareWith2WithData($('#viewSharedWith') , '/signal/user/searchShareWithUserGroupList' , viewMap.sharedWithGroups.concat(viewMap.sharedWithUsers) )


            });
        })
    };

    var generateViewInfo = function (filterIndex) {
        var oTable = $('#alertsDetailsTable').DataTable();

        //generate filter value map
        var filterMap = new Object();
        $.each(filterIndex, function (idx, obj) {
            var filterVal = yadcf.exGetColumnFilterVal(oTable, obj);
            if (filterVal) {
                filterMap[obj] = filterVal
            }
        });


        $.each(fixedFilterColumn, function (idx, obj) {
            if(obj["value"] != "") {
                filterMap[obj.column] = obj["value"];
            } else {
                delete filterMap[obj.column];
            }
        });


        //generate columns visible list
        var notVisibleColumn = $('#columnIndex').val();

        //generate sorting column info
        var sortedColumn = new Object();
        if ($('#alertsDetailsTable').DataTable().order()) {
            var sortedCol = $('#alertsDetailsTable').DataTable().order()[0][0];
            var columnName = $('#alertsDetailsTable').find("th").eq(sortedCol).attr("data-field");
            var index = 0;
            columnSeq.every(function () {
                var seqName = columnSeq[index].name;
                if (columnName == seqName) {
                    sortedCol = columnSeq[index].seq;
                    return false;
                }
                index++;
                return true;
            });
            var sortedDir = $('#alertsDetailsTable').DataTable().order()[0][1];
            sortedColumn[sortedCol] = sortedDir;
        }

        //generate advanced filter info
        var advancedFiterId = $('.advanced-filter-dropdown').val();
        return {
            'filterMap': filterMap,
            'notVisibleColumn': notVisibleColumn,
            'sortedColumn': sortedColumn,
            'advancedFilter': advancedFiterId
        }
    };

    var createSortingMap = function (infoKey, viewName) {
        var sortingMap = [];
        var sorting = {};
        var sessionStoredVal;
        if (sessionStorage.getItem(viewName) == $('.viewSelect :selected').text().replace("(default)", "").trim()) {
            sessionStoredVal = sessionStorage.getItem(infoKey);
        }
        if (sessionStoredVal) {
            sorting = JSON.parse(sessionStoredVal);
        } else if ($("#sortedColumn").val() != "" && $("#sortedColumn").val() != "{}" && callingScreen == 'review') {
            sorting = JSON.parse($('#sortedColumn').val());
        }
        $.each(sorting, function (key, value) {
            sortingMap.push([parseInt(key), value])
        });
        return sortingMap
    };

    var createFilterMap = function (infoKey, viewName) {
        var filtersValue = [];
        var filterMap = {};
        var sessionStoredVal;
        if (sessionStorage.getItem(viewName) == $('.viewSelect :selected').text().replace("(default)", "").trim()) {
            sessionStoredVal = sessionStorage.getItem(infoKey);
        }
        if (sessionStoredVal && sessionStoredVal != {}) {
            filterMap = JSON.parse(sessionStoredVal);
        } else if ($('#filterMap').val() != "" && $('#filtermDataMap').val() != "{}") {
            filterMap = JSON.parse($('#filterMap').val());
        }
        $.each(filterMap, function (key, value) {
            var filterArr = [parseInt(key), value];
            filtersValue.push(filterArr)
        });
        return filtersValue
    };

    var createListOfIndex = function (infoKey, isEvdas, viewName) {
        var loi = [];
        var columnIndex = []
        var sessionStoredVal;
        if (sessionStorage.getItem(viewName) == $('.viewSelect :selected').text().replace("(default)", "").trim()) {
            sessionStoredVal = sessionStorage.getItem(infoKey);
        }
        if (callingScreen == 'review') {
            if (sessionStoredVal) {
                columnIndex = sessionStoredVal.split(',')
            } else {
                columnIndex = $('#columnIndex').val().split(',');
            }
            $.each(columnIndex, function () {
                if (isEvdas) {
                    loi.push(parseInt(this))
                } else {
                    loi.push(parseInt(this) + 1)
                }
            });
        }
        return loi
    };

    var setSortOrder = function () {
        $("#alertsDetailsTableRow").on('mousedown', 'th', function () {
            if ($(this).find('.alert-select-all,#select-all').length !== 1) {
                index = $(this).attr("data-column-index");
                dir = 'asc';
                if ($(this).hasClass('dt-ordering-asc')) {
                    dir = 'desc'
                } else if ($(this).hasClass('dt-ordering-desc')) {
                    dir = 'asc'
                }
                isViewInstance = 0;
            }
        });
    };

    var openAlertTagModal = function () {
        $(document).on('click', '.editAlertTags', function (evt) {
            var parent_row = $(event.target).closest('tr');
            var index = $(this).closest('tr').index();
            var rowObject = table.rows(index).data()[0];
            var alertId = rowObject.id;
            var execConfigId = parent_row.find('.execConfigId').attr("value");
            $.ajax({
                url: fetchTagsUrl,
                data: {
                    alertId: alertId
                },
            })
                .done(function (result) {
                    var alertTagModalObj = $('#alertTagModal');
                    alertTagModalObj.find('#singleAlertTags').select2({
                        placeholder: "Add Categories",
                        multiple: true,
                        tags: true,
                        data: result.tagList

                    });
                    alertTagModalObj.modal('show');
                    alertTagModalObj.find('#singleAlertTags').val(result.alertTagList);
                    alertTagModalObj.find('#singleAlertTags').trigger('change');
                    alertTagModalObj.find(".addTags").off().on('click', function () {
                        $.ajax({
                            url: saveTagUrl,
                            data: {
                                alertTags: JSON.stringify(alertTagModalObj.find('#singleAlertTags').val()),
                                alertId: alertId,
                                execConfigId: execConfigId
                            },
                            contentType: 'application/json',
                        })
                            .done(function (result) {
                                table.ajax.reload()
                            });
                    });
                });
        });
    };

    var openSingleAlertTagModal = function (tableObj, tagsObj) {
        $(document).on('click', '.editAlertTags', function (event) {
            var parent_row = $(event.target).closest('tr');
            var index = $(this).closest('tr').index();
            if (isAbstractViewOrCaseView(index)) {
                index = index / 2
            }
            var rowObject = tableObj.rows(index).data()[0];
            var alertId = rowObject.id;
            var execConfigId = parent_row.find('.execConfigId').attr("value");
            var alertTagModalObj = $('#alertTagModal');
            var $singleCaseAlertTags = alertTagModalObj.find('#singleAlertTags');
            var $globalTags = alertTagModalObj.find('#globalTags');
            applySelect2ForTags($singleCaseAlertTags, fetchTagsUrl, 'Case Series Categories', true);
            applySelect2ForTags($globalTags, fetchTagsUrl, 'Global Categories', false);
            $singleCaseAlertTags.find('option').remove()
            $globalTags.find('option').remove()
            $singleCaseAlertTags.val(null).trigger("change");
            $globalTags.val(null).trigger("change");
            $.each(rowObject.caseSeriesTags, function (index, value) {
                var option = new Option(value, value, true, true);
                $singleCaseAlertTags.append(option);
            });
            $.each(rowObject.globalTags, function (index, value) {
                var option = new Option(value, value, true, true);
                $globalTags.append(option);
            });
            alertTagModalObj.find('#singleAlertTags').trigger('change');
            alertTagModalObj.find('#globalTags').trigger('change');
            var enableCaseSeriesTags = rowObject.isCaseSeriesGenerated;
            if (!enableCaseSeriesTags) {
                alertTagModalObj.find('#singleAlertTags').attr('disabled', '');
            }
            alertTagModalObj.modal('show');
            alertTagModalObj.find(".addTags").off().on('click', function () {
                $.ajax({
                    url: saveTagUrl,
                    data: {
                        alertTags: JSON.stringify(alertTagModalObj.find('#singleAlertTags').val()),
                        globalTags: JSON.stringify(alertTagModalObj.find('#globalTags').val()),
                        deletedCaseSeriesTags: JSON.stringify(tagsObj.deletedCaseSeriesTags),
                        deletedGlobalTags: JSON.stringify(tagsObj.deletedGlobalTags),
                        addedCaseSeriesTags: JSON.stringify(tagsObj.addedCaseSeriesTags),
                        addedGlobalTags: JSON.stringify(tagsObj.addedGlobalTags),
                        alertId: alertId,
                        execConfigId: execConfigId
                    },
                    contentType: 'application/json',
                })
                    .done(function (payload) {
                        if (payload.status) {
                            tableObj.ajax.reload();
                            $.Notification.notify('success', 'top right', "Success", payload.message, {autoHideDelay: 5000});
                        } else {
                            $.Notification.notify('error', 'top right', "Error", payload.message, {autoHideDelay: 5000});
                        }
                        $('#single-case-alert-spinner').addClass('hidden');
                    });
                $('#single-case-alert-spinner').removeClass('hidden');
            });
        });
    };

    var populateAdvancedFilterSelect = function (alertType) {

        if($("#isAdhocCaseSeries").val() != undefined && $('#isAdhocCaseSeries').val() == 'true' && $("#isJader").val() != "true"){
            alertType = ALERT_CONFIG_TYPE.SINGLE_CASE_ALERT_DRILL_DOWN_ADHOC
        }
        if ($("#isAdhocCaseSeries").val() != undefined && ($('#isAdhocCaseSeries').val() == 'true' || $('#isAdhocCaseSeries').val() == true) && ($('#isFaers').val() == 'true' || $('#isFaers').val() == true)) {
            alertType = getAlertType()
        }
        $(".advanced-filter-dropdown").select2({
            placeholder: $.i18n._('selectOne'),
            allowClear: true,
            ajax: {
                url: fetchAdvFilterUrl,
                dataType: 'json',
                type: "GET",
                quietMillis: 50,
                data: function (params) {
                    return {
                        alertType: alertType,
                        term: params.term || '',
                        page: params.page || 1,
                        max: params.max || 30,
                        callingScreen: callingScreen
                    };
                },
                processResults: function (data, params) {
                    params.page = params.page || 1;
                    return {
                        results: $.map(data.list, function (filter) {
                            return {
                                text: filter.name,
                                id: filter.id
                            }
                        }),
                        pagination: {
                            more: (params.page * 30) < data.totalCount
                        }
                    };
                }
            }
        });

        //select advanced filter based on current view
        if ($('#advancedFilterView').val()) {
            var advancedFilterView = JSON.parse($('#advancedFilterView').val());
            var option = new Option(advancedFilterView.name, advancedFilterView.id, true, true);
            $(".advanced-filter-dropdown").append(option).trigger('change');
        }

        $('#addAdvancedFilter').on('click', function () {
            signal.advancedFilter.initializeAdvancedFilters($('#createAdvancedFilterModal'));
            $('#createAdvancedFilterModal #selectOperator').empty().append('<option selected="selected" value="">Select Operator</option>');
            $('#createAdvancedFilterModal #name').val('').attr("disabled", false);
            $('#createAdvancedFilterModal #description').val('');
            $('#createAdvancedFilterModal #queryJSON').val('');
            $('#createAdvancedFilterModal #alertType').val('');
            $('#createAdvancedFilterModal #advancedFilterSharedWith').empty();
            $('#createAdvancedFilterModal #builderAll').find('.expression').remove();
            $('#createAdvancedFilterModal #filterId').val("");
            $('#createAdvancedFilterModal .deleteAdvFilter').addClass('hide');
            $('#createAdvancedFilterModal .deleteAdvFilter').attr("disabled", false);
            $('#createAdvancedFilterModal #addExpression').attr("disabled", false);
            $('#createAdvancedFilterModal .filtersWithoutSaving').removeClass('hide');
            $('#createAdvancedFilterModal .saveAdvancedFilters').attr("disabled", false);
            $('#createAdvancedFilterModal').modal('show');

        });


        $('#editAdvancedFilter').off().on('click', function () {
            if (filterOpened == 0 && $('#advanced-filter').val()) {
                var advancedFilterId = $('.advanced-filter-dropdown').val();
                if (advancedFilterId) {
                    $.ajax({
                        url: fetchAdvancedFilterInfoUrl,
                        data: {'advancedFilter.id': advancedFilterId},
                        dataType: 'json',
                        type: "GET",
                    })
                        .done(function (result) {
                            var isDisabled = (result.isFilterUpdateAllowed == true) ? false : true
                            $('#createAdvancedFilterModal #selectOperator').empty().append('<option selected="selected" value="">Select Operator</option>');
                            $('#createAdvancedFilterModal #name').val(result.name).attr("disabled", isDisabled);
                            $('#createAdvancedFilterModal #name').val(result.name);
                            $('#createAdvancedFilterModal #description').val(result.description);
                            $('#createAdvancedFilterModal #queryJSON').val(result.JSONQuery);
                            $('#createAdvancedFilterModal #alertType').val(alertType);
                            $('#createAdvancedFilterModal #filterId').val(advancedFilterId);
                            $('#createAdvancedFilterModal #shareAdvFilterId').html(result.shareWithElement);
                            $('#createAdvancedFilterModal .filtersWithoutSaving').addClass('hide');
                            $('#createAdvancedFilterModal .deleteAdvFilter').removeClass('hide');
                            $('#createAdvancedFilterModal #addExpression').attr("disabled", isDisabled);
                            $('#createAdvancedFilterModal .deleteAdvFilter').attr("disabled", isDisabled);
                            $('#createAdvancedFilterModal .saveAdvancedFilters').attr("disabled", isDisabled);
                            $('#createAdvancedFilterModal .modal-title').html("Edit Filter");
                            signal.advancedFilter.initializeAdvancedFilters();
                            $('#createAdvancedFilterModal').modal('show');
                        });
                }
                filterOpened = 1;
            }
        });

        $(".advanced-filter-dropdown").on("change", function (e) {
            main_disposition_filter=false
            advancedFilterChanged = true;
            var filterValues = [];
            var passedFilters = [];
            var restDisp
            $('.dynamic-filters').each(function (index) {
                if(passedFilters.indexOf($(this).val()) === -1) {
                    passedFilters.push($(this).val())
                    if($(this).is(':checked')) {
                        filterValues.push($(this).val());
                    }
                }
            });
            restDisp=filterValues
            if( $(".advanced-filter-dropdown").val()==null)
            {
                main_disposition_filter=true
                if(applicationName===ALERT_CONFIG_TYPE.AGGREGATE_CASE_ALERT)
                {
                    filterValues=JSON.parse(sessionStorage.getItem('agg_filters_value'))
                }
               else if(applicationName===ALERT_CONFIG_TYPE.SINGLE_CASE_ALERT)
                {
                    filterValues=JSON.parse(sessionStorage.getItem('sca_filters_value'))
                }
               else if(applicationName===ALERT_CONFIG_TYPE.EVDAS_ALERT)
                {
                    filterValues=JSON.parse(sessionStorage.getItem('ev_filters_value'))
                }
            }
            if(filterValues==null)      //when advanced filter not contains any disposition
            {
                filterValues=restDisp
            }
            // This will be used advanced filter is empty
            if (typeof table !== 'undefined' && typeof (table.ajax) !== 'undefined') {
                var urlStr = table.ajax.url().replace("&advancedFilterChanged=false", "&advancedFilterChanged=true");
                urlStr = urlStr.replace("&isFilterRequest=false", "&isFilterRequest=true");
                var filterIdx = urlStr.indexOf("filters=");
                if (urlStr.match('&tempViewId=') && !urlStr.match('&isFilterRequest=true') && !urlStr.match('&advancedFilterChanged=true')) {
                    urlStr += "&isFilterRequest=true&advancedFilterChanged=true"
                    filterIdx = urlStr.length
                }
                var tmpStr = urlStr.substring(filterIdx);
                var newAmpIdx = tmpStr.indexOf("&");
                var newTmpStr = "";
                if (newAmpIdx > 0) {
                    tmpStr.substring(newAmpIdx);
                }
                var finalUrlStr = urlStr.substring(0, filterIdx);
                if (urlStr.match('&tempViewId=')) {
                    finalUrlStr += "&";
                }
                finalUrlStr += "filters=" + encodeURIComponent(JSON.stringify(filterValues)) + newTmpStr;
                table.ajax.url(finalUrlStr).load()
            }
        });
    };

    var openCommentHistoryModal = function (alertType) {

        //Bind the click event on the case history icon.
        $(document).on('click', '.comment-history-icon', function (event) {
            event.preventDefault();

            var commentHistoryModal = $('#commentHistoryModal');
            commentHistoryModal.modal('show');
            var commentModal = $('#commentModal');
            commentModal.css("display","none");

            $('#commentHistoryModal').on('hidden.bs.modal', function (e) {
                commentModal.css("display","block");
            });
            signal.alertComments.populate_comments_history(commentModal, null, null);
        });
    };

    return {
        openAlertCommentModal: openAlertCommentModal,
        openCaseHistoryModal: openCaseHistoryModal,
        restartReview: restartReview,
        openSimilarCasesModal: openSimilarCasesModal,
        applyBusinessRules: applyBusinessRules,
        showAttachmentModal: openAttachmentModal,
        bindGridDynamicFilters: bindGridDynamicFilters,
        enableMenuTooltips: enableMenuTooltips,
        enableMenuTooltipsDynamicWidth: enableMenuTooltipsDynamicWidth,
        disableTooltips: disableTooltips,
        sortIconHandler: sortIconHandler,
        isAlertPersistedInSessionStorage: isAlertPersistedInSessionStorage,
        removeFiltersFromSessionStorage: removeFiltersFromSessionStorage,
        openSaveViewModal: openSaveViewModal,
        createSortingMap: createSortingMap,
        createFilterMap: createFilterMap,
        createListOfIndex: createListOfIndex,
        openAlertTagModal: openAlertTagModal,
        setSortOrder: setSortOrder,
        openSingleAlertTagModal: openSingleAlertTagModal,
        generateViewInfo: generateViewInfo,
        populateAdvancedFilterSelect: populateAdvancedFilterSelect,
        initiateCommentHistoryTable: initiateCommentHistoryTable,
        openCommentHistoryModal: openCommentHistoryModal
    }

})();

function showErrorMessageInModal($modal, text) {
    clear_errors();
    if (text) {
        text = text;
    } else {
        text = 'Please fill  the required Fields';
    }
    var alertHtml = getErrorMessageHtml(text);
    $modal.find('.modal-body').prepend(alertHtml);
}

function getErrorMessageHtml(msg) {
    var alertHtml = '<div class="alert alert-danger alert-dismissible" role="alert"> ' +
        '<button type="button" class="close" data-dismiss="alert"> ' +
        '<span aria-hidden="true">&times;</span> ' +
        '<span class="sr-only"><g:message code="default.button.close.label" /></span> ' +
        '</button> ' + msg;
    '</div>';
    return alertHtml;
}

var clear_errors = function () {
    $('.modal .modal-body .alert').remove();
};

function search(nameKey, myArray) {
    for (var i = 0; i < myArray.length; i++) {
        if (myArray[i].name === nameKey) {
            return myArray[i];
        }
    }
}

function populateTopicDetail($modal, topicObj) {
    $modal.find('#startDate').val(topicObj.startDate);
    $modal.find('#endDate').val(topicObj.endDate);
    $modal.find('.product-span').text(getProductNameList(JSON.parse(topicObj.products)));
    $modal.find('#startDate').attr("disabled", "");
    $modal.find('#endDate').attr("disabled", "");
    if (topicObj.products) {
        $modal.find('#product').attr("disabled", "");
    }
}

function depopulateTopicDetail($modal) {
    var productJson = $modal.find('.product-json-container').val();
    $modal.find('#startDate').val("");
    $modal.find('#endDate').val("");
    $modal.find('#product').val("");
    $modal.find('.product-span').text(getProductNameList(JSON.parse(productJson)));
    $modal.find('#startDate').removeAttr("disabled");
    $modal.find('#endDate').removeAttr("disabled");
    $modal.find('#product').removeAttr("disabled");
}

function generateProductJson(productId, productName, level) {
    var topicProductValues = {"1": [], "2": [], "3": [], "4": [], "5": []};
    if (level != ASSESSMENT_DRUG_REC_NUM_LEVEL) {
        topicProductValues[level].push({name: productName, id: productId});
    }
    return topicProductValues;
}

function getProductNameList(obj) {
    var productArray = [];
    var objArray = obj['3'];
    $.each(objArray, function (index, value) {
        productArray.push(value.name);
    });
    return productArray.join(',');
}

function getProductJson(applicationName, row) {
    var productJson;
    var productName = row.find('span[data-field="productName"]').attr("data-id");
    var productId = row.find('.row-product-id').val();
    var level = row.find('.row-level-id').val();
    if (!level) {
        level = 3;
    }
    productJson = generateProductJson(productId, productName, level);
    return productJson;
}

function applySelect2ForTags($selector, url, placeHolderText, isCaseSeriesTag) {
    $selector.select2({
        minimumInputLength: 0,
        multiple: true,
        tags: true,
        placeholder: placeHolderText,
        allowClear: true,
        width: "100%",
        createTag: function (tag) {

            // check if the option is already there
            found = false;
            $selector.find("option").each(function () {
                if ($.trim(tag.term).toUpperCase() === $.trim($(this).text()).toUpperCase()) {
                    prevTag = $(this);
                    found = true;
                }
            });

            // if it's not there, then show the suggestion
            if (!found) {
                return {
                    id: $.trim(tag.term),
                    text: $.trim(tag.term)
                };
            } else {
                return {
                    id: $.trim(prevTag.text()),
                    text: $.trim(prevTag.text())
                };
            }
        },
        ajax: {
            quietMillis: 250,
            dataType: "json",
            url: url,
            data: function (params) {
                return {
                    term: params.term,
                    page: params.page || 1,
                    max: 30,
                    lang: userLocale,
                    isCaseSeriesTag: isCaseSeriesTag
                };
            },
            processResults: function (data, params) {
                params.page = params.page || 1;
                return {
                    results: data.list,
                    pagination: {
                        more: (params.page * 30) < data.totalCount
                    }
                };
            }
        }
    });
}

function showCommentIcon(currentRow) {
    var $btnGroup = $(currentRow).closest('tr').find('td.dropDown .btn-group');
    if ($($btnGroup).find('.comment').length === 0) {
        $($btnGroup).find('.dropdown-toggle').after('<i class="mdi mdi-chat blue-2 font-13 pos-ab comment" title="' + $.i18n._('commentAvailable') + '"></i>');
    }
}

function removeCommentIcon(currentRow) {
    var $btnGroup = $(currentRow).closest('tr').find('td.dropDown .btn-group');
    $($btnGroup).find('.dropdown-toggle').next('i').remove();
}

function removeAttachmentIcon(currentRow) {
    var $btnGroup = $(currentRow).closest('tr').find('td.dropDown .btn-group');
    if ($($btnGroup).find('.comment').length === 0) {
        $($btnGroup).find('.dropdown-toggle').next('i').remove();
    } else {
        $($btnGroup).find('.comment').next('i').remove();
    }
}

function showAttachmentIcon(trRow) {
    var $btnGroup = $(trRow).find('td.dropDown .btn-group');
    if ($($btnGroup).find('.attach').length === 0) {
        if ($($btnGroup).find('.comment').length === 0) {
            $($btnGroup).find('.dropdown-toggle').after(' <i class="mdi mdi-attachment blue-1 font-13 pos-ab attach" title="' + $.i18n._('attachmentAvailable') + '"></i> ');
        } else {
            $($btnGroup).find('.comment').after(' <i class="mdi mdi-attachment blue-1 font-13 pos-ab attach" title="' + $.i18n._('attachmentAvailable') + '"></i> ');
        }
    }
}

var isSafetyLeadAllowed = function (listProductIds, selectedProductId) {
    return listProductIds.indexOf(selectedProductId) !== -1
};

var setColumnSeq = function (mapSeq) {
    columnSeq = mapSeq
};

function connectWebsocket() {
    //Create a new SockJS socket - this is what connects to the server
    var socket = new SockJS(socketURL);

    //Build a Stomp client to receive/send messages over the socket we built.
    var client = Stomp.over(socket);

    var queueToSubscribe = NOTIFICATION_QUEUE + userId;

    //Have SockJS connect to the server.
    client.connect("", "", function () {
        //Listening to the connection we established
        client.subscribe(queueToSubscribe, function (message) {
            if(message.body.includes(ALERT_CONFIG_TYPE.CASE_DRILLDOWN)){
                $('#alertsDetailsTable').DataTable().ajax.reload()
            }

        });
    });
}
var updateExistingView = function(viewInfo, applicationName){
    var request = new Object()
    var sharedWithData = $('#viewSharedWith').val() ? JSON.stringify($('#viewSharedWith').val()) : null;
    request['name'] =  $( "#viewsListSelect option:selected" ).text().replace('(S)' , '').trim();
    request['filterMap'] = JSON.stringify(viewInfo.filterMap);
    request['columnList'] = viewInfo.notVisibleColumn.toString();
    request['alertType'] = applicationName;
    request['id'] =  $( "#viewsListSelect option:selected" ).val();
    request['sorting'] = JSON.stringify(viewInfo.sortedColumn);
    request['advancedFilter'] = viewInfo.advancedFilter;
    request['viewSharedWith'] = sharedWithData;
    request['defaultView'] = $('#view_default').prop('checked')
    $.ajax({
        url: updateViewUrl,
        type: "POST",
        data: request,
        dataType: "json"
    })
        .done(function (data) {
            var pageURL = $(location).attr("href");
            var selectedView = data.viewId;
            var preIndex = pageURL.indexOf('viewId')
            if (pageURL.indexOf("viewId") != -1 && (pageURL.indexOf("isVaers") != -1 || pageURL.indexOf("isFaers") != -1 || pageURL.indexOf("isVigibase") != -1 || pageURL.indexOf("isJader") != -1) && pageURL.indexOf('&' , preIndex ) != -1) {
                window.location = pageURL.slice(0, preIndex - 1) + pageURL.slice(pageURL.indexOf('&' , preIndex )) + "&viewId=" + selectedView
            } else if (pageURL.indexOf("viewId") != -1 && (pageURL.indexOf("isVaers") != -1 || pageURL.indexOf("isFaers") != -1 || pageURL.indexOf("isVigibase") != -1 || pageURL.indexOf("isJader") != -1)) {
                window.location = pageURL.slice(0, preIndex - 1) + "&viewId=" + selectedView
            } else if (pageURL.indexOf("viewId") != -1) {
                window.location = pageURL.slice(0, pageURL.indexOf("viewId") + 7) + selectedView
            } else {
                if (pageURL.indexOf("#") != -1) {
                    pageURL = pageURL.slice(0, pageURL.indexOf("#"))
                }
                window.location = pageURL + "&viewId=" + selectedView
            }
            if(data.errorMessage != ''){
                showErrorNotification(data.errorMessage, 10000);
            }
        })


}

var bookmarkDrag = function () {
    bookmarkDragStart();
    bookmarkDragOver();
    bookmarkDropped();
    bookmarkDragLeave();
}

var bookmarkDragStart = function() {
    document.addEventListener("dragstart", (event) => {
        $(event.target).addClass("color-black");
        dragged = event.target;
        value = event.target.value;
        list = $(".bookmark");
        $.each(list, function (key, value) {
            if (value == dragged) {
                index = key;
            }
        });
    });
};


var bookmarkDragOver = function() {
    document.addEventListener("dragover", (event) => {
        event.preventDefault();
    });
};

var bookmarkDragLeave = function() {
    document.addEventListener("dragleave", (event) => {
        event.preventDefault();
    });
}

var bookmarkDropped = function() {
    document.addEventListener("drop", ({target}) => {
        $(dragged).removeClass("color-black");
        if (target.className == "bookmark" && target.value !== value) {
            dragged.remove(dragged);
            $.each(list, function (key, value) {
                if (value == target) {
                    indexDrop = key;
                }
            });
            if (index > indexDrop) {
                target.before(dragged);
            } else {
                target.after(dragged);
            }
            if (index > 4 && indexDrop <= 4) {
                if ($("#sortable1 li:nth-child(6)").eq(0).hasClass("active-bookmark")) {
                    $("#sortable2").prepend($('#sortable1 li:nth-child(5)').eq(0))
                } else {
                    $("#sortable2").prepend($('#sortable1 li:nth-child(6)').eq(0))
                }
            }
            else if (index <= 4 && indexDrop > 4)
                ($('#sortable1 li:nth-child(4)')).after($('#sortable2 li:nth-child(1)'))
            var changedViewOrder = []
            var newOrder = $(".bookmark")
            $.each(newOrder, function (key, value) {
                if (value.value != list[key].value) {
                    changedViewOrder.push({id: value.value, order: key + 1})
                }
            });
            $.ajax({
                url: '/signal/viewInstance/saveBookmarkPositions',
                data: {updatedViewsOrder: JSON.stringify(changedViewOrder)},
            });
        }
    });
}

var fetchViewInstanceList = function(isTempView = false) {
    if($("#isAdhocCaseSeries").val() != undefined && $('#isAdhocCaseSeries').val() == 'true' && $("#isJader").val() != "true"){
        alertType = ALERT_CONFIG_TYPE.SINGLE_CASE_ALERT_DRILL_DOWN_ADHOC
    }
    $.ajax({
        url: "/signal/viewInstance/fetchViewInstances",
        data: {alertType: alertType, viewId: viewId},
    })
        .done(function (data) {
            var viewsList = data.viewsList;
            var isDropDownRequired  = viewsList.length > 5 ? true : false;
            viewsGridList =  signal.utils.render('views_list_v1', {
                selectedViewId : viewId,
                viewsList : viewsList,
                isDropDownRequired : isDropDownRequired,
                viewId : viewId,
                isTempViewSelected : isTempView
            });
            $(".views-list").html(viewsGridList);
            webUiPopInit();
            openSelectedView();
            bookmarkDrag();
            viewsList.sort((a, b) => (a.name.toUpperCase() > b.name.toUpperCase()) ? 1 : -1);
            filteredViews = viewsList.filter(item => item.name !== "System View")
            $.each(filteredViews , function(key , value) {
                if(value.id != viewId) {
                    $("#viewsListSelect").append(new Option(value.name, value.id));
                } else {
                    $('#save-view-modal #view_name').val(value.name.replace("(S)" , ""));
                    $('#save-view-modal #view_default').prop('checked', (value.defaultView == '(default)'));
                    $("#viewsListSelect").append(new Option(value.name, value.id , true , true));
                }
            })
            $("#temp-view-bookmark").on('click', function () {
                deleteTempView();
                var pageURL = $(location).attr("href");
                if(pageURL.indexOf("tempViewId") != -1) {
                    var preIndex = pageURL.indexOf('tempViewId')
                    pageURL = pageURL.slice(0, preIndex) + pageURL.slice(pageURL.indexOf('&' , preIndex) , -1)
                }
                isUpdateTemp = false
                window.location.href = pageURL
            })
        });
};

var openSelectedView = function() {
    $('.bookmark').on('click', function () {
        var pageURL = $(location).attr("href");
        var selectedView = $(this).val();
        sessionStorage.setItem('isViewCall', 'true');
        if(pageURL.indexOf("tempViewId") != -1) {
            var preIndex = pageURL.indexOf('tempViewId')
            pageURL = pageURL.slice(0, preIndex) + pageURL.slice(pageURL.indexOf('&' , preIndex) , -1)
        }
        if (pageURL.indexOf("detailedAdvancedFilterId") != -1) {
            const advanceFilterRegex = /&detailedAdvancedFilterId=(\d+)/;
            pageURL = pageURL.replace(advanceFilterRegex, "");
        }
        isUpdateTemp = false
        var preIndex = pageURL.indexOf('viewId')
        if (pageURL.indexOf("viewId") != -1 && (pageURL.indexOf("isJader") != -1 || pageURL.indexOf("isVigibase") != -1 || pageURL.indexOf("isVaers") != -1 || pageURL.indexOf("isFaers") != -1) && pageURL.indexOf('&' , preIndex ) != -1) {
            window.location.href = pageURL.slice(0, preIndex - 1) + pageURL.slice(pageURL.indexOf('&' , preIndex )) + "&viewId=" + selectedView
        } else if (pageURL.indexOf("viewId") != -1 && (pageURL.indexOf("isJader") != -1 || pageURL.indexOf("isVigibase") != -1 || pageURL.indexOf("isVaers") != -1 || pageURL.indexOf("isFaers") != -1)) {
            window.location.href = pageURL.slice(0, preIndex - 1) + "&viewId=" + selectedView
        } else if (pageURL.indexOf("viewId") != -1) {
            window.location.href = pageURL.slice(0, pageURL.indexOf("viewId") + 7) + selectedView
        } else {
            if (pageURL.indexOf("#") != -1) {
                pageURL = pageURL.slice(0, pageURL.indexOf("#"))
            }
            window.location.href = pageURL + "&viewId=" + selectedView
        }
    });
};

window.onload = function() {
    $(".mdi-checkbox-marked-outline , .saveViewPanel").on('click', function(){ $('.tooltip').hide()});
}

var showTempViewModal = function (isTempAvailable) {
    var pageURL = $(location).attr("href");
    var isShowModal = pageURL.indexOf("viewId") == -1 && pageURL.indexOf("isCaseDetailView") == -1
    if(($('#isFaers').length == 0 || $('#isFaers').val() == "" || $('#isFaers').val() == "false") && (isArchived == "false" || isArchived==false) && (isShowModal || isTempAvailable)) {
        if (!$(".bootbox").hasClass('in')) {
            if (tempViewPresent != false) {
                var clipboardPopup = bootbox.dialog({
                    title: 'Copy from Clipboard',
                    closeButton: false,
                    message: $.i18n._('pasteFromClipboard'),
                    buttons: {
                        yes: {
                            label: 'Yes',
                            className: 'btn-primary',
                            callback: function () {
                                fetchTempView();
                            }
                        },
                        no: {
                            label: 'No',
                            className: 'btn-primary',
                            callback: function () {
                                updateTempView(true)
                            }
                        },
                        clearClipboard: {
                            label: 'Clear Clipboard',
                            className: 'btn-default',
                            callback: function () {
                                deleteTempView();
                            }
                        },
                    }
                });
                clipboardPopup.init(function() {
                    clipboardPopup.attr("id", "clipboardPopup")
                });
            }
        }
    }
}

var deleteTempView = function() {
    $.ajax({
        url: deleteTempViewUrl,
    });
}

var fetchTempView = function() {
    fetchViewInstanceList(true);
    isTempViewSelected = true;
    $('.yadcf-filter-wrapper').hide();
    table.search('').columns().search('');
    var newUrl = listConfigUrl + "&tempViewId=" + tempViewPresent;
    $("#advanced-filter").val('').trigger('change.select2');
    $('#alertsDetailsTable').DataTable().ajax.url(newUrl).load();
    $(".yadcf-filter-wrapper").each(function () {
        $(this).children().val(null);
        $(this).children().removeClass("inuse");
    });
    $.Notification.notify('success', 'top right', "Success", "Temporary view is created, with cases filtered based on the case series available in the clipboard", {autoHideDelay: 5000});

    $("#view-types-menu").find("li").addClass("disabled");
    $("#view-types-menu").find("li").find('a').off('click');
    $("#view-types-menu").find("li").find('a').removeAttr("href");
    $("#saveViewTypes").find("li").addClass("disabled");
    $("#saveViewTypes").find("li").find('a').off('click');
    $("#saveViewTypes").find("li").find('a').removeAttr("href");
    $(".alert-check-box").prop('checked', true)
    $(".alert-select-all").prop('checked', true)
    $(".alert-check-box").prop('checked', true)
    $(".copy-select:checked").each(function () {
        alertIdSet.add($(this).attr('data-id'));
        if($(this).is(':checked')){
            if(selectedCases.indexOf($(this).attr("data-id")) == -1 && $(this).is(':checked')){
                selectedCases.push($(this).attr("data-id"));
                var selectedRowIndex = $(this).closest('tr').index();
                if (isAbstractViewOrCaseView(selectedRowIndex)) {
                    selectedRowIndex = selectedRowIndex / 2
                }
                selectedCasesInfo.push(populateDispositionDataFromGrid(selectedRowIndex));
            } else if(selectedCases.indexOf($(this).attr("data-id")) != -1 && !$(this).is(':checked')){
                selectedCases.splice( $.inArray($(this).attr("data-id"), selectedCases), 1 );
            }
        }
    });
}

function set_disable_click(element, status, name){
    if(status == 1){
        $(element).find('span').text(name);
        $(element).closest('li').addClass("disabled");
        $(element).find('span').addClass("generation-tooltip")
        $(element).find('span').attr('data-title', "The data analysis generation is in progress")
    }
};

function generate_analysis(element, spotfireDateRange, text, checker) {
    var exStatus = $(element).attr('data-status')
    if (exStatus == 0) {
        var executedConfigId = $(element).attr('data-id');
        $(element).attr('data-status', 1)
        $(element).find('span').text(text);
        $(element).find('span').addClass("generation-tooltip")
        $(element).find('span').attr('data-title', "The data analysis generation is in progress")
        $(element).closest('li').addClass("disabled");
        $.ajax({
            url: '/signal/dataAnalysis/generateDataAnalysis?executedConfigId=' + executedConfigId + '&spotfireDateRange=' + spotfireDateRange + '&analysisPeriod=' + checker
        })
            .done(function (data) {
                if (data[0] == "Error") {
                    $.Notification.notify('error', 'top right', "Error", "An Error occurred while Processing request.", {autoHideDelay: 5000});
                }
            })
            .fail(function (exception) {
                console.log(exception)
                $.Notification.notify('error', 'top right', "Error", "An Error occurred while Processing request.", {autoHideDelay: 5000});
            });
    }
};

function updateTempView(isDeleted) {
    $.ajax({
        url: "/signal/viewInstance/updateTempView",
        data: {isDeleted: isDeleted},
    });
}

function fetchIfTempAvailable() {
    if(($('#isFaers').length == 0 || $('#isFaers').val() == "" || $('#isFaers').val() == "false") && (isArchived == "false" || isArchived==false)) {
        $.ajax({
            url: "/signal/viewInstance/fetchIfTempAvailable",

        })
            .done(function (result) {
                if (result.status == 200 && result.instanceId != null) {
                    tempViewPresent = result.instanceId
                    showTempViewModal(true)
                }
                else {
                    $("#clipboardPopup").modal('hide');
                }
            });
    }
}


$(document).on('click', '.commentOldPopup .mdi-dots-horizontal', function (e) {
    e.preventDefault();
    var currRow = $(this).closest('tr');
    showCommentPopup(currRow, 2, "Old Comment");
});

$(document).on('click', '.commentNewPopup .mdi-dots-horizontal', function (e) {
    e.preventDefault();
    var currRow = $(this).closest('tr');
    showCommentPopup(currRow, 3, "New Comment");
});

function showCommentPopup(currRow, index, label){
    var extTextAreaModal = $("#comment-ext1");
    extTextAreaModal.find('.commentValue').html(currRow[0].cells[index].children[0].getAttribute("data"));
    extTextAreaModal.find('.modal-title').text(label);
    extTextAreaModal.modal("show");
}

function updateQueryStringParameter(uri, key, value) {
    var re = new RegExp("([?&])" + key + "=.*?(&|$)", "i");
    var separator = uri.indexOf('?') !== -1 ? "&" : "?";
    if (uri.match(re)) {
        return uri.replace(re, '$1' + key + "=" + value + '$2');
    }
    else {
        return uri + separator + key + "=" + value;
    }
}

function exportReport(){

    var sorting = "asc";
    var column = ""
    var element;
    if($("#alertsDetailsTableRow").find($(".dt-ordering-desc")).length){
        element = $("#alertsDetailsTableRow").find($(".dt-ordering-desc"))[0];
        sorting = "desc"
        column = element.getAttribute("data-field");
    }else if($("#alertsDetailsTableRow").find($(".dt-ordering-asc")).length){
        element = $("#alertsDetailsTableRow").find($(".dt-ordering-asc"))[0];
        sorting = "asc"
        column = element.getAttribute("data-field");
    }
    $(".export-report").each(function (e) {
        var currentUrl = $(this).attr("href");
        currentUrl = typeof currentUrl !== 'undefined' ? updateQueryStringParameter(currentUrl, 'column', column) : "";
        currentUrl = typeof currentUrl !== 'undefined' ? updateQueryStringParameter(currentUrl, 'sorting', sorting) : "";
        $(this).attr("href", currentUrl);
    })

}


$(function(){
    const sanitizerAllowList = $.fn.tooltip.Constructor.DEFAULTS.whiteList;
    sanitizerAllowList.textarea = [];
    sanitizerAllowList.table = [];
    sanitizerAllowList.tr = [];
    sanitizerAllowList.th = [];
    sanitizerAllowList.i = [...sanitizerAllowList.i, 'data-id'];
    $("#alertsDetailsTableRow th").on('click', function() {
        var column = $(this).attr("data-field");
        var sorting
        if($(this).hasClass("dt-ordering-asc"))
        {
            sorting="desc"
        }else if($(this).hasClass("dt-ordering-desc"))
        {
            sorting="asc"
        }else{
            sorting="asc"
        }

        $(".export-report").each(function (e) {
            var currentUrl = $(this).attr("href");
            currentUrl = typeof currentUrl !== 'undefined' ? updateQueryStringParameter(currentUrl, 'column', column) : "";
            currentUrl = typeof currentUrl !== 'undefined' ? updateQueryStringParameter(currentUrl, 'sorting', sorting) : "";
            $(this).attr("href", currentUrl);
        })
    });

    $(".export-report").on('click', function (e) {
        e.preventDefault();
        exportReport();
    });


});

$(document).on("shown.bs.dropdown", ".dropdown", function () {
    $( ".undo-alert-disposition" ).on( "click", popoverUndoAlert );
});

$(document).on("hidden.bs.dropdown", ".dropdown", function () {
    $( ".undo-alert-disposition" ).off( "click", popoverUndoAlert );
    if(currentAlertDisp){currentAlertDisp.popover('hide');}
});
var currentAlertDisp;

function popoverUndoAlert(e){
    e.preventDefault();
    e.stopPropagation();
    var id = $(this).closest('.undo-alert-disposition').attr('data-id');
    if(typeof selectedCases !== "undefined" && (selectedCases.length < 1 || (selectedCases.length === 1 && selectedCases[0] === id))){
        currentAlertDisp = $(this);
        currentAlertDisp.popover('toggle');
        addCountBoxToInputField(8000, $(".popover").find('textarea'));
    } else {
        var element = $(this).closest('.undo-alert-disposition')[0]; //Added for PVS-71182
        if (element && !window.getComputedStyle(element).cursor.includes('not-allowed')) {
            $.Notification.notify('warning', 'top right', "Warning", "Undo disposition change is not possible when multiple rows are selected", {autoHideDelay: 5000});
        }
    }
}

$(document).on('click',".popover-parent .popover ",function (e) {
    e.preventDefault();
    e.stopImmediatePropagation();
    return false;
});

function getPECsTable(alertType) {
    if (alertType === ALERT_CONFIG_TYPE.AGGREGATE_CASE_ALERT || alertType === ALERT_CONFIG_TYPE.SINGLE_CASE_ALERT || alertType === ALERT_CONFIG_TYPE.EVDAS_ALERT || alertType === ALERT_CONFIG_TYPE.LITERATURE_SEARCH_ALERT || alertType === ALERT_CONFIG_TYPE.SINGLE_CASE_ALERT_ON_DEMAND || alertType === ALERT_CONFIG_TYPE.AGGREGATE_CASE_ALERT_ON_DEMAND || alertType === ALERT_CONFIG_TYPE.ADHOC_ALERT) {
        return table.rows().data();
    } else if (alertType !== ALERT_CONFIG_TYPE.ADHOC_ALERT) {
        return tableSingleReview.rows().data();
    }

    return table.rows(index).data();
}



