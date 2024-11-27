//= require js/jquery-ui-1.10.4.custom.min
//= require jquery/jquery-picklist
var defaultDispositionValList;
var defaultDispositionTextList;

$(function() {
    signal.group_utils.init_group_management();
    signal.group_utils.bind_default_disposition_change();
    signal.group_utils.trigger_change_event();
    signal.group_utils.populate_select2_values();
});

var signal = signal || {};

signal.group_utils = (function () {

    var loadAutoRouteValues=function(data){
        var $autoRouteDisposition = $("#autoRouteDisposition");
        if (data) {
            var autoRouteDispValue = $autoRouteDisposition.val();
            $autoRouteDisposition.removeAttr('disabled');
            $autoRouteDisposition.empty();

            for (var i = 0; i < defaultDispositionValList.length; i++) {
                if (defaultDispositionValList[i] !== data) {
                    $autoRouteDisposition.append('<option value="' + defaultDispositionValList[i] + '">'
                        + defaultDispositionTextList[i] + '</option>');
                }
            }
            $autoRouteDisposition.val(autoRouteDispValue);
            $("#justification").removeAttr('disabled');
        } else {
            $autoRouteDisposition.attr('disabled', '');
            $autoRouteDisposition.empty();
            $("#justification").attr('disabled', '');
        }
    }

    var bind_default_disposition_change = function () {
        generateDispositionList();
        $('#defaultQualiDisposition').on('change', function () {
            loadAutoRouteValues($(this).val())
        });
    };


    var init_group_management = function() {
        $('#allowedProductList').pickList();
        handleGroupTypeChange();
        $('#groupType').trigger('change');
    };

    var handleGroupTypeChange = function() {
        $('#groupType').on('change', function () {
            if($(this).val() === 'WORKFLOW_GROUP') {
                $('.pickList_removeAll').trigger('click');
                $('.productSelection').hide();
                $('#defaultQualiDisposition').trigger('change');
                $('.justificationSelection').show();
            } else {
                $('.productSelection').show();
                $('.justificationSelection').hide();
            }
        });
    };

    //This function will populate the disposition list in javascript variable
    var generateDispositionList = function () {
        var $defaultDispositionOption = $('#defaultQualiDisposition > option');
        defaultDispositionValList = $defaultDispositionOption.map(function () {
            return this.value;
        }).get();
        defaultDispositionTextList = $defaultDispositionOption.map(function () {
            return this.text;
        }).get();
    };

    var populate_select2_values = function() {
        var $alertLevelDisposition = $("#alertLevelDisposition");
        var $dispositionValue = $("#dispositionValue");

        if ($dispositionValue.val()) {
            var sourceArray = $dispositionValue.val().toString().replace("[", "").replace("]", "").split(',');
            for (var i = 0; i < sourceArray.length; i++) {
                sourceArray[i] = sourceArray[i].trim()
            }
            $alertLevelDisposition.select2().val(sourceArray).trigger("change");
        } else {
            $alertLevelDisposition.select2();
        }
    };

    var trigger_change_event = function () {
        $('#defaultQualiDisposition').trigger('change');
    };

    return {
        bind_default_disposition_change: bind_default_disposition_change,
        trigger_change_event: trigger_change_event,
        init_group_management :  init_group_management,
        populate_select2_values: populate_select2_values,
        loadAutoRouteValues:loadAutoRouteValues

    }
})();
