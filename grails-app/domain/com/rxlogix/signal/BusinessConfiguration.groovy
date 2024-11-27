package com.rxlogix.signal

import com.rxlogix.Constants
import com.rxlogix.user.User
import com.rxlogix.util.AlertUtil
import com.rxlogix.util.DateUtil
import com.rxlogix.util.DbUtil
import com.rxlogix.util.MiscUtil
import grails.plugins.orm.auditable.AuditEntityIdentifier

class BusinessConfiguration implements GroovyInterceptable, AlertUtil, Serializable {

    static auditable = [ignore:['productDictionarySelection','dateCreated','ruleInformations','version', 'lastUpdated', 'lastUpdatedBy', 'dateCreated', 'createdBy','modifiedBy']]

    String ruleName
    String productSelection
    String dataSource
    String description
    Boolean enabled = false

    //Common db table fields.
    String createdBy
    Date dateCreated
    Date lastUpdated
    String modifiedBy
    String productDictionarySelection
    Boolean isGlobalRule = false
    String productGroupSelection
    Boolean isMultiIngredient


    static hasMany = [ruleInformations: RuleInformation]

    static mapping = {
        table("BUSINESS_CONFIGURATION")
        productSelection column: "PRODUCTS", sqlType: DbUtil.longStringType
        productGroupSelection sqlType: "varchar2(8000 CHAR)"
    }

    static constraints = {
        description nullable: true, maxSize: 8000, validator: { value, object ->
            return MiscUtil.validator(value, "Description", Constants.SpecialCharacters.TEXTAREA_CHARS as String[])
        }
        ruleName nullable: false,blank: false, unique: true, validator: { value, object ->
            return MiscUtil.validator(value, "Rule Name", Constants.SpecialCharacters.DEFAULT_CHARS as String[])
        }
        dataSource nullable: false, blank: false
        productDictionarySelection nullable: true
        productGroupSelection nullable: true
        productSelection nullable: true, validator: { value, obj ->
            def result = true
            if(!obj.isGlobalRule && !obj.productSelection && !obj.productGroupSelection){
                result = 'businessConfiguration.productSelection.nullable'
            }
            return result
        }
        isMultiIngredient nullable: true
    }

    def toDto(String timezone = "UTC") {

        [
                id          : this.id,
                ruleName    : this.ruleName?.trim()?.replaceAll("\\s{2,}", " "),
                ruleRank    : 1,
                lastModified: DateUtil.toDateStringWithTimeInAmPmFormat(this.lastUpdated, timezone),
                modifiedBy  : User.findByUsername(this.modifiedBy)?.fullName?.equalsIgnoreCase(Constants.Commons.SYSTEM)?Constants.Commons.SYSTEM:User.findByUsername(this.modifiedBy)?.fullName,
                products    : this.isGlobalRule? 'Global Rule': (this.productNameList ?: getGroupNameFieldFromJson(this.productGroupSelection)),
                enabled     : this.enabled,
                description : this.description ?: Constants.Commons.DASH_STRING,
                isGlobalRule: this.isGlobalRule,
                dataSource  : this.dataSource,
                ruleInformations: this.ruleInformations
        ]
    }

    def getProductNameList() {
        String prdName = getNameFieldFromJson(this.productSelection)
        if (prdName) {
            prdName.tokenize(',')
        } else {
            []
        }
    }

    def getProductIdList() {
        String prdName = getIdFieldFromJson(this.productSelection)
        if (prdName) {
            prdName.tokenize(',')
        } else {
            []
        }
    }

    List getAllProductIdsList() {
        getAllIdFieldFromJson(this.productSelection)
    }

    @Override
    String toString() {
        this.ruleName
    }

    def getInstanceIdentifierForAuditLog() {
        if(this.isGlobalRule){
            return "Global Rule(${getDataSource(dataSource)}): ${ruleName}"
        }else{
            return "Product Rule(${getDataSource(dataSource)}): ${ruleName}"
        }
    }

    def getEntityValueForDeletion(){
        return "Name-${ruleName}"
    }
}
