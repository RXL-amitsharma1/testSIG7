$(function () {
    var preventEnter = false;
    $(window).keydown(function (event) {
        if (event.keyCode == 13 && preventEnter )  {
            event.preventDefault();
        }
    });
    $('#productModal, #eventModal, #studyModal, #createActionModal').on('shown.bs.modal', function (e) {
        preventEnter = true;
        console.log('True');
    });
    $('#productModal, #eventModal, #studyModal, #createActionModal').on('hidden.bs.modal', function (e) {
        preventEnter = false;
        console.log('False');
    })
});
