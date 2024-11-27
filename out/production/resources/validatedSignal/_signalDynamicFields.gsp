<%@ page import="com.rxlogix.util.DateUtil;grails.util.Holders;" %>

<g:if test="${entry.enabled == true && entry.fieldName == 'UD_Dropdown1'}">
    <div class="form-group">
        <label for="udDropdown1">${entry.label}</label>
        <g:select id="udDropdown1" name="udDropdown1" class="form-control" optionKey="key"
                  optionValue="value"
                  from="${Holders.config.signal.summary.dynamic.dropdown.values.UD_Dropdown1?.sort{it.value?.toUpperCase()}}" value="${validatedSignal.udDropdown1}" multiple="true"/>
        <g:hiddenField name="udDropdown1Value" id="udDropdown1Value"
                       value="${validatedSignal?.udDropdown1}"/>

    </div>
</g:if>

<g:if test="${entry.enabled == true && entry.fieldName == 'UD_Dropdown2'}">
    <div class="form-group">
        <label for="udDropdown2">${entry.label}</label>
        <g:select id="udDropdown2" name="udDropdown2" class="form-control" optionKey="key"
                  optionValue="value"
                  from="${Holders.config.signal.summary.dynamic.dropdown.values.UD_Dropdown2?.sort{it.value?.toUpperCase()}}" value="${validatedSignal.udDropdown2}" multiple="true"/>
        <g:hiddenField name="udDropdown2Value" id="udDropdown2Value"
                       value="${validatedSignal?.udDropdown2}"/>
    </div>
</g:if>

<g:if test="${entry.enabled == true && entry.fieldName == 'UD_Date1'}">
    <div class="form-group">
        <div class="fuelux">
            <div class="datepicker toolbarInline datepickerUD" id="ud-date-picker1">
                <label>${entry.label}
                </label>
                <div class="input-group">
                    <input placeholder="${entry.label}" name="udDate1" id="udDate1"
                           class="form-control input-sm detectedDate"
                           type="text"
                           data-date="" value="${validatedSignal?.udDate1?(DateUtil.toDateStringWithoutTimezone(validatedSignal.udDate1)):""}"/>
                    <g:render template="/includes/widgets/datePickerTemplate"/>
                </div>
            </div>
        </div>
    </div>
</g:if>

<g:if test="${entry.enabled == true && entry.fieldName == 'UD_Date2'}">
    <div class="form-group">
        <div class="fuelux">
            <div class="datepicker toolbarInline datepickerUD" id="ud-date-picker2">
                <label>${entry.label}
                </label>
                <div class="input-group">
                    <input placeholder="${entry.label}" name="udDate2" id="udDate2"
                           class="form-control input-sm detectedDate"
                           type="text"
                           data-date="" value="${validatedSignal?.udDate2?(DateUtil.toDateStringWithoutTimezone(validatedSignal.udDate2)):""}"/>
                    <g:render template="/includes/widgets/datePickerTemplate"/>
                </div>
            </div>
        </div>
    </div>
</g:if>


<g:if test="${entry.enabled == true && entry.fieldName == 'UD_Text1'}">
    <div class="form-group textarea-ext" >
        <label for="udText1">${entry.label}</label>
        <g:if test="${entry.largeText}">
            <g:textArea name="udText1"
                        class="form-control" id="udText1"
                        style="height: 90px;">${renderFormattedComment(comment:udText1)}</g:textArea>
            <a class="btn-text-ext openTextArea" id="udText1Modal"  href="" tabindex="0" title="Open in extended form"><i
                    class="mdi mdi-arrow-expand font-20 blue-1"></i></a>
        </g:if>
        <g:else>
            <input class="form-control" value="${renderFormattedComment(comment:udText1)}" name="udText1" id="udText1"/>
        </g:else>
    </div>
</g:if>

<g:if test="${entry.enabled == true && entry.fieldName == 'UD_Text2'}">
    <div class="form-group textarea-ext" >
        <label for="udText2">${entry.label}</label>
        <g:if test="${entry.largeText}">
            <g:textArea name="udText2"
                        class="form-control" id="udText2"
                        style="height: 90px;">${renderFormattedComment(comment:udText2)}</g:textArea>
            <a class="btn-text-ext openTextArea" id="udText1Modal"  href="" tabindex="0" title="Open in extended form"><i
                    class="mdi mdi-arrow-expand font-20 blue-1"></i></a>
        </g:if>
        <g:else>
            <input class="form-control" value="${renderFormattedComment(comment:udText2)}" name="udText2" id="udText2"/>
        </g:else>
    </div>
</g:if>
