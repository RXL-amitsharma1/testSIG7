
<%@ page import="com.rxlogix.config.ActionType" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'actionType.label', default: 'ActionType')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<a href="#list-actionType" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home btn btn-primary" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
				<li><g:link class="create btn btn-primary" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
			</ul>
		</div>
		<div id="list-actionType" class="content scaffold-list" role="main">
			<h1><g:message code="default.list.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
				<div class="message" role="status">${flash.message}</div>
			</g:if>
			<table>
			<thead>
					<tr>
					
						<g:sortableColumn property="description" title="${message(code: 'actionType.description.label', default: 'Description')}" />
					
						<g:sortableColumn property="description_local" title="${message(code: 'actionType.description_local.label', default: 'Descriptionlocal')}" />
					
						<g:sortableColumn property="displayName" title="${message(code: 'actionType.displayName.label', default: 'Display Name')}" />
					
						<g:sortableColumn property="displayName_local" title="${message(code: 'actionType.displayName_local.label', default: 'Display Namelocal')}" />
					
						<g:sortableColumn property="value" title="${message(code: 'actionType.value.label', default: 'Value')}" />
					
					</tr>
				</thead>
				<tbody>
				<g:each in="${actionTypeInstanceList}" status="i" var="actionTypeInstance">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
					
						<td><g:link action="show" id="${actionTypeInstance.id}">${fieldValue(bean: actionTypeInstance, field: "description")}</g:link></td>
					
						<td>${fieldValue(bean: actionTypeInstance, field: "description_local")}</td>
					
						<td>${fieldValue(bean: actionTypeInstance, field: "displayName")}</td>
					
						<td>${fieldValue(bean: actionTypeInstance, field: "displayName_local")}</td>
					
						<td>${fieldValue(bean: actionTypeInstance, field: "value")}</td>
					
					</tr>
				</g:each>
				</tbody>
			</table>
			<div class="pagination">
				<g:paginate total="${actionTypeInstanceCount ?: 0}" />
			</div>
		</div>
	</body>
</html>
