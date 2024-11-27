package com.rxlogix.api

import com.rxlogix.config.Configuration
import com.rxlogix.user.User
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.rest.RestfulController

import static org.springframework.http.HttpStatus.NOT_FOUND

class OwnershipRestController extends RestfulController{

    def springSecurityService
    def userService
    def ownershipService;

    OwnershipRestController() {
        super()
    }

    @Secured(['ROLE_ADMIN'])
    def listReports(){
        if(params.id){
           def user =  User.get(params.id)
           render Configuration.createCriteria().list {
               projections {
                   property ('id')
                   property ('name')
                   property ('owner')

               }
               eq("owner", user)

           } as JSON
        }else{
            render status: NOT_FOUND
        }
    }

    @Secured(['ROLE_ADMIN'])
    def listUsers(){
        User.findAllByEnabled(true)
    }

    @Secured(['ROLE_ADMIN'])
    def changeOwners(){
        if(params.id) {
            def previous =  User.get(params.id)
            def current = User.get(params.owner)
            def results = [:]
            def resultMsg;
            try {
               results =  ownershipService.updateOwners(previous, current);
               resultMsg = message(code: 'default.ownershipchange.message', args: [results?.get("configuration"),
                   previous.fullName, current.fullName])
            } catch (Exception ex) {
                flash.message = message(code: 'default.ownershipchange.failed.message', args: [results?.get("configuration"),
                    results?.get("queries"), results?.get("templates"), previous.fullName, current.fullName])
                redirect(uri: "/user/show/" + params.id, model: [userInstance: previous])
                return
            }

            request.withFormat {
                form {
                    flash.message = resultMsg
                    redirect(uri: "/user/show/" + params.owner, model: [userInstance: current])
                }

            }

        }

    }


}
