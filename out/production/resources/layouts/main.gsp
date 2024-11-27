<g:set var="samlEnabled" value = "${grailsApplication.config.grails.plugin.springsecurity.saml.active}" />
<g:set var="samlURL" value = "${grailsApplication.config.saml.local.logout.url}" />
<!doctype html>
<html>
<head>
    <g:render template="/includes/layout/layoutHead"/>
    <script>
        if(typeof userLocale ==="undefined"){
            userLocale="en"
        }
        $.getJSON('/signal/assets/i18n/' + userLocale + '.json', function (data) {
            $.i18n.load(data);
        });
        var resizefunc = [];
        var lastVisitedUrl = localStorage.getItem('lastVisitedURL');
        localStorage.setItem('lastVisitedURL', document.URL.split('?')[0]);
    </script>
    <g:render template="/includes/modals/notification_email_modal"/>

    <meta name="_csrf" content="${_csrf?.token}"/>
    <!-- default header name is X-CSRF-TOKEN -->
    <meta name="_csrf_header" content="${_csrf?.headerName}"/>
    <meta name="_csrf_parameter" content="${_csrf?.parameterName}"/>

    <g:layoutHead/>
    <r:layoutResources/>
    <asset:deferredScripts/>
    <g:javascript>
    var isDataSourceEnabled = ${grailsApplication.config.signal.evdas.enabled || grailsApplication.config.signal.faers.enabled || grailsApplication.config.signal.vaers.enabled  || grailsApplication.config.signal.vigibase.enabled}
    </g:javascript>

</head>

<body class="fixed-left">

<!-- Begin page -->
<div id="wrapper" class="enlarged forced">
    <!-- Top Bar Start -->
    <g:render template="/includes/layout/topNav"/>
    %{--<g:render template="/poc/topBar" />--}%
    <!-- Top Bar End -->
    <!-- ========== Left Sidebar Start ========== -->
    %{--<g:render template="/poc/side_bar"/>--}%
    <!-- Top Bar End -->

    <!-- Left Sidebar Start -->
    <g:render template="/includes/layout/leftNav"/>
    <!-- Left Sidebar End -->

    <!-- ============================================================== -->
    <!-- Start right Content here -->
    <!-- ============================================================== -->
    <div class="content-page">
        <!-- Start content -->
        <div id="mainContent" class="content">
            <div class="container">
                <g:layoutBody/>
                <r:layoutResources/>
            </div>
        </div>
        <!-- End content -->

        %{--<footer class="footer text-right">--}%
        %{--<g:render template="/includes/layout/footer"/>--}%
        %{--</footer>--}%
    </div>
    <!-- ============================================================== -->
    <!-- End Right content here -->
    <!-- ============================================================== -->
</div>
<g:if test="${!session.hideMenu && !params.boolean('iframe')}">
    <sec:ifLoggedIn>
        <g:render template="/sessionTimeout/sessiontimeout"/>
    </sec:ifLoggedIn>
</g:if>
<asset:javascript src="app/jquery.core.js"/>
<asset:javascript src="app/jquery.app.js"/>
<asset:javascript src="app/pvs/common/rx_common.js"/>
</body>
</html>
