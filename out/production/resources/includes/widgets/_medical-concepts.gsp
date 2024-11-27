<div class="row">
    <div class="col-lg-6">
        <table class="table table-striped bs-example fixHeader-table text-center" id="pvTopHead">
            <thead>
            <tr>
                <th class="headRow text-left">Medical Concepts</th>
                <th class="headRow text-center">Case Count</th>
                <th class="headRow text-center">PEC Count(PVA)</th>
                <g:if test="${grailsApplication.config.signal.evdas.enabled}">
                    <th class="headRow text-center">PEC Count(EVDAS)</th>
                </g:if>

            </tr>
            </thead>
            <tbody>
            <g:each in="${conceptsMap}" var="iterator" status="i">
                <tr>
                    <td class="text-left">${iterator.key}</td>
                    <td class="align-center">${iterator.value['singleCaseAlerts'] ?: 0}</td>
                    <td class="align-center">${iterator.value['aggregateAlerts'] ?: 0}</td>
                    <g:if test="${grailsApplication.config.signal.evdas.enabled}">
                        <td class="align-center">${iterator.value['evdasAlerts'] ?: 0}</td>
                    </g:if>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
    <div class="col-lg-6">
        <div class="panel panel-default ">
            <div class="panel-heading panel-title">
                <div class="rxmain-container-header-label">
                    Medical Concepts Distribution
                    <i class="fa fa-refresh medicalConceptChart" style="cursor:pointer; float: right"></i>
                </div>
            </div>
            <div class="panel-body">
                <g:hiddenField name="medConceptsData" id="medConceptsData" value="${conceptsMap.collect{it.key}}"/>
                <g:hiddenField name="medConceptsDataCC" id="medConceptsDataCC" value="${conceptsMap.collect{it.value['singleCaseAlerts'] ?: 0}}"/>
                <g:hiddenField name="medConceptsDataPA" id="medConceptsDataPA" value="${conceptsMap.collect{it.value['aggregateAlerts'] ?: 0}}"/>
                <g:hiddenField name="medConceptsDataPF" id="medConceptsDataPF" value="${conceptsMap.collect{it.value['faersAlerts'] ?: 0}}"/>
                <g:hiddenField name="medConceptsDataPE" id="medConceptsDataPE" value="${conceptsMap.collect{it.value['evdasAlerts'] ?: 0}}"/>
                <div id="medicalConcept" class="maxWidth">
                    <span class="medicalConceptChart"><g:message code="app.label.chart.fail.error"/></span>
                </div>
            </div>
        </div>
    </div>
</div>