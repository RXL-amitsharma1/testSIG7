<%@ page import="com.rxlogix.util.DateUtil; com.rxlogix.signal.AttachmentDescription" %>
<div class="attachments">
    <g:if test="${source == 'detail'}">
        <g:set var="actionString" value="alertDetail"/>
        <g:set var="instanceId" value="${alertInst?.id}"/>
    </g:if>

    <g:if test="${source == 'edit'}">
        <g:set var="actionString" value="edit"/>
        <g:set var="instanceId" value="${alertInst?.id}"/>
    </g:if>

    <g:if test="${source == 'detail' || source == 'edit'}">
        %{--<i class="thumbnail fa fa-plus" data-toggle="modal" data-target="#myModal"></i>--}%
        <a class="btn btn-primary ${buttonClass}" href="#myModal" data-toggle="modal">Add File</a>
    </g:if>

    <table id="attachment-table" class="row-border hover dataTable no-footer" style="width: 100%">
        <thead>
        <tr>
            %{--<g:sortableColumn property="id" title="Id" titleKey="action.id"/>--}%
            <th>${message(code: "caseDetails.link")}</th>
            <th>${message(code: "app.label.description")}</th>
            <th>${message(code: "app.label.timestamp")}</th>
            <th>${message(code: "caseDetails.modified.by")}</th>
            <th class="${buttonClass}">${message(code: "app.label.action")}</th>

        </tr>
        </thead>
        <tbody>
        <attachments:each bean="${alertInst}" status="i">
            <tr role="row" class="${i / 2 == 0 ? 'odd' : 'even'}">
                <td>
                    <i class=' attachment-file <prettyAttachments:icon attachment="${attachment}"/>'></i>
                    <attachments:downloadLink
                            attachment="${attachment}"/>
                    ${attachment.niceLength}
                </td>
                <td>${com.rxlogix.signal.AttachmentDescription.findByAttachment(attachment)?.description}</td>
                <td>
                    <div>
                        <g:if test="${com.rxlogix.signal.AttachmentDescription.findByAttachment(attachment)?.dateCreated != null}">
                            <g:defaultPvSignalDateFormat
                                    date="${com.rxlogix.signal.AttachmentDescription.findByAttachment(attachment)?.dateCreated}"/>
                        </g:if>
                    </div>
                </td>
                <td>${com.rxlogix.signal.AttachmentDescription.findByAttachment(attachment)?.createdBy}</td>
                <td class="${buttonClass}">
                    <g:if test="${source == 'detail' || source == 'edit'}">
                        <span class="attachment" id="deleteAttachment">
                            <attachments:deleteLink
                                    attachment="${attachment}"
                                    label="${'[X]'}"
                                    alertId = "${instanceId}"
                                    returnPageURI="${createLink(action: actionString, id: instanceId)}"/>
                        </span>
                    </g:if>
                </td>
            </tr>
        </attachments:each>
        </tbody>
    </table>
</div>