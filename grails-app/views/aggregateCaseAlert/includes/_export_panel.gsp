<%@ page import="com.rxlogix.enums.ReportFormat" %>

<div id="exportTypes" class="export-type-list">

    <g:link controller="${controller}" action="${action}" class="m-r-30"
            params="${[outputFormat: ReportFormat.DOCX,id : id]+extraParams}">
        <i class="fa fa-file-word-o m-r-5" aria-hidden="true"></i><g:message code="save.as.word" />
    </g:link>

    <g:link controller="${controller}" action="${action}" class="m-r-30"
            params="${[outputFormat: ReportFormat.XLSX,id : id]+extraParams}">
        <i class="fa fa-file-excel-o m-r-5" aria-hidden="true"></i><g:message code="save.as.excel" />
    </g:link>

    <g:link controller="${controller}" action="${action}" class="m-r-30"
            params="${[outputFormat: ReportFormat.PDF,id : id]+extraParams}">
        <i class="fa fa-file-pdf-o m-r-5" aria-hidden="true"></i><g:message code="save.as.pdf" />
    </g:link>
</div>