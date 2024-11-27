<%@ page import="com.rxlogix.enums.ActionStatus; com.rxlogix.Constants.AlertConfigType; grails.util.Holders" %>
<g:hiddenField name="id" value="${actionInstance?.id}" id="actionId"/>
<g:hiddenField name="version" value="${actionInstance?.version}" />
<g:hiddenField name="alertId" value="${alertId}" class="alertId"/>
<g:hiddenField name="action-meeting-id" value="${meetingId}" class="action-meeting-id"/>
<g:hiddenField name="fetchMeetingTitleUrl" value="${createLink(controller: 'meeting',action: 'fetchMeetingTitles',params: [alertId:alertId,isTopicFlow:isTopicFlow])}"/>
<script>
    $(document).ready(function () {
        $("#createActionModal .select2, #action-edit-modal .select2").select2();
        var $createActionModal = $("#createActionModal");
        var $editActionModal = $("#editActionModal");
        $('.action-value').change(function () {
            var $modal;
            var text = $(this).data('text');
            if(text == 'create'){
                $modal = $createActionModal;
            }else{
                $modal = $editActionModal;
            }
            var selectedActionId = $(this).val();
            var meetingId = $('.action-meeting-id').val();
            var value = $modal.find("#config").find('option:selected').text();
            if(value == 'Meeting'){
                $.ajax({
                    type: "GET",
                    url: $("#fetchMeetingTitleUrl").val(),
                    data:{},
                    dataType: 'json',
                    success: function (data) {
                        var $titleSelect = $modal.find('#meetingElement');
                        $titleSelect.find('option').remove();
                        $.each(data, function( index, value ) {
                            $titleSelect.append("<option value="+value.id+">"+value.title+"</option>");
                        });
                        $titleSelect.val($modal.find("#meetingValue").val());
                    }
                });
            }
            if(meetingId == selectedActionId && meetingId != "") {
                $('.meeting-decider').val('true');
                $('.action-type-list').addClass('hidden');
                $('.meeting-list').removeClass('hidden');
            } else {
                $('.meeting-decider').val('false');
                $('.action-type-list').removeClass('hidden');
                $('.meeting-list').addClass('hidden');
            }
        });
        $('#actionStatus').change(function () {
            if ($(this).val() === 'Closed') {
                var completedDate = moment.utc(new Date()).tz(serverTimeZone).format("DD-MMM-YYYY");
                $('#completion-date-picker').find('#completedDate').val(completedDate);
            }
        });
        $('#due-date-picker').datepicker({
            date: $("#due-date-picker").val() ? $("#due-date-picker").val() : null,
            allowPastDates: true,
            momentConfig: {
                culture: userLocale,
                tz: userTimeZone,
                format: DEFAULT_DATE_DISPLAY_FORMAT
            }
        });

        $('#due-date-picker').datepicker({
            date: $("#due-date-picker").val() ? new Date($("#due-date-picker").val()) : null,
            allowPastDates: true,
            momentConfig: {
                culture: userLocale,
                tz: userTimeZone,
                format: DEFAULT_DATE_DISPLAY_FORMAT
            }
        });

        $('#completion-date-picker').datepicker({
            date: $("#completion-date-picker").val() ? new Date($("#completion-date-picker").val()) : '',
            allowPastDates: true,
            restricted: [{from: TOMORROW , to: Infinity}],
            momentConfig: {
                culture: userLocale,
                tz: userTimeZone,
                format: DEFAULT_DATE_DISPLAY_FORMAT
            }
        }).on('inputParsingFailed.fu.datepicker',function (e) {
            $('#completedDate').val('');
        });

        $('#actionStatus').change(function () {
            if ($(this).val() === 'Closed') {
                var completedDate = moment.utc(new Date()).tz(serverTimeZone).format("DD-MMM-YYYY");
                $('#createActionModal #completion-date-picker').find('#completedDate').val(completedDate);
            }
        });

        $('#completedDate').focusout(function(){
            $(this).val(newSetDefaultDisplayDateFormat( $(this).val()));
            if($(this).val()=='Invalid date'){
                $(this).val('');
            }
        });

        $("#type option:first-child").hide();
        $("#config option:first-child").hide();
        $('#dueDate').focusout(function(){
            $(this).val(newSetDefaultDisplayDateFormat( $(this).val()));
            if($(this).val()=='Invalid date'){
                $(this).val('');
            }
        });
    })
