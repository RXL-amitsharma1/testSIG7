//= require js/jquery-ui-1.10.4.custom.min
//= require jquery/jquery-picklist

$(function() {
    var init = function() {
        $('#allowedProductList').pickList()
        $("#searchProd").on('click', function () {
            populateProducts();
        });

        var savedList = $("#savedProductsList").val();

        if(typeof savedList != "undefined" && savedList != ""){
            populateProductsForEdit();
        }
    }

    init()
});
