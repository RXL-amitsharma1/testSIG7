<%@ page import="com.rxlogix.enums.GroupType; grails.plugin.springsecurity.SpringSecurityUtils" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName"
           value="${type == "workflow" ? message( code: 'user.workflow.group.label' ) : message( code: 'user.user.group.label' )}"/>
    <title><g:message code="default.show.label" args="[entityName]"/></title>
</head>

<body>

<rx:container title="${type == "user" ? message( code: "app.label.groupManagement.user" ) : message( code: "app.label.groupManagement.workflow" )}">
    <div class="row">
        <g:render template="/includes/layout/flashErrorsDivs" bean="${groupInstance}" var="theInstance"/>
    </div>
    <br/>

    <div class="row">
        <g:link action="index" class="btn btn-primary">Group List</g:link>
        <g:if test="${SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN")}">
            <g:render template="/includes/widgets/buttonBarCRUD" bean="${groupInstance}" var="theInstance"
                      model="[showDeleteButton: false , showAddButton:false , showEditButton :false]"/>
        </g:if>
    </div>
    <br/>

    <div class="row">
        <div class="col-md-12">

            <div class="col-md-12">
                <div class="row">
                    <div class="col-md-4"><label><g:message code="group.name.label"/></label></div>

                    <div class="col-md-8 word-break"><g:message code="app.role.${groupInstance.name}"
                                                     default="${groupInstance.name}"/></div>
                </div>

                <div class="row">
                    <div class="col-md-4"><label><g:message code="group.description.label"/></label></div>

                    <div class="col-md-8 word-break">${groupInstance.description}</div>
                </div>

               <g:if test="${groupInstance.groupType == GroupType.WORKFLOW_GROUP}">
                    <div class="row">
                        <div class="col-md-4"><label><g:message code="group.force.justification"/></label></div>

                        <div class="col-md-8">${groupInstance.forceJustification ? 'Yes' : 'No'}</div>
                    </div>

                    <div class="row">
                        <div class="col-md-4"><label><g:message
                                code="com.rxlogix.group.default.adhoc.disposition.type.label"/></label></div>

                        <div class="col-md-8">${groupInstance.defaultAdhocDisposition?.displayName}</div>
                    </div>

                    <div class="row">
                        <div class="col-md-4"><label><g:message
                                code="com.rxlogix.group.default.evdas.disposition.type.label"/></label></div>

                        <div class="col-md-8">${groupInstance.defaultEvdasDisposition?.displayName}</div>
                    </div>

                    <div class="row">
                        <div class="col-md-4"><label><g:message
                                code="com.rxlogix.group.default.lit.disposition.type.label"/></label></div>

                        <div class="col-md-8">${groupInstance.defaultLitDisposition?.displayName}</div>
                    </div>

                    <div class="row">
                        <div class="col-md-4"><label><g:message
                                code="com.rxlogix.group.default.quant.disposition.type.label"/></label></div>

                        <div class="col-md-8">${groupInstance.defaultQuantDisposition?.displayName}</div>
                    </div>

                    <div class="row">
                        <div class="col-md-4"><label><g:message
                                code="com.rxlogix.group.default.quali.disposition.type.label"/></label></div>

                        <div class="col-md-8">${groupInstance.defaultQualiDisposition?.displayName}</div>
                    </div>

                    <div class="row">
                        <div class="col-md-4"><label><g:message
                                code="com.rxlogix.group.signal.disposition.label"/></label></div>

                        <div class="col-md-8">${groupInstance.defaultSignalDisposition?.displayName}</div>
                    </div>

                    <div class="row">
                        <div class="col-md-4"><label><g:message
                                code="com.rxlogix.group.auto.route.disposition.label"/></label></div>

                        <div class="col-md-8">${groupInstance.autoRouteDisposition?.displayName}</div>
                    </div>

                    <div class="row">
                        <div class="col-md-4"><label><g:message code="com.rxlogix.group.justification.label"/></label>
                        </div>

                        <div class="col-md-8">${groupInstance.justificationText}</div>
                    </div>

                    <div class="row">
                        <div class="col-md-4"><label><g:message
                                code="com.rxlogix.group.alert.level.disposition.label"/></label></div>

                        <div class="col-md-8">${groupInstance.alertDispositions ? groupInstance.alertDispositions?.join(", ") : g.message(code: "app.label.none")}</div>
                    </div>
                </g:if>
            </div>

        </div>
    </div>

    <div class="rxmain-container-content">
        <div style="margin-top: 40px"></div>
        <g:if test="${type =="user"}">
            <h3 class="sectionHeader">Roles</h3>
            <div class="row">
                <g:if test="${groupRoleList.size(  ) ==0}">
                    <div class="row">
                        <div class="col-md-6">
                            <div class="col-md-8">
                                (None)
                            </div>
                        </div>
                    </div>
                </g:if>
                <g:else>
                    <g:each var="role" in="${groupRoleList.findAll { !("ROLE_DEV".equalsIgnoreCase(it)) }}" status="i">
                        <div class="row">
                            <div class="col-md-6">
                                <div class="col-md-8">
                                    <g:message code="app.role.${role}" default="${role}"/>
                                </div>
                            </div>
                        </div>
                    </g:each>
                </g:else>
            </div>
        </g:if>
        <h3 class="sectionHeader">Users</h3>
        <div class="row">
            <g:if test="${groupUsersList.size(  ) ==0}">
                <div class="row">
                    <div class="col-md-6">
                        <div class="col-md-8">
                            (None)
                        </div>
                    </div>
                </div>
            </g:if>
            <g:else>
                <g:each var="users" in="${groupUsersList}" status="i">
                    <div class="row">
                        <div class="col-md-6">
                            <div class="col-md-8">
                                ${i+1}.  ${users}
                            </div>
                        </div>
                    </div>
                </g:each>
            </g:else>
        </div>
    </div>
</rx:container>




</body>
</html>
