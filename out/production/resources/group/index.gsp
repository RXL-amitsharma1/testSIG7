<%@ page import="com.rxlogix.util.DateUtil; com.rxlogix.enums.GroupType; com.rxlogix.user.Group" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message( code: 'group.label' )}"/>
    <title><g:message code="groups.label"/></title>

    <style>
    #group-table {
        overflow-x: initial;
        padding: 15px;
    }
    </style>

    <asset:javascript src="yadcf/jquery.datatables.yadcf.js"/>
    <asset:stylesheet src="yadcf/jquery.dataTables.yadcf.css"/>
    <asset:javascript src="app/pvs/group/group_detail.js"/>

</head>

<body>
<g:javascript>
    window.onloadstart = resetSearchBox();
</g:javascript>
<rx:container title="${message( code: 'app.label.groupManagement.list' )}">
    <g:render template="/includes/layout/flashErrorsDivs" bean="${groupInstance}" var="theInstance"/>
    <div class="row">
        <sec:ifAnyGranted roles="ROLE_ADMIN">
            <div class="navScaffold col-xs-12">
                <g:if test="${entityName == 'Group'}">
                    <g:link class="btn btn-primary" action="create" params="[ type: 'workflow' ]">
                        <span class="glyphicon glyphicon-plus icon-white"></span>
                        <g:message code="default.new.label.workflow.group"/>
                    </g:link>
                    <g:link class="btn btn-primary" action="create" params="[ type: 'user' ]">
                        <span class="glyphicon glyphicon-plus icon-white"></span>
                        <g:message code="default.new.label.user.group"/>
                    </g:link>
                </g:if>
                <g:else>
                    <g:link class="btn btn-primary" action="create">
                        <span class="glyphicon glyphicon-plus icon-white"></span>
                        <g:message code="default.new.label" args="[ entityName ]"/>
                    </g:link>
                </g:else>
                <a class="btn btn-primary" href="#copyBulkGroupsModal" data-toggle="modal"><g:message
                        code="default.copy.groups.label"/></a>
            </div>
        </sec:ifAnyGranted>

        <div class="modal fade" id="copyBulkGroupsModal" tabindex="-1" role="dialog" aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <label><g:message code="default.copy.groups.label"/></label>
                    </div>

                    <div class="modal-body">
                        <g:message code="default.copy.groups.modal.body" args="${[ activeGroupsCount ]}"/>
                    </div>

                    <div class="modal-footer">
                        <button type="button" id="addBulkGroups" class="btn btn-primary"><g:message
                                code="default.button.confirm.label"/></button>
                        <button type="button" class="btn btn-default" data-dismiss="modal"><g:message
                                code="default.button.cancel.label"/></button>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div id="group-table" class="table-responsive curvedBox">
        <table id="groupTable" class="table table-striped table-curved table-hover">
            <thead class="sectionsHeader">
            <tr>
                <th><g:message code="group.name.label"/></th>
                <th><g:message code="group.description.label"/></th>
                <th><g:message code="group.type.label"/></th>
                <th><g:message code="group.lastUpdated.label"/></th>
                <th><g:message code="group.dateCreated.label"/></th>
                <th style="width: 100px;"><g:message code="group.createdBy.label"/></th>
                <th><g:message code="group.action.label"/></th>
            </tr>
            </thead>
            <tbody>
            <g:if test="${groupInstanceTotal > 0}">
                <g:set var="userService" bean="userService"/>
                <g:each in="${groupInstanceList}" status="i" var="groupInstance">
                    <tr>
                        <td style="word-break:break-all;word-wrap:break-word;"><g:if test="${groupInstance.groupType == GroupType.USER_GROUP}">
                            <g:link action="show" id="${groupInstance.id}" type="user"
                                    params="[ type: 'user' ]">${groupInstance.name}</g:link>
                        </g:if>
                        <g:if test="${groupInstance.groupType == GroupType.WORKFLOW_GROUP}">
                            <g:link action="show" id="${groupInstance.id}" type="workflow"
                                    params="[ type: 'workflow' ]">${groupInstance.name}</g:link>
                        </g:if>
                        </td>
                        <td style="word-break:break-all;word-wrap:break-word;">${groupInstance.description}</td>
                        <td style="width: 108px;">${groupInstance.groupType.value}</td>
                        <td style="width: 100px;" class="dataTableColumnCenter forceLineWrapDateAlign">${com.rxlogix.util.DateUtil.toDateStringWithTimeInAmPmFormat(groupInstance.lastUpdated,  userService.getUser()?.preference?.timeZone)}</td>
                        <td style="width: 100px;" class="dataTableColumnCenter forceLineWrapDateAlign">${com.rxlogix.util.DateUtil.toDateStringWithTimeInAmPmFormat(groupInstance.dateCreated, userService.getUser()?.preference?.timeZone)}</td>
                        <td>${groupInstance.createdBy}</td>


                        <td class="col-min-75 no-padding"><div class="hidden-btn btn-group dataTableHideCellContent" align="center">
                            <a class="btn btn-success btn-xs" href="/signal/group/show/${groupInstance.id}?type=${groupInstance.groupType == GroupType.USER_GROUP ? 'user' : 'workflow'}"> View</a>
                            <g:if test="${editGroup}">
                                <button type="button" class="btn btn-default btn-xs dropdown-toggle"
                                        data-toggle="dropdown" aria-expanded="true">
                                    <span class="caret"></span>
                                    <span class="sr-only">Toggle Dropdown</span>
                                </button>
                                <ul class="dropdown-menu dropdown-menu-right" role="menu"
                                    style="min-width: 80px !important; font-size: 12px;">
                                    <li role="presentation"><a data-id="${groupInstance.id}" role="menuitem"
                                                               href="/signal/group/edit/${groupInstance.id}?type=${groupInstance.groupType == GroupType.USER_GROUP ? 'user' : 'workflow'}">Edit</a>
                                    </li>
                                </ul>
                            </g:if>


                        </div>
                        </td>
                    </tr>
                </g:each>
            </g:if>
            <g:else>
                <tr>
                    <td colspan="3"><g:message code="app.label.none"/></td>
                </tr>
            </g:else>
            </tbody>
        </table>
    </div>

</rx:container>
</body>
</html>
