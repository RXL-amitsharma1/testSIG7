<%@ page import="com.rxlogix.Constants" %>
<div class="rxmain-container-inner panel panel-default m-b-5">
    <div class="rxmain-container-row rxmain-container-header panel-heading pv-sec-heading">
        <label class="rxmain-container-header-label">
            <a data-toggle="collapse" href="#accordion-pvs-singlereview">
                <g:message code="app.validatedSignal.single.case.review"/>
            </a>
        </label>
        <g:if test="${scaData}">
            <span style="cursor: pointer;font-size: 125%; float: right;right: 12px;" data-field="removeSignals"
                  alertType="${Constants.AlertConfigType.SINGLE_CASE_ALERT}"
                  signalId="${signalId}" class="disassociateSignals glyphicon
                      glyphicon-link" title="Disassociate all case(s).">

            </span>
        </g:if>
    </div>

    <div class="panel-collapse rxmain-container-content rxmain-container-show collapse in pv-scrollable-dt"
         id="accordion-pvs-singlereview">
        <div id="sca-table" class="table-responsive curvedBox">
            <table id="scaTable" class="table table-striped table-curved table-hover">
                <thead>
                <tr>
                    <th>Alert Name</th>
                    <th>Case Number</th>
                    <th>Product Name</th>
                </tr>
                </thead>
                <tbody>
                <g:if test="${scaData}">
                    <g:each var="entry" in="${scaData}">
                        <tr>
                            <td>${entry.name}</td>
                            <td>${entry.caseNumber}(${(entry.followUpNumber < 1) ? 0 : entry.followUpNumber})</td>
                            <td>${entry.productName}</td>
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