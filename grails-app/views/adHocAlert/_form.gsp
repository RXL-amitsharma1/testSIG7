<%@ page import="grails.util.Holders;" %>
<g:set var="userService" bean="userService"/>
<g:if test="${actionName == 'create' || actionName == 'save' || actionName == 'run'}" >
    <g:set var="createMode" value="${true}"/>
</g:if>

<g:if test="${actionName == 'edit' || actionName == 'update' || actionName == 'disable' || actionName == 'enable'}" >
    <g:set var="editMode" value="${true}"/>
</g:if>

<!-- For the adhoc alert we are going to keep the selected datasource as pva -->
<input type="hidden" id="selectedDatasource" class="selectedDatasource" value="pva" />
<g:hiddenField name="productGroupSelection" id="productGroupSelection" value="${alertInstance.productGroupSelection}"/>
<g:hiddenField name="eventGroupSelection" id="eventGroupSelection" value="${alertInstance.eventGroupSelection}"/>

<g:render template="/configuration/copyPasteModal" />

<g:render template="includes/alertConfiguration" model="[alertInstance: alertInstance, formulations: formulations, lmReportTypes: lmReportTypes, countryNames: countryNames, isPVCM: isPVCM,
                                                         alertCriteriaList: alertCriteriaList, sectionLabelMap: sectionLabelMap]"/>

<g:render template="includes/alertDetailsDynamic" model="[alertInstance            : alertInstance,
                                                          safetyLeadSecurityService: safetyLeadSecurityService, userService:userService,
                                                          alertDetailsList         : alertDetailsList, sectionLabelMap: sectionLabelMap]"/>
<g:if test="${signalStatusList?.any { it.enabled }}">
    <g:render template="includes/ha_signal_statusDynamic" model="[alertInstance: alertInstance, signalStatusList: signalStatusList, sectionLabelMap: sectionLabelMap]"/>
</g:if>
<g:if test="${editMode}">
    <rx:container title="${message(code: 'app.label.attachments')}">
        <g:render template="/includes/widgets/attachment_panel" model="[alertInst: alertInstance, source: 'edit']" />
    </rx:container>
</g:if>

