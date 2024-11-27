var case_form_list_table
var selectedCases = [];
var selectedCasesInfo = [];
var caseFormNames = [];
var selectedCasesInfoSpotfire = [];
$(function () {
    setCaseFormModal($("#case-form-modal"))
    alertNameClose();
    hoverCaseForm();
    hoverCaseFormPin();
});
var setCaseFormModal = function (modal) {
    $(".view-case-form-list").on('click', function (evt) {
        evt.preventDefault();
        modal.modal('show');
    })

    modal.on('shown.bs.modal', function (evt) {
        var formListUrl = "/signal/singleCaseAlert/fetchCaseForms?execConfigId=" + executedConfigId;
        case_form_list_table = $('#case-form-table').DataTable({
            destroy: true,
            "language": {
                "url": "../assets/i18n/dataTables_" + userLocale + ".json"
            },
            drawCallback: function () {

            },
            "initComplete": function (oSettings, json) {
            },
            "search": {
                "smart": false
            },
            "ajax": {
                "url": formListUrl,
                cache: false,
                "dataSrc": ""
            },
            "oLanguage": {
                "oPaginate": {
                    "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                    "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                    "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                    "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
                },
            },
            "searching": true,
            "bProcessing": true,
            "bLengthChange": true,
            "iDisplayLength": 5,
            "aaSorting": [[2, "desc"]],
            "aLengthMenu": [[5, 10, 20, -1], [5, 10, 20, "All"]],
            "aoColumns": [
                {
                    "mData": "reportName",
                    "mRender": function (data, type, row) {
                        return "<a href='/signal/singleCaseAlert/downloadCaseForm?id=" + row.id + "&outputFormat=PDF'>" + row.reportName + "</a>"
                    },
                    "className": "col-sm-4 cell-break"
                },
                {
                    "mData": "reportBy",
                    "className": "col-sm-3 cell-break"
                },
                {
                    "mData": "reportOn",
                    "className": "col-sm-3 cell-break"
                },
                {
                    "mRender": function (data, type, row) {
                        var downloadUrl = caseFormDowanloadUrl
                        var XLSXUrl = downloadUrl + "?outputFormat=XLSX&id=" + row.id
                        var pdfUrl = downloadUrl + "?outputFormat=PDF&id=" + row.id
                        var docUrl = downloadUrl + "?outputFormat=DOCX&id=" + row.id
                        var isFaers = false

                        var downloadIcons = '<span><a href="' + pdfUrl + '">' +
                            '<img src="/signal/assets/pdf-icon.jpg" class="pdf-icon" height="20" width="20"/></a></span>'
                        downloadIcons = downloadIcons + "&nbsp"
                        if(typeof $("#isFaers").val() != "undefined" && typeof $("#isFaers").val() != undefined && $("#isFaers").val() != '') {
                            isFaers = JSON.parse($("#isFaers").val());
                        }
                        if(row.excelGenerated && !isFaers) {
                            downloadIcons = downloadIcons + '<span><a href="' + XLSXUrl + '">' +
                                '<img src="/signal/assets/excel.gif" class="pdf-icon" height="20" width="20"/></a></span>'
                            downloadIcons = downloadIcons + "&nbsp"
                            downloadIcons = downloadIcons + '<span><a href="' + docUrl + '">' +
                                '<img src="/signal/assets/word-icon.png" class="pdf-icon" height="20" width="20"/></a></span>'
                        }else if(isFaers){
                            downloadIcons = downloadIcons + '<span><a href="' + XLSXUrl + '">' +
                                '<img src="/signal/assets/excel.gif" class="pdf-icon" height="20" width="20"/></a></span>'
                            downloadIcons = downloadIcons + "&nbsp"
                            downloadIcons = downloadIcons + '<span title="Word Format not available for Faers data source" style="cursor: not-allowed;"><a style="pointer-events: none;" href="' + docUrl + '">' +
                                '<img src="/signal/assets/word-icon.png" class="pdf-icon" height="20" width="20"/></a></span>'
                        } else{
                            downloadIcons = downloadIcons + '<span title="This Case Form was only generated in the PDF \n format as part of the older implementation, \n therefore MS Excel or MS Word files are not \n available for this Case Form." style="cursor: not-allowed;"><a style="pointer-events: none;" href="' + XLSXUrl + '">' +
                                '<img src="/signal/assets/excel.gif" class="pdf-icon" height="20" width="20"/></a></span>'
                            downloadIcons = downloadIcons + "&nbsp"
                            downloadIcons = downloadIcons + '<span title="This Case Form was only generated in the PDF \n format as part of the older implementation, \n therefore MS Excel or MS Word files are not \n available for this Case Form." style="cursor: not-allowed;"><a style="pointer-events: none;" href="' + docUrl + '">' +
                                '<img src="/signal/assets/word-icon.png" class="pdf-icon" height="20" width="20"/></a></span>'
                        }
                        return downloadIcons
                    },
                    "className": "sorting_disabled col-sm-2"
                }
            ],
            scrollX: true,
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }]
        })
    });

    modal.on('hidden.bs.modal', function (evt) {
        modal.find("#case-form-table").empty();
    });
    return case_form_list_table
};

