package com.rxlogix

import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.mapping.LmIngredient
import com.rxlogix.mapping.LmLicense
import com.rxlogix.mapping.LmProduct
import com.rxlogix.mapping.LmProductFamily
import com.rxlogix.util.SignalQueryHelper
import com.rxlogix.util.SignalUtil
import grails.util.Holders
import groovy.sql.GroovyResultSet
import groovy.sql.Sql
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.sql.JoinType

import java.sql.SQLException

class PvsProductDictionaryService {

    def grailsApplication
    def userService
    def cacheService
    def signalDataSourceService
    def dataObjectService
    def productBasedSecurityService
    def dataSource_pva
    def restAPIService

    /**
     * Method to return the products/family/ingredient/licence. It applies the associated product safety to it.
     * @param level : This is required to determine the level in the dictionary.
     * 1: Ingredient, 2: Family, 3: Product, 4: Trade Name or licence.
     * @param objectId : This is required to fetch the object from id.
     * @param searchTerm : This represents the search term entered in the browser.
     * @param selectedDatasource : This is required for the multiple ds systems. Default should be 'pva'
     * @return
     */
    def getProductInstance(String level, String objectId, String searchTerm, String selectedDatasource, def allowedProductsToUser) {
        Map dictionaryMap = dataObjectService.getIdLabelMap()

        String label = dictionaryMap.get(level)
        def showProducts = []
        switch (label) {
            case "Ingredient": //Representing the ingredients.

                List<LmIngredient> lmIngredients = []
                if (objectId && objectId != "null") {
                    LmIngredient."$selectedDatasource".withTransaction {
                        showProducts = LmIngredient."$selectedDatasource".get(objectId)
                    }
                } else if (searchTerm) {
                    LmIngredient."$selectedDatasource".withTransaction {
                        lmIngredients = LmIngredient."$selectedDatasource".findAllByIngredientIlike('%' + searchTerm + '%', [sort: "ingredient", order: "asc", max: 100])
                    }
                }

                if (lmIngredients) {
                    if (selectedDatasource == Constants.DataSource.PVA) {
                        def allowedIngredients = ingredientList(objectId, selectedDatasource, allowedProductsToUser)
                        List finalIngredients = allowedIngredients.collect{it.ingredientId}.unique()
                        List commonIngredients = lmIngredients.collect { it.id }.intersect(finalIngredients)

                        commonIngredients.each { def ingredientId ->
                            showProducts << lmIngredients.find { it.id == ingredientId }
                        }
                    } else {
                        showProducts = lmIngredients
                    }
                }
                break
            case "Family": //Representing the family.
                LmProductFamily."$selectedDatasource".withTransaction {
                    List<LmProductFamily> productFamilies = []

                    if (objectId && objectId != "null") {

                        LmProductFamily."$selectedDatasource".withTransaction {
                            showProducts = LmProductFamily."$selectedDatasource".get(objectId)
                        }
                    } else if (searchTerm) {
                        LmProductFamily."$selectedDatasource".withTransaction {
                            productFamilies = LmProductFamily."$selectedDatasource".findAllByNameIlike('%' + searchTerm + '%', [sort: "name", order: "asc", max: 100])
                        }
                    }
                    if (productFamilies && selectedDatasource == Constants.DataSource.PVA) {
                        def allowedProducts = getAllowedProducts(objectId, selectedDatasource, allowedProductsToUser)
                        def allowedFamilies = allowedProducts*.family
                        showProducts = productFamilies.intersect(allowedFamilies)
                    }
                }
                break
            case "Product Generic Name": //Representing the products.
                showProducts = prepareProducts(objectId, selectedDatasource, searchTerm, allowedProductsToUser)
                break
            case "Trade Name": //Representing the license or trade name.
                LmLicense."$selectedDatasource".withTransaction {
                    List<LmLicense> licenses = []
                    if (objectId && objectId != "null") {
                        LmLicense."$selectedDatasource".withTransaction {
                            showProducts = LmLicense."$selectedDatasource".get(objectId)
                        }
                    } else if (searchTerm) {
                        LmLicense."$selectedDatasource".withTransaction {
                            licenses = LmLicense."$selectedDatasource".findAllByTradeNameIlike('%' + searchTerm + '%', [sort: "tradeName", order: "asc", max: 100])
                        }
                    }
                    if (licenses && selectedDatasource == Constants.DataSource.PVA) {
                        def allowedProducts = getTradeList(objectId, selectedDatasource, allowedProductsToUser)
                        def allowedLicenses = allowedProducts.collect{it.licenseId}.unique()
                        List commonTradeNames = licenses.collect { it.id}.intersect(allowedLicenses)

                        commonTradeNames.each { def licenseId ->
                            showProducts << licenses.find { it.id == licenseId }
                        }
                    }
                }
                break
        }
        showProducts
    }

