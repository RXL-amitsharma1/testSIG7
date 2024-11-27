var signal = signal || {};

signal.evdasCaseDrillDown = (function () {

    var init_case_drill_down_table = function (url, caseDetailUrl) {

        $("#susp-add-info").hover(function(){
            $(this).attr("title", "Suspect/interacting Drug List (Drug Char - Indication PT - Action taken - [Duration - Dose - Route])");
        });
        $("#con-med-info").hover(function(){
            $(this).attr("title", "Concomitant/Not Administered Drug List (Drug Char - Indication PT - Action taken - [Duration - Dose - Route])");
        });
        $("#event-term-info").hover(function(){
            $(this).attr("title", "Reaction List PT (Duration – Outcome - Seriousness Criteria)");
        });

        var table = $('#evdas-case-drill-down-table').DataTable({

            language: {
                "url": "../assets/i18n/dataTables_" + userLocale + ".json"
            },
            "ajax": {
                "url": url,
                "dataSrc": ""
            },

            "bLengthChange": true,
            "iDisplayLength": 50,
            "aLengthMenu": [[50, 100, 200, 500], [50, 100, 200, 500]],
            "oLanguage": {
                "oPaginate": {
                    "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                    "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                    "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                    "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
                },
            },
            drawCallback: function () {
                colEllipsis();
                webUiPopInit();
                $(".more-option").webuiPopover({
                    html: true,
                    trigger: 'hover',
                    content: function () {
                        return $(this).attr('more-data')
                    }
                });
            },
            initComplete: function () {
                if(table.rows().data().toArray().length===50){
                    // All 50 rows are fetched ==> more data exist
                    var caseSeriesMessage = "Showing 50 entries. Case List Generating in Background";
                    $.Notification.notify('warning', 'top right', "Warning", caseSeriesMessage, {autoHideDelay: 5000});
                    table.ajax.url(url+"&async="+true).load();
                }
            },
            "aoColumns": [
                {
                    "mData": "caseNum",
                    "mRender": function (data, type, row) {
                        return '<a target="_blank" href="' + (caseDetailUrl + "?" + signal.utils.composeParams({
                                caseNumber: row.caseNum,
                                version: row.version,
                                alertId: row.alertId,
                                exeConfigId:executedConfigId
                            })) + '">' + "<span data-field ='caseNum' data-id='" + row.caseNum + "'>" +
                            (row.caseNum) + "</span></a>"
                    },
                    "className": "col-min-150"

                },
                {
                    "mData": 'caseIdentifier',
                    "mRender": function (data, type, row) {
                        if (row.companyFlag) {
                            return '<a target="_blank" href="' + (caseDetailUrl + '?' + signal.utils.composeParams({
                                    wwid: row.caseIdentifier,
                                    alertId: row.alertId
                                })) + '">' +
                                "<span data-field ='caseNum' data-id='" + row.caseNum + "'>" +
                                (row.caseIdentifier) + "</span>" +
                                "</a>"
                        } else {
                            return row.caseIdentifier
                        }
                    },
                    "className": "col-min-250"

                },
                {
                    "mData": "hcp"
                },
                {
                    "mData": "ageGroup",
                    "className": "col-min-100"
                },
                {
                    "mData": "gender"
                },
                {
                    "mData": "dateFirstReceipt",
                    "className": "col-min-100"
                },
                {
                    "mData": "primarySourceCountryDesc",
                    "className": "col-min-100"
                },
                {
                    "mRender": function (data, type, row) {
                        var text = (row.suspList) ? row.suspList:'';
                        return addEllipsisWithEscape(text);

                    },
                    "className": "col-min-150 col-max-200"
                },
                {
                    "mRender": function (data, type, row) {
                        var text = (row.suspAddInfoList) ? row.suspAddInfoList:'';
                        return addEllipsisWithEscape(text);

                    },
                    "className": "col-min-150 col-max-200"
                },
                {
                    "mRender": function (data, type, row) {
                        var text = (row.concList) ? row.concList:'';
                        return addEllipsisWithEscape(text);

                    },
                    "className": "col-min-150 col-max-200"
                },
                {
                    "mRender": function (data, type, row) {
                        var text = (row.conMedInfoList) ? row.conMedInfoList:'';
                        return addEllipsisWithEscape(text);

                    },
                    "className": "col-min-150 col-max-200"
                },
                {
                    "mData": "reactList",
                    "mRender": function (data, type, row) {
                        var text = row.reactList;
                        if (data) {
                            text = data.split(",").join(', ')
                        } else {
                            text = "-"
                        }
                        return addEllipsisWithEscape(text);
                    },
                    "className": "col-min-150 col-max-200"
                },
                {
                    "mData": "eventAddInfoList",
                    "mRender": function (data, type, row) {
                        var text = row.eventAddInfoList;
                        if (data) {
                            text = data.split(",").join(', ')
                        } else {
                            text = "-"
                        }
                        return addEllipsisWithEscape(text);
                    },
                    "className": "col-min-150 col-max-200"
                },
                {
                    "mData": "icsr",
                    "mRender": function (data, type, row) {
                        return '<a target="_blank" href="' + row.icsr + '"><i class="fa fa-external-link"></i></a>'
                    }
                }
            ],
            scrollX:true,
            scrollY:"calc(100vh - 247px)",
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }]
        });
        return table
    };

    var bind_evdas_drill_down_table = function () {
        $(document).on('click', '.evdas-case-drill-down-link', function (evt) {
            evt.preventDefault();
            var srcEle = $(this);
            if(applicationName === "EVDAS Alert") {
                evdasAlertId = $(this).closest('tr').find('td.dt-center').children().next().attr('data-id');
            }else if(applicationName === "Aggregate Case Alert"){
                evdasAlertId = $(this).closest("tr").children().find(".copy-select").attr("data-id")
            }else if(applicationName === "EVDAS Alert on Demand"){
                evdasAlertId = $(this).closest("tr").find(".row-alert-id").val()
            }
            $('#evdas-case-drill-down-modal').attr('data-archive', srcEle.data('archive'));
            $('#evdas-case-drill-down-modal').attr('data-url', srcEle.attr('href'));
            $('#evdas-case-drill-down-modal').modal({});
        });

        $('#evdas-case-drill-down-modal').on('shown.bs.modal', function () {
            var drill_down_table = signal.utils.render('evdas_case_drill_down_table', {});
            $('#evdas-drill-down-table-container').html(drill_down_table);
            var url = $('#evdas-case-drill-down-modal').attr('data-url');
            init_case_drill_down_table(url, evdasCaseDetailUrl)
        })

        $('#evdas-case-drill-down-modal').on('hidden.bs.modal', function () {
            $('#evdas-drill-down-table-container').remove('evdas-case-drill-down-table');
            var drill_down_table = signal.utils.render('evdas_case_drill_down_table', {});
            $('#evdas-drill-down-table-container').html(drill_down_table);
        })


    };

    return {
        bind_evdas_drill_down_table: bind_evdas_drill_down_table
    }
})();
