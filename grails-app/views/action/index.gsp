
<%@ page import="com.rxlogix.config.Action" %>
<!DOCTYPE html>
<html>
<head>
	<meta name="layout" content="main">
	<g:set var="entityName" value="${message(code: 'action.label', default: 'Action')}" />
	<title><g:message code="default.list.label" args="[entityName]" /></title>
</head>
<body>
<a href="#list-action" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
<div class="nav" role="navigation">
	<ul>
		<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
	</ul>
</div>
<div id="list-action" class="content scaffold-list" role="main">
	<h1><g:message code="default.list.label" args="[entityName]" /></h1>
	<g:if test="${flash.message}">
		<div class="message" role="status">${flash.message}</div>
	</g:if>
	<table>
		<thead>
		<tr>

			<g:sortableColumn property="details" title="${message(code: 'action.details.label', default: 'Details')}" />

			<g:sortableColumn property="createdDate" title="${message(code: 'app.label.createdDate', default: 'Created Date')}" />

			<g:sortableColumn property="dueDate" title="${message(code: 'app.label.due.date', default: 'Due Date')}" />

			<g:sortableColumn property="completedDate" title="${message(code: 'app.label.complete.date', default: 'Completed Date')}" />

			<th><g:message code="app.label.assigned.to" default="Assigned To" /></th>

			<th><g:message code="app.label.owner" default="Owner" /></th>

		</tr>
		</thead>
		<tbody>
		<g:each in="${actionInstanceList}" status="i" var="actionInstance">
			<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">

				<td><g:link action="show" id="${actionInstance.id}">${fieldValue(bean: actionInstance, field: "details")}</g:link></td>

				<td><g:formatDate date="${actionInstance.createdDate}" /></td>

				<td><g:formatDate date="${actionInstance.dueDate}" /></td>

				<td><g:formatDate date="${actionInstance.completedDate}" /></td>

				<td>${fieldValue(bean: actionInstance, field: "assignedTo")}</td>

				<td>${fieldValue(bean: actionInstance, field: "owner")}</td>

			</tr>
		</g:each>
		</tbody>
	</table>
	<div class="pagination">
		<g:paginate total="${actionInstanceCount ?: 0}" />
	</div>
</div>
</body>
</html>
