package com.rxlogix.mapping

import com.rxlogix.SelectableList
import com.rxlogix.util.DbUtil
import grails.util.Holders

class LmProduct implements SelectableList {

    Long id
    LmProductFamily family
    String name
    String genericName

    static hasMany = [licenses : LmLicense, ingredients : LmIngredient]

    static mapping = {
        datasources(Holders.getGrailsApplication().getConfig().pvsignal.supported.datasource.call())
        table "VW_PRODUCT_DSP"

        cache: "read-only"
        version false

        id column: "PRODUCT_ID", generator: "assigned"
        name column: "PRODUCT_NAME"
        family column: "PROD_FAMILY_ID"
        genericName column: "PROD_GENERIC_NAME", sqlType: DbUtil.longStringType
        licenses joinTable: [name: "VW_PROD_LICENSE_LINK_DSP", key:"PRODUCT_ID", column:"LICENSE_ID"]
        ingredients joinTable: [name: "VW_PROD_INGRED_LINK", key: "PRODUCT_ID", column: "INGREDIENT_ID"]
    }

    static constraints = {
        id(nullable:false, unique:true)
        name(blank:false, maxSize:70)
    }

    @Override
    def getSelectableList() {
        LmProduct.withTransaction {
            return this.executeQuery("select distinct lmp.name from LmProduct lmp order by lmp.name asc")
        }
    }
}
