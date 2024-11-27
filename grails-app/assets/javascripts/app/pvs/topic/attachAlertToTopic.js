var showTopicWidget = function () {
    $("#topic").select2({
        tags: true,
        placeholder: "Select topic",
        allowClear: true,
        width: "100%",
        createTag: function (params) {
            var term = $.trim(params.term);
            if (term != "") {
                return {
                    id: term,
                    text: term
                }
            }
            return null
        }
    });
};