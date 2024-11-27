//= require app/pvs/common/rx_common.js
//= require app/pvs/common/rx_alert_utils.js
//= require app/pvs/alerts_review/alert_review.js
//= require app/pvs/alertComments/alertComments.js
//= require app/pvs/actions/actions.js
//= require app/pvs/caseHistory/caseHistoryTable.js
//= require app/pvs/validated_signal/signal_charts.js
//= require app/pvs/common_tag.js
var applicationName = "Single Case Alert";
var applicationLabel = "Case Detail";
var appType = "Single Case Alert";
var versionAvailable = undefined;
var versionCompare = undefined;
var followUpAvailable = undefined;
var followUpCompare = undefined;
var outcomeInput;
var outcomeResult;

$(function () {

    if($('#isAggAdhoc').val() === "true" &&  $("#isCaseSeries").val() === "true"){
        $(".changeDisposition").css({
            "pointer-events": "none",
            "cursor": "not-allowed"
        });
    }
    $(function () {

        var absentValue;
        var treeNodesUrlVal
        var caseReferences = $("tbody td.referenceNumber");
        caseReferences.each(function(index,ele){
            var row = $(this).parents('tr');
            row.find("td.referenceNumber").html($(ele).text());
        });
        $('tbody td.referenceNumber a').attr('target', '_blank');

        if(typeof isVaers !== "undefined" && isVaers === "true"){
            absentValue = $("#absentValue").val().split(",");
            treeNodesUrlVal = treeNodesUrl+ "&absentValue="+absentValue;
            $('#caseDetailTree').on('click', '.jstree-anchor', function (e) {
                $('#caseDetailTree').jstree(true).toggle_node(e.target);
            }).jstree({
                'core': {
                    'data': {
                        'url': treeNodesUrlVal
                    },
                    'dblclick_toggle': false
                }
            });

            $('#caseDetailTree').on('loaded.jstree', function () {
                $("#caseDetailTree .jstree-anchor").each(function () {
                    var val = $(this).attr('id').replace("anchor", "container");
                    $(this).attr('href', '#' + val)
                });
                var scrollDuring = 1000;
                var scrollBegin = 163;
                $('a.jstree-anchor').on('click', function () {
                    $('#case_detail_container').collapse('show');
                    if (location.pathname.replace(/^\//, '') === this.pathname.replace(/^\//, '') && location.hostname === this.hostname) {
                        var $targetid = $(this.hash);
                        $targetid = $targetid.length && $targetid || $('[id=' + this.hash.slice(1) + ']');
                        $($targetid.selector).collapse('show');
                        if ($targetid.length) {
                            var targetOffset = $targetid.offset().top - scrollBegin;
                            $('html,body').animate({scrollTop: targetOffset}, scrollDuring);
                            $("a").removeClass("active");
                            $(this).addClass("active");
                            return false;
                        }
                    }
                })
            });
        } else {
            $('#caseDetailTree').on('click', '.jstree-anchor', function (e) {
                $('#caseDetailTree').jstree(true).toggle_node(e.target);
            }).jstree({
                'core': {
                    'data': sectionNameList,
                    'dblclick_toggle': false
                }
            });

            $("#caseDetailTree .jstree-anchor").each(function () {
                var val = $(this).attr('id').replace("anchor", "container").replace("(","").replace(")","");
                $(this).attr('href', '#' + val)
            });
            var scrollDuring = 1000;
            var scrollBegin = 143;
            $('a.jstree-anchor').on('click', function () {
                $('#case_detail_container').collapse('show');
                if (location.pathname.replace(/^\//, '') === this.pathname.replace(/^\//, '') && location.hostname === this.hostname) {
                    var $targetid = $(this.hash);
                    $targetid = $targetid.length && $targetid || $('[id=' + this.hash.slice(1) + ']');
                    $($targetid.selector).collapse('show');
                    if ($targetid.length) {
                        var targetOffset = $targetid.offset().top - $targetid.parent().find(".rxmain-container-header").height() - scrollBegin;
                        $('html,body').animate({scrollTop: targetOffset}, scrollDuring);
                        $("a").removeClass("active");
                        $(this).addClass("active");
                        return false;
                    }
                }
            });
        }

        $('#caseDetailTree').on("dblclick.jstree", function (event) {
            var node = $(event.target).closest("li");
            var $targetid = node[0].id;
            $targetid = $targetid.length && $targetid || $('[id=' + this.hash.slice(1) + ']');
            $targetid = "#"+$targetid+"_container";
            $($targetid).collapse('hide');
        });

        $('#caseDetailTree').on("changed.jstree", function (e, data) {
            window.selectedId = data.selected;
            var containerText = getContainerText(data.node.text);
            $('#' + containerText).collapse('toggle');
        });

        $('.collapse').collapse('toggle');

        function getContainerText(text) {
            return text.toLowerCase().replace(/ /g, "_") + "_container";
        }

        $(document).on('click', '.caseNumberRef', function (e) {
            e.preventDefault();
            var scaAlertId = encodeToHTML($(this).attr("data-alertId"));
            var caseVersion = encodeToHTML($(this).attr("data-caseVersion"));
            var caseNumber = encodeToHTML($(this).attr("data-caseNumber"));
            var followUpNumber = encodeToHTML($(this).attr("data-followUpNumber"))
            $.ajax({
                url: updateAutoRouteDispositionUrl,
                data: {'id': scaAlertId},
            })
                .done(function (result) {
                    signal.utils.postUrl(caseDetailUrl, {
                        caseNumber: caseNumber,
                        isCommentsEditEnable: isCommentsEditEnable, // added to check comments flag for PVS-63960
                        version: caseVersion,
                        followUpNumber: followUpNumber,
                        alertId: scaAlertId,
                        isFaers: isFaers,
                        isVaers: isVaers,
                        isVigibase: isVigibase,
                        isJader: isJader,
                        fullCaseList: $("#fullCaseList").val(),
                        detailsParameters: $("#detailsParameters").val(),
                        isArchived: isArchived,
                        totalCount: $("#totalCount").val(),
                        execConfigId: $("#execConfigId").val(),
                        isSingleAlertScreen: $("#isSingleAlertScreen").val(),
                        isAdhocRun: $(".isAdhocRun").val(),
                        isCaseSeries: $("#isCaseSeries").val(),
                        oldFollowUp  : $("#oldFollowUp").val(),
                        oldVersion   : $("#oldVersion").val(),
                        isStandalone : result.data == "isStandalone"? true:false
                    });
                })
                .fail(function () {
                    console.error("Some error occured while updating AutoRoute Disposition");
                });
        });
    });
    $(document).on('click', '.linked-child-case', function (e) {
        e.preventDefault();
        openCaseDetailsPageForChildCase($(this).data("casenumber"))
    });
    addCountBoxToInputField(8000,$('#attachmentDescription'));
    $("#attachmentForm input[type=file]").on("change", function () {
        var imgVal = $(this).val();
        var imgSize;
        if (imgVal != '') {
            imgSize = $(this)[0].files[0].size;
        }
        if (imgVal != '' && imgSize < 20000000) {
            $('#fileSizeMessage').hide()
            $("[name=_action_upload]").attr('disabled', false)
        } else if (imgVal != '') {
            $('#fileSizeMessage').show();
            $("[name=_action_upload]").attr('disabled', true);
        } else {
            $("[name=_action_upload]").attr('disabled', true);
        }
    });


    $("form#attachmentForm").off('submit').on('submit', function () {
        var formData = new FormData(this);
        $("form#attachmentForm input[type=submit]").attr('disabled', true);
        $.ajax({
            url: getUploadUrl,
            type: "POST",
            data: formData,
            async: true,
            cache: false,
            contentType: false,
            processData: false
        })
            .done(function () {
                $('#attachmentCaseDetailModal').modal('hide');
                $('#attachment-table').DataTable().ajax.reload();
            })
            .fail(function () {
                $("form#attachmentForm input[type=submit]").attr('disabled', false);
                $.Notification.notify('error', 'top right', "Error", "Sorry, This File Format Not Accepted", {autoHideDelay: 5000});
            })
        return false;
    });


    $('#attachmentCaseDetailModal').on('hidden.bs.modal', function (e) {
        $(this)
            .find("input[type=file],input[type=text]")
            .val('')
            .end()
            .find("[name=_action_upload]")
            .attr('disabled', true)
            .end();
        $("#fileSizeMessage").css({display:"none"});
    });

    $('#attachment-table').DataTable({
        destroy: true,
        searching: false,

        responsive: false,
        language: {
            "url": "../assets/i18n/dataTables_" + userLocale + ".json",
            "lengthMenu": 'Show _MENU_ entries'
        },
        "ajax": {
            "url": getAttachmentUrl,
            "dataSrc": ""
        },
        drawCallback: function () {
            $('.remove-attachment').on('click', function (e) {
                var attachmentRow = $(e.target).closest('tr');
                var attachmentId = attachmentRow.find('span[data-field="removeAttachment"]').attr("data-attachmentId");
                var removeUrl = encodeURI(deleteAttachmentUrl + '&attachmentId=' + encodeToHTML(attachmentId)+ '&isArchived=' + isArchived);
                $.ajax({
                    type: "POST",
                    url: removeUrl,
                    beforeSend: function () {
                        $('.remove-attachment').css('pointer-events', 'none'); // Disables the span element
                        $('.remove-attachment').css('opacity', '0.5');
                    },
                })
                    .done(function (result) {
                        if(result.success) {
                            $.Notification.notify('success', 'top right', "Success", "Attachment deleted successfully.", {autoHideDelay: 5000});
                            $('#attachment-table').DataTable().ajax.reload();
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
        "aoColumns": [
            {
                "mData": "name",
                "width": "10%",
                'className': 'dt-center',
                "mRender": function (data, type, row) {
                    return '<a href="/signal/attachmentable/download?id=' + row.id + '">' + encodeToHTML(row.name) + '</a>'
                }
            }, {
                "mData": "description",
                'className': 'dt-center',
                "width": "35%",
                "mRender": function (data, type, row) {
                    return encodeToHTML(row.description)
                }
            }, {
                "mData": "timeStamp",
                "width": "10%",
                'className': 'dt-center',
                "mRender": function (data, type, row) {
                    if(row.timeStamp=="null" || row.timeStamp==null){
                        return '' ;
                    } else {
                        return moment.utc(row.timeStamp).format('DD-MMM-YYYY hh:mm:ss A');
                    }
                }
            }, {
                "mData": "modifiedBy",
                "width": "10%",
                'className': 'dt-center'
            }, {
                "mData": "id",
                "width": "10%",
                'className': 'dt-center',
                "sortable": false,
                "mRender": function (data, type, row) {
                    return '<span tabindex="0" title="Remove Attachment" class="glyphicon glyphicon-remove remove-attachment" style="cursor: pointer" data-field="removeAttachment" data-attachmentId=' + row.id + '></span>'
                },
                "visible": isVisible()
            }
        ],
        "bLengthChange": false,
        columnDefs: [{
            "targets": '_all',
            "render": $.fn.dataTable.render.text(),
            orderSequence: ['desc', 'asc']
        }]
    });

    function isVisible() {
        var isVisible = true;
        if(typeof hasReviewerAccess !== "undefined" && !hasReviewerAccess) {
            isVisible = false;
        }
        return isVisible
    }

    var checkedIdList = [];
    var checkedRowList = [];
    signal.alertReview.openAlertCommentModal("Single Case Alert", applicationName, applicationLabel, checkedIdList, checkedRowList);

    var caseJson = {
        "alertType": "Single Case Alert",
        "productFamily": encodeToHTML($(".comment").find("#prodFamily").html()),
        "configId": $(".comment").find("#configId").html(),
        "caseNumber": encodeToHTML($(".comment").find("#caseNo").html()),
        "alertName": $(".comment").find("#alertName").html(),
        "caseId": $(".comment").find("#caseId").html(),
        "versionNum": $(".comment").find("#versionNum").html(),
        "caseVersion": $(".comment").find("#versionNum").html(),
        "followUpNumber": $(".comment").find("#followUpNumber").html(),
        "exConfigId": $(".comment").find("#executedConfigId").html(),
    };
    signal.alertComments.populate_comments($(".comment"), caseJson);
    signal.alertComments.save_comment($(".comment"));
    signal.actions_utils.init_action_table($('#action-table'), appType);
    initRelatedCaseSeriesModal();
    signal.alertReview.enableMenuTooltips();
    signal.alertReview.disableTooltips();
    initCaseHistorySection();
    bindAssignedToSelect();
    zoomIn();
    zoomOut();
    refreshZoom();
    comparePreviousVersions();
    $('#assignedTo').append(option).trigger('change');
    $('.assignedToValue').append(option).trigger('change');

    var caseNo=$('.duplicateCases').text().split(',');
    $('.duplicateCases').text('');

    caseNo.forEach(function (value) {

        var tag = document.createElement('a');
        var caseNumber = value.substring(0,value.indexOf('('));
        type = (value.indexOf('(M)')>0)?'M':'';
        var version = value.substr(value.indexOf('(')+1,1);
        var isAdhocRun = $("input[name='isAdhocRun']").val()
        var pushVal = '/signal/caseInfo/caseDetail?caseNumber='+encodeToHTML(caseNumber)+'&version='+version+'&followUpNumber=&isFaers='+$("#isFaers").val()+'&isVaers='+$("#isVaers").val()+'&isVigibase='+$("#isVigibase").val()+'&isJader='+$("#isJader").val()+'&isDuplicate='+type+'&isAdhocRun='+isAdhocRun;
        tag.href = pushVal;
        tag.text = value;
        tag.className = 'badge badge-info';
        $('.duplicateCases').append(tag);
        $('.duplicateCases').append("&nbsp;&nbsp;");

    });

    $('.outcome-value').hover(function () {
        var abbreviatedOutcome = $(this).text();
        var currentValue = $(this);
        if(typeof outcomeInput != 'undefined' && typeof outcomeResult != 'undefined' && abbreviatedOutcome === outcomeInput){
            $(currentValue).attr("title", outcomeResult);
        } else {
            $.ajax({
                url: fetchOutcomeUrl,
                data: {"abbreviatedOutcome": abbreviatedOutcome},
            })
                .done(function (result) {
                    outcomeInput = abbreviatedOutcome;
                    outcomeResult = result;
                    if(result){
                        $(currentValue).attr("title", result);
                    } else {
                        $(currentValue).attr("title", abbreviatedOutcome);
                    }
                });
        }
    })

});

var bindAssignedToSelect = function() {
    $('#assignedTo, .assignedToValue').on("select2:opening", function (e) {
        if(typeof hasReviewerAccess !== "undefined" && !hasReviewerAccess){
            e.preventDefault();
            $.Notification.notify('warning', 'top right', "Warning", "You don't have access to perform this action", {autoHideDelay: 5000});
        }
    });
    $('#assignedTo, .assignedToValue').on("select2:selecting", function(e) {
        var value = e.params.args.data.id;
        $.ajax({
            url: changeAssignedToUrl,
            data:{
                assignedToValue: value,
                isArchived: $('#isArchived').val(),
                selectedId: JSON.stringify([parseInt($('#alertId').val())])
            }
        }).done(function (payload) {
            if(payload.status){
                $.Notification.notify('success','top right', "Success", payload.message, {autoHideDelay: 5000});
            }else{
                $.Notification.notify('error','top right', "Error", payload.message, {autoHideDelay: 5000});
            }
        });
    });
};
var initRelatedCaseSeriesModal = function () {
    $("#relatedCaseSeries").off().on('click', function () {
        var caseDiffModal = $('#relatedCaseSeriesModal');
        caseDiffModal.modal('show');
        caseDiffModal.on('shown.bs.modal', function (evt) {
            $('#relatedCaseSeriesTable').DataTable({
                "destroy": true,
                "aaSorting": [[4, "desc"]],
                "searching": true,
                "bLengthChange": true,
                "iDisplayLength": 5,
                "aLengthMenu": [[5, 10, 20, 50, -1], [5, 10, 20, 50, "All"]],
                language: {
                    "url": "../assets/i18n/dataTables_" + userLocale + ".json"
                },
                "ajax": {
                    "url": relatedCaseSeriesUrl,
                    "dataSrc": "aaData",
                    "data": function (data) {
                        data.searchString = data.search.value;
                    }
                },
                serverSide: true,
                "aoColumns": [
                    {
                        "mData": "name",
                        "mRender": function (data, type, row) {
                            if (row.isAlertVisible) {
                                return  '<a target="_blank" href="' + signal.utils.composeUrl('singleCaseAlert', 'details', {callingScreen: 'review', configId: row.alertId}) + '" class="alertDetailLink" data-alert-id=' + row.alertId + '>' + encodeToHTML(row.name) + '</a>'
                            } else {
                                return '<span title="User does not have permission to view the alert" >' + encodeToHTML(row.name) + '</span>'
                            }
                        },
                        "className":'col-min-150'
                    },
                    {
                        "mData": "productSelection",
                        "className":'col-min-150',
                        "sortable": false,
                    },
                    {
                        "mData": "description",
                        "className":'col-min-150'
                    },
                    {
                        "mRender": function (data, type, row) {
                            var criteria = '<b>Date Range :</b>' + " " + row.dateRange;
                            if(row.query){
                                criteria += '<br/><b>Query :</b>' + " " + row.query;
                            }
                            return criteria
                        },
                        "className":'col-min-250',
                        "sortable": false,

                    },
                    {
                        "mData": "lastExecuted",
                        "className":'col-min-100'

                    }
                ],
                "scrollX":true,
                columnDefs: [{
                    "targets": '_all',
                    "render": $.fn.dataTable.render.text(),
                    orderSequence: ['desc', 'asc']
                }]
            });
        });

    });
};

var initCaseSeriesDetailsLink = function () {
    $('.alertDetailLink').off().on("click", function () {
        var alertDetailsUrl = detailsUrl + '&configId=' + $(this).data('alert-id');
        window.open(alertDetailsUrl, '_blank');
    });
};

var initCaseHistorySection = function () {
    signal.caseHistoryTable.init_case_history_table(caseHistoryUrl,isArchived);
    signal.caseHistoryTable.init_case_history_table_suspect(caseHistorySuspectUrl);
};

function getProductJson() {
    var productName = $('#productName').val();
    var level = 3;
    return generateProductJson(productName, level);
}

function generateProductJson(productName, level) {
    var signalProductValues = {"1": [], "2": [], "3": [], "4": [], "5": []};
    if (level != ASSESSMENT_DRUG_REC_NUM_LEVEL) {
        signalProductValues[level].push({name: productName});
    }
    return signalProductValues;
}

$(function () {

    var categories=$('#categoryList').val();
    if(categories){
        var catObj= JSON.parse(categories);
        $('#categories').append('<div class="col-max-300 pos-rel">'+signal.alerts_utils.get_tags_element(catObj)+'</div>');
    }

});
function catEllipsis() {
    tagEllipsis($("#workflow_and_categories_management_container"));
    webUiPopInitForCategories();
    webUiPopInit();
    closePopupOnScroll();
}

var tags =[]
$(document).on('click', '.editAlertTags', function () {
    $('#commonTagModal').find('#addTags').attr('disabled', true);
    fetchTags();
    var commonTagModalObj = $('#commonTagModal')
    var alertId = $('#catAlertId').val();
    if(alertId != null && alertId != undefined && alertId != "") {
        var domain = $('#isAdhocRun').val() == 'false' || !$('#isAdhocRun').val() ? 'Qualitative' : "Qualitative on demand"
        var module = 'PVS'
        var caseVersion
        // Added for manually added case
        var isStandalone = $('#isStandalone').val() == 'true' ? true :false
        if(isStandalone){
            caseVersion = $("#caseVersionIsStandalone").val()
        }else{
             caseVersion = $('#isAdhocRun').val() == 'false' || !$('#isAdhocRun').val() ? $("#caseVersion").val() : $("#adHocVersionNum").val();
        }
        var isPrevAlertArchived = $('#isFoundAlertArchived').val();
        var prevAlertId = $('#foundAlertId').val();
        fillAlertTags(commonTagModalObj, fillCommonTagUrl, module, domain, tags, alertId, caseVersion, isPrevAlertArchived, prevAlertId);
        commonTagModalObj.modal('show');
        if ($(this).attr('aria-describedby')) {
            $('#' + $(this).attr('aria-describedby')).hide();
        }
        tags = [];
    }

});

var fetchTags = function () {
    $.ajax({
        url: fetchCommonTagsUrl,
        async: false,
    })
        .done(function (result) {
            result.commonTagList.forEach(function(map){
                tags.push({
                    id: map.id,
                    text: map.text,
                    parentId: map.parentId,
                    display: map.display
                });
            });
        });
};



var fillAlertTags = function (tagModal, fillCommonTagUrl, module, domain, tags, alertId,caseVersion,isPrevAlertArchived,prevAlertId) {
    var dataUrl = fillCommonTagUrl + "/"+module+"?domain="+domain + "&alertId="+alertId + '&isArchived=' + isArchived+'&caseVersion='+caseVersion + '&isPrevAlertArchived='+isPrevAlertArchived+'&prevAlertId='+prevAlertId;
    var isStandalone = $('#isStandalone').val() == 'true' ?  true :false
    var disabledSubCatList = [];
    var isCategoryEditable=$('#isCategoryEditable').val();
    var disabledCategoryList = [];
    categoryModalTable = $("#categoryModalTable").DataTable({
        "language": {
            "url": "../assets/i18n/dataTables_" + userLocale + ".json"
        },
        "createdRow": function ( row, data, index ) {
            var tagsEnabled = nonConfiguredEnabled != "true" ? false : true;
            var categories = [];
            tags.forEach(function (map) {
                if (map.id == data.category.id) {
                    disabledCategoryList.push(map.id);
                }
            });
            tags.forEach(function (map) {
                if(map.parentId ==0 || map.parentId == null) {
                    if (disabledCategoryList.includes(map.id)) {
                        categories.push({id: map.id, text: map.text, disabled: true});
                    } else {
                        categories.push({id: map.id, text: map.text, disabled: map.display == 0 ? true : false});
                    }
                }
            });
            $('select', row).eq(0).select2({
                tags: tagsEnabled,
                data: categories,
                placeholder: "Select Category"
            }).on('change', function(obj) {

                var subtagsEnabled = nonConfiguredEnabled != "true" ? false : true;
                var subcategories = [];
                tags.forEach(function (map) {
                    if(map.parentId == obj.currentTarget.value) {
                        var isDisabledSubCat = map.display == 0 ? true : false
                        subcategories.push({id: map.id, text: map.text, disabled:isDisabledSubCat });
                        if(isDisabledSubCat)
                            disabledSubCatList.push(map.id)

                    }
                });

                if(data.action == 1) {
                    $('select', row).eq(1).empty();
                }

                // if no subcategory configure then can not add on fly subcategory
                if(subcategories.length <= 0){
                    subtagsEnabled = false;
                }else{
                    subtagsEnabled = (subtagsEnabled && true);
                }

                $('select', row).eq(1).select2({
                    tags: subtagsEnabled,
                    data: subcategories,
                    placeholder: "Select Sub Category"
                });
            }).trigger('change');
            $('select', row).eq(1).on('change', function (e) {
                setSelectWidth($(this));
            });

            if(data.action != 1) {
                $('input', row).closest('tr').find(":input").attr('disabled', true);
                if(data.subcategory.length <= 0)
                    $('input', row).closest('tr').find(":input[class=select2-search__field]").attr("placeholder", "");
            }

            $(".edit-row", row).off().on('click', function(){
                if(data.autoTaggedEditable == false){
                    return false;
                }
                var isDisabled = $(this).closest('tr').find(":input:not(:first)").attr('disabled');
                $(this).closest('tr').find(":input:not(:first)").attr('disabled', !isDisabled);
                if(isDisabled && data.subcategory.length <= 0)
                    $(this).closest('tr').find(":input[class=select2-search__field]").attr("placeholder", "Select Sub Category").css("width", "100%");
                else if(isDisabled && data.subcategory.length > 0)
                    $(this).closest('tr').find(":input[class=select2-search__field]").attr("placeholder", "");
                else if(!isDisabled && data.subcategory.length <= 0)
                    $(this).closest('tr').find(":input[class=select2-search__field]").attr("placeholder", "");
                var isPrivate = $(this).closest('tr').find(":input[class=private-check-box]").attr("data-id");
                // Addded if case is added manually
                if(isPrivate == 'false' || isStandalone)
                    $(this).closest('tr').find(":input[class=private-check-box]").attr("disabled",true)
                if(isStandalone){
                    $(this).closest('tr').find(":input[class=alert-check-box-tag]").attr("disabled",true)
                }
                $('#commonTagModal').find('#addTags').attr('disabled', false);

            });

            $(".delete-row", row).off().on('click', function(){
                var rowId = $("select", row).val();
                var indexToRemove = disabledCategoryList.indexOf(parseInt(rowId));
                if (indexToRemove !== -1) {
                    disabledCategoryList.splice(indexToRemove, 1);
                }
                if(data.autoTaggedEditable == false){
                    return false;
                }
                categoryModalTable
                    .row( $(this).parents('tr') )
                    .remove()
                    .draw();
                $('#commonTagModal').find('#addTags').attr('disabled', false);
            });

            $(".up-row", row).off().on('click', function(){
                const $row = $(this).parents('tr');
                if ($row.index() === 0) {
                    return;
                }
                $row.prev().before($row.get(0));
            });

            $(".down-row", row).off().on('click', function(){
                const $row = $(this).parents('tr');
                $row.next().after($row.get(0));
            });

            if(data.autoTaggedEditable == false){
                $('input', row).closest('tr').find(".edit-row,.delete-row").css('color', "rgb(183, 183, 183)");
                $('input', row).closest('tr').find(".edit-row,.delete-row").css('cursor', "not-allowed");
            }

        },
        drawCallback: function (settings) {
            setSelectWidth($(".dynamicSelectFieldSubCategory"));
        },
        rowCallback: function( row, data, index ) {
            if (data['private'] == true && data['privateAccess'] != true) {
                $(row).hide();
            }
        },
        initComplete: function(settings, json){
            existingRows = [];
            categoryModalTable.rows().every(function(index, element){
                var value = this.data();
                var currentRow = {category: value.category, subcategory: value.subcategory, alert: value.alert, private: value.private,
                    createdBy: value.createdBy, createdDate: value.createdDate, priority: value.priority, updatedDateInit: value.updatedDateInit,
                    updatedByInit: value.updatedByInit, createdDateInit: value.createdDateInit,
                    createdByInit: value.createdByInit, autoTagged: value.autoTagged, isRetained: value.isRetained,
                    privateUserId: value.privateUserId,
                    execConfigId: value.execConfigId};
                if(currentRow.category.id != 0)
                    existingRows.push(currentRow);
            });
            if(isCategoryEditable=="false" || (isArchived == "true" && domain == "Qualitative")) {
                $(".delete-row").hide()
                $(".edit-row").hide()
                $('#add-row').hide()
                $('#addTags').hide()
            }
            disabledSubCatList.forEach(function (value, index1) {
                $('.dynamicSelectFieldSubCategory').find('option[value="' + value + '"]').prop("disabled", true);
            });
            categoryMaximumSize(nonConfiguredEnabled);
        },
        "ajax": {
            "url": dataUrl,
            "dataSrc": ""
        },
        searching: true,
        "bLengthChange": true,
        "paging":   false,
        "ordering": false,
        "searching": false,
        "info": false,
        "bDestroy": true,
        stateSave: true,
        "scrollY":        "350px",
        "scrollCollapse": true,
        "aoColumns": [
            {
                "mData": "category",
                "width": "25%",
                'className': 'dt-center',
                "mRender": function (data, type, row) {
                    var html = '<select name="selectField" id="dynamicSelectFieldCategory" class="dynamicSelectFieldCategory form-control">';
                    if(data) {
                        html += '<option selected="selected" value="' + data.id + '">' + encodeToHTML(data.name) + '</option>';
                    }
                    html += '</select>';
                    return html;
                }
            },
            {
                "mData": "subcategory",
                'className': 'dt-center',
                "width": "35%",
                "mRender": function (data, type, row) {

                    var html = '<select name="selectField" id="dynamicSelectFieldSubCategory" multiple="multiple" class="dynamicSelectFieldSubCategory form-control">';
                    row.subcategory.forEach(function (obj) {
                        html += '<option selected="selected" value="'+obj.id+'">'+encodeToHTML(obj.name)+'</option>';
                    });
                    html += '</select>';
                    return html

                }
            },
            {
                "mData": "alert",
                "className": "justification-cell dataTableColumnCenter text-center",
                "width": "15%",
                "mRender": function (data, type, row) {
                    var checked = row.alert == true ? "checked": "";
                    if(isStandalone) {
                        return '<input type="checkbox"  disabled="disabled" class="alert-check-box-tag" '  + checked + ' data-id=' + row.alert + ' />';
                    }else{
                        return '<input type="checkbox" class="alert-check-box-tag" ' + checked + ' data-id=' + row.alert + ' />';
                    }

                }
            },
            {
                "mData": "private",
                "width": "12%",
                "mRender": function (data, type, row) {
                    var checked = row.private == true ? "checked": "";
                    if(isStandalone) {
                        return '<input type="checkbox" disabled="disabled" class="private-check-box" ' + checked  +' data-id=' + row.private + ' />';
                    }else{
                        return '<input type="checkbox" class="private-check-box" ' + checked  +' data-id=' + row.private + ' />';
                    }
                },
                'className': 'dt-center dataTableColumnCenter text-center'
            },
            {
                "mData": "action",
                "width": "13%",
                "className": "td-action",
                "mRender": function (data, type, row) {
                    var saveCategoryAccessVar = "";
                    if(typeof saveCategoryAccess !== "undefined"){
                        saveCategoryAccessVar = saveCategoryAccess;
                    }
                    if(data === 1){
                        return '<i class="fa fa-trash-o delete-row tag-action"></i>';
                    }
                    else {

                        var details = '<b>Created By:</b> '+row.createdBy +'<hr/> <b>Created Date:</b> '+row.createdDate +'<hr/> <b>Last Modified By:</b> '+row.updatedBy
                            +'<hr/> <b>Last Modified Date:</b> '+row.updatedDate;
                        return '<i id="tag-popup" class="glyphicon glyphicon-info-sign themecolor popoverMessage tag-action" title="Details" data-content="' + details + '"></i>' +
                            '  <i class="fa fa-pencil edit-row tag-action '+ saveCategoryAccessVar+'" ></i>' +
                            '  <i class="fa fa-trash-o delete-row tag-action '+ saveCategoryAccessVar+'"></i>';
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
    $( "#categoryModalTable" ).sortable({
        items: 'tr:not(:first)',
        opacity: 0.6,
        scrollSpeed: 40,
        cancel: "thead",
        containment : "parent"
    });

    if(privateEnabled != "true") {
        categoryModalTable.column(3).visible(false);
    }else{
        categoryModalTable.column(3).visible(true);
    }
    $('#categoryModalTable').on('mouseover', 'tr', function () {
        $('.popoverMessage').popover({
            placement: 'left',
            trigger: 'hover focus',
            viewport: 'body',
            html: true,
            container: 'body'
        });
    });

    $("#add-row").off().on('click', function () {
        var lastRow = categoryModalTable.row(':last').data();
        var tagsEnabled = nonConfiguredEnabled != "true" ? false : true;
        var lastRowCategory = $('#categoryModalTableBody tr:last').find("td select.dynamicSelectFieldCategory").val();
        $('#categoryModalTableBody tr').each(function () {
            var th = $(this);
            var categoryId = $(th).find("td select.dynamicSelectFieldCategory").val();
            categoryId = parseInt(categoryId);
            if (!disabledCategoryList.includes(categoryId)) {
                disabledCategoryList.push(categoryId);
            }
        });
        if(lastRowCategory != 0) {
            categoryModalTable.row.add({
                "category": {id: 0, name: "Select Category"},
                "subcategory": [],
                "alert": false,
                "private": false,
                "action": 1,
                "priority": 9999,
                "new": true
            }).draw(false);
            categoryMaximumSize(nonConfiguredEnabled);
            var $scrollBody = $(categoryModalTable.table().node()).parent();
            $scrollBody.scrollTop($scrollBody.get(0).scrollHeight);
        }
        $('#categoryModalTableBody tr:last select').eq(0).focus();
        $('#commonTagModal').find('#addTags').attr('disabled', false);
    });

    $(".addTags").off().one('click',function () {
        var newRows = [];
        var count = 1;
        $('#categoryModalTableBody tr').each(function(){
            var th = $(this);
            var categoryId = $(th).find("td select.dynamicSelectFieldCategory").val();
            var categoryText = $(th).find("td select.dynamicSelectFieldCategory option:selected").text();
            var category = {id: categoryId, name: categoryText};
            var subcategories = [];
            $(th).find("td select.dynamicSelectFieldSubCategory option:selected").each(function(){
                subcategories.push({id: $(this).val(), name:$(this).text()});
            });
            var alert = $(th).find("td input.alert-check-box-tag").is(":checked");
            var pvt = $(th).find("td input.private-check-box").is(":checked");
            var currentRow = {category: category, subcategory: subcategories, alert: alert, private: pvt, priority: count,
                autoTagged: false, isRetained: false, execConfigId: -1};
            if(currentRow.category.name != 'Select Category' && currentRow.category.name != '') {
                newRows.push(currentRow);
                count = count + 1;
            }

        });
        $("#commonTagModal").hide();

        var data = {
            newRows: JSON.stringify(newRows),
            existingRows: JSON.stringify(existingRows),
            alertId: alertId,
            type: domain,
            isArchived: isArchived
        };

        if (data.newRows != data.existingRows) {
            $.ajax({
                url: saveCommonTagsUrl,
                type: "POST",
                async: false,
                data: JSON.stringify(data),
                contentType: 'application/json; charset=utf-8',
                dataType: "json"
            })
                .done(function (data) {
                    // window.location.reload();
                    var element = $('#categories').find('.tag-container');
                    $.Notification.notify('success', 'top right', "Success", "Categories saved successfully.", {autoHideDelay: 5000});
                    element.remove();
                    $('.isCategoriesProcessing').show();
                    fetchCategories()
                })
                .fail(function () {
                    $.Notification.notify('error', 'top right', "Failed", 'Something Went Wrong, Please Contact Your Administrator.', {autoHideDelay: 5000});
                });
        }
    });
    return categoryModalTable
};

window.onload = function() {
    catEllipsis();
};
function fetchCategories() {
    var alertId = $('#catAlertId').val();
    var archived = $('#isArchived').val();
    var isAdhoc =$('#isAdhocRun').val();
    var version
    // Added for manually added case
    var isStandalone = $('#isStandalone').val() == 'true' ? true :false
    if(isStandalone){
        version = $("#caseVersionIsStandalone").val()
    }else{
        version =$('#isAdhocRun').val() == 'false' ||  !$('#isAdhocRun').val()? $("#caseVersion").val():$("#adHocVersionNum").val()
    }
    $.ajax({
        url: getCatByVersionUrl + '?alertId=' + alertId + "&version=" + version + "&archived=" + archived + "&isAdhoc=" + isAdhoc,
    })
        .done(function (result) {

            $('#categoryList').val(JSON.stringify(result));
            $('.isCategoriesProcessing').hide();
            var categories=$('#categoryList').val();
            if(categories){
                var catObj= JSON.parse(categories);
                $('#categories').append('<div class="col-max-300 pos-rel">'+signal.alerts_utils.get_tags_element(catObj)+'</div>');
                catEllipsis();
            }
            $("#caseHistoryModalTable").DataTable().ajax.reload();
        });
}

