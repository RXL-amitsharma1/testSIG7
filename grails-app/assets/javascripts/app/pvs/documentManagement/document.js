var signal = signal || {};

signal.alertdoc_util = (function () {

    var init_doc_modal = function (url) {

        var dataUrl = url;

        //Data table for the document modal window.
        var documentModalTable = $("#documentModalTable").DataTable({
            "language": {
                "url": "../assets/i18n/dataTables_" + userLocale + ".json",
                "lengthMenu": 'Show _MENU_',
            },

            initComplete: function () {
            },

            "ajax": {
                "url": dataUrl,
                "dataSrc": ""
            },
            aaSorting: [[3, "desc"]],
            "bLengthChange": true,
            "bProcessing": true,
            "oLanguage": {

                "sZeroRecords": "", "sEmptyTable": '',
                "oPaginate": {
                    "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                    "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                    "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                    "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
                },
                "sLengthMenu":"Show _MENU_",
                "sInfo":"of _TOTAL_ entries",
                "sInfoFiltered": "",
            },
            "bAutoWidth": true,
            "iDisplayLength": 10,
            "aLengthMenu": [[10, 20, 50, -1], [10, 20, 50, "All"]],
            "scrollX":true,
            "aoColumns": [
                {
                    "mData": "",
                    "sortable":false,
                    "className":"col-min-25",
                    mRender: function (data, type, row) {
                        return "<input type='checkbox' data-chronical-id='" +
                            row.chronicleId + "'/>"
                    }
                },
                {
                    "mData": "chronicleId",
                    "className":"col-min-100"

                },
                {
                    "mData": "documentType",
                    "className":"col-min-150"
                },
                {
                    "mData": "documentLink",
                    "className":"col-min-150",
                    mRender: function(data, type, row) {
                        return '<a href="' + row.documentLink + '" target="_blank">' + row.linkText + '</a>';
                    }
                },
                {
                    "mData": "productName",
                    "className":"col-min-150"
                },
                {
                    "mData": "startDate",
                    "className":"col-min-100",
                    mRender: function (data, type, row) {
                        return row.startDate
                    }
                },
                {
                    "mData": "documentStatus",
                    "className":"col-min-150"
                },
                {
                    "mData": "author",
                    "className":"col-min-150"

                },
                {
                    "mData": "statusDate",
                    "className":"col-min-100"
                }
            ],

            "initComplete": function () {
                $('.filterDocuments').on('click', function () {
                    var productName = $("#productNameFilter").val();
                    var documentType = $("#documentTypeFilter").val();
                    var dataModalObj = $('#documentModalTable').DataTable();
                    var doc_list_url = '/signal/alertDocument/filterAlerts' +
                        '?productName=' + productName +
                        '&documentType=' + documentType;
                    dataModalObj.ajax.url(doc_list_url).load()
                });
                $('.resetFilter').on('click', function () {
                    var dataModalObj = $('#documentModalTable').DataTable();
                    var doc_list_url = '/signal/alertDocument/getUnlinkedDocuments';
                    reset_filter();
                    dataModalObj.ajax.url(doc_list_url).load()
                });
            },
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text()
            }]
        });

        return documentModalTable
    };

    var init_doc_list = function (doc_list_url) {

        //Datatable for the document list table.
        var documentTable = $('#alert-document-table').DataTable({
            "language": {
                "url": "../assets/i18n/dataTables_" + userLocale + ".json"
            },
            layout: {
                topStart: null,
                topEnd: null,
                bottomStart: null,
                bottomEnd: null
            },
            drawCallback: function () {

                var stop_waiting = function () {};

                var start_waiting = function () {};

                //Bind the click event on the update.
                $('.document-update').off().on('click', function() {
                    var row = $(this).parents('tr');
                    var comment = row.find('#comments').val();
                    var documentType = row.find('#documentType').val();
                    var chronicleId = row.find('#chronicleId').html();

                    var values = {
                        comment: comment,
                        chronicleId: chronicleId,
                        alertId: alertId
                    };

                    $(this).closest('tr').find('.iconContainer .fa-check').addClass('fa-spin fa-circle-o-notch');
                    $(this).closest('tr').find('.iconContainer .fa-check').removeClass('fa-check');
                    $.ajax({
                        url: '/signal/alertDocument/updateAlertDocument',
                        type: 'post',
                        data: values,
                    })
                        .fail(function () {
                            console.log('Error!!!!')
                        })
                        .done(function () {
                            stop_waiting();

                            if (typeof signalId != "undefined" && signalId != null) {
                                //Reload the datatable
                                refresh_doc_list_signal(signalId);
                            } else {
                                //Reload the datatable
                                refresh_doc_list($("#alertId").val());
                            }
                            $(this).closest('tr').find('.iconContainer .fa-cirlcle-o-notch').addClass('fa-check');
                            $(this).closest('tr').find('.iconContainer .fa-cirlcle-o-notch').removeClass('fa-spin fa-circle-o-notch')
                        })
                });


                //Bind the click event on the document unbind..
                $('.document-unbind').off().on('click', function () {
                    var row = $(this).parents('tr');
                    var chronicleId = row.find('#chronicleId').html();
                    var values = null
                    if (typeof signalId != "undefined" && signalId != null) {
                        values = {
                            chronicleId: chronicleId,
                            signalId: signalId
                        };
                    } else {
                        values = {
                            chronicleId: chronicleId,
                            alertId: alertId
                        };
                    }
                    $.ajax({
                        url: '/signal/alertDocument/unlinkDocument',
                        type: 'post',
                        data: values,
                    })
                        .fail(function () {
                            console.log('Error!!!!')
                        })
                        .done(function () {
                            if (typeof signalId != "undefined" && signalId != null) {
                                //Reload the datatable
                                refresh_doc_list_signal(signalId);
                            } else {
                                //Reload the datatable
                                refresh_doc_list($("#alertId").val());
                            }
                        })
                });

                $('input[name="comments"]').on("keydown", function (evt) {
                    $(this).closest('tr').find('.iconContainer .fa-check').addClass('activated')
                })
            },

            "ajax": {
                "url": doc_list_url,
                "dataSrc": ""
            },
            searching: true,
            "bLengthChange": true,
            "iDisplayLength": 10,
            "aLengthMenu": [[10, 20, 50, -1], [10, 20, 50, "All"]],
            "aoColumns": [
                {
                    "mData": "chronicleId",
                    "className":"col-min-150 col-max-150",
                    mRender: function (data, type, row) {
                        return '<span id="chronicleId">' + row.chronicleId + '</span>'
                    }

                },
                {
                    "mData": "documentType",
                    "className":"col-min-200 col-max-200"
                },
                {
                    "mData": "documentLink",
                    "className":"col-min-150 col-max-150",
                    mRender: function(data, type, row) {
                        return '<a href="' + row.documentLink + '" target="_blank">' + row.linkText + '</a>';
                    }

                },
                {
                    "mData": "productName",
                    "className":"col-min-150 col-max-150"
                },
                {
                    "mData": "startDate",
                    "className":"col-min-150 col-max-150"

                },
                {
                    "mData": "documentStatus",
                    "className":"col-min-100 col-max-100"
                },
                {
                    "mData": "author",
                    "className":"col-min-150 col-max-150"
                },
                {
                    "mData": "statusDate",
                    "className":"col-min-150 col-max-150"
                },
                {
                    mData: "targetDate",
                    "className":"col-min-150 col-max-150"
                },
                {
                    mData: "comments",
                    "className":"col-min-150 col-max-150",
                    mRender: function (data, type, row) {
                        var commentValue = '';
                        if (typeof row.comments != 'undefined' && row.comments != null) {
                            commentValue = row.comments
                        }
                        //Populate the comment box
                        return '<input type="text" name="comments" id="comments" class="form-control" value="' + commentValue + '"/>'
                    }
                },
                {
                    mData: "",
                    mRender: function () {
                        //Populate the icons.
                        return '<div class="iconContainer" style="width:45px;">' +
                               '<span class="document-update" style="cursor: pointer"><i class="fa fa-check"></i></span>' +
                               '<span class="document-unbind closed"  style="cursor: pointer"><i class="fa fa-trash-o"></i></span>' +
                               '</div>'
                    }
                }
            ],
            scrollX:true,
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }]
        });
        return documentTable
    };

    var select_doc = function () {
        var checked_rows = $('#documentModalTable input:checked[type="checkbox"]');
        return _.map(checked_rows, function (x) {
            return $(x).attr('data-chronical-id')
        }).join(',')
    };

    var refresh_doc_list = function (alertId) {
        var documentList = $('#alert-document-table').DataTable();
        var doc_list_url = '/signal/alertDocument/listByAlert?alertId=' + alertId;
        documentList.ajax.url(doc_list_url).load();

        //Refresh the activity table
        signal.activities_utils.reload_activity_table();
    };

    var refresh_doc_list_signal = function (signalId) {
        var documentList = $('#alert-document-table').DataTable();
        var doc_list_url = '/signal/alertDocument/listBySignal?signalId=' + signalId;
        documentList.ajax.url(doc_list_url).load();

        //Refresh the activity table
        signal.activities_utils.reload_activity_table();
    };

    var reset_filter = function () {
        $('#documentModal #productNameFilter').val('');
        $('#documentModal #documentTypeFilter').val('');
    };

    return {
        init_doc_modal: init_doc_modal,
        init_doc_list: init_doc_list,
        select_doc: select_doc,
        reset_filter: reset_filter
    }
})();

