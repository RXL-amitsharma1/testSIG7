<%@ page import="com.rxlogix.config.SignalStrategy; com.rxlogix.config.Priority; com.rxlogix.user.User;" %>

<script>
    $(document).ready(function() {
        $('#start-date-picker').datepicker();
        $('#end-date-picker').datepicker();

        $("#topicStrategy").change(function() {
            var currentValue = $(this).val();
            if (currentValue != '') {
                //if the selected signal is not null then we show the product names
                $.ajax({
                    url: searchStrategyProducts+"?id="+currentValue,
                    success: function(data) {
                        $("#productNames").val(data.productNames);
                    }
                })
                var comboLabel = $("#topicStrategy  option:selected").text();
                $("#topicName").val(comboLabel+"_topic");
            } else {
                $("#topicName").val('');
                $("#productNames").val('');
            }
        })
    });
</script>

<div class="row ">

    <div class="col-md-4 form-group">
        <label class="">Products
            <span class="required-indicator">*</span>
        </label>

        <div class="wrapper">
            <div id="showProductSelection" class="showDictionarySelection"></div>

            <div class="iconSearch">
                <i class="fa fa-search" id="searchProducts" data-toggle="modal" data-target="#productModal"></i>
            </div>
            <g:textField name="productSelection" value="${topic?.products}" hidden="hidden"/>
        </div>
    </div>


    <div class="col-md-4 form-group">
        <label class="">Topic Name<span class="required-indicator">*</span></label>
        <input class="form-control" value="${topic.name}" name="name" id="topicName"/>
    </div>

    <div class="col-md-4 form-group">
        <label><g:message code="app.label.assigned.to"/></label>
        <g:select name="assignedTo" from="${User.list()}" optionKey="id" id="assignedTo" optionValue="fullName"
                  noSelection="['null': '--Select Assigned User--']" value="${topic.assignedTo?.id}"
                  class="form-control"/>
    </div>

</div>

<div class="row ">

    <div class="col-md-4 form-group">
        <label class="">Priority</label>
        <g:select class="form-control" name="priority" id="priority" from="${Priority.list()}"
                  value="${topic.priority?.id}" noSelection="['null': '--Select Priority--']" optionKey="id"
                  optionValue="displayName"/>
    </div>

    <div class="col-md-4 form-group">
        <label><g:message code="app.label.initial.datasource"/></label>
        <g:select id="initialDataSource" name="initialDataSource" from="${initialDataSource}"
                  value="${topic.initialDataSource}" noSelection="${['': message(code: 'select.one')]}" class="form-control"/>
    </div>

</div>

<div class="row">

    <div class="col-md-4">
        <div class="fuelux">
            <div class="datepicker" id="start-date-picker">
                <label>Start Date<span class="required-indicator">*</span></label>
                <div class="input-group">
                    <input placeholder="Start Date" name="startDate" class="form-control input-sm startDate" id="startDate"
                           type="text" data-date="" value=""/>
                    <g:render template="/includes/widgets/datePickerTemplate"/>
                </div>
            </div>
        </div>
        <g:hiddenField name="startDate" value="${topic.startDate}"/>
    </div>
    <div class="col-md-4">
        <div class="fuelux">
            <div class="datepicker" id="end-date-picker">
                <label>End Date<span class="required-indicator">*</span></label>
                <div class="input-group">
                    <input placeholder="End Date" name="endDate" class="form-control input-sm endDate" id="endDate" type="text"
                           data-date="" value=""/>
                    <g:render template="/includes/widgets/datePickerTemplate"/>
                </div>
            </div>
        </div>
        <g:hiddenField name="endDate" value="${topic.endDate}"/>
    </div>

</div>

<br/>

