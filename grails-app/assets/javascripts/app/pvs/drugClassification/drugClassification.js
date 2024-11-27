var userLocale = "en";
$(function () {

    $("#classification").prop('disabled', true);
    $('.add-classification-btn').css("pointer-events", "none");
    $("#selectedDatasource").select2({
        multiple: true
    });
    $('#selectedDatasource').val("faers").trigger('change.select2');
    $('#selectedDatasource').next(".select2-container").hide();
    $('#dataSourcesProductDict').val('faers').trigger('change.select2');

    $("#productModal").on('hidden.bs.modal', function () {
        if ($('#productSelection').val() == "") {
            var productsTemp = [];
            $(".removeSingleDictionaryProductValue").each(function () {
                var elem = JSON.parse($(this).attr("data-element"));
                elem.name = elem.name.toString().split("(")[0].trim();
                productsTemp.push(JSON.stringify(elem));
            })
            if (productsTemp != null && typeof productsTemp !== "undefined" && productsTemp !== "") {
                $('#productSelection').val('{"1":[],"2":[],"3":[' + productsTemp.toString() + '],"4":[],"5":[]}');
            }
        }
    })
    var clearForm = function () {
        //clear all text fields
        $('form[name="drug-classification-form"]')[0].reset();
        $('#added-classification').html('');

        //clear product selection
        $('#showProductSelection').html('');
        $('#productSelection').html('');
        $("ul.productDictionaryColWidth").find('li').each(function () {
            $(this).remove()
        });
        $("input.searchProducts").each(function () {
            $(this).val("")
        });
        $("div.productDictionaryValue").text("");
        productValues = {"1": [], "2": [], "3": [], "4": [], "5": []};


        //reload drug classification table
        var table = $('#drugClassificationTable').DataTable();
        table.ajax.reload();

        //enable add-on button
        $('.add-classification-btn').css("pointer-events", "auto");

        //show default buttons
        $('#save-drug-classification').show();
        $('#edit-drug-classification').hide();
        $('.clearProductValues').trigger('click');
    };
    // aaColumns are improperly defined
    var aoColumns = [
        {"mData": "product"},
        {"mData": "className"},
        {
            "mData": "addedClassification",
            "class": "textPre"
        }
    ];
    if (isAdmin) {
        aoColumns.push.apply(aoColumns, [{
            "mRender": function (data, type, row) {
                var actionButton = '<div class=" hidden-btn btn-group dropdown dataTableHideCellContent" style="min-width: 80px !important;" align="center"> \
                                                    <a class="btn btn-success btn-xs edit-record" href="#" data-className = "' + escapeHTML(row.className) + '">' + $.i18n._('edit') + '</a> \
                            <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                                <span class="caret"></span> \
                                <span class="sr-only">Toggle Dropdown</span> \
                            </button> \
                            <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                                <li role="presentation"><a role="menuitem" data-toggle="modal" \
                                        data-target="#deleteDrugModal" href="#" data-className="' + escapeHTML(row.className) + '" class="delete-record">' + $.i18n._('delete') + '</a></li> \
                            </ul> \
                        </div>';
                return actionButton;
            }
        }])
    }

    var showSuccessMessage = function (isSuccess, message) {
        var messageDiv = $('#error-message');
        if (isSuccess) {
            messageDiv = $('#success-message')
        }
        messageDiv.html(message);
        messageDiv.show();
        setTimeout(function () {
            messageDiv.hide();
        }, 10000);
    };


    //cancel button click event
    $('#cancel-drug-classification').on('click', function (e) {
        e.preventDefault();
        clearForm();
    });

    //Delete Modal
    $('#deleteDrugModal').on('show.bs.modal', function (event) {

        var button = $(event.relatedTarget); // Button that triggered the modal
        var name = button.data('classname');

        //deleting a drug classification
        $('#deleteDrugButton').on('click', function () {
            $.ajax({
                url: deleteDrugClassificationUrl + '?className=' +  encodeURIComponent(name),
                type: "POST",
            })
                .done(function (response) {
                    showSuccessMessage(response.status, response.message);
                    if (response.status) {
                        clearForm();
                    }
                })
        });

    });

    //save new drug classification
    $('#save-drug-classification').on('click', function (e) {
        e.preventDefault();
        var data = $('#drug-classification-form').serialize();
        var classifications = [];
        $.each($('#added-classification div'), function () {
            classifications.push({classificationName :$(this).data("name"),classificationType:$(this).data("type")})
        });
        $.ajax({
            type: "POST",
            dataType: "json",
            data : {classificationList : JSON.stringify(classifications)},
            url: saveDrugClassificationUrl + '?' + data,
        })
            .done(function (response) {
                showSuccessMessage(response.status, response.message);
                if (response.status) {
                    clearForm();
                }
            })
    });

    $('.add-classification-btn').on('click', function(){

        var classification = escapeHTML($('#classification').val());
        var classificationType = $('#classificationType');
        var addedClassification = classification + '(' + $('#classificationType :selected').text() + ')';
        $('#classification').val('');
        $('.classificationsDiv').show();
        if(classification){
            $('#added-classification').append("<div class='border' data-name='" + classification + "' data-type='" + classificationType.val() + "'>" +escapeHTML(addedClassification)+"</div>");
            classificationType.val('');
            classificationType.trigger('change');

        }
    });

    $('#classificationType').on('change', function(){
       if($(this).val() != ''){
           $("#classification").prop('disabled', false);
           $('.add-classification-btn').css("pointer-events", "auto");
       }else{
           $("#classification").prop('disabled', true);
           $('.add-classification-btn').css("pointer-events", "none");

       }
    });


    //drug classification table list
    var drugClassificationTable = $('#drugClassificationTable').DataTable({
        "language": {
            "url": "../assets/i18n/dataTables_" + userLocale + ".json"
        },
        drawCallback: function () {

            //editing a drug classification
            $('.edit-record').off('click');
            $('.edit-record').on('click', function () {
                clearForm();
                var className = $(this).data('classname');
                $.ajax({
                    url: fetchDrugClassificationUrl + '?className=' + encodeURIComponent(className),
                    type: "POST",
                })
                    .done(function (response) {
                        if (response.success) {
                            $('#save-drug-classification').hide();
                            $('#edit-drug-classification').show();

                            //populating fields
                            var valMap = response.valMap;
                            var prevClassName = valMap.className;
                            $('#classificationType').val(valMap.classificationType);
                            $('#classification').val(valMap.classification);
                            $('#className').val(prevClassName);
                            $('#productSelection').val(valMap.productSelection);
                            //todo:update
                            if (typeof productSelectionModal === undefined) {//"old dictionary"
                                productSelectionModal.loadProducts();
                            } else {//dictionary plugin
                                productDictionaryObj.setValues(JSON.parse($("#productSelection").val()));
                                productDictionaryObj.refresh();
                                var all_products = $("#productSelection").val();

                                if (all_products != "") {
                                    $("#showProductSelection").html("");
                                    $.each(JSON.parse(all_products), function (index, element) {

                                        var prod_to_show = [];
                                        if (element && element.length > 0) {
                                            $.each(element, function (idx, elem) {
                                                prod_to_show.push(elem.name);
                                            });
                                            $("#showProductSelection").append("<div style='padding: 2px'>" + prod_to_show + "</div>");
                                        }

                                    });
                                }
                            }
                            $('.add-classification-btn').css("pointer-events", "none");
                            var addedClassificationList = valMap.addedClassification;

                            $.each(addedClassificationList, function (propName,propVal) {
                                $('#added-classification').append("<div class='border' data-name='" + propVal.classification + "' data-type='" + propVal.classificationType + "'>" + escapeHTML(propVal.addedClassification)+"</div>");
                            });


                            //updating drug classification
                            $('#edit-drug-classification').off('click');
                            $('#edit-drug-classification').on('click', function (e) {
                                e.preventDefault();
                                var data = $('#drug-classification-form').serialize();
                                var classifications = [];
                                $.each($('#added-classification div'), function () {
                                    classifications.push({classificationName :$(this).data("name"),classificationType:$(this).data("type")})
                                });
                                $.ajax({
                                    type: "POST",
                                    dataType: "json",
                                    data : {classificationList : JSON.stringify(classifications),prevClassName: prevClassName},
                                    url: editDrugClassificationUrl + '?' + data,
                                })
                                    .done(function (response) {
                                        showSuccessMessage(response.status, response.message);
                                        if (response.status) {
                                            clearForm();
                                        }
                                    })
                            })

                        }
                    })
            });
        },

        initComplete: function () {
            //Init complete functions go here
            addGridShortcuts('#drugClassificationTable');
        },

        "ajax": {
            "url": drugClassificationListUrl,
            "cache": false,
            "dataSrc": ""
        },
        "aaSorting": [[1, "desc"]],
        "bLengthChange": true,
        "iDisplayLength": 50,
        "bProcessing": true,
        "oLanguage": {

            "sZeroRecords": "", "sEmptyTable": "No data available in table ",
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
        searching: true,
        "aoColumns": aoColumns,
        scrollX: true,
        scrollY: 'calc(100vh - 535px)',
        columnDefs: [{
            "targets": '_all',
            "render": $.fn.dataTable.render.text()
        }]
    });

    disableDictionaryValues(false, false, true, false, true);
});
