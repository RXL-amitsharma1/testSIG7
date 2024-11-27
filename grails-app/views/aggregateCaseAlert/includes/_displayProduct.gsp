<%@ page import="com.rxlogix.pvdictionary.config.PVDictionaryConfig; com.rxlogix.Constants; com.rxlogix.dto.SpotfireSettingsDTO; com.rxlogix.enums.ProductClassification; grails.converters.JSON; com.rxlogix.enums.DictionaryTypeEnum; com.rxlogix.enums.DateRangeEnum; com.rxlogix.util.DateUtil; com.rxlogix.config.ExecutedConfiguration; com.rxlogix.util.ViewHelper; com.rxlogix.enums.DateRangeTypeCaseEnum;  com.rxlogix.config.DateRangeValue; com.rxlogix.enums.DrugTypeEnum ; grails.util.Holders" %>
<%@ page import="com.rxlogix.enums.EvaluateCaseDateEnum" %>
<div class="row">
    <div class="col-xs-12 cell-break">
        <g:if test="${configurationInstance.productSelection || configurationInstance.productGroupSelection || configurationInstance.dataMiningVariable || configurationInstance.productHierarchyMasterTmplt}">
            <label><g:message code="app.reportField.productDictionary"/></label>
            <g:if test="${configurationInstance.dataMiningVariable && configurationInstance.productSelection}">
                <div id="showProductSelection"></div>
                ${ViewHelper.getProductDictionaryValues(configurationInstance, DictionaryTypeEnum.PRODUCT)}
            </g:if>
            <g:elseif test="${configurationInstance.dataMiningVariable && configurationInstance.productGroupSelection}">
                <div id="showProductSelection"></div>
                ${ViewHelper.getDictionaryValues(configurationInstance, DictionaryTypeEnum.PRODUCT_GROUP)}
            </g:elseif>
            <g:elseif test="${configurationInstance.dataMiningVariable}">
                <div id="showProductSelection"></div>
                ${configurationInstance.dataMiningVariable}
            </g:elseif>
            <g:elseif test="${configurationInstance.productSelection}">
                <div id="showProductSelection"></div>
                ${ViewHelper.getProductDictionaryValues(configurationInstance, DictionaryTypeEnum.PRODUCT)}
            </g:elseif>
            <g:elseif test="${configurationInstance.productGroupSelection}">
                <div id="showProductGroupSelection"></div>
                ${ViewHelper.getDictionaryValues(configurationInstance, DictionaryTypeEnum.PRODUCT_GROUP)}
            </g:elseif>
            <g:elseif test="${configurationInstance.isMasterTemplateAlert && configurationInstance.productHierarchyMasterTmplt}">
                <div id="showProductGroupSelection"></div>
                ${ViewHelper.getProductHierarchyData(configurationInstance.productHierarchyMasterTmplt)}
            </g:elseif>
        </g:if>
    </div>
</div>