<%@ page import="com.rxlogix.Constants" %>
<div class="rxmain-container-inner panel panel-default m-b-5">
    <div class="rxmain-container-row rxmain-container-header panel-heading pv-sec-heading">
        <label class="rxmain-container-header-label">
            <a data-toggle="collapse" href="#accordion-pvs-evdasreview">
                Evdas Review Observations
            </a>
        </label>
        <g:if test="${evdasData}">
            <span style="cursor: pointer;font-size: 125%; float: right;right: 12px;" data-field="removeSignals"
                  alertType="${Constants.AlertConfigType.EVDAS_ALERT}"
                  signalId="${signalId}" class="disassociateSignals glyphicon
                      glyphicon-link" title="Disassociate all case(s).">

            </span>
        </g:if>
    </div>

    <div class="panel-collapse rxmain-container-content rxmain-container-show collapse in pv-scrollable-dt"
         id="accordion-pvs-evdasreview">
        <div id="evdas-table" class="table-responsive curvedBox">
            <table id="evdasTable" class="table table-striped table-curved table-hover">
                <colgroup>
                    <col style="width: 25%;">
                    <col style="width: 25%;">
                    <col style="width: 25%;">
                    <col style="width: 25%;">
                </colgroup>
                <thead>
                <tr>
                    <th>Alert Name</th>
                    <th>SOC</th>
                    <th>Product Name</th>
                    <th>Event PT</th>
                </tr>
                </thead>
                <tbody>
                <g:if test="${evdasData}">
                    <g:each var="entry" in="${evdasData}">
                        <tr>
                            <td class ="cell-break word-break">${entry.name}</td>
                            <td class ="cell-break word-break">${entry.soc}</td>
                            <td class ="cell-break word-break">${entry.substance}</td>
                            <td class ="cell-break word-break">${entry.pt}</td>
                        </tr>
                    </g:each>
                </g:if>
                <g:else>
                    <tr>
                        <td colspan="7">No data available.</td>
                    </tr>
                </g:else>
                </tbody>

            </table>
        </div>
    </div>
</div>