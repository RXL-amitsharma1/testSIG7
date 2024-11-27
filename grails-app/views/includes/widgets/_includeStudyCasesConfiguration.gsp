<div class="col-xs-3">
    <g:if test="${configurationInstance.studyCases}">
        <div class="row">
            <div class="col-xs-12">
                <label><g:message code="app.label.configuration.medication.ignore"/></label>
                <div>
                    <g:formatBoolean boolean="${configurationInstance.ignoreStudyType}"
                                     true="${message(code: "default.button.yes.label")}"
                                     false="${message(code: "default.button.no.label")}"/>
                </div>
            </div>
        </div>
        <div class="row">
            <div class="col-xs-12">
                <label><g:message code="app.label.configuration.poi"/></label>
                <div>
                    <g:formatBoolean boolean="${configurationInstance.onlyPoi}"
                                     true="${message(code: "default.button.yes.label")}"
                                     false="${message(code: "default.button.no.label")}"/>
                </div>
            </div>
        </div>
    </g:if>
</div>
