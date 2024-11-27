<%@ page import="com.rxlogix.user.User" %>
<div class="modal fade" id="add-alert-to-topic" role="dialog" aria-hidden="true">
    <div class="modal-dialog modal-md">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <label class="modal-title" id="">Add to Topic</label>
            </div>
            <div class="modal-body">
                <div class="row">
                    <div class="col-md-4">
                        <label>
                            <g:message code="app.topic.name"/>
                        </label>
                        <span class="required-indicator">*</span>
                        <g:render template="/includes/widgets/topic_select"/>
                    </div>

                    <div class="col-md-offset-1 col-md-3">
                        <label>
                            Product
                        </label>
                        <span class="product-span" style="display:block"></span>
                        <input type="hidden" class="product-json-container"/>
                    </div>

                    <div class="col-md-4">
                        <label>
                            <g:message code="app.label.medical.concepts" default="Medical Concepts" />
                            <span class="required-indicator">*</span></label>
                        <g:select name="medicalConcepts" id="medicalConcepts"
                                  from="${com.rxlogix.config.MedicalConcepts.list()}" multiple="true"
                                  optionKey="id" optionValue="name"
                                  value="" class="form-control select2 select2-active clear-field" />
                    </div>
                </div>

                <div class="row m-t-10">

                    <div class="col-md-6">
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
                    <div class="col-md-6">
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


                </div>

            <div class="modal-footer">
                <div class="buttons ">

                    <button class="btn btn-primary attach-topic-to-alert" id="update-bt">Add to Topic</button>
                    <button class=" btn btn-default" data-dismiss="modal" id="cancel-bt">Cancel</button>
                </div>
            </div>
        </div>
    </div>
</div>
