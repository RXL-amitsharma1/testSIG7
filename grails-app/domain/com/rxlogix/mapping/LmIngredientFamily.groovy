package com.rxlogix.mapping

import grails.util.Holders

class LmIngredientFamily implements Serializable {

    LmIngredient ingredient
    LmProductFamily family

    static mapping = {
        datasources(Holders.getGrailsApplication().getConfig().pvsignal.supported.datasource.call())
        table "VW_LPI_FAMILY_ID_DSP"
        ingredient column: "INGREDIENT_ID"
        family column: "PROD_FAMILY_ID"
        id composite:["ingredient","family"]
        cache: "read-only"
        version false
    }
}