<%@ page import="com.rxlogix.Constants" %>
<div class="rxmain-container-inner panel panel-default m-b-5">
    <div class="rxmain-container-row rxmain-container-header panel-heading pv-sec-heading">
        <label class="rxmain-container-header-label">
            <a data-toggle="collapse" href="#accordion-pvs-literaturereview">
                <g:message code="app.validatedSignal.new.literature.review"/>
            </a>
        </label>
        <g:if test="${literatureData}">
            <span style="cursor: pointer;font-size: 125%; float: right;right: 12px;" data-field="removeSignals"
                  alertType="${Constants.AlertConfigType.LITERATURE_SEARCH_ALERT}"
                  signalId="${signalId}" class="disassociateSignals glyphicon
                      glyphicon-link" title="Disassociate all case(s).">

            </span>
        </g:if>
    </div>

    <div class="panel-collapse rxmain-container-content rxmain-container-show collapse in pv-scrollable-dt"
         id="accordion-pvs-literaturereview">
        <div id="literature-table" class="table-responsive curvedBox">
            <table id="literatureTable" class="table table-striped table-curved table-hover">
                <thead>
                <tr>
                    <th>Alert Name</th>
                    <th>Title</th>
                    <th>Authors</th>
                </tr>
                </thead>
                <tbody>
                <g:if test="${literatureData}">
                    <g:each var="entry" in="${literatureData}">
                        <tr>
                            <td>${entry.name}</td>
                            <td>${entry.articleTitle}</td>
                            <td>${entry.articleAuthors}</td>
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