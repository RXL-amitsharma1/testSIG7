<%@ page import="com.rxlogix.config.SignalStrategy; com.rxlogix.mapping.MedDraPT; com.rxlogix.config.Priority; com.rxlogix.user.User" %>

<div class="modal fade" id="topic-modal" tabindex="-1" role="dialog" aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title" id="">Create Topic</h4>
            </div>

            <div class="modal-body" >

                <div class="row ">

                    <div class="col-md-4 form-group">
                        <label class="">Products</label>
                        <input class="form-control" name="productNames" id="productNames" disabled="disabled"/>
                    </div>

                    <div class="col-md-4 form-group">
                        <label class="">Preferred Term</label>
                        <g:select class="form-control" name="prefferedTerm" id="pt"
                                  from="${['Rash', 'Fever', 'Nausea']}" value="" noSelection="['null': '']"
                        />
                    </div>

                </div>

                <div class="row ">

                    <div class="col-md-4 form-group">
                        <label class="">Topic Name<span class="required-indicator">*</span></label>
                        <input class="form-control" name="topicName" id="topicName"/>
                    </div>

                    <div class="col-md-4 form-group">
                        <label class="">Priority<span class="required-indicator">*</span></label>
                        <g:select class="form-control" name="priority" id="priority"
                                  from="${Priority.list()}" value="" noSelection="['null': '--Select Priority--']"
                                  optionKey="id" optionValue="displayName"
                        />
                    </div>

                    <div class="col-md-4 form-group">

                    </div>

                </div>

                <div class="row">

                    <div class="col-md-4">
                        <label class="add-margin-top"><g:message code="app.label.assigned.to"/><span class="required-indicator">*</span></label>
                        <g:select name="assignedTo" from="${User.list()}" optionKey="id" id="assignedTo"
                                  optionValue="fullName" noSelection="['null':'--Select Assigned User--']"
                                  value="" class="form-control"/>
                    </div>

                    <div class="col-md-4">

                        <div class="fuelux">
                            <div class="datepicker" id="start-date-picker">
                                <label>Start Date<span class="required-indicator">*</span></label>
                                <div class="input-group">
                                    <input placeholder="Start Date" name="startDate"
                                           class="form-control input-sm startDate" id="startDate" type="text"
                                           data-date=""
                                           value=""/>
                                    <g:render template="/includes/widgets/datePickerTemplate"/>
                                </div>
                            </div>
                        </div>
                        <g:hiddenField name="startDate" value=""/>
                    </div>

                    <div class="col-md-4">

                        <div class="fuelux">
                            <div class="datepicker" id="end-date-picker">
                                <label>End Date<span class="required-indicator">*</span></label>
                                <div class="input-group">
                                    <input placeholder="End Date" name="endDate"
                                           class="form-control input-sm endDate" id="endDate" type="text"
                                           data-date=""
                                           value=""/>
                                    <g:render template="/includes/widgets/datePickerTemplate"/>
                                </div>
                            </div>
                        </div>
                        <g:hiddenField name="endDate" value=""/>
                    </div>

                </div>

                <div class="row ">
                    <div class="col-md-12">
                        <label>Description</label>
                        <g:textArea id="description" class="form-control" name="description" value="" style="height:150px" />
                    </div>

                </div>

            </div>

            <div class="modal-footer">
                <div class="buttons ">
                    <button type="button" class="btn btn-primary" id="topic-create-button">Create</button>
                    <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
                </div>
            </div>
        </div>
    </div>
</div>