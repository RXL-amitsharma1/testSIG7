package com.rxlogix

import com.rxlogix.config.ProductGroup
import com.rxlogix.enums.ProductClassification
import grails.plugin.springsecurity.annotation.Secured
import com.fasterxml.jackson.databind.ObjectMapper

@Secured(["isAuthenticated()"])
class ProductGroupController {

    def productGroupService

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def index() {
        redirect(action: "create")
    }

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def create() {
        ProductGroup productGroup = new ProductGroup()
        render(view: "create", model: [productGroup: productGroup])
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def edit(Long id) {
        ProductGroup productGroup = ProductGroup.get(id)
        render(view: "create", model: [productGroup: productGroup])
    }


    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def save(Long id) {
        ProductGroup productGroup
        int hierarchyCount = 0
        if (id) {
            productGroup = ProductGroup.get(id)
        } else {
            productGroup = new ProductGroup()
        }
        ObjectMapper objectMapper = new ObjectMapper();
        if (params.productSelection) {
            Map<String, String> prodSelectionMap = objectMapper.readValue(params.productSelection, Map.class)
            prodSelectionMap?.each { k, v ->
                if (prodSelectionMap.get(k))
                    hierarchyCount++
            }
        }
        productGroup.productSelection = params.productSelection
        productGroup.groupName = params.groupName
        productGroup.classification = ProductClassification.getValue(params.classification)
        productGroup.display = params.display ? true : false
        if (hierarchyCount > 1) {
            flash.error = "Product grouping across product hierarchies is not allowed"
            render(view: "create", model: [productGroup: productGroup])
            return
        }
        try {
            productGroup = productGroupService.saveUpdateProductGroupMart(productGroup)
            flash.message = "Product Group ${productGroup.id} created"
            flash.args = [productGroup.id]
            flash.defaultMessage = "Product Group ${productGroup.id} created"
            redirect(action: "index")

        } catch (Exception e) {
            log.error("Some error occurred while saving product group", e)
            render(view: "create", model: [productGroup: productGroup])
        }
    }


    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def list() {
        def productGroupList = ProductGroup.list().collect { ProductGroup productGroup ->
            [
                    "id"            : productGroup.id,
                    "groupName"     : productGroup.groupName,
                    "products"      : productGroup.getProductNameList(),
                    "classification": productGroup.classification.id,
                    "display"       : productGroup.display
            ]
        }
        respond productGroupList, [formats: ['json']]
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def delete(Long id) {
        productGroupService.deleteProductGroupMart(id)
        flash.message = "Product Group ${id} deleted."
        redirect(controller: 'productGroup', action: 'index')
    }

}
