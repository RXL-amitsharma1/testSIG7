package com.rxlogix

import com.rxlogix.mapping.LmIngredient
import com.rxlogix.mapping.LmProduct
import com.rxlogix.pvdictionary.config.PVDictionaryConfig
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.util.Holders
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject

@Secured(["isAuthenticated()"])
class PvsProductDictionaryController {

    def userService
    def pvsProductDictionaryService
    def productBasedSecurityService
    DataObjectService dataObjectService

    /**
     * Main action that is hit when any product/family/ingredient/license is selected in the dictionary.
     * @return
     */
    def searchProducts() {
        if(params.id) params.selectedDatasource = params.id

        log.info("Inside the searchProducts method..........")
        log.info("The selected datasource value coming is : "+ params.selectedDatasource)

        if (params.selectedDatasource == 'eudra') {
            searchEudraSubstances()
        } else {
            def allowedProductsToUser = productBasedSecurityService.allAllowedProductForUser(userService.getUser())
            def productList = pvsProductDictionaryService.getProductInstance(params.dictionaryLevel, null, params.contains, params.selectedDatasource, allowedProductsToUser)
            JSONArray showElements = new JSONArray()
            productList?.each {
                JSONObject item = new JSONObject()
                item.level = params.dictionaryLevel
                item.id = it.id
                item.name = getNameFieldForProduct(it)
                item.genericName = getGenericNameForProduct(it)
                showElements.add(item)
            }
            respond showElements, [formats: ['json']]
        }
    }

    def searchEudraSubstances() {
        def searchTerm = params.contains
        def lmIngredients
        JSONArray showElements
        def dataSourceValue = "pva"

        if(Holders.config.signal.evdas.enabled) {
            dataSourceValue = "eudra"
        }
        List dictionaryList = Holders.config.pvsignal.evdas.dictionary.list

        if (dictionaryList.get(params.dictionaryLevel.toInteger()-1) == "Ingredient") {

            lmIngredients = LmIngredient."${dataSourceValue}".withTransaction {
                LmIngredient."${dataSourceValue}".findAllByIngredientIlike('%' + searchTerm + '%', [sort: "ingredient", order: "asc"])
            }
            showElements = new JSONArray()
            LmIngredient."${dataSourceValue}".withTransaction {
                lmIngredients?.each {
                    JSONObject item = new JSONObject()
                    item.level = params.dictionaryLevel
                    item.id = it.id
                    item.name = it.ingredient
                    item.genericName = null
                    showElements.add(item)
                }
            }
        } else {

            lmIngredients = LmProduct."${dataSourceValue}".withTransaction {
                LmProduct."${dataSourceValue}".findAllByNameIlike('%' + searchTerm + '%', [sort: "name", order: "asc"])
            }
            showElements = new JSONArray()
            LmIngredient."${dataSourceValue}".withTransaction {
                lmIngredients?.each {
                    JSONObject item = new JSONObject()
                    item.level = params.dictionaryLevel
                    item.id = it.id
                    item.name = it.name
                    item.genericName = it.genericName
                    showElements.add(item)
                }
            }
        }
        respond showElements, [formats: ['json']]
    }

    /**
     * Action that is hit when any of the component option is selected from UI and we need to fetch the all other associated components.
     * @return
     */
    def getSelectedProduct() {
        if(params.id ) {//means that request from page with multi dataSources and it is not pva
            def empty = ['id': params.productId, 'name': "", 'nextLevelItems': [], 'genericName': '']
            render empty as JSON //for non pva datasources function forbidden
            return
        }

        def allowedProductsToUser = productBasedSecurityService.allAllowedProductForUser(userService.getUser())
        def product = pvsProductDictionaryService.getProductInstance(params.dictionaryLevel, params.productId, null , params.selectedDatasource, allowedProductsToUser)

        if (product instanceof List) {
            def values = [:]
            render values as JSON
        } else {
            int level = Integer.parseInt(params.dictionaryLevel)
            def name = getNameFieldForProduct(product)
            def nextLevelItems = pvsProductDictionaryService.getChildProducts(product, level, params.selectedDatasource, allowedProductsToUser)
            def values = ['id': product.id, 'name': name, 'nextLevelItems': nextLevelItems, 'genericName': getGenericNameForProduct(product)]
            render values as JSON
        }
    }

    def getPreLevelProductParents() {
        if(params.id ) {//means that request from page with multi dataSources and it is not pva
            def empty = [:]
            render empty as JSON //for non pva datasources function forbidden
            return
        }
        def allowedProductsToUser = productBasedSecurityService.allAllowedProductForUser(userService.getUser())
        int level = Integer.parseInt(params.dictionaryLevel)
        JSONArray parents = new JSONArray()
        def selectedDatasource = params.selectedDatasource
        if (selectedDatasource != Constants.DataSource.FAERS) {
            params.productIds?.split(",")?.each {
                def product = pvsProductDictionaryService.getProductInstance(params.dictionaryLevel, it, null, params.selectedDatasource, allowedProductsToUser)
                pvsProductDictionaryService.getParentProducts(product, level, parents, params.selectedDatasource, allowedProductsToUser)
            }
        }
        respond parents, [formats: ['json']]
    }

    private static getNameFieldForProduct(def product) {
        if (product instanceof List) {
            null
        } else if (product.hasProperty("name") || product.get("name")) {
            return product.name
        } else if (product.hasProperty("ingredient") || product.get("ingredient")) {
            return product.ingredient
        } else if (product.hasProperty("tradeName") || product.get("tradeName")) {
            return product.tradeName
        }
    }

    private static String getGenericNameForProduct(def product) {
        if (product.hasProperty("genericName") || product.get("genericName")) {
            return product.genericName
        } else {
            return null
        }
    }

    def fetchDictionaryList() {
        render( ["dictMap": PVDictionaryConfig.ProductConfig.levels] as JSON)
    }

    def fetchDictionaryListEvdas () {
        render (["dictMap" : Holders.config.pvsignal.evdas.dictionary.list] as JSON)
    }
}
