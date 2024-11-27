<%@ page import="com.rxlogix.Constants; com.rxlogix.util.DateUtil; com.rxlogix.config.Tag;com.rxlogix.enums.ReportFormat" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main">

    <title>${message(code: "app.label.substance.frequency.editTitle")}</title>

    <asset:javascript src="fuelux/fuelux.js"/>
    <asset:stylesheet src="fuelux.css" />
    <asset:javascript src="app/pvs/bootbox.min.js"/>
    <g:javascript>
    $(document).ready(function(){
    addCountBoxToInputField(255, $('input#frequency'));
    addCountBoxToInputField(255, $('input#product-name'));
    });
    var confirmationMessage = "${message(code: "app.substance.frequency.update.warning")}";
    var alertTypeInstance = "${instance.alertType}";
    </g:javascript>
    <asset:javascript src="app/pvs/substanceFrequency.js"/>

</head>
<body>
<g:if test="${flash.error}">
    <div class="alert alert-danger alert-dismissible" role="alert" style="word-break: break-all">
        <button type="button" class="close" data-dismiss="alert">
            <span  onclick="this.parentNode.parentNode.remove(); return false;">x</span>
            <span class="sr-only"><g:message code="default.button.close.label" /></span>
        </button>
        <g:if test="${flash.error}">
            <g:if test="${flash.error.contains('<linkQuery>')}">
                ${flash.error.substring(0, flash.error.indexOf('<linkQuery>'))}
                <a href="${flash.error.substring(flash.error.indexOf('<linkQuery>') + 11)}"><g:message code="see.details" /></a>
            </g:if>
            <g:else>
                ${raw(flash.error)}
            </g:else>
        </g:if>
    </div>
</g:if><rx:container title="${message(code: "app.label.substance.frequency.editTitle")}" >
    <g:form method='post' url="[controller:'SubstanceFrequency',action:'update']" id="${instance.id}">
        <div class="form-group">
            <label for= "frequency">Frequency Label</label>
            <input class="form-control" placeholder = "Frequency" maxlength="255" name="frequencyName" type = "text" value="${instance.frequencyName}" id="frequency">
        </div>
        <div class="form-group">
            <label for= "product-name">Product Name</label>
            <input class="form-control" placeholder = "Product Name" maxlength="255" name="name" type = "text" value="${instance.name}" id="product-name">
        </div>

        <div class="form-group">
            <div class="col-md-6">
                <div class="fuelux">
                    <div class="datepicker" id="start-date-picker">
                        <label>Start Date<span class="required-indicator">*</span></label>
                        <div class="input-group">
                            <input placeholder="Start Date" name="startDate" class="form-control input-sm startDate" id="startDate_freq"
                                   type="text" data-date="" value="${instance.startDate}"/>
                            <g:render template="/includes/widgets/datePickerTemplate"/>
                        </div>
                    </div>
                </div>
            </div>

            <div class="form-group">
                <div class="col-md-6">
                    <div class="fuelux">
                        <div class="datepicker" id="end-date-picker">
                            <label>End Date<span class="required-indicator">*</span></label>
                            <div class="input-group">
                                <input placeholder="End Date"  name="endDate" class="form-control input-sm endDate" id="endDate_freq" type="text"
                                       data-date="" value="${instance.endDate}" />
                                <g:render template="/includes/widgets/datePickerTemplate"/>
                            </div>
                        </div>
                    </div>
                </div>
            </div>


        </div>
        <br> </br>


        <div class="form-group">
            <div class="col-md-6">
                <label for="u-frequency">Upload Frequency</label>
                <g:select class="form-control" id="u-frequency" value="${instance.uploadFrequency}" name="uploadFrequency"
                          from="${frequencyList}"/>
            </div>
        </div>

        <div class="form-group">
            <div class="col-md-6">
                <label for="m-frequency">Mining Frequency</label>
                <g:select class="form-control" id="m-frequency" value="${instance.miningFrequency}" name="miningFrequency"
                          from="${frequencyList}"/>
            </div>

            <div class="form-group">
                <label for="alert-type">Alert Type</label>
                <g:select class="form-control" id="alert-type" value="${instance.alertType}" name="alertType"
                          from="${alertTypeList}"/>
            </div>
        </div>
        <div class="modal-footer">
            <div class="buttons ">
                <g:hiddenField name="id" value="${instance.id}" />
                <g:submitButton name="btnSubmit" value="Save" class="btn btn-primary"></g:submitButton>
            </div>
        </div>
    </g:form>
</rx:container>
</body>
</html>