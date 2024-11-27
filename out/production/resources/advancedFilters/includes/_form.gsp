<%@ page import="com.rxlogix.util.ViewHelper" %>

<g:form name="advancedFilterForm" method="post" autocomplete="off" controller="advancedFilter"
        action="save">

    <input type="hidden" name="alertType" value="${appType}"/>
    <input type="hidden" name="icrAlertType" value="${icrAlertType}"/>
    <input type="hidden" name="miningVariable" value="${miningVariable}"/>
    <input type="hidden" name="filterId" id="filterId" value=""/>

    <div class="form-group row">
        <div class="col-md-4">
            <label for="name"><g:message code="app.label.advanced.filters.label.name"/><span
                    class="required-indicator">*</span></label>
            <input type="text" class="form-control" name="name" id="name" maxlength="255">
        </div>
        <g:if test="${isShareFilterViewAllowed}">
            <div class="col-md-4 mw-110" id="shareAdvFilterId">
                <g:initializeSharedWithElement shareWithId="advancedFilterSharedWith" isWorkflowEnabled="false"/>
            </div>
        </g:if>
    </div>

    <div class="form-group">
        <label for="description"><g:message
                code="app.label.advanced.filters.label.description"/></label>
        <textarea class="form-control" rows="5" name="description" id="description" maxlength="8000"></textarea>
    </div>


    <div class="form-group loading">
        <i class="fa fa-refresh fa-spin"></i>
    </div>

    <div class="form-group doneLoading clearfix addContainerTopmost" id="addContainerWithSubmit">
        <label>Filter Criteria<span class="required-indicator">*</span></label>
        <g:render template="/advancedFilters/toAddFilter" model="[fieldList: fieldList]"/>
    </div>

    <div hidden="hidden">
        <input name="JSONQuery" id="queryJSON" value="${null}"/>
        <a href="http://jsonlint.com/" target="_blank" rel="noopener noreferrer">Prettify my JSON here!</a>

        <div>has blanks?
            <input name="hasBlanks" id="hasBlanksQuery" value=""/>
        </div>
    </div>

    <div class="form-group">
        <div class="loading container">
            <i class="fa fa-refresh fa-spin"></i>
        </div>

        <div id="builderAll" class="builderAll doneLoading p-lr-0">
        </div>
    </div>

    <div class="col-xs-12 m-t-10">
        <div style="text-align: right">
            <input type="submit" class="btn btn-primary saveAdvancedFilters"
                   value="${message(code: 'default.button.save.label')}" id="btnSubmit"/>
            <button class="btn btn-primary filtersWithoutSaving">${message(code: 'default.button.filter.without.saving')}</button>
            <button class="btn btn-primary deleteAdvFilter hide">${message(code: 'default.button.filter.delete')}</button>
            <button class="button btn btn-default pv-btn-grey" data-dismiss="modal"
                    id="cancel-bt">${message(code: "default.button.cancel.label")}</button>
        </div>
    </div>
</g:form>
