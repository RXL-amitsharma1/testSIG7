<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Signal Strategy</title>
    <asset:stylesheet src="configuration.css"/>
    <asset:javascript src="fuelux/fuelux.js"/>
    <asset:stylesheet src="fuelux.css" />
    <asset:javascript src="app/pvs/pvConcepts/pvConcepts.js"/>
    <script>
        $(document).ready(function() {
            $(window).keydown(function(event){
                if(event.keyCode == 13) {
                    event.preventDefault();
                    return false;
                }
            });
        });
    </script>
</head>

<body>
    <rx:container title="Signal Strategy" >
        <g:render template="/includes/layout/flashErrorsDivs" bean="${signalStrategy}" var="theInstance"/>
        <g:form method="post" autocomplete="off">
            <g:render template="form" model="[edit: false, signalStrategy: signalStrategy]" />
        </g:form>

    </rx:container>
</body>
</html>