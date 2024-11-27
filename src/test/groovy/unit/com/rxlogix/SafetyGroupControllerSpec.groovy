package unit.com.rxlogix

import com.rxlogix.AlertService
import com.rxlogix.CRUDService
import com.rxlogix.ProductDictionaryCacheService
import com.rxlogix.SafetyGroupController
import com.rxlogix.UserService
import com.rxlogix.cache.CacheService
import com.rxlogix.config.ProductDictionaryCache
import com.rxlogix.config.SafetyGroup
import com.rxlogix.signal.Justification
import com.rxlogix.user.Preference
import com.rxlogix.user.User
import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

class SafetyGroupControllerSpec extends Specification implements ControllerUnitTest<SafetyGroupController> {
    def setup(){
        SafetyGroup safetyGroup1=new SafetyGroup(name:"safetyGroup_1",allowedProd: "paracetamol1")
        safetyGroup1.save(validate:false)
        SafetyGroup safetyGroup2=new SafetyGroup(name:"safetyGroup_2",allowedProd: "paracetamol2")
        safetyGroup2.save(validate:false)
        User user1=new User(username: "user1")
        user1.addToSafetyGroups(safetyGroup2)
        user1.save(validate:false)
        Preference preference=new Preference(timeZone: "UTC")
        User user=new User(username: "user1",preference: preference)
        user.save(validate:false)
        SpringSecurityService springSecurityService=Mock(SpringSecurityService)
        springSecurityService.isLoggedIn()>>{
            return true
        }
        springSecurityService.principal>>{
            return user
        }
        controller.CRUDService.userService.springSecurityService=springSecurityService
        ProductDictionaryCacheService productDictionaryCacheService=Mock(ProductDictionaryCacheService)
        productDictionaryCacheService.updateProductDictionaryCache(_,_,_)>>{
            return true
        }
        controller.productDictionaryCacheService=productDictionaryCacheService
        CacheService cacheService=Mock(CacheService)
        cacheService.setProductGroupCache(_)>>{
            return true
        }
        controller.cacheService=cacheService
        AlertService alertService=Mock(AlertService)
        alertService.isProductSecurity()>>{
            return true
        }
        controller.alertService=alertService
    }
    def clean(){
    }