    /**
     * Thie method prepares the list of products based on the security.
     * @param objectId
     * @param selectedDatasource
     * @param searchTerm
     * @param allowedProductsLowerCase
     * @return
     */
    def ingredientList(def objectId, def selectedDatasource, def allowedProductsToUser){
        List<LmProduct> products = null
        //There will be no security on Products when user selected FAERS DB
        if (selectedDatasource == Constants.DataSource.FAERS) {
            return products
        }

        if (objectId && objectId != "null") {
            LmProduct."$selectedDatasource".withTransaction {
                products = LmProduct."$selectedDatasource".withCriteria {
                    eq("id", objectId)
                    'or' {
                        allowedProductsToUser?.collate(1000).each {
                            'in'("name", it)
                        }
                    }
                }
            }
        } else {
            LmProduct."$selectedDatasource".withTransaction {
                products = LmProduct.createCriteria().list {
                    resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
                    createAlias("ingredients", "ingredients", JoinType.LEFT_OUTER_JOIN)
                    isNotNull("ingredients.id")
                    projections {
                        property("ingredients.id", "ingredientId")
                    }
                    'or' {
                        allowedProductsToUser?.collate(1000).each {
                            'in'("name", it)
                        }
                    }

                } as List
            }
        }
        products

    }
    def getTradeList(def objectId, def selectedDatasource, def allowedProductsToUser){
        List<LmProduct> products = null
        //There will be no security on Products when user selected FAERS DB
        if (selectedDatasource == Constants.DataSource.FAERS) {
            return products
        }

        if (objectId && objectId != "null") {
            LmProduct."$selectedDatasource".withTransaction {
                products = LmProduct."$selectedDatasource".withCriteria {
                    eq("id", objectId)
                    'or' {
                        allowedProductsToUser?.collate(1000).each {
                            'in'("name", it)
                        }
                    }
                }
            }
        } else {
            LmProduct."$selectedDatasource".withTransaction {
                products = LmProduct.createCriteria().list {
                    resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
                    createAlias("licenses", "licenses", JoinType.LEFT_OUTER_JOIN)
                    isNotNull("licenses.id")
                    projections {
                        property("licenses.id", "licenseId")
                    }
                    'or' {
                        allowedProductsToUser?.collate(1000).each {
                            'in'("name", it)
                        }
                    }

                } as List
            }
        }
        products

    }
    private prepareProducts(String objectId, String selectedDatasource, String searchTerm, allowedProductsLowerCase) {
        List<LmProduct> products = null
        def showProducts = []
        if (objectId && objectId != "null") {
            def lmProduct = null
            LmProduct."$selectedDatasource".withTransaction {
                lmProduct = LmProduct."$selectedDatasource".get(objectId)
            }
            return lmProduct
        } else if (searchTerm) { // && selectedDatasource != Constants.DataSource.FAERS) {
            LmProduct."$selectedDatasource".withTransaction {
                products = LmProduct."$selectedDatasource".findAllByNameIlike('%' + searchTerm + '%', [sort: "name", order: "asc", max: 100]).unique {
                    it.name
                }
                products.each {
                    if (Holders.config.pvsignal.product.based.security) {
                        if (selectedDatasource != Constants.DataSource.PVA) {
                            showProducts.add(it)
                        } else if (allowedProductsLowerCase?.contains(it.name)) {
                            showProducts.add(it)
                        }
                    } else {
                        showProducts.add(it)
                    }
                }
            }
        }
        showProducts
    }

