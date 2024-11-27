var dataSources = {"PVA": "pva", "Vigi Base": 'vigibase', "FAERS": 'faers', 'EUDRA': 'eudra'};

$(function () {

    var clear_all_records_and_inputs = function () {
        if ($('#fromDate').length) {
            $('#search').prop('disabled', true);
            $("#fromDate").val('');
            $("#toDate").val('');
        }
        $('.clearProductValues').trigger('click');

        $("input.searchProducts").each(function () {
            $(this).val("")
        });

        $("div.productDictionaryValue").text("");
        $("#showProductSelection").html("");
        $("#productSelection").val("");
        $("#isMultiIngredient").val(false);

    };


    var close_and_add_selected_items = function () {
    };


    $('#dataSource').on('change', function () {
        clear_all_records_and_inputs();
        close_and_add_selected_items();
        if (this.value === dataSources.FAERS) {
            disableDictionaryValues(false, false, true, false, true);
            $(".repeat-container .dropdown-menu [data-value='hourly']").hide();
        } else if (this.value === dataSources.EUDRA) {
            disableDictionaryValues(true, false, true, true, true);
            $(".repeat-container .dropdown-menu [data-value='hourly']").hide();

        } else {
            disableDictionaryValues(false, false, false, false, false);
            $(".repeat-container .dropdown-menu [data-value='hourly']").show()

        }

    });
});
