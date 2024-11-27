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
    if (typeof userTimeZone === "undefined") {
            userTimeZone = "UTC"
        }
    //userTimeZone = '${grails.util.Holders.config.server.timezone}';
</g:javascript>
<asset:stylesheet href="application.css"/>
<asset:stylesheet href="app/pvs/pvs_app_css.css"/>
<asset:stylesheet href="mdi-fonts/css/materialdesignicons.css"/>
<asset:stylesheet href="app/pvs/pvs_508c.css"/>

<asset:javascript src="vendorUi/jquery/jquery-3.7.1.min.js"/>
<asset:javascript src="UIConstants.js"/>
<asset:javascript src="common/change-theme.js"/>
<asset:javascript src="application.js"/>
<asset:javascript src="vendorUi/fuelux/fuelux-3.17.1.min.js"/>
<asset:javascript src="app/pvs/menu.js"/>
<asset:javascript src="app/pvs/pvs_app_widget.js"/>
<asset:javascript src="app/pvs/userGroupSelect.js"/>
<script>
    if(typeof userLocale ==="undefined"){
        userLocale="en"
    }
    $(function() {
        if(localStorage.getItem('i18keys')==null) {
            $.getJSON('/signal/assets/i18n/' + userLocale + '.json', function (data) {
                $.i18n.load(data);
                localStorage.setItem('i18keys', JSON.stringify(data));
            });
        } else {
            $.i18n.load(JSON.parse(localStorage.getItem('i18keys')));
        }
    })
</script>

<script>
    <sec:ifLoggedIn>
    userLocale = "${getCurrentUserLanguage()}";
    moment.locale(userLocale);
    userTimeZone = "${getCurrentUserTimezone()?:TimeZone.default.ID}";
    userDefaultTimeZone = "${TimeZone.default.ID}"; // added to fix bug/PVS-53974
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