    private getAllowedProducts(String objectId, String selectedDatasource, allowedProductsToUser) {
        List<LmProduct> products = null
        //There will be no security on Products when user selected FAERS DB
        if (selectedDatasource == Constants.DataSource.FAERS) {
            return products
        }

        if (objectId && objectId != "null") {
            LmProduct."$selectedDatasource".withTransaction {
                products = LmProduct."$selectedDatasource".withCriteria {
                    eq("id", objectId)
                    'or' {
                        allowedProductsToUser?.collate(1000).each {
                            'in'("name", it)
                        }
                    }
                }
            }
        } else {
            LmProduct."$selectedDatasource".withTransaction {
                products = LmProduct."$selectedDatasource".withCriteria {
                    allowedProductsToUser?.collate(1000).each {
                        'in'("name", it)
                    }
                }
            }
        }
        products
    }

    JSONArray getChildProducts(def item, int level, String selectedDatasource, List<String> allowedProductsToUser) {
        Map dictionaryMap = dataObjectService.getIdLabelMap()
        String label = dictionaryMap.get(level.toString())
        JSONArray children = new JSONArray()
        try {
            switch (label) {
                case "Ingredient":
                    LmProduct."$selectedDatasource".withTransaction {
                        List<LmProduct> relatedProducts = LmProduct."$selectedDatasource".createCriteria().list {
                            if (allowedProductsToUser) {
                                or {
                                    allowedProductsToUser.collate(1000).each {
                                        'in'("name", it)
                                    }
                                }
                                'ingredients' {
                                    'eq'("id", item.id as Long)
                                }
                            }
                        }?.unique { it.name }
                        List<LmProductFamily> allowedFamilies = []
                        relatedProducts?.each { LmProduct product ->
                            allowedFamilies.add(product.family)
                        }
                        List<LmProductFamily> productFamilies = item.family as List<LmProductFamily>
                        productFamilies.each { LmProductFamily productFamily ->
                            if (allowedFamilies.id.contains(productFamily.id)) {
                                children.add(new JSONObject(['id'  : productFamily?.id,
                                                             'name': productFamily?.name, 'level': level + 1]))
                            }
                        }
                    }
                    break
                case "Family":
                    LmProduct."$selectedDatasource".withTransaction {
                        List<LmProduct> relatedProducts = LmProduct."$selectedDatasource".createCriteria().list {
                            if (allowedProductsToUser) {
                                or {
                                    allowedProductsToUser.collate(1000).each {
                                        'in'("name", it)
                                    }
                                }
                            }
                            'eq'("family", item)
                        }?.unique { it.name }
                        relatedProducts.each {
                            children.add(new JSONObject(['id': it.id, 'name': it.name, 'level': level + 1]))
                        }
                    }
                    break
                case "Product Generic Name":
                    item.licenses.each {
                        children.add(new JSONObject(['id': it.id, 'name': it.tradeName, 'level': level + 1]))
                    }
                    break
            }
        }catch (Exception ex){
            log.error("Some error occurred while getChildProducts method called.",ex)
        }
        children
    }

