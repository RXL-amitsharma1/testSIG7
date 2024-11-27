<%@ page import="com.rxlogix.util.ViewHelper" %>

<g:form name="justificationForm" method="post" autocomplete="off" controller="justification" action="save" >
    <g:hiddenField name="id" id="justificationId" value=""/>
    <div class="form-group col-lg-3">
        <label>
            <g:message code="app.label.justification.name" default="Name" />
            <span class="required-indicator">*</span>
        </label>
        <input type="text" name="justificationName" id="justificationName" class="form-control" maxlength="255">
    </div>
    <div class="form-group col-lg-6 linkedDisposition">
        <label class=""><g:message code="app.label.signal.workflow.state.allowedDispositions"/></label>
        <ul class="dropdown-menu dropdown-menu-left" role="menu">
        </ul>
        <select name="linkedDisposition" id="linkedDisposition" class="form-control col-lg-3" multiple="multiple">
            <g:each in="${dispositions}" var="disposition">
                <option value="${disposition.id}" ${disposition.id in justification?.dispositions*.id ? 'selected="selected"' : ''}>${disposition.name}</option>
            </g:each>
        </select>
    </div>
    <div class="form-group col-lg-12">
        <label>
            <g:message code="app.label.justification.text" default="Justification Text" />
            <span class="required-indicator">*</span>
        </label>
        <g:textArea style="height: 150px" class=" form-control" name="justificationText" id="justificationText"
                    value="" />
    </div>
    <div class="form-group">
        <g:each in="${justificationFeatureEnumList}" var="justificationFeature">
            <div class="col-lg-3">
                <div class="checkbox checkbox-primary checkbox-inline">
                    <g:checkBox name="${justificationFeature.toString()}"/>
                    <label for="${justificationFeature}"><g:message code="label.alertWorkflow" default="${justificationFeature.val}"/> </label>
                </div>
            </div>
        </g:each>
    </div>



    <div class="col-xs-12 m-t-10">
        <input type="submit" class="btn btn-primary saveJustificationButton" style="float: right" value="${message(code: 'default.button.save.label')}"/>
    </div>
</g:form>
