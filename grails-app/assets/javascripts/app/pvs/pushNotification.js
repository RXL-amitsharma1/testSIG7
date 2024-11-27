var $notificationContainer;
var notificationCount = 0;
var $notificationRows;
var $notificationHeader;
var $inboxMsgMaxCount = 100;

// NotificationLevel values
var N_LEVEL_INFO_NAME = 'Information';
var N_LEVEL_WARN_NAME = 'Warning';
var N_LEVEL_ERROR_NAME = 'Error';

var SIGNAL_CREATION= 'Signal Creation';

$(function () {
    $.fn.dataTable.ext.errMode = 'throw';
    //fetching the notifications whenever the page loads
    startPoller();
    //establish websocket connection
    try{
        connect();
    }catch (e) {
        window.location.href=window.location.origin+"/signal/logout"
    }
    var notificationRow;
    var notificationId;
    $notificationContainer = $('#notificationContainer');
    $notificationRows = $('#notificationRows');
    $notificationHeader = $('#notificationHeader');
    $(document).on('click', '#menuNotification', function () {
        if ($notificationContainer.is(":visible")) {
            $notificationContainer.hide();
        } else {
            $notificationContainer.show();
        }
    });

    $(document).on('click', '#notificationContainer', function () {
        event.stopPropagation();
        event.preventDefault();
        return false;
    });

    $(document).on('click', '#clearNotifications', function () {
        // Call server method to delete all notifications for this user.
        if (notificationCount > 0) {
            $.post(notificationMarkAsReadByUserURL, {id: this.getAttribute('userId')},
                function (result) {
                    // result is true/false if deletion was successful.
                }, 'text');

            $notificationRows.empty();

            setNotificationCount(0);
        }

        event.stopPropagation();
        return false;
    });

    //load the notifications and establish connection on tab change
    $(window).on('blur', function(e) {
        startPoller();
    });


    $(document).on('click', '#notificationRows span a', function () {
        notificationRow = $(this).closest('span');
        notificationId = notificationRow.attr('notificationId');
        var link = notificationRow.attr('executedConfigId');
        var detailUrl = notificationRow.attr('detailUrl');
        var type = notificationRow.attr('type');
        var content = notificationRow.attr('content');
        var subject = notificationRow.attr('subject');
        if (typeof subject === "undefined" || subject == null || subject == "") {
            subject = content;
        }
        const paramsForCaseSeriesUrl = new Map();
        if (type === CASE_SERIES_DRILLDOWN){
            let params = content.replaceAll('[','').replaceAll(']','');
            let paramsList = params.split(',');
            $.each(paramsList, function (idx, val) {
                paramsForCaseSeriesUrl.set(val.split(':')[0].trim(),val.split(':')[1].trim())
            });
        }
        // Redirect to the clicked report's criteria page if we can
        if (link != 0 && link != "null") {
            if (detailUrl in detailUrls) {
                if (type === CASE_SERIES_DRILLDOWN) {
                    window.open(caseSeriesURL + "?" +
                        signal.utils.composeParams({
                            aggExecutionId: paramsForCaseSeriesUrl.get('aggExecutionId'),
                            aggAlertId: paramsForCaseSeriesUrl.get('aggAlertId'),
                            aggCountType: paramsForCaseSeriesUrl.get('aggCountType'),
                            productId: paramsForCaseSeriesUrl.get('productId'),
                            ptCode: paramsForCaseSeriesUrl.get('ptCode'),
                            type: paramsForCaseSeriesUrl.get('type'),
                            typeFlag: paramsForCaseSeriesUrl.get('typeFlag'),
                            isArchived: paramsForCaseSeriesUrl.get('isArchived')
                        }));
                }   else if (type === SIGNAL_CREATION) {
                    window.open(detailUrls[detailUrl] + "?id=" + link);
                }   else {
                    window.open(detailUrls[detailUrl] + "?callingScreen=review&configId=" + link, '_self');
                }
            } else {
                window.location.href = '#'
            }
        } else if (type === REPORT_GENERATED && detailUrl) {
            window.open(detailUrl,'_blank')
        } else if (type === ANALYSIS_REPORT_GENERATED && detailUrl) {
            window.location.href = detailUrl;
        } else {
            $(this).attr("data-toggle","modal");
            $(this).attr("href","#contentTagModal");
            var contentTagModalObj = $('#contentTagModal');
            contentTagModalObj.find("#content").html(subject);
            contentTagModalObj.modal("show");
        }
        decreaseNotification();
    });

    $(document).on('click', '.removeNotification', function () {
        notificationRow = $(this).closest('span');
        notificationId = notificationRow.attr('notificationId');
        decreaseNotification();
    });


    var decreaseNotification = function () {
        $.post(notificationMarkAsReadURL, {id: notificationId},
            function (result) {
                if (result) {
                    notificationRow.hide();
                    notificationCount = notificationCount - 1;
                    setNotificationCount(notificationCount);
                    event.stopPropagation();
                }
            });
    };
});

function startPoller() {
    pollAJAX();
}

