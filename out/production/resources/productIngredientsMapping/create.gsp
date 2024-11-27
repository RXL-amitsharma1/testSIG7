<%@ page import="com.rxlogix.pvdictionary.config.PVDictionaryConfig;grails.util.Holders;com.rxlogix.util.ViewHelper; grails.plugin.springsecurity.SpringSecurityUtils" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.ProductIngredientMapping.title"/></title>
    <g:if test="${!Holders.config.pv.plugin.dictionary.enabled}">
        <asset:stylesheet src="dictionaries.css"/>
    </g:if>
    <asset:javascript src="app/pvs/productIngredientMapping/ingredient_mapping.js"/>
    <g:if test="${!Holders.config.pv.plugin.dictionary.enabled}">
        <asset:javascript src="app/pvs/alert_utils/alert_product_utils.js"/>
    </g:if>
    <g:else>
        <asset:javascript src="app/pvs/alert_utils/dictionary-utils.js"/>
        <asset:javascript src="app/pvs/alert_utils/multi_datasource_dictionary.js"/>
        <asset:stylesheet src="copyPasteModal.css"/>
    </g:else>
    <asset:stylesheet src="configuration.css"/>

    <g:javascript>
         function checkMultipleHierarchy() {
        $("button.addProductValues, button.addAllProductValues").off("click").click(function (e) {
        var selectedProduct = $(".productDictionaryColWidthCalc").find(".dicLi.selected.selectedBackground")
        var level = selectedProduct.attr("dictionarylevel")
        if (level == undefined) {
            $('.searchProducts').each(function () {
                if ($(this).val()) {
                    level = $(this).attr('level')
                }
            });
        }
        var dictLevel = $(".productDictionaryValue.level" + level).find(".dictionaryItem")
        var allLevel = $(".productDictionaryValue").find(".dictionaryItem")
        if (dictLevel.size() != allLevel.size() && level != undefined) {
            e.stopPropagation();
            showErrorMsgInDialog("Product grouping can only be configured on the same level of product hierarchy")
        }
    });
}
        function showErrorMsgInDialog(msg) {
            removeExistingMessageHolderInDialog();
            var alertHtml = getErrorMessageHtml(msg);
            $("#productModal .modal-body").prepend(alertHtml);
            $(".alert-danger").delay(5000).fadeOut('slow')
        }

        function removeExistingMessageHolderInDialog() {
            $('.messageContainerForDialog').html("");
        }

        function getErrorMessageHtml(msg) {
            var alertHtml = '<div class="alert alert-danger alert-dismissible" role="alert"> ' +
                            '<button type="button" class="close" data-dismiss="alert"> ' +
                            '<span aria-hidden="true">&times;</span> ' +
                            '<span class="sr-only"><g:message code="default.button.close.label"/></span> ' +
                            '</button> ' + msg +
                            '</div>';
            return alertHtml;
        }

        var options = { spinnerPath:"${assetPath(src: 'select2-spinner.gif')}" };


        options.product = {
            levelNames: "${PVDictionaryConfig.ProductConfig.views.collect { message(code: it.code) }.join(",")}",
            dicColumnCount: ${com.rxlogix.pvdictionary.config.PVDictionaryConfig.ProductConfig.columns.size()},
            selectUrl: "${createLink(controller: 'productDictionary', action: 'getSelectedItem')}",
            preLevelParentsUrl: "${createLink(controller: 'productDictionary', action: 'getPreLevelProductParents')}",
            searchUrl: "${createLink(controller: 'productDictionary', action: 'searchViews')}"
        };

    </g:javascript>
</head>

<body>
<g:render template="/includes/layout/flashErrorsDivs" bean="${productIngredientMapping}" var="theInstance"/>
<rx:container title="Product/Ingredients Mapping List" bean="${error}">

    <div class="messageContainer"></div>

    <g:form controller="productIngredientsMapping" action="save">
        <g:hiddenField name="productIngredientId" value="${productIngredientMapping?.id}"/>
        <div class="row">
            <div class="col-md-6">
                <div class="panel panel-default">
                    <div class="panel-body">
                        <div class="row">
                            <div class="col-md-2"><strong>Other Data Source</strong></div>

                            <div class="col-md-4">

                                <g:select name="otherDataSource" class="form-control selectedDatasourceIngredient"
                                          id="selectedDatasourceIngredient"
                                          from='${dataSourceMap?.entrySet()}' optionValue="value"
                                          optionKey="key"
                                          value="${productIngredientMapping?.otherDataSource ?: 'eudra'}"/>
                            </div>
                        </div>

                        <div class="row m-t-10">
                            <div class="col-md-12">
                                <label>
                                    <g:message code="app.label.productSelection"/>
                                    <span class="required-indicator">*</span>
                                </label>

                                <div class="wrapper">
                                    <div id="showProductSelectionIngredient" class="showDictionarySelection">
                                    </div>

                                    <div class="iconSearch">
                                        <a tabindex="0" id="searchProductsIngredient" data-toggle="modal"
                                           data-target="#productModalIngredient" class="productRadio">
                                            <i class="fa fa-search "></i>
                                        </a>
                                    </div>
                                </div>
                                <g:textField class="productSelection" name="productSelectionIngredient"
                                             value="${productIngredientMapping?.productSelection}" hidden="hidden"/>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-md-6">
                <div class="panel panel-default">
                    <div class="panel-body">
                        <div class="row">
                            <div class="col-md-2"><strong>Company Data</strong></div>
                            <g:hiddenField name="selectedDatasource" class="form-control selectedDatasource"
                                           id="selectedDatasource"
                                           value="${com.rxlogix.Constants.DataSource.PVA}"/>
                        </div>

                        <div class="row m-t-18">
                            <div class="col-md-12">
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
                                             value="${productIngredientMapping?.pvaProductSelection}" hidden="hidden"/>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>


        <div class="row">
            <div class="col-md-12">
                <input type="submit" value="${productIngredientMapping?.id ? 'Update' : 'Save'}"
                       class="btn btn-primary"/>
                <g:link controller="productIngredientsMapping" action="index" class="btn btn-default">Cancel</g:link>
            </div>
        </div>
    </g:form>
</rx:container>
<g:render template="includes/ingredient_product_selection_modal"/>
<g:if test="${grails.util.Holders.config.pv.plugin.dictionary.enabled}">
    <input type="hidden" id="editable" value="true">
    <g:render template="/plugin/dictionary/dictionaryModals" plugin="pv-dictionary"
              model="[filtersMapList: Holders.config.product.dictionary.filtersMapList, viewsMapList: Holders.config.product.dictionary.viewsMapList]"/>
    <g:render template="/configuration/copyPasteModal"/>
</g:if>
<g:else>
    <g:render template="/includes/modals/event_selection_modal"/>
    <g:render template="/includes/modals/product_selection_modal"/>
</g:else>
<g:if test="${Holders.config.pv.plugin.dictionary.enabled}">
    <script>
        var pvaUrls = {
            selectUrl: options.product.selectUrl,
            preLevelParentsUrl: options.product.preLevelParentsUrl,
            searchUrl: options.product.searchUrl
        };
        var otherUrls = {
            selectUrl: "${createLink(controller: 'pvsProductDictionary', action: 'getSelectedProduct')}",
            preLevelParentsUrl: "${createLink(controller: 'pvsProductDictionary', action: 'getPreLevelProductParents')}",
            searchUrl: "${createLink(controller: 'pvsProductDictionary', action: 'searchProducts')}"
        };
        changeDataSource("${businessConfiguration?.dataSource?:'pva'}");
    </script>
</g:if>
<script>
    addColumnClass();
</script>
</body>