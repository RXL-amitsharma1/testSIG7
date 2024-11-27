<html>
<head>
    <title><g:message code="springSecurity.login.title"/></title>
    <asset:stylesheet href="login.css"/>
    <asset:stylesheet href="application.css"/>
    <g:javascript>
        if (typeof serverTimeZone === "undefined") {
            serverTimeZone = "UTC"
        }
    </g:javascript>
    <asset:javascript src="application.js"/>
</head>
    <body>
        <div class="container">
            <div id="loginStyle">
                <form action='${postUrl}' method='POST' id="loginForm" role="form" autocomplete="off">
                    <input type="hidden" name="${_csrf?.parameterName}" value="${_csrf?.token}"/>
                    <div id="loginTable">
                        <div id="loginHeader">
                            <div class="pvLogo">
                                <asset:image src="pv-signal-logo.png" id="loginLogo" />
                            </div>
                            <div class="header-lbl">
                                <g:message code="log.in" />
                                %{--<label id="loginLabel"><g:message code="log.in" /></label>--}%
                            </div>
                        </div>

                        <div id="loginUserContainer">
                            <input type="text" name="username" id="username" class="loginFieldUser" placeholder="${message(code: 'user.username.label')}" required autofocus>
                        </div>

                        <div id="loginPassContainer">
                            <input type="password" name="password" id="password" class="loginFieldPass" placeholder="${message(code: 'user.password.label')}" required>
                        </div>

                        <div id="loginFooter">
                            <button id="loginSubmit" type="submit"><g:message code="log.in" /></button>
                        </div>

                    </div>
                    <div class="text-center">
                        <div class="login_message text-white"><em style="font-style: inherit"><g:message
                                code="app.restricted.use.message"/></em></div>
                    </div>
                    <div class="margin20Top">
                        <g:render template="/includes/layout/flashErrorsDivs"/>
                    </div>

                </form>
            </div>
            <footer class="page-footer font-small pt-4">
                <!-- Copyright -->
                <div class="footer-copyright text-center py-3"> ${message} <rx:renderSecurityPolicyLink class="policy-link" target="_blank"/>
                </div>
                <!-- Copyright -->
            </footer>
        </div>
    <script type='text/javascript'>
        (function () {
            localStorage.removeItem("dashboardWidgetsConfig");
            document.forms['loginForm'].elements['username'].focus();
        })();
    </script>
    </body>
</html>
