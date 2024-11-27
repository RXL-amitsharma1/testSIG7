<%@ page import="com.rxlogix.pvdictionary.config.PVDictionaryConfig" %>
<div class="${filters.size() <= 6 ? "row" : "row scrollableView1 p-b-10"}">
    <g:each var="filter" in="${filters}" status="i">

        <div class="${filters.size() <= 6 ? "prodDictSearchColCalc" : "prodDictFilterCol"}">
            <label><g:message code="${filter.code}"/></label>

            <div>
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
                                    <g:render class="datePickerIcon" template="/plugin/dictionary/datePickerTemplate"/>
                                </div>
                            </div>
                        </div>
                    </div>
                </g:elseif>
            </div>
        </div>

    </g:each>
</div>

<div class="row">
    <div class="col-xs-5 padding-md-bottom">
        <div class="checkbox checkbox-primary">
            <g:checkBox id="ingredient_product_exactSearch" name="ingredient_product_exactSearch"/>
            <label for="ingredient_product_exactSearch">
                <g:message code="app.label.dictionary.exact.search"/>
            </label>
        </div>
    </div>
</div>

<div class="${column_names.size() <= 6 ? "m-b-15 scroll-hidden" : "m-b-15 scroll-hidden scrollableView1"}">

    <g:each var="viewEntry" in="${views}" status="i">

        <div class="${column_names.size() <= 6 ? "prodDictFilterColCalc ingredientCol padding-md-bottom" : "prodDictFilterCol"}">
            <label><g:message code="${viewEntry.code}"/></label>
            <input level="${i + 1}" id="ingredientSearch"
                   class="productValuesIngredient form-control ${viewEntry.code == productColumnCode ? 'productColumn' : ''}"
                   type="text">
        </div>
    </g:each>
</div>

<div class="row">
    <div class="col-xs-12">
        <column-view id="columnView1Ingredient" class="${column_names.size() <= 6 ? "" : "scrollableView2"}" ondata="source"
                     path="0" dic="product"
                     dictionary_type="product" columns_name="${column_names.join(',')}">
            <div id="carriage" class="style-scope column-view">
                <ul class="productDictionaryColWidthCalc dicUlFormat" dictionaryLevel="Ingredient"></ul>
                <ul class="productDictionaryColWidthCalc dicUlFormat" dictionaryLevel="Ingredient"></ul>
                <ul class="productDictionaryColWidthCalc dicUlFormat" dictionaryLevel="Family"></ul>
                <ul class="productDictionaryColWidthCalc dicUlFormat" dictionaryLevel="Product Name"></ul>
                <ul class="productDictionaryColWidthCalc dicUlFormat" dictionaryLevel="Trade Name"></ul>
            </div>
        </column-view>
    </div>
</div>

%{--Show selected value as text--}%
<div class="row selectedProductDictionaryValueIngredient">
    <div class="col-xs-1">
        <label><g:message code="app.label.selected"/>:</label>
    </div>

    <div class="col-xs-10">
        <g:each var="viewEntry" in="${views}" status="i">
            <div class="row">
                <div class="col-xs-3">
                    <label><g:message code="${viewEntry.code}"/></label>
                </div>

                <div class="col-xs-9 productDictionaryValueIngredient level${i + 1}"></div>
            </div>
        </g:each>
    </div>
</div>

<div class="row">
    <div class="col-xs-12">
    </div>
</div>
<asset:javascript src="/plugin/dictionary/productDictionary.js"/>
<style>
.prodDictFilterColCalc, .productDictionaryColWidthCalc {
    width: calc(100% /${column_names.size()});
}

.prodDictSearchColCalc {
    width: calc(100% /${filters.size()});
}
</style>