//This function connects to the server using Websocket-Connection and Subscribes to it.
function connect() {
    //Create a new SockJS socket - this is what connects to the server
    var socket = new SockJS(socketURL);

    //Build a Stomp client to receive/send messages over the socket we built.
    var client = Stomp.over(socket);

    var queueToSubscribe = NOTIFICATION_QUEUE + userId;

    //Have SockJS connect to the server.
    client.connect("", "", function () {
        //Listening to the connection we established
        client.subscribe(queueToSubscribe, function (message) {
            var msg = JSON.parse(message.body);
            addNotification($notificationRows, msg);
            notificationCount = notificationCount + 1;
            setNotificationCount(notificationCount);
            var messageMap=JSON.parse(message.body)
            if (message.body.includes(ALERT_CONFIG_TYPE.CASE_DRILLDOWN) && (typeof executedConfigId!=="undefined" && messageMap?.executedConfigId.toString()===executedConfigId)) {
                $('#alertsDetailsTable').DataTable().ajax.reload()
            }
        });
    });
}

function pollAJAX() {
    $.ajax({
        cache: false,
        url: notificationURL,
        dataType: 'json'
    })
        .done(function (data) {
            $notificationRows.empty();

            // Sorts notifications by date, newest to oldest
            data.sort(function (a, b) {
                return new Date(b.createdOn).getTime() - new Date(a.createdOn).getTime();
            });

            if (data.length > 0) {
                $.each(data, function () {
                    addNotification($notificationRows, this);
                });
            }
            setNotificationCount(data.length);
    });
}

function addNotification($container, JSONObject) {
    var toAdd = document.createElement('span');
    toAdd.setAttribute("id", "notif");
    toAdd.setAttribute("tabindex", "0");
    toAdd.classList.add('list-group-item');
    toAdd.style.padding = "12px 20px 1px 20px";
    toAdd.setAttribute('notificationId', JSONObject.id);
    toAdd.setAttribute('executedConfigId', JSONObject.executedConfigId);
    toAdd.setAttribute('detailUrl', JSONObject.detailUrl);
    toAdd.setAttribute('content', JSONObject.content);
    toAdd.setAttribute('subject', JSONObject.subject);
    toAdd.setAttribute('type', JSONObject.type);

    if (JSONObject.type === REPORT_GENERATED && JSONObject.level === N_LEVEL_INFO_NAME) {
        $('#signalReportTable').DataTable().ajax.reload();
    }
    var divLeft = document.createElement('div');
    divLeft.classList.add('pull-left', 'p-r-10');

    var iconLeft = document.createElement('em');
    iconLeft.classList.add('fa');

    var divMedia = document.createElement('a');
    divMedia.classList.add('media');
    divMedia.setAttribute('tabindex', '-1');
    divMedia.style.borderBottom = "1px dotted #ccc";
    divMedia.style.paddingBottom = "15px";
    divMedia.setAttribute('href', '#');

    // var labelLeft = document.createElement('span');
    // labelLeft.classList.add('label');

    if (JSONObject.level === N_LEVEL_INFO_NAME) {
        iconLeft.classList.add('noti-success');
        iconLeft.classList.add('fa-check');
    } else if (JSONObject.level === N_LEVEL_WARN_NAME) {
        iconLeft.classList.add('noti-warning');
        iconLeft.classList.add('fa-envelope');
    } else if (JSONObject.level === N_LEVEL_ERROR_NAME) {
        iconLeft.classList.add('noti-danger');
        iconLeft.classList.add('fa-warning');
    }

    divLeft.appendChild(iconLeft);
    // divLeft.appendChild(labelLeft);

    var divMiddle = document.createElement('div');
    divMiddle.classList.add('middle');

    var headerMiddle = document.createElement('h5');
    headerMiddle.classList.add('media-heading');
    headerMiddle.style.fontWeight = "bold";
    headerMiddle.style.fontSize = "13px";
    headerMiddle.appendChild(document.createTextNode(JSONObject.message));

    var parMiddle = document.createElement('p');
    parMiddle.classList.add('m-0');

    var timeSpan = document.createElement('span');
    timeSpan.classList.add('small');
    timeSpan.style.color = "#f1ab28";
    timeSpan.style.fontStyle = "italic";

    // 2015-09-03T22:42:55Z
    var relativeDate = moment(JSONObject.createdOn, "YYYY-MM-DDTHH:mm:ssZ").fromNow();
    var textTime = document.createTextNode(relativeDate);

    timeSpan.appendChild(textTime);
    parMiddle.appendChild(timeSpan);

    divMiddle.appendChild(headerMiddle);
    divMiddle.appendChild(parMiddle);

    var divRight = document.createElement('button');
    divRight.classList.add('close');
    divRight.classList.add('removeNotification');
    divRight.style.marginTop = "-29px";
    var iconRight = document.createElement('span');
    divRight.appendChild(iconRight);
    divMedia.appendChild(divLeft);
    divMedia.appendChild(divMiddle);

    toAdd.appendChild(divMedia);
    $("#notif").append(divRight);
    $container.append(toAdd);
}

function setNotificationCount(count) {
    notificationCount = count;
    showNotiCount(count);
    var countText = count>=$inboxMsgMaxCount ?  $inboxMsgMaxCount + "+" : count;
    $('#notificationBadge').text(countText);
    if (count == 1) {
        $notificationHeader.text($.i18n._('notificationEndOne',count));
    } else {
        $notificationHeader.text($.i18n._('notificationEndMultiple',countText));
    }
}

function showNotiCount(count){
    if(count > 0){
        $(".pv-head-noti-icon").append('<span id="notificationBadge"  class="badge badge-xs badge-pink noSelect"></span>')
    } else{
        $(".pv-head-noti-icon #notificationBadge").remove();
    }
}