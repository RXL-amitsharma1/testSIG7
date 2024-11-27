package com.rxlogix

import com.rxlogix.mapping.LmCenters
import com.rxlogix.mapping.LmIngredient
import com.rxlogix.mapping.LmProtocols
import com.rxlogix.mapping.LmStudies
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject


class StudyDictionaryService {

    def getStudyInstance(String level, String studyId, String searchTerm, String selectedDatasource) {
        def showEvents
        switch (level) {
            case "1":
                if(studyId){
                    LmProtocols."$selectedDatasource".withTransaction{
                        showEvents = LmProtocols."$selectedDatasource".get(studyId)
                    }
                }else if(searchTerm){
                    LmProtocols."$selectedDatasource".withTransaction{
                        showEvents = LmProtocols."$selectedDatasource".findAllByDescriptionIlike('%' + searchTerm + '%')
                    }
                }
                break
            case "2":
                if(studyId){
                    LmStudies."$selectedDatasource".withTransaction{
                        showEvents = LmStudies."$selectedDatasource".get(studyId)
                    }
                }else if(searchTerm){
                    LmStudies."$selectedDatasource".withTransaction{
                        showEvents = LmStudies."$selectedDatasource".findAllByStudyNumIlike('%' + searchTerm + '%')
                    }
                }
                break
            case "3":
                if (studyId){
                    LmCenters."$selectedDatasource".withTransaction{
                        showEvents = LmCenters."$selectedDatasource".get(studyId)
                    }
                }else if(searchTerm){
                    LmCenters."$selectedDatasource".withTransaction{
                        showEvents = LmCenters."$selectedDatasource".findAllByCenterIlike('%' + searchTerm + '%')
                    }
                }
                break
        }
    }

    def getChildStudies(def item, int level, String selectedDatasource) {
        JSONArray children = new JSONArray()
        switch (level) {
            case 1:
                def relatedStudies = LmStudies."$selectedDatasource".findAllByProtocol(item)
                relatedStudies.each {
                    JSONObject child = new JSONObject(['id': it.id, 'name': it.studyNum, 'level': level + 1])
                    if (!children.contains(child)) {
                        children.add(child)
                    }
                }
                break
            case 2:
                item.centers.each {
                    JSONObject child = new JSONObject(['id': it.id, 'name': it.center, 'level': level + 1])
                    if (!children.contains(child)) {
                        children.add(child)
                    }
                }
                break
        }
        return children
    }

    private def getParentStudies(def item, int level, JSONArray parents) {
        switch (level) {
            case 2:
                JSONObject parent = new JSONObject(['id': item.protocol.id, 'name': item.protocol.description, 'level': level - 1])
                if (!parents.contains(parent)) {
                    parents.add(parent)
                }
                break
            case 3:
                def relatedStudies = LmStudies.where {
                    centers {
                        id == item.id
                    }
                }
                relatedStudies.each {
                    JSONObject parent = new JSONObject(['id': it.id, 'name': it.studyNum, 'level': level - 1])
                    if (!parents.contains(parent)) {
                        parents.add(parent)
                    }
                }
                break
        }
        return parents
    }
}
