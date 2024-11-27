package com.rxlogix.signal

import com.rxlogix.Constants
import com.rxlogix.enums.AuditLogCategoryEnum
import com.rxlogix.enums.DrugClassificationTypeEnum
import com.rxlogix.util.MiscUtil
import grails.gorm.dirty.checking.DirtyCheck
import grails.plugins.orm.auditable.AuditEntityIdentifier
import grails.util.Holders
@DirtyCheck
class DrugClassification {
    def signalAuditLogService

    Long id
    String product
    String productIds
    String productNames
    String productDictionarySelection
    @AuditEntityIdentifier
    String className
    DrugClassificationTypeEnum classificationType
    String classification

    static transients = ['product']

    static mapping = {
        datasource Holders.getGrailsApplication().getConfig().signal.drugClassification
        version false
        table("PVS_DRUGS_SUBS_GROUP")
        id column: "ID", generator: "sequence", params:[sequence: 'drug_classification_seq']
        productDictionarySelection(column: "PRODUCT_DICTIONARY_SELECTION")
        productIds(column: "BASE_ID")
        productNames(column: "BASE_NAME")
        classification(column: "MAPPING_NAME")
        classificationType(column: "MAP_TYPE")
        className(column: "CLASS_NAME")
    }
    static constraints = {
        className blank: false, nullable: false, validator: { value, object ->
            return MiscUtil.validator(value, "Class Name", Constants.SpecialCharacters.DEFAULT_CHARS as String[])
        }
        classification blank: false, nullable: false, validator: { value, object ->
            return MiscUtil.validator(value, "Added Classifications", Constants.SpecialCharacters.TEXTAREA_CHARS as String[])
        }
    }

    def toDto() {
        [
            id                : this.id,
            product           : this.productNames,
            classificationType: this.classificationType.value,
            className         : this.className,
            classification    : this.classification
        ]
    }
    List detectChangesForAuditLog(theInstance, AuditLogCategoryEnum auditLogCategoryEnum) {
        List changesMade = signalAuditLogService.detectChangesMade(theInstance, auditLogCategoryEnum)
        changesMade.flatten()
    }

    def getEntityValueForDeletion() {
        List<DrugClassification> drugClassificationList = []
        String dataSource = Holders.config.signal.drugClassification
        DrugClassification."$dataSource".withNewSession {
            drugClassificationList = DrugClassification.findAllByClassName(this.className)
        }
        drugClassificationList.removeIf {
            it.classification == it.productNames
        }
        List classificationString = []
        classificationString = drugClassificationList?.collect {
            it.classification + "(" + DrugClassificationTypeEnum.valueOf(it.classificationType as String).value() + ")"
        }
        return "Product-${productNames},Class Name-${className},Classification-${classificationString.toString()}"
    }
}
