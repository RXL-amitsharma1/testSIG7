<%@ page import="com.rxlogix.util.DateUtil; com.rxlogix.signal.AttachmentDescription; com.rxlogix.enums.AttachmentType" %>
<div class="container-fluid attachments">
    <g:if test="${source == 'detail'}">
        <g:set var="actionString" value="alertDetail"/>
        <g:set var="instanceId" value="${alertInst?.id}"/>
    </g:if>

    <g:if test="${source == 'edit'}">
        <g:set var="actionString" value="edit"/>
        <g:set var="instanceId" value="${alertInst?.id}"/>
    </g:if>

    <g:if test="${source == 'detail' || source == 'edit'}">
        <a class="btn btn-primary" href="#myModal" data-toggle="modal"><g:message code="signal.workflow.add.file"/></a>
        <a class="btn btn-primary" href="#myReference" data-toggle="modal"><g:message
                code="signal.workflow.add.reference"/></a>
    </g:if>


    <table id="attachment-table" class="dataTable row-border hover no-footer">
        <thead>
        <tr>
            <th class="sorting_disabled"><g:message code="signal.workflow.link"/></th>
            <th class="sorting_disabled"><g:message code="signal.workflow.type"/></th>
            <th class="sorting_disabled"><g:message code="signal.workflow.description"/></th>
            <th class="sorting_disabled"><g:message code="signal.workflow.timestamp"/></th>
            <th class="sorting_disabled"><g:message code="signal.workflow.modifiedBy"/></th>
            <th class="sorting_disabled"><g:message code="signal.workflow.action"/></th>
        </tr>
        </thead>
        <tbody>
        </tbody>
    </table>
</div>