package com.rxlogix.config

import com.rxlogix.util.AlertUtil

import static com.rxlogix.util.DateUtil.toDateString

class SignalStrategy implements AlertUtil {
    static auditable = true

    String name
    String type
    String description
    String productSelection
    Date startDate

    //Standard fields
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy

    static constraints = {
        name(validator: {val, obj ->
            //Name is unique to user
            if (!obj.id || obj.isDirty("name")) {
                def existingStrategy = SignalStrategy.countByName(obj.name)
                if (existingStrategy > 0) {
                    return "com.rxlogix.config.signal.strategy.name.unique.per.user"
                }
            }
        }, blank : false)
        type nullable: true
        startDate nullable: true
        productSelection blank: false
        medicalConcepts(validator: {val, obj ->
            if (!obj.medicalConcepts) {
               return "com.rxlogix.config.SignalStrategy.medicalConcepts.nullable"
            }
        })
    }

    static mapping = {
        table name: "SIGNAL_STRATEGY"
        name column: "NAME"
        productSelection type: 'text', sqlType: 'clob'
        medicalConcepts joinTable: [name: "STRATEGY_MEDICAL_CONCEPTS", column: "MEDICAL_CONCEPTS_ID", key: "SIGNAL_STRATEGY_ID"]
    }

    static hasMany = [medicalConcepts : MedicalConcepts]

    def toDto(timeZone = 'UTC') {
        [
           id : this.id,
           name : this.name?.trim()?.replaceAll("\\s{2,}", " "),
           type : this.type,
           description : this.description,
           productSelection : getProductNameList(),
           startDate : toDateString(this.startDate, timeZone),
           medicalConcepts : medicalConcepts?.collect { it.name }
        ]
    }

    def getProductNameList() {
        String prdName = getNameFieldFromJson(this.productSelection)
        if(prdName) {
            prdName.toLowerCase().tokenize(',')
        } else {
            []
        }
        return prdName
    }
    @Override
    String toString() {
        "$name"
    }
}
