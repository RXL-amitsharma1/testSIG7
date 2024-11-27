package com.rxlogix.config

import com.rxlogix.enums.ProductClassification
import com.rxlogix.util.AlertUtil
import com.rxlogix.util.DbUtil

class ProductGroup implements AlertUtil, Serializable {
    static auditable = true

    String groupName
    String productSelection
    ProductClassification classification
    Boolean display

    //Standard fields
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy

    static constraints = {
        groupName nullable: false,blank: false,unique: true
        productSelection nullable: false,blank: false
        classification nullable: false,blank: false
        display nullable: false,blank: false
    }

    static mapping = {
        productSelection column: "PRODUCTS", sqlType: DbUtil.longStringType
    }

    def getProductNameList() {
        String prdName = getNameFieldFromJson(this.productSelection)
        if (prdName) {
            prdName.toLowerCase().tokenize(',')
        } else {
            []
        }
    }

    def getProductIdList() {
        String prdName = getIdFieldFromJson(this.productSelection)
        if (prdName) {
            prdName.toLowerCase().tokenize(',')
        } else {
            []
        }
    }
    @Override
    String toString(){
        "$groupName"
    }
}
