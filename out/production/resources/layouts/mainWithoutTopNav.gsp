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
    </script>
    <g:layoutHead/>
    <r:layoutResources/>
</head>

<body>

<!-- Begin page -->
<div id="wrapper" class="enlarged forced">

    <!-- ============================================================== -->
    <!-- Start right Content here -->
    <!-- ============================================================== -->
    <div>
        <!-- Start content -->
        <div id="mainContent" class="content">
            <div class="container">
                <g:layoutBody/>
                <r:layoutResources/>
            </div>
        </div>
        <!-- End content -->
    </div>
    <!-- ============================================================== -->
    <!-- End Right content here -->
    <!-- ============================================================== -->
</div>

</body>
</html>