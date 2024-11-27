<%@ page import="grails.util.Holders; com.rxlogix.config.EvaluationReferenceType; com.rxlogix.util.DateUtil; com.rxlogix.util.ViewHelper" %>


<g:if test="${entry?.enabled == true && (entry?.name in ['productSelection', 'study', 'studySelection'])}">
<g:render template="/includes/widgets/product_study_generic-selection" bean="${alertInstance}"
          model="[theInstance: alertInstance, showHideProductGroupVal: 'hide', alertCriteriaList: alertCriteriaList]" />
    </g:if>

%{--Event Selection--}%
    <g:if test="${entry?.enabled == true && entry?.name == 'eventSelection'}">
        <div class="form-group">
            <label>${entry?.label}<span class="required-indicator">${entry?.mandatory == true ? '*' : ''}</span></label>

            <div class="wrapper">
                <div id="showEventSelection" class="showDictionarySelection"></div>

                <div class="iconSearch">
                    <a id="searchEvents" data-toggle="modal" data-target="#eventModal" tabindex="0" role="button" title="Select Event" accesskey="}"><i class="fa fa-search"></i></a>
                </div>
            </div>
            <g:textField name="eventSelection" value="${alertInstance?.eventSelection}" hidden="hidden"/>

        </div>
    </g:if>
    <g:if test="${entry?.enabled == true && entry?.name == 'formulations'}">
        <!-- Reference. -->
                <div class="form-group">
                    <label for="formulations">${entry?.label}<span class="required-indicator">${entry?.mandatory == true ? '*' : ''}</span></label>
                    <g:if test="${alertAttributesService?.get('formulations')}">
                        <g:select name="formulations" id="formulations"
                                  from="${alertAttributesService?.get('formulations')}"
                                  optionValue="value"
                                  value="${alertInstance?.formulations}"
                                  multiple="true"
                                  class="form-control"/>
                    </g:if>
                    <g:else>
                        <g:select id="formulations" name="formulations"
                                  from="${formulations}"
                                  value="${alertInstance?.formulations}"
                                  multiple="true"
                                  optionKey="formulation"
                                  optionValue="formulation"
                                  class="form-control"/>
                    </g:else>
                </div>
    </g:if>
    <g:if test="${entry?.enabled == true && entry?.name == 'indication'}">
                <div class="form-group">
                    <label for="indication">${entry?.label}<span class="required-indicator">${entry?.mandatory == true ? '*' : ''}</span></label>
                    <g:textField class="form-control" name="indication" value="${alertInstance?.indication}" />
                </div>
    </g:if>
    <g:if test="${entry?.enabled == true && entry?.name == 'reportType'}">
                <div class="form-group">
                    <label>${entry?.label}<span class="required-indicator">${entry?.mandatory == true ? '*' : ''}</span></label>
                    <g:if test="${alertAttributesService?.get('reportType')}">
                        <g:select name="reportType" id="reportType"
                                  from="${alertAttributesService?.get('reportType')}"
                                  optionValue="value"
                                  value="${alertInstance?.reportType}"
                                  multiple="true"
                                  class="form-control"/>
                    </g:if>
                    <g:else>
                        <g:select name="reportType" id="reportType"
                                  from="${lmReportTypes}"
                                  optionKey="type"
                                  optionValue="type"
                                  value="${alertInstance?.reportType}"
                                  multiple="true"
                                  class="form-control"/>
                    </g:else>
                </div>
    </g:if>
    <g:if test="${entry?.enabled == true && entry?.name == 'deviceRelated'}">
                <div class="form-group">
                    <label for="deviceRelated">${entry?.label}<span class="required-indicator">${entry?.mandatory == true ? '*' : ''}</span></label>
                    <div></div>
                        <g:select name="deviceRelated"
                                  from="${alertAttributesService?.get('deviceRelated')}"
                                  value="${alertInstance ? 'No' : alertInstance.deviceRelated}"
                                  class="form-control" />
                </div>
    </g:if>
    <g:if test="${entry?.enabled == true && entry?.name == 'topic'}">
                <div class="form-group ${hasErrors(bean: alertInstance, field: 'topic', 'has-error')}">
                    <label for="topic">${entry?.label}<span class="required-indicator">${entry?.mandatory == true ? '*' : ''}</span></label>
                    <g:textField id="topic" class="form-control" name="topic" value="${alertInstance?.topic}" />
                </div>
    </g:if>
    <g:if test="${entry?.enabled == true && entry?.name == 'countryOfIncidence'}">
                <div class="form-group">
                    <label>${entry?.label}<span class="required-indicator">${entry?.mandatory == true ? '*' : ''}</span></label>
                    <g:if test="${alertAttributesService?.get('countryOfIncidence')}">
                        <g:select name="countryOfIncidence" id="countryOfIncidence"
                                  from="${alertAttributesService?.get('countryOfIncidence')}"
                                  optionValue="value"
                                  value="${alertInstance.countryOfIncidence}"
                                  multiple="true"
                                  class="form-control"/>
                    </g:if>
                    <g:else>
                        <g:select id="countryOfIncidence" name="countryOfIncidence"
                                  from="${countryNames}"
                                  optionKey="name"
                                  optionValue="name"
                                  multiple="true"
                                  value="${alertInstance.countryOfIncidence}"
                                  class="form-control"/>
                    </g:else>

                </div>
    </g:if>

    <g:if test="${entry?.enabled == true && entry?.name == 'numberOfICSRs'}">
                <div class="form-group">
                    <label>${entry?.label}<span class="required-indicator">${entry?.mandatory == true ? '*' : ''}</span></label>
                    <g:textField class="form-control" name="numberOfICSRs"
                                 value="${alertInstance?.numberOfICSRs}" />
                </div>
    </g:if>
    <g:if test="${entry?.enabled == true && entry?.name == 'refType'}">
                <div class="form-group">
                    <label class="">${entry?.label}<span class="required-indicator">${entry?.mandatory == true ? '*' : ''}</span></label>
                    <g:select name="refType"
                              from="${EvaluationReferenceType.findAllByDisplay(true)}"
                              optionKey="id"
                              optionValue="name"
                              value="${alertInstance?.refType}"
                              noSelection="${['': message(code: 'select.one')]}"
                              class="form-control"/>
                </div>
    </g:if>
    <g:if test="${entry?.enabled == true && entry?.name == 'matchingAlerts'}">
                <div class="pull-right pull-down">
                    <a class="btn btn-primary" id="matching-alert-btn" tabindex="0" role="button">
                        <span class="glyphicon glyphicon-refresh glyphicon-refresh-animate"></span>
                        ${entry?.label}
                    </a>
                </div>
    </g:if>