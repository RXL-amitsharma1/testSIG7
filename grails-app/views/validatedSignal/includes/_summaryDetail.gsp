<div class="row">
    <div class="col-lg-6">
        <table class="table">
            <thead style="background: lightgrey;">
            <tr>
                <td>&nbsp;</td><td>PVA</td><g:if test="${grailsApplication.config.signal.evdas.enabled}"><td>EVDAS</td></g:if>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td>Case Counts in Signal</td><td>${caseCount}</td><g:if test="${grailsApplication.config.signal.evdas.enabled}"><td>NA</td></g:if>
            </tr>
            <tr>
                <td>PEC Counts in Signal</td><td>${pecCountArgus}</td><g:if test="${grailsApplication.config.signal.evdas.enabled}"><td>${evdasCount}</td></g:if>
            </tr>
            </tbody>
        </table>
        <g:if test="${adhocAlertDetails}">
            <div class="panel panel-default">
                <div class="panel-heading panel-title">
                    Other Observations
                </div>

                <div class="panel-body">
                    <ul>
                        <g:each in="${adhocAlertDetails}" var="detail">
                            <li>${detail}</li>
                        </g:each>
                    </ul>
                </div>
            </div>
        </g:if>
    </div>
    <div class="col-lg-6">
        <div class="panel panel-default ">
            <div class="panel-heading panel-title">
                <div class="rxmain-container-header-label">
                    PEC & Case Counts
                </div>
            </div>
            <div class="panel-body">
                <div id="caseCount" class="maxWidth">
                    <span class="caseCountChart"><g:message code="app.label.chart.fail.error"/></span>
                </div>
            </div>
        </div>
    </div>
</div>

