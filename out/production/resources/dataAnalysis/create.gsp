<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.DataAnalysis.title" /></title>
    <asset:javascript src="fuelux/fuelux.js"/>
    <asset:stylesheet src="fuelux.css"/>
    <asset:javascript src="app/pvs/dataAnalysis/dataAnalysis.js"/>
    <asset:stylesheet href="spotfire_integration.css" />
    <g:javascript>
        var spotfireFilesListUrl = "${createLink(controller: 'dataAnalysis', action:'list')}";
        var spotfireFileViewUrl = "${createLink(controller: 'dataAnalysis', action:'view')}";
        var ajaxProductFamilySearchUrl = "${createLink(controller: 'dataAnalysis', action: 'getProductFamilyList')}"
    </g:javascript>
    <asset:javascript src="app/pvs/configuration/configurationCommon.js"/>
    <script type="application/javascript">
        $(function () {
            spotfire.init();
            spotfire.bindUiInputs();
        });
    </script>
    <g:if test="${productFamilyJson}">
        <g:javascript>
            var productFamilyJson = JSON.parse("${productFamilyJson}");
        </g:javascript>
    </g:if>

    <g:javascript>
        $(document).ready(function() {
            var startDate = "${spotfireCommand?.fromDate}";
            var endDate = "${spotfireCommand?.endDate}";
            var asOfDate = "${spotfireCommand?.asOfDate}";
            var fileName = "${spotfireCommand?.fullFileName}";
            var caseSeriesId = "${spotfireCommand?.caseSeriesId}";
            var type = "${spotfireCommand?.type}";
            console.log(startDate,endDate,asOfDate,fileName,caseSeriesId,type);
            if(!type){
                 $("#spotfireDrug").attr('checked', true);
            }
            if(startDate){
                $('#fromDate').val(moment(startDate).format(DEFAULT_DATE_DISPLAY_FORMAT));
            }else{
                $('#fromDate').val(moment(CUMMULATIVE_START_DATE).format(DEFAULT_DATE_DISPLAY_FORMAT));
            }
            if(endDate){
                $('#endDate').val(moment(endDate).format(DEFAULT_DATE_DISPLAY_FORMAT));
            }
            if(asOfDate){
                $('#asOfDate').val(moment(asOfDate).format(DEFAULT_DATE_DISPLAY_FORMAT));
            }
            if(caseSeriesId && caseSeriesId != -1) {
                $('#caseSeriesId').val(caseSeriesId).trigger('change');
            }
            if(fileName){
                 $('#fullFileName').val(fileName);
            }

        });
    </g:javascript>
</head>

<g:set var="spotfireService" bean="spotfireService"/>

<body>
<rx:container title="${message(code: "app.label.dataAnalysis", default: "Data Analysis")}">
    <g:render template="/includes/layout/flashErrorsDivs" bean="${spotfireCommand}" var="theInstance"/>
    <div id="spotfire-configuration"
         data-spotfire-server="${grailsApplication.config.spotfire.server}"
         data-spotfire-path="${grailsApplication.config.spotfire.path}"
         data-spotfire-domain-name="${grailsApplication.config.spotfire.domainName}"
         data-spotfire-version="${grailsApplication.config.spotfire.version}"
         data-spotfire-filename="${grailsApplication.config.spotfire.filename}"
         data-spotfire-user="${user_name}"
         data-spotfire-libraryRoot="${grailsApplication.config.spotfire.libraryRoot}"
         data-spotfire-protocol="${grailsApplication.config.spotfire.protocol}"
         data-spotfire-keepAlive.interval="${grailsApplication.config.spotfire.keepAlive.interval}">

        <g:form action="generate">
            <g:render template="form"/>
        </g:form>
    </div>
</rx:container>
</body>
