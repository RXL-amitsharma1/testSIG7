//= require app/pvs/common/rx_common.js

var signal = signal || {}

signal.businessConfig = (function() {

    var bindScreenOnLoad = function() {

        //Configuring Screens if PRR is enabled.
        var enablePrrValue = $("#enablePrrChk").is(":checked");

        if (enablePrrValue) {

            var prrCustomConfigValue = $("#enablePrrCustomConfigChk").is(":checked");

            if (prrCustomConfigValue) {
                $("#prrCustomConfig").removeClass("hide");
                $("#prrCustomConfigDiv").removeClass("hide");
            } else {
                $('.prrElement').removeClass('hide')
            }
        } else {
            $('.prrElement').addClass('hide')
        }

        //Configuring Screens if EBGM is enabled.
        var enableEbgmValue = $("#enableEbgmChk").is(":checked");

        if (enableEbgmValue) {
            var ebgmCustomSqlValue = $("#enableEbgmCustomConfigChk").is(":checked");
            if (ebgmCustomSqlValue) {
                $(".ebgmCustomConfig").removeClass("hide");
                $("#ebgmCustomConfigDiv").removeClass("hide");
            } else {
                $('.ebgmElement').removeClass('hide');
            }
        } else {
            $('.ebgmElement').addClass('hide');
        }

        //Configuring Screens if ROR is enabled.
        var enableRorValue = $("#enableRorChk").is(":checked");

        if (enableRorValue) {
            var rorCustomSqlValue = $("#enableRorCustomConfigChk").is(":checked");
            if (rorCustomSqlValue) {
                $(".rorCustomConfig").removeClass("hide");
                $("#rorCustomConfigDiv").removeClass("hide");
            } else {
                $('.rorElement').removeClass('hide');
            }
        } else {
            $('.rorElement').addClass('hide');
        }

        $(".ebgmAlgoType").each(function( index ) {

            var ebgmAlgoTypeVal = $(this).val()
            var row = $(this).closest("tr");

            if (ebgmAlgoTypeVal == "EB05 > Prev-EB95") {
                row.find("input[id$=minThreshold]").attr("disabled", "disabled");
                row.find("input[id$=maxThreshold]").attr("disabled", "disabled");
            } else {
                row.find("input[id$=minThreshold]").attr("disabled", false);
                row.find("input[id$=maxThreshold]").attr("disabled", false);
            }
        });



    }

    var bindEnableAlgoEvent = function() {

        $('.enableChk').on('switchChange.bootstrapSwitch', function(event, state) {
            var callingSection = $(this).attr("id")
            switch (callingSection) {
                case "enableEbgmChk":
                    if(state) {
                        var ebgmCustomSqlValue = $("#enableEbgmCustomConfigChk").is(":checked");
                        if (ebgmCustomSqlValue) {
                            $(".ebgmCustomConfig").removeClass("hide");
                            $("#ebgmCustomConfigDiv").removeClass("hide");
                        } else {
                            $('.ebgmElement').removeClass('hide');
                            $(".ebgmCustomConfig").addClass("hide");
                        }
                    } else {
                        $('.ebgmElement').addClass('hide');
                        $(".ebgmCustomConfig").addClass("hide");
                    }
                    break;
                case "enablePrrChk":
                    if(state) {
                        var prrCustomConfigValue = $("#enablePrrCustomConfigChk").is(":checked");
                        if (prrCustomConfigValue) {
                            $("#prrCustomConfig").removeClass("hide");
                            $("#prrCustomConfigDiv").removeClass("hide");
                        } else {
                            $('.prrElement').removeClass('hide');
                            $(".prrCustomConfig").addClass("hide");
                        }
                    } else {
                        $('.prrElement').addClass('hide');
                        $(".prrCustomConfig").addClass("hide");
                    }
                    break;
                case "enableRorChk":
                    if(state) {
                        var rorCustomSqlValue = $("#enableRorCustomConfigChk").is(":checked");
                        if (rorCustomSqlValue) {
                            $(".rorCustomConfig").removeClass("hide");
                            $("#rorCustomConfigDiv").removeClass("hide");
                        } else {
                            $('.rorElement').removeClass('hide');
                            $(".rorCustomConfig").addClass("hide");
                        }
                    } else {
                        $('.rorElement').addClass('hide');
                        $(".rorCustomConfig").addClass("hide");
                    }
                    break;
            }
        });
    }

    var bindCustomConfigEvents = function() {

        $('.enableCustomChk').on('switchChange.bootstrapSwitch', function(event, state) {

            var callingSection = $(this).attr("id");

            switch (callingSection) {
                case "enablePrrCustomConfigChk":
                    if (state) {
                        //Show the table and ci code.
                        $('#prrCustomConfig').removeClass('hide');
                        $("#prrConfigSection").addClass('hide');
                        $(".addPrrDiv").addClass('hide');
                        $("#prrCiDiv").addClass('hide');
                    } else {
                        $('#prrCustomConfig').addClass('hide');
                        $("#prrConfigSection").removeClass('hide');
                        $(".addPrrDiv").removeClass('hide');
                        $("#prrCiDiv").removeClass('hide');
                    }
                    break;
                case "enableEbgmCustomConfigChk":
                    if (state) {
                        //Show the table and ci code.
                        $('#ebgmCustomConfig').removeClass('hide');
                        $("#ebgmConfigSection").addClass('hide');
                        $(".addEbgmDiv").addClass('hide');
                        $("#ebgmCiDiv").addClass('hide');
                    } else {
                        $('#ebgmCustomConfig').addClass('hide');
                        $("#ebgmConfigSection").removeClass('hide');
                        $(".addEbgmDiv").removeClass('hide');
                        $("#ebgmCiDiv").addClass('hide');
                    }
                    break;
                case "enableRorCustomConfigChk":
                    if (state) {
                        //Show the table and ci code.
                        $('#rorCustomConfig').removeClass('hide');
                        $("#rorConfigSection").addClass('hide');
                        $(".addRorDiv").addClass('hide');
                        $("#rorCiDiv").addClass('hide');
                    } else {
                        $('#rorCustomConfig').addClass('hide');
                        $("#rorConfigSection").removeClass('hide');
                        $(".addRorDiv").removeClass('hide');
                        $("#rorCiDiv").addClass('hide');
                    }
                    break;
            }

        });
    }

    var bindHiddenFieldValues = function() {
        $('.bcCheckBox').on('switchChange.bootstrapSwitch', function(event, state) {
            var hiddenElement = $(this).parents('.chkDiv').find(".bcCheckHidden");
            event.preventDefault();
            event.stopPropagation();
            if (state) {
                hiddenElement.val(true);
            } else {
                hiddenElement.val(false);
            }
        });
    }

    var bindEbgmAlgoChangeEvent = function() {
        $(".ebgmAlgoType").on("change", function() {
            var row = $(this).closest("tr");
            if ($(this).val() == "EB05 > Prev-EB95") {
                row.find("input[id$=minThreshold]").attr("disabled", "disabled");
                row.find("input[id$=maxThreshold]").attr("disabled", "disabled");
            } else {
                row.find("input[id$=minThreshold]").attr("disabled", false);
                row.find("input[id$=maxThreshold]").attr("disabled", false);
            }
        });
    }

    var bindTargetStateChangeEvent = function() {

        $(".targetState").on("change", function() {
            var targetStateCombo = $(this)
            var workflowStateId = targetStateCombo.val();
            $.ajax({
                url: "getDispositions",
                data: {id: workflowStateId},
            })
                .done(function (result) {
                    var disposition = signal.utils.render('disposition_dropdown', {"dispositions" : result})
                    targetStateCombo.closest("tr").find("select[id$=targetDisposition]").html(disposition)
                });
        });
    }

    var bindFirstTimeCheckEvent = function() {
        $(".firstTimeCheck").on("click", function() {
            var hiddenElement = $(this).parent().find(".bcCheckHidden");
            if ($(this).is(':checked')) {
                hiddenElement.val(true);
            } else {
                hiddenElement.val(false);
            }
        });
    }

    var bindAddAlgoRowEvent = function() {

        $(".addRowDiv").on("click", function() {

            var addRowElementId = $(this).attr("id");

            var lastRow = $("#"+addRowElementId+"Table").find("tr:last");

            var currentIndex = lastRow.attr("id").split(addRowElementId)[1];
            var newIndex = parseInt(currentIndex) + 1

            //Clone the last row.
            var newRow = lastRow.clone(true);

            //Set the properties to the row elements.
            newRow.attr("id", addRowElementId+newIndex);

            newRow.find("select[id$=algoType]").attr("id", addRowElementId+'['+newIndex+'].algoType').
            attr("name", addRowElementId+'['+newIndex+'].algoType');

            newRow.find("input[id$=minThreshold]").attr("id", addRowElementId+'['+newIndex+'].minThreshold').
            attr("name", addRowElementId+'['+newIndex+'].minThreshold').val('');

            newRow.find("input[id$=maxThreshold]").attr("id", addRowElementId+'['+newIndex+'].maxThreshold').
            attr("name", addRowElementId+'['+newIndex+'].maxThreshold').val('');

            if (newRow.find("select[id$=algoType]").val() == "EB05 > Prev-EB95") {
                newRow.find("input[id$=minThreshold]").attr("disabled", "disabled");
                newRow.find("input[id$=maxThreshold]").attr("disabled", "disabled");
            } else {
                newRow.find("input[id$=minThreshold]").attr("disabled", false);
                newRow.find("input[id$=maxThreshold]").attr("disabled", false);
            }

            newRow.find("select[id$=targetState]").attr("id", addRowElementId+'['+newIndex+'].targetState').
            attr("name", addRowElementId+'['+newIndex+'].targetState').val('');

            newRow.find("select[id$=targetDisposition]").attr("id", addRowElementId+'['+newIndex+'].targetDisposition').
            attr("name", addRowElementId+'['+newIndex+'].targetDisposition').val('');

            newRow.find("input[id$=justification]").attr("id", addRowElementId+'['+newIndex+'].justification').
            attr("name", addRowElementId+'['+newIndex+'].justification').val('');

            //Set the properties to the hidden elements.
            newRow.find("input[id$=id]").attr("id", addRowElementId+'['+newIndex+'].id').
            attr("name", addRowElementId+'['+newIndex+'].id').val('');

            newRow.find("input[id$=new]").attr("id", addRowElementId+'['+newIndex+'].new').
            attr("name", addRowElementId+'['+newIndex+'].new').val(true);

            newRow.find("input[id$=deleted]").attr("id", addRowElementId+'['+newIndex+'].deleted').
            attr("name", addRowElementId+'['+newIndex+'].deleted').val(false);

            newRow.find("span").attr("id", "delete"+newIndex).removeClass("hide");

            //Now to update in the first time rule things.
            newRow.find("input[id$=firstTimeRule]").attr("id", addRowElementId+'['+newIndex+'].firstTimeRule').
            attr("name", addRowElementId+'['+newIndex+'].firstTimeRule').val(false);

            newRow.find("input[id$=firstTimeRuleChk]").attr("id", addRowElementId+'['+newIndex+'].firstTimeRuleChk').
            attr("name", addRowElementId+'['+newIndex+'].firstTimeRuleChk').val(false).prop('checked', false);

            $("#"+addRowElementId+"TableBody").append(newRow);

            newRow.removeClass("hide");
        });
    }

    return {
        bindScreenOnLoad : bindScreenOnLoad,
        bindCustomConfigEvents : bindCustomConfigEvents,
        bindEnableAlgoEvent : bindEnableAlgoEvent,
        bindFirstTimeCheckEvent : bindFirstTimeCheckEvent,
        bindTargetStateChangeEvent : bindTargetStateChangeEvent,
        bindEbgmAlgoChangeEvent : bindEbgmAlgoChangeEvent,
        bindHiddenFieldValues : bindHiddenFieldValues,
        bindAddAlgoRowEvent : bindAddAlgoRowEvent
    }
})()