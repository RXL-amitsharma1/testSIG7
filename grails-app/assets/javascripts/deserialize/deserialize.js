$.fn.deserialize = function (serializedString)
{
    var $form = $(this);
    if($form[0] != undefined) {
        $form[0].reset();
    }
    serializedString = serializedString.replace(/\+/g, '%20');
    var formFieldArray = serializedString.split("&");
    $.each(formFieldArray, function(i, pair){
        var nameValue = pair.split("=");
        var name = decodeURIComponent(nameValue[0]);
        var value = decodeURIComponent(nameValue[1]);
        // Find one or more fields
        var $field = $form.find('[name=' + name + ']');
        if (($field[0] != undefined) && ($field[0].type == "radio"
            || $field[0].type == "checkbox"))
        {
            var $fieldWithValue = $field.filter('[value="' + value + '"]');
            var isFound = ($fieldWithValue.length > 0);
            if (!isFound && value == "on") {
                $field.first().prop("checked", true);
            } else {
                $fieldWithValue.prop("checked", isFound);
            }
        } else if($field.hasClass('select2')) {
            if(value.split(',').length > 1) {
                $field.val(value.split(','));
            } else {
                $field.val(value);
            }
        }
        else {
            if (($field[0] != undefined) && ($field[0].type == "select-one") && ($field.hasClass('select2'))) {
                $field.val(value);
            } else {
                $field.val(value);
            }
        }
    });
}
