<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils" %>
<head>
    <meta name="layout" content="main"/>
    <title>Decision Support System</title>

</head>

<body>
<rx:container title="Decision Support System">
    <div id="dssViewPanel"></div>
    <g:javascript>
        var finalURL =  "${finalUrl}";
        var iframe = document.createElement('iframe');
        iframe.setAttribute('id', 'view-dssscores-iframe');
        iframe.setAttribute('width', '100%');
        iframe.setAttribute('frameborder', '0');

        function adjustIframeHeight() {
            var windowHeight = $(window).height();
            var iframeHeight = windowHeight - ($("#dssViewPanel").offset().top + 30);
            $("#view-dssscores-iframe").height(iframeHeight);
        }

        $(iframe).on('load', function() {
            adjustIframeHeight();
        });
        $(window).resize(adjustIframeHeight);

        iframe.src = finalURL;
        var dssViewPanel = document.getElementById('dssViewPanel')
        dssViewPanel.innerHTML = "";
        dssViewPanel.appendChild(iframe);
    </g:javascript>
    </div>


</rx:container>
</body>
