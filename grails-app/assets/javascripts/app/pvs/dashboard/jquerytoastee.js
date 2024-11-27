(function($){

    $.fn.toastee = function(options) {

        var settings = $.extend ({
            type: 'info',
            header: '',
            message: '',
            color: '#fff',
            background: '#3498db',
            width: 150,
            height: 95,
            message1: 'You Have',
            message2:'alerts'
        }, options);

        var self = this;
        var dataId = Math.floor(Math.random() * 100000);
        var backgrounds = {'info': '#058DC7', 'error': '#ED561B', 'success': '#499639','actionItems':'#767676'};

        if (options == undefined) {
            options = {'empty': 'empty'};
        }

        switch (settings.type) {
            case 'info':
                settings.background = options.background || backgrounds.info;
                settings.header = options.header || headers.info;
                var toast1 = '<a href = "/signal/singleCaseAlert/details?callingScreen=dashboard">'
                toast1 += '<div data-id="' + dataId +'" style="display: block; position: relative; border-radius: 10px; min-width: 80%; max-width: 80%'+ settings.width +'px; height: '+ settings.height +'px; background: '+ settings.background +'; box-shadow: 0 5px 5px 2px #ccc">';
                toast1 += '<h5 style="text-align: center; padding: 10px 10px 0; color: '+ settings.color +'">' + settings.header + '</h5>';
                toast1 += '<hr style="color: transparent; width: 80%; margin: 0 auto; border-bottom: 1px solid ' + settings.color +'; opacity: 0.3" />'
                toast1 += '<h1 style="margin-top:2%; text-align: center;  color: '+ settings.color +'">' + settings.message + '</h1>';
                toast1 += '</div>';
                toast1 += '</a>';
                break;
            case 'error':
                settings.background = options.background || backgrounds.error;
                settings.header = options.header || headers.error;
                var toast1 ='<a href = "/signal/aggregateCaseAlert/details?callingScreen=dashboard">'
                toast1 += '<div data-id="' + dataId +'" style="display: block; position: relative; border-radius: 10px; min-width: 80%; max-width: 80%'+ settings.width +'px; height: '+ settings.height +'px; background: '+ settings.background +'; box-shadow: 0 5px 5px 2px #ccc">';
                toast1 += '<h5 style="text-align: center; padding: 10px 10px 0; color: '+ settings.color +'">' + settings.header + '</h5>';
                toast1 += '<hr style="color: transparent; width: 80%; margin: 0 auto; border-bottom: 1px solid ' + settings.color +'; opacity: 0.3" />'
                toast1 += '<h1 style="margin-top:2%; text-align: center;  color: '+ settings.color +'">' + settings.message + '</h1>';
                toast1 += '</div>';
                toast1 += '</a>';
                break;
            case 'actionItems':
                settings.background = options.background || backgrounds.actionItems;
                settings.header = options.header || headers.error;
                var toast1 ='<a href="/signal/action/list">'
                toast1 +=  '<div data-id="' + dataId +'" style="display: block; position: relative; border-radius: 10px; min-width: 80%; max-width: 80%'+ settings.width +'px; height: '+ settings.height +'px; background: '+ settings.background +'; box-shadow: 0 5px 5px 2px #ccc">';
                toast1 += '<h5 style="text-align: center; padding: 10px 10px 0; color: '+ settings.color +'">' + settings.header + '</h5>';
                toast1 += '<hr style="color: transparent; width: 80%; margin: 0 auto; border-bottom: 1px solid ' + settings.color +'; opacity: 0.3" />'
                toast1 += '<h1 style="margin-top:2%; text-align: center;  color: '+ settings.color +'">' + settings.message + '</h1>';
                toast1 += '</div>';
                toast1 += '</a>';
                break;
            case 'success':
                settings.background = options.background || backgrounds.success;
                settings.header = options.header || headers.success;
                var toast1 = '<a href = "/signal/adHocAlert/index?callingScreen=dashboard"'
                toast1 += '<div data-id="' + dataId +'" style="display: block;padding 10px; position: relative; border-radius: 10px; min-width: 80%; max-width: 80%'+ settings.width +'px; height: '+ settings.height +'px; background: '+ settings.background +'; box-shadow: 0 5px 5px 2px #ccc">';
                toast1 += '<h5 style="text-align: center; padding: 10px 10px 0; color: '+ settings.color +'">' + settings.header + '</h5>';
                toast1 += '<hr style="color: transparent; width: 80%; margin: 0 auto; border-bottom: 1px solid ' + settings.color +'; opacity: 0.3" />'
                toast1 += '<h1 style="margin-top:2%; text-align: center;  color: '+ settings.color +'">' + settings.message + '</h1>';
                toast1 += '</div>';
                toast1 += '</a>';
        };

        var toast = toast1

        var stopTimer = function () {
            clearTimeout(timer.dataId);
        };

        $(this).append(toast);

        $('.closeToastee').on('click', function () {
            $(this).parent().hide().remove();
        });

        $('div[data-id="' + dataId + '"]').mouseover(function(){
            $(this).stop().fadeIn(0);
        }).mouseout(function(){

        });
        return this;
    };
    
})(jQuery);
