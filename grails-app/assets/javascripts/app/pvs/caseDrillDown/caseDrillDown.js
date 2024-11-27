var signal = signal || {}

signal.caseDrillDown = (function() {

    var init_case_drill_down_table = function(url, caseDetailUrl) {

        var table = $('#case-drill-down-table').DataTable({
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
            "aoColumns": [
                {
                    "mData": "caseNum",
                    "mRender": function (data, type, row) {
                        return '<a target="_blank" href="'+ (caseDetailUrl + '?' + signal.utils.composeParams({caseNumber: row.caseNum, version: row.caseVersion}))+'">'+"<span data-field ='caseNum' data-id='" + row.caseNum + "'>" +
                            (row.caseNum) + "</span><span data-field ='caseVersion' data-id='" + row.caseVersion + "'>" +
                            "</span></a>"
                    }
                },
                {
                    mData: 'productName'
                },
                {
                    "mData": "genericName"
                },
                {
                    "mData": 'pt'
                },
                {
                    "mData": "soc"
                },
                {
                    "mData": "listedness"
                },
                {
                    "mData": "outcome"
                },
                {
                    "mData": "seriousness"
                },
                {
                    "mData": "determinedCausality"
                },
                {
                    "mData": "reportedCausality"
                },
            ],
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }]
        });

        return table;
    };


    var bind_drill_down_table = function(caseDetailUrl) {
        $(document).on('click', '.case-drill-down-link', function (evt) {
            //Predent default will make sure that default event does not happen.
            evt.preventDefault()

            var srcEle = $(evt.target)
            $('#case-drill-down-modal').attr('data-url', srcEle.attr('data-url'))
            $('#case-drill-down-modal').modal({})
        })

        $('#case-drill-down-modal').on('shown.bs.modal', function() {
            $('#drill-down-table-container').remove('#case-drill-down-table')
            var drill_down_table = signal.utils.render('case_drill_down_table', {})
            $('#drill-down-table-container').html(drill_down_table)
            var url = $('#case-drill-down-modal').attr('data-url')
            var case_drill_down_table = init_case_drill_down_table(url, caseDetailUrl)
        })
    }

    return {
        init_case_drill_down_table: init_case_drill_down_table,
        bind_drill_down_table :  bind_drill_down_table
    }
})()
