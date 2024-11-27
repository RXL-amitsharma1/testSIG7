<%@ page import=" grails.util.Holders" %>
<div class="rxmain-container ">

    <div class="rxmain-container-inner">
        <div class="rxmain-container-row rxmain-container-header">
            <label class="rxmain-container-header-label">
                <g:message code="app.label.linked.configurations"/>
            </label>

            <sec:ifAnyGranted roles="ROLE_SINGLE_CASE_CONFIGURATION, ROLE_AGGREGATE_CASE_CONFIGURATION, ROLE_EVDAS_CASE_CONFIGURATION, ROLE_FAERS_CONFIGURATION, ROLE_VAERS_CONFIGURATION, ROLE_VIGIBASE_CONFIGURATION,ROLE_JADER_CONFIGURATION">
                <span class="pull-right ico-menu width-auto">
                    <span class="dropdown-toggle exportPanel" data-toggle="dropdown" tabindex="0">
                        <i class="mdi mdi-plus-box blue-1 font-24" title="Alert Configuration"></i>
                        <span class="caret hidden"></span>
                    </span>

                    <ul class="dropdown-menu">
                        <sec:ifAnyGranted roles="ROLE_SINGLE_CASE_CONFIGURATION">
                            <li>
                                <g:link controller="singleCaseAlert" action="create" params="[signalId: validatedSignal.id]"
                                        target="_blank">
                                    <g:message code="app.new.individual.case.configuration"/>
                                </g:link>
                            </li>
                        </sec:ifAnyGranted>
                        <sec:ifAnyGranted roles="ROLE_AGGREGATE_CASE_CONFIGURATION, ROLE_FAERS_CONFIGURATION, ROLE_VAERS_CONFIGURATION, ROLE_VIGIBASE_CONFIGURATION,ROLE_JADER_CONFIGURATION">
                            <li>
                                <g:link controller="aggregateCaseAlert" action="create"
                                        params="[signalId: validatedSignal.id]" target="_blank" tabindex="0">
                                    <g:message code="app.new.aggregate.case.alert"/>
                                </g:link>
                            </li>
                        </sec:ifAnyGranted>
                        <sec:ifAnyGranted roles="ROLE_EVDAS_CASE_CONFIGURATION">
                            <li>
                                <g:link controller="evdasAlert" action="create" params="[signalId: validatedSignal.id]"
                                        target="_blank" tabindex="0">
                                    EVDAS Configuration
                                </g:link>
                            </li>
                        </sec:ifAnyGranted>
                    </ul>

                </span>
            </sec:ifAnyGranted>

        </div>

        <div class="rxmain-container-content dropdown-outside">
            <table id="linkedConfigurationTable" class="row-border hover" width="100%">
                <thead>
                <tr>
                    <th><g:message code="app.label.alert.name"/></th>
                    <th><g:message code="app.label.alert.type"/></th>
                    <th><g:message code="app.label.version"/></th>
                    <th><g:message code="app.label.DateRange"/></th>
                    <th><g:message code="app.label.alert.criteria"/></th>
                    <th class="col-min-100 col-max-150"><g:message code="caseDetails.execution.date"/></th>
                    <th class="col-min-120 col-max-150"><g:message code="app.label.action"/></th>
                </tr>
                </thead>
            </table>
        </div>
    </div>

</div>
