<%@ page import="com.rxlogix.util.DateUtil; com.rxlogix.util.ViewHelper" %>

<input type="hidden" id="selectedDatasource" class="selectedDatasource" value="eudra" />

<!-- Alert Criteria -->
<g:render template="includes/alertCriteria" model="[configurationInstance: configurationInstance,action:action]" />
<g:render template="/configuration/copyPasteModal" />
<!-- Alert Details -->
<g:render template="includes/alertDetails" model="[configurationInstance: configurationInstance, priorityList: priorityList,userList:userList]" />

<!-- Alert Details -->
<g:render template="includes/alertSchedule" model="[configurationInstance: configurationInstance]" />


