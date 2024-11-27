<%@ page import="com.rxlogix.util.ViewHelper; com.rxlogix.enums.GroupType; com.rxlogix.config.Tag; com.rxlogix.user.UserRole; com.rxlogix.user.Role; com.rxlogix.user.Group; com.rxlogix.config.SafetyGroup;org.joda.time.DateTimeZone; com.rxlogix.mapping.LmProduct"%>
<asset:javascript src="app/pvs/users/user_edit.js" />
<asset:stylesheet src="app/pvs/updatedFixedColumn.css"/>

<g:set var="column1Width" value="4"/>
<g:set var="column2Width" value="8"/>

<g:if test="${actionName == 'create' || actionName == 'save'}" >
    <g:set var="createMode" value="${true}"/>
</g:if>

<g:if test="${actionName == 'edit' || actionName == 'update'}" >
    <g:set var="editMode" value="${true}"/>
</g:if>

<div class="row">

    %{--User Details--}%
    <div class="col-md-12">

        <h3 class="sectionHeader"><g:message code="user.details" /></h3>

        <div class="col-md-12">

            <div class="col-md-6">

                <div class="form-group m-0 m-r-10">
                    <label class="col-md-4 control-label" for="username">
                        <g:message code="user.username.label"/><span class="required-indicator">*</span>
                    </label>

                    %{--Create Mode--}%
                    <g:if test="${createMode}">
                        <div class="col-md-8">
                            <select name="username" id="username" class="form-control text-left"></select>
                            <div class="bs-callout bs-callout-info">
                                <h5><g:message code="search.for.ldap.user" />:</h5>
                                <div class="text-muted text-left">- <g:message code="search.by.username.fullname.or.email" /></div>
                                <div class="text-muted text-left">- <g:message code="users.previously.created.do.not.appear.in.search.results" /></div>
                            </div>
                        </div>

                            <div class="col-md-4 control-label">
                                <label><g:message code="user.department.label"/></label>
                            </div>
                            <div class="col-md-8">
                                <g:render template="/includes/widgets/department_select" model="['domainInstance': userInstance]"/>
                            </div>


                    </g:if>

                    %{--Edit Mode--}%
                    <g:if test="${editMode}" >
                        <div class="col-md-8">
                            <g:textField name="anyNameHereWillDoAsLongAsItsNotUsername"
                                         value="${userInstance?.username?.equalsIgnoreCase('SYSTEM')?userInstance?.username?.toUpperCase():userInstance?.username}"
                                         placeholder="${message(code: 'user.username.label')}"
                                         disabled="disabled"
                                         class="form-control"/>
                        </div>
                    </g:if>
                </div>

                %{--Edit Mode--}%
                <g:if test="${editMode}" >
                    <div class="form-group m-0 m-l-10">
                        <label class="col-md-${column1Width} control-label" for="fullName">
                            <g:message code="user.fullName.label"/>
                        </label>

                        <div class="col-md-${column2Width}">
                            <g:textField name="fullName"
                                         value="${userInstance?.fullName?.equalsIgnoreCase('SYSTEM')?userInstance?.fullName?.toUpperCase():userInstance?.username}"
                                         placeholder="${message(code: 'user.fullName.label')}"
                                         disabled="disabled"
                                         class="form-control"/>
                        </div>
                    </div>

                    <div class="form-group m-0 m-l-10">
                        <label class="col-md-${column1Width} control-label" for="email"><g:message code="user.email.label"/></label>

                        <div class="col-md-${column2Width}">
                            <g:textField name="email"
                                         value="${userInstance?.email}"
                                         placeholder="${message(code: 'user.email.label')}"
                                         disabled="disabled"
                                         class="form-control"/>
                        </div>
                    </div>
                    <div class="form-group m-0 m-l-10">
                        <div class="col-md-4 control-label">
                            <label><g:message code="user.department.label"/></label>
                        </div>
                        <div class="col-md-8">
                            <g:render template="/includes/widgets/department_select" model="['domainInstance': userInstance]"/>
                        </div>
                    </div>
                </g:if>

            </div>


            <div class="col-md-6 ">

                <div class="form-group m-0 m-l-10" style="padding-bottom: 8px">
                    <label class="col-md-${column1Width} control-label" for="enabled">
                        <g:message code="user.enabled.label"/>
                    </label>

                    <div class="col-md-${column2Width}">
                        <g:checkBox id="enabled" name="enabled" value="${userInstance.enabled}"/>
                    </div>
                </div>

                <div class="form-group m-0 m-l-10" style="padding-bottom: 8px">
                    <label class="col-md-${column1Width} control-label" for="accountLocked">
                        <g:message code="user.accountLocked.label"/>
                    </label>

                    <div class="col-md-${column2Width}">
                        <g:checkBox id="accountLocked" name="accountLocked" value="${userInstance.accountLocked}"/>
                    </div>
                </div>

                <div class="form-group m-0 m-l-10" style="padding-bottom: 8px">
                    <label class="col-md-${column1Width} control-label">
                        <g:message code="user.badPasswordAttempts.label"/>
                    </label>

                    <div class="col-md-${column2Width}">
                        ${userInstance.badPasswordAttempts}
                    </div>
                </div>

                <div class="form-group m-0 m-l-10" style="padding-bottom: 8px">
                    <label class="col-md-${column1Width} control-label" for="accountExpired">
                        <g:message code="user.accountExpired.label"/>
                    </label>

                    <div class="col-md-${column2Width}">
                        <g:checkBox id="accountExpired" name="accountExpired" value="${userInstance.accountExpired}"/>
                    </div>
                </div>

            </div>

        </div>
    </div>



            %{--Roles--}%
    <div class="col-md-6 ">

        <h3 class="sectionHeader">Roles</h3>

        %{--Create Mode--}%
        <g:if test="${actionName == 'create' || actionName == 'save'}" >
            <g:each var="role" in="${Role.list().findAll { !("ROLE_DEV".equalsIgnoreCase(it.authority)) }.sort{ a,b-> a.authorityDisplay?.toLowerCase() <=> b.authorityDisplay?.toLowerCase() }}" status="i">
                <div class="form-group m-0 m-l-10" style="padding-bottom: 8px">
                    <div class="col-md-${column1Width}">
                        <g:message code="app.role.${role.authority}" default="${role.authority}"/>
                    </div>

                    <div class="col-md-7">
                        <g:checkBox name="${role.authority}" value="${userInstance?.roles?.contains(role.authority)}"/>
                    </div>
                </div>
            </g:each>
        </g:if>

        %{--Edit Mode--}%
        <g:if test="${actionName == 'edit' || actionName == 'update'}" >
            <g:each var="entry" in="${roleMap.findAll { !("ROLE_DEV".equalsIgnoreCase(it.key.authority)) }}">

                <div class="form-group m-0 m-l-10" style="padding-bottom: 8px">
                    <div class="col-md-${column1Width}">
                        <g:message code="app.role.${entry.key.authority}" default="${entry.key.authority}"/>
                    </div>

                    <div class="col-md-7">
                        <g:checkBox name="${entry.key.authority}" value="${entry.value}"/>
                    </div>
                </div>
            </g:each>
        </g:if>

    </div>



    %{--Preferences--}%
    <div class="col-md-6">

        <h3 class="sectionHeader"><g:message code="app.label.preference"/></h3>

        <div class="form-group m-0 m-l-10">
            <label class="col-md-${column1Width} control-label" for="email"><g:message code="app.label.language"/><span class="required-indicator">*</span> </label>

            <div class="col-md-${column2Width}">
                <rx:localeSelect id="locale"
                          name="preference.locale"
                          value="${userInstance.preference.locale}"
                          noSelection="${['': message(code:'select.one')]}"
                          class="form-control"/>
            </div>
        </div>

        <div class="form-group m-0 m-l-10">
            <label class="col-md-${column1Width} control-label" for="email"><g:message code="app.label.timezone"/></label>

            <div class="col-md-${column2Width}">
                <g:select id="timeZone"
                          name="timeZone"
                          from="${com.rxlogix.util.ViewHelper.getTimezoneValues()}"
                          optionKey="name"
                          optionValue="display"
                          class="form-control"
                          value="${userInstance.preference.timeZone}"/>
            </div>
        </div>

    <g:if test="${createMode}">
%{--        Only adding when new User is added--}%
        <input type="hidden" name="groups" value="${Group.findByName('All Users').id}">
    </g:if>


    </div>
</div>
