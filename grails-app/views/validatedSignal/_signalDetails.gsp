<%@ page import="com.rxlogix.util.DateUtil; grails.util.Holders" %>
<div class="row">
    <g:if test="${Holders.config.validatedSignal.show.detected.by}">
        <div class="col-md-3">
            <div class="form-group">
                <label><g:message code="app.label.detected.by"/></label>
                <g:select id="detectedBy" name="detectedBy"
                          from="${detectedBy}"
                          optionValue="value"
                          value="${validatedSignal?.detectedBy}"
                          noSelection="${['': message(code: 'select.one')]}"
                          class="form-control"/>
            </div>
        </div>
    </g:if>

    <g:if test="${Holders.config.validatedSignal.show.topic.information}">
        <div class="col-md-3">
            <div class="form-group">
                <label class=""><g:message code="app.label.topicInformation"/></label>
                <input class="form-control" value="${validatedSignal?.topic}" name="topic" id="topic"/>
            </div>
        </div>
    </g:if>

    <g:if test="${Holders.config.validatedSignal.aggregateDate.enabled}">
        <div class="col-md-3">
            <div class="form-group">
                <label><g:message code="app.label.aggregate.report.start.date"/></label>
                <div class="fuelux">
                    <div class="datepicker toolbarInline" id="aggStartDatePicker">
                        <div class="input-group">
                            <input id="aggStartDate" placeholder="Select Date"
                                   class="form-control input-sm startDate"
                                   name="aggReportStartDate" type="text" data-date=""
                                   value="${validatedSignal?.aggReportStartDate ? (DateUtil.toDateStringWithoutTimezone(validatedSignal.aggReportStartDate)) : ""}"/>
                            <g:render id="myAggStartDate"
                                      template="/includes/widgets/datePickerTemplate"/>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="col-md-3">
            <div class="form-group">
                <label><g:message code="app.label.aggregate.report.end.date"/></label>
                <div class="fuelux">
                    <div class="datepicker toolbarInline" id="aggEndDatePicker">
                        <div class="input-group">
                            <input id="aggEndDate"
                                   placeholder="Select Date"
                                   class="form-control"
                                   name="aggReportEndDate" type="text"
                                   value="${validatedSignal?.aggReportEndDate ? (DateUtil.toDateStringWithoutTimezone(validatedSignal.aggReportEndDate)) : ""}"/>
                            <g:render id="myAggEndDate"
                                      template="/includes/widgets/datePickerTemplate"/>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </g:if>
</div>

<g:if test="${Holders.config.validatedSignal.shareWith.enabled}">
    <div class="row">
        <div class="col-md-3">
            <div class="form-group">
                <g:initializeShareWithElement bean="${validatedSignal}"/>
            </div>
        </div>
    </div>
</g:if>