<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'controlPanel.label')}"/>
    <title><g:message code="app.controlPanel.title"/></title>
</head>

<body>

<rx:container title="${message(code: "controlPanel.label")}">

    <g:render template="/includes/layout/flashErrorsDivs" bean="${applicationSettingsInstance}" var="theInstance"/>

    <h3><g:message code="app.dms.title.label"/></h3>

    <g:form controller="test" action="saveDmsConfiguration">
        <g:hiddenField name="id" value="${applicationSettingsInstance?.id}"/>
        <div class="margin20Top">
            <div class="row">
                <div class="col-md-4">
                    <g:textArea name="dmsSettings" class="dmsSettings"
                                value="${applicationSettingsInstance.dmsIntegration}"
                                style="resize: both;width: 100%;height: 270px;"/>
                </div>

                <div class="col-md-4">
                    <b><g:message code="example"/></b>
                    <pre style="height: 250px;">
                        ${defaultSetting}
                    </pre>
                </div>

                <div class="col-md-4">

                </div>
            </div>

            <div id="errorDmsDiv" style="display: none"></div>
            <input type="submit" value="Update" class="btn btn-primary saveDmsSettings"/>
        </div>
    </g:form>
</rx:container>
</body>
</html>
