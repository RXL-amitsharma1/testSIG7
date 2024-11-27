package com.rxlogix

import grails.plugin.springsecurity.annotation.Secured

@Secured('Authenticated')
class TaskController {

    @Secured(['ROLE_TASK_VIEW'])
    def index() {

    }

    @Secured(['ROLE_TASK_VIEW'])
    def show() {

    }

    @Secured(['ROLE_TASK_CRUD'])
    def create() {

    }

    @Secured(['ROLE_TASK_CRUD'])
    def save()  {

    }

    @Secured(['ROLE_TASK_CRUD'])
    def edit() {

    }

    @Secured(['ROLE_TASK_CRUD'])
    def update() {

    }

    @Secured(['ROLE_TASK_CRUD'])
    def delete() {

    }


}
