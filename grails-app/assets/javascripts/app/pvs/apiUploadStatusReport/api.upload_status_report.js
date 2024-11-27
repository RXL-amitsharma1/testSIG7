$(function () {
    function showPopup(text) {
        var evdasDataLogModal = $("#evdasDataLogModal");
        $(evdasDataLogModal).find("span#message").html(text);
        evdasDataLogModal.modal("show");
    };

    // Bug:PVS-37813: this method has been added for fractional sorting, even though the code seems dead as the usage of this was not found
    addFractionSorting()

    var eudraDataTable = $('#eudraDataTable').DataTable({

        responsive: true,
        language: {
            "url": "../assets/i18n/dataTables_" + userLocale + ".json"
        },
        "ajax": {
            "url": getEvdasDataUrl,
            "dataSrc": "response"
        },
        "oLanguage": {
            "oPaginate": {
                "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
            },
        },
        "aaSorting": [[6, "desc"]],
        "bLengthChange": true,
        "iDisplayLength": 10,
        "aLengthMenu": [[10, 50, 100, 200, -1], [10, 50, 100, 200, "All"]],
        "scrollY":"calc(100vh - 421px)",
        "initComplete": function () {
            addGridShortcuts('#eudraDataTable');
        },
        drawCallback: function () {
            $(".show-error-log").on('click', function (event) {
                showPopup($(event.target).data('error-log'));
            });
        },
        "aoColumns": [
            {
                "mData": "documentName",
                "mRender": function (data, type, row) {
                    var textToDisplay = (type === 'display' && data.length > 255) ? data.substr(0, 255) + '…' : data;
                    return '<a href="' + downloadDocumentUrl + "/" + row.documentId + '" style="margin-right: 20px">' +
                        '<span style="cursor:pointer" >' + escapeHTML(textToDisplay) + '</span></a>'
                },
                'className': 'col-min-100 col-max-300 mustwrap'
            },
            {
                "mData": 'dataType',
                "mRender": signal.list_utils.truncateTextAndShowTooltip(255, true)
            },
            {
                "mData": 'description',
                "mRender": signal.list_utils.truncateTextAndShowTooltip(255, true)
            },
            {
                "mData": 'dataRange'
            },
            {
                "mData": 'substanceName',
                "mRender": signal.list_utils.truncateTextAndShowTooltip(255, true)
            },
            {
                "mRender": function (data, type, row) {
                    var element = row.processedRecords + '/' + row.totalRecords;
                    if (row.processedRecords < row.totalRecords) {
                        element = '<a href="' + downloadErrorLogUrl + "/" + row.documentId + '" style="margin-right: 20px">' +
                            '<span style="cursor:pointer" >' + row.processedRecords + '/' + row.totalRecords + '</span>' +
                            '</a>'
                    } else {
                        element = row.processedRecords + '/' + row.totalRecords;
                    }
                    return element
                }
            },
            {
                "mData": 'uploadTimeStamp'

            }, {
                "mData": 'uploadedBy'

            },
            {
                "mRender": function (data, type, row) {
                    if (row.uploadStatus == "SUCCESS") {
                        return '<i class="fa fa-check" aria-hidden="true" style="color:green"></i>'
                    } else if (row.uploadStatus == "FAILED") {
                        return '<i class="fa fa-window-close show-error-log" aria-hidden="true" style="color:red; cursor: pointer" data-error-log="' + row.errorLog + '"></i>'
                    } else if (row.uploadStatus == "PROCESSING") {
                        return '<i class="fa fa-refresh fa-spin" aria-hidden="true" style="color:orange"></i>'
                    }
                }

            }
        ],
        scrollX:true,
        columnDefs: [
            {
                "type": 'fraction',
                "targets": 5  //to sort 6th column
            },
            {
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }
        ]
    });

    setInterval(function () {
        eudraDataTable.ajax.reload(null, false);
    }, 10000);

    $("input[name=dataType]:radio").on('click', function () {
        $("input[name=optDuplicate]:radio").attr('disabled', $(this).val() === "Case Listing");
        if ($(this).val() === "Case Listing") {
            $('.substanceNameSelector').css('visibility', 'visible');
            $("#substanceName").select2().data('select2').$dropdown.addClass('cell-break');
        } else {
            $('.substanceNameSelector').css('visibility', 'hidden');
        }
    });

    // Function to show modal for uploaded directories
    $(".show-directory").on('click', function () {
        var showEvdasFilesModal = $("#showEvdasFilesModal");
        $.ajax({
            cache: false,
            url: '/signal/evdasData/getFilesInfo',
        })
            .done(function (result) {
                var data_content = signal.utils.render('evdas_files', result);
                showEvdasFilesModal.find("#evdasFiles").html(data_content);
                $('#allowedProductList').pickList();
                $('.pickList_sourceListLabel').text('Unprocessed Files');
                $('.pickList_targetListLabel').text('Selected Files');
                showEvdasFilesModal.modal("show");
            })
            .fail(function (err) {
                console.log(err);
            })
    });

    $('#file').on('change', function () {
        if ($(this).val()) {
            if ($(this)[0].files[0].size > maxUploadLimit) {
                $.Notification.notify('error', 'top right', "Failed", $.i18n._('fileUploadMaxSizeExceedError', maxUploadLimit/1048576), {autoHideDelay: 5000});
                $(this).val('');
                $('input:submit').attr('disabled', true);
            } else {
                $('input:submit').attr('disabled', false);
            }
        } else {
            $('input:submit').attr('disabled', true);
        }
    });

    $('#formEvdasDataUpload').on('submit', function () {
        $('input:submit', this).attr('disabled', true);
        return true;
    });
    $('#submit').on('click', function () {
        $(".rxmain-container-content").append('<div id="compare-screens-spinner" > <div class="grid-loading spinner-compare"><img src="/signal/assets/spinner.gif"  width="30" align="middle"/></div></div>')
    });

});
