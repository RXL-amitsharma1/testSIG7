<%@ page import="com.rxlogix.util.ViewHelper; com.rxlogix.enums.TemplateTypeEnum" %>
<g:set var="templateService" bean="templateService"/>

<div class="margin20Top">
    <h4>${g.message(code:reportTemplateInstance.templateType.getI18nKey())}</h4>
</div>

<div>
    <g:if test="${reportTemplateInstance.templateType == TemplateTypeEnum.CASE_LINE}">
        <g:render template="includes/caseLineListing" model="['reportTemplateInstance': reportTemplateInstance, 'editable': editable]"/>
    </g:if>

    <g:if test="${reportTemplateInstance.templateType == TemplateTypeEnum.DATA_TAB}">
        <div class="tab-pane" id="dataTabulation">
            <g:render template="includes/dataTabulation" model="['reportTemplateInstance': reportTemplateInstance, 'editable': editable]"/>
        </div>
    </g:if>

    <g:if test="${reportTemplateInstance.templateType == TemplateTypeEnum.CUSTOM_SQL}">
        <div class="tab-pane" id="customSQL">
            <g:render template="includes/customSQL" model="['template': reportTemplateInstance, 'editable': editable]"/>
        </div>
    </g:if>

    <g:if test="${reportTemplateInstance.templateType == TemplateTypeEnum.NON_CASE}">
        <div class="tab-pane" id="nonCase">
            <g:render template="includes/nonCase" model="['template': reportTemplateInstance, 'editable': editable]"/>
        </div>
    </g:if>
</div>


%{-- //todo:  Push the items below into the specific template where they are used--}%
<g:hiddenField name="templateType" value="${reportTemplateInstance.templateType.key}"/>
