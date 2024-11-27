<%@ page import="com.rxlogix.enums.GroupType; com.rxlogix.user.UserRole; com.rxlogix.user.Role; grails.plugin.springsecurity.SpringSecurityUtils" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'user.label')}"/>
    <title><g:message code="user.label"/></title>
</head>

<body>

<g:set var="column1Width" value="4"/>
<g:set var="column2Width" value="8"/>

<rx:container title="${message(code: "app.label.userManagement")}">

    <g:link action="index"><< <g:message code="users.list" /></g:link>

    <h3 class="page-header"><g:message code="user.label"/></h3>

    <g:render template="/includes/layout/flashErrorsDivs" bean="${userInstance}" var="theInstance"/>

    <g:set var="whatIsBeingDeleted" value="${userInstance.fullName + (userInstance?.email ? ' (' + userInstance.email + ')' : '')}"/>
    <g:if test="${SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN")}">
        <g:render template="/includes/widgets/buttonBarCRUD" bean="${userInstance}" var="theInstance"
                  model="[showDeleteButton: false]"/>
    </g:if>

    %{--User Details--}%
    <div class="row">
        <div class="col-md-12">

            <h3 class="sectionHeader"><g:message code="user.details"/></h3>

            <div class="col-md-6">

                <div class="row">
                    <div class="col-md-${column1Width}"><label><g:message code="user.username.label"/></label></div>
                    <g:if test="${userInstance.username?.equalsIgnoreCase("SYSTEM")}">
                        <div class="col-md-${column2Width}">${userInstance.username?.toUpperCase()}</div>
                    </g:if>
                    <g:else>
                        <div class="col-md-${column2Width}">${userInstance.username}</div>
                    </g:else>
                </div>

                <div class="row">
                    <div class="col-md-${column1Width}"><label><g:message code="user.fullName.label"/></label></div>
                    <g:if test="${userInstance.fullName?.equalsIgnoreCase("SYSTEM")}">
                        <div class="col-md-${column2Width}">${userInstance.fullName?.toUpperCase()}</div>
                    </g:if>
                    <g:else>
                        <div class="col-md-${column2Width}">${userInstance.fullName}</div>
                    </g:else>
                </div>

                <div class="row">
                    <div class="col-md-${column1Width}"><label><g:message code="user.email.label"/></label></div>

                    <div class="col-md-${column2Width}">${userInstance.email}</div>
                </div>
                <div class="row">
                    <div class="col-md-${column1Width}"><label><g:message code="user.department.label"/></label></div>
                    <g:if test="${userInstance.userDepartments?.departmentName.join(",")}">
                        <div class="col-md-${column2Width}">${userInstance.userDepartments?.departmentName.join(",")}</div>
                    </g:if>
                    <g:else>
                        <div class="col-md-${column2Width}">-</div>
                    </g:else>
                </div>
            </div>
            <div class="col-md-6">

                <div class="row">
                    <div class="col-md-${column1Width}"><label><g:message code="user.enabled.label"/></label></div>

                    <div class="col-md-${column2Width}"><g:formatBoolean boolean="${userInstance.enabled}" false="No"
                                                                         true="Yes"/></div>
                </div>

                <div class="row">
                    <div class="col-md-${column1Width}"><label><g:message code="user.accountLocked.label"/></label></div>

                    <div class="col-md-${column2Width}"><g:formatBoolean boolean="${userInstance.accountLocked}" false="No"
                                                                         true="Yes"/></div>
                </div>

                <div class="row">
                    <div class="col-md-${column1Width}"><label><g:message code="user.badPasswordAttempts.label"/></label></div>

                    <div class="col-md-${column2Width}">${userInstance.badPasswordAttempts}</div>
                </div>

                <div class="row">
                    <div class="col-md-${column1Width}"><label><g:message code="user.accountExpired.label"/></label></div>

                    <div class="col-md-${column2Width}"><g:formatBoolean boolean="${userInstance.accountExpired}" false="No"
                                                                         true="Yes"/></div>
                </div>

            </div>
        </div>
    </div>

    <div style="margin-top: 40px"></div>

    <div class="row">

        %{--Roles--}%
        <div class="col-md-6">

            <h3 class="sectionHeader"><g:message code="roles.label"/></h3>



            <g:each in="${roles.findAll { !("ROLE_DEV".equalsIgnoreCase(it)) }}" var="userRoleInstance" status="i">
                <div class="row">
                    <div class="col-md-${column2Width}">
                        <g:message code="app.role.${userRoleInstance}" default="${userRoleInstance}"/>
                    </div>
                </div>
            </g:each>

        </div>

        %{--Preferences--}%
        <div class="col-md-6">

            <h3 class="sectionHeader"><g:message code="app.label.preference"/></h3>

            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message code="app.label.language"/></label></div>

                <div class="col-md-${column2Width}">${userInstance.preference.locale.displayName}</div>
            </div>

            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message code="app.label.timezone"/></label></div>
                <div class="col-md-${column2Width}">${com.rxlogix.util.ViewHelper.getMessageByTimeZoneId(userInstance.preference.timeZone)}</div>

            </div>
        </div>
   </div>


</rx:container>


</body>
</html>


