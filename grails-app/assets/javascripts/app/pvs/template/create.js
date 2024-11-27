function removeField(selectedButton){
    removeAlert();
    $(selectedButton).parent().remove();
}
function removeAllFields(){
    removeAlert();
    $("#fieldList").empty();
}
function removeAlert(){
    $("#fieldAlert").empty();
}
function text12(){

}
$(function () {

    function createDiv(val, id) {
        var strNewField = '<div class="additionalFieldInputs">' +
            '<input class="add-margin-right" id="fieldvalSelected" type="text" name="fieldNameSelected[]" value="' + val + '" disabled="disabled"/>' +
            '<input type="hidden" id="fieldIdSelected" name="selectedColumns" value="' + id + '"/>' +
            '<button type="button" class="btn btn-default btn-xs" onclick="removeField(this);" >-</button>' +
            '</div>" ';
        $(strNewField).appendTo("#fieldList");
    }
    $("#fieldNames").on('change', function() {
        removeAlert();
    });

    $("#addOne").on('click', function() {
        removeAlert();
        var fieldName = $("#fieldNames option:selected").text();
        var fieldId = $("#fieldNames").val();
        var nameFound = false;
        var $inputs = $('#fieldList :input');

        $inputs.each(function() {
            if($(this).val() == fieldName){
                nameFound = true;
            }
        });
        if(!nameFound){
            createDiv(fieldName, fieldId);
        } else {
            var strNewAlert = '<label class="alert alert-danger alert-addfield" role="alert">This field has already been added!</label>';
            $(strNewAlert).appendTo("#fieldAlert");
        }
    });

    $("#addAll").on('click', function(){
        removeAlert();
        var values = [];
        var ids = [];
        var inputs = $('#fieldList :input');
        $("#fieldNames option").each(function(){
            values.push( this.text);
            ids.push( this.value);
        });

        if (inputs.length == 0) {
            while(values.length > 0) {
                var val = values.pop();
                var id = ids.pop();
                createDiv(val, id);
            }
        } else {
            while(values.length > 0) {
                var nameFound = false;
                var k = values.pop();
                var i = ids.pop();
                inputs.each(function() {
                    if($(this).val() == k){
                        nameFound = true;
                    }
                });
                if(!nameFound){
                    createDiv(k, i);
                }
            }
        }

    });
    $("#myform").on('submit', function(e) {
        var inputs = $('#fieldList :input[id="fieldIdSelected"]');

        //prevent Default functionality
        e.preventDefault();
        var fields = [];
        inputs.each(function() {

            fields.push ($(this).val());
        });

        $.ajax({
            url: 'saveTemplate',
            type: 'post',
            dataType: 'json',
            data: $("#myform").serialize(),

        })
            .done(function(data) {
                $('<label class="alert alert-success alert-addfield" role="alert">Saved New Template!</label>').appendTo("#myform");
            });

    });
});