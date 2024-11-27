<style>
.row{
    padding:0;
}
.table td, table th {
    word-wrap: break-word;
    max-width: 15px;

}
.table tr{
    border: 1px solid #dddddd;
}
.odd {
    background-color: #EFEFEF;
    padding:0;
}
/*table.narrow th, td, tr {*/
    /*width:10px;*/
    /*height:10px;*/
}

</style>
<div class="container-fluid">
    <div class="row">
        <table class="table m-t-0 m-b-0">
            <thead>
                 <g:if test="${column==0}">
                    <g:each in="${parameter}" var="paramKey, paramValue" status="i">
                        <th>${paramKey}</th>
                     </g:each>
                 </g:if>
            </thead>
            <tbody>
            <tr>
                <g:each in="${parameter}" var="paramKey, paramValue" status="i">
                    <g:if test="${column%2==0}">
                        <td class="even" style="word-break: break-all !important;">${paramValue}</td>
                    </g:if>
                    <g:else>
                        <td class="odd" style="word-break: break-all !important;">${paramValue}</td>
                    </g:else>
                </g:each>
            </tr>
            </tbody>
        </table>
    </div>
</div>