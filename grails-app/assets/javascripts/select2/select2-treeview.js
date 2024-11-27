function getSelect2TreeView(selectElem) {
    return selectElem.select2({
        escapeMarkup: function (markup) {
            return markup;
        },
        formatResult: function (data) {
            if (data.loading) {
                return data.text;
            }
            var markup = "";
            if (data.children) {
                markup = "<div class='select2-treeview'><div class='select2-treeview-triangle select2-treeview-down'></div><span>" + data.text + "</span></div>";
            } else {
                markup = "<div class='select2-treeview-item'><span>" + data.text + "</span></div>";
            }
            return markup;
        },
        formatSelection: function (data) {
            return data.text;
        },

        queryComplete: function (select2, term) {
            select2.results.children().click(function () {

                var triangle = $(this).find(".select2-treeview-triangle");
                if (triangle.hasClass("select2-treeview-down")) {
                    triangle.removeClass("select2-treeview-down").addClass("select2-treeview-right");
                } else {
                    triangle.removeClass("select2-treeview-right").addClass("select2-treeview-down");
                }

                $(this).children("ul").toggle();

            }).click();

            var highlighted = select2.results.find('.select2-highlighted');

            highlighted.parent().show();

            if (!(highlighted.hasClass("select2-results-dept-0") && highlighted.hasClass("select2-result-selectable"))) {
                var triangle = highlighted.parent().parent().find(".select2-treeview-triangle");
                triangle.removeClass("select2-treeview-right").addClass("select2-treeview-down");
            }

            select2.results.scrollTop(highlighted.offsetTop - 35 - 29);
        },
        placeholder: "Select Field",
        allowClear: true,
        minimumResultsForSearch: 15
    });
}