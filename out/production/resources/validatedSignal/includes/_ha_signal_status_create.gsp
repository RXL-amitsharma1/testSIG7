<%@ page import="com.rxlogix.util.DateUtil"%>
<div class="row">
    <div class="col-xs-3">
        <div class="form-group">
            <label><g:message code="app.label.ha.signal.status"/></label>
            <g:select name="haSignalStatus"
                      from="${haSignalStatusList}"
                      optionKey="id"
                      optionValue="value"
                      value="${validatedSignal?.haSignalStatus?.id}"
                      noSelection="${['': message(code: 'select.one')]}"
                      class="form-control"/>
        </div>

        <div class="form-group">
            <label><g:message code="app.label.ha.date.closed"/></label>

            <div class="fuelux">
                <div class="datepicker toolbarInline" id="haDateClosedDatePicker">
                    <div class="input-group">
                        <input placeholder="Select Date"
                               class="form-control"
                               name="haDateClosed" type="text"
                               id="haDateClosed" value="${validatedSignal?.haDateClosed?(DateUtil.toDateStringWithoutTimezone(validatedSignal.haDateClosed)):""}"/>
                        <g:render template="/includes/widgets/datePickerTemplate"/>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="col-xs-9 ">
        <div class="form-group textarea-ext"><label><g:message code="app.label.ha.signal.status.comments"/></label>
        <g:textArea name="commentSignalStatus"
                    class="form-control">${validatedSignal?.commentSignalStatus}</g:textArea>
        <a class="btn-text-ext openTextArea" href="" tabindex="0" title="Open in extended form"><i class="mdi mdi-arrow-expand font-20 blue-1"></i></a>

        </div>
    </div>
</div>

