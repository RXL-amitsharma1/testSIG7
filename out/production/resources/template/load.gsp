<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.loadTemplates.title" /></title>
    <asset:javascript src="app/pvs/expandingTextarea.js"/>
    <asset:stylesheet src="expandingTextarea.css"/>
</head>
<body>
<rx:container title="${message(code:"app.label.loadTemplates")}" bean="${error}">

%{--todo:  This should be cleaned up. This "error" object is really the query being created. - morett  --}%
    <g:render template="/includes/layout/flashErrorsDivs" bean="${error}" var="theInstance"/>

    <div class="container-fluid">
        <g:form controller="template" action="saveJSONTemplates" method="post">

            <pre><g:message code="templates.paste" /></pre>
            <div class="expandingArea">
                <pre><span></span><br></pre>
                <g:textArea name="JSONTemplates" value="" />
            </div>

            <div class="row">
                <div class="col-xs-12">
                    <div class="pull-right">
                        <g:actionSubmit class="btn primaryButton btn-primary" action="saveJSONTemplates" value="${message(code:'default.button.save.label')}"></g:actionSubmit>
                        <a class="btn btn-default pv-btn-grey" href="${createLink(controller:'template', action:'index')}"><g:message code="default.button.cancel.label"/></a>
                    </div>
                </div>
            </div>
        </g:form>
    </div>
</rx:container>
</body>
