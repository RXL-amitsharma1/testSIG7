<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <asset:javascript src="app/pvs/configuration/deliveryOption.js"/>
    <asset:javascript src="app/pvs/dataTablesActionButtons.js"/>
    <asset:javascript src="fuelux/fuelux.js"/>
    <asset:stylesheet src="fuelux.css" />
    <title>Process Types</title>
</head>
<rx:container id="show-process" title="Workflow Types" options="true">
    <g:form controller="${params['controller']}" method="GET">
        <div class="row">
            <div class="form-submit text-right">
                <g:actionSubmit action="createProcess" value="Add" class="btn btn-primary" style="margin-bottom:8px;"/>
            </div>
            <table class="table">
                <thead>
                <tr>
                    <g:sortableColumn property="type" title="Workflow ID" />
                    <th>Description</th>
                    <th>&nbsp;</th>
                </tr>
                </thead>
                <tbody>
                <g:each in="${processClasses}" var="item" status="i">
                    <g:if test="${scripts[item]}">
                        <tr>
                            <td><g:set var="label" value="${gf.translatedValue(['translations': scripts[item].label, 'default': scripts[item].processType])}" scope="page" />${label?.encodeAsHTML()}</td>
                            <td><g:set var="description" value="${gf.translatedValue(['translations': scripts[item].description, 'default': ''])}" scope="page" />${description?.encodeAsHTML()}</td>
                            <td>
                                <div class="btn-group input-group-btn form-submit text-right">
                                    <nobr>
                                        <g:link action="editProcessDef" controller="${params['controller']}" id="${scripts[item].processType}" title="${common['grailsflow.command.edit']}"  class="btn btn-sm btn-default">
                                            <span class="glyphicon glyphicon-edit"></span>&nbsp;
                                            Edit
                                        </g:link>
                                        <g:link action="deleteProcessDef" controller="${params['controller']}" id="${scripts[item].processType}"
                                                onclick="return askConfirmation('${common['grailsflow.question.confirm']}');" title="${common['grailsflow.command.delete']}" class="btn btn-sm btn-default">
                                            <span class="glyphicon glyphicon-remove text-danger"></span>&nbsp;
                                            Delete
                                        </g:link>
                                    </nobr>
                                </div>
                            </td>
                        </tr>
                    </g:if>
                    <g:else>
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                            <td>${item?.encodeAsHTML()}</td>
                            <td><div class="alert-danger">Invalid</div></td>
                            <td>
                                <div class="btn-group input-group-btn">
                                    <nobr>
                                        <g:link action="editProcessScript" controller="${params['controller']}" id="${item}"
                                                title="Edit"  class="btn btn-sm btn-default">
                                            <span class="glyphicon glyphicon-edit"></span>&nbsp;
                                            Edit
                                        </g:link>
                                        <g:link action="deleteProcessScript" controller="${params['controller']}" id="${item}"
                                                onclick="return askConfirmation('Confirm');" title="Delete"  class="btn btn-sm btn-default">
                                            <span class="glyphicon glyphicon-remove text-danger"></span>&nbsp;
                                            Delete
                                        </g:link>
                                    </nobr>
                                </div>
                            </td>
                        </tr>
                    </g:else>
                </g:each>
                </tbody>
            </table>
        </div>
    </g:form>
</rx:container>
</body>
</html>