    def getParentProducts(
            def item, int level, JSONArray parents, String selectedDatasource, def allowedProductsToUser) {
        Map dictionaryMap = dataObjectService.getIdLabelMap()
        String label =  dictionaryMap.get(level?.toString())

        switch (label) {
            case "Family":
                LmProduct."$selectedDatasource".withTransaction {
                    def relatedProducts = []
                    allowedProductsToUser?.collate(1000).each {
                        relatedProducts << LmProduct."${selectedDatasource}".findAll("from LmProduct as p where p.name in(:l) and p.family = :family", [l: it, family: item])
                    }
                    relatedProducts?.each {
                        it.ingredients.each {
                            JSONObject parent = new JSONObject(['id': it.id, 'name': it.ingredient, 'level': level - 1])
                            if (!parents.contains(parent)) {
                                parents.add(parent)
                            }
                        }
                    }
                }
                break
            case "Product Generic Name":
                JSONObject parent = new JSONObject(['id': item.family?.id, 'name': item.family?.name, 'level': level - 1])
                if (!parents.contains(parent)) {
                    parents.add(parent)
                }
                break
            case "Trade Name":
                LmProduct."$selectedDatasource".withTransaction {
                    def relatedProducts = []
                    allowedProductsToUser?.collate(1000).each {
                        relatedProducts << LmProduct."${selectedDatasource}".findAll("from LmProduct as p where p.name in(:l) and p.family = :family", [l: it, family: item])
                    }
                    relatedProducts.each {
                        JSONObject parent = new JSONObject(['id': it.id, 'name': it.name, 'level': level - 1])
                        if (!parents.contains(parent)) {
                            parents.add(parent)
                        }
                    }
                }
                break
        }
        parents
    }

    def getDictionaryProducts(def item, int level, String selectedDatasource, def allowedProductsToUser) {
        def products = []
        if (item && allowedProductsToUser) {
            switch (level) {
                case 1:
                    LmProduct."$selectedDatasource".withTransaction {
                        LmProduct."$selectedDatasource".withTransaction {
                            products = LmProduct."$selectedDatasource".createCriteria().list {
                                if (allowedProductsToUser.size() > 0) {
                                    or {
                                        allowedProductsToUser.collate(1000).each {
                                            'in'("name", it)
                                        }
                                    }
                                    'ingredients' {
                                        'eq'("id", item.id as Long)
                                    }
                                }
                            }?.unique { it.name }
                        }
                    }
                    break
                case 2:
                    LmProduct."$selectedDatasource".withTransaction {
                        LmProduct."$selectedDatasource".withTransaction {
                            products = LmProduct."$selectedDatasource".createCriteria().list {
                                if (allowedProductsToUser.size() > 0) {
                                    or {
                                        allowedProductsToUser.collate(1000).each {
                                            'in'("name", it)
                                        }
                                    }
                                }
                                'eq'("family",item)
                            }?.unique { it.name }
                        }
                    }
                    break
                case 4:
                    LmProduct."$selectedDatasource".withTransaction {
                        LmProduct."$selectedDatasource".withTransaction {
                            products = LmProduct."$selectedDatasource".createCriteria().list {
                                if (allowedProductsToUser.size() > 0) {
                                    or {
                                        allowedProductsToUser.collate(1000).each {
                                            'in'("name", it)
                                        }
                                    }
                                    'licenses' {
                                        'eq'("id", item.id as BigDecimal)
                                    }
                                }
                            }?.unique { it.name }
                        }
                    }
                    break
            }
        }
        products
    }

    List getProductsNameList() {
        def products = []
        try {
            String viewName = dataObjectService.getProductViewName()
            //TODO: This language will be changed later for customers using other localizations.
            String productColumn = Holders.config.dictionary.product.name.column
            String sqlStatement = SignalQueryHelper.product_name_selection(viewName, productColumn, "en")
            products = fetchProductsData(productColumn, sqlStatement)
        } catch (Throwable th) {
            th.printStackTrace()
        }
        products?.sort()
    }

    List fetchProductsData(String columnName, String sqlStatement) {

        List products = []
        Sql sql = null
        try {
            sql = new Sql(dataSource_pva)
            sql.eachRow(sqlStatement) { GroovyResultSet resultSet ->
                products.add(resultSet.getString(columnName))
            }
        } catch(SQLException sqlException) {
            sqlException.printStackTrace()
            log.error(sqlException.getMessage())
        } catch(Throwable th) {
            th.printStackTrace()
            log.error(th.getMessage())
        } finally {
            sql?.close()
        }
        products
    }

