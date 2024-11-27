<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Topic</title>
    <asset:stylesheet src="configuration.css"/>
    <asset:javascript src="fuelux/fuelux.js"/>
    <asset:stylesheet src="fuelux.css"/>
    <asset:stylesheet src="dictionaries.css"/>
    <asset:javascript src="app/pvs/alert_utils/alert_product_utils.js"/>
    <g:javascript>
        var searchStrategyProducts = "${createLink(controller: 'validatedSignal', action: 'searchStrategyProducts')}"
    </g:javascript>

    <script>
        $(document).ready(function () {
            $(window).keydown(function (event) {
                if (event.keyCode == 13) {
                    event.preventDefault();
                    return false;
                }
            });
        });
    </script>
</head>

<body>
<rx:container title="Edit Topic">
    <g:render template="/includes/layout/flashErrorsDivs" bean="${topic}" var="theInstance"/>
    <g:form method="post" autocomplete="off" controller="topic" action="update">
        <g:hiddenField name="topicId" value="${topic?.id}"/>
        <g:render template="form" model="[topic: topic, initialDataSource: initialDataSource]"/>
        <input type="submit" style="float:right" class="btn btn-default btn-primary"
               value="${message(code: 'default.button.update.label')}"/>
        <g:link style="float:right" class="btn btn-default" controller="validatedSignal"
                action="index">${message(code: 'default.button.cancel.label')}</g:link>
    </g:form>
</rx:container>

<g:render template="/includes/modals/product_selection_modal"/>
<input type="hidden" id="selectedDatasource" class="selectedDatasource" value="pva"/>
</body>
</html>