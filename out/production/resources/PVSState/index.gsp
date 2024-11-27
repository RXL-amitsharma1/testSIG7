<%@ page import="com.rxlogix.config.WorkflowRule" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="layout" content="main"/>
	<title><g:message code="PVSState.list"/></title>
	<g:javascript>
        var pvsStateListUrl = "${createLink(controller: 'PVSState', action: 'list')}";
        var createUrl = "${createLink(controller: 'PVSState', action: 'create')}";
        var isAdmin = ${grails.plugin.springsecurity.SpringSecurityUtils.ifAnyGranted("ROLE_CONFIGURATION_CRUD")};

	</g:javascript>
	<asset:javascript src="app/pvs/pvsState/pvs_state.js"/>
</head>

<body>
<rx:container title="Workflow State List">
	<g:render template="/includes/layout/flashErrorsDivs" bean="${PVSStateInstanceTotal}" var="theInstance"/>
	<div class="row">
		<div class="col-lg-12">
			<table id="pvsStateTable" class="row-border hover simple-alert-table" width="100%">
				<thead>
				<tr>
					<th><g:message code="PVSState.value"/></th>
					<th><g:message code="PVSState.description"/></th>
					<th><g:message code="PVSState.displayName"/></th>
					<th><g:message code="PVSState.display"/></th>
					<th><g:message code="PVSState.finalState"/></th>
				</tr>
				</thead>
			</table>
		</div>
	</div>
</rx:container>
</body>
</html>
