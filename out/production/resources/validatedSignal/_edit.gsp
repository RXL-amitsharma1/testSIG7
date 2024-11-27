<div class="alert alert-danger alert-dismissible hide edit-alert-danger" role="alert">
    <button type="button" class="close errorButton">
        <span aria-hidden="true">&times;</span>
        <span class="sr-only"><g:message code="default.button.close.label"/></span>
    </button>

    <div class="error-message">
    </div>
</div>

<div class="alert alert-success alert-dismissible hide" role="alert" id="alert-success">
    <button type="button" class="close successButton">
        <span aria-hidden="true">&times;</span>
        <span class="sr-only"><g:message code="default.button.close.label"/></span>
    </button>

    <div class="success-message">
    </div>
</div>

<g:form name="signalEditForm" method="post" autocomplete="off">
    <div class="rxmain-container ">
        <div class="rxmain-container-inner">
            <div class="rxmain-container-row rxmain-container-header">
                <label class="rxmain-container-header-label">
                    <g:message code="app.signal.summary"/>
                </label>
            </div>

            <div class="rxmain-container-content">
                <g:render template="signalConfiguration"
                          model="[validatedSignal: validatedSignal, genericComment: genericComment, initialDataSource: initialDataSource, signalOutcomes: signalOutcomes, genericComment: genericComment,
                                                                    priorityList   : priorityList, userList: userList, actionTakenList: actionTakenList, signalTypeList: signalTypeList, linkedSignals: linkedSignals, timezone: timezone]"/>
            </div>
        </div>
    </div>

    <g:if test="${showDetectedBy || showTopicInformation || showAggregateDate || showShareWith}">
        <div class="rxmain-container ">
            <div class="rxmain-container-inner">
                <div class="rxmain-container-row rxmain-container-header">
                    <label class="rxmain-container-header-label">
                        <g:message code="signal.details.label"/>
                    </label>
                </div>

                <div class="rxmain-container-content">
                    <g:render template="signalDetails"
                              model="[validatedSignal: validatedSignal, genericComment: genericComment, timezone: timezone]"/>
                </div>
            </div>
        </div>
    </g:if>

    <g:if test="${showHealthAuthority}">
        <div class="rxmain-container ">
            <div class="rxmain-container-inner">
                <div class="rxmain-container-row rxmain-container-header">
                    <label class="rxmain-container-header-label">
                        <g:message code="app.header.label.ha.signal.status"/>
                    </label>
                </div>

                <div class="rxmain-container-content">
                    <g:render template="includes/ha_signal_status_create"
                              model="[validatedSignal: validatedSignal, genericComment: genericComment, haSignalStatusList: haSignalStatusList, timezone: timezone]"/>
                </div>
            </div>
        </div>
    </g:if>

    <div class="m-t-15 text-right">
        <g:hiddenField name="signalId" value="${validatedSignal.id}"/>
        <input class="btn primaryButton btn-primary repeat ${hasSignalReviewerAccess?'':'hidden'}" id="updateSignal" tabindex="0" accesskey="r" type="submit"
               value="${message(code: 'default.button.update.label')}"/>
    </div>
</g:form>
