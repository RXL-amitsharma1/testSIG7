$(function() {
    $("#dispositionId .select2").select2();
    var init = function () {
        $("#select-groups").select2();
        $("#select-workflow-groups").select2();
        $("#topicCategories").select2();

        if(!isAdmin){
            $('#workflowRuleForm input,select').prop('disabled', true);
        }
    };

    init();
});