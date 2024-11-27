$(function () {
    var pageLoc = window.location.pathname.split('/')[2];
    var subPageLoc = window.location.pathname.split('/')[3];
    $('#pvrTabs').find('a').each(function () {
        $('#menuHelp').removeClass();
        $('#menuPreference').removeClass();
        switch (pageLoc) {
            case 'preference':
                $('#menuPreference').attr('class', 'active');
                break;
            case 'report':
                break;
            case 'help':
                $('#menuHelp').attr('class', 'active');
                break;
            default :
                if (subPageLoc == $(this).attr('href').split('/')[3] && pageLoc == $(this).attr('href').split('/')[2]) {
                    $(this.parentNode).attr('class', 'active');
                } else {
                    $(this.parentNode).removeClass();
                }
                break;
        }
    });

    var init = function() {
        $('#whatsNewLink').on('click', function(evt) {
            var whatsNewContainer = $('#whatsNewContainer')
            if (whatsNewContainer.is(":visible")) {
                whatsNewContainer.hide();
            } else {
                whatsNewContainer.show();
            }
        })
    }

    init()
})




