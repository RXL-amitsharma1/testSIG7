var prevProducts;
var selectedProds= [];
var searchProduct;

var URL = getProductURL + "?searchProduct=";

var populateProducts = function () {
    var pickListItemHTML = "";
    var selectedItemHTML = "";
    searchProduct = $('#searchedProducts').val();

    $.ajax({
        url: URL + searchProduct,
        beforeSend: function () {
            $("#searchedProducts").prop("disabled", true);
        }
    })
        .done(function (result) {
            if(result){

                if($(".pickList_targetList").children()) {
                    prevProducts = $(".pickList_targetList").children();
                    prevProducts.each(function (index) {
                        selectedProds.push($(this).text());
                    });
                }

                $("#allowedProductList").empty();
                $.each(result, function(key,value) {
                    if(!(selectedProds.indexOf(value)>=0)) {
                        $("#allowedProductList").append($("<option></option>").attr("value", value).text(value));
                    }
                });

                $("#allowedProductList").pickList("destroy");
                $("#allowedProductList").pickList({
                    sortItems: false
                });


                $.each(selectedProds, function(i, prod) {
                    pickListItemHTML += "<li data-value= \"" + prod + "\" label= \"" + prod + "\" class=\"pickList_listItem\" >" + escapeHTML(prod) + "</li>";
                    selectedItemHTML += "<option value= \"" + prod + "\" + selected=\"selected\">" + escapeHTML(prod) + "</option>";
                });

                selectedProds = [];
                $(".pickList_targetList").append(pickListItemHTML);
                $("#allowedProductList").append(selectedItemHTML);
                $("#searchedProducts").prop('disabled', false);
            }
    });
};

var populateProductsForEdit = function(){
    var pickListItemHTML = "";
    var selectedItemHTML = "";
    var groupProducts = $("#savedProductsList").val();

    if(groupProducts!="" && groupProducts!=undefined) {
        $.each(groupProducts.split("#%#"), function (i, prod) {
            pickListItemHTML += "<li data-value= \"" + prod + "\" label= \"" + prod + "\" class=\"pickList_listItem\" >" + escapeHTML(prod) + "</li>";
            selectedItemHTML += "<option value= \"" + prod + "\" + selected=\"selected\">" + escapeHTML(prod) + "</option>";
        });
    }

    $(".pickList_targetList").append(pickListItemHTML);
    $("#allowedProductList").append(selectedItemHTML);

}
