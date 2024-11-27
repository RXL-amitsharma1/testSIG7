package com.rxlogix

import com.rxlogix.config.Tag
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.AlertTag
import com.rxlogix.signal.SingleCaseAlert
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured(["isAuthenticated()"])
class AlertTagController {

    def userService

    def index() {}

    def listTags(){
        def tagInfo = []
        def alertTagList = AlertTag.list()

        alertTagList.each{
            tagInfo << it.toDto()
        }
        respond tagInfo, [formats: ['json']]
    }

    def editTag(){
        def response
        try {
            AlertTag alertTag = AlertTag.findById(params.id)
            alertTag.name = params.name
            alertTag.save(flush: true)
            response = [success: true]
        }catch(Exception ex){
            response = [success: false, errorMessage: "Fill in the Alert Tag Name correctly"]
        }
        render(response as JSON)
    }

    def editSystemTag(){
        def response
        try {
            Tag systemTag = Tag.findById(params.id)
            systemTag.name = params.name
            systemTag.save(flush: true)
            response = [success: true]
        }catch(Exception ex){
            response = [success: false, errorMessage: "Fill in the Tag Name correctly"]
        }
        render(response as JSON)
    }

    def removeAlertTag(){
        def response
        try{
            AlertTag alertTag = AlertTag.findById(params.id)
            alertTag.delete(flush: true)
            response = [success: true]
        }catch(Exception ex){
            response = [success: false, errorMessage: "Tag is attached to Alerts and cannot be deleted"]
        }
        render(response as JSON)
    }

    def removeSystemTag(){
        def response
        try{
            Tag systemTag = Tag.findById(params.id)
            systemTag.delete(flush: true)
            response = [success: true]
        }catch(Exception ex){
            response = [success: false, errorMessage: "Tag is attached to Configuration and cannot be deleted"]
        }
        render(response as JSON)
    }

    def saveAlertTag(){
        def response
        try{
            AlertTag alertTag = new AlertTag(name: params.name, createdBy: userService.getUser(), dateCreated: new Date())
            alertTag.save(flush:true)
            response = [success: true]
        }catch(Exception ex){
            response = [success: false, errorMessage: "Fill in the Alert Tag Name correctly"]
        }
        render(response as JSON)
    }

    def saveSystemTag(){
        def response
        try{
            Tag systemTag = new Tag(name: params.name, createdBy: userService.getUser(), dateCreated: new Date())
            systemTag.save(flush:true)
            response = [success: true]
        }catch(Exception ex){
            response = [success: false, errorMessage: "Fill in the Tag Name correctly"]
        }
        render(response as JSON)
    }
}
