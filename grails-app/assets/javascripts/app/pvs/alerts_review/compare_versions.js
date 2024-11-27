//= require app/pvs/common/rx_common.js
//= require app/pvs/common/rx_alert_utils.js
//= require app/pvs/alerts_review/alert_review.js

var applicationLabel = "Case Detail";
var versionAvailable = undefined;
var versionCompare = undefined;
var isChangePresent = false;

$(function () {
    backScreen();
    comparePreviousVersions();
    $('#assignedTo').append(option).trigger('change');
    $("#assignedTo").prop('disabled', true);
    var catObj = JSON.parse($('#compared-categories').val());
    var categoriesElement = fetchCategoriesElement(catObj);
    $("#categories-comparison").append(categoriesElement);
    $("#workflow_and_categories_management_container").collapse('show');
    tagEllipsis($("#workflow_and_categories_management_container"));
    webUiPopInit();
    closePopupOnScroll();

    $("#compare-screens-spinner").removeClass("hidden");
    $.ajax({
        url: versionsCaseDetailUrl,
    })
        .done(function (result) {

            fetchTreeNodes(result.sectionNameList)
            var returnElement = signal.utils.render('case_compare_screen_flex', {
                versionAvailableData: result.versionAvailableData,
                versionCompareData: result.versionCompareData,
                deviceInformation: deviceInformation,
                freeTextFieldList: freeTextField
            });
            $("#compare-screens-spinner").addClass("hidden");
            if(!isChangePresent) {
                $.Notification.notify('success', 'top right', "Success", $.i18n._('noChangesPresent'), {autoHideDelay: 5000});

            }
            $(".comparing-details").append(returnElement)
            $(".wikEdDiffDelete").attr('title',"")
            $(".wikEdDiffInsert").attr('title',"")

            zoomIn();
            zoomOut();
            refreshZoom();
        })
        .fail(function () {
            console.log("Error in fetching the case narrative")
        });
});

function fetchTreeNodes(sectionNameList) {



    $('#caseDetailTree').on('click', '.jstree-anchor', function (e) {
        $('#caseDetailTree').jstree(true).toggle_node(e.target);
    }).jstree({
        'core': {
            'data': sectionNameList,
            'dblclick_toggle': false
        }
    });

    $('#caseDetailTree').on("dblclick.jstree", function (event) {
        var node = $(event.target).closest("li");
        var $targetid = node[0].id;
        $targetid = $targetid.length && $targetid || $('[id=' + this.hash.slice(1) + ']');
        $targetid = "#" + $targetid + "_container";
        $($targetid).collapse('hide');
    });

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

    $('#caseDetailTree').on("changed.jstree", function (e, data) {
        window.selectedId = data.selected;
        var containerText = getContainerText(data.node.text);
        $('#' + containerText).collapse('toggle');
    });

    function getContainerText(text) {
        return text.toLowerCase().replace(/ /g, "_") + "_container";
    }
}

function isVisible() {
    var isVisible = true;
    if (typeof hasReviewerAccess !== "undefined" && !hasReviewerAccess) {
        isVisible = false;
    }
    return isVisible
}

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


signal.alertReview.enableMenuTooltips();
signal.alertReview.disableTooltips();


var caseNo = $('.duplicateCases').text().split(',');
$('.duplicateCases').text('');

caseNo.forEach(function (value) {

    var tag = document.createElement('a');
    var caseNumber = value.substring(0, value.indexOf('('));
    type = (value.indexOf('(M)') > 0) ? 'M' : '';
    var version = value.substr(value.indexOf('(') + 1, 1);
    var isAdhocRun = $("input[name='isAdhocRun']").val()
    var pushVal = '/signal/caseInfo/caseDetail?caseNumber=' + encodeToHTML(caseNumber) + '&version=' + version + '&followUpNumber=&isFaers=' + $("#isFaers").val() + '&isDuplicate=' + type + '&isAdhocRun=' + isAdhocRun;
    tag.href = pushVal;
    tag.text = value;
    tag.className = 'badge badge-info';
    $('.duplicateCases').append(tag);
    $('.duplicateCases').append("&nbsp;&nbsp;");

});

function getProductJson() {
    var productName = $('#productName').val();
    var level = 3;
    return generateProductJson(productName, level);
}

