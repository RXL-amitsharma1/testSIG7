package com.rxlogix

import com.rxlogix.util.DbUtil
import com.rxlogix.util.MiscUtil
import grails.gorm.dirty.checking.DirtyCheck
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.xpath.operations.Bool
import groovy.transform.EqualsAndHashCode

@DirtyCheck
@EqualsAndHashCode(includes = ['caseNumber', 'caseVersion'])
abstract class BaseSingleAlert {

    String name
    String caseNumber
    Integer caseVersion

    Date reviewDate
    Date detectedDate

    String productFamily
    String productName
    BigInteger productId
    String pt

    //followUp
    Integer followUpNumber = 0
    Boolean followUpExists

    String attributes
    Map<String, Object> attributesMap

    String createdBy
    Date dateCreated
    Date lastUpdated
    String modifiedBy

    Date periodStartDate
    Date periodEndDate
    String listedness
    String outcome
    Date caseInitReceiptDate
    String caseReportType
    String reportersHcpFlag
    String death
    Date lockedDate
    String rechallenge
    String age
    String gender
    String country
    String masterPrefTermAll
    String conComit
    String suspProd
    String serious
    String conMeds
    String caseNarrative
    String primSuspProd
    String primSuspPai
    String paiAll
    String allPt

    String caseType
    Double completenessScore
    String indNumber
    String appTypeAndNum
    String compoundingFlag
    String medErrorsPt
    Double patientAge
    String submitter
    String badge

    Boolean isNew
    Boolean adhocRun = false
    Long caseId
    Boolean flagged = false
    boolean isDuplicate


    static mapping = {
        attributes type: 'text', sqlType: 'clob'
        caseNarrative column: "CASE_NARRATIVE", sqlType: DbUtil.longStringType
        conMeds column: 'CON_MEDS', sqlType: DbUtil.longStringType
        conComit sqlType: DbUtil.longStringType
        suspProd column: 'SUSP_PROD', sqlType: DbUtil.longStringType
        medErrorsPt column: 'MED_ERRORS_PT', type: 'text', sqlType: 'clob'
        masterPrefTermAll sqlType: DbUtil.longStringType
        primSuspProd sqlType: DbUtil.longStringType
        primSuspPai sqlType: DbUtil.longStringType
        paiAll sqlType: DbUtil.longStringType
        allPt sqlType: DbUtil.longStringType
        pt sqlType: "varchar2(4000 CHAR)"
    }

    static constraints = {
        caseNumber nullable: true
        caseVersion nullable: true
        reviewDate nullable: true
        createdBy nullable: true
        dateCreated nullable: true
        lastUpdated nullable: true
        modifiedBy nullable: true
        attributes nullable: true
        followUpNumber nullable: true
        periodStartDate nullable: true
        periodEndDate nullable: true
        listedness nullable: true
        outcome nullable: true
        isNew nullable: false
        caseInitReceiptDate nullable: true
        caseReportType nullable: true
        reportersHcpFlag nullable: true
        death nullable: true
        lockedDate nullable: true
        rechallenge nullable: true
        gender nullable: true
        age nullable: true
        country nullable: true
        masterPrefTermAll nullable: true
        conComit nullable: true
        suspProd nullable: true
        serious nullable: true
        conMeds nullable: true
        caseNarrative nullable: true
        caseId nullable: true
        caseType nullable: true
        completenessScore nullable: true
        indNumber nullable: true
        appTypeAndNum nullable: true
        medErrorsPt nullable: true
        compoundingFlag nullable: true, maxSize: 4000
        patientAge nullable: true
        submitter nullable: true
        isDuplicate nullable :false
        badge nullable: true
        primSuspProd nullable: true
        primSuspPai nullable: true
        paiAll nullable: true
        allPt nullable: true
    }

    def singleCaseAlertService


    String beforeValidate() {
        if (attributesMap) {
            attributes = JsonOutput.toJson(attributesMap)
        }
    }

    def afterLoad() {
        attributesMap = populateAttributesMap()
    }

    def populateAttributesMap() {
        JsonSlurper jsonSlurper = new JsonSlurper()
        Map transformedMap = (attributes ? jsonSlurper.parseText(attributes) ?: [:] : [:]) as Map
        if (transformedMap) {
            singleCaseAlertService = MiscUtil.getBean("singleCaseAlertService")
            transformedMap = singleCaseAlertService.getTransformedMap(transformedMap as Map)
        }
        transformedMap
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

    String generateReportersHcpFlagValue() {
        try {
            if (this.reportersHcpFlag == null || this.reportersHcpFlag.trim().equalsIgnoreCase('null'))
                return '-'
        } catch (Exception e) {
            return '-'
        }
        return this.reportersHcpFlag
    }

    @Override
    String toString(){
        "$name"
    }

}
