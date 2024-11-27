//= require app/pvs/alerts_review/alert_review

var productSummaryListTable;
$(function () {

    $("#toDate").prop("disabled", true);

    if ($("#dispositionValue").val()) {
        var sourceArray = $("#dispositionValue").val().toString().replace("[", "").replace("]", "").split(',');
        for (var i = 0; i < sourceArray.length; i++) {
            sourceArray[i] = sourceArray[i].trim()
        }
        $("#disposition").select2().val(sourceArray).trigger('change')
    } else {
        $("#disposition").select2();
    }


    productSummaryListTable = $('#productSummaryTable').DataTable({
        "language": {
            "url": "../assets/i18n/dataTables_" + userLocale + ".json"
        },
        "ajax": {
            "url": listProductSummaryUrl,
            "type": "POST",
            "dataSrc": "aaData",
            "cache": false,
            "data": function (d) {
                d.previousStartDate = $("#fromDate :selected").prev().val();
                d.previousEndDate = $("#toDate :selected").prev().val();
                d.startDate = $("#fromDate").val();
                d.endDate = $("#toDate").val();
                d.productSelection = $("#productSelection").val();
                d.frequency = $("#frequency").val();
                d.selectedDatasource = $("#selectedDatasource").val();
                if($("#disposition").val()) {
                    d.disposition = $("#disposition").val().toString()
                }
            }
        },
        "aaSorting": [],
        "bLengthChange": true,
        "bProcessing": true,
        "bServerSide": true,
        "deferLoading": 0,
        "oLanguage": {

            "sZeroRecords": "No data available in table", "sEmptyTable": "No data available in table"
        },
        "aLengthMenu": [[50, 100, 200, 500], [50, 100, 200, 500]],
        "pagination": true,
        "iTotalDisplayRecords": "50",

        initComplete: function () {
            init_filter(productSummaryListTable);
            var theDataTable = $('#productSummaryTable').DataTable();

            $("#toggle-column-filters", "#ic-toggle-column-filters").on('click', function () {
                var ele = $('.yadcf-filter-wrapper');
                var inputEle = $('.yadcf-filter');
                if (ele.is(':visible')) {
                    ele.hide();
                } else {
                    ele.show();
                    inputEle.first().focus();
                }
            });
            signal.alertReview.enableMenuTooltips();
            signal.alertReview.disableTooltips();
            addGridShortcuts('#productSummaryTable');
        },
        drawCallback: function () {
            $(".more-option").webuiPopover({
                html: true,
                trigger: 'hover',
                content: function () {
                    return $(this).attr('more-data')
                }
            });
        },
        "aoColumns": [
            {
                "mData": "event",
                "mRender": function (data, type, row) {
                    return "<span data-field ='eventName' data-ptcode='" + row.ptCode + "' data-productid='" + row.productId + "' data-assignedto='" + row.assignedTo + "' data-productName='" + row.productName + "' data-id='" + row.event + "' data-config-id='" + row.configId+"'>" + (row.event) + "</span>"
                },
                "className": "col-min-150"
            },
            {
                "mData": "disposition",
                "className": "col-min-75"
            },
            {
                "mData": "name",
                "mRender": function (data, type, row) {
                    if (row.alertType == 'Signal Management') {
                        return "<span data-field ='alertType' data-alerttype='" + row.alertType + "' data-id='" + row.validatedSignalId + "'>" + (row.name) + "</span>"
                    }
                    return "<span data-field ='alertType' data-execid='" + row.executedConfigId + "' data-alerttype='" + row.alertType + "' data-id='" + row.event + "'>" + (row.name) + "</span>"
                },
                "className": "col-min-100"

            },
            {
                "mData": "source",
                "className": "col-min-75"
            },
            {"mData": "sponCounts",
                "className": "col-min-75"
            },
            {
                "mData": "eb05Counts",
                "className": "col-min-75"
            },
            {
                "mData": "eb95Counts",
                "className": "col-min-75"
            },

            {
                "mData": "requestedBy",
                "mRender": function (data, type, row) {
                    var strLength = row.requestedBy.split(" ").join("").length;
                    var moreOption = strLength > 45 ? '<a class="pull-right" type="link"><i class="fa fa-ellipsis-h more-option"  more-data="' + row.requestedBy + '"> </i> </a>' : "";
                    return '<span class="show-comment" data-field="requestedBy" data-requestedby="' + row.requestedBy +
                        '"><i class="ico-msg requested-by fa fa-commenting"></i><span>' + row.requestedBy.substring(0, 45) +
                        '</span>' + moreOption + '</span>';

                },
                "className": "col-min-150 col-max-200"
            },
            {
                "mData": "comments",
                "mRender": function (data, type, row) {

                    var strLength = row.comment.split(" ").join("").length;
                    var moreOption = strLength > 45 ? '<a class="pull-right" type="link"><i class="fa fa-ellipsis-h more-option"  more-data="' + row.comment + '"> </i> </a>' : "";
                    return '<span class="show-comment"><i class="ico-msg comment fa fa-commenting"></i><span>' + row.comment.substring(0, 45) + '</span>' + moreOption + '</span>';
                },
                "className": "col-min-150 col-max-200"
            },
            {
                "mData": "assessmentComments",
                "mRender": function (data, type, row) {
                    if(row.alertType == 'Signal Management'){
                        var strLength = row.assessmentComment.split(" ").join("").length;
                        var moreOption = strLength > 45 ? '<a class="pull-right" type="link"><i class="fa fa-ellipsis-h more-option"  more-data="' + row.assessmentComment + '"> </i> </a>' : "";
                        return '<span class="show-comment"><i class="ico-msg assessment-comment fa fa-commenting"></i><span>' + row.assessmentComment.substring(0, 45) + '</span>' + moreOption + '</span>';

                    }
                    return '<span class="disabled"><i class="fa fa-commenting" style="color: #888;"></i></span>'

                },
                "className": "col-min-150 col-max-200"

            }
        ],
        "responsive": true,
        layout: {
            topStart: 'pageLength',
            topEnd: null,
            bottomStart: ['info', {paging: {type: "full_numbers"}}],
            bottomEnd: null,
        },
        customProcessing: true,
        scrollX: true,
        scrollY: "calc(100vh - 488px)",
        columnDefs: [{
            "targets": '_all',
            "render": $.fn.dataTable.render.text(),
            orderSequence: ['desc', 'asc']
        }]
    });
    actionButton('#productSummaryTable');
    var init_filter = function (data_table) {
        yadcf.init(data_table, [
            {
                column_number: 0,
                filter_type: 'text',
                filter_reset_button_text: false,
                filter_delay: 600,
                filter_default_label: ''
            },
            {
                column_number: 1,
                filter_type: 'text',
                filter_reset_button_text: false,
                filter_delay: 600,
                filter_default_label: ''
            },
            {
                column_number: 2,
                filter_type: "text",
                filter_reset_button_text: false,
                filter_delay: 600,
                filter_default_label: ''
            },
            {
                column_number: 3,
                filter_type: "text",
                filter_reset_button_text: false,
                filter_delay: 600,
                filter_default_label: ''
            },
            {
                column_number: 4,
                filter_type: "text",
                filter_reset_button_text: false,
                filter_delay: 600,
                filter_default_label: ''
            },
            {
                column_number: 5,
                filter_type: "text",
                filter_reset_button_text: false,
                filter_delay: 600,
                filter_default_label: ''
            },
            {
                column_number: 6,
                filter_type: "text",
                filter_reset_button_text: false,
                filter_delay: 600,
                filter_default_label: ''
            },
            {
                column_number: 7,
                filter_type: "text",
                filter_reset_button_text: false,
                filter_delay: 600,
                filter_default_label: ''
            },
            {
                column_number: 8,
                filter_type: "text",
                filter_reset_button_text: false,
                filter_delay: 600,
                filter_default_label: ''
            },
            {
                column_number: 9,
                filter_type: "text",
                filter_reset_button_text: false,
                filter_delay: 600,
                filter_default_label: ''
            }
        ]);
        $('.yadcf-filter-wrapper').hide();
        $(".yadcf-filter").removeAttr('placeholder');
    };



    $('#search').on('click', function (e) {
        e.preventDefault();
        $("#toDate").prop("disabled", false);
        productSummaryListTable.ajax.reload(function () {
            $("#toDate").prop("disabled", true);
        });
    });

    $('.export_icon a').on('click', function () {
        var format = $(this).data('format');
        $("#toDate").prop("disabled", false);
        var data = $('#product-summary-form').serialize();
        var exportUrl = exportReportUrl + '?' + data + "&outputFormat=" + format;
        $(this).attr('href', exportUrl);
        $("#toDate").prop("disabled", true);

    });

    $(document).on('click', '.comment', function (event) {
        event.preventDefault();
        var parent_row = $(event.target).closest('tr');
        var alertType = parent_row.find('span[data-field="alertType"]').attr("data-alerttype");
        var commentModal = $('#commentModal');
        var caseJson = {};

        if (alertType == 'Signal Management') {
            var validatedSignalId = parent_row.find('span[data-field="alertType"]').attr("data-id");
            commentModal.find("#application").html(alertType);
            commentModal.find("#validatedSignalId").html(validatedSignalId);
            commentModal.modal('show');
            caseJson = {
                "alertType": alertType,
                "validatedSignalId": validatedSignalId
            };

        } else {
            var eventName = parent_row.find('span[data-field="eventName"]').attr("data-id");
            var productName = parent_row.find('span[data-field="eventName"]').attr("data-productname");
            var assignedTo = parent_row.find('span[data-field="eventName"]').attr("data-assignedto");
            var configId = parent_row.find('span[data-field="eventName"]').data("configId");
            var productId = parseInt(parent_row.find('span[data-field="eventName"]').attr("data-productid"));
            var ptCode = parseInt(parent_row.find('span[data-field="eventName"]').attr("data-ptcode"));
            var executedConfigId = parent_row.find('span[data-field="alertType"]').attr("data-execid");
            var commentMetaInfo = '<span id="productName">' + productName + '</span> - <span id="eventName">' + eventName + '</span>' + '<span class="hidden" id="productId">' + productId + '</span>' +
                '<span class="hidden" id="ptCode">' + ptCode + '</span>'

            commentModal.find("#comment-meta-info").html(commentMetaInfo);
            commentModal.find("#application").html(alertType);
            commentModal.find("#executedConfigId").html(executedConfigId);
            commentModal.find("#assignedTo").html(assignedTo);
            commentModal.find("#validatedSignalId").html('');
            commentModal.find("#configId").html(configId);
            commentModal.modal('show');
            caseJson = {
                "alertType": alertType,
                "productName": productName,
                "productId": productId,
                "ptCode": ptCode,
                "eventName": eventName,
                "assignedTo": assignedTo,
                "configId": configId,
                "executedConfigId": executedConfigId
            };
        }
        signal.alertComments.populate_comments(commentModal, caseJson);
        signal.alertComments.save_comment(commentModal);
        commentModal.on('hidden.bs.modal', function () {
            if (commentModal.find("#isUpdated").val() == 'true') {
                productSummaryListTable.ajax.reload()
            }
        })
    });


    $(document).on('click', '.assessment-comment', function (event) {
        var commentModal = $('#assesmentCommentModal');
        var parent_row = $(event.target).closest('tr');
        var validatedSignalId = parent_row.find('span[data-field="alertType"]').attr("data-id");
        $.ajax({
            url: fetchAssessmentNotesUrl + "?validatedSignal.id=" + validatedSignalId,
        })
            .done(function (result) {
                $("#assessmentNotes").val(result.comment);
                commentModal.modal('show');
            });
        saveAssessmentNotes(commentModal,validatedSignalId)
    });

    $(document).on('click', '.requested-by', function (event) {
        var parent_row = $(event.target).closest('tr');
        var alertType = parent_row.find('span[data-field="alertType"]').attr("data-alerttype");
        var eventName = parent_row.find('span[data-field="eventName"]').attr("data-id");
        var productName = parent_row.find('span[data-field="eventName"]').attr("data-productname");
        var requestedBy = parent_row.find('span[data-field="requestedBy"]').attr("data-requestedby");
        var commentMetaInfo = '<span id="productName">' + productName + '</span> - <span id="eventName">' + eventName + '</span>'
        var commentModal = $('#requestByModal');
        commentModal.find("#comment-meta-info").html(commentMetaInfo);
        commentModal.find('#requestByBox').val(requestedBy);
        if(requestedBy.length > 0) {
            commentModal.find('.add-requested-by').html("Update");
        }else{
            commentModal.find('.add-requested-by').html("Add");

        }
        commentModal.modal('show');
        saveRequestBy(commentModal, alertType, productName, eventName);
    });

});


var saveAssessmentNotes = function (assessmentCommentModal,signalId) {
    assessmentCommentModal.find(".add-assessment-comment").off().on('click', function () {
        $.ajax({
            type: "POST",
            data: {'validatedSignal.id': signalId, 'comment': assessmentCommentModal.find("#assessmentNotes").val()},
            url: saveAssessmentNotesUrl,
        })
            .done(function (result) {
                if (result.success) {
                    assessmentCommentModal.modal("hide");
                    productSummaryListTable.ajax.reload()
                }
            });
    });

};

var saveRequestBy = function (requestByModal, alertType, productName, eventName) {
    requestByModal.find(".add-requested-by").off().on('click', function () {
        var data = {
            "alertType": alertType,
            "productName": productName,
            "eventName": eventName,
            "requestedBy": requestByModal.find('#requestByBox').val()
        };
        $.ajax({
            url: saveRequestByUrl,
            data: data,
            async: false,
        })
            .done(function (result) {
                if (result.success) {
                    requestByModal.modal("hide");
                    productSummaryListTable.ajax.reload()
                }
            })
            .fail(function () {});
    });

};
