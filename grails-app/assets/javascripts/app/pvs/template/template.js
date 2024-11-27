    $(function () {

        var search = location.search.substring(1);
        var parametersFromUrl = JSON.parse('{"' + decodeURI(search).replace(/"/g, '\\"').replace(/&/g, '","').replace(/=/g,'":"') + '"}')
        var templateUrl = TEMPLATE.generatedReportsUrl + "?configId=" + parametersFromUrl.configId + "&alertId=" + parametersFromUrl.alertId  + "&preferredTerm="+ parametersFromUrl.preferredTerm + "&eventName="+ parametersFromUrl.eventName + '&isAggScreen=' + isAggScreen + "&aggExecutionId="+ parametersFromUrl.aggExecutionId;
        webUiPopInit();
        colEllipsis();
        productEllipses();
        $('#signalReportTable').DataTable({
            "sPaginationType": "full_numbers",
            "aaSorting": [[5, "desc"]], //By default sort on the basis of descending order of Generation Date.
            "bLengthChange": true,
            "iDisplayLength": 5,
            "pagination": true,
            "oLanguage": {
                "oPaginate": {
                    "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                    "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                    "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                    "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page

                },
                "sLengthMenu": "Show _MENU_",
            },
            "aLengthMenu": [[5, 10, 20, 50, -1], [5, 10, 20, 50, "All"]],
            "language": {
                "url": "../assets/i18n/dataTables_" + userLocale + ".json"
            },
            "ajax": {
                "url": templateUrl,
                "dataSrc": ""
            },
            "aoColumns": [
                {
                    "mData": 'reportName',
                    "mRender": function (data, type, row) {
                        return row.reportUrl ? '<a target="_blank" href="' + row.reportUrl + '">' + row.reportName + '</a>' : row.reportName
                    }
                },
                {"mData": 'productName'},
                {"mData": 'eventName'},
                {"mData": 'countType'},
                {"mData": 'generatedBy'},
                {"mData": 'generatedDate'},
                {
                    "mData": "",
                    "mRender": function (data, type, row) {
                        var downloadUrl = TEMPLATE.downloadReportUrl
                        var XLSXUrl = downloadUrl + "?outputFormat=XLSX&id=" + row.id
                        var pdfUrl = downloadUrl + "?outputFormat=PDF&id=" + row.id
                        if(row.isWordFileAvailable){
                            var docUrl = downloadUrl + "?outputFormat=DOCX&id=" + row.id
                        }

                        var downloadIcons = '<span><a target="_blank" href="' + pdfUrl + '">' +
                            '<img src="/signal/assets/pdf-icon.jpg" class="pdf-icon" height="16" width="16"/></a></span>'
                        downloadIcons = downloadIcons + "&nbsp"
                        downloadIcons = downloadIcons + '<span><a target="_blank" href="' + XLSXUrl + '">' +
                            '<img src="/signal/assets/excel.gif" class="pdf-icon" height="16" width="16"/></a></span>'
                        downloadIcons = downloadIcons + "&nbsp"
                        if(docUrl){
                            downloadIcons = downloadIcons + '<span><a target="_blank" href="' + docUrl + '">' +
                                '<img src="/signal/assets/word-icon.png" class="pdf-icon" height="16" width="16"/></a></span>'
                        }
                        if(isDMSEnabled){
                            downloadIcons = downloadIcons + '<span><a href="#" class="sendToDms" role="menuitem" data-doc-type="' + dmsDocTypeValue + '" data-id="' + row.id + '"' +
                                ' data-target="#sendToDmsModal" datsa-toggle="modal"><i data-toggle="tooltip" class="fa fa-upload font-16" title="Send to DMS"> </i></a></span>';
                        }
                        return downloadIcons
                    }
                }
            ],
            drawCallback: function() {
                if(!isAggScreen){
                    $('#signalReportTable').DataTable().column(2).visible(false);
                    $('#signalReportTable').DataTable().column(3).visible(false);
                }else{
                    $('#signalReportTable').DataTable().column(2).visible(true);
                }
            },
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }]
        });

        $('#rxTableTemplates').DataTable({
            "language": {
                "url": "../assets/i18n/dataTables_"+userLocale+".json"
            },
            "sPaginationType": "full_numbers",
            initComplete: function() {
                $('#rxTableTemplates tbody tr').each(function(){
                    $(this).find('td:eq(5)').attr('nowrap', 'nowrap');
                    $(this).find('td:eq(7)').attr('nowrap', 'nowrap');
                    $(this).find('td:eq(9)').attr('nowrap', 'nowrap');
                });

                $('.dt-search .dt-input').val("");
            },
            "ajax": {
                "url": TEMPLATE.listUrl,
                "dataSrc": "aaData"
            },
            processing: true,
            serverSide: true,
            searching: true,
            "aaSorting": [[ 5, "desc" ]],
            "bLengthChange": true,
            "iDisplayLength": 50,
            "pagination": true,
            "oLanguage": {
                "oPaginate": {
                    "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                    "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                    "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                    "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page

                },
                "sLengthMenu": "Show _MENU_",
            },
            "aLengthMenu": [[50, 100, 200, 500], [50, 100, 200, 500]],
            "aoColumns": [
                { "mData": "category", className:"col-min-150 col-max-150"},
                { "mData": "name",className:"col-min-150 col-max-150 col-height word-break"},
                { "mData": "description",className:"col-min-200 col-max-200" },
                { "mData": "lastUpdated",
                    "aTargets": ["lastUpdated"],
                    "className": "col-min-150 col-max-150 dataTableColumnCenter",
                    "mRender": function(data, type, full) {
                        var lastUpdated = new Date(data);
                        return moment(lastUpdated).tz(userTimeZone).format('lll');
                    }
                },
                { "mData": "owner.fullName" },
                { "mData": "dateCreated",
                    "aTargets": ["dateCreated"],
                    "className": "col-min-150 col-max-150 dataTableColumnCenter",
                    "mRender": function(data, type, full) {
                        var dateCreated = new Date(data);
                        return moment(dateCreated).tz(userTimeZone).format('lll');
                    }
                },
                {
                    "mData": null,
                    "sortable": false,
                    "aTargets": ["id"],
                    "mRender": function (data, type, full) {
                        var runReportUrl =TEMPLATE.runReport+'&type='+ parametersFromUrl.type +'&alertId='+ parametersFromUrl.alertId + '&selectedTemplate=' + data["id"] +  '&templateName=' + encodeURIComponent(data["name"]) + '&configId=' + parametersFromUrl.configId +'&typeFlag=' + parametersFromUrl.typeFlag+'&isAggScreen=' + isAggScreen;
                        if($('#selectedCases').val() && $('#selectedCases').val().length > 0 ){
                            runReportUrl+= '&selectedCases='+ $('#selectedCases').val()
                        }
                        if($('#versionNumber').text()){
                            runReportUrl+= '&version='+ $('#versionNumber').text()
                        }
                        if(parametersFromUrl.aggExecutionId && parametersFromUrl.aggExecutionId !== 'undefined' ){
                            runReportUrl+= '&aggExecutionId='+ parametersFromUrl.aggExecutionId
                        }
                        if(isAggScreen){
                            runReportUrl+= "&productName=" + parametersFromUrl.productName + "&preferredTerm="+ parametersFromUrl.preferredTerm + "&eventName="+ parametersFromUrl.eventName
                        }
                        var actionButton = '<div class="btn-group dropdown dataTableHideCellContent hidden-btn" align="center"> \
                                <a class="btn btn-success btn-xs" href="'+runReportUrl+'" >'+ $.i18n._('run') + '</a> \
                            </div>';
                        return actionButton;
                    },
                    "visible": isVisible()
                }
            ],
            scrollX: true,
            scrollY: "calc(100vh - 261px)",
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }]
        });
        actionButton('#rxTableTemplates');
    });

    function isVisible() {
        var isVisible = true;
        if(typeof hasReviewerAccess !== "undefined" && !hasReviewerAccess && typeof isAggScreen !== "undefined" && isAggScreen) {
            isVisible = false;
        }
        return isVisible
    }
