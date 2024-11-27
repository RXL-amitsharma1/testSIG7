package com.rxlogix

import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject

@Secured(["isAuthenticated()"])
class PvsStudyDictionaryController {

    def studyDictionaryService

    def searchStudies() {
        def studyList = studyDictionaryService.getStudyInstance(params.dictionaryLevel, null, params.contains, params.selectedDatasource)
        JSONArray showStudies = new JSONArray()
        studyList.each {
            JSONObject item = new JSONObject()
            item.level = params.dictionaryLevel
            item.id = it.id
            item.name = getNameFieldForStudy(it)
            showStudies.add(item)
        }
        respond showStudies, [formats: ['json']]
    }

    def getSelectedStudy() {
        def study = studyDictionaryService.getStudyInstance(params.dictionaryLevel, params.studyId, null, params.selectedDatasource)
        int level = Integer.parseInt(params.dictionaryLevel)
        def nextLevelItems = studyDictionaryService.getChildStudies(study, level, params.selectedDatasource)
        def studyNames = getNameFieldForStudy(study)
        def values = ['id': study?.id, 'name': studyNames, 'nextLevelItems': nextLevelItems]
        render values as JSON
    }

    def getPreLevelStudyParents() {
        int level = Integer.parseInt(params.dictionaryLevel)
        JSONArray parents = new JSONArray()
        params.studyIds.split(",").each {
            def study = studyDictionaryService.getStudyInstance(params.dictionaryLevel, it, null, params.selectedDatasource)
            studyDictionaryService.getParentStudies(study, level, parents)
        }
        respond parents, [formats: ['json']]
    }

    private static getNameFieldForStudy(def study) {
        if (study.hasProperty("studyNum")) {
            return study.studyNum
        } else if (study.hasProperty("description")) {
            return study.description
        } else if (study.hasProperty("center")) {
            return study.center
        }
    }


}
