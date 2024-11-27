$(function () {
    if ($('#completedMileStones').val() != undefined) {
        $('#completedMileStones').append(populateMileStoneTable(JSON.parse(milestoneTableMap),JSON.parse(progressTrackerMap),highestExecutionLevel, true));
        $('#completedMileStones').find('table').addClass('table table-bordered')
        $('#completedMileStones').find('td').addClass('text-center')
        $('#completedMileStones').find('th').addClass('text-center')
    }
});

QUALITATIVE_PROGRESS_BAR_STATUS = {
    0: 0,
    1: 10,
    2: 20,
    3: 60,
    4: 100
};

ALERT_PROGRESS_BAR_STATUS = {
    0: 0,
    1: 10,
    2: 60,
    3: 100
};

QUALITATIVE_PROGRESS_BAR_COLOR = {
    0: '',
    1: 'progress-background-25',
    2: 'progress-background-50',
    3: 'progress-background-75',
    4: 'progress-background-75'
};


function populateMileStoneTable(row, progressTrackerMap, highestExecutionLevel, isAlert) {
    var tbl_body = "";
    var execPercentage = "";
    var count = 1;
    var timeTaken;
    var timeStampJSONMap = JSON.parse(row.timeStampJSON);
    if (isAlert) {
        tbl_body = "<table><tr><th>%&nbsp;Completed</th><th>Time Taken</th></tr>"
        if (row.alertType == ALERT_CONFIG_TYPE.EVDAS_CONFIGURATION || row.alertType == ALERT_CONFIG_TYPE.LITERATURE_CONFIGURATION || row.alertType == ALERT_CONFIG_TYPE.QUALTITATIVE_CONFIGURATION) {
            $.each(timeStampJSONMap, function (executionLevel, executionTime) {
                var tbl_row = "";
                if (row.alertType == ALERT_CONFIG_TYPE.EVDAS_CONFIGURATION || row.alertType == ALERT_CONFIG_TYPE.LITERATURE_CONFIGURATION) {
                    execPercentage = ALERT_PROGRESS_BAR_STATUS[executionLevel];
                } else if (row.alertType == ALERT_CONFIG_TYPE.QUALTITATIVE_CONFIGURATION) {
                    execPercentage = progressTrackerMap[executionLevel]
                    if (typeof execPercentage === "undefined") {
                        execPercentage = QUALITATIVE_PROGRESS_BAR_STATUS[executionLevel];
                    }
                }
                tbl_row += "<td>" + execPercentage + "%</td>";
                if (executionLevel == 1 || row.alertType == ALERT_CONFIG_TYPE.LITERATURE_CONFIGURATION) {
                    timeTaken = executionTime - row.startTime;
                } else {
                    timeTaken = executionTime - timeStampJSONMap[executionLevel - 1];
                }
                tbl_row += "<td>" + getTimeInFormat(timeTaken) + "</td>";
                tbl_body += "<tr>" + tbl_row + "</tr>";
                count += 1;
            });
        } else if (row.alertType == ALERT_CONFIG_TYPE.QUANTITATIVE_CONFIGURATION) {
            $.each(timeStampJSONMap, function (executionLevel, executionTime) {
                if (executionLevel == highestExecutionLevel) {
                    var tbl_row = "";
                    execPercentage = progressTrackerMap[executionLevel]
                    if (typeof execPercentage === "undefined") {
                        execPercentage = QUALITATIVE_PROGRESS_BAR_STATUS[executionLevel];
                    }
                    tbl_row += "<td>" + execPercentage + "%</td>";
                    if (executionLevel == 1) {
                        timeTaken = executionTime - row.startTime;
                    } else {
                        timeTaken = executionTime - timeStampJSONMap[executionLevel - 1];
                    }
                    tbl_row += "<td>" + getTimeInFormat(timeTaken) + "</td>";
                    tbl_body += "<tr>" + tbl_row + "</tr>";
                    count += 1;
                }
            });
        }
        if (count == 1)
            tbl_body = "";
        else
            tbl_body = tbl_body + "</table>"
    }
    return tbl_body
}

function getTimeInFormat(data) {
    var time = '';
    if (data !== '-') {
        var duration = moment.duration(parseInt(data, 10));
        var addZero = function (v) {
            return Math.floor(v);
        };
        if (duration.days() == 1) {
            time += addZero(duration.days()) + $.i18n._('day');
        } else if (duration.days() != 0) {
            time += addZero(duration.days()) + $.i18n._('days');
        }
        if (duration.hours() == 1) {
            time += " " + addZero(duration.hours()) + $.i18n._('hour');
        } else if (duration.hours() != 0) {
            time += " " + addZero(duration.hours()) + $.i18n._('hours');
        }
        if (duration.minutes() == 1) {
            time += " " + addZero(duration.minutes()) + $.i18n._('minute');
        } else if (duration.minutes() != 0) {
            time += " " + addZero(duration.minutes()) + $.i18n._('minutes');
        }
        if (duration.seconds() == 1 && duration.milliseconds() == 0) {
            time += " " + addZero(duration.seconds()) + '.' + addZero(duration.milliseconds()) + $.i18n._('second');
        } else if (duration.seconds() != 0 && duration.milliseconds() != 0) {
            time += " " + addZero(duration.seconds()) + '.' + addZero(duration.milliseconds()) + $.i18n._('seconds');
        } else if (duration.seconds() == 0 && duration.milliseconds() != 0) {
            time += '.' + addZero(duration.milliseconds()) + ' seconds ';
        }
        if (time[0] == '.') {
            time = '0' + time;
        }
    } else {
        time = data;
    }
    return time
}
