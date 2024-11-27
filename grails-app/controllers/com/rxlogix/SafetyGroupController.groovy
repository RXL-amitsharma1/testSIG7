package com.rxlogix

import com.rxlogix.config.SafetyGroup
import com.rxlogix.enums.GroupType
import com.rxlogix.user.Group
import grails.plugin.springsecurity.annotation.Secured

@Secured(["isAuthenticated()"])
class  SafetyGroupController {

    def CRUDService
    def pvsProductDictionaryService
    def productDictionaryCacheService
    def cacheService
    AlertService alertService


    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def index() {
        params.max = Constants.Search.MAX_SEARCH_RESULTS
        render view: "index", model: [safetyGroupInstanceList:  SafetyGroup.list(params), safetyGroupInstanceTotal: SafetyGroup.count()]
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def create() {
        def safetyGroupInstance = new SafetyGroup()
        return [safetyGroupInstance: safetyGroupInstance]
    }


    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def save() {
        def safetyGroupInstance = new SafetyGroup()
        bindData(safetyGroupInstance, params)
        setAllowedProducts(safetyGroupInstance)
        if (SafetyGroup.findByName(params.name)) {
            flash.error = "Safety Group Name already exists!!!"
            render(view: "create", model: [safetyGroupInstance: safetyGroupInstance, allowedProductsList: safetyGroupInstance.allowedProductList])
            return
        }
        if (!safetyGroupInstance.hasErrors() && CRUDService.save(safetyGroupInstance)) {
            if(alertService.isProductSecurity()){
                productDictionaryCacheService.updateProductDictionaryCache(safetyGroupInstance, safetyGroupInstance.allowedProductList,false)
                cacheService.setProductGroupCache(safetyGroupInstance)
                List<Long> safetyGroupList = []
                safetyGroupList.add(safetyGroupInstance.id)
                cacheService.updateProductsCacheForSafetyGroup(safetyGroupList)
            }
            flash.message = message(code: 'safety.group.created', args: [safetyGroupInstance.name])
            flash.args = [safetyGroupInstance.id]
            flash.defaultMessage = "SafetyGroup ${safetyGroupInstance.id} created"
            redirect(action: "show", id: safetyGroupInstance.id)
        }
        else {
            render(view: "create", model: [safetyGroupInstance: safetyGroupInstance, allowedProductsList: safetyGroupInstance.allowedProductList])
        }
    }

    def setAllowedProducts(safetyGroupInstance) {
        def productParams = params['allowedProductList']

        if (productParams instanceof Collection || productParams.getClass().isArray() ||
                productParams == null) {
            safetyGroupInstance.allowedProductList = productParams
        } else {
            safetyGroupInstance.allowedProductList = [productParams]
        }
    }

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def show() {
        def safetyGroupInstance = SafetyGroup.get(params.id)
        if (!safetyGroupInstance) {
            flash.message = message(code: 'safety.group.not.found')
            flash.args = [params.id]
            flash.defaultMessage = "SafetyGroup not found with id ${params.id}"
            redirect(action: "list")
        }
        else {
            return [safetyGroupInstance: safetyGroupInstance]
        }
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def edit() {
        def safetyGroupInstance = SafetyGroup.get(params.id)
        if (!safetyGroupInstance) {
            flash.message = message(code: 'safety.group.not.found')
            flash.args = [params.id]
            flash.defaultMessage = "SafetyGroup not found with id ${params.id}"
            redirect(action: "index")
        }
        else {
            return [safetyGroupInstance: safetyGroupInstance, allowedProductsList: safetyGroupInstance.allowedProductList, edit:true]
        }
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def update() {
        def safetyGroupInstance = SafetyGroup.get(params.id)
        if (safetyGroupInstance) {
            def name = safetyGroupInstance.name
            bindData(safetyGroupInstance, params)
            setAllowedProducts(safetyGroupInstance)
            safetyGroupInstance.markDirty("allowedProd", safetyGroupInstance.allowedProductList?.sort()?.join('%#%'))
            if (params.name != name && SafetyGroup.findByName(params.name)) {
                flash.error = "Safety Group Name already exists!!"
                render(view: "edit", model: [safetyGroupInstance: safetyGroupInstance, allowedProductsList: safetyGroupInstance.allowedProductList])
                return
            }
            if (params.version) {
                def version = params.version.toLong()
                if (safetyGroupInstance.version > version) {
                    safetyGroupInstance.errors.rejectValue("version", "safetyGroup.optimistic.locking.failure",
                            "Another user has updated this SafetyGroup while you were editing")
                    render(view: "edit", model: [safetyGroupInstance: safetyGroupInstance, allowedProductsList: safetyGroupInstance.allowedProductList])
                    return
                }
            }

            if (!safetyGroupInstance.hasErrors() && CRUDService.update(safetyGroupInstance)) {
                productDictionaryCacheService.updateProductDictionaryCache(safetyGroupInstance, safetyGroupInstance.allowedProductList,false)
                cacheService.setProductGroupCache(safetyGroupInstance)
                List<Long> safetyGroupList = []
                safetyGroupList.add(safetyGroupInstance.id)
                cacheService.updateProductsCacheForSafetyGroup(safetyGroupList)
                flash.message = message(code: 'safety.group.updated', args: [safetyGroupInstance.name])
                flash.args = [params.id]
                flash.defaultMessage = "SafetyGroup ${params.id} updated"
                redirect(action: "show", id: safetyGroupInstance.id)
            }
            else {
                render(view: "edit", model: [safetyGroupInstance: safetyGroupInstance,
                                             allowedProductsList: safetyGroupInstance.allowedProductList])}
        }
        else {
            flash.message = message(code: 'safety.group.not.found')
            flash.args = [params.id]
            flash.defaultMessage = "SafetyGroup not found with id ${params.id}"
            redirect(action: "edit", id: params.id)
        }
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def delete() {
        def groupInstance = SafetyGroup.get(params.id)
        if (groupInstance) {
            try {
                CRUDService.delete(groupInstance)
                flash.message = message(code: 'safety.group.deleted', args: [groupInstance.name])
                flash.args = [params.id]
                flash.defaultMessage = "SafetyGroup ${params.id} deleted"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = message(code: 'safety.group.not.deleted')
                flash.args = [params.id]
                flash.defaultMessage = "SafetyGroup ${params.id} could not be deleted"
                redirect(action: "show", id: params.id)
            }
        } else {
            flash.message = message(code: 'safety.group.not.found')
            flash.args = [params.id]
            flash.defaultMessage = "SafetyGroup not found with id ${params.id}"
            redirect(action: "index")
        }
    }
}
