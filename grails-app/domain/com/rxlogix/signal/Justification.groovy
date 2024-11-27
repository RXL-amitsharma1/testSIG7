package com.rxlogix.signal

import com.rxlogix.Constants
import com.rxlogix.config.Disposition
import com.rxlogix.enums.JustificationFeatureEnum
import com.rxlogix.util.MiscUtil
import grails.converters.JSON
import grails.gorm.dirty.checking.DirtyCheck
import grails.plugins.orm.auditable.AuditEntityIdentifier
import grails.plugins.orm.auditable.JSONAudit
import groovy.json.JsonSlurper
import groovy.transform.ToString

import static com.rxlogix.util.DateUtil.toDateString

@ToString(includes = ['name'])
@DirtyCheck
class Justification {
    static auditable = true
    def userService
    @AuditEntityIdentifier
    String name
    String justification
    @JSONAudit
    String feature
    Map<String, Object> attributesMap

    //Standard fields
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy
    List<Disposition> dispositions=[]

    static transients = ['attributesMap']

    static hasMany = [dispositions : Disposition]

    static constraints = {
        name nullable: false, blank: false, validator: { value, object ->
            return MiscUtil.validator(value, "Name", Constants.SpecialCharacters.DEFAULT_CHARS as String[])
        }
        justification nullable: false, blank: false, maxSize: 8000
        dateCreated nullable: true
        lastUpdated nullable: true
        createdBy nullable: true
        modifiedBy nullable: true
        feature maxSize: 4000
    }

    static mapping = {
        dispositions joinTable: [name: "DISPOSITION_JUSTIFICATIONS", key: "JUSTIFICTION_ID", column: "DISPOSITION_ID"]
        sort("justification")
    }

    Map toDto(timeZone) {
        [
                id               : this.id,
                name             : this.name?.trim().replaceAll("\\s{2,}", " "),
                justificationText: this.justification,
                features         : getFeatureList(this),
                lastUpdated      : toDateString(this.lastUpdated, timeZone),
                modifiedBy       : this.modifiedBy ? userService.getUserFromCacheByUsername(this.modifiedBy).fullName : '',
                dispositions     : this.dispositions*.id
        ]
    }

    def getFeatureList(justificationObj) {

        Map checkBoxList = ["alertWorkflow" : "Alert Workflow",
                            "topicWorkflow" : "Topic Workflow",
                            "signalWorkflow": "Signal Workflow",
                            "signalPriority": "Signal Priority",
                            "alertPriority" : "Alert Priority",
                            "topicPriority" : "Topic Priority",
                            "caseAddition"  : "Case Addition"]
        List featuresSelected = []

        checkBoxList.each { key, val ->
            def feature = justificationObj.getAttr(key) == "on" ? val : null
            if (feature) {
                featuresSelected.add(val)
            }
        }
        featuresSelected
    }

    def getAttr(attrName) {
        if (!attributesMap) {
            attributesMap = populateAttributesMap()
        }
        if (attributesMap) {
            attributesMap[attrName]
        } else {
            null
        }
    }

    def populateAttributesMap() {
        def jsonSlurper = new JsonSlurper()
        feature ? jsonSlurper.parseText(feature) ?: [:] : [:]
    }

    static List<Justification> fetchByAnyFeatureOn(List<JustificationFeatureEnum> featureInList, Boolean withAndCondition) {
        List<Justification> justificationList = createCriteria().list {
            "${withAndCondition ? 'and': 'or'}" {
                for (JustificationFeatureEnum feature in featureInList) {
                    like('feature', '%"' + feature.toString() + '": "on"%')
                }
            }

        }
        return justificationList.sort({
            it.justification.toUpperCase()})
    }


    def getEntityValueForDeletion(){
        return "Name-${name}, Justification-${justification}${featureString(feature)}"
    }

    def featureString(String feature){
        Map featureJson = JSON.parse(feature) as Map
        Map checkBoxList = ["alertWorkflow" : "Alert Workflow",
                            "topicWorkflow" : "Topic Workflow",
                            "signalWorkflow": "Signal Workflow",
                            "signalPriority": "Signal Priority",
                            "alertPriority" : "Alert Priority",
                            "topicPriority" : "Topic Priority",
                            "caseAddition"  : "Case Addition"]
        String featureString = ""
        featureJson.entrySet().each {
            featureString += ",${checkBoxList.get(it.getKey())}-${it.getValue()=='on'?"Yes":"No"}"
        }
        featureString
    }
}
