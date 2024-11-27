var scheduleDateJSONDefault;

$(function () {
    if(( typeof editAlert != "undefined" && editAlert === "copy")){
        $('.nextScheulerInfo').addClass('hidden');
    }

    $('.spinbox').spinbox({
        min: 1,
        max: 1000
    });

    $('#myDatePicker').datepicker({
        date: $("#myDatePicker").val() ? new Date($("#myDatePicker").val()) : null,
        momentConfig: {
            culture: userLocale,
            tz: userTimeZone,
            format: DEFAULT_DATE_DISPLAY_FORMAT
        }
    });
    //Added for PVS-56007
    if ($('#editable').val() === 'false'){
        $('#myScheduler').find('.required-indicator').addClass('hide');
    }

    //isValidationError for scheduler population on alert config page
    if (($('#scheduleDateJSON').val() && $('#scheduleDateJSON').val()!='null') && (($('#isEnabled').val() === 'true')|| (isValidationError))) {
        var scheduleInfo = ($("#scheduleDateJSON").val());
        var schedulInfoMap = JSON.parse(scheduleInfo);
        if(schedulInfoMap) {
            delete schedulInfoMap["timeZone"]["text"];
            delete schedulInfoMap["timeZone"]["selected"];
            if ($('#editable').val() === 'false' || (typeof isAlertScheduled != 'undefined' && isAlertScheduled)) {
                $('#myScheduler').scheduler('value', schedulInfoMap);
                var newInfo = schedulInfoMap;
                $('#configSelectedTimeZone').val(newInfo.timeZone.name);
            } else {
                setToday();
                highlightCurrentDayForWeeklyFrequency(userTimeZone);
            }
        }
        for (var i = 0; i < 24; i++) {
            var min = 0;
            for (var j = 0; j < 2; j++) {
                $("#myScheduler #timeSelect ul").append('<li><a href="#">' + calculateAllIntervals(i, min) + '</a></li>');
                min = 30
            }
        }
    } else {
        setToday();
        highlightCurrentDayForWeeklyFrequency(userTimeZone);
    }

    $('#myScheduler').on('changed.fu.scheduler', function (e, data) {
        var newInfo = $('#myScheduler').scheduler('value');
        $('#scheduleDateJSON').val(JSON.stringify(newInfo));
        $('#configSelectedTimeZone').val(newInfo.timeZone.name);
    }).trigger('changed.fu.scheduler');

    $('#myDatePicker').on('changed.fu.datepicker dateClicked.fu.datepicker', function () {
        setDatePickerTime()
    });
    var oldDatePickerValue = $("#myDatePicker").datepicker('getDate');
    function setDatePickerTime() {
        var selectedDate = $("#myDatePicker").datepicker('getDate')
        if (moment(oldDatePickerValue).tz(userTimeZone) < (moment(selectedDate).tz(userTimeZone))) {
            $("#myScheduler #timeSelect ul").empty();
            for (var i = 0; i < 24; i++) {
                var min = 0;
                for (var j = 0; j < 2; j++) {
                    $("#myScheduler #timeSelect ul").append('<li><a href="#">' + calculateAllIntervals(i, min) + '</a></li>');
                    min = 30
                }
                $("#myStartTime").val(12 + ':' + 0 + +0 + ' ' + 'AM');
            }
        }
        var newInfo = $('#myScheduler').scheduler('value');
        $('#scheduleDateJSON').val(JSON.stringify(newInfo));
        $('#configSelectedTimeZone').val(newInfo.timeZone.name);
        oldDatePickerValue = selectedDate
    }

    if (document.getElementById("enable")) {
        var enable = $("#enable").val();
        if (enable) {
            $('#schedule *').prop('disabled', true);
        }
    }

    scheduleDateJSONDefault = JSON.stringify($('#myScheduler').scheduler('value'));

    function calculateNxtInterval(date, interval) {
        var hour = date.hour() + 0;
        var minute = date.minute() + interval;
        if (minute >= 60) {
            hour = hour + Math.floor(minute / 60);
            minute = minute % 60
        }
        var amPm = hour >= 12 ? "PM" : "AM";
        hour = hour % 12;
        hour = hour ? hour : 12;
        minute = (minute > 9 ? minute : "0" + minute);
        hour = hour > 9 ? hour : "0" + hour;
        var now = hour + ':' + minute + ' ' + amPm;
        return now;
    }

    function calculateAllIntervals(hour, minute) {
        var amPm = hour >= 12 ? "PM" : "AM";
        hour = hour % 12;
        hour = hour ? hour : 12;
        minute = (minute > 9 ? minute : "0" + minute);
        hour = hour > 9 ? hour : "0" + hour;
        var now = hour + ':' + minute + ' ' + amPm;
        return now;
    }

    function calculateNumberOfIntervals(hour, min, ampm) {
        hour = hour%12;
        var count = (12 - hour) * 2;
        if (ampm == "AM") {
            count = count + 48
        }
        var listCount = min < 30 ? count : count - 1;
        return listCount
    }

    function setToday() {
        var scheduledDate = moment($("#schedulerTime").val()) // get the date time from server
        var now = calculateNxtInterval(scheduledDate, 0);
        var hour = now.split(":")[0];
        var nowMin = now.split(":")[1].split(" ")[0];
        var nxtInterval = nowMin < 30 ? 30 - nowMin : 60 - nowMin;
        $("#time").text(now);
        var count = calculateNumberOfIntervals(hour, nowMin, now.split(":")[1].split(" ")[1]);
        for (var i = 0; i < count - 1; i++) {
            $("#myScheduler #timeSelect ul").append('<li><a href="#">' +
                calculateNxtInterval(scheduledDate, nxtInterval) + '</a></li>');
            nxtInterval += 30
        }
        // Its going to be always UTC based as server would run in UTC only and now we have custom Timezone support list only so could break if any other timezone server. TODO need to make full proof
        var prefTimezone = $("#timezoneFromServer").val();
        if (prefTimezone) {
            var timeZoneData = prefTimezone.split(",");
            if(timeZoneData){
                var name = timeZoneData[0].split(":")[1].trim();
                var offset = timeZoneData[1].substring(8).trim();
                $('#myScheduler').scheduler('value', {
                    startDateTime: scheduledDate.format("YYYY-MM-DDTHH:mm:ss")+offset,
                    timeZone: {
                        name : name
                    }
                });
            }
        }


    }

    function highlightCurrentDayForWeeklyFrequency(timeZone) {

        var currentDayOfWeek = moment.tz(timeZone).day();
        switch (currentDayOfWeek) {
            case 0:
                $('#repeat-weekly-sun').checkbox('check').addClass("active");
                break;
            case 1:
                $('#repeat-weekly-mon').checkbox('check').addClass("active");
                break;
            case 2:
                $('#repeat-weekly-tue').checkbox('check').addClass("active");
                break;
            case 3:
                $('#repeat-weekly-wed').checkbox('check').addClass("active");
                break;
            case 4:
                $('#repeat-weekly-thu').checkbox('check').addClass("active");
                break;
            case 5:
                $('#repeat-weekly-fri').checkbox('check').addClass("active");
                break;
            case 6:
                $('#repeat-weekly-sat').checkbox('check').addClass("active");
        }
    }

});


