package com.rxlogix

import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject

@Secured(["isAuthenticated()"])
class PvsEventDictionaryController {

    def pvsEventDictionaryService

    def searchEvents() {
        def eventList = pvsEventDictionaryService.getEventInstance(params.dictionaryLevel, null, params.contains, params.selectedDatasource)
        JSONArray showEvents = new JSONArray()
        eventList.each {
            JSONObject event = new JSONObject()
            event.level = params.dictionaryLevel
            event.id = it.id
            event.name = it.name
            showEvents.add(event)
        }
        respond showEvents, [formats: ['json']]
    }

    // For Event Dictionary.
    def getSelectedEvent() {
        def event = pvsEventDictionaryService.getEventInstance(params.dictionaryLevel, params.eventId, null, params.selectedDatasource)
        int level = Integer.parseInt(params.dictionaryLevel)
        def rst =[id: event.id, name: event.name, nextLevelItems: getChildEvents(event, level)]
        render(rst as JSON)
    }

    def getPreLevelEventParents() {
        int level = Integer.parseInt(params.dictionaryLevel)
        JSONArray parents = new JSONArray()
        params.eventIds.split(",").each {
            def event = pvsEventDictionaryService.getEventInstance(params.dictionaryLevel, it, null, params.selectedDatasource)
            getParentEvents(event, level, parents)
        }
        parents=parents.sort()
        respond parents, [formats: ['json']]
    }

    def getChildEvents(def event, int level) {
        JSONArray children = new JSONArray()
        if (event.hasProperty("childEvents")) {
            event.childEvents.each {
                children.add(new JSONObject(['id': it.id, 'name': it.name, 'level': level + 1]))
            }
        }
        children = children.sort()
        return children
    }

    private def getParentEvents(def event, int level, JSONArray parents) {
        if (event.hasProperty("parentEvents")) {
            event.parentEvents.each {
                JSONObject parent = new JSONObject(['id': it.id, 'name': it.name, 'level': level - 1])
                if (!parents.contains(parent)) {
                    parents.add(parent)
                }
            }
        }
        parents =parents.sort()
        return parents
    }
}
