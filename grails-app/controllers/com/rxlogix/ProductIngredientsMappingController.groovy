package com.rxlogix

import com.rxlogix.signal.ProductIngredientMapping
import com.rxlogix.util.AlertUtil
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException

@Secured(["isAuthenticated()"])
class ProductIngredientsMappingController implements AlertUtil {

    def CRUDService

    def index() {
    }

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def list() {
        def productIngredientMappingList = ProductIngredientMapping.list().collect { it.toDto() }
        render(productIngredientMappingList as JSON)
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def create() {
        def dataSourceMap = getDataSourceMap()
        if(dataSourceMap.containsKey('pva')){
            dataSourceMap.remove('pva')
        }
        [dataSourceMap : dataSourceMap]
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def save(Long productIngredientId, String otherDataSource, String productSelectionIngredient, String productSelection) {
        Boolean isUpdateMode = productIngredientId ? true : false
        Boolean isError = false
        ProductIngredientMapping productIngredientMapping = null
        try {
            if (isUpdateMode) {
                productIngredientMapping = ProductIngredientMapping.get(productIngredientId)
            } else {
                productIngredientMapping = new ProductIngredientMapping()
            }
            productIngredientMapping.otherDataSource = otherDataSource
            productIngredientMapping.productSelection = productSelectionIngredient
            productIngredientMapping.pvaProductSelection = productSelection
            if (isUpdateMode) {
                CRUDService.save(productIngredientMapping)
            } else {
                CRUDService.update(productIngredientMapping)
            }
        } catch (ValidationException e) {
            isError = true
            log.error("Some error occurred while ${isUpdateMode ? 'Updating' : 'Saving'} Product/Ingredient Mapping.", e)
        } catch (Exception e) {
            isError = true
            log.error("Some error occurred while ${isUpdateMode ? 'Updating' : 'Saving'} Product/Ingredient Mapping.", e)
            flash.error = "Some error occurred while ${isUpdateMode ? 'Updating' : 'Saving'} Product/Ingredient Mapping."
        }
        if (isError) {
            def dataSourceMap = getDataSourceMap()
            if(dataSourceMap.containsKey('pva')){
                dataSourceMap.remove('pva')
            }
            render(view: 'create', model: [productIngredientMapping: productIngredientMapping , dataSourceMap : dataSourceMap])
        } else {
            redirect(controller: 'productIngredientsMapping', action: 'index')
        }
    }


    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def edit(Long id) {
        ProductIngredientMapping productIngredientMapping = ProductIngredientMapping.read(id)
        def dataSourceMap = getDataSourceMap()
        if(dataSourceMap.containsKey('pva')){
            dataSourceMap.remove('pva')
        }
        render(view: 'create', model: [productIngredientMapping: productIngredientMapping,dataSourceMap : dataSourceMap])
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def toggleEnableRule(Long id, Boolean enabled) {
        try {
            ProductIngredientMapping productIngredientMapping = ProductIngredientMapping.get(id)
            productIngredientMapping.enabled = enabled
            CRUDService.update(productIngredientMapping)
            flash.message = message(code: "app.label.product.ingredient.mapping.toggle.enable.success", args: [(enabled ? 'Enabled' : 'Disabled')])
        } catch (Exception e) {
            log.error("error occurred while enabling or disabling rule information", e)
        }
        redirect(controller: 'productIngredientsMapping', action: 'index')
    }

}
