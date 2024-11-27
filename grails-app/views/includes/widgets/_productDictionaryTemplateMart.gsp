<div class="row prodDictFilterColCalcMart">
    <div class="col-xs-3 col-5div">
        <label><g:message code="app.reportField.productProductGroup"/></label>
        <input level="1" class="searchProducts form-control" type="text">

    </div>
    <div class="col-xs-3 col-5div">
        <label><g:message code="productDictionary.ingredient"/></label>
        <input level="2" class="searchProductsMart form-control" type="text">

    </div>

    <div class="col-xs-3 col-5div">
        <label><g:message code="productDictionary.family"/></label>
        <input level="3" class="searchProductsMart form-control" type="text">

    </div>

    <div class="col-xs-3 col-5div">
        <label><g:message code="app.reportField.productProductName"/></label>
        <input level="4" class="searchProductsMart form-control" type="text">

    </div>

    <div class="col-xs-3 col-5div">
        <label><g:message code="app.reportField.trade.name"/></label>
        <input level="5" class="searchProductsMart form-control" type="text">

    </div>

</div>

<div class="row">
    <div class="col-xs-12 host m-t-5">
        <column-view style="height: 200px;" id="columnView1" ondata="source" path="0" dic="product"
                     dictionary_type="product" tabindex="0" role="tree">
            <div id="carriage" class="style-scope column-view">
                <ul class="productDictionaryColWidthMart dicUlFormat" dictionarylevelMart="Product Group"></ul>
                <ul class="productDictionaryColWidthMart dicUlFormat" dictionarylevelMart="Ingredient"></ul>
                <ul class="productDictionaryColWidthMart dicUlFormat" dictionarylevelMart="Family"></ul>
                <ul class="productDictionaryColWidthMart dicUlFormat" dictionarylevelMart="Product Name"></ul>
                <ul class="productDictionaryColWidthMart dicUlFormat" dictionarylevelMart="Trade Name"></ul>
            </div>
        </column-view>
    </div>
</div>

%{--Show selected value as text--}%
<div class="row selectedProductDictionaryValueMart">
    <div class="col-xs-1">
        <label><g:message code="app.label.selected"/>:</label>
    </div>

    <div class="col-xs-10">
        <div class="row">
            <div class="col-xs-3">
                <label><g:message code="app.reportField.productProductGroup"/></label>
            </div>

            <div class="col-xs-9 productDictionaryValueMart level1"></div>
        </div>
        <div class="row">
            <div class="col-xs-3">
                <label><g:message code="productDictionary.ingredient"/></label>
            </div>

            <div class="col-xs-9 productDictionaryValueMart level2"></div>
        </div>

        <div class="row">
            <div class="col-xs-3">
                <label><g:message code="productDictionary.family"/></label>
            </div>

            <div class="col-xs-9 productDictionaryValueMart level3"></div>
        </div>

        <div class="row">
            <div class="col-xs-3">
                <label><g:message code="app.reportField.productProductName"/></label>
            </div>

            <div class="col-xs-9 productDictionaryValueMart level4"></div>
        </div>

        <div class="row">
            <div class="col-xs-3">
                <label><g:message code="app.reportField.trade.name"/></label>
            </div>

            <div class="col-xs-9 productDictionaryValueMart level5"></div>
        </div>

    </div>
</div>

<div class="row">
    <div class="col-xs-12">
        %{--<input type="button" class="btn btn-default" value="Clear">--}%
    </div>
</div>