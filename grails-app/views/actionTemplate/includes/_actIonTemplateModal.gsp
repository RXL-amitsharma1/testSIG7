<%@ page import="com.rxlogix.enums.ActionStatus; com.rxlogix.user.User; com.rxlogix.config.ActionType; com.rxlogix.config.ActionConfiguration;" %>

<div class="modal fade action-template-modal" data-backdrop="static" role="dialog" aria-labelledby="action-template-modal-label">
    <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <label class="rxmain-container-header-label click">Action Template</label>
            </div>

            <div class="modal-body">
                <div class="msgContainer" style="display:none">
                    <div class="alert alert-danger" role="alert">
                        <span class="message"></span>
                    </div>
                </div>
                <div class="panel-group m-b-10">
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            <h4 class="panel-title text-no-uppercase">Action Template Details</h4>
                        </div>
                        <div class="panel-body actionType">
                            <div class="row">
                                <div class="col-lg-6">
                                    <label for="name">
                                        Template Name
                                        <span class="required-indicator">*</span>
                                    </label>
                                    <input type="text" id="name" value="" class="form-control" maxlength="255"/>
                                </div>
                                <div class="col-lg-6">
                                    <label for="name">
                                        Template Description
                                    </label>
                                    <input type="text" id="description" name="description" class="form-control" maxlength="255"/>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="panel-group m-b-10">
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            <h4 class="panel-title text-no-uppercase">Action Template Properties</h4>
                        </div>
                        <div class="panel-body actionType">
                            <div class="row">

                                <div class="col-lg-3 action-type-list">
                                    <label for="type.id">
                                        <g:message code="app.label.action.types" default="Type"/>
                                        <span class="required-indicator">*</span>
                                    </label>
                                    <g:select class="form-control " name="type.id" id="type"
                                              from="${ActionType.list()}" optionKey="id"
                                              value="" noSelection="['null': '']"/>
                                </div>

                                <div class="col-lg-3">
                                    <label for="config.id">
                                        <g:message code="app.label.action" default="Action"/>
                                        <span class="required-indicator">*</span>
                                    </label>
                                    <g:select class="form-control action-value select2" name="config.id" id="config"
                                              from="${ActionConfiguration.list().sort({ it.value.toUpperCase()})}" optionKey="id"
                                              value="" noSelection="['null': 'Select Action']"/>
                                </div>

                                <div class="col-lg-3">
                                    <label for="assignedTo">
                                        <g:message code="app.label.assigned.to" default="Assigned To" />
                                        <span class="required-indicator">*</span>
                                    </label>
                                    <g:select class="form-control" name="assignedTo" id="assignedTo"
                                              from="${User.findAllByEnabled(true)?.sort{it.fullName?.toLowerCase()}}"
                                              optionKey="id" optionValue="fullName" value="" />
                                </div>

                                <div class="col-lg-3">
                                    <label>Due In(Days)</label>
                                    <span class="required-indicator">*</span>
                                    <input type="number" id="dueIn" name="dueIn" value="" class="form-control"/>
                                </div>

                            </div>
                            <div class="row">
                                <div class="col-lg-6 form-group">
                                    <label for="details">
                                        <g:message code="action.details.label" default="Action Details" />
                                        <span class="required-indicator">*</span>
                                    </label>
                                    <g:textArea class="form-control tarea-200" name="details" id="details" value="" />
                                </div>

                                <div class="col-lg-6 form-group">
                                    <label for="comments">
                                        <g:message code="app.label.comments" default="Comments" />
                                    </label>
                                    <g:textArea class="form-control tarea-200" name="comments" id="comments" value="" />
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

            </div>

            <div class="modal-footer">
                <div class="buttons ">
                    <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
                    <button type="button" id="save-action-template" class="btn btn-primary id-element">Save</button>
                </div>
            </div>
        </div>
    </div>
</div>