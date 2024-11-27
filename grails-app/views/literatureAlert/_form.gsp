<g:set var="userService" bean="userService"/>
<g:if test="${actionName == 'create' || actionName == 'save' || actionName == 'run'}">
    <g:set var="createMode" value="${true}"/>
</g:if>

<g:if test="${actionName == 'edit' || actionName == 'update' || actionName == 'disable' || actionName == 'enable'}">
    <g:set var="editMode" value="${true}"/>
</g:if>

<div class="panel-group">

    <g:render template="/configuration/copyPasteModal" />
    <g:hiddenField name="productGroupSelection" id="productGroupSelection" value="${configurationInstance.productGroupSelection}"/>
    <g:hiddenField name="eventGroupSelection" id="eventGroupSelection" value="${configurationInstance.eventGroupSelection}"/>

    <g:render template="includes/alertConfiguration"
              model="[configurationInstance: configurationInstance, action: action, templateList: templateList, productGroupList: productGroupList, dateMap: dateMap]"/>

    <g:render template="alertDetails"
              model="[configurationInstance: configurationInstance, userList: userList, priorityList: priorityList]"/>

    <g:render template="/includes/widgets/alertSchedule"
              model="[configurationInstance: configurationInstance, userService: userService, isLiterature : true]"/>

    <input name="repeatExecution" id="repeatExecution" hidden="hidden"
           value="${configurationInstance?.repeatExecution}"/>
</div>