function generateProductJson(productName, level) {
    var signalProductValues = {"1": [], "2": [], "3": [], "4": [], "5": []};
    if (level !== ASSESSMENT_DRUG_REC_NUM_LEVEL) {
        signalProductValues[level].push({name: productName});
    }
    return signalProductValues;
}



function fetchCategoriesElement(tagObj) {
    var tagsElement = '<div class="col-max-300 pos-rel"><div class="tag-container block-ellipsis"><div class="tag-length">';
    var globalTagsArray = [];
    var alertTagsrray = [];
    var privateTagsArray = [];
    $.each(tagObj, function (key, value) {
        if (value.privateUser == '(P)') {
            privateTagsArray.push(value)
        } else if (value.tagType == '(A)') {
            alertTagsrray.push(value)
        } else {
            globalTagsArray.push(value);
        }
        tagsElement += signal.utils.render('tags_details_compared', {
            key: key,
            value: value,
            tags: tagObj,
        });
    });
    var viewAllElements = fetchViewAllComparedCategories(globalTagsArray, alertTagsrray, privateTagsArray);

    tagsElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + viewAllElements + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
    tagsElement += '</div></div></div>';
    return tagsElement
}

var fetchViewAllComparedCategories = function (globalTagsArray, alertTagsArray, privateTagsArray) {
    var returnElement = "";
    var subTags = [];
    var completedTags = []
    if (globalTagsArray.length > 0) {
        returnElement = "<p class='pbr' style='margin-top:7px'><b>Global Category</b><hr class='tagehr'>";
        $.each(globalTagsArray, function (key, value) {
            if (completedTags.indexOf(value.tagText) == -1) {
                completedTags.push(value.tagText)
                returnElement += signal.utils.render('tags_details_view_all_compared', {
                    tag: value,
                    tags: globalTagsArray
                });
            }
        });
        returnElement += "</p>"
    }
    if (alertTagsArray.length > 0) {
        completedTags = []
        returnElement += "<p class='pbr' style='margin-top:7px'><b>Alert Specific Category</b><hr class='tagehr'>";
        $.each(alertTagsArray, function (key, value) {
            if (completedTags.indexOf(value.tagText) == -1) {
                completedTags.push(value.tagText)
                returnElement += signal.utils.render('tags_details_view_all_compared', {
                    tag: value,
                    tags: alertTagsArray
                });
            }
        });
        returnElement += "</p>"
    }
    if (privateTagsArray.length > 0) {
        completedTags = []
        returnElement += "<p class='pbr' style='margin-top:7px'><b>Private Category</b><hr class='tagehr'>";
        $.each(privateTagsArray, function (key, value) {
            if (completedTags.indexOf(value.tagText) == -1) {
                completedTags.push(value.tagText)
                returnElement += signal.utils.render('tags_details_view_all_compared', {
                    tag: value,
                    tags: privateTagsArray
                });
            }
        });
        returnElement += "</p>"
    }
    return returnElement
};

function backScreen() {
    var version;
    if ($('#caseVersion').val()) {
        version = $('#caseVersion').val();
    } else {
        version = $('#version').val();
    }
    var followUp = $("#followUpNumber").val();
    $("#back-button").on('click', function () {
        signal.utils.postUrl("/signal/caseInfo/caseDetail", {
            caseNumber: $(".caseNumber").val(),
            version:  version,
            followUpNumber: followUp,
            alertId: $(".alertId").val(),
            isFaers: $("#isFaers").val(),
            fullCaseList: $('#fullCaseList').val(),
            detailsParameters: $("#detailsParameters").val(),
            isAdhocRun: $(".isAdhocRun").val(),
            totalCount: $("#totalCount").val(),
            execConfigId: $("#execConfigId").val(),
            detailsParameters: $("#detailsParameters").val(),
            isSingleAlertScreen: $("#isSingleAlertScreen").val(),
            isCaseSeries: $("#isCaseSeries").val(),
            isArchived: $("#isArchived").val(),
            oldFollowUp  : $("#oldFollowUp").val(),
            oldVersion   : $("#oldVersion").val(),
            isChildCase : typeof isChildCase !== "undefined" ? isChildCase : false
        });
    })
}