function fetchCaseFormNames() {
    $.ajax({
        url: "/signal/singleCaseAlert/fetchCaseFormNames",
        data: {execConfigId: executedConfigId}
    })
        .done(function (result) {
            caseFormNames = result.names
        })
        .fail(function () {
            caseFormNames = []
    });
}

function populateSelectedCases() {
    $(".copy-select").on('change', function () {
        if (selectedCases.indexOf($(this).attr("data-id")) == -1 && $(this).is(':checked')) {
            selectedCases.push($(this).attr("data-id"));
            var currentIndex = $(this).closest('tr').index();
            if(typeof isCaseDetailView !== "undefined" && isCaseDetailView == "true"){
                currentIndex = currentIndex/2;
            }
            selectedCasesInfo.push(populateDispositionDataFromGrid(currentIndex));
            selectedCasesInfoSpotfire.push(populateDispositionData(currentIndex));
        } else if (selectedCases.indexOf($(this).attr("data-id")) != -1 && !$(this).is(':checked')) {
            selectedCasesInfo.splice($.inArray($(this).attr("data-id"), selectedCases), 1);
            selectedCasesInfoSpotfire.splice($.inArray($(this).attr("data-id"), selectedCases), 1);
            selectedCases.splice($.inArray($(this).attr("data-id"), selectedCases), 1);
        }
    });
}

var populateDispositionData = function (index) {
    var data = {};
    var rowObject = table.rows(index).data()[0];
    data['ptCode'] = rowObject.ptCode;
    data['productId'] = rowObject.productId;
    if(rowObject.preferredTerm !== undefined) {
        if (rowObject.preferredTerm.endsWith("(Narrow)")) {
            data['smqType'] = 2
        } else if (rowObject.preferredTerm.endsWith("(Broad)")) {
            data['smqType'] = 1
        } else {
            data['smqType'] = 0
        }
    }
    data['alert.id'] = rowObject.id;
    return data;
};

function populateSelectedCasesAdhoc() {
    $(".copy-select").on('change', function () {
        if (selectedCases.indexOf($(this).attr("data-id")) == -1 && $(this).is(':checked')) {
            selectedCases.push($(this).attr("data-id"));
        } else if (selectedCases.indexOf($(this).attr("data-id")) != -1 && !$(this).is(':checked')) {
            selectedCases.splice($.inArray($(this).attr("data-id"), selectedCases), 1);
        }
    });
}

function exportCaseForm() {
    $("#case-form-name-modal").find('.save-case-form').on('click', function () {
        $(".save-case-form").prop('disabled', true);
        var url = $("#case-form-name-modal").find('#case-form-url').val();
        var filename = $("#case-form-name-modal").find('#case-form-file-name').val();
        if (filename.trim() == "") {
            $("#form-name-error").empty()
            $("#form-name-error").closest(".alert-dismissible").removeClass("hide")
            $("#form-name-error").append("File Name field is mandatory")
        } else if (caseFormNames.indexOf(filename) != -1) {
            $("#form-name-error").empty()
            $("#form-name-error").closest(".alert-dismissible").removeClass("hide")
            $("#form-name-error").append("<li>The case form report name is already in use.</li>")
        } else if(selectedCases.length > 50000 || (recordsFiltered > 50000 && selectedCases.length === 0)){
            // limiting due to Jasper limitation
            $("#form-name-error").empty()
            $("#form-name-error").closest(".alert-dismissible").removeClass("hide")
            $("#form-name-error").append("<li>Total number of cases exceeds the maximum limit allowed for report</li>")
        }else {
            $.ajax({
                url: url,
                data: {filename: filename}
            })
                .done(function (result) {
                    $("#case-form-name-modal").modal('hide');
                    $.Notification.notify('success', 'top right', "Success", "Your report is generating.", {autoHideDelay: 5000});
                })
                .fail(function () {
                    $("#case-form-name-modal").modal('hide');
            });
        }
        sleepCaseForm(600).then(() => {
            $(".save-case-form").prop('disabled', false);
        });
    })
}
function sleepCaseForm(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}
function alertNameClose() {
    $(".alert-name-close").on('click', function () {
        $(this).closest(".alert-dismissible").addClass('hide')
    })
}

function hoverCaseForm() {
    $(".dropdown-case").hover(function () {
        $(this).find('.ul-ddm-child').removeClass("hide");
    }, function () {
        $(this).find('.ul-ddm-child').addClass("hide");
    });
}

function hoverCaseFormPin() {
    $(".dropdown-case-pin").hover(function () {
        $(this).find('.ul-ddm-child').removeClass("hide");
    }, function () {
        $(this).find('.ul-ddm-child').addClass("hide");
    });
}