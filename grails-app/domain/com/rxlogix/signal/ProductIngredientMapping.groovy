package com.rxlogix.signal

import com.rxlogix.Constants
import com.rxlogix.user.User
import com.rxlogix.util.AlertUtil
import com.rxlogix.util.DbUtil

class ProductIngredientMapping implements GroovyInterceptable, AlertUtil {

    static auditable = true

    String otherDataSource
    String productSelection
    String pvaProductSelection
    Boolean enabled = true

    //Common db table fields.
    String createdBy
    Date dateCreated
    Date lastUpdated
    String modifiedBy
    def dataObjectService

    static mapping = {
        table("PRODUCT_INGREDIENT_MAPPING")
        productSelection column: "PRODUCTS", sqlType: DbUtil.longStringType
        pvaProductSelection column: "PVA_PRODUCTS", sqlType: DbUtil.longStringType
    }

    static constraints = {
        otherDataSource nullable: false, blank: false
        productSelection nullable: false, blank: false
        pvaProductSelection nullable: false, blank: false
    }

    def toDto() {
        [
                id             : this.id,
                otherDataSource: this.otherDataSource == Constants.DataSource.EUDRA
                        ? Constants.DataSource.EVDAS : this.otherDataSource,
                lastModified   : this.lastUpdated,
                modifiedBy     : User.findByUsername(this.modifiedBy)?.fullName,
                products       : getProductNameList(this.productSelection),
                pvaProducts    : getProductNameList(this.pvaProductSelection),
                enabled        : this.enabled,
                level          : getLevel(this.pvaProductSelection)
        ]
    }

    def getProductNameList(String productJson) {
        String prdName = getNameFieldFromJson(productJson)
        if (prdName) {
            prdName.tokenize(',')
        } else {
            []
        }
    }

    String getLevel(String productJson) {
        Map dicLevelMap = dataObjectService.getIdLabelMap()
        Map jsonMap = parseJsonString(productJson)
        def level = jsonMap.find { it.value }?.key
        dicLevelMap.get(level)
    }

}
