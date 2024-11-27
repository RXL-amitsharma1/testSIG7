package com.rxlogix.mapping

import grails.util.Holders

class LmProductIngredientMapping implements Serializable{

    Long productId
    Long ingredientId


    static mapping = {
        datasources(Holders.getGrailsApplication().getConfig().pvsignal.supported.datasource.call())
        table "VW_PROD_INGRED_LINK"
        cache: "read-only"
        version false
        id composite: ["productId", "ingredientId"]
    }

    static constraints = {
        id(nullable: false, unique: true)
    }

}