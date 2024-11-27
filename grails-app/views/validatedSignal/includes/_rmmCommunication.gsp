<div class="panel panel-default rxmain-container pv-max-scrollable-table rxmain-container-top">
    <div class="rxmain-container-row rxmain-container-header panel-heading pv-sec-heading">
        <label class="rxmain-container-header-label">
            <a data-toggle="collapse" href="#accordion-pvs-rmms">
                ${message(code: 'signal.rmms.label')}
            </a>
        </label>

        <span class="pv-head-config configureFields">
            <a href="#" class="pull-right ic-sm add-new-row ${buttonClass}" id="addRMMSRow" data-toggle="dropdown" aria-expanded="false">
                <i class="md md-add" aria-hidden="true" title="Add RMMs"></i>
            </a>
            <a href="javascript:void(0);" title="Remove RMMs" id="remove-rmms-row" class="table-row-del hidden-ic hide ${buttonClass}">
                <i class="mdi mdi-close" aria-hidden="true"></i>
            </a>
        </span>

    </div>

    <div id="signalRmms">
        <g:render template="/validatedSignal/includes/signal_rmms" model="[rmmType: rmmType, rmmStatus: rmmStatus]"/>
    </div>

</div>

<div class="panel panel-default rxmain-container pv-max-scrollable-table rxmain-container-top">
    <div class="rxmain-container-row rxmain-container-header panel-heading pv-sec-heading">
        <label class="rxmain-container-header-label">
            <a data-toggle="collapse" href="#accordion-pvs-communication">
                ${message(code: 'signal.communications.label')}
            </a>
        </label>

        <span class="pv-head-config configureFields">
            <a href="#" class="pull-right ic-sm add-new-row ${buttonClass}" id="addCommunicationRow" data-toggle="dropdown" aria-expanded="false">
                <i class="md md-add" aria-hidden="true" title="Add Communications"></i>
            </a>
            <a href="javascript:void(0);" title="Remove Communications" id="remove-comm-row" class="table-row-del hidden-ic hide">
                <i class="mdi mdi-close" aria-hidden="true"></i>
            </a>
        </span>

    </div>

    <div id="signalCommunication">
        <g:render template="/validatedSignal/includes/signal_communication" model="[communicationType: communicationType, rmmStatus: rmmStatus]"/>
    </div>

</div>
<g:render template="/validatedSignal/includes/email_generation_modal"/>
