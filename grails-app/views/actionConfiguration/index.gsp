
<%@ page import="com.rxlogix.config.ActionConfiguration" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'actionConfiguration.label', default: 'ActionConfiguration')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<a href="#list-actionConfiguration" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
				<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
			</ul>
		</div>
		<div id="list-actionConfiguration" class="content scaffold-list" role="main">
			<h1><g:message code="default.list.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
				<div class="message" role="status">${flash.message}</div>
			</g:if>
			<table>
			<thead>
					<tr>
					
						<g:sortableColumn property="description" title="${message(code: 'actionConfiguration.description.label', default: 'Description')}" />
					
						<g:sortableColumn property="description_local" title="${message(code: 'actionConfiguration.description_local.label', default: 'Descriptionlocal')}" />
					
						<g:sortableColumn property="displayName" title="${message(code: 'actionConfiguration.displayName.label', default: 'Display Name')}" />
					
						<g:sortableColumn property="displayName_local" title="${message(code: 'actionConfiguration.displayName_local.label', default: 'Display Namelocal')}" />
					
						<g:sortableColumn property="value" title="${message(code: 'actionConfiguration.value.label', default: 'Value')}" />
					
					</tr>
				</thead>
				<tbody>
				<g:each in="${actionConfigurationInstanceList}" status="i" var="actionConfigurationInstance">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
					
						<td><g:link action="show" id="${actionConfigurationInstance.id}">${fieldValue(bean: actionConfigurationInstance, field: "description")}</g:link></td>
					
						<td>${fieldValue(bean: actionConfigurationInstance, field: "description_local")}</td>
					
						<td>${fieldValue(bean: actionConfigurationInstance, field: "displayName")}</td>
					
						<td>${fieldValue(bean: actionConfigurationInstance, field: "displayName_local")}</td>
					
						<td>${fieldValue(bean: actionConfigurationInstance, field: "value")}</td>
					
					</tr>
				</g:each>
				</tbody>
			</table>
			<div class="pagination">
				<g:paginate total="${actionConfigurationInstanceCount ?: 0}" />
			</div>
		</div>
	</body>
</html>
