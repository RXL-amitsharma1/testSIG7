<%@ page import="grails.util.Holders; com.rxlogix.config.EvaluationReferenceType; com.rxlogix.util.DateUtil; com.rxlogix.util.ViewHelper" %>

<div class="row">
    <div class="col-md-12">
        <div class="row">

            %{--Product Selection/Study/Generic Selection--}%
            <g:render template="/includes/widgets/product_study_generic-selection" bean="${alertInstance}"
                      model="[theInstance: alertInstance, showHideProductGroupVal: 'hide']" />

            %{--Event Selection--}%
            <div class="col-md-4">
                <label><g:message code="app.label.eventSelection"/><span class="required-indicator"/><span class="required-indicator"/></label>

                <div class="wrapper">
                    <div id="showEventSelection" class="showDictionarySelection"></div>

                    <div class="iconSearch">
                        <a id="searchEvents" data-toggle="modal" data-target="#eventModal" tabindex="0" role="button" title="Select Event" accesskey="}"><i class="fa fa-search"></i></a>
                    </div>
                </div>
                <g:textField name="eventSelection" value="${alertInstance?.eventSelection}" hidden="hidden"/>

            </div>

            <!-- Reference. -->
            <div class="col-md-4">

               <div class="form-group">
                   <label for="formulations">Formulation<span class="required-indicator"/><span class="required-indicator"/></label>
                   <g:select id="formulations" name="formulations"
                      from="${formulations}"
                      value="${alertInstance?.formulations}"
                      multiple="true"
                      optionKey="formulation"
                      optionValue="formulation"
                      class="form-control"/>

               </div>
               <div class="form-group">
                   <label for="indication">Indication</label>
                   <g:textField class="form-control" name="indication" value="${alertInstance?.indication}" />
               </div>

            </div>
        </div>

        <div class="row">
            <div class="col-xs-4">
                 <div class="form-group">
                      <label>Report Type<span class="required-indicator"/></label>
                      <g:select name="reportType" id="reportType"
                         from="${lmReportTypes}"
                         optionKey="type"
                         optionValue="type"
                         value="${alertInstance?.reportType}"
                         multiple="true"
                         class="form-control"/>
                 </div>
                 <div class="form-group">
                     <label for="deviceRelated"><g:message code="device.related.label" /><span class="required-indicator"/></label>
                     <div></div>
                     <g:select name="deviceRelated"
                               from="['Yes','No']"
                               value="${alertInstance ? 'No' : alertInstance.deviceRelated}"
                               class="form-control" />
                 </div>
            </div>
            <div class="col-xs-4">
                <div class="form-group ${hasErrors(bean: alertInstance, field: 'topic', 'has-error')}">
                    <label for="topic"><g:message code="app.label.topicInformation"/><span class="required-indicator">*</span></label>
                    <g:textField id="topic" class="form-control" name="topic" value="${alertInstance?.topic}" />
                </div>
                <div class="form-group">
                    <label><g:message code="app.label.countryOfIncidence"/><span class="required-indicator"/></label>
                    <g:select id="countryOfIncidence" name="countryOfIncidence"
                              from="${countryNames}"
                              optionKey="name"
                              optionValue="name"
                              multiple="true"
                              value="${alertInstance.countryOfIncidence}"
                              class="form-control"/>

                </div>
            </div>
            <div class="col-xs-4">

                <div class="form-group">
                    <label>Number of ICSRs<span class="required-indicator"/></label>
                    <g:textField class="form-control" name="numberOfICSRs"
                    value="${alertInstance?.numberOfICSRs}" />
                </div>
                <div class="form-group">
                    <label class=""><g:message code="app.label.ReferenceType"/></label>
                    <g:select name="refType"
                              from="${EvaluationReferenceType.findAllByDisplay(true)}"
                              optionKey="id"
                              optionValue="name"
                              value="${alertInstance?.refType}"
                              noSelection="${['': message(code: 'select.one')]}"
                              class="form-control"/>
                </div>
                <div class="pull-right pull-down">
                    <a class="btn btn-primary" id="matching-alert-btn" tabindex="0" role="button">
                        <span class="glyphicon glyphicon-refresh glyphicon-refresh-animate"></span>
                        <g:message code="app.label.matching.alerts"/>
                    </a>
                </div>
        </div>

    </div>
</div>
<g:if test="${grails.util.Holders.config.pv.plugin.dictionary.enabled}">
    <input type="hidden" id="editable" value="true">
    <g:render template="/plugin/dictionary/dictionaryModals" plugin="pv-dictionary"
              model="[filtersMapList: Holders.config.product.dictionary.filtersMapList, viewsMapList:Holders.config.product.dictionary.viewsMapList]"/>
</g:if>
<g:else>
    <g:render template="/includes/modals/event_selection_modal" />
    <g:render template="/includes/modals/product_selection_modal" />
    <g:render template="/includes/modals/study_selection_modal" />
</g:else>
<g:render template="includes/matching_alerts" />

