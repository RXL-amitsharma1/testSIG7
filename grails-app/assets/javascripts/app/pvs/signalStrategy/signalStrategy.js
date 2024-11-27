//= require app/pvs/common/rx_common.js

var signal = signal || {}

signal.strategy = (function() {

    var init_strategy_table = function(url, createStrategyUrl) {

        var table = $('#strategyTable').DataTable({

            language: {
                "url": "../assets/i18n/dataTables_" + userLocale + ".json"
            },
            "ajax": {
                "url": url,
                "dataSrc": ""
            },
            "initComplete": function() {
                var $divToolbar = $('<div class="toolbarDiv col-md-7 col-xs-4"></div>');
                var $rowDiv = $('<div class=""></div>');
                $divToolbar.append($rowDiv);
                $("#strategyTable_filter").attr("class","");
                $($("#strategyTable_filter").children()[0]).attr("class","col-md-5 col-xs-7 ");
                $($("#strategyTable_filter").children()[0]).attr("style","font-weight:normal;float:right;width:auto;padding:0");
                $("#strategyTable_filter").prepend($divToolbar);
                tableElements($rowDiv);
                $("#create-strategy").on('click', function() {
                    location.href = createStrategyUrl;
                });
            },
            "bLengthChange": true,
            "iDisplayLength": 50,
            "aLengthMenu": [[50, 100, 200, 500], [50, 100, 200, 500]],
            "aoColumns": [
                {
                    "mData": 'name',
                    "mRender": function(data, type, row) {
                        return '<a style="cursor:pointer" href="'+ (editStrategyUrl + '?' +
                            signal.utils.composeParams({id: row.id}))+'">'+ row.name +
                           '</a>'
                    }
                },
                {
                    "mData" : "productSelection"
                },
                {
                    "mData": "type"
                },
                {
                    "mData" : "medicalConcepts"
                },
                {
                    "mData": "startDate"
                },
                {
                    "mData": 'description'
                },
            ],
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }]
        })

        return table
    }

    var tableElements =  function(toolbar) {
        var elements = '<div class="">';
        elements = elements + '&nbsp;&nbsp;&nbsp;<g:link class="create btn btn-success" id="create-strategy" style="float:right">Create</g:link>'
        elements = $(elements);
        $(toolbar).append(elements);
    }

    return {
        init_strategy_table: init_strategy_table
    }
})()
