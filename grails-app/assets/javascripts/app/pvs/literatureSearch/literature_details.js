//= require app/pvs/common/rx_common.js
//= require app/pvs/caseDrillDown/caseDrillDown
//= require app/pvs/alertComments/alertComments
//= require app/pvs/common/rx_alert_utils
//= require app/pvs/alerts_review/alert_review
//= require app/pvs/actions/actions.js
//= require app/pvs/activity/activities.js
//= require app/pvs/common/rx_list_utils.js
//= require app/pvs/common_tag.js
//= require app/pvs/alerts_review/archivedAlerts.js



DATE_FMT_TZ = "YYYY-MM-DD";
var table;
var activities_table;
var applicationName = "Literature Search Alert";
var applicationLabel = "Literature Search Alert";
var tags = [];
var prev_page = [];
var selectedCases = [];
var selectedCasesInfo = [];
var literatureEntriesCount=sessionStorage.getItem('literaturePageEntries')!=null?sessionStorage.getItem('literaturePageEntries'):50

$(function () {
    $(document).on('fieldsRearranged', function (e) {
        $('.yadcf-filter-wrapper').remove();
        init_filter($('#alertsDetailsTable').DataTable())
    })
    var buttonClassVar = "";
    if(typeof buttonClass !=="undefined" && buttonClass){
        buttonClassVar = buttonClass;
    }
    assignedToData = [];
    var prefix = "lsa_";

    $("a[data-toggle=\"tab\"]").on("shown.bs.tab", function (e) {
        $('#alertsDetailsTable').DataTable().columns.adjust();
    });
    $('a[href="#details"]').on("shown.bs.tab", function (e) {
        $('#alertsDetailsTable').DataTable().columns.adjust();
        addGridShortcuts('#alertsDetailsTable');
        removeGridShortcuts('#activitiesTable');
    });
    var genArticleLink = function (articleId, title, litId) {
        return '<a target="_blank" class="articleTitle" data-articleId = "' + articleId + '" data-litId = "' + litId + '" href="' + pubMedUrl + articleId + '">' + title + '</a>';
    };

    $('#activity_tab').on('click', function () {
        if (!activities_table) {
            activities_table = signal.activities_utils.init_activities_table("#activitiesTable", alertActivities, applicationName);
        }
    });

    $('a[href="#archivedAlerts"]').on("shown.bs.tab", function (e) {
        $('#archivedAlertsTable').DataTable().columns.adjust();
        addGridShortcuts('#archivedAlertsTable');
        removeGridShortcuts('#activitiesTable');
        removeGridShortcuts('#alertsDetailsTable');
    });

    $('a[href="#archivedAlerts"]').on('click', function () {
        if (callingScreen == CALLING_SCREEN.REVIEW) {
            archived_table = signal.archived_utils.init_archived_table("#archivedAlertsTable", archivedAlertUrl, applicationName, literatureDetailsUrl);
        }
    });

    fetchTags();

    $(window).on('beforeunload', function () {
        //do not discard temporary changes when exporting the report
        if (document.activeElement && $(document.activeElement).closest('#exportTypes').length > 0) {
            return;
        }

        //discard the temporary changes in the view
        $.ajax({
            url: discardTempChangesUrl,
            method: 'GET'
        })
    });


    var checkedIdList = [];
    var checkedRowList = [];
    signal.fieldManagement.populateColumnList(gridColumnsViewUrl, gridColumnsViewUpdateUrl);
    signal.actions_utils.set_action_create_modal($('#action-create-modal'));
    signal.actions_utils.set_action_list_modal($('#action-modal'));
    signal.commonTag.openCommonAlertTagModal("PVS","Literature", tags);
    signal.alertReview.openAlertCommentModal("Literature Search Alert", applicationName, applicationLabel, checkedIdList, checkedRowList);
    signal.alertReview.showAttachmentModal();
    literature.alertReview.openLiteratureHistory();
    literature.alertReview.exportLiteratureHistory();

    bindAbstractView();
    bindAbstractViewMenu();

    var constructColumns = function () {
        var aocolumns = [
            {
                "mData": "selected",
                "mRender": function (data, type, row) {
                    var checkboxhtml = '<input class="execConfigId" id="execConfigId" type="hidden" value="' + row.execConfigId + '" />' +
                        '<input class="alertConfigId" id="alertConfigId" type="hidden" value="' + row.alertConfigId + '" />';
                    if (alertIdSet.has(JSON.stringify(row.id))) {
                        return checkboxhtml + '<input type="checkbox" class="alert-check-box editor-active copy-select" data-id=' + row.id + ' checked/>';
                    }  else if(selectedCases.includes(row.id.toString())){
                        return  checkboxhtml +  '<input type="checkbox" class="alert-check-box editor-active copy-select" data-id=' + row.id + ' checked/>';
                    } else{
                        return checkboxhtml + '<input type="checkbox" class="alert-check-box editor-active copy-select" data-id=' + row.id + ' />';
                    }
                },
                "className": 'col-min-50 col-max-50',
                "orderable": false
            },
            {
                "mData": "dropdown",
                "mRender": function (data, type, row) {
                    var actionButton = '<div style="display: block;" class="btn-group dropdown dataTableHideCellContent" align="center"> \
                        <a class="dropdown-toggle" data-toggle="dropdown" tabindex="0"> \
                        <span style="cursor: pointer;font-size: 125%;" class="glyphicon glyphicon-option-vertical"></span><span class="sr-only">Toggle Dropdown</span> \
                        </a> ';

                    if(row.comment) {
                        actionButton +=  '<i class="mdi mdi-chat blue-2 font-13 pos-ab comment" title="' + $.i18n._('commentAvailable') + '"></i>';
                    }

                    if(row.isAttachment == true) {
                        actionButton += ' <i class="mdi mdi-attachment blue-1 font-13 pos-ab attach" title="' + $.i18n._('attachmentAvailable') + '"></i>';
                    }

                    actionButton += '<ul class="dropdown-menu menu-cosy" role="menu"><li role="presentation"><a class="review-row-icon literature-history-icon" tabindex="0"><span class="fa fa-list m-r-10"></span>' + $.i18n._('contextMenu.history') + '</a></li>';
                    actionButton += '<li role="presentation"><a class="review-row-icon comment-icon " data-info="row" tabindex="0"><span class="fa fa-comment m-r-10"></span>' + $.i18n._('contextMenu.comments') + '</a></li>';
                    actionButton += '<li role="presentation"><a tabindex="0" class="review-row-icon show-attachment-icon" data-field="attachment" data-id="' + row.id + '" data-controller="literatureAlert"><span class="fa fa-file m-r-10"></span>' + $.i18n._('contextMenu.attachment') + '</a></li>';
                    if((row.isUndoEnabled=="true" && row.isDefaultState === "true") || row.isDefaultState === "false"){
                        if(!row.isValidationStateAchieved && (row.isUndoEnabled === "true" && (isAdmin || row.dispPerformedBy === currUserName))){
                            actionButton += '<li role="presentation" class="popover-parent">' +
                                '<a tabindex="0" data-id ="' + row.id + '" title="Undo Disposition Change" data-html="true" class="review-row-icon undo-alert-disposition" ' +
                                'data-toggle="popover" data-content="<textarea class=\'form-control editedJustification\'>' +
                                '</textarea>' +
                                '<ol class=\'confirm-options\' id=\'revertConfirmOption\'>' +
                                '<li><a tabindex=\'0\' href=\'javascript:void(0);\' title=\'Save\'><i class=\'mdi mdi-checkbox-marked green-1\' data-id =\'' + row.id + '\' id=\'confirmUndoJustification\'></i></a></li>' +
                                '<li><a tabindex=\'0\' href=\'javascript:void(0);\' title=\'Close\'><i class=\'mdi mdi-close-box red-1\' id=\'cancelUndoJustification\'></i> </a></li>' +
                                '</ol>" '+
                                '</a>' +
                                '<span class="md md-undo m-r-10"></span>Undo Disposition Change';
                            actionButton += '</li>';
                        } else {
                            actionButton += '<li role="presentation">' +
                                '<a tabindex="0" data-id ="' + row.id + '"   class="review-row-icon undo-alert-disposition" style="cursor: not-allowed; opacity: 0.65"> <span class="md md-undo m-r-10"></span>Undo Disposition Change</a>';
                        }
                    }

                    actionButton += '</ul></div>';
                    return actionButton;
                },
                "sortable": false,
                "visible": true,
                "className":"col-min-50 col-max-50 dropDown"
            }];
        if(isPriorityEnabled) {
            aocolumns.push.apply(aocolumns, [
                {
                    "mData": "priority",
                    "name": "priority",
                    "aTargets": ["priority"],
                    "mRender": function (data, type, row) {
                        return signal.utils.render('priority', {
                            priorityValue: row.priority.value,
                            priorityClass: row.priority.iconClass,
                            isPriorityChangeAllowed: true
                        });
                    },
                    'className': 'col-min-50 col-max-50 priorityParent',
                    "visible": true
                }]);
        }
        aocolumns.push.apply(aocolumns, [
            {
                "mData": "actions",
                "name": "actions",
                "mRender": function (data, type, row) {
                    row["buttonClass"]= buttonClassVar;
                    return signal.actions_utils.build_action_render(row)
                },
                'className': 'col-min-50 action-col col-max-50 text-center',
                "visible": true
            },
            {
                "mData": "articleId",
                "name": "articleId",
                'className': 'col-min-100 col-max-100'
            },
            {
                "mData": "alertTags",
                "mRender": function (data, type, row) {
                    var tagsElement = signal.alerts_utils.get_tags_element(row.alertTags);
                    return tagsElement
                },
                "className": 'col-max-200 col-max-200',
                "sortable": false,
                "visible": signal.fieldManagement.visibleColumns('alertTags')
            },
            {
                "mData": "title",
                "name": "title",
                "className": 'col-min-200 col-max-200',
                "visible": signal.fieldManagement.visibleColumns('title'),
                "mRender": function (data, type, row) {
                    var colElement = '<div class="col-container"><div class="col-height">';
                    if (selectedDatasource === 'embase') {
                        colElement += escapeHTML(row.title);
                    } else {
                        colElement += genArticleLink(row.articleId, row.title, row.id);
                    }
                    colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="'+escapeHTML(row.title)+'"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                    colElement += '</div></div>';
                    return colElement
                }
            },
            {
                "mData": "authors",
                "name": "authors",
                "className": 'col-min-200 col-max-200',
                "visible": signal.fieldManagement.visibleColumns('authors'),
                "mRender": function (data, type, row) {
                    var colElement = '<div class="col-container"><div class="col-height">';
                    colElement+=row.authors;
                    colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="'+row.authors+'"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                    colElement += '</div></div>';
                return colElement
                }
            },
            {
                "mData": "publicationDate",
                "name": "publicationDate",
                "className": 'col-min-150 col-max-150',
                "visible": signal.fieldManagement.visibleColumns('publicationDate')

            },
            {
                "mData": 'signal',
                "mRender": function (data, type, row) {
                    var signalAndTopics = '';
                    $.each(row.signal, function(i, obj){
                        var url = signalDetailUrl + '?id=' + obj['signalId'];
                        if($('#hasSignalManagementAccess').val() == 'true') {
                            signalAndTopics = signalAndTopics + '<span class="click box-inline word-wrap-break-word col-max-150"><a  class="cell-break" title="' + obj.disposition.displayName + '"  onclick="validateAccess(event,' + obj['signalId'] + ')" href="' + url + '">' + escapeHTML(obj['name']) + '</a></span>&nbsp;'
                        }else {
                            signalAndTopics = signalAndTopics + '<span class="click box-inline signalSummaryAuth word-wrap-break-word col-max-150"><a  class="cell-break" title="' + obj.disposition.displayName + '" href="javascript:void(0)">' + escapeHTML(obj['name']) + '</a></span>&nbsp;'
                        }
                        signalAndTopics = signalAndTopics + ","
                    });
                    if(signalAndTopics.length > 1)
                        return '<div class="cell-break word-wrap-break-word col-max-150>' + signalAndTopics.substring(0, signalAndTopics.length - 1) + '</div>';
                    else
                        return '-';
                },
                'className': 'col-min-150 col-max-150 signalInformation',
                "sortable": false,
                "visible": signal.fieldManagement.visibleColumns('signal')
            },
            {
                "mData": "currentDisposition",
                "name": "currentDisposition",
                "mRender": function (data, type, row, meta) {
                    return signal.utils.render('disposition_dss3', {
                        allowedDisposition: dispositionIncomingOutgoingMap[row.disposition],
                        currentDisposition: row.disposition,
                        forceJustification: forceJustification,
                        isReviewed: row.isReviewed,
                        isValidationStateAchieved: row.isValidationStateAchieved,
                        id:row.currentDispositionId,
                        useDropdown: (dispositionDropdownEnabled === 'true'),
                        rowIndex: meta.row
                    });
                },
                "visible": signal.fieldManagement.visibleColumns('currentDisposition'),
                "class": 'col-max-200  col-min-200 dispositionAction'
            },
            {
                "mData": "assignedTo",
                "name": "assignedTo",
                "mRender": function (data, type, row) {
                    return signal.list_utils.assigned_to_comp(row.id, row.assignedTo)
                },
                "className": 'col-min-150 col-max-150 assignedTo',
                "sortable": false,
                "visible": signal.fieldManagement.visibleColumns('assignedTo')
            },
            {
                "mData": "productName",
                "name": "productName",
                "className": 'col-min-150 col-max-150',
                "visible": signal.fieldManagement.visibleColumns('productName')
            },
            {
                "mData": "eventName",
                "name": "eventName",
                "className": 'col-min-150 col-max-150',
                "visible": signal.fieldManagement.visibleColumns('eventName')
            },
            {
                "mData": "disposition",
                "name" : "disposition",
                "mRender": function (data, type, row) {
                    return row.disposition;
                },
                "className": 'col-max-200 col-min-200 currentDisposition',
                "visible": signal.fieldManagement.visibleColumns('disposition')
            }
        ]);

        if (selectedDatasource === 'pubmed') {
            aocolumns.push.apply(aocolumns, [
                {
                    "mData": "citation",
                    "name": "citation",
                    "sortable": false,
                    "mRender": function (data, type, row) {
                        return addEllipsis(row.citation);
                    },
                    "className": 'col-min-150 col-max-150 word-break cell-break',
                    "visible": signal.fieldManagement.visibleColumns('citations')
                },
                {
                    "mData": "publicationYear",
                    "name": "publicationYear",
                    "sortable": false,
                    "className": 'col-min-150 col-max-150',
                    "visible": signal.fieldManagement.visibleColumns('publicationYear')
                }
            ]);
        }

        if (selectedDatasource === 'embase') {
            aocolumns.push.apply(aocolumns, [
                {
                    "mData": "affiliationOrganization",
                    "name": "affiliationOrganization",
                    "className": 'col-min-250 col-max-250',
                    "visible": signal.fieldManagement.visibleColumns('affiliationOrganization'),
                    "mRender": function (data, type, row) {
                        var colElement = '<div class="col-container"><div class="col-height">';
                        colElement += row.affiliationOrganization || '-';
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + escapeHTML(row.affiliationOrganization || '-') + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement
                    }
                },
                {
                    "mData": "sourceTitle",
                    "name": "sourceTitle",
                    "className": 'col-min-150 col-max-150',
                    "visible": signal.fieldManagement.visibleColumns('sourceTitle'),
                    "mRender": function (data, type, row) {
                        var colElement = '<div class="col-container"><div class="col-height">';
                        colElement += row.sourceTitle || '-';
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + escapeHTML(row.sourceTitle || '-') + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement
                    }
                },
                {
                    "mData": "issn",
                    "name": "issn",
                    "className": 'col-min-150 col-max-150',
                    "visible": signal.fieldManagement.visibleColumns('issn'),
                    "mRender": function (data, type, row) {
                        var colElement = '<div class="col-container"><div class="col-height">';
                        colElement += row.issn || '-';
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + escapeHTML(row.issn || '-') + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement
                    }
                },
                {
                    "mData": "sourceLink",
                    "name": "sourceLink",
                    "className": 'col-min-150 col-max-150',
                    "visible": signal.fieldManagement.visibleColumns('sourceLink'),
                    "mRender": function (data, type, row) {
                        var colElement = '<div class="col-container"><div class="col-height">';
                        if (row.sourceLink) {
                            colElement += '<a href="' + escapeHTML(row.sourceLink) + '" target="_blank">' + escapeHTML(row.sourceLink) + '</a>';
                        } else {
                            colElement += '-';
                        }
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + escapeHTML(row.sourceLink || '-') + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement
                    }
                },
                {
                    "mData": "publicationYear",
                    "name": "publicationYear",
                    "className": 'col-min-150 col-max-150',
                    "visible": signal.fieldManagement.visibleColumns('publicationYear'),
                    "mRender": function (data, type, row) {
                        var colElement = '<div class="col-container"><div class="col-height">';
                        colElement += row.publicationYear || '-';
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + escapeHTML(row.publicationYear || '-') + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement
                    }
                },
                {
                    "mData": "chemicalName",
                    "name": "chemicalName",
                    "className": 'col-min-150 col-max-150',
                    "visible": signal.fieldManagement.visibleColumns('chemicalName'),
                    "mRender": function (data, type, row) {
                        var colElement = '<div class="col-container"><div class="col-height">';
                        colElement += row.chemicalName || '-';
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + escapeHTML(row.chemicalName || '-') + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement
                    }
                },
                {
                    "mData": "casRegistryNumber",
                    "name": "casRegistryNumber",
                    "className": 'col-min-150 col-max-150',
                    "visible": signal.fieldManagement.visibleColumns('casRegistryNumber'),
                    "mRender": function (data, type, row) {
                        var colElement = '<div class="col-container"><div class="col-height">';
                        colElement += row.casRegistryNumber || '-';
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + escapeHTML(row.casRegistryNumber || '-') + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement
                    }
                },
                {
                    "mData": "manufacturer",
                    "name": "manufacturer",
                    "className": 'col-min-150 col-max-150',
                    "visible": signal.fieldManagement.visibleColumns('manufacturer'),
                    "mRender": function (data, type, row) {
                        var colElement = '<div class="col-container"><div class="col-height">';
                        colElement += row.manufacturer || '-';
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + escapeHTML(row.manufacturer || '-') + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement
                    }
                },
                {
                    "mData": "tradeName",
                    "name": "tradeName",
                    "className": 'col-min-150 col-max-150',
                    "visible": signal.fieldManagement.visibleColumns('tradeName'),
                    "mRender": function (data, type, row) {
                        var colElement = '<div class="col-container"><div class="col-height">';
                        colElement += row.tradeName || '-';
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + escapeHTML(row.tradeName || '-') + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement
                    }
                },
                {
                    "mData": "tradeItemManufacturer",
                    "name": "tradeItemManufacturer",
                    "className": 'col-min-150 col-max-150',
                    "visible": signal.fieldManagement.visibleColumns('tradeItemManufacturer'),
                    "mRender": function (data, type, row) {
                        var colElement = '<div class="col-container"><div class="col-height">';
                        colElement += row.tradeItemManufacturer || '-';
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + escapeHTML(row.tradeItemManufacturer || '-') + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement
                    }
                },
                {
                    "mData": "externalSource",
                    "name": "externalSource",
                    "className": 'col-min-150 col-max-150',
                    "visible": signal.fieldManagement.visibleColumns('externalSource'),
                    "mRender": function (data, type, row) {
                        var colElement = '<div class="col-container"><div class="col-height">';
                        colElement += row.externalSource || '-';
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + escapeHTML(row.externalSource || '-') + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement
                    }
                },
                {
                    "mData": "publisherEmail",
                    "name": "publisherEmail",
                    "className": 'col-min-150 col-max-150',
                    "visible": signal.fieldManagement.visibleColumns('publisherEmail'),
                    "mRender": function (data, type, row) {
                        var colElement = '<div class="col-container"><div class="col-height">';
                        colElement += row.publisherEmail || '-';
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + escapeHTML(row.publisherEmail || '-') + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement
                    }
                },
                {
                    "mData": "enzymeCommissionNumber",
                    "name": "enzymeCommissionNumber",
                    "className": 'col-min-150 col-max-150',
                    "visible": signal.fieldManagement.visibleColumns('enzymeCommissionNumber'),
                    "mRender": function (data, type, row) {
                        var colElement = '<div class="col-container"><div class="col-height">';
                        colElement += row.enzymeCommissionNumber || '-';
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + escapeHTML(row.enzymeCommissionNumber || '-') + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement
                    }
                },
                {
                    "mData": "digitalObjectIdentifier",
                    "name": "digitalObjectIdentifier",
                    "className": 'col-min-250 col-max-250',
                    "visible": signal.fieldManagement.visibleColumns('digitalObjectIdentifier'),
                    "mRender": function (data, type, row) {
                        var colElement = '<div class="col-container"><div class="col-height">';
                        colElement += row.digitalObjectIdentifier || '-';
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + escapeHTML(row.digitalObjectIdentifier || '-') + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement
                    }
                }
            ]);
        }

        return aocolumns
    };

    $(document).on('click', '#alertsDetailsTable_paginate', function () {
        if($('input#select-all').is(":checked") && !prev_page.includes($('li.active').text().slice(-3).trim())){
            prev_page.push($('li.active').text().slice(-3).trim());
        }
        if((!$('input#select-all').is(":checked") && prev_page.includes($('li.active').text().slice(-3).trim()))){
            var position = prev_page.indexOf($('li.active').text().slice(-3).trim());
            prev_page.splice(position,1);
        }
    });

    table = $('#alertsDetailsTable').DataTable({
        layout: {
            topStart: null,
            topEnd: null,
            bottomStart: ['pageLength', 'info', {paging: {type: "full_numbers"}}],
            bottomEnd: null,
        },
        customProcessing: true,
        "language": {
        },
        stateSave: true,
        drawCallback: function (settings) {
            var current_page = $('li.active').text().slice(-3).trim();
            if(typeof prev_page != 'undefined' && $.inArray(current_page,prev_page) == -1) {
                $("input#select-all").prop('checked', false);
            } else {
                $("input#select-all").prop('checked',true);
            }
            if(typeof hasReviewerAccess !== "undefined" && !hasReviewerAccess) {
                $(".changeDisposition").prop("data-target","");
                $(".changePriority").prop("data-target","");
            }
            if(typeof hasSignalCreationAccessAccess !== "undefined" && !hasSignalCreationAccessAccess) {
                $(".changeDisposition[data-validated-confirmed=true]").removeAttr("data-target");
            }
            var rowsLiterature = $('#alertsDetailsTable').DataTable().rows().data();
            if(settings.json != undefined) {
                pageDictionaryForAlertDetails($('#alertsDetailsTable_wrapper')[0], settings.aLengthMenu[0][0], settings.json.recordsFiltered);
            }else{
                pageDictionaryForAlertDetails($('#alertsDetailsTable_wrapper')[0], 50, rowsLiterature.length);
            }
            this.api().state.clear();
            if ( $('#detailed-view-checkbox').prop("checked") == true) {
                showAbstractDetailsView()
            }
            tagEllipsis($('#alertsDetailsTable'));
            colEllipsis();
            webUiPopInitForCategories();
            webUiPopInit();
            showInfoPopover();
            closeInfoPopover();
            showInfoPopover();
            $('.dt-pagination').on('change', function () {
                var countVal = $('.dt-pagination').val()
                sessionStorage.setItem("literaturePageEntries", countVal);
                literatureEntriesCount = sessionStorage.getItem("literaturePageEntries");
            })
            if (!$(table.table().body()).hasClass('detailsTableBody')) {
                $(table.table().body()).addClass('detailsTableBody');
            }
            enterKeyAlertDetail();
            closePopupOnScroll();
            populateSelectedCases();
        },
        initComplete: function (settings, json) {
            signal.alertReview.bindGridDynamicFilters(json.filters, prefix, json.configId);
            assignedToData = [];
            signal.alertReview.enableMenuTooltips();
            signal.alertReview.disableTooltips();
            addGridShortcuts('#alertsDetailsTable');
            $('#detailed-view-checkbox').trigger('change');
            $("#toggle-column-filters, #ic-toggle-column-filters").on('click', function () {
                var ele = $('.yadcf-filter-wrapper');
                var inputEle = $('.yadcf-filter');
                if (ele.is(':visible')) {
                    ele.hide();
                } else {
                    ele.show();
                    inputEle.first().focus();
                }
            });
        },

        "ajax": {
            "url": listConfigUrl,
            "dataSrc": "aaData",
            "cache": false,
            data: function (args) {
                return {
                    "args": JSON.stringify(args),
                    "configId": configId,
                    "isArchived": isArchived,
                    "datasource": selectedDatasource
                };
            }
        },
        "bLengthChange": true,
        "bProcessing": true,
        "bServerSide": true,
        "oLanguage": {
            "oPaginate": {
                "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
            },
            "sLengthMenu":"Show _MENU_",
        },
        "aLengthMenu": [[50, 100, 200, 500], [50, 100, 200, 500]],
        "pagination": true,
        "iTotalDisplayRecords": literatureEntriesCount,
        "iDisplayLength": parseInt(literatureEntriesCount),
        "aoColumns":constructColumns(),
        "responsive": true,
        scrollX: true,
        scrollY: "65vh",
        "rowCallback": function (row, data, index) {
            //Bind AssignedTo Select Box
            // workaround, select2 initialization is called before elements displayed
            setTimeout(function () {
                signal.user_group_utils.bind_assignTo_to_grid_row($(row), searchUserGroupListUrl, {
                    name: data.assignedTo.fullName,
                    id: data.assignedTo.id
                });
                if (typeof hasReviewerAccess !== "undefined" && !hasReviewerAccess) {
                    $(row).find(".assignedToSelect").select2({
                        minimumInputLength: 0,
                        multiple: false,
                        placeholder: 'Select Assigned To',
                        allowClear: false,
                        width: "100%",
                    })
                }
            }, 0)
        },
        columnDefs: [{
            "targets": '_all',
            "render": $.fn.dataTable.render.text(),
            orderSequence: ['desc', 'asc']
        }]
    });
    setTimeout(function () {
        init_filter(table);
    }, 500);

    var init_filter = function (data_table) {
        var filters = [
            {
                column_number: isPriorityEnabled ? 4 : 3
            },
            {
                column_number: isPriorityEnabled ? 5 : 4,
                filter_type: "text",
                filter_reset_button_text: false
            },
            {
                column_number: isPriorityEnabled ? 6 : 5
            },
            {
                column_number: isPriorityEnabled ? 7 : 6
            },
            {
                column_number: isPriorityEnabled ? 8 : 7
            },
            {
                column_number: isPriorityEnabled ? 9 : 8
            },
            {
                column_number: isPriorityEnabled ? 10 :9,
                filter_type: "text",
                style_class: "hidden",
                filter_reset_button_text: false
            },
            {
                column_number: isPriorityEnabled ? 11 : 10,
                filter_type: "text",
                filter_reset_button_text: false
            },
            {
                column_number: isPriorityEnabled ? 12 : 11
            },
            {
                column_number: isPriorityEnabled ? 13 : 12
            },
            {
                column_number: isPriorityEnabled ? 14 : 13
            }

        ];
        if (selectedDatasource === 'pubmed') {
            filters.push(
                {
                    column_number: isPriorityEnabled ? 15 : 14
                },
                {
                    column_number: isPriorityEnabled ? 16 : 15
                }
            );
        }

        if (selectedDatasource === 'embase') {
            filters.push(
                {
                    column_number: isPriorityEnabled ? 15 : 14 // affiliationOrganization
                },
                {
                    column_number: isPriorityEnabled ? 16 : 15 // sourceTitle
                },
                {
                    column_number: isPriorityEnabled ? 17 : 16 // issn
                },
                {
                    column_number: isPriorityEnabled ? 18 : 17 // sourceLink
                },
                {
                    column_number: isPriorityEnabled ? 19 : 18 // publicationYear
                },
                {
                    column_number: isPriorityEnabled ? 20 : 19 // chemicalName
                },
                {
                    column_number: isPriorityEnabled ? 21 : 20 // casRegistryNumber
                },
                {
                    column_number: isPriorityEnabled ? 22 : 21 // manufacturer
                },
                {
                    column_number: isPriorityEnabled ? 23 : 22 // tradeName
                },
                {
                    column_number: isPriorityEnabled ? 24 : 23 // tradeItemManufacturer
                },
                {
                    column_number: isPriorityEnabled ? 25 : 24 // externalSource
                },
                {
                    column_number: isPriorityEnabled ? 26 : 25 // publisherEmail
                },
                {
                    column_number: isPriorityEnabled ? 27 : 26 // enzymeCommissionNumber
                },
                {
                    column_number: isPriorityEnabled ? 28 : 27 // digitalObjectIdentifier
                }
            );
        }

        yadcf.init(data_table, filters, {
            filter_type: "text",
            filter_reset_button_text: false,
            filter_delay: 600,
            filter_default_label: ''
        });
        $(".yadcf-filter").removeAttr('placeholder');
        var fixedColCount = isPriorityEnabled ? 4 : 3;
        signal.fieldManagement.init($('#alertsDetailsTable').DataTable(), '#alertsDetailsTable', fixedColCount, true);
        $('.yadcf-filter-wrapper').hide();
    };
    signal.user_group_utils.bind_assignTo_selection(assignToGroupUrl, table, hasReviewerAccess);

    $(document).on('click', '.articleTitle', function (e) {
        e.preventDefault();
        var literatureId = $(this).data("litid");
        var articleId = $(this).data("articleid");
        if(hasReviewerAccess) {
            $.ajax({
                url: updateAutoRouteDispositionUrl,
                data: {
                    'literatureId': literatureId,
                    isArchived: isArchived,
                },
            })
                .done(function (result) {
                    signal.utils.postUrl(pubMedUrl + articleId, {
                        articleId: articleId
                    }, true);
                    if (result.status) {
                        $('#alertsDetailsTable').DataTable().ajax.reload();
                    }
                })
                .fail(function () {
                    console.error("Some error occured while updating AutoRoute Disposition");
                });
        }  else {
            $.Notification.notify('warning', 'top right', "Warning", "You don't have access to perform auto route disposition", {autoHideDelay: 5000});
            signal.utils.postUrl(pubMedUrl + articleId, {
                articleId: articleId
            }, true);
        }
    });

    $(document).on('click', '.alert-check-box', function () {
        if(!this.checked){
            $('#literatureDetailsTableRow input#select-all').prop("checked",false);
        }
    });

    $('ul#exportTypes li a').on('click', function (e) {
        var filter_params = "";
        var ids = [];
        var filterValues = [];
        $('.copy-select:checked').each(function () {
            if ($(this)[0]['checked']) {
                ids.push($(this).attr('data-id'))
            }
        });

        $.each($(".disposition-ico").eq(0).find("li"),function (id,val) {
            if($(val).find('input').is(':checked') === true){
                filterValues.push($(val).find('input').val());
            }
        });

        $('#alertsDetailsTable').DataTable().columns().every(function () {
            if (this.search() !== "") {
                filter_params = filter_params + "&" + this.dataSrc() + "=" + this.search();
            }
        });
        var url = $(this).attr('href').split('&')[0] + "&isAbstractEnabled=" + $('#detailed-view-checkbox').prop("checked") + '&dispositionFilters=' + encodeURI(JSON.stringify(filterValues));
        $(this).attr('href', url + encodeURI(filter_params) + "&configId=" + configId + "&isArchived=" + isArchived +"&selectedCases=" + ids);
    });
    $(document).on('change', '.copy-select', function(){
        addToSelectedCheckBox($(this))
    });
});

