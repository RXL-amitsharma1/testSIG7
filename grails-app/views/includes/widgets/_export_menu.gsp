<%@ page import="com.rxlogix.Constants; com.rxlogix.enums.ReportFormat" %>

<span class="pull-right m-r-10 pos-rel" style="cursor: pointer">
    <span class="dropdown-toggle exportPanel grid-menu-tooltip" data-toggle="dropdown" tabindex="0" role="button" accesskey="x" title="Export to">
        <i class="mdi mdi-export blue-1 font-24 "></i>
        <span class="caret hidden"></span>
    </span>
    <ul class="dropdown-menu export-type-list" id="exportTypes">
        <strong class="font-12">Export</strong>
        <g:if test="${isLatest && appType == Constants.AlertConfigType.SINGLE_CASE_ALERT}">
            <li><g:link controller="${domainName}" action="exportReport" class="m-r-30"
                        params="${[outputFormat: ReportFormat.XLSX,id : id, alertName : domainName,
                                   callingScreen: callingScreen, length: '-1', start: '0', adhocRun: false,cumulativeExport: true, isArchived: false]}">
                <img src="/signal/assets/excel.gif" class="m-r-10" height="16" width="16" /><g:message code="cumulative.export" />
            </g:link></li>
        </g:if>
        <li><g:link controller="${domainName}" action="exportReport" class="m-r-30"
                    params="${[outputFormat: ReportFormat.DOCX,id : id, alertName : domainName,
                               callingScreen: callingScreen, length: '-1', start: '0', adhocRun: false, isArchived: isArchived]}">
            <img src="/signal/assets/word-icon.png" class="m-r-10" height="16" width="16" /><g:message code="save.as.word" />
        </g:link>
        </li>
        <li><g:link controller="${domainName}" action="exportReport" class="m-r-30"
                    params="${[outputFormat: ReportFormat.XLSX,id : id, alertName : domainName,
                               callingScreen: callingScreen, length: '-1', start: '0', adhocRun: false, isArchived: isArchived]}">
            <img src="/signal/assets/excel.gif" class="m-r-10" height="16" width="16" /><g:message code="save.as.excel" />
        </g:link></li>
        <li><g:link controller="${domainName}" action="exportReport" class="m-r-30"
                    params="${[outputFormat: ReportFormat.PDF,id : id, alertName : domainName,
                               callingScreen: callingScreen, length: '-1', start: '0', adhocRun: false, isArchived: isArchived]}">
            <img src="/signal/assets/pdf-icon.jpg" class="m-r-10" height="16" width="16" /><g:message code="save.as.pdf" />
        </g:link></li>
        <g:set var="userService" bean="userService"/>
        <g:if test="${callingScreen != Constants.Commons.DASHBOARD && callingScreen != Constants.Commons.TRIGGERED_ALERTS}">
            <STRONG class="font-12">Detection Summary</STRONG>
            <li>
                <a target="_blank"
                   href="exportSignalSummaryReport?outputFormat=DOCX&id=${executedConfigId}&cumulative=${cumulative}&isArchived=${isArchived}"><img
                        src="/signal/assets/word-icon.png" class="m-r-10" height="16" width="16"/><g:message
                        code="save.as.word"/></a>
            </li>
            <li>
                <a target="_blank"
                   href="exportSignalSummaryReport?outputFormat=XLSX&id=${executedConfigId}&cumulative=${cumulative}&isArchived=${isArchived}"><img
                        src="/signal/assets/excel.gif" class="m-r-10" height="16" width="16"/><g:message
                        code="save.as.excel"/></a>
            </li>
            <li>
                <a target="_blank"
                   href="exportSignalSummaryReport?outputFormat=PDF&id=${executedConfigId}&cumulative=${cumulative}&isArchived=${isArchived}"><img
                        src="/signal/assets/pdf-icon.jpg" class="m-r-10" height="16" width="16"/><g:message
                        code="save.as.pdf"/></a>
            </li>
        </g:if>
    </ul>
</span>