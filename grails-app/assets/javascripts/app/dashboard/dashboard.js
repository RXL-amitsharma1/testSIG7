
$(function () {
    function saveWidgets() {
        var serializedData = _.map($('.grid-stack > .grid-stack-item:visible'), function (el) {
            return {
                id: parseInt($(el).attr("gs-id")),
                x: parseInt($(el).attr("gs-x")),
                y: parseInt($(el).attr("gs-y")),
                width: parseInt($(el).attr("gs-w")),
                height: parseInt($(el).attr("gs-h")),
            };
        }, this);
        $.ajax({
            type: "POST",
            url: CONFIGURATION.updateWidgetsUrl,
            dataType: "json",
            data: {items: JSON.stringify(serializedData, null, '    ')},
        })
            .fail(function (err) {
                console.log(err);
            });
    }

    $(".remove-widget").on("click", function() {
        var widget = $(this).closest(".grid-stack-item");
        var url = $(this).data("url");
        var params = $(this).data("params");
        if (CONFIGURATION[url]) {
            $.ajax({
                type: "POST",
                url: CONFIGURATION[url],
                dataType: "json",
                data: params,
            }).done(function (data) {
                const id = $(widget).attr("id");
                const widgetToRemove = gridstack
                    .getGridItems()
                    .find((widgetEl) => $(widgetEl).attr("id") === id);
                gridstack.removeWidget(widgetToRemove);
            }).fail(function (err) {
                console.log(err);
            })
        }
    });
    $( ".rx-widget-menu-content").menu({
        select: function( event, ui ) {
            var url = ui.item.data("url");
            var params = ui.item.data("params");
            if (CONFIGURATION[url]) {
                window.location.href = CONFIGURATION[url] + "?" + $.param(params);
            }
        }
    });

    /*var gridstackOptions = {
        column: 12,
        resizable: { autoHide: true, handles: "all" },
        margin: "10px 5px",
        draggable: {
            handle: ".rx-widget-header",
        },
        cellHeight: 60,
    };*/

    var gridstackOptions = {
        column: 12,
        draggable: {
            handle: ".pv-sec-heading",
        }
    };
    var gridstack = GridStack.init(gridstackOptions, '.grid-stack')
    $(".grid-stack").on('change', function(event, items) {
        saveWidgets();
        for (var i = 0; i < items.length; i++) {
            var item = items[i];
            var chartContainer = $(item.el).find('.chart-container').first();
            if (chartContainer.length) {
                if (typeof chartContainer.highcharts() != "undefined" && chartContainer.highcharts() != null) {
                    chartContainer.highcharts().setSize(chartContainer.innerWidth()-25,chartContainer.innerHeight()-20);
                }
            }
        }
    });
    $(window).on("resize", function() {
        $('.chart-container').each(function(){
            var chart=$(this);
            chart.highcharts().setSize(chart.innerWidth()-25,chart.innerHeight()-20);
        });
        $('.widget-calendar').each(function(){
            var calendar=$(this);
            calendar.fullCalendar('option', 'contentHeight', calendar.parent().height()-150);
            //calendar.fullCalendar('render');
        });
    });
    $('.chart-container').each(function(index, element) {
        var container = $(this);
        var options = JSON.parse(container.attr("data"),function(key, value) {
            if (key === "formatter") {
                value = eval("(" + value + ")")
            }
            return value;
        });
        container.highcharts(options);
    });

    function checkIfSessionTimeOutThenReload(event, json) {
        if (json && json[SESSION_TIME_OUT]) {
            event ? event.stopPropagation() : "";
            alert($.i18n._('sessionTimeOut'));
            window.location.reload();
            return false
        }
    }
});