</script>
<g:hiddenField name="meetingValue"/>
<div class="col-lg-12 action">
    <div class="row">
        <div class="col-lg-4 action-type-list">
            <label for="type.id">
                <g:message code="app.label.action.types" default="Type"/>
                <span class="required-indicator">*</span>
            </label>
            <g:select class="form-control select2" name="type.id" id="type"
                      from="${actionTypeList}" optionKey="id" optionValue="text"
                      value="${actionInstance?.type?.id}" noSelection="['': 'Select Action Type']"
                      disabled="${edit}"/>

            <input type="hidden" class="meeting-decider" name="isMeetingAction" value="false"/>
        </div>

        <div class="col-lg-4">
            <label for="config.id">
                <g:message code="app.label.action" default="Action"/>
                <span class="required-indicator">*</span>
            </label>
            <g:select class="form-control action-value select2" name="config.id" id="config"
                      from="${actionConfigList?.sort({it.value.toUpperCase()})}" optionKey="id" optionValue="value" data-text="${text}"
                      noSelection="['': 'Select Action']"
                      disabled="${edit}"/>
        </div>

        <div class="col-lg-4 hidden meeting-list">
            <label for="meeting.id">
                <g:message code="app.label.meeting.title" default="Meeting Title" />
                <span class="required-indicator">*</span>
            </label>
            <g:select class="form-control" name="meeting.id" id="meetingElement"
                    from="${[]}" optionKey="id" optionValue="meetingTitle"
                    value="" noSelection="['': '']"
                    disabled="${edit}"/>
        </div>

        <g:render template="/includes/widgets/assigned_to_Select" model="[isAction:true]"/>

    </div>
    <br>
    <div class="row">
        <div class="col-lg-4">
            <div class="fuelux">
                <div class="datepicker" id="due-date-picker">
                    <label>Due Date<span class="required-indicator">*</span></label>
                    <div class="input-group">
                        <input placeholder="Due Date" name="dueDate" class="form-control input-sm" id="dueDate" type="text" data-date="" value=""/>
                        <g:render template="/includes/widgets/datePickerTemplate"/>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-lg-4">
            <div class="fuelux">
                <div class="datepicker" id="completion-date-picker">
                    <label>Completion Date</label>
                    <div class="input-group">
                        <input placeholder="Select Completion Date" name="completedDate" class="form-control input-sm" id="completedDate" type="text" data-date="" value=""/>
                        <g:render template="/includes/widgets/datePickerTemplate"/>
                    </div>
                </div>
            </div>
        </div>
        <div class="status-icon">
            <div class="col-lg-4 form-group ">
                <label for="actionStatus">
                    <g:message code="action.actionStatus" default="Status" /></label>
                <g:select class="form-control" name="actionStatus"
                          from="${com.rxlogix.enums.ActionStatus?.allValues()}"
                          optionValue="id" value="${actionInstance?.actionStatus?:Holders.config.actionStatus.bydefault}"/>
            </div>
        </div>


    </div>
    <div class="row">
        <div class="col-lg-6 form-group">
            <label for="details">
                <g:message code="action.details.label" default="Action Details" />
                <span class="required-indicator">*</span>
            </label>
            <g:textArea class="form-control tarea-200 actionDetails actionDetailsData" name="details"
                        value="${fieldValue(bean: actionInstance, field: 'details')}" />
        </div>

        <div class="col-lg-6 form-group">
            <label for="comments">
                <g:message code="app.label.comments" default="Comments" />
            </label>
            <g:textArea class="form-control tarea-200" name="comments" id="comments"
                        value="${fieldValue(bean: actionInstance, field: 'comments')}" />
        </div>
    </div>



    <div>
        <g:hiddenField name="backUrl" value="${backUrl}" />
    </div>

    <g:if test="${appType == AlertConfigType.SIGNAL_MANAGEMENT && text != 'create' }">
        <div class="row">
            <div class="col-lg-4 form-group cell-break">
                <label><g:message code="validated.signal.label"/></label><br>
                <a href="/signal/validatedSignal/details?id=${alertId}">${name}</a>
            </div>
            <div class="col-lg-4 form-group cell-break">
                <label><g:message code="app.label.signal.product.name" /></label><br>
                <span id="prodName"></span>
            </div>
            <div class="col-lg-4 form-group cell-break">
                <label><g:message code="app.label.signal.event.name" /></label><br>
                <span id="eventName"></span>
            </div>
        </div>

    </g:if>

    <div id="createdBy" class="row">
    </div>

    <div class="buttons ">
        <g:if test="${edit}">
            <span class="button"><g:actionSubmit class="update" action="update"
                                         value="Update" /></span>
        </g:if>
    </div>
</div>
