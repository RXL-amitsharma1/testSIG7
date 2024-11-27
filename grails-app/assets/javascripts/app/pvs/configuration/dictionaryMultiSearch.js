var delimiter = null;

$(function () {
    $(".confirm-paste-dic-values").prop('disabled', true);
    var selectedField;
    $("input.searchProducts, input.searchStudies, input.searchEvents").on("focusin", function () {
        showPencilIcon($(this));
        selectedField = $(this);
    }).on("focusout", function (event) {
        var self = $(this);
        setTimeout(function () {
            hidePencilIcon(self);
        }, 500);
    });
    $('#copyAndPasteDicModal').on('show.bs.modal', function () {
        $("#productModal").css('z-index', 1000);
        $("#eventModal").css('z-index', 1000);
        $("#studyModal").css('z-index', 1000);
        $(this).find("input[name=delimiter][value='none']").prop("checked", true);
    });

    $('#copyAndPasteDicModal').on('hidden.bs.modal', function () {
        resetForm($('#copyAndPasteDicModal'));
        $(".confirm-paste-dic-values").prop('disabled', true);
        $("#productModal").css('z-index', 1050);
        $("#eventModal").css('z-index', 1050);
        $("#studyModal").css('z-index', 1050);
        $("body").addClass("modal-open");
    });
    $('.copyPasteContent').on('input propertychange', function() {
        $(".confirm-paste-dic-values").prop('disabled', true);
        if(this.value.length){
            $(".confirm-paste-dic-values").prop('disabled', false);
        }
    });
    function showPencilIcon(elem) {
        elem.prev().find(".fa-pencil-square-o").css("opacity","1.0");
    }

    function hidePencilIcon(elem) {
        elem.prev().find(".fa-pencil-square-o").css("opacity","0.01");
    }

    $(document).on('click', '.confirm-paste-dic-values', function (evt) {
        evt.preventDefault();
        $(".confirm-paste-dic-values").prop('disabled', true);
        var container = $("#copyAndPasteDicModal");

        delimiter = getCopyAndPasteDelimiter(container);
        var pasteContent = container.find('.copyPasteContent').val();

        var delimiterRegex = new RegExp(delimiter, "g");
        pasteContent = pasteContent.replace(delimiterRegex, DEFAULT_DELIMITER);
        delimiter = DEFAULT_DELIMITER;

        var level = selectedField.attr("level");

        if (delimiter != null) {
            if (delimiter == '|') {
                delimiter = '\\|';
            }

            if (delimiter == '\\n') {
                delimiter = '\\\\n+';
                pasteContent = pasteContent.replace(/\n\r?/g, '\\n');
            }
        }

        if (pasteContent != null && !_.isEmpty(pasteContent)) {
            selectedField.val(pasteContent);
            if($(".selectedDatasource").val() == 'eudra'){
                var e = $.Event('keyup');
                e.keyCode= 13; // enter key event
                selectedField.trigger(e);
            }else{
                selectedField.trigger("focusout");
            }
        }
    });

    function getCopyAndPasteDelimiter(container) {
        var selectedValue = $(container).find('input:radio[name^=delimiter]:checked').val();
        if (selectedValue === 'none') {
            return null;
        } else if (selectedValue === 'others') {
            var value = $(container).find('.c_n_p_other_delimiter').val();
            if (_.isEmpty(value)) {
                return null;
            }
            else {
                return value;
            }
        } else {
            return selectedValue;
        }
    }
});