var formatArticleAbstract = function (d) {
    var res = [];
    var value = d.articleAbstract;
    var lines = value.split("\n");
    lines.forEach(function(v,i){
        var keyValue = v.split(":");
        if(keyValue.length == 2 && lines.length > 1) {
            res.push({heading: keyValue[0] + ": ", text: keyValue[1]});
        }else if(keyValue.length > 2 && lines.length > 1) {
            var index = v.indexOf(":");
            res.push({heading: v.substr(0, index) + ": ", text: v.substr(index + 1)});
        }else
            res.push({heading:"", text:v});
    });
    var resultedObj = {
        articleAbstract: res
    };
    html = signal.utils.render("literature_details_child_view", resultedObj);
    return html;
};

var formatDescriptorTerms = function (d) {
    var descriptorGroups = JSON.parse(d.descriptorGroups);
    var resultedObj = {
        descriptorGroups: descriptorGroups
    };
    var html = signal.utils.render("descriptor_terms_child_view", resultedObj);
    return html;
};

var formatExplosionTerms = function (d) {
    var explosionGroups = JSON.parse(d.explosionGroups);
    var resultedObj = {
        explosionGroups: explosionGroups
    };
    var html = signal.utils.render("explosion_terms_child_view", resultedObj);
    return html;
};

