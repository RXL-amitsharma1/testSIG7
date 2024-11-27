package com.rxlogix.config


class DictionaryMapping {

    Integer id
    String label
    String viewName
    String productLinkView
    String viewType
    Boolean isProduct
    String languageValue
    String code
    String labelText

    static mapping = {
        table("PVR_SEARCH_DICT")
        datasource "pva"
        version false
        id column: "ID"
        label column: "FIELD_LABEL"
        viewName column: "DISP_VIEW_NAME"
        productLinkView column: "PRODUCT_LINK_VIEW"
        isProduct column: "IS_PRODUCT"
        viewType column: "VIEW_TYPE"
        languageValue column: "LANG"
        labelText column: "JSON_COLUMN_TEXT"
    }
}
