$(function () {
    function showGroupsWidget() {
        $("#groups").select2({
            placeholder: "Select groups",
            allowClear: true,
            width: "100%"

        });
    }
    showGroupsWidget();

})