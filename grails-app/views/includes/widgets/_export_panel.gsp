<%@ page import="com.rxlogix.enums.ReportFormat" %>

<span tabindex="0" class="pull-right pos-rel m-r-15" style="cursor: pointer" data-toggle="tooltip" data-title="Export to" data-placement="bottom">
    <span class="dropdown-toggle exportPanel" data-toggle="dropdown" >
        <i class="mdi mdi-export blue-1 font-22 lh-1"></i>
        <span class="caret hidden"></span>
    </span>
    <ul class="dropdown-menu export-type-list" id="exportTypesCaseHistory">
        <li>
            <g:link controller="${controller}" action="${action}" style="margin-right: 20px" class="exportAlertHistories"
                    params="${[outputFormat: ReportFormat.PDF]+extraParams}">
                <asset:image src="pdf-icon.jpg" class="pdf-icon" height="16" width="16"/> <g:message code="save.as.pdf" />
            </g:link>
        </li>
        <li>
            <g:link controller="${controller}" action="${action}" style="margin-right: 20px" class="exportAlertHistories"
                    params="${[outputFormat: ReportFormat.XLSX]+extraParams}">
                <asset:image src="excel.gif" class="excel-icon" height="16" width="16"/> <g:message code="save.as.excel" />
            </g:link>
        </li>
        <li>
            <g:link controller="${controller}" action="${action}" class="exportAlertHistories"
                    params="${[outputFormat: ReportFormat.DOCX]+extraParams}">
                <asset:image src="word-icon.png" class="word-icon" height="16" width="16"/> <g:message code="save.as.word" />
            </g:link>
        </li>
    </ul>
</span>

