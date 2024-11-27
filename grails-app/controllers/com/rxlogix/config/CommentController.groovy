package com.rxlogix.config

import grails.plugin.springsecurity.annotation.Secured

@Secured(["isAuthenticated()"])
class CommentController {

    def index = { redirect(action: "list", params: params) }

    // the delete, save and update actions only accept POST requests
    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def list = {
        params.max = Math.min(params.max ? params.max.toInteger() : 10,  100)
        [commentInstanceList: Comment.list(params), commentInstanceTotal: Comment.count()]
    }

    def create = {
        def commentInstance = new Comment()
        commentInstance.properties = params
        return [commentInstance: commentInstance]
    }

    def save = {
        def commentInstance = new Comment(params)
        if (!commentInstance.hasErrors() && commentInstance.save()) {
            flash.message = "comment.created"
            flash.args = [commentInstance.id]
            flash.defaultMessage = "Comment ${commentInstance.id} created"
            redirect(action: "show", id: commentInstance.id)
        }
        else {
            render(view: "create", model: [commentInstance: commentInstance])
        }
    }

    def show = {
        def commentInstance = Comment.get(params.id)
        if (!commentInstance) {
            flash.message = "comment.not.found"
            flash.args = [params.id]
            flash.defaultMessage = "Comment not found with id ${params.id}"
            redirect(action: "list")
        }
        else {
            return [commentInstance: commentInstance]
        }
    }

    def edit = {
        def commentInstance = Comment.get(params.id)
        if (!commentInstance) {
            flash.message = "comment.not.found"
            flash.args = [params.id]
            flash.defaultMessage = "Comment not found with id ${params.id}"
            redirect(action: "list")
        }
        else {
            return [commentInstance: commentInstance]
        }
    }

    def update = {
        def commentInstance = Comment.get(params.id)
        if (commentInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (commentInstance.version > version) {
                    
                    commentInstance.errors.rejectValue("version", "comment.optimistic.locking.failure", "Another user has updated this Comment while you were editing")
                    render(view: "edit", model: [commentInstance: commentInstance])
                    return
                }
            }
            commentInstance.properties = params
            if (!commentInstance.hasErrors() && commentInstance.save()) {
                flash.message = "comment.updated"
                flash.args = [params.id]
                flash.defaultMessage = "Comment ${params.id} updated"
                redirect(action: "show", id: commentInstance.id)
            }
            else {
                render(view: "edit", model: [commentInstance: commentInstance])
            }
        }
        else {
            flash.message = "comment.not.found"
            flash.args = [params.id]
            flash.defaultMessage = "Comment not found with id ${params.id}"
            redirect(action: "edit", id: params.id)
        }
    }

    def delete = {
        def commentInstance = Comment.get(params.id)
        if (commentInstance) {
            try {
                commentInstance.delete()
                flash.message = "comment.deleted"
                flash.args = [params.id]
                flash.defaultMessage = "Comment ${params.id} deleted"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "comment.not.deleted"
                flash.args = [params.id]
                flash.defaultMessage = "Comment ${params.id} could not be deleted"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "comment.not.found"
            flash.args = [params.id]
            flash.defaultMessage = "Comment not found with id ${params.id}"
            redirect(action: "list")
        }
    }
}
