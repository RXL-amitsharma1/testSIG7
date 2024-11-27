$(function () {
    dataSheetOptions();
    var data;
    if (typeof dataSheets === "undefined" ||  dataSheets === "" ) {
        data = {}
    } else {
        data = JSON.parse(dataSheets);
    }
    $("input[name='allSheets']").on('change', function () {
        $("#selectedDatasheets").val(!$("input[type='checkbox'][name='allSheets']:checked").val());
        $('#dataSheet').select2({placeholder: $.i18n._("selectOne"), allowClear:true});
        bindDatasheet2WithData($("#dataSheet"), dataSheetList, data);
    });
    bindDatasheet2WithData($("#dataSheet"), dataSheetList, data);

    $("#dataSheet").on('scroll', function() {
        if($(this).scrollTop() + $(this).innerHeight() >= $(this)[0].scrollHeight) {
            alert('end reached');
        }
    });
});

function dataSheetOptions() {
    var dataSheetOptions = $(".datasheet-options");
    let selectedDataSheet = []
    dataSheetOptions.hide();
    $('#selectedDatasheet').on('change', function () {
        if (!$(this).is(":checked")) {
            $("#dataSheet").find('option:selected').each(function () {
                selectedDataSheet.push({
                    value: $(this).val(),
                    text: $(this).text()
                });
            });
            $("#dataSheet").empty()
            dataSheetOptions.hide();
        } else {
            if (typeof selectedDataSheet !== "undefined" && Array.isArray(selectedDataSheet) && selectedDataSheet.length > 0) {
                // Temporarily hold the options to append later, PVS-71118
                const options = selectedDataSheet.map(function (option) {
                    return new Option(option.text, option.value, true, true);//Added for PVS-71982
                });
                $("#dataSheet").append(options).trigger('change.select2');
                selectedDataSheet = [];
            }
            dataSheetOptions.show();
        }
        $('#dataSheet').data('select2').selection.resizeSearch()
    });
}
$("#allSheets").on('change', function(){
    if($(this).is(':checked')){
        $(this).val('ALL_SHEET');
    }else{
        $(this).val('CORE_SHEET');
    }
});
function bindDatasheet2WithData(selector, dataSheetList, data) {
    return bindDataSheet2WithData(selector, dataSheetList, data);
}