Handlebars.registerHelper('increment', function (val) {
    return val + 1;
});

function bindAssignToSelection(searchUserGroupListUrl, assignToGroupUrl, table) {
    $(".assignedToSelect").each(function (i) {
        signal.user_group_utils.bind_assign_to($(this), searchUserGroupListUrl, assignedToData[i]);
    });
    signal.user_group_utils.bind_assignTo_selection(assignToGroupUrl, table);
}

function bindAbstractView() {
    var detailedViewCheckbox = $('#detailed-view-checkbox');
    let setMenuCheck = $('.abstract-menu');
    detailedViewCheckbox.on("click", function () {
        if ($(this).prop("checked") == true) {
            $(setMenuCheck).children(0).prop("checked", true);
            showAbstractDetailsView();
            addAbstract(detailedViewCheckbox)
        } else {
            $(setMenuCheck).children(0).prop("checked", false);
            hideAbstractDetailsView();
            removeAbstract();
        }
        saveAbstractViewForUser(detailedViewCheckbox.prop('checked'));
    });
}
function bindAbstractViewMenu() {
    var detailedViewCheckbox = $('.abstract-menu');
    let setPinnedCheck = $('#detailed-view-checkbox');
    detailedViewCheckbox.on("click", function () {
        if ($(this).attr("data-checked") == "false") {
            $(this).attr("data-checked", "true");
            $(this).children(0).prop("checked", true);
            $(setPinnedCheck).prop("checked", true);
            showAbstractDetailsView();
            addAbstract(detailedViewCheckbox.children(0))
        } else {
            $(this).attr("data-checked", "false");
            $(this).children(0).prop("checked", false);
            $(setPinnedCheck).prop("checked", false);
            hideAbstractDetailsView();
            removeAbstract();
        }
        saveAbstractViewForUser(setPinnedCheck.prop('checked'));
    });
}

