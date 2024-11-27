<%--
  Created by IntelliJ IDEA.
  User: lei
  Date: 8/30/17
  Time: 10:15 AM
--%>

<%@ page import="grails.converters.JSON" %>
<%@ page contentType="text/html;charset=UTF-8" defaultCodec="none" %>

<head>
    <meta name="layout" content="main"/>
    <title>Heat Map</title>
    <asset:javascript src="highcharts.js"/>
    <asset:javascript src="vendorUi/highcharts/highcharts-more.js"/>
    <asset:javascript src="vendorUi/highcharts/highcharts-3d.js"/>
    <asset:javascript src="solid-gauge.js"/>
    <asset:javascript src="dygraphs/dygraph.min.js"/>
    <asset:stylesheet src="dygraph.css"/>
    <asset:javascript src="app/pvs/validated_signal/heat_map.js"/>
    <g:javascript>
        var xAxis = ${heatMap['years']};
        var yAxis = ${heatMap['socs']};
        var chartData = ${heatMap['data']}
    </g:javascript>
</head>

<body>
<rx:container title="Heat Map">
    <div id="chart-container">

    </div>
</rx:container>
</body>
