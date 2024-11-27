<%@ page import="com.rxlogix.util.ViewHelper" %>

<form id="businessConfigurationForm" name="businessConfigurationForm" method="post" autocomplete="off" >
    <div class="row form-group">
        <div class="col-lg-3 hide">
            <label><g:message code="app.label.business.configuration.analysis.level" /></label>
            <g:select name="analysisLevel"
                      from="${['Product', 'PT']}"
                      value="${businessConfiguration.analysisLevel}"
                      class="form-control "/>
        </div>
        <div class="col-lg-3">
            <label><g:message code="app.label.business.configuration.min.cases" /></label>
            <label><span class="required-indicator">*</span></label>
            <input class="form-control" name="minCases" type="text" value="${businessConfiguration.minCases}"/>
        </div>
        <div class="col-lg-3">
            <label><g:message code="app.label.business.configuration.auto.route.algo" /></label>
            <g:select id="autoStateConfiguration" name="autoStateConfiguration" from="${ViewHelper.getBusinessConfigurationType()}"
                      optionValue="display" optionKey="name" value="${businessConfiguration?.autoStateConfiguration}"
                      class="form-control"/>
        </div>
    </div>

    <div style="margin-top:15px;">
        <div style="text-align: right">
            <g:if test="${mode == 'create'}" >
                <g:actionSubmit class="btn primaryButton btn-primary" action="save" type="submit" value="Save"/>
            </g:if>
            <g:else>
                <g:actionSubmit class="btn primaryButton btn-primary" action="update" type="submit" value="Update"/>
            </g:else>
        </div>
    </div>

</form>