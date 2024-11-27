<div id="agg-table" class="table-responsive curvedBox">
    <table id="aggTable" class="table table-striped table-curved table-hover">
        <thead>
        <tr>
            <th>Signal Name</th>
            <th>SOC</th>
            <th>Product Name</th>
            <th>Event PT</th>
            <g:if test="${totalInstancesCount > 0}">
                <th><span style="cursor: pointer;font-size: 125%;" data-field="removeSignals" alertType="${alertType}"
                      configId="${configId}" deleteLatest=${deleteLatest} class="disassociateSignals glyphicon
                      glyphicon-link" title="Disassociate all signal(s)."></span></th>
            </g:if>
        </tr>
        </thead>
        <tbody>
        <g:if test="${totalInstancesCount > 0}">
            <g:each var="entry" in="${linkedSignalsData.alertData}">
                <tr>
                    <td>${entry.name}</td>
                    <td>${entry.soc}</td>
                    <td>${entry.productName}</td>
                    <td>${entry.eventName}</td>
                    <g:if test="${totalInstancesCount > 0}">
                        <td></td>
                    </g:if>
                </tr>
            </g:each>
        </g:if>
        <g:else>
            <tr>
                <td colspan="7" style="text-align: center;">No data available.</td>
            </tr>
        </g:else>
        </tbody>

    </table>
</div>