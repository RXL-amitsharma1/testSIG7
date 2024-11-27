$(function () {

    $("#addRemoveUserModal").on('show.bs.modal', function (e) {
        let elements = document.getElementsByName("selectedUsers");
        if (typeof elements !== "undefined" && elements != null) {
            for (let i = 0; i < elements.length; i++) {
                let data = elements[i].defaultValue;
                $("ul.pickList_sourceList li").each(function () {
                    if (data == $(this).attr("data-value")) {
                        $("ul.pickList_targetList").append($(this));
                    }
                })
            }
            if (elements.length > 0) {
                $(".pickList_removeAll").removeAttr("disabled");
            }
        }
    });


    var groupRoleList = $("#groupRoleList").val()
    $(".bootstrap-switch-container").on('click', function () {
        if ($(this).css('margin-left') == '-40px') {
            $(this).removeAttr("style");
            $(this).attr('style', 'width: 120px');
            $(this).parent().parent().find("input").attr("value", "on");
        } else {
            $(this).removeAttr("style");
            $(this).attr('style', 'width: 120px;margin-left: -40px');
            $(this).parent().parent().find("input").attr("value", "off");
        }

    });

    $(".bootstrap-switch-container").each(function () {
        if (groupRoleList.includes($(this).find("input").attr("name"))) {
            $(this).removeAttr("style");
            $(this).attr('style', 'width: 120px');
            $(this).parent().parent().find("input").attr("value", "on");
        }
    })


    $('#allowedUsers').pickList({
        afterRefreshControls: enableDisableAddAllBtn,
        afterRefresh: caseInsensitiveSortWrapper
    });

    $(document).on('keyup', '.fieldNameFilter', function () {
        var f = $(this).val().toLowerCase();
        if (_.isEmpty(f)) {
            $(".pickList_controlsContainer").find(".pickList_addAll").attr("disabled", false);
        } else {
            $(".pickList_controlsContainer").find(".pickList_addAll").attr("disabled", true);
        }
        $(".pickList_sourceList li").each(function () {
            var elem = $(this);
            if (elem.html().toLowerCase().indexOf(f) > -1)
                elem.show();
            else
                elem.hide();
        });
    });
    $(".pickList_listLabel.pickList_sourceListLabel").html($(".pickList_listLabel.pickList_sourceListLabel").html() +
        '<br> <input class="fieldNameFilter" style="width:100%;" placeholder="Search" >');

});

function enableDisableAddAllBtn() {
    var f = $(".fieldNameFilter").val();
    if(_.isEmpty(f)) {
        $(".pickList_controlsContainer").find(".pickList_addAll").attr("disabled", false);
    }else {
        $(".pickList_controlsContainer").find(".pickList_addAll").attr("disabled", true);
    }
}

function caseInsensitiveSortWrapper() {
    var pickList_sourceList = $('.pickList_sourceList');
    var pickList_targetList = $('.pickList_targetList');
    caseInsensitiveSort(pickList_sourceList, 'label');
    caseInsensitiveSort(pickList_targetList, 'label');
}

function caseInsensitiveSort(list, sortItem) {
    var items = [];

    list.children().each(function () {
        items.push($(this));
    });

    items.sort(function (a, b) {
        var t1 = a.attr(sortItem).toLowerCase();
        var t2 = b.attr(sortItem).toLowerCase();
        return t1 > t2 ? 1 : t1 < t2 ? -1 : 0;
    });

    list.empty();

    for (var i = 0; i < items.length; i++) {
        list.append(items[i]);
    }
}

function changeUser() {
    var modalDiv = $('div#addRemoveUserModal');
    var itemIds = modalDiv.find("#allowedUsers").val();
    var $table = $('table.userTable');
    var selected = [];
    $(".pickList_targetList li").each(function (e) {
        selected.push($(this).attr("data-value"));
    });
    if (itemIds !== undefined) {
        var rows = $table.find("tr");
        for (var i = 1; i < rows.length; i++) {
            if (!$(rows[i]).hasClass("userTableHeader"))
                $(rows[i]).detach();
        }
        var items = modalDiv.find('ul.pickList_targetList li.pickList_listItem');
        for (var j = 0; j < items.length; j++) {
            var item = $(items[j]);
            $table.append('<tr><td>' + item.attr("label") + '<input type="hidden" name="selectedUsers" value="' + item.attr("data-value")
                + '"/></td></tr>');

        }


    }
    modalDiv.modal('hide');
}