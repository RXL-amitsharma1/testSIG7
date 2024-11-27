<%@ page import="com.rxlogix.util.ViewHelper" %>
<div style="display: none">
    <div id="executionStatusDropDown" class="form-group col-md-3">
        <label for="executionStatus">Status</label>
        <g:select name="executionStatus" from="${ViewHelper.getReportExecutionStatusEnumI18n()}"
                  optionKey="name" optionValue="display"
                  class="form-control" style="text-align-last: center; width: 70%"
                  id="executionStatus" />
    </div>
</div>
<div style="display: none">
    <div id="alertTypeDropDown" class="form-group col-md-4">
        <label for="alertType">Alert Type</label>
        <g:select name="alertType" from="${alertTypeList}"
                  optionKey="name" optionValue="display" value="${alertType}"
                  class="form-control" style="text-align-last: center; width: 60%"
                  id="alertType" />
    </div>
</div>