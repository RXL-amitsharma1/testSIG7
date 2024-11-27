<%@ page import="com.rxlogix.enums.TemplateTypeEnum" %>
<g:if test="${editable}" >
    <div class="expandingArea">
        <pre><span></span><br></pre>
        <g:textArea class="sqlBox" name="nonCaseSql"
                    placeholder='${message(code:("app.template.nonCase.exampleText1"))}'
                    value="${template?.templateType == TemplateTypeEnum.NON_CASE ? template.nonCaseSql : ''}"/>
    </div>

    <div class="bs-callout bs-callout-info">
        <h5><g:message code="example" />:</h5>
        <div class="text-muted"><pre>select case_num "Case Number" from case_master cm where rownum < 15</pre></div>
    </div>
</g:if>
<g:else>
    <pre>${template?.templateType == TemplateTypeEnum.NON_CASE ? template.nonCaseSql : ''}</pre>
    <div style="padding-bottom: 20px;"></div>
</g:else>