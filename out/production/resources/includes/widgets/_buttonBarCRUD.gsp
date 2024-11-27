<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils" %>
<div class="buttonBarTop pull-right">

    <g:set var="controller" value="${controller ?: controllerName}"/>

    <g:form controller="${controller}" method="delete">

    %{--Add button--}%
        <g:if test="${showAddButton != false && SpringSecurityUtils.ifAnyGranted("ROLE_CONFIGURATION_CRUD")}">
            <g:link class="btn btn-primary" controller="${controller}" action="create">
                <span class="glyphicon glyphicon-plus icon-white"></span>
                <g:message code="default.new.label" args="[entityName]"/>
            </g:link>
        </g:if>

    %{--Edit button--}%
        <g:if test="${showEditButton != false && SpringSecurityUtils.ifAnyGranted("ROLE_CONFIGURATION_CRUD")}">
            <g:hiddenField name="id" value="${theInstance?.id}"/>
            <g:link class="btn btn-default" controller="${controller}" action="edit" id="${theInstance?.id}">
                <span class="glyphicon glyphicon-pencil icon-white"></span>
                <g:message code="default.button.edit.label"/>
            </g:link>
        </g:if>

    %{--Delete button--}%
        <g:if test="${showDeleteButton != false && SpringSecurityUtils.ifAnyGranted("ROLE_CONFIGURATION_CRUD")}">
            <button type="button" class="btn pv-btn-dark-grey" data-toggle="modal" data-target="#deleteModal" data-instancetype="${controller}" data-instanceid="${theInstance?.id}" data-instancename="${whatIsBeingDeleted}">
                <span class="glyphicon glyphicon-trash icon-white"></span>
                ${message(code: 'default.button.delete.label')}
            </button>
            <g:render template="/includes/widgets/deleteRecord"/>
        </g:if>

    </g:form>
</div>