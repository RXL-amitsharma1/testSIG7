<%@ page import="com.rxlogix.config.Action" %>
<asset:javascript src="app/pvs/dashboard/actions_list.js" />
<g:set var="actionService" bean="actionService" />
<g:set var="userService" bean="userService" />

<div class="page-header text-muted"><h3> <strong><small>Action List</small></strong> </h3></div>
<div class="container col-lg-4" >
    <ul class="list-group" id="actions-for-me">
        <g:each in="${actionService.listByAssignedTo(userService.getUser())}">
        <li class="list-group-item actions">
            <div class="row">
                <div class="col-sm-2">
                    <i class="fa fa-clock-o fa-2x text-primary"></i>
                </div>
                <div class="col-sm-2"></div>
                <div class="text-muted col-sm-8">
                    <div class="row">
                        <a href="/signal/action/edit?id=${it.id}" >
                            ${it.brief()}
                        </a>
                    </div>
                    <div class="row">
                        <div class="col-sm-6">
                            Due: ${it.displayDueDate}
                        </div>
                        <div class="col-sm-6">
                            Assigned To: ${it.owner?.fullName}
                        </div>
                    </div>
                </div>
            </div>
        </li>
        </g:each>
    </ul>
</div>