<%@ page import="com.rxlogix.enums.ReportFormat" %>
<head>
    <meta name="layout" content="main"/>
    <title>Clinical Trial</title>

</head>
<body>

<div>
    <!-- Nav tabs -->
    <ul id="detail-tabs" class="rxmain-container-header-label validation-tab" style="margin-left: 0px; padding-left:0px; ">
        <li class="active">
            <a href="#demographies"  aria-controls="details" role="tab" data-toggle="tab">Demographics</a>
        </li>
        <li role="presentation">
            <a href="#subjectExposure" aria-controls="assessments" role="tab" data-toggle="tab">Subject Exposure</a>
        </li>
        <li role="presentation">
            <a href="#seriousAdverseEvents" aria-controls="notifications" role="tab" data-toggle="tab">Events Incidences (%)</a>
        </li>
        <li role="presentation">
            <a href="#distributionOfDays" aria-controls="docManagement" role="tab" data-toggle="tab">AE Onset for Subjects</a>
        </li>
        <li role="presentation">
            <a href="#boxPlot" aria-controls="activities" role="tab" data-toggle="tab">Box Plot</a>
        </li>

    </ul>

    <!-- Tab panes -->
    <div class="tab-content">
        <div role="tabpanel" class="tab-pane active" id="demographies">
            <g:render template="includes/demography"/>
        </div>
        <div id="subjectExposure" class="tab-pane fade" role="tabpanel">
            <g:render template="includes/subjectExposure"/>
        </div>
        <div id="seriousAdverseEvents" class="tab-pane fade " role="tabpanel">
            <g:render template="includes/eventIncidences"/>
        </div>
        <div id="distributionOfDays" class="tab-pane fade" role="tabpanel">
            <g:render template="includes/onSetAE"/>
        </div>
        <div id="boxPlot" class="tab-pane fade" role="tabpanel">
            <g:render template="includes/boxPlot"/>
        </div>
    </div>
</div>
<input type="hidden" id="signalName" value="${signalName}" />
<script>
    $("#detail-tabs a").click(function(event) {
        setTimeout(function(){
            _.each($('#detail-tabs a'), function(ele) {
                if ($(ele).parent().hasClass('active')) {
                    $(ele).css('background', "slategrey")
                    $(ele).parent().addClass('rx-main-tab')
                } else {
                    $(ele).css('background', "darkgray")
                    $(ele).parent().removeClass('rx-main-tab')
                }
            })
        }, 100);
    });
</script>
</body>