function showAbstractDetailsView() {
    table.rows().every(function () {
        var row = this;
        var rowData = row.data();

        if (selectedDatasource === 'embase') {
            var container = $('<div></div>');

            if (rowData.descriptorGroups) {
                container.append(formatDescriptorTerms(rowData));
            }
            container.append('<div style="margin: 10px 0;"><hr style="border: none; height: 1px; background-color: #ddd;"></div>');
            if (rowData.explosionGroups) {
                container.append(formatExplosionTerms(rowData));
            }
            container.append('<div style="margin: 10px 0;"><hr style="border: none; height: 1px; background-color: #ddd;"></div>');

            container.append(formatArticleAbstract(row.data()));

            row.child(container, row.node().className).show();
        } else {
            row.child(formatArticleAbstract(row.data()), row.node().className).show();
        }
    });
}

function hideAbstractDetailsView() {
    table.rows().every(function () {
        this.child().hide();
    });
}

$(document).on('click', 'input#select-all', function () {
    $(".copy-select").prop('checked', this.checked);
    $(".alert-select-all").prop('checked', this.checked);
    if (typeof isCaseDetailView !== "undefined" && isCaseDetailView == "true") {
        checkboxSelector = 'table#alertsDetailsTable .copy-select';
    } else {
        checkboxSelector = '.copy-select';
    }
    $.each($(checkboxSelector), function () {
        if(selectedCases.indexOf($(this).attr("data-id")) == -1 && $(this).is(':checked')){
            selectedCases.push($(this).attr("data-id"));
            var selectedRowIndex = $(this).closest('tr').index();
            if (isAbstractViewOrCaseView(selectedRowIndex)) {
                selectedRowIndex = selectedRowIndex / 2
            }
            selectedCasesInfo.push(populateDispositionDataFromGrid(selectedRowIndex));
        } else if(selectedCases.indexOf($(this).attr("data-id")) != -1 && !$(this).is(':checked')){
            selectedCases.splice( $.inArray($(this).attr("data-id"), selectedCases), 1 );
            selectedCasesInfo.splice($.inArray($(this).attr("data-id"), selectedCases), 1);
        }
    })
});

