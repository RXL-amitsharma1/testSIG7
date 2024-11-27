
<div class="row m-b-10">
        <table class="table dataTable">
            <thead>
                  <th class="sorting_disabled"><g:message code="app.label.priority.details"/></th>
                  <th class="sorting_disabled"><g:message code="app.label.disposition"/></th>
            </thead>
            <tbody>
                 <tr>
                    <g:each in="${statusAttributes}" var="k, v" status="i">
                         <td>${v[1]}</td>
                    </g:each>
                 </tr>
            </tbody>
        </table>
</div>