
<div class="row m-b-10">
    <table class="table dataTable">
        <thead>
        <g:each in="${statusAttributes}" var="k, v" status="i">
            <th class="sorting_disabled">${v[0]}</th>
        </g:each>
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
