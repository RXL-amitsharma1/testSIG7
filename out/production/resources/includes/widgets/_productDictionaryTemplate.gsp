<div class="row prodDictFilterColCalc">
    <div class="col-xs-3">
        <label id="productDicIng"><g:message code="productDictionary.ingredient" /></label>
        <input level="1" class="searchProducts form-control" type="text">

    </div>
    <div class="col-xs-3">
        <label><g:message code="productDictionary.family" /></label>
        <input level="2" class="searchProducts form-control" type="text">

    </div>
    <div class="col-xs-3">
        <label><g:message code="app.reportField.productProductName"/></label>
        <input level="3" class="searchProducts form-control" type="text">

    </div>
    <div class="col-xs-3">
        <label><g:message code="app.reportField.trade.name" /></label>
        <input level="4" class="searchProducts form-control" type="text">

    </div>

</div>

<div class="row">
    <div class="col-xs-12 host m-t-5">
        <column-view style="height: 200px;" id="columnView1" ondata="source" path="0" dic="product" dictionary_type="product" tabindex="0" role="tree">
            <div id="carriage" class="style-scope column-view" columns_name="${column_names.join(',')}">
                <ul class="productDictionaryColWidth dicUlFormat" dictionaryLevel="Ingredient"></ul>
                <ul class="productDictionaryColWidth dicUlFormat" dictionaryLevel="Family"></ul>
                <ul class="productDictionaryColWidth dicUlFormat" dictionaryLevel="Product Name"></ul>
                <ul class="productDictionaryColWidth dicUlFormat" dictionaryLevel="Trade Name"></ul>
            </div>
        </column-view>
    </div>
</div>

%{--Show selected value as text--}%
<div class="row selectedProductDictionaryValue">
    <div class="col-xs-1">
        <label><g:message code="app.label.selected"/>:</label>
    </div>
    <div class="col-xs-10">
        <div class="row">
            <div class="col-xs-3">
                <label><g:message code="productDictionary.ingredient" /></label>
            </div>
            <div class="col-xs-9 productDictionaryValue level1"></div>
        </div>
        <div class="row">
            <div class="col-xs-3">
                <label><g:message code="productDictionary.family" /></label>
            </div>
            <div class="col-xs-9 productDictionaryValue level2"></div>
        </div>
        <div class="row">
            <div class="col-xs-3">
                <label><g:message code="app.reportField.productProductName"/></label>
            </div>
            <div class="col-xs-9 productDictionaryValue level3"></div>
        </div>
        <div class="row">
            <div class="col-xs-3">
                <label><g:message code="app.reportField.trade.name" /></label>
            </div>
            <div class="col-xs-9 productDictionaryValue level4"></div>
        </div>

    </div>
</div>
<div class="row">
    <div class="col-xs-12">
        %{--<input type="button" class="btn btn-default" value="Clear">--}%
    </div>
</div>
<style>
.productDictionaryColWidth {
    width: calc(100%/${column_names.size()}) !important;
}
</style>
