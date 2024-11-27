<div class="col-sm-12">
    <div class="rxmain-container ">
        <div class="rxmain-container-inner">
            <div class="rxmain-container-row rxmain-container-header">
                <label class="rxmain-container-header-label">
                    ${message(code: 'app.label.signalWorkflow')}
                </label>
            </div>

            <div class="rxmain-container-content">
                <div id="signalHistory">
                    <g:if test="${enableSignalWorkflow}">
                        <g:render template="/validatedSignal/includes/signal_history" model="[timezone: timezone]"/>
                    </g:if>
                    <g:else>
                        <g:render template="/validatedSignal/includes/signal_history_disable_workflow" model="[timezone: timezone]"/>
                    </g:else>
                </div>
            </div>
        </div>
    </div>
</div>