    void "test index action when there are safety group"(){
        when:
        controller.index()
        then:
        response.status==200
        view=="/safetyGroup/index"
        model.safetyGroupInstanceTotal==2
        model.safetyGroupInstanceList.get(0)==SafetyGroup.get(1)
        model.safetyGroupInstanceList.get(1)==SafetyGroup.get(2)
    }
    void "test index action when there are no safety group"(){
        given:
        SafetyGroup.list().each{
            it.delete()
        }
        when:
        controller.index()
        then:
        response.status==200
        view=="/safetyGroup/index"
        model.safetyGroupInstanceTotal==0
        model.safetyGroupInstanceList.size()==0
    }
    void "test create action"(){
        when:
        Map result=controller.create()
        then:
        response.status==200
        result.safetyGroupInstance.getClass()==SafetyGroup
    }
    void "test save action on success"(){
        setup:
        params.name="safetyGroup_3"
        params.allowedProd="paracetamol0"
        params.allowedProductList=["paracetamol1","paracetamol2"]
        when:
        controller.save()
        then:
        response.status==302
        response.redirectedUrl=="/safetyGroup/show/3"
        flash.message=='safety.group.created'
        flash.args==[3]
        flash.defaultMessage=="SafetyGroup 3 created"
    }
    void "test save action when exception occurs"(){
        setup:
        CRUDService crudService=Mock(CRUDService)
        crudService.save(_)>>{
            return false
        }
        controller.CRUDService=crudService
        params.name="safetyGroup_3"
        params.allowedProd="paracetamol0"
        params.allowedProductList=["paracetamol1","paracetamol2"]
        when:
        controller.save()
        then:
        response.status==200
        view=="/safetyGroup/create"
        model.safetyGroupInstance.name=="safetyGroup_3"
        model.allowedProductsList==["paracetamol1","paracetamol2"]
        flash.error==null
    }
    void "test save action when safety group already exist with same name"(){
        setup:
        params.name="safetyGroup_1"
        params.allowedProd="paracetamol0"
        params.allowedProductList=["paracetamol1","paracetamol2"]
        when:
        controller.save()
        then:
        response.status==200
        view=="/safetyGroup/create"
        model.safetyGroupInstance.name=="safetyGroup_1"
        model.allowedProductsList==["paracetamol1","paracetamol2"]
        flash.error=="Safety Group Name already exists!!!"
    }
    void "test setAllowedProducts action when params has list"(){
        setup:
        params.allowedProductsList==["paracetamol1","paracetamol2"]
        when:
        controller.setAllowedProducts(SafetyGroup.get(1))
        then:
        response.status==200
        SafetyGroup.get(1).allowedProductList==params.allowedProductsList
    }
    void "test setAllowedProducts action when params has a string"(){
        setup:
        params.allowedProductsList=="paracetamol1"
        when:
        controller.setAllowedProducts(SafetyGroup.get(1))
        then:
        response.status==200
        SafetyGroup.get(1).allowedProductList==params.allowedProductsList
    }
    void "test show action when safety group exist for given id"(){
        setup:
        params.id=1
        when:
        Map result=controller.show()
        then:
        response.status==200
        result.safetyGroupInstance.id==1
    }
    void "test show action when safety group doesn't exist for given id"(){
        setup:
        params.id=10
        when:
        controller.show()
        then:
        response.status==302
        response.redirectedUrl=="/safetyGroup/list"
        flash.message=='safety.group.not.found'
        flash.args==[params.id]
        flash.defaultMessage=="SafetyGroup not found with id ${params.id}"
    }
    void "test edit action when safety group exist for given id"(){
        setup:
        params.id=1
        when:
        Map result=controller.edit()
        then:
        response.status==200
        result.safetyGroupInstance.id==1
        result.edit==true
    }
    void "test edit action when safety group doesn't exist for given id"(){
        setup:
        params.id=10
        when:
        controller.edit()
        then:
        response.status==302
        response.redirectedUrl=="/safetyGroup/index"
        flash.message=='safety.group.not.found'
        flash.args==[params.id]
        flash.defaultMessage=="SafetyGroup not found with id ${params.id}"
    }
    void "test update action on success"(){
        setup:
        params.id=1
        params.name="changed name"
        params.allowedProductsList==["paracetamol1","paracetamol2"]
        params.version=1
        when:
        controller.update()
        then:
        response.status==302
        response.redirectedUrl=="/safetyGroup/show/1"
        flash.message =='safety.group.updated'
        flash.args == [params.id]
        flash.defaultMessage == "SafetyGroup ${params.id} updated"
    }
    void "test update action when params.version is smaller than safetyGroup version"(){
        setup:
        SafetyGroup safetyGroup1=SafetyGroup.get(1)
        safetyGroup1.name="old name1"
        safetyGroup1.save(validate:false)
        SafetyGroup safetyGroup2=SafetyGroup.get(1)
        safetyGroup2.name="old name2"
        safetyGroup2.save(validate:false)
        params.id=1
        params.name="changed name"
        params.allowedProductsList==["paracetamol1","paracetamol2"]
        params.version=1
        when:
        controller.update()
        then:
        response.status==200
        view=="/safetyGroup/edit"
        model.safetyGroupInstance==SafetyGroup.get(params.id)
        model.allowedProductsList==params.allowedProductsList
        flash.error==null
    }
    void "test update action when another safety group exist with name params.name"(){
        setup:
        params.id=1
        params.name="safetyGroup_2"
        params.allowedProductsList==["paracetamol1","paracetamol2"]
        params.version=1
        when:
        controller.update()
        then:
        response.status==200
        view=="/safetyGroup/edit"
        model.safetyGroupInstance==SafetyGroup.get(params.id)
        model.allowedProductsList==params.allowedProductsList
        flash.error=="Safety Group Name already exists!!"
    }
    void "test update action when update fails"(){
        setup:
        CRUDService crudService=Mock(CRUDService)
        crudService.update(_)>>{
            false
        }
        controller.CRUDService=crudService
        params.id=1
        params.name="changed name"
        params.allowedProductsList==["paracetamol1","paracetamol2"]
        params.version=1
        when:
        controller.update()
        then:
        response.status==200
        view=="/safetyGroup/edit"
        model.safetyGroupInstance==SafetyGroup.get(params.id)
        model.allowedProductsList==params.allowedProductsList
        flash.error==null
    }
    void "test update action when safety group doesn't exist with given param.id"(){
        setup:
        params.id=10
        params.name="changed name"
        params.allowedProductsList==["paracetamol1","paracetamol2"]
        params.version=1
        when:
        controller.update()
        then:
        response.status==302
        response.redirectedUrl=="/safetyGroup/edit/$params.id"
        flash.message == 'safety.group.not.found'
        flash.args == [params.id]
        flash.defaultMessage == "SafetyGroup not found with id ${params.id}"
    }
    void "test delete delete on success"(){
        given:
        params.id=1
        when:
        controller.delete()
        then:
        response.status==302
        flash.message =='safety.group.deleted'
        flash.args == [params.id]
        flash.defaultMessage == "SafetyGroup ${params.id} deleted"
        response.redirectedUrl=="/safetyGroup/list"
    }
    void "test delete delete when safety group doesn't exist for params.id"(){
        given:
        params.id=10
        when:
        controller.delete()
        then:
        response.status==302
        flash.message =='safety.group.not.found'
        flash.args == [params.id]
        flash.defaultMessage == "SafetyGroup not found with id ${params.id}"
        response.redirectedUrl=="/safetyGroup/index"
    }
}
