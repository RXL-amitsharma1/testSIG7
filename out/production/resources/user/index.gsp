<%@ page import="com.rxlogix.util.DateUtil; com.rxlogix.enums.GroupType" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message( code: 'user.label' )}"/>
    <g:set var="userService" bean="userService"/>
    <title><g:message code="users.label"/></title>

    <style>
    #user-table {
        overflow-x: initial;
        padding: 15px;
    }

    .height-50 {
        height: 50px !important;;
    }
    </style>

    <asset:javascript src="yadcf/jquery.datatables.yadcf.js"/>
    <asset:stylesheet src="yadcf/jquery.dataTables.yadcf.css"/>
    <asset:javascript src="app/pvs/users/user_detail.js"/>
    <g:javascript>
        var encodePasswordLink = "${createLink( controller: 'user', action: 'encodePassword' )}"
    </g:javascript>
    <asset:javascript src="app/pvs/users/user_management.js"/>

    %{--Removed extra bulk update ajax call as it already exist in user_detail.js--}%
</head>

<body>
<g:javascript>
    window.onloadstart = resetSearchBox();
</g:javascript>
<rx:container title="${message( code: "app.label.userManagement" )}">
    <g:render template="/includes/layout/flashErrorsDivs" bean="${userInstance}" var="theInstance"/>
    <sec:ifAnyGranted roles="ROLE_ADMIN">
        <div class="row">
            <div class="navScaffold col-xs-12">
                <g:link class="btn btn-primary" action="create">
                    <span class="glyphicon glyphicon-plus icon-white"></span>
                    <g:message code="default.new.label" args="[ entityName ]"/>
                </g:link>

                <a class="btn btn-primary" href="#copyBulkUsersModal" data-toggle="modal"><g:message
                        code="default.copy.users.label"/></a>
                <a data-toggle="modal" data-target="#passwordEncoder" title="Password Encoder"><i style="float: right"
                                                                                                  class="md md-settings"></i>
                </a>
            </div>

            <div class="modal fade" id="copyBulkUsersModal" tabindex="-1" role="dialog" aria-hidden="true">
                <div class="modal-dialog">
                    <div class="modal-content">
                        <div class="modal-header">
                            <label><g:message code="default.copy.users.label"/></label>
                        </div>

                        <div class="modal-body">
                            <g:message code="default.copy.users.modal.body" args="${[ activeUsersCount ]}"/>
                        </div>

                        <div class="modal-footer">
                            <button type="button" id="addBulkUsers" class="btn btn-primary clearEventValues"><g:message
                                    code="default.button.confirm.label"/></button>
                            <button type="button" class="btn btn-default" data-dismiss="modal"><g:message
                                    code="default.button.cancel.label"/></button>
                        </div>
                    </div>
                </div>
            </div>

            <div class="modal fade" id="passwordEncoder" role="dialog"
                 aria-hidden="true">
                <div class="modal-dialog none">
                    <div class="modal-content">
                        <div class="modal-header">
                            <button type="button" class="close encode-close-bt" data-dismiss="modal"
                                    aria-label="Close"><span
                                    aria-hidden="true">&times;</span></button>
                            <h4 class="modal-title" id="">Password Encoder</h4>
                        </div>

                        <div class="modal-body clearfix">
                            <div class="form-group">
                                <label><g:message code="user.password.label"/></label>
                                <g:passwordField class="form-control" name="passwordToBeEncoded"
                                                 id="passwordToBeEncoded"></g:passwordField>
                            </div>

                            <div hidden="true" class="form-group" id="encodeTextBox">
                                <label><g:message code="user.encodedPassword.label"/></label>
                                <g:textField class="form-control" name="encodedPassword"
                                             id="encodedPassword"
                                             readOnly="true"></g:textField>
                            </div>

                        </div>

                        <div class="modal-footer">
                            <div class="buttons ">
                                <button class="button  btn btn-primary" id="encode-bt">Encode</button>
                                <button data-dismiss="modal" aria-label="Close"
                                        class="button btn btn-default encode-close-bt">Cancel</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </sec:ifAnyGranted>

    <div id="user-table" class="table-responsive curvedBox">
        <table id="userTable" class="table table-striped table-curved table-hover">
            <thead>
            <tr>
                <th><g:message code="user.username.label"/></th>
                <th><g:message code="user.fullName.label"/></th>
                <th><g:message code="user.email.label"/></th>
                <th><g:message code="user.enabled.label"/></th>
                <th><g:message code="user.lastLogin.label"/></th>
                <th><g:message code="user.roles.label"/></th>
                <th><g:message code="user.user.group.label"/></th>
                <th><g:message code="user.workflow.group.label"/></th>
            </tr>
            </thead>
            <tbody>
            <g:if test="${userInstanceTotal > 0}">
                <%
                    def userTimeZone = userService.getCurrentUserPreference().timeZone
                %>
                <g:each in="${userInstanceList}" status="i" var="userInstance">
                    <tr class="height-50">
                        <g:if test="${userInstance.username?.equalsIgnoreCase( "SYSTEM" )}">
                            <td><g:link action="show"
                                        id="${userInstance.id}">${userInstance.username?.toUpperCase()}</g:link></td></g:if>
                        <g:else>
                            <td><g:link action="show" id="${userInstance.id}">${userInstance.username}</g:link></td>
                        </g:else>
                        <g:if test="${userInstance.fullName?.equalsIgnoreCase( "SYSTEM" )}">
                            <td>${userInstance.fullName?.toUpperCase()}</td>
                        </g:if>
                        <g:else>
                            <td>${userInstance.fullName}</td>
                        </g:else>
                        <td>${userInstance.email}</td>
                        <td><g:formatBoolean boolean="${userInstance.enabled}" false="No" true="Yes"/></td>
                        <td class="dataTableColumnCenter forceLineWrapDateAlign">
                            <span hidden>${userInstance.lastLogin}</span>
                            <g:if test="${userInstance.lastLogin}">
                                ${com.rxlogix.util.DateUtil.toDateStringWithTimeInAmPmFormat(userInstance.lastLogin, userTimeZone)}
                            </g:if>
                            <g:else>
                                <g:message code="user.neverLoggedIn.label"/>
                            </g:else>

                        </td>
                        <td>
                            <g:each var="data" in="${userInstance.getAuthorities(false).findAll { !("ROLE_DEV".equalsIgnoreCase(it.authority)) }}">
                                ${data}<br>
                            </g:each>

                        </td>
                        <td class = "pvi-col-md" style="word-break:break-all;word-wrap:break-word;">
                            <g:each var="data"
                                    in="${userGroupMapping[userInstance.id as Long]?.userGroups}">
                                ${data}<br>
                            </g:each>

                        </td>
                        <td style="word-break:break-all;word-wrap:break-word;">
                            <g:each var="data"
                                    in="${userGroupMapping[userInstance.id as Long]?.workFlowGroup}">
                                ${data}<br>
                            </g:each>

                        </td>
                    </tr>
                </g:each>
            </g:if>
            <g:else>
                <tr>
                    <td colspan="7"><g:message code="app.label.none"/></td>
                </tr>
            </g:else>
            </tbody>
        </table>
    </div>

</rx:container>
</body>
</html>

