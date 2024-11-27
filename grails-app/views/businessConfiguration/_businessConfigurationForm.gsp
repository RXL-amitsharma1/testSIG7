<div class="row">

    <div class="col-md-3">

        <label for="datasource">
            Data Source
            <span class="required-indicator">*</span>
        </label>

        <g:if test="${businessConfiguration?.id}">
            <div id="datasource">
                ${datasourceMap.containsKey(businessConfiguration.dataSource) ? datasourceMap[businessConfiguration.dataSource] : businessConfiguration.dataSource}
                <input class="selectedDatasource" type="hidden" name="dataSource" value="${businessConfiguration.dataSource}" />
            </div>
        </g:if>
        <g:else>
            <div id="datasource">
                <g:select name="dataSource" class="form-control selectedDatasource"
                          from="${datasourceMap.entrySet()}" optionValue="value"
                          optionKey="key"
                          value="${businessConfiguration?.dataSource?:'pva'}"/>
            </div>
        </g:else>
    </div>

    <div class="col-md-3">

        <label for="ruleName">
            <g:message code="app.label.business.configuration.ruleName"/>
            <span class="required-indicator">*</span>
        </label>
        <div>
            <g:textField id="ruleName" name="ruleName" class="form-control required" value="${businessConfiguration?.ruleName}" maxlength="255"/>
        </div>

    </div>

    <g:if test="${!isGlobalRule}">
        <div class="col-md-3">
            <label>
                <g:message code="app.label.productSelection"/>
                <span class="required-indicator">*</span>
            </label>

            <div class="wrapper">
                <div id="showProductSelection" class="showDictionarySelection"></div>

                <div class="iconSearch">
                    <i class="fa fa-search productRadio" data-toggle="modal"
                       data-target="#productModal"></i>
                </div>
            </div>
            <g:textField class="productSelection" name="productSelection"
                         value="${businessConfiguration?.productSelection}" hidden="hidden"/>
            <g:hiddenField name="productGroupSelection" id="productGroupSelection" value="${businessConfiguration?.productGroupSelection}"/>
            <g:hiddenField name="isMultiIngredient" id="isMultiIngredient" value="${businessConfiguration?.isMultiIngredient}"/>
        </div>
    </g:if>


    <div class="col-md-3">
        <label for="description"><g:message code="app.label.reportDescription"/></label>
        <g:textArea style="height: 100px;" name="description" id = "brDescription"
                    class="form-control">${businessConfiguration?.description}</g:textArea>
    </div>
</div>

<div class="row">
    <div class="col-md-2">
        <g:submitButton name="saveBusinessConfigButton" class="btn btn-primary" onsubmit="return onSubmitBusinessConfigForm()" value="${businessConfiguration?.id ? "Update":"Save"}"/>
        <g:link name="cancelButton" controller="businessConfiguration" action="index" class="btn btn-default">Cancel</g:link>
    </div>
</div>