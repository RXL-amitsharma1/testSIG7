<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">

    <title><g:message code="logout.local.title"/></title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="shortcut icon" href="${assetPath(src: 'favicon.ico')}" type="image/x-icon">
    <asset:stylesheet href="application.css"/>
    <asset:stylesheet href="theme_gradient_blue.css"/>
</head>

<body class="fixed-left">

<!-- Begin page -->
<div id="wrapper" class="enlarged forced">
    <!-- Top Bar Start -->
    <div class="topbar">
        <!-- LOGO -->
        <div class="pull-left">
            &nbsp;
            <span class="clearfix"></span>
        </div>

        <div class="topbar-left" style="padding-left: 40px;">
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
    <div class="content-page">
        <!-- Start content -->
        <div id="mainContent" class="content" style="">
            <div class="page-header">
                <h1><g:message code="logout.local.title"/></h1>
            </div>
            <g:link controller="login" action="auth">
                <button type="button" class="btn btn-primary"><g:message code="logout.local.link.start"/></button>
            </g:link>
        </div>
    </div>
    <!-- End content -->
</div>
<!-- ============================================================== -->
<!-- End Right content here -->
<!-- ============================================================== -->
</body>
</html>