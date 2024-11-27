<%@ page import="com.rxlogix.util.ViewHelper" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.Preference.title"/></title>
    <script>
        $(document).ready(function () {
            $("#language").select2();
            $("#timeZone").select2();
        });
    </script>
</head>

<body>
<rx:container title="${message(code: 'app.label.preference')}" bean="${error}">

    <g:render template="/includes/layout/flashErrorsDivs"/>

    <div class="row">
        <div class="col-xs-offset-0 col-xs-4">
            <g:form name="selectedLocale" action="update">
                <div class="form-group">
                    <label><g:message code="app.label.language"/></label>

                    <div>
                        <g:select class="form-control" id="language" name="language" from="${locales}"
                                  value="${currentLocale.lang_code}" optionKey="lang_code" optionValue="display"/>
                    </div>

                </div>

                <div class="form-group">
                    <label><g:message code="app.label.timezone"/></label>

                    <div class="form-group">
                        <g:select id="timeZone"
                                  name="timeZone"
                                  from="${ViewHelper.getTimezoneValues()}"
                                  optionKey="name"
                                  optionValue="display"
                                  class="form-control"
                                  value="${theUserTimezone}"/>
                    </div>
                </div>

                <div class="form-group hide"> //hiding this instead of removing due to critival build delivery PVS-64981
                    <label class="m-r-10" for="isEmailEnabled"><g:message code="label.description"
                                                                          default="Notify By Email"/>:</label>
                    <input type="checkbox" name="isEmailEnabled" id="isEmailEnabled"
                        ${isEmailEnabled ? 'checked' : ''}/>
                </div>
                <sec:ifAnyGranted roles="ROLE_CONFIGURATION_CRUD">
                    <div class="form-group">
                        <input class="btn btn-primary" type="submit"
                               value="${message(code: 'default.button.ok.label')}"/>
                    </div>
                </sec:ifAnyGranted>
            </g:form>
        </div>
    </div>
    <div>
        <div class="form-group">
            <label class="m-r-10" for="appVersion">
                <g:message code="app.label.application.version"
                           default="PVS Application Version"/>:</label>
            <g:meta id="appVersion" name="info.app.version"></g:meta>
        </div>
    </div>

</rx:container>
</body>
