$(function () {
    var dicLevelMap = {1: 'Study Number', 2: 'Project Number', 3: 'Center'}
    var studyValues = {"1": [], "2": [], "3": []};
    var init = function () {
        $("#studyModal").find("input.searchStudies").on('keyup', function (e) {
            if (e.keyCode == 13) {
                $("#fetchingStudy").show();
                $.ajax({
                    cache: false,
                    url: '/signal/pvsStudyDictionary/searchStudies',
                    data: {
                        contains: $(this).val(),
                        dictionaryLevel: $(this).attr('level'),
                        selectedDatasource: $(".selectedDatasource").val()
                    },
                })
                    .done(function (data) {
                        clear_all_records()
                        $(data).each(function (index) {
                            clear_all_inputs_except($(this).attr('level'))
                            var containerLevel = dicLevelMap[$(this).attr('level')]
                            $("ul[dictionarylevel='" + containerLevel + "']").append(
                                "<li class='dicLi' data-value='" + data[index].id + "' dictionarylevel='" + data[index].level + "'>" +
                                data[index].name + "</li>")
                        })
                        $("#fetchingStudy").hide();
                        init_get_components()
                })
            }
        })

        $("button.addStudyValues").on('click', function (event) {
            if ($('#studyModal').hasClass('in')) {
                add_study_selected_items()
            }
        })

        $("button.addAllStudies").on("click", function (event) {
            if ($('#studyModal').hasClass('in')) {
                close_and_add_selected_items()
            }
        })

        $("button.clearStudyValues").on("click", function (event) {
            if ($('#studyModal').hasClass('in')) {
                clear_all_records_and_inputs()
            }
        })

        load_products()
    }

    var clear_all_records = function () {
        $("ul.studyDictionaryColWidth").find('li').each(function () {
            $(this).remove()
        })
    }

    var clear_all_records_and_inputs = function () {
        $("ul.studyDictionaryColWidth").find('li').each(function () {
            $(this).remove()
        })

        $("input.searchStudies").each(function () {
            $(this).val("")
        })

        $("div.studyDictionaryValue").text("")

        studyValues = {"1": [], "2": [], "3": []}
    }

    var clear_all_child_records = function (level) {
        var levelVal = parseInt(level)
        while(levelVal < 4) {
            levelVal = levelVal +1
            $("ul.studyDictionaryColWidth").find("li[dictionarylevel="+levelVal+"]").remove()
        }
    }

    var clear_all_inputs_except = function (level) {
        $("input.searchStudies").each(function () {
            if ($(this).attr('level') != level) {
                $(this).val("")
            }
        })
    }

    var init_get_components = function () {
        $("ul.studyDictionaryColWidth").find('li').on('click', function (event) {
            var studyId = $(this).attr("data-value")
            var dicLevel = $(this).attr("dictionarylevel")
            deselect_items()
            deselect_items_except(dicLevel)
            $(this).addClass('selected selectedBackground')
            clear_all_child_records($(this).attr('dictionarylevel'))
            get_selected_study(studyId, dicLevel)
            get_pre_level_parents(dicLevel, studyId)
        })
    }

    var get_new_components = function () {
        $("ul.studyDictionaryColWidth").find('li').on('click', function (event) {
            var studyId = $(this).attr("data-value")
            var dicLevel = $(this).attr("dictionarylevel")
            $(this).addClass('selected selectedBackground')
            clear_all_child_records($(this).attr('dictionarylevel'))
            get_selected_study(studyId, dicLevel)
        })
    }

    var get_selected_study = function (studyId, dicLevel) {
        $.ajax({
            cache: false,
            url: '/signal/pvsStudyDictionary/getSelectedStudy',
            data: {
                dictionaryLevel: dicLevel,
                studyId: studyId,
                selectedDatasource: $(".selectedDatasource").val()
            },
        })
            .done(function (data) {
                var nextLevelItems = data.nextLevelItems
                $.each(nextLevelItems, function (index, element) {
                    if ($("ul[dictionarylevel='" + dicLevelMap[element.level] + "'] li[data-value='" + element.id + "'").length == 0) {
                        if (nextLevelItems.length == 1) {
                            $("ul[dictionarylevel='" + dicLevelMap[element.level] + "']").append("<li class='highlighted selected dicLi' data-value='" + element.id + "' dictionarylevel='" + element.level + "'>" + element.name + "</li>")
                        } else {
                            $("ul[dictionarylevel='" + dicLevelMap[element.level] + "']").append("<li class='dicLi' data-value='" + element.id + "' dictionarylevel='" + element.level + "'>" + element.name + "</li>")
                        }
                    }
                });
                select_item()
                get_new_components()
        })
    }

    var get_pre_level_parents = function (level, studyId) {

        var studyIds = ""

        if (studyId == undefined) {
            $.each($("ul li[dictionarylevel='" + level + "']"), function () {
                studyIds += $(this).attr("data-value") + ","
            })
        } else {
            studyIds = studyId
        }
        $.ajax({
            cache: false,
            url: '/signal/pvsStudyDictionary/getPreLevelStudyParents',
            data: {
                studyIds: studyIds,
                dictionaryLevel: level,
                selectedDatasource: $(".selectedDatasource").val()
            },
        })
            .done(function (data) {
                $.each(data, function (index, element) {
                    if ($("ul[dictionarylevel='" + dicLevelMap[element.level] + "'] li[data-value='" + element.id + "'").length == 0) {
                        if (data.length == 1) {
                            $("ul[dictionarylevel='" + dicLevelMap[element.level] + "']").append("<li class='highlighted selected dicLi' data-value='" + element.id + "' dictionarylevel='" + element.level + "'>" + element.name + "</li>")
                        } else {
                            $("ul[dictionarylevel='" + dicLevelMap[element.level] + "']").append("<li class='dicLi' data-value='" + element.id + "' dictionarylevel='" + element.level + "'>" + element.name + "</li>")
                        }
                    }

                    if (level > 1) {
                        get_pre_level_parents(level - 1)
                    }
                });
                select_item()
        })
    }

    var add_study_selected_items = function () {
        var studyModal = $("#studyModal")
        var id = studyModal.find("ul li.selectedBackground").attr("data-value")
        var name = studyModal.find("ul li.selectedBackground").text()
        var level = studyModal.find("ul li.selectedBackground").attr("dictionarylevel")

        var oldValue = studyModal.find("div.level" + level).text();
        if (oldValue) {
            oldValue = oldValue + ", ";
        }
        $("div.selectedStudyDictionaryValue").find("div.level" + level).text(oldValue + " " + name + " (" + id + ")")

        if (level) {
            var selectedObj = {"name": name, "id": id};
            studyValues[level].push(selectedObj)
        }
    }

    var close_and_add_selected_items = function () {
        var studyList = []
        $.each(studyValues, function (index, element) {
            $.each(element, function (indx, elem) {
                studyList.push(elem.name + " (" + dicLevelMap[index] + ")")
            })
        })

        if (!$.isEmptyObject(studyList)) {
            $("#studySelection").val(JSON.stringify(studyValues))
        } else {
            $("#studySelection").val("")
        }

        $("#showStudySelection").html("")
        $("#showStudySelection").append("<div style='padding: 8px'>" + studyList + "</div>")
    }

    var load_products = function () {
        var all_studies = $("#studySelection").val()

        if ((all_studies!=null) && $("#studySelection").val() != "") {
            $("#showStudySelection").html("")
            $.each(JSON.parse(all_studies), function (index, element) {
                var study_to_show = []
                $.each(element, function (idx, elem) {
                    study_to_show.push(elem.name)
                    studyValues[index].push(elem)
                })
                $("#showStudySelection").append("<div style='padding: 5px'>" + study_to_show + "</div>")
                $("div.selectedStudyDictionaryValue").find("div.level" + index).text(study_to_show)
            })
        }

    }

    var deselect_items = function () {
        $("ul.studyDictionaryColWidth").find('li.selectedBackground').removeClass('selectedBackground').addClass('highlighted')
    }

    var deselect_items_except = function (level) {
        $("ul.studyDictionaryColWidth").find('li.selected').each(function () {
            if ($(this).attr('dictionarylevel') == level) {
                $(this).removeClass("highlighted selected dicLi")
            }
        })
    }

    var select_item = function () {
        $("ul.studyDictionaryColWidth").find('li').on('click', function (event) {
            $("ul.studyDictionaryColWidth").find('li.selectedBackground').removeClass('selectedBackground').addClass('highlighted')
            $(this).addClass('selected selectedBackground dicLi')
        })
    }

    init();
})