    List getProducts(String searchTerm){
        List products = []
        List showProducts =[]
        String selectedDatasource = Constants.DataSource.PVA

        LmProduct."${selectedDatasource}".withTransaction {
            products = LmProduct."${selectedDatasource}".withCriteria {
                ilike("name", "%${searchTerm}%")
                maxResults(2000)
            }
        }

        products.each {
            showProducts.add(it.name)
        }
        return showProducts
    }

    String fetchProductNamesfromUpperHierarchy(String productSelection) {
        Map levelAndIds = fetchDictionaryLevelAndIds(productSelection)
        String dictionaryValues = levelAndIds.ids.collect { "'" + it + "'" }.join(",")
        String productLinkView = dataObjectService.getProductMapping().get(levelAndIds.level)?.productLinkView
        if (!productLinkView) {
           return ""
        }
        String sqlStatement = prodNameListSql(productLinkView, dictionaryValues)
        Sql sql = null
        Set<String> products = []
        String productColName = Holders.config.dictionary.product.name.column
        try {
            sql = new Sql(dataSource_pva)
            sql.eachRow(sqlStatement) { GroovyResultSet resultSet ->
                products.add(resultSet.getString(productColName))
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace()
            log.error(sqlException.getMessage())
        } catch (Throwable th) {
            th.printStackTrace()
            log.error(th.getMessage())
        } finally {
            sql?.close()
        }
        products?.join(", ")
    }

    String prodNameListSql(String mappingView, String dictValue) {
        String productViewName = dataObjectService.productViewName
        String productColName = Holders.config.dictionary.product.name.column
        String productColId = Holders.config.dictionary.id.column
        """select distinct(prod.${productColName}) from ${productViewName} prod join ${mappingView} vw
        on prod.${productColId} = vw.COL_2 and vw.COL_1 in (${dictValue}) 
        order by prod.${productColName}"""
    }

    Map fetchDictionaryLevelAndIds(String productJson) {
        Map<Integer, String> jsonMap = SignalUtil.parseJsonString(productJson)
        Integer dictionaryProductLevel = dataObjectService.getDictionaryProductLevel() as Integer
        def levelMap = jsonMap.find { it.value && (it.key as Integer) > dictionaryProductLevel }
        [level: levelMap?.key as Integer, ids: levelMap?.value?.id] as Map
    }

    Boolean isLevelGreaterThanProductLevel(def executedConfiguration) {
        Integer dictionaryProductLevel = dataObjectService.getDictionaryProductLevel() as Integer
        Integer productDictionarySelection = executedConfiguration?.productDictionarySelection as Integer
        productDictionarySelection > dictionaryProductLevel
    }
    Boolean isLevelGreaterThanProductLevelAgg(def productDictionarySelection){
        Integer dictionaryProductLevel = dataObjectService.getDictionaryProductLevel() as Integer
        productDictionarySelection > dictionaryProductLevel
    }

    String getUpperHierarchyProducts(ExecutedConfiguration executedConfiguration) {
        String products
        if (isLevelGreaterThanProductLevel(executedConfiguration)) {
            products = fetchProductNamesfromUpperHierarchy(executedConfiguration?.productSelection)
        }
        products
    }

    Map fetchProductGroup(Integer dictionaryType, String term, String dataSource, Integer page, Integer max, String userName, Boolean exactSearch = null, Boolean isMultiIngredient = false) {
        Map productGroupResult = null
        String url = grailsApplication.config.app.dictionary.base.url
        String path = grailsApplication.config.app.dictionary.group.fetchlist.api
        Map response = restAPIService.get(url, path, [dictionaryType: dictionaryType, term: term, dataSource: dataSource, page: page, max: max, userName: userName, exactSearch: exactSearch, isMultiIngredient: isMultiIngredient])
        log.info('Product Group response status from  PVR :' + response.status)
        if (response && response.status == HttpURLConnection.HTTP_OK && response.data?.items) {
            productGroupResult = response.data.items[0] as Map
        }
        return productGroupResult
    }
}