function addAbstract(detailedViewCheckbox){
    $('#exportTypes').find('a').each(function() {
        $(this).attr('href', $(this).attr('href') + "&isAbstractEnabled=" + detailedViewCheckbox.prop("checked"));
    });
}

function removeAbstract(){
    $('#exportTypes').find('a').each(function() {
        $(this).attr('href',$(this).attr('href').split("&isAbstractEnabled")[0]);
    });
}

$(document).on('click', '.signalSummaryAuth', function (evt) {
    $.Notification.notify('warning','top right', "Warning", "You don't have access to view signal summary", {autoHideDelay: 5000});
});

var fetchTags = function () {
    $.ajax({
        url: fetchCommonTagsUrl,
        async:false,
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

function populateSelectedCases() {
    $(".copy-select").on('change', function () {
        if (selectedCases.indexOf($(this).attr("data-id")) == -1 && $(this).is(':checked')) {
            var selectedRowIndex = $(this).closest('tr').index();
            if (isAbstractViewOrCaseView(selectedRowIndex)) {
                selectedRowIndex = selectedRowIndex / 2
            }
            selectedCases.push($(this).attr("data-id"));
            selectedCasesInfo.push(populateDispositionDataFromGrid(selectedRowIndex));
        } else if (selectedCases.indexOf($(this).attr("data-id")) != -1 && !$(this).is(':checked')) {
            selectedCasesInfo.splice($.inArray($(this).attr("data-id"), selectedCases), 1);
            selectedCases.splice($.inArray($(this).attr("data-id"), selectedCases), 1);
        }
    });
}

function saveAbstractViewForUser(flagValue) {
    $.ajax({
        url: '/signal/preference/updateUserPreferenceAbstractView',
        data: {flagValue: flagValue},
    }).done(function () {
        $.Notification.notify('success', 'top right', "Success", "Successfully updated abstract view for user", {autoHideDelay: 5000});
    }).fail(function () {
        $.Notification.notify('error', 'top right', "Error", "Error occurred while update abstract view for user", {autoHideDelay: 5000});
    })
}