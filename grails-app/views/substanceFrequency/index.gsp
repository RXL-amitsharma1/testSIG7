<%@ page import="com.rxlogix.Constants; com.rxlogix.util.DateUtil; com.rxlogix.config.Tag;com.rxlogix.enums.ReportFormat" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main">


    <title><g:message code="app.label.substance.frequency"/></title>

</head>

<body>
<style>
    .glyphicon-trash{
        cursor:pointer;
    }

    .table-container {
        max-width: 100%;
        overflow-x: auto;
    }

    .dataTable {
        width: 100%;
    }
</style>
<g:if test="${flash.message}">
        <div class="alert alert-success alert-dismissible" role="alert" id="alert-success">
            <button type="button" class="close successButton">
                <span onclick="this.parentNode.parentNode.remove();
                return false;">x</span>
                <span class="sr-only"><g:message code="default.button.close.label"/></span>
            </button>
            <div class="success-message">
                ${flash.message}
            </div>
        </div>
</g:if>
<rx:container title="${message(code: "app.label.substance.frequency")}">

    <sec:ifAnyGranted roles="ROLE_CONFIGURATION_CRUD">
        <form method="post" controller="substanceFrequency" action="create">
            <button class="btn btn-primary pull-right">&#43</button>
        </form>
    </sec:ifAnyGranted>
    <br>
    <br>
    <div class="table-container">
    <table class="dataTable">
        <thead>
            <tr>
                <th class="sorting_disabled"><g:message code="app.label.substance.frequency.label" /></th>
                <th class="sorting_disabled"><g:message code="app.label.substance.frequency.productName" /></th>
                <th class="sorting_disabled"><g:message code="app.label.substance.frequency.startDate" /></th>
                <th class="sorting_disabled"><g:message code="app.label.substance.frequency.endDate" /></th>
                <th class="sorting_disabled"><g:message code="app.label.substance.frequency.uploadFrequency" /></th>
                <th class="sorting_disabled"><g:message code="app.label.substance.frequency.miningFrequency" /></th>
                <th class="sorting_disabled"><g:message code="app.label.substance.frequency.alertType" /></th>
            </tr>
        </thead>
        <tbody>
            <g:each in="${substanceFrequencyList}" status="i" var="substanceFrequency">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                    <td class = "pvi-col-md col-height word-break">${substanceFrequency.frequencyName}</td>
                    <td class = "pvi-col-md col-height word-break">${substanceFrequency.name}</td>
                    <td class = "pvi-col-sm col-height word-break">${substanceFrequency.startDate}</td>
                    <td class = "pvi-col-sm col-height word-break">${substanceFrequency.endDate}</td>
                    <td class = "pvi-col-sm col-height word-break">${substanceFrequency.uploadFrequency}</td>
                    <td class = "pvi-col-sm col-height word-break">${substanceFrequency.miningFrequency}</td>
                    <td class = "pvi-col-md col-height word-break">${substanceFrequency.alertType}</td>
                    <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_CONFIGURATION_CRUD">
                        <td class="pvi-col-xxs col-height word-break"><g:link action="edit"
                                                                              id="${substanceFrequency.id}"><i
                                    class="glyphicon glyphicon-edit"></i></g:link></td>
                        <td class="pvi-col-xxs col-height word-break"><i class="glyphicon glyphicon-trash"
                                                                         onclick="deleteFrequency(${substanceFrequency.id}, '${message(code: 'app.label.substance.frequency.confirm', default: 'Are you sure ?')}')"></i>
                        </td>
                    </sec:ifAnyGranted>
                 </tr>
            </g:each>

        </tbody>
    </table>
</rx:container>
</div>
<script>
    function deleteFrequency(id,message) {
        if(confirm(message)) {
            signal.utils.postUrl("/signal/substanceFrequency/delete", {id: id}, false);
        }
    }
</script>
</body>
</html>


