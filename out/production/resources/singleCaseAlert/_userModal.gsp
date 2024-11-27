<%@ page import="com.rxlogix.user.User" %>
<div class="modal fade" id="assignedToModal" tabindex="-1" role="dialog" aria-labelledby="productDictionaryLabel"
     aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="createActionLabel"><g:message code="app.action.user.modal.label"/></h4>
            </div>

            <div class="modal-body">
                <g:if test="${flash.message}">
                    <div class="message"><g:message code="${flash.message}" args="${flash.args}"
                                                    default="${flash.defaultMessage}"/></div>
                </g:if>
                <g:hasErrors bean="${actionInstance}">
                    <div class="errors">
                        <g:renderErrors bean="${actionInstance}" as="list"/>
                    </div>
                </g:hasErrors>
                <g:form name="tempForm" controller="action" action="save" method="post">
                    <g:hiddenField name="change-user-url" value="${createLink(controller: 'singleCaseAlert', action: 'changeAssignedTo')}"/>
                    <g:hiddenField name="alertId" />
                    <div class="row">
                        <div class="col-lg-3">
                            <label for="_assignedTo">
                                <g:message code="action.assignedTo.label" default="Assigned To"/>
                                <span class="required-indicator">*</span>
                            </label>
                            <g:select class="form-control" name="_assignedTo"
                                      from="${User.list().sort{it.username.toLowerCase()}}"
                                      optionKey="id" optionValue="fullName"
                                      value="${actionInstance?.assignedTo?.id}"/>
                        </div>
                    </div>
                </g:form>
            </div>

            <div class="modal-footer">
                <div class="buttons ">
                    <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
                    <a href="javascript:void(0);" id="updateAssignedToBtn" class="btn btn-primary">Update</a>
                </div>
            </div>
        </div>
    </div>
</div>