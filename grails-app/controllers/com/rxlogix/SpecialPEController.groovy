package com.rxlogix

import com.rxlogix.config.SpecialPE
import com.rxlogix.dto.ResponseDTO
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import org.apache.http.util.TextUtils

@Secured(["isAuthenticated()"])
class SpecialPEController {

    def CRUDService

    def userService

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def index() {
        redirect(action: "create")
    }


    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def create() {
        SpecialPE specialPE = new SpecialPE()
        render (view: "create", model: [specialPE: specialPE])
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def save(Long id) {
        SpecialPE specialPE
        def eventSelection = params.eventSelection
        def productSelection = params.productSelection
        if(id){
            specialPE = SpecialPE.get(id)
        }else{
            specialPE = new SpecialPE()
        }
        specialPE.specialEvents = eventSelection
        specialPE.specialProducts = productSelection


        if (TextUtils.isEmpty(specialPE.specialEvents) || TextUtils.isEmpty(specialPE.specialProducts)) {
            flash.error = "Please fill all the required fields"
            render(view: "create", model: [specialPE: specialPE])
        } else if (!specialPE.hasErrors() && CRUDService.save(specialPE)) {
            flash.message = "Special Product Event created"
            redirect(action: "index")
        }
        else {
            render(view: "create", model: [specialPE: specialPE])
        }
    }

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def list () {
        def specialPEList = SpecialPE.list().sort{ it.lastUpdated }.collect {it.toDto()}
        specialPEList.reverse()
        respond specialPEList, [formats:['json']]
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def edit(Long id){
        SpecialPE specialPE = SpecialPE.get(id)
        render (view: "create", model: [specialPE: specialPE])
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def delete(Long id) {
        ResponseDTO responseDTO = new ResponseDTO(status: true)
        SpecialPE specialPE = SpecialPE.get(id)
        CRUDService.delete(specialPE)
        flash.message = message(code: "specialPe.delete.success")
        render(responseDTO as JSON)
    }
}