<%@ page import="com.rxlogix.util.ViewHelper" %>
<div class="rxmain-container rxmain-container-top">
    <g:set var="userService" bean="userService"/>
    <g:if test="${action == "copy"}">
        <g:hiddenField name="owner" id="owner" value="${userService.getUser().id}"/>
    </g:if>
    <g:else>
        <g:hiddenField name="owner" id="owner" value="${configurationInstance?.owner?.id ?: userService.getUser().id}"/>
    </g:else>
    <div class="rxmain-container-inner">
        <div class="rxmain-container-row rxmain-container-header">
            <label class="rxmain-container-header-label">
                <g:message code="app.label.alert.criteria"/>
            </label>
        </div>

        <div class="rxmain-container-content">
            <g:render template="includes/alertCriteria"
                      model="[configurationInstance: configurationInstance, action: action, sMQList: sMQList, templateList: templateList, productGroupList: productGroupList, isPVCM:isPVCM]"/>
        </div>
    </div>
</div>
