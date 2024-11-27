<%@ page import="grails.util.Holders; com.rxlogix.user.User;com.rxlogix.user.Preference" %>

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
<title><g:layoutTitle default="PV Signal"/></title>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link rel="shortcut icon" href="${assetPath(src: 'favicon.ico')}" type="image/x-icon">
<link rel="apple-touch-icon" href="${assetPath(src: 'apple-touch-icon.png')}">
<link rel="apple-touch-icon" sizes="114x114" href="${assetPath(src: 'apple-touch-icon-retina.png')}">
    <g:javascript>
    serverTimeZone = '${grails.util.Holders.config.server.timezone}';
    </g:javascript>
<asset:stylesheet href="application.css"/>
<asset:stylesheet href="theme_gradient_blue.css"/>
<asset:stylesheet href="app/pvs/pvs_app_css.css"/>
<asset:stylesheet href="app/pvs/pvs_508c.css"/>
<asset:stylesheet href="mdi-fonts/css/materialdesignicons.css"/>
<asset:stylesheet href="perfect-scrollbar/perfect-scrollbar.css"/>
<asset:javascript src="vendorUi/modernizr.min.js"/>
<asset:javascript src="vendorUi/jquery/jquery-2.2.4.js"/>
<asset:javascript src="vendorUi/jquery.i18n.js"/>
<script>
    if(typeof userLocale ==="undefined"){
        userLocale="en"
    }
    if(localStorage.getItem('i18keys')==null) {
        $.getJSON('/signal/assets/i18n/' + userLocale + '.json', function (data) {
            $.i18n.load(data);
            localStorage.setItem('i18keys', JSON.stringify(data));
        });
    } else {
        $.i18n.load(JSON.parse(localStorage.getItem('i18keys')));
    }
</script>
<asset:javascript src="vendorUi/underscore/underscore.min.js"/>
<asset:javascript src="vendorUi/jquery-ui/jquery-ui.min.js"/>
<asset:javascript src="vendorUi/bootstrap/bootstrap.min.js"/>
<asset:javascript src="vendorUi/moment/moment.js"/>
<asset:javascript src="vendorUi/moment/momentlocales.js"/>
<asset:javascript src="vendorUi/moment/momentTimezones-with-data.js"/>
<asset:javascript src="vendorUi/popover/popover.min.js"/>
<asset:javascript src="vendorUi/fuelux/fuelux.min.js"/>
<asset:javascript src="vendorUi/select2/select2.js"/>
<asset:javascript src="datatables/jquery.dataTables_custom.js"/>
<asset:javascript src="vendorUi/datatables/dataTables.bootstrap.js"/>
<asset:javascript src="vendorUi/datatables/dataTables.bootstrapPagination.js"/>
<asset:javascript src="vendorUi/datatables/datetime-moment.js"/>
<asset:javascript src="vendorUi/datatables/dataTables.fixedColumns.min.js"/>
<asset:javascript src="vendorUi/spinner/spinner.min.js"/>
<asset:javascript src="vendorUi/multiselect/jquery.multi-select2.0.js"/>
<asset:javascript src="vendorUi/wow/wow.min.js"/>
<asset:javascript src="vendorUi/mobile/fastclick.js"/>
<asset:javascript src="vendorUi/mobile/detect.js"/>
<asset:javascript src="vendorUi/jquery/jquery.nicescroll.js"/>
<asset:javascript src="vendorUi/jquery/jquery.slimscroll.js"/>
<asset:javascript src="vendorUi/jquery/jquery.blockUI.js"/>
<asset:javascript src="vendorUi/jquery/jquery.scrollTo.min.js"/>
<asset:javascript src="vendorUi/notifyjs/dist/notify.min.js"/>
<asset:javascript src="vendorUi/notifications/notify-metro.js"/>
<asset:javascript src="vendorUi/sweet-alert/sweet-alert.min.js"/>
<asset:javascript src="vendorUi/sweet-alert/rx-notify.js"/>
<asset:javascript src="vendorUi/bootstrapUploader/bootstrap-uploader.js"/>
<asset:javascript src="vendorUi/waypoints/lib/jquery.waypoints.min.js"/>
<asset:javascript src="vendorUi/counterup/jquery.counterup.min.js"/>
<asset:javascript src="vendorUi/handlebar/handlebars-v4.0.5.js"/>
<asset:javascript src="vendorUi/bootstrap-switch/bootstrap-switch.js"/>
<asset:javascript src="vendorUi/jquery.ba-throttle-debounce.js"/>
<asset:javascript src="common/jquery.core.js"/>
<asset:javascript src="application.js"/>
<asset:javascript src="vendorUi/bower_components/webcomponentsjs/webcomponents-lite.js"/>
<asset:javascript src="bootstrap-multiselect/bootstrap-multiselect.js"/>
<asset:javascript src="vendorUi/datatable/dataTables.buttons.min.js"/>
<asset:javascript src="datatables/buttons.bootstrap.js"/>
<asset:javascript src="vendorUi/waves/waves.js"/>
<asset:javascript src="app/pvs/rxTitleOptions.js"/>
<asset:javascript src="app/pvs/menu.js"/>
<asset:javascript src="app/pvs/pvs_app_widget.js"/>
<asset:javascript src="app/pvs/userGroupSelect.js"/>
<asset:javascript src="perfect-scrollbar/perfect-scrollbar.min.js"/>

<script>
    <sec:ifLoggedIn>
    userLocale = "${getCurrentUserLanguage()}";
    moment.locale(userLocale);
    userTimeZone = "${getCurrentUserTimezone()?:TimeZone.default.ID}";
    serverTimeZone = "${grails.util.Holders.config.server.timezone}";
    calenderUserTimeZone = "${getCurrentUserTimezone()}"
    <g:applyCodec encodeAs="none">
    var loggedInUser = "${getCurrentUserName()}";
    var loggedInFullname = "${getCurrentUserFullName()}";
    </g:applyCodec>
    maxUploadLimit = "${getMaxUploadLimit()}";
    userId = "${getCurrentUserInboxId()}"
    </sec:ifLoggedIn>
</script>