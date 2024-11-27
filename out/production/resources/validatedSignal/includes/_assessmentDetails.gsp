<div>
    <div>
        <table class="table table-striped bs-example fixHeader-table text-center" id="pvTopHead">
            <thead>
            <tr>
                <th class="headRow text-left">Characteristics</th>
                <th class="headRow text-left"><div class="col-md-4">Category</div>

                    <div class="col-md-4 text-center">Count of Cases (%)</div>
                    <div class="col-md-4 text-center">Count of Cases </div></th></th>
            </tr>
            </thead>
            <tbody>
            <g:if test="${assessmentDetails.keySet()}">
                <g:each in="${assessmentDetails.keySet()}" var="key">
                    <tr>
                        <td class="text-left"><strong>${key}</strong></td>
                        <td class="text-left">
                            <g:each in="${assessmentDetails.get(key)}" var="obj">
                                <g:each in="${obj.keySet()}" var="valObj">
                                    <g:set var="count" value="${0}"/>
                                    <g:set var="keys" value="${obj.keySet() as List}"/>
                                    <g:if test="${!valObj.contains('Counts')}">
                                        <div class="col-md-4">${valObj}</div>

                                        <div class="col-md-4 text-center">
                                            <g:if test="${obj.get(valObj) > 50}">
                                                <span style="color: red">${obj.get(valObj)}%</span>
                                            </g:if>
                                            <g:else>
                                                <span>${obj.get(valObj)}%</span>
                                            </g:else>
                                        </div>
                                        <div class="col-md-4 text-center">${obj.get(keys[++count])}</div>
                                    </g:if>
                                </g:each>
                            </g:each>
                        </td>
                    </tr>
                </g:each>
            </g:if>
            <g:else>
                <tr>
                    <td class="text-left">-</td>
                    <td class="text-left">
                        <div class="col-md-4">-</div>

                        <div class="col-md-4">
                            <span>-</span>
                        </div>
                        <div class="col-md-4">
                            <span>-</span>
                        </div>
                    </td>
                </tr>
            </g:else>
            </tbody>
        </table>
    </div>
</div>