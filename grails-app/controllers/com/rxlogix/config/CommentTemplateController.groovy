package com.rxlogix.config

import com.rxlogix.CRUDService
import com.rxlogix.Constants
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.ArchivedAggregateCaseAlert
import com.rxlogix.util.MiscUtil
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import org.apache.http.util.TextUtils
import org.springframework.context.MessageSource
import grails.util.Holders
import org.springframework.util.StringUtils

import java.lang.reflect.Field
import java.util.regex.Matcher
import java.util.regex.Pattern

import java.util.regex.Matcher
import java.util.regex.Pattern

@Secured(["isAuthenticated()"])
class CommentTemplateController {

    CRUDService CRUDService
    def cacheService
    MessageSource messageSource
    def pvsAlertTagService
    def pvsGlobalTagService
    def alertFieldService
    def aggregateCaseAlertService
    def alertService
    def alertCommentService



    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def index() {
        Map labelConfig = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT', null, null, null).collectEntries {
            b -> [b.name, b.display]
        }

        List commentScoresList = aggregateCaseAlertService.fieldListAdvanceFilter('', 'AGGREGATE_CASE_ALERT').collect {
            [
                    name   : it.name,
                    enabled: it.enabled,
                    display: it.display
            ]
        }?.sort()

        commentScoresList.removeAll {
            (it.name.toString().contains("cum") || it.name.toString().contains("new") || it.enabled == false || it.name.toString() in ["disposition.id", "dispLastChange", "currentDisposition",
           "name", "assignedTo.id", "assignedToGroup.id", "currentRun", "flags", "subTags","justification","dispPerformedBy","comment","freqPeriod","reviewedFreqPeriod",
                    "freqPriority","freqPriorityFaers"
            ])
        }
        List commentCountList = alertFieldService.getCommentTemplateCount()?.sort()
        render(view: 'index', model: [commentScoresList: commentScoresList, commentCountList: commentCountList, labelConfig: labelConfig as JSON])
    }

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def list() {
        def commentTemplateList = CommentTemplate.list().collect { it.toDto() }.sort({ -it.id })
        respond commentTemplateList, [formats: ['json']]
    }

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def listOption() {
        def commentTemplateList = CommentTemplate.list().collect { it.toDto() }.sort({ it.name.toUpperCase()})
        render(commentTemplateList as JSON)
    }

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def save() {
        params.templateName = params.templateName?.trim()?.replaceAll("\\s{2,}", " ")
        ResponseDTO responseDTO = new ResponseDTO(status: true)
        try {
            if(params?.templateName?.toString()?.length()>255){
                responseDTO.status = false
                responseDTO.message = "The template name length is greater than 255 characters"
                render(responseDTO as JSON)
                return
            }
            if(TextUtils.isEmpty(params.templateName.trim())||TextUtils.isEmpty(params.comment.trim())){
                responseDTO.status = false
                responseDTO.message = "Please add all mandatory details"
                render(responseDTO as JSON)
                return
            }

            def commentTemplateInstance = CommentTemplate.findByName(params.templateName.trim())
            if(commentTemplateInstance){
                responseDTO.status = false
                responseDTO.message = "The template name is already in use, please use a different name"
                render(responseDTO as JSON)
                return
            }

            CommentTemplate commentTemplate = new CommentTemplate()
            commentTemplate.name = params.templateName.trim()
            commentTemplate.comments = params.comment?.length() == 8000 ? params.comment + " " : params.comment
            CRUDService.save(commentTemplate)
            responseDTO.message = "Comment Template saved successfully."

        } catch (grails.validation.ValidationException vx) {
            responseDTO.message = MiscUtil.getCustomErrorMessage(vx)
            responseDTO.status = false
            log.error("Exception is : ${vx}")
        } catch(Exception exp){
            responseDTO.status = false
            responseDTO.message = "Please fill all the required fields."
            log.error("${exp} Exception while saving Comment Template")
        }
        render(responseDTO as JSON);
    }

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def delete(Long id) {
        ResponseDTO responseDTO = new ResponseDTO(status: true)
        CommentTemplate commentTemplate = CommentTemplate.get(id)
        try {
            CRUDService.delete(commentTemplate)
            responseDTO.message = "Comment Template ${commentTemplate.name} deleted successfully"
        }
        catch (Exception e) {
            responseDTO.message = "This template can not be deleted as it is being used in some safety observations/signals"
            responseDTO.status = false
        }
        render(responseDTO as JSON)
    }

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def edit(Long id) {
        ResponseDTO responseDTO = new ResponseDTO(status: true)
        CommentTemplate commentTemplate = CommentTemplate.read(id)
        responseDTO.data = commentTemplate.toDto()
        render(responseDTO as JSON)
    }

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def update(Long id) {
        params.templateName = params.templateName?.trim()?.replaceAll("\\s{2,}", " ")
        ResponseDTO responseDTO = new ResponseDTO(status: true)
        try {
            if(TextUtils.isEmpty(params.templateName)||TextUtils.isEmpty(params.comment)){
                responseDTO.status = false
                responseDTO.message = "Please fill the required fields."
                render(responseDTO as JSON)
                return
            }

            CommentTemplate commentTemplate = CommentTemplate.get(id)
            def commentTemplateInstance = CommentTemplate.findByName(params.templateName.trim())
            if(commentTemplateInstance && params.templateName!= commentTemplate.name){
                responseDTO.status = false
                responseDTO.message = "The template name is already in use, please use a different name"
                render(responseDTO as JSON)
                return
            }

            commentTemplate.name = params.templateName.trim()
            commentTemplate.comments = params.comment?.length() == 8000 ? params.comment + " " : params.comment
            CRUDService.update(commentTemplate)
            responseDTO.message = "Comment Template updated successfully."
        } catch (grails.validation.ValidationException vx) {
            responseDTO.message = MiscUtil.getCustomErrorMessage(vx)
            responseDTO.status = false
            log.error("Exception is : ${vx}")
        } catch(Exception exp){
            responseDTO.status = false
            responseDTO.message = "Please fill all the required fields."
            log.error("${exp} Exception while saving Comment Template")
        }
        render(responseDTO as JSON);
    }

    def createCommentFromTemplate(){
        ResponseDTO responseDTO = new ResponseDTO(status: true)
        String comment  = alertCommentService.createCommentFromTemplate(params.templateId as Long, params.acaId as Long)
        responseDTO.data = comment
        render(responseDTO as JSON);
    }
}
