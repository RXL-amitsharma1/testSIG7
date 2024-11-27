
<%@ page import="com.rxlogix.config.Disposition" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'disposition.label', default: 'Disposition')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<a href="#list-disposition" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
				<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
			</ul>
		</div>
		<div id="list-disposition" class="content scaffold-list" role="main">
			<h1><g:message code="default.list.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
				<div class="message" role="status">${flash.message}</div>
			</g:if>
			<table class="dispositionInstance">
			<thead>
					<tr>
						<g:sortableColumn property="display" title="${message(code: 'disposition.display.label', default: 'Display')}" />
						<g:sortableColumn property="validatedConfirmed" title="${message(code: 'disposition.validatedConfirmed.label', default: 'Validated Confirmed')}" />
						<g:sortableColumn property="value" title="${message(code: 'disposition.value.label', default: 'Value')}" />
					</tr>
				</thead>
				<tbody>
				<g:each in="${dispositionInstanceList}" status="i" var="dispositionInstance">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
						<td><g:link action="show" id="${dispositionInstance.id}">${fieldValue(bean: dispositionInstance, field: "display")}</g:link></td>
						<td><g:formatBoolean boolean="${dispositionInstance.validatedConfirmed}" /></td>
						<td>${fieldValue(bean: dispositionInstance, field: "value")}</td>
					</tr>
				</g:each>
				</tbody>
			</table>
			<div class="pagination">
				<g:paginate total="${dispositionInstanceCount ?: 0}" />
			</div>
		</div>
	</body>
</html>
