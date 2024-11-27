var pt
var productDss
var socDss
var rationale
var dateRangeArray
var rationaleData
var historyData
var exConfigId
var configId
var hier_parent = ['LISTEDNESS', 'TREND','SEVERITY INCREASE','IME'];
function formatRationale ( d ) {

    var childNodes = $();
    var hasChildren = false;
    $.each(rationaleData, function(){
        if(d != undefined && this.parent === d.pv_concept){
            childNodes = childNodes.add('<tr>'+
                '<td></td>'+
                '<td>' + $('<div>').text(this.pv_concept).html()+'</td>'+
                '<td class ="text-center"><span class =" badge badge-'+this.potential_signal+'">' +$('<div>').text(this.potential_signal).html()+ '</span></td>'+
                '<td class ="text-center">' +$('<div>').text(this.score_confidence).html()+'</td>'+
                '<td>' + $('<div>').text(this.rationale).html() +'</td>'+
                '</tr>');

            hasChildren = true;
        }
    });

    return childNodes ;
}

function formatHistory ( d ) {
    var childNodes = $();
    var hasChildren = false;
    $.each(historyData, function(){
        if(d != undefined && this.parent === d.pv_concept) {

            var tableRow = '<tr>' +
                '<td></td>' +
                '<td>' + $('<div>').text(this.pv_concept).html() + '</td>';
            for (var i = 0; i < dateRangeArray.length; i++) {
                tableRow = tableRow+ '<td class ="text-center"><span class =" badge badge-'+this[dateRangeArray[i].trim()]+'">' +$('<div>').text(this[dateRangeArray[i].trim()]).html()+ '</span></td>';
            }
            tableRow = tableRow+"</tr>";
            childNodes = childNodes.add(tableRow);
            hasChildren = true;
        }
    });

    return childNodes ;
}

