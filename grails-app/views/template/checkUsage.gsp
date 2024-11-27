<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.CheckUsage.title"/></title>
    <asset:javascript src="app/pvs/template/usage.js"/>
</head>

<body>
<rx:container title="Check usage for template: ${template}" options="${true}" bean="${error}">

    <g:render template="/includes/layout/flashErrorsDivs"/>

    <input type="button" class="btn btn-default pull-right pv-btn-grey" onclick="goBack()"
           value="${message(code: 'default.back.label')}">
    <table id="rxCheckTemplateUsage" class="row-border hover" width="100%">
        <thead>
        <tr>
            <th><g:message code="app.label.name"/></th>
            <th><g:message code="app.label.queryName"/></th>
            <th><g:message code="app.label.reportDescription"/></th>
            <th><g:message code="app.label.dateCreated"/></th>
            <th><g:message code="app.label.scheduled"/></th>
            <th><g:message code="app.label.tag"/></th>
        </tr>
        </thead>
        <g:each in="${usages}" var="usage">
            <g:each in="${usage.templateQueries}" var="templateQuery">
                <tr>
                    <td>${templateQuery.report.name}</td>
                    <td>${templateQuery.query?.name}</td>
                    <td>${templateQuery.report.description}</td>
                    <td>${templateQuery.report.dateCreated}</td>
                    <td>${templateQuery.report.owner.fullName}</td>
                    <td>
                        <g:each in="${templateQuery.report.tags}" var="tag">
                            <div>${tag.name}</div>
                        </g:each>
                    </td>
                </tr>
            </g:each>
        </g:each>
    </table>
    <input type="button" class="btn btn-default pull-right pv-btn-grey" onclick="goBack()"
           value="${message(code: 'default.back.label')}">
</rx:container>
</body>