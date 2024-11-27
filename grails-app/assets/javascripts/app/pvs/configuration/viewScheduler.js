$(function () {

    $('#relativeDateRangeValueX').hide()
    // Disable the scheduler.
    var inputs = document.getElementsByTagName("input");
    for (var i = 0; i < inputs.length; i++) {
        inputs[i].disabled = true;
    }
    var buttons = document.getElementsByTagName("button");
    for (var i = 0; i < buttons.length; i++) {
        buttons[i].disabled = true;
    }
    var fieldset = document.getElementsByTagName("fieldset");
    for (var i = 0; i < fieldset.length; i++) {
        fieldset[i].disabled = true;
    }

    var dateRangeValueRelElem = document.getElementById('dateRangeValueRelative')

    var value = null
    if (typeof dateRangeValueRelElem != "undefined" && dateRangeValueRelElem != null) {
        value = dateRangeValueRelElem.value;
    }

    if (value) {
        if (value.toLowerCase().indexOf('x') != -1) {
            $('#relativeDateRangeValueX').show()
        }else{
            $('#relativeDateRangeValueX').hide()
        }
    }

    // Keep these code for future discussion if we need to change the layout.
//    var StringScheduler = $('#viewScheduler').val();
//    var JSONScheduler = $.parseJSON(StringScheduler);
//
//    var startDate = JSONScheduler.startDateTime;
//    $('#viewSchedulerDateTime').html(startDate);
//
//    var timezone = JSONScheduler.timeZone.name + " (" + JSONScheduler.timeZone.offset + ")";
//    $('#viewSchedulerTimezone').html(timezone);
//
//    var recurrencePattern = JSONScheduler.recurrencePattern;
//    var recurrenceString = "";
//    var endString = "Never";
//    var freq;
//    var interval;
//    var byDay;
//
//    var repeatArray = recurrencePattern.split(";");
//    console.log(repeatArray);
//    repeatArray.forEach(function (repeat) {
//        var length = repeat.length;
//        var equalIndex = repeat.indexOf("=");
//        if (equalIndex > 0) {
//            var field = repeat.substring(0, equalIndex);
//            console.log(field);
//            var value = repeat.substring(equalIndex + 1, length);
//            console.log(value);
//
//            switch (field) {
//                case "FREQ":
//                    freq = value;
//                    break;
//                case "INTERVAL":
//                    interval = value;
//                    break;
//
//                case "BYDAY":
//                    byDay = value;
//                    break;
//
//                case "UNTIL":
//                    endString = "On date " + value;
//                    break;
//                case "COUNT":
//                    endString = "After " + value + " occurance(s)";
//                    break;
//            }
//            recurrenceString = freq + ", interval is " + interval;
//        }
//    });
//    $('#viewSchedulerRecurrencePattern').html(recurrenceString);
//    $('#viewSchedulerEnd').html(endString);
});