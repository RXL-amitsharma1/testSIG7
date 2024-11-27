<%@ page import="com.rxlogix.Constants" %>
<div class="rxmain-container-inner panel panel-default m-b-5">
    <div class="rxmain-container-row rxmain-container-header panel-heading pv-sec-heading">
        <label class="rxmain-container-header-label">
            <a data-toggle="collapse" href="#accordion-pvs-adhocreview">
                <g:message code="ad.hoc.validatedSignal.review.label"/>
            </a>
        </label>
        <g:if test="${adhocData}">
            <span style="cursor: pointer;font-size: 125%; float: right;right: 12px;" data-field="removeSignals"
                  alertType="${Constants.AlertConfigType.AD_HOC_ALERT}"
                  signalId="${signalId}" class="disassociateSignals glyphicon
                      glyphicon-link" title="Disassociate all case(s).">

            </span>
        </g:if>
    </div>

    <div class="panel-collapse rxmain-container-content rxmain-container-show collapse in pv-scrollable-dt"
         id="accordion-pvs-adhocreview">
        <div id="adhoc-table" class="table-responsive curvedBox">
            <table id="adhocTable" class="table table-striped table-curved table-hover">
                <thead>
                <tr>
                    <th>Alert Name</th>
                    <th>Description</th>
                    <th>Detected By</th>
                    <th>Data Source</th>
                </tr>
                </thead>
                <tbody>
                <g:if test="${adhocData}">
                    <g:each var="entry" in="${adhocData}">
                        <tr>
                            <td>${entry.name}</td>
                            <td>${entry.description}</td>
                            <td>${entry.detectedBy}</td>
                            <td>${entry.initialDataSource}</td>
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