$(function () {

    $('a.export-icon-link').on('click', function(e){
        $('#loading').show();
        var fullUrl = $(this)[0].pathname.split("/");
        var method = fullUrl[fullUrl.length -1].toLowerCase();
        if(method === "details")
            method = "generatesignalsummaryreport";
        setCookie([method,"somevalue"]);
        var refreshId = setInterval(function(){
            if(getCookie(method) == "") {
                clearInterval(refreshId);
                $('#loading').hide();
            }
        }, 1500);
    });

    var _doc_table_inited = false;

    var _init_doc_modal = function () {
        var documentModal = $('#documentModal');
        signal.alertdoc_util.reset_filter();
        documentModal.modal({});
        var unlinkedDocumentsUrl = "/signal/alertDocument/getUnlinkedDocuments";

        if (!_doc_table_inited) {
            signal.alertdoc_util.init_doc_modal(unlinkedDocumentsUrl);
            _doc_table_inited = true
        } else {
            var modal_doc_table = $('#documentModalTable').DataTable();
            modal_doc_table.ajax.url(unlinkedDocumentsUrl).load()
        }
    };

    var _init_signal_doc_modal = function () {
        var documentModal = $('#documentModal');
        signal.alertdoc_util.reset_filter();
        documentModal.modal({});
        var unlinkedDocumentsUrl = "/signal/alertDocument/getUnlinkedsignalDocuments";

        if (!_doc_table_inited) {
            signal.alertdoc_util.init_doc_modal(unlinkedDocumentsUrl);
            _doc_table_inited = true
        } else {
            var modal_doc_table = $('#documentModalTable').DataTable();
            modal_doc_table.ajax.url(unlinkedDocumentsUrl).load()
        }
    };

    var add_doc_to_alert = function (chronicle_ids, alert_id) {

        var chronicle_ids = signal.alertdoc_util.select_doc();

        var values = {
            chronicle_ids: chronicle_ids,
            alertId: alertId
        };

        $.ajax({
            url: '/signal/alertDocument/addToAlert',
            type: 'post',
            data: values,
        })
            .fail(function () {
                //TODO error handling
                console.log('Error!!!!')
            })
            .done(function () {
                //Hide the modal window
                var documentModal = $('#documentModal').modal('hide');

                //Reload the datatable
                var documentList = $('#alert-document-table').DataTable();
                var doc_list_url = '/signal/alertDocument/listByAlert?alertId=' + alertId;
                documentList.ajax.url(doc_list_url).load();

                //Refresh the activity table
                signal.activities_utils.reload_activity_table();

            })
    };

    var add_doc_to_signal = function () {

        var chronicle_ids = signal.alertdoc_util.select_doc();

        var values = {
            chronicle_ids: chronicle_ids,
            signalId: signalId
        };

        $.ajax({
            url: '/signal/alertDocument/addToSignal',
            type: 'post',
            data: values,
        })
            .fail(function () {
                //TODO error handling
                console.log('Error!!!!')
            })
            .done(function () {
                //Hide the modal window
                var documentModal = $('#documentModal').modal('hide');

                //Reload the datatable
                var documentList = $('#alert-document-table').DataTable();
                var doc_list_url = '/signal/alertDocument/listBySignal?signalId=' + signalId;
                documentList.ajax.url(doc_list_url).load();

                //Refresh the activity table
                signal.activities_utils.reload_activity_table();
            })
    };

    $('.downloadReport').on('click', function() {

        var url;

        if ($(this).hasClass('pdf')) {
            url = downloadSummaryReportPDFUrl;
        } else if ($(this).hasClass('xlsx')) {
            url = downloadSummaryReportXLSXUrl;
        } else if ($(this).hasClass('word')) {
            url = downloadSummaryReportDocXUrl;
        }

        if (url) {
            var data = {};
            var eventSelection = $('#eventSelectionAssessment').val();
            var productSelection = $('#productSelectionAssessment').val();
            data['dataSource'] = $('#dataSources').val();
            data['dateRange'] = $('#dateRange').val();
            data['productSelection'] = productSelection;
            data['productGroupSelection'] = $('#productGroupSelectionAssessment').val();
            data['eventSelection'] = eventSelection;
            data['eventGroupSelection'] = $('#eventGroupSelectionAssessment').val();
            data['validatedSignal.id'] = $("#signalIdPartner").val();
            signal.utils.postUrl(url, data, false)
        }
    });

    var init = function () {
        var doc_list_url = '/signal/alertDocument/listByAlert?alertId=' + alertId;
        signal.alertdoc_util.init_doc_list(doc_list_url);
        $('.modal-add-btn').off().on('click', add_doc_to_alert);
        $('#show-doc-list-bt').off().on('click', _init_doc_modal)
    };

    var initSignal = function() {
        var doc_list_url = '/signal/alertDocument/listBySignal?signalId=' + signalId;
        signal.alertdoc_util.init_doc_list(doc_list_url);
        $('.modal-add-btn').off().on('click', add_doc_to_signal);
        $('#show-doc-list-bt').off().on('click', _init_signal_doc_modal);
    };

    if (typeof signalId != "undefined" && signalId != null) {
        initSignal();
    } else {
        init();
    }
});

/* function creates cooke with random key */
function setCookie(inputs) {
    /* cookie name */
    var name = (inputs[0]) ? inputs[0] : "key" + document.cookie.length;
    /* cookie expire in 120 seconds */
    var date = new Date();
    date.setTime(date.getTime() + (120 * 1000));
    var expires = "; expires=" + date.toGMTString();
    /* sets cookie */
    document.cookie = name + "=" + inputs[1] + expires;
};

/* get the cookie based on input */
function getCookie(input) {
    var cookies = document.cookie.split(';');
    for (var i = 0; i < cookies.length; i++) {
        console.log(cookies[i])
        if(cookies[i].trim() != "") {
            var name = cookies[i].split('=')[0].toLowerCase().trim();
            var value = cookies[i].split('=')[1].toLowerCase().trim();
            if (name == input) {
                return value;
            } else if (value === input) {
                return name;
            }
        }
    }
    return "";
};
