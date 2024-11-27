package com.rxlogix.mapping

import com.rxlogix.SelectableList
import grails.util.Holders

class LmIngredient implements SelectableList {

    Long id
    String ingredient
    String lowerIngredient

    static hasMany = [family : LmProductFamily]

    static mapping = {
        datasources(Holders.getGrailsApplication().getConfig().pvsignal.supported.datasource.call())
        table "VW_INGREDIENT_DSP"

        cache: "read-only"
        version false
        id column: "INGREDIENT_ID", generator: "assigned"
        ingredient column: "INGREDIENT"
        lowerIngredient formula: "lower(INGREDIENT)"
        family joinTable: [name: "VW_LPI_FAMILY_ID_DSP", key:"INGREDIENT_ID", column:"PROD_FAMILY_ID"]
    }

    static constraints = {
        id(nullable:false, unique:true)
        ingredient(blank:false, maxSize:120)
    }

    @Override
    def getSelectableList() {
        LmIngredient.withTransaction {
            return this.executeQuery("select distinct c.ingredient from LmIngredient c order by c.ingredient asc")
        }
    }
}
