<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <asset:javascript src="app/pvs/departments.js"/>
    <g:set var="entityName" value="${message(code: 'user.label')}"/>
    <title><g:message code="default.edit.label" args="[entityName]"/></title>
</head>

<body>
<script>
    $(document).ready(function () {
       $('#editUserButton').on('click', function () {
          $(this).attr('disabled', true);
          $('#editUserForm').submit();
       });
    });
</script>

<rx:container title="${message(code: "app.label.userManagement")}">

    <h1 class="page-header"><g:message code="default.edit.label" args="[entityName]"/></h1>

    <g:render template="/includes/layout/flashErrorsDivs" bean="${userInstance}" var="theInstance"/>

    <g:form method="put" action="update" class="form-horizontal form-elements-left" name="editUserForm">
        <g:hiddenField name="id" value="${userInstance?.id}"/>
        <g:hiddenField name="version" value="${userInstance?.version}"/>

        <g:render template="form" model="[userInstance: userInstance]"/>

        <div class="buttonBar">
            <button name="edit" class="btn btn-primary" id="editUserButton">
                %{--<span class="glyphicon glyphicon-ok icon-white"></span>--}%
                ${message(code: 'default.button.save.label')}
            </button>
            <g:link class="cancelLink btn btn-default" action="index"><g:message code="default.button.cancel.label"/></g:link>
        </div>
    </g:form>

</rx:container>

</body>
</html>
