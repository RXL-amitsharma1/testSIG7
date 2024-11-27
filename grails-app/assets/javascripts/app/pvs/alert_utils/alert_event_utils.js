//= require app/pvs/alert_utils/common_key_prevent.js

var eventSelectionModal = {};
var searchListData  //This Variable is used to store the data which is to be added when user clicks AddAll

$(function () {

    var dicLevelMap = {1: 'SOC', 2: 'HLGT', 3: 'HLT', 4: 'PT', 5: 'LLT', 6: 'Synonyms', 7: '', 8: ''};
    var eventValues = {"1": [], "2": [], "3": [], "4": [], "5": [], "6": [], "7": [], "8": []};
    var init = function () {
        $("#eventModal").find("input.searchEvents").on('keyup', function (e) {
            if (e.keyCode == 13) {
                $("#fetchingEvents").show();
                $.ajax({
                    cache: false,
                    url: '/signal/pvsEventDictionary/searchEvents',
                    data: {
                        contains: $(this).val(),
                        dictionaryLevel: $(this).attr('level'),
                        selectedDatasource: $(".selectedDatasource").val()
                    },
                })
                    .done(function (data) {
                        searchListData=data
                        clear_all_records();
                        addAllEventValues();
                        $(data).each(function (index) {
                            clear_all_inputs_except($(this).attr('level'));
                            var containerLevel = dicLevelMap[$(this).attr('level')];
                            $("ul.eventDictionaryColWidth[dictionarylevel='" + containerLevel + "']").append(
                                "<li tabindex='0' role='button' class='dicLi' data-value='" + data[index].id + "' dictionarylevel='" +
                                data[index].level + "'>" + data[index].name + "</li>")
                        });
                        $("#fetchingEvents").hide();
                        init_get_components();
                    })
                    .fail(function () {
                        $("#fetchingEvents").hide();
                })
            }
        });


        $("button.addAllEvents").on("click", function (event) {
            if ($('#eventModal').hasClass('in')) {
                close_and_add_selected_items();
            }
        });

        $("button.addEventValues").on("click", function (event) {
            if ($('#eventModal').hasClass('in')) {
                add_event_selected_items()
            }
        });

        $("button.clearEventValues").on("click", function (event) {
            if ($('#eventModal').hasClass('in')) {
                clear_all_records_and_inputs();
            }
        });

        $('#smqValues').select2({
            placeholder: "Select one"
        }).on('change', function (e) {
            if ($(this).val()) {
                $(".searchEvents").attr("disabled", true).val('');
                $(".eventDictionaryColWidth li").remove();
                $(".eventDictionaryValue").not('.level7').html('');
                eventValues = {"1": [], "2": [], "3": [], "4": [], "5": [], "6": [], "7": [], "8": []};
            } else {
                $(".searchEvents").attr("disabled", false);
            }
        });

        load_events();
    };

    var clear_all_records = function () {
        $("ul.eventDictionaryColWidth").find('li').each(function () {
            $(this).remove();
        })
    };

    var addAllEventValues = function () {
        $(document).on('click', '.addAllEventValues', function () {
            var eventModal = $("#eventModal");
            $(searchListData).each(function (index) {
                var id = searchListData[index].id;
                var name = searchListData[index].name;
                var level = searchListData[index].level;
                var oldValue = eventModal.find("div.level" + level).text();
                if (oldValue) {
                    oldValue = oldValue + ", ";
                }

                var selectedValues = oldValue.split(',');
                selectedValues = _.map(selectedValues, function (v) {
                    return v.trim().toLowerCase()
                });

                if (!_.contains(selectedValues, name.trim().toLocaleLowerCase())) {
                    $("div.selectedEventDictionaryValue").find("div.level" + level).text(oldValue + " " + name);
                    if (level) {
                        var selectedObj = {"name": name, "id": id};
                        eventValues[level].push(selectedObj)
                    }
                }

            })
            load_events();

        });
    }

    var clear_all_records_and_inputs = function () {
        $("ul.eventDictionaryColWidth").find('li').each(function () {
            $(this).remove();
        });
        searchListData=[];

        $("input.searchEvents").each(function () {
            $(this).val("");
        });

        $("div.eventDictionaryValue").text("");

        $('#smqValues').val(null).trigger("change");

        eventValues = {"1": [], "2": [], "3": [], "4": [], "5": [], "6": [], "7": [], "8": []};
    };

    var clear_all_child_records = function (level) {
        var selectedLevel = parseInt(level);
        var levelVal = 0;
        while (levelVal <= 6) {
            levelVal = levelVal + 1;
            if (levelVal != selectedLevel) {
                $("ul.eventDictionaryColWidth").find("li[dictionarylevel=" + levelVal + "]").remove()
            }
        }
    };

    var clear_all_pre_levels = function (level) {
        var levelVal = parseInt(level);
        var plevel = 1;
        while (plevel < levelVal) {
            $("ul.eventDictionaryColWidth").find("li[dictionarylevel=" + plevel + "]").remove()
            plevel = plevel + 1;
        }
    };

    var clear_all_inputs_except = function (level) {
        $("input.searchEvents").each(function () {
            if ($(this).attr('level') != level) {
                $(this).val("")
            }
        })
    };

    var init_get_components = function () {
        $("ul.eventDictionaryColWidth").find('li').on('click', function (event) {
            var eventId = $(this).attr("data-value");
            var dicLevel = $(this).attr("dictionarylevel");

            deselect_items();
            deselect_items_except(dicLevel);
            $(this).addClass('selected selectedBackground');
            clear_all_child_records($(this).attr('dictionarylevel'));
            ($(this).attr('dictionarylevel'));
            get_selected_event(eventId, dicLevel);
            get_pre_level_parents(dicLevel, eventId)
        })
    };

    var get_new_components = function (elemId) {
        $("ul.eventDictionaryColWidth").find('li[data-value=' + elemId + ']').on('click', function (event) {
            var eventId = $(this).attr("data-value");
            var dicLevel = $(this).attr("dictionarylevel");
            $(this).addClass('selectedBackground selected');
            clear_all_child_records($(this).attr('dictionarylevel'));
            get_selected_event(eventId, dicLevel)
        })
    };

    var get_selected_event = function (eventId, dicLevel) {
        $("#fetchingEvents").show();
        $.ajax({
            cache: false,
            url: '/signal/pvsEventDictionary/getSelectedEvent',
            data: {
                dictionaryLevel: dicLevel,
                eventId: eventId,
                selectedDatasource: $(".selectedDatasource").val()
            }
        })
            .done(function (data) {
                var nextLevelItems = data.nextLevelItems;
                $.each(nextLevelItems, function (index, element) {
                    if ($("ul.eventDictionaryColWidth[dictionarylevel='" + dicLevelMap[element.level] +
                        "'] li[data-value='" + element.id + "'").length == 0) {
                        if (nextLevelItems.length == 1) {
                            $("ul.eventDictionaryColWidth[dictionarylevel='" + dicLevelMap[element.level] + "']").append(
                                "<li tabindex='0' role='button' class='highlighted selected dicLi' data-value='" + element.id +
                                "' dictionarylevel='" + element.level + "'>" + element.name + "</li>")
                        } else {
                            $("ul.eventDictionaryColWidth[dictionarylevel='" + dicLevelMap[element.level] + "']").append(
                                "<li tabindex='0' role='button' class='dicLi' data-value='" + element.id + "' dictionarylevel='" +
                                element.level + "'>" + element.name + "</li>")
                        }
                        get_new_components(element.id);
                    }
                });
                select_item();
                $("#fetchingEvents").hide();
            })
            .fail(function () {
                $("#fetchingEvents").hide();
        })
    };

    var get_pre_level_parents = function (level, eventId) {
        var eventIds = "";
        if (eventId == undefined) {
            $.each($("ul li[dictionarylevel='" + level + "']"), function () {
                eventIds += $(this).attr("data-value") + ","
            })
        } else {
            eventIds = eventId;
        }
        $.ajax({
            cache: false,
            url: '/signal/pvsEventDictionary/getPreLevelEventParents',
            data: {
                eventIds: eventIds,
                dictionaryLevel: level,
                selectedDatasource: $(".selectedDatasource").val()
            },
        })
            .done(function (data) {
                $.each(data, function (index, element) {
                    if ($("ul.eventDictionaryColWidth[dictionarylevel='" + dicLevelMap[element.level] +
                        "'] li[data-value='" + element.id + "'").length == 0) {
                        if (index == 0 && $("ul[dictionarylevel='" + dicLevelMap[element.level] + "'] .highlighted").length == 0) {
                            $("ul.eventDictionaryColWidth[dictionarylevel='" + dicLevelMap[element.level] + "']").append(
                                "<li tabindex='0' role='button' class='highlighted dicLi' data-value='" + element.id +
                                "' dictionarylevel='" + element.level + "'>" + element.name + "</li>")
                        } else {
                            $("ul.eventDictionaryColWidth[dictionarylevel='" + dicLevelMap[element.level] + "']").append(
                                "<li tabindex='0' role='button' class='dicLi' data-value='" + element.id + "' dictionarylevel='" +
                                element.level + "'>" + element.name + "</li>");
                        }
                    }

                    if (level > 1) {
                        get_pre_level_parents(level - 1);
                    }
                });

                select_item();
        })
    };

    var add_event_selected_items = function () {
        var eventModal = $("#eventModal");
        var id = eventModal.find("ul li.selectedBackground").attr("data-value");
        var name = eventModal.find("ul li.selectedBackground").text();
        var level = eventModal.find("ul li.selectedBackground").attr("dictionarylevel");

        var oldValue = eventModal.find("div.level" + level).text();
        if (oldValue) {
            oldValue = oldValue + ", ";
        }

        var selectedValues = oldValue.split(',');
        selectedValues = _.map(selectedValues, function (v) {
            return v.trim().toLowerCase()
        });

        if (!_.contains(selectedValues, name.trim().toLocaleLowerCase())) {
            $("div.selectedEventDictionaryValue").find("div.level" + level).text(oldValue + " " + name);
            if (level) {
                var selectedObj = {"name": name, "id": id};
                eventValues[level].push(selectedObj)
            }
        }
    };

    var add_selected_smq_items = function () {
        eventValues = {"1": [], "2": [], "3": [], "4": [], "5": [], "6": [], "7": [], "8": []};

        var eventModal = $("#eventModal");

        var valToDisplay = [];

        $.each($('#smqValues').select2('data'), function (id, data) {
            var selectedObj = {"name": data.text, "id": data.id};
            valToDisplay.push(data.text);
            if(data.text.includes('(Broad)')) {
                selectedObj.id = selectedObj.id.replace(/\(B\)/g, '');
                eventValues[7].push(selectedObj);
            } else if(data.text.includes('(Narrow)')) {
                selectedObj.id = selectedObj.id.replace(/\(N\)/g, '');
                eventValues[8].push(selectedObj);
            }
        });

        $(eventModal).find('div.level7').html(valToDisplay.join(', '));
    };

    var close_and_add_selected_items = function () {
        var eventList = [];
        $.each(eventValues, function (dicLevel, element) {
            $.each(element, function (index, elem) {
                if (dicLevel == 7 || dicLevel == 8) {
                    eventList.push(elem.name);
                } else {
                    eventList.push(elem.name + " (" + dicLevelMap[dicLevel] + ")");
                }
            })
        });

        if (!$.isEmptyObject(eventList)) {
            $("#eventSelection").val(JSON.stringify(eventValues));
        } else {
            $("#eventSelection").val("");
        }


        $("#showEventSelection").html("");
        $("#showEventSelection").append("<div style='padding: 5px'>" + eventList + "</div>");
    };

    var load_events = function () {
        var all_studies = $("#eventSelection").val();
        if ($("#eventSelection").val() != "") {
            $("#showEventSelection").html("");
            $.each(JSON.parse(all_studies), function (index, element) {
                var events_to_show = [];
                $.each(element, function (idx, elem) {
                    events_to_show.push(elem.name);
                    eventValues[index].push(elem)
                });

                $("#showEventSelection").append("<div style='padding: 1px'>" + events_to_show + "</div>");
                if (index == 8) {
                    var oldVal = $("div.selectedEventDictionaryValue").find("div.level7").text();
                    if (oldVal) {
                        events_to_show = oldVal + ", " + events_to_show;
                    }
                    $("div.selectedEventDictionaryValue").find("div.level7").text(events_to_show);
                } else {
                    $("div.selectedEventDictionaryValue").find("div.level" + index).text(events_to_show)
                }
            })
        }
        return "TY"
    };

    var deselect_items = function () {
        $("ul.eventDictionaryColWidth").find('li.selectedBackground').removeClass('selectedBackground').addClass('selected');
    };

    var deselect_items_except = function (level) {
        $("ul.eventDictionaryColWidth").find('li.selected').each(function () {
            if ($(this).attr('dictionarylevel') == level) {
                $(this).removeClass("selected");
            }
        })
    };


    var select_item = function () {
        $("ul.eventDictionaryColWidth").find('li').on('click', function (event) {
            var dicLevel = $(this).attr("dictionarylevel");
            $("ul.eventDictionaryColWidth").find('li.selectedBackground').removeClass('selectedBackground').addClass('selected');
            $("ul.eventDictionaryColWidth").find('li.selected').each(function () {
                if ($(this).attr('dictionarylevel') == dicLevel) {
                    $(this).removeClass("selected");
                }
            });
            $(this).addClass('selectedBackground');
        })
    };

    init();

    eventSelectionModal = {
        loadEvents: load_events
    }
});
