<%@ page import="com.rxlogix.enums.ReportFormat" %>

<div id="exportTypes" style="margin-top: 20px;">
    <g:link controller="${controller}" action="${action}" style="margin-right: 20px"
            params="${[outputFormat: ReportFormat.PDF,id : id]+extraParams}">
        <asset:image src="pdf-icon.jpg" class="pdf-icon" height="16" width="16"/> <g:message code="save.as.pdf" />
    </g:link>

    <g:link controller="${controller}" action="${action}" style="margin-right: 20px"
            params="${[outputFormat: ReportFormat.XLSX,id : id]+extraParams}">
        <asset:image src="excel.gif" class="excel-icon" height="16" width="16"/> <g:message code="save.as.excel" />
    </g:link>

    <g:link controller="${controller}" action="${action}"
            params="${[outputFormat: ReportFormat.DOCX,id : id]+extraParams}">
        <asset:image src="word-icon.png" class="word-icon" height="16" width="16"/> <g:message code="save.as.word" />
    </g:link>
</div>