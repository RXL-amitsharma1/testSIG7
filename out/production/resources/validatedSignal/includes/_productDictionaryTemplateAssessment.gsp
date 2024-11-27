<%@ page import="com.rxlogix.pvdictionary.config.PVDictionaryConfig" %>
<div class="row">
    <div class="col-xs-2 padding-md-bottom">
        <label><g:message code="app.productDictionary.product.group"/></label>
    </div>
    <div class="col-xs-10 padding-md-bottom">
        <select id="productGroupSelectAssessment" class="form-control" style="width: 100%"></select>
    </div>
</div>
<div class="row">
    <div class="${filters.size() <= 6 ? "" : "scrollableView1 p-b-10"}">
        <g:each var="filter" in="${filters}" status="i">

            <div class="${filters.size() <=6 ? (filters.size() == 1 ? "prodDictSearchColCalcOne" :"prodDictSearchColCalc") : "prodDictFilterCol"}" style="padding-left: 5px">
                <label style="${filters.size() == 1 ? "width: 16.66%":""}"><g:message code="${filter.code}"/></label>

                <div style="${filters.size() == 1? "width: 32.5%":""}">
                <g:if test="${filter.type == 'SELECTBOX'}">
                    <g:if test="${PVDictionaryConfig.select2v4}">
                        <select level="${i + 1 + views.size()}" name="${filter.name}"
                                class="form-control searchProducts additionalCriteria dictionary-select"
                                data-url="${createLink(controller: 'productDictionary', action: 'getFilterList', params: [name: filter.view])}"></select>
                    </g:if><g:else>
                    <g:hiddenField level="${i + 1 + views.size()}" name="${filter.name}"
                                   class="form-control searchProducts additionalCriteria dictionary-select"
                                   data-url="${createLink(controller: 'productDictionary', action: 'getFilterList', params: [name: filter.view])}"/>
                </g:else>
                </g:if>
                <g:elseif test="${filter.type == 'CHECKBOX'}">
                    <g:checkBox name="${filter.name}" level="${i + 1 + views.size()}"
                                class="form-control searchProducts additionalCriteria"/>
                </g:elseif>
                <g:elseif test="${filter.type == 'INPUT'}">
                    <input type="text" name="${filter.name}" level="${i + 1 + views.size()}"
                           class="form-control searchProducts additionalCriteria"/>
                </g:elseif>
                <g:elseif test="${filter.type == 'DATE'}">
                    <div class="fuelux">
                        <div>
                            <div class="datepicker toolbarInline">
                                <div class="input-group ">
                                    <g:textField name="${filter.name}"
                                                 class="form-control searchProducts additionalCriteria dictionary-datepicker"/>
                                    <g:render class="datePickerIcon"
                                              template="/plugin/dictionary/datePickerTemplate"/>
                                </div>
                            </div>
                        </div>
                    </div>
                </g:elseif>
            </div>
            </div>
        </g:each>
    </div>
</div>


<div class="row"  style="padding-top: 1%" hidden>
    <div class="col-xs-2">
        <label><g:message code="app.productDictionary.datasource"/>
    </div>
    <div class="col-xs-4">
        <g:select id="dataSourcesProductDictAssessment"
                  name="dataSourcesProductDict"
                  from="${PVDictionaryConfig.dataSources}"
                  optionKey="key"
                  optionValue="value"
                  class="form-control"/>
    </div>
</div>
<div class="row" style="margin-top: 1%">
    <div class="col-xs-5 padding-md-bottom" style="width: 140px;">
        <div class="checkbox checkbox-primary">
            <g:checkBox id="product_exactSearchAssessment" name="product_exactSearch"/>
            <label for="product_exactSearchAssessment">
                <g:message code="app.label.dictionary.exact.search"/>
            </label>
        </div>
    </div>
    <div class="col-xs-5 padding-md-bottom" style="width: 140px;">
        <div class="checkbox checkbox-primary">
            <g:checkBox id="product_multiIngredient_assessment" name="isMultiIngredient" value="${multiIngredientValue}"/>
            <label for="product_multiIngredient_assessment" tabindex="0">
                <g:if test="${isPVCM}">
                    <g:message code="app.label.productDictionary.multi.substance"/>
                </g:if>
                <g:else>
                    <g:message code="app.label.productDictionary.multi.ingredient"/>
                </g:else>
            </label>
        </div>
    </div>
</div>

<div class="${column_names.size() <=6 ? "m-b-15 scroll-hidden" : "m-b-15 scroll-hidden scrollableView1"}">

    <g:each var="viewEntry" in="${views}" status="i">

        <div class="${column_names.size() <=6 ? "prodDictFilterColCalc padding-md-bottom" : "prodDictFilterCol"}">
            <label><g:message code="${viewEntry.code}"/><i tabindex="0"
                                                           class="fa fa-pencil-square-o copy-n-paste modal-link" data-toggle="modal"
                                                           data-target="#copyAndPasteDicModal"></i></label>
            <input level="${i + 1}"
                   class="searchProducts form-control ${viewEntry.code == productColumnCode ? 'productColumn' : ''}"
                   type="text">
        </div>
    </g:each>
</div>

<div class="row">
    <div class="col-xs-12">
        <column-view id="columnView1" class="${column_names.size() <=6 ? "" : "scrollableView2"}" ondata="source" path="0" dic="product"
                     dictionary_type="product" columns_name="${column_names.join(',')}"></column-view>
    </div>
</div>

%{--Show selected value as text--}%
<div class="row selectedProductDictionaryValueAssessment">
    <div class="col-xs-1">
        <label><g:message code="app.label.selected"/>:</label>
    </div>

    <div class="col-xs-10">
        <div class="row">
            <div class="col-xs-3">
                <label><g:message code="app.productDictionary.product.group"/></label>
            </div>
            <div class="col-xs-9 level-product-group-assessment"></div>
        </div>
        <g:each var="viewEntry" in="${views}" status="i">
            <div class="row">
                <div class="col-xs-3">
                    <label><g:message code="${viewEntry.code}"/></label>
                </div>

                <div class="col-xs-9 productDictionaryValueAssessment level${i + 1}"></div>
            </div>
        </g:each>
    </div>
</div>

<div class="row">
    <div class="col-xs-12">
    </div>
</div>

<asset:javascript src="app/pvs/validated_signal/assessment_product_group.js"/>
<asset:javascript src="/plugin/dictionary/productDictionary.js"/>
<style>
.prodDictFilterColCalc ,.productDictionaryColWidthCalc {
    width: calc(100% /${column_names.size()});
}
.prodDictSearchColCalc ,.prodDictSearchColCalcOne {
    width: calc(100% /${filters.size()});
}
</style>