$(function () {

    dateRangeArray = abc.replace('[', "").replace(']','').split(',');

    $('#dss-modal').on('show.bs.modal', function (event) {
        $('a[href="#rationale"]').tab('show');
        pt  = $(event.relatedTarget).data('pt');
        productDss=$(event.relatedTarget).data('productdss')
        socDss=$(event.relatedTarget).data('socdss')
        exConfigId = $(event.relatedTarget).data('exconfigid')
        configId = $(event.relatedTarget).data('configid')
        var prevDisposition = $(event.relatedTarget).data('rationale');
        $("#confidence").text("Confidence in Potential Signal: " + $(event.relatedTarget).data('pecimpnumhigh'));
        $("#rationale-disposition").text("Prior Review: " + ((!prevDisposition) ? '-' : prevDisposition));
        $("#newCount").text("Cases in Current Period: " + $(event.relatedTarget).data('newcount'));

        
        var table =  $('#dssRationaleTable').DataTable({
            "ajax": {
                "url": dssRationaleUrl + "&pt=" +  window.encodeURIComponent(pt)+ "&productDss=" +  window.encodeURIComponent(productDss)+ "&socDss=" + window.encodeURIComponent(socDss)+ "&executedConfigId=" + window.encodeURIComponent(exConfigId) + "&configId=" + window.encodeURIComponent(configId),
                "dataType": "json",
                "dataSrc": function(d){
                    dateRangeArray = d.dssDateRange;
                    $("#current-date-range").text("Current Period: "+ dateRangeArray[0]);
                    updateTableHeaders(dateRangeArray);
                    rationaleData = d.aaData;
                    var dataParent = []
                    $.each(d.aaData, function(){
                        if(this.parent === null){
                            dataParent.push(this);
                        }
                    });
                    return dataParent;
                }
            },
            columns: [
                {
                    "className":  'dss-details-control',
                    "sortable": false,
                    "data":  null,
                    "defaultContent": '',
                },
                {
                    "mData": "pv_concept",

                }, {
                    "mData": "potential_signal",
                    "mRender": function (data, type, row) {
                        if(row.potential_signal == "No"){
                            return "<span class='badge badge-No' >" + row.potential_signal + "</span>"
                        }
                        else
                            return "<span class='badge badge-Yes'>" + row.potential_signal + "</span>"
                    }
                }, {
                    "mData": "score_confidence",
                }, {
                    "mData": "rationale",
                    "mRender": function (data, type, row) {
                        var abc = row.rationale;
                        var res = abc.split('_');
                        return res;
                    }
                }
            ],
            columnDefs: [
                {
                    "targets": 0,
                },
                {
                    "targets": 1,
                    "className": "text-left",
                },
                {
                    "targets": 2,
                    "className": "text-center",
                },
                {
                    "targets": 3,
                    "className": "text-center",
                },
                {
                    "targets": 4,
                    "className": "text-left",
                },{
                    "targets": '_all',
                    "render": $.fn.dataTable.render.text(),
                    orderSequence: ['desc', 'asc']
                }
            ],
            "createdRow": function( row, data, dataIndex ) {

                if ( hier_parent.includes(data["pv_concept"]) ) {
                    $("td.dss-details-control", row).removeClass("dss-details-control");
                }
            },
            "bLengthChange": false,
            "searching": false,
            "paging": false,
            "order": [[1,"asc"]],
            "destroy": true,
            info: false
        });

        $('#dssRationaleTable tbody').on('click', 'td.dss-details-control', function () {
            console.log('In click')
            var tr = $(this).closest('tr');
            var row = table.row( tr );
            if ( row.child.isShown() ) {
                // This row is already open - close it
                row.child.hide();
                tr.removeClass('shown');
            }
            else {
                // Open this row
                row.child( formatRationale(row.data()) ).show();
                tr.addClass('shown');
            }
        } );


        // Expand and collapse all the rows
        $("#expand-colapse-all-rationale").on('click', function () {
            if(this.classList.contains('double_down')){
                this.classList.remove('double_down');
                this.classList.add('double_up');

                // Enumerate all rows
                table.rows().every(function(){
                    // If row has details collapsed
                    if(!this.child.isShown()){
                        // Open this row
                        this.child(formatRationale(this.data())).show();
                        $(this.node()).addClass('shown');
                    }
                });

            }
            else if(this.classList.contains('double_up')){
                this.classList.remove('double_up');
                this.classList.add('double_down');
                table.rows().every(function(){
                    // If row has details expanded
                    if(this.child.isShown()){
                        // Collapse row details
                        this.child.hide();
                        $(this.node()).removeClass('shown');
                    }
                });
            }
            else{
                this.classList.remove('double_down');
                this.classList.add('double_up');
                table.rows(':not(.parent)').nodes().to$().find('td:first-child').trigger('click');
            }
        });


    });
    function updateTableHeaders(dssDateRange) {
        var tableHeadersPlaceholder = $('#tableHeadersPlaceholder');
        var headerContent = '<tr class="gray-strip1 common-bg">' +
            '<td id="expand-colapse-all-history" class="double_down"></td>' +
            '<th class="">PV Concept</th>';
        dssDateRange.forEach(function(dateRange) {
            headerContent += `<th class="" data-field="${dateRange}">
                                        <div class='stacked-cell-center-top'>${dateRange}</div>
                                    </th>`;
        });
        headerContent += '</tr>';
        tableHeadersPlaceholder.html(headerContent);
    }

    $('#detailed_history_tab').on('click', function (event) {
        var historyTable = $('#dssDetailsTable').DataTable({

            "ajax": {
                "url": dssHistoryUrl + "&pt=" + pt+ "&executedConfigId=" + window.encodeURIComponent(exConfigId)+ "&configId=" + window.encodeURIComponent(configId),
                "dataSrc": function(d){
                    historyData = d.aaData;
                    var dataParent = []
                    $.each(d.aaData, function(){
                        if(this.parent === null){
                            dataParent.push(this);
                        }
                    });
                    return dataParent;
                },
                "dataType": "json",
            },
            columns: addColumn(),
            columnDefs: [{
                "targets": 0,
                "sortable":false

            },
                {
                    "targets": 1,
                    "className": "text-left",
                },
                {
                    "targets": '_all',
                    "render": $.fn.dataTable.render.text()
                }],
            "createdRow": function( row, data, dataIndex ) {

                if ( hier_parent.includes(data["pv_concept"]) ) {
                    $("td.dss-details-control", row).removeClass("dss-details-control");
                }
            },
            "bLengthChange": false,
            "searching": false,
            "paging": false,
            "destroy": true,
            "order": [[1,"asc"]],
            info: false
        });

        $('#dssDetailsTable tbody').on('click', 'td.dss-details-control', function () {
            var tr = $(this).closest('tr');
            var row = historyTable.row( tr );
            if ( row.child.isShown() ) {
                // This row is already open - close it
                row.child.hide();
                tr.removeClass('shown');
            }
            else {
                // Open this row
                row.child( formatHistory(row.data()) ).show();
                tr.addClass('shown');
            }
        } );

        // Expand and collapse all the rows
        $("#expand-colapse-all-history").on('click', function () {
            if(this.classList.contains('double_down')){
                this.classList.remove('double_down');
                this.classList.add('double_up');

                // Enumerate all rows
                historyTable.rows().every(function(){
                    // If row has details collapsed
                    if(!this.child.isShown()){
                        // Open this row
                        this.child(formatHistory(this.data())).show();
                        $(this.node()).addClass('shown');
                    }
                });

            }
            else if(this.classList.contains('double_up')){
                this.classList.remove('double_up');
                this.classList.add('double_down');
                historyTable.rows().every(function(){
                    // If row has details expanded
                    if(this.child.isShown()){
                        // Collapse row details
                        this.child.hide();
                        $(this.node()).removeClass('shown');
                    }
                });
            }
            else{
                this.classList.remove('double_down');
                this.classList.add('double_up');
                historyTable.rows(':not(.parent)').nodes().to$().find('td:first-child').trigger('click');
            }
        });


    });


});

var addColumn = function() {
    var addColumns = [];

    addColumns.push.apply(addColumns, [{

        "className":  'dss-details-control',
        "data":  null,
        "sortable": false,
        "defaultContent": ''
    }]);
    addColumns.push.apply(addColumns, [{
        "mData": "pv_concept",
    }
    ]);
    $.each(dateRangeArray, function (key, value) {
        if (value !== "") {
            addColumns.push.apply(addColumns, [{
                "mData": value,
                "mRender": function (data, type, row) {
                    for (var i = 0; i < dateRangeArray.length; i++) {
                        if (row[dateRangeArray[i]] == "No")
                            return "<span class='badge badge-No' >" + row[dateRangeArray[i]] + "</span>";
                        else
                            return "<span class='badge badge-Yes'>" + row[dateRangeArray[i]] + "</span>";
                    }
                },
                "className": "text-center",

            }]);
        }
    });
    return addColumns
};