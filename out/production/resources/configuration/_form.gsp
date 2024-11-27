<g:set var="userService" bean="userService"/>
<g:if test="${actionName == 'create' || actionName == 'save' || actionName == 'run'}" >
    <g:set var="createMode" value="${true}"/>
</g:if>

<g:if test="${actionName == 'edit' || actionName == 'update' || actionName == 'disable' || actionName == 'enable'}" >
    <g:set var="editMode" value="${true}"/>
</g:if>

<g:render template="includes/reportConfigurationSection" model="[configurationInstance: configurationInstance]"/>

<g:render template="includes/reportSectionsSection" model="[configurationInstance: configurationInstance]"/>

%{--<g:render template="includes/deliveryOptionsSection" model="[configurationInstance: configurationInstance]"/>--}%