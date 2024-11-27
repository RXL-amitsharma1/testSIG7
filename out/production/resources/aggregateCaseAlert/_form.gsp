<g:set var="userService" bean="userService"/>
<g:if test="${actionName == 'create' || actionName == 'save' || actionName == 'run'}" >
    <g:set var="createMode" value="${true}"/>
</g:if>

<g:if test="${actionName == 'edit' || actionName == 'update' || actionName == 'disable' || actionName == 'enable'}" >
    <g:set var="editMode" value="${true}"/>
</g:if>
<div class="panel-group">
    <g:if test="${grailsApplication.config.pvsignal.multiple.datasource.toBoolean() == true}" >
        <g:render template="/includes/widgets/datasource" model="[dataSourceMap:dataSourceMap,configurationInstance: configurationInstance,listOfSelectedDataSource:listOfSelectedDataSource]"/>
    </g:if>
    <g:else>
        <input type="hidden" id="selectedDatasource" class="selectedDatasource" value="pva" />
    </g:else>
    <g:hiddenField name="isSpotfireEnabled" id="isSpotfireEnabled" value="${spotfireEnabled}"/>
    <g:hiddenField name="productGroupSelection" id="productGroupSelection" value="${configurationInstance.productGroupSelection}"/>
    <g:hiddenField name="eventGroupSelection" id="eventGroupSelection" value="${configurationInstance.eventGroupSelection}"/>
    <g:render template="/configuration/copyPasteModal" />
    <g:render template="includes/alertConfiguration" model="[configurationInstance: configurationInstance,action:action, sMQList: sMQList, templateList: templateList, productGroupList: productGroupList, isPVCM: isPVCM]"/>

    <g:render template="includes/advancedOptions" model="[configurationInstance: configurationInstance, safetyMiningVariables: safetyMiningVariables, faersMiningVariables: faersMiningVariables]"/>

    <g:render template="/includes/widgets/reportSectionsSection" model="[configurationInstance: configurationInstance, spotfireEnabled:spotfireEnabled,appType: appType]"/>

    <g:render template="/includes/widgets/alertDetails" model="[configurationInstance: configurationInstance, userList: userList, priorityList: priorityList,action:action]"/>

    <g:render template="/includes/widgets/alertSchedule" model="[configurationInstance: configurationInstance]" />

    <input name="repeatExecution" id="repeatExecution" hidden="hidden" value="${configurationInstance.repeatExecution}"/>
</div>
