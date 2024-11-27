var selectedElement;

$(function () {
    var mapSelectedElementToGlobalVariable = function () {
        $("ul.pickList_targetList li").on('click', function () {
            selectedElement = this;
        });
    };

    var getItemsMap = function () {
        var userPreference = {};

        userPreference.required = getSelectedListItems($('ul.pickList_targetList'));
        userPreference.ignore = getSelectedListItems($('ul.pickList_sourceList'));

        return userPreference
    };

    var getSelectedListItems = function (element) {
        var phrases = [];

        $(element).each(function () {
            $(this).find('li').each(function () {
                // cache jquery var
                var current = $(this);
                phrases.push(current.text());
            });

        });
        return phrases;
    };

    var saveSummaryReportPreferenceForUser = function () {
        var itemStr = getItemsMap();
        var validatedSignalId = $("#signalIdPartner").val();
        $.ajax({
            url: '/signal/validatedSignal/saveSummaryReportPreferenceForUser',
            type: 'post',
            data: {'preference': JSON.stringify(itemStr), 'validatedSignal.id': validatedSignalId},
        })
            .fail(function () {
                //TODO error handling
                console.log('Error!!!!')
            })
            .done(function () {
                $('#signalSummaryReportPreferenceModal').modal('hide')
            })
    };

    var init = function () {
        $('.signalSummaryReportPreferenceSave').on('click', saveSummaryReportPreferenceForUser);

        $('#signalSummaryReportUserPreference').pickList(
            {
                sourceListLabel: "Available Sections",
                targetListLabel: "Selected Sections",
                sortAttribute: "value"
            }
        );

        ignored.forEach(function (data) {
            $("#signalSummaryReportUserPreference").pickList("insert",
                {
                    label: data
                });
        });
        required.forEach(function (data) {
            $("#signalSummaryReportUserPreference").pickList("insert",
                {
                    label: data,
                    selected: true
                });
        });


        $.fn.moveUp = function () {
            before = $(this).prev();
            $(this).insertBefore(before);
        };

        $.fn.moveDown = function () {
            after = $(this).next();
            $(this).insertAfter(after);
        };

        mapSelectedElementToGlobalVariable();
    };

    init();

    $('div.pickList_controlsContainer button').on('click', function () {
        mapSelectedElementToGlobalVariable();
    });

});
