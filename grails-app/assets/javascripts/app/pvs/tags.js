function showTagWidget(isAdmin) {
    $("#tags").select2({
        tags: true,
        placeholder: "Select Tags",
        allowClear: true,
        width: "100%",
        createTag: function (params) {
            var term = $.trim(params.term);
            if (term != "" && isAdmin) {
                return {
                    id: term,
                    text: term,
                }
            }

            return null
        }
    })
}