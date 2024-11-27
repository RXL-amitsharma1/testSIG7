<%@ page import="com.rxlogix.Constants" %>
<table id="aggTable" class="table table-striped table-curved table-hover">
    <thead>
    <tr>
        <th>Alert Name</th>
        <th>Description</th>
        <th>Date Created</th>
        <th>Owner</th>
    </tr>
    </thead>
    <tbody>
    <g:if test="${aggData}">
        <g:each var="entry" in="${aggData}">
            <tr>
                <td>${entry.name}</td>
                <td>${entry.description?:'-'}</td>
                <td>${entry.dateCreated}</td>
                <td>${entry.owner}</td>
            </tr>
        </g:each>
    </g:if>
    <g:else>
        <tr>
            <td colspan="7">No data available.</td>
        </tr>
    </g:else>
    </tbody>

</table>
