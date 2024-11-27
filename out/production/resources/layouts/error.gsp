<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">

    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="shortcut icon" href="${assetPath(src: 'favicon.ico')}" type="image/x-icon">
    <asset:stylesheet href="application.css"/>
    <asset:javascript src="vendorUi/jquery/jquery-2.2.4.js"/>
    <script>
        var LOAD_THEME_URL = "${createLink(controller: 'preference', action: 'loadTheme')}";
        var userLocale = "en";
        var APP_PATH='${request.contextPath}';
        var appContext = "${request.contextPath}";
        var APP_ASSETS_PATH=APP_PATH+'/assets/';
    </script>
    <asset:javascript src="application.js"/>
</head>

<body class="fixed-left">

<!-- Begin page -->
<div id="wrapper" class="enlarged forced">
    <asset:javascript src="UIConstants.js"/>
    <asset:javascript src="common/change-theme.js"/>
    <!-- Top Bar Start -->
    <div class="topbar">
        <!-- LOGO -->
        <div class="pull-left">
            <button class="button-menu-mobile open-left waves-effect">
                <i class="md md-menu"></i>
            </button>
            <span class="clearfix"></span>
        </div>

        <div class="topbar-left">
            <div class="pull-left">
                <g:link controller="dashboard" action="index">
                    <asset:image src="pv-signal-logo.png" class="pvLogo"/>
                </g:link>
            </div>
        </div>
        <!-- Navbar -->
        <div class="navbar navbar-default"></div>
    </div>
    <!-- Top Bar End -->

    <!-- Left Sidebar Start -->
    <div class="left side-menu"></div>
    <!-- Left Sidebar End -->

    <!-- ============================================================== -->
    <!-- Start right Content here -->
    <!-- ============================================================== -->
    <div class="content-page container" style = "height:100vh;margin-top: 70px">
        <!-- Start content -->
        <div id="mainContent" class="content">
            <g:layoutBody/>
        </div>
    </div>
    <!-- End content -->
</div>
<!-- ============================================================== -->
<!-- End Right content here -->
<!-- ============================================================== -->

</body>
</html>