<%@ page import="com.rxlogix.enums.TemplateTypeEnum" %>
<g:if test="${editable}">
    <div class="expandingArea">
        <pre><span></span><br></pre>
        <g:textArea class="sqlBox" name="customSQLTemplateSelectFrom" placeholder='${message(code:("app.template.customSQL.exampleText1"))}' value="${template?.templateType == TemplateTypeEnum.CUSTOM_SQL ? template.customSQLTemplateSelectFrom : ''}"/>
    </div>
    <pre class="textSegment">where exists (select 1 from GENERATED_QUERY_RESULT caseList where cm.case_id = caseList.case_id and cm.version_num = caseList.dlp_revision_number)</pre>
    <div class="expandingArea">
        <pre><span></span><br></pre>
        <g:textArea class="sqlBox" name="customSQLTemplateWhere" placeholder='${message(code:("app.template.customSQL.exampleText2"))}' value="${template?.templateType == TemplateTypeEnum.CUSTOM_SQL ? template.customSQLTemplateWhere : ''}"/>
    </div>

    <div class="bs-callout bs-callout-info">
        <h5><g:message code="example" />:</h5>
        <div class="text-muted"><pre>select case_num "Case Number" from case_master cm</pre></div>
    </div>
</g:if>
<g:else>
    <pre>${template?.templateType == TemplateTypeEnum.CUSTOM_SQL ? template.customSQLTemplateSelectFrom : ''}</pre>
    <pre>where exists (select 1 from GENERATED_QUERY_RESULT caseList where cm.case_id = caseList.case_id and cm.version_num = caseList.dlp_revision_number)</pre>
    <pre>${template?.templateType == TemplateTypeEnum.CUSTOM_SQL ? template.customSQLTemplateWhere : ''}</pre>
    <div style="padding-bottom: 20px;"></div>
</g:else>
