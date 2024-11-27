<div class="container-fluid attachments">
    <g:if test="${source == 'detail'}">
        <g:set var="actionString" value="alertDetail"/>
        <g:set var="instanceId" value="${alertInst?.id}"/>
    </g:if>

    <g:if test="${source == 'edit'}">
        <g:set var="actionString" value="edit"/>
        <g:set var="instanceId" value="${alertInst?.id}"/>
    </g:if>

    %{--<g:if test="${source == 'detail' || source == 'edit'}">--}%
        %{--<i class="thumbnail fa fa-plus fa-2x hide-parent-modal" data-toggle="modal" data-target="#myMeetingModal"></i>--}%
    %{--</g:if>--}%
    %{--<div class="row">--}%
        %{--<div><g:hiddenField name="source" value="${source}"/></div>--}%
    %{--</div>--}%
    <div class="row">
        <div class="col-xs-12">
            <input multiple class="multi" type="file" name="attachments" id="attachments"/>
        </div>
    </div>
    %{--<attachments:each bean="${alertInst}">--}%

        %{--<i class='thumbnail attachment-file <prettyAttachments:icon attachment="${attachment}"/>'></i>--}%
        %{--<attachments:downloadLink--}%
                %{--attachment="${attachment}"/>--}%
        %{--${attachment.niceLength}--}%

        %{--<g:if test="${source == 'detail' || source == 'edit'}">--}%
            %{--<span class="attachment">--}%
                %{--<g:link controller="meeting" action="deleteAttachment"--}%
                        %{--params="[attachmentId: attachment.id, id:instanceId]">[X]</g:link>--}%
            %{--</span>--}%
        %{--</g:if>--}%
    %{--</attachments:each>--}%
    <div class="attachment-body">

    </div>
</div>