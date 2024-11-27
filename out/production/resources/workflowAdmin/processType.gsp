<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <asset:javascript src="app/pvs/configuration/deliveryOption.js"/>
    <asset:javascript src="app/pvs/dataTablesActionButtons.js"/>
    <asset:javascript src="fuelux/fuelux.js"/>
    <asset:stylesheet src="fuelux.css" />
    <title>Process Types</title>
</head>
<body>
    <rx:container id="show-process" title="Process Types" options="true">
        <g:applyLayout name="workflow">
            <content>
                <g:render plugin="grailsflow" template="/process/showTypes_temp" />
            </content>
        </g:applyLayout>
    </rx:container>
</body>
</html>