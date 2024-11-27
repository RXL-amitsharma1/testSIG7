package com.rxlogix

import grails.plugin.springsecurity.annotation.Secured

@Secured(['ROLE_CONFIGURATION_CRUD'])
class WorkflowAdminController {
    def processManagerService

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def index() {}

    def create() {
        render(plugin: "grailsflow", view: "/")
    }

    def processType() {
        render (view: "processType")
    }

    def editTypes() {/*
        if (!params.sort) params.sort = "type"
        if (!params.order) params.order = "asc"
        def lang = request.locale.language.toString()
        def scripts = processManagerService.getSupportedProcessScripts()

        def processClasses = []
        if (scripts) {
            scripts.keySet().sort { a, b ->
                def labelA = scripts[a] ?
                        TranslationUtils.getTranslatedValue(scripts[a].label, scripts[a].processType, lang) : a
                def labelB = scripts[b] ?

                        TranslationUtils.getTranslatedValue(scripts[b].label, scripts[b].processType, lang) : b
                return (params.order == "asc") ? labelA.compareTo(labelB) : -labelA.compareTo(labelB)
            }
        }
        */
        render(view: "editTypes", model: [processClasses: processClasses, scripts: scripts], params: params)
    }
}
