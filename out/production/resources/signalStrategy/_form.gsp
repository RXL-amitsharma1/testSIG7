<%@ page import="com.rxlogix.config.MedicalConcepts; grails.plugin.springsecurity.SpringSecurityUtils; com.rxlogix.config.PVConcept; com.rxlogix.util.ViewHelper" %>
<asset:javascript src="app/pvs/alert_utils/alert_product_utils.js"/>
<asset:stylesheet src="dictionaries.css" />
<script>
    $(document).ready(function () {
        var isAdmin = ${SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN")};
        $("#medicalConcepts").select2()
        $("#frequency").select2();
        $('#due-date-picker').datepicker();
        $("#type").select2()
    });
</script>

<div class="row">

    <div class="col-xs-3">
        <label><g:message code="app.label.signal.strategy.reference.number" /><span class="required-indicator">*</span></label>
        <input type="text" name="name" placeholder="${g.message(code: 'input.name.placeholder')}"
                           class="form-control" value="${signalStrategy?.name}"/>
    </div>

    <div class="col-xs-3">
        <label><g:message code="app.label.signal.strategy.type"/></label>
        <g:select id="type" name="type"
                  from="${ViewHelper.getStretegyType()}"
                  value="${signalStrategy?.type}"
                  optionValue="display" optionKey="display"
                  class="form-control"/>
    </div>

    <div class="col-xs-3">
        <label><g:message code="app.label.signal.strategy.medical.concept"/><span class="required-indicator">*</span></label>
        <g:select id="medicalConcepts" name="medicalConcepts"
                  from="${com.rxlogix.config.MedicalConcepts.list()}"
                  value="${signalStrategy?.medicalConcepts?.id}"
                  optionKey="id"
                  optionValue="name"
                  multiple="true"
                  class="form-control"/>
    </div>

    <div class="col-xs-3">

        <div class="fuelux">
            <div class="datepicker" id="due-date-picker">
                <label>Start Date</label>
                <div class="input-group">
                    <input placeholder="Start Date" name="startDate"
                           class="form-control input-sm startDate" id="startDate" type="text"
                           data-date="${signalStrategy?.startDate}"
                           value=""/>
                    <g:render template="/includes/widgets/datePickerTemplate"/>
                </div>
            </div>
        </div>
        <g:hiddenField name="myDetectedDate" value="${signalStrategy?.startDate ?: null}"/>
    </div>

</div>

<div class="row">
   <div class="col-md-6">
        <label><g:message code="app.label.signal.strategy.product" /><span class="required-indicator">*</span></label>
        <div class="wrapper">
            <div id="showProductSelection" class="showDictionarySelection"></div>
            <div class="iconSearch">
                <i class="fa fa-search" class="productRadio" data-toggle="modal" data-target="#productModal"></i>
            </div>
        </div>
        <g:textField class="productSelected" name="productSelection" value="${signalStrategy?.productSelection}" hidden="hidden"/>
   </div>

   <div class="col-md-6">
        <label><g:message code="app.label.description" /></label>
       <span class="required-indicator">*</span>
       <g:textArea name="description" class="form-control" value="${signalStrategy?.description}"
                   style="height: 100px;">${signalStrategy?.description}</g:textArea>

   </div>
</div>

<div class="row form-group m-t-10">
    <div class="col-lg-5">
        <g:if test="${edit}">
            <span class="button"><g:actionSubmit class="save btn btn-primary" action="update" value="${message(code: 'update', 'default': 'Update')}" /></span>
        </g:if>
        <g:else>
            <span class="button"><g:actionSubmit class="save btn btn-primary" action="save" value="${message(code: 'save', 'default': 'Save')}" /></span>
        </g:else>
        <span class="button"><g:actionSubmit class="btn pv-btn-grey" action="index" value="${message(code: 'cancel', 'default': 'Cancel')}" /></span>
    </div>
</div>

<g:render template="/includes/modals/product_selection_modal" />
<input type="hidden" id="selectedDatasource" class="selectedDatasource" value="pva" />
<input type="hidden" name="id" value="${signalStrategy.id}" />


