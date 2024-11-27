package com.rxlogix.mapping

import com.rxlogix.SelectableList
import grails.util.Holders

class LmProductJ implements SelectableList {

    Long id
    LmProductFamily family
    String name

    static hasMany = [licenses:LmLicense, ingredients:LmIngredient]

    static mapping = {
        datasources(Holders.getGrailsApplication().getConfig().pvsignal.supported.datasource.call())
        table "VW_PRODUCT_DSP"

        cache: "read-only"
        version false

        id column: "PRODUCT_ID", type: "big_decimal", generator: "assigned"
        name column: "PRODUCT_NAME"
        family column: "FAMILY_ID"
        licenses joinTable: [name: "VW_PROD_LICENSE_LINK", key:"LICENSE_ID", column:"PRODUCT_ID"]
        ingredients joinTable: [name: "VW_PROD_INGRED_LINK", key: "PRODUCT_ID", column: "INGREDIENT_ID"]
    }

    static constraints = {
        id(nullable:false, unique:true)
        name(blank:false, maxSize:70)
    }

    @Override
    def getSelectableList() {
        return this.executeQuery("select distinct lms.name from LmProductJ lms order by lms.name asc")
    }
}