//= require app/pvs/common/rx_common.js
//= require app/pvs/common/rx_alert_utils.js

var applicationName = "Single Case Alert";
var applicationLabel = "Case Detail";
var appType = "Single Case Alert";

$(function () {
    $(function () {
        $('#caseDetailTree').on('click', '.jstree-anchor', function (e) {
            $('#caseDetailTree').jstree(true).toggle_node(e.target);
        }).jstree({
            'core': {
                'data': {
                    'url': treeNodesUrl
                },
                'dblclick_toggle': false
            }
        });

        $('#caseDetailTree').on("dblclick.jstree", function (event) {
            var node = $(event.target).closest("li");
            var $targetid = node[0].id;
            $targetid = $targetid.length && $targetid || $('[id=' + this.hash.slice(1) + ']');
            $targetid = "#" + $targetid + "_container";
            $($targetid).collapse('hide');
        });

        $('#caseDetailTree').on('loaded.jstree', function () {
            $("#caseDetailTree .jstree-anchor").each(function () {
                var val = $(this).attr('id').replace("anchor", "container");
                $(this).attr('href', '#' + val)
            });
            var scrollDuring = 1000;
            var scrollBegin = 163;
            $('a.jstree-anchor').on('click', function () {
                $('#case_detail_container').collapse('show');
                if (location.pathname.replace(/^\//, '') === this.pathname.replace(/^\//, '') && location.hostname === this.hostname) {
                    var $targetid = $(this.hash);
                    $targetid = $targetid.length && $targetid || $('[id=' + this.hash.slice(1) + ']');
                    $($targetid.selector).collapse('show');
                    if ($targetid.length) {
                        var targetOffset = $targetid.offset().top - scrollBegin;
                        $('html,body').animate({scrollTop: targetOffset}, scrollDuring);
                        $("a").removeClass("active");
                        $(this).addClass("active");
                        return false;
                    }
                }
            })
        });

        $('#caseDetailTree').on("changed.jstree", function (e, data) {
            window.selectedId = data.selected;
            var containerText = getContainerText(data.node.text);
            $('#' + containerText).collapse('toggle');
        });

        $('.collapse').collapse('toggle');

        function getContainerText(text) {
            return text.toLowerCase().replace(/ /g, "_") + "_container";
        }
    });
});