<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:javascript>var getProductURL = "${createLink( controller: 'group', action: 'getProducts' )}"</g:javascript>
    <g:set var="entityName"
           value="${type == "workflow" ? message( code: 'user.workflow.group.label' ) : message( code: 'user.user.group.label' )}"/>
    <asset:stylesheet src="jquery-picklist.css"/>
    <asset:javascript src="application.js"/>
    <asset:javascript src="jquery-ui/jquery-ui.min.js"/>
    <asset:javascript src="app/pvs/group/groups.js"/>
    <asset:javascript src="jquery/jquery-picklist.js"/>
    <asset:javascript src="vendorUi/select2/select2.js"/>
    <title><g:message code="default.edit.label" args="[ entityName ]"/></title>
    <asset:javascript src="app/pvs/productSelection.js"/>

    <g:javascript>
        $(document).ready(function () {
            $("#disposition").find(".commonSelect2Class").select2();
            $(".rxmain-container-content").click(function () {
                $("#notificationContainer").hide();
            });
        });
    </g:javascript>
</head>

<body>
%{--<rx:container title="${message(code: "app.label.groupManagement")}">--}%
<rx:container
        title="${type == "user" ? message( code: "app.label.groupManagement.user" ) : message( code: "app.label.groupManagement.workflow" )}">

    <g:if test="${flash.error}">
        <div class="alert alert-danger alert-dismissible" role="alert" style="word-break: break-all">
            <button type="button" class="close" data-dismiss="alert">
                <span  onclick="this.parentNode.parentNode.remove(); return false;">x</span>
                <span class="sr-only"><g:message code="default.button.close.label" /></span>
            </button>
            <g:if test="${flash.error}">
                <g:if test="${flash.error.contains('<linkQuery>')}">
                    ${flash.error.substring(0, flash.error.indexOf('<linkQuery>'))}
                    <a href="${flash.error.substring(flash.error.indexOf('<linkQuery>') + 11)}"><g:message code="see.details" /></a>
                </g:if>
                <g:else>
                    <g:if test="${flash.error instanceof List}">
                        <g:each in="${flash.error}" var="field">
                            <li>${raw(field)}</li>
                        </g:each>
                    </g:if>
                    <g:else>
                        ${raw(flash.error)}<br>
                    </g:else>
                </g:else>
            </g:if>
        </div>
    </g:if>

    <g:form method="put" action="update" class="form-horizontal" name="groupMngtForm">
        <input type="hidden" name="type" value="${type}">
        <g:hiddenField name="id" value="${groupInstance?.id}"/>
        <g:hiddenField name="version" value="${groupInstance?.version}"/>


        <g:if test="${type == "workflow"}">
            <g:render template="form"
                      model="[groupUsersList:groupUsersList, groupInstance            : groupInstance, allowedProductsList: allowedProductsList, defaultDispositionList: defaultDispositionList,
                               alertLevelDispositionList: alertLevelDispositionList, defaultSignalDispositionList: defaultSignalDispositionList, edit: true, type: type ]"/>
        </g:if>
        <g:else>
            <g:render template="userForm"
                      model="[groupUsersList:groupUsersList, groupRoleList:groupRoleList, groupInstance            : groupInstance, allowedProductsList: allowedProductsList, defaultDispositionList: defaultDispositionList,
                               alertLevelDispositionList: alertLevelDispositionList, defaultSignalDispositionList: defaultSignalDispositionList, edit: true, type: type ]"/>
        </g:else>



        <div class="buttonBar m-t-10" align="right">
            <button name="edit" class="btn btn-primary">
                ${message( code: 'default.button.save.label' )}
            </button>
            <g:link class="cancelLink btn btn-default" action="index"><g:message
                    code="default.button.cancel.label"/></g:link>
        </div>
    </g:form>

</rx:container>
<g:render template="addRemoveUser" model="[allowedUsers:userGroupUserList, allUserList: allUserList]"/>
<g:javascript>
$(document).ready(function(){
    addCountBoxToInputField(255, $('input#name'));
    addCountBoxToInputField(4000, $('textarea#description'));
    $('#groupType').focus();
});
</g:javascript>
</body>
</html>
