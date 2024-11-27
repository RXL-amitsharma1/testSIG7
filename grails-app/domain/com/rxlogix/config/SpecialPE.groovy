package com.rxlogix.config

import com.rxlogix.user.User
import com.rxlogix.util.AlertUtil

class SpecialPE implements GroovyInterceptable, AlertUtil {
    static auditable = true

    def auditLogService
    String specialEvents
    String specialProducts

    //Standard fields
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy

    static constraints = {
        specialEvents nullable: true
        specialProducts nullable: true
    }

    static mapping = {
        table ("specialpe")
        specialProducts type: 'text', sqlType: 'clob'
        specialEvents type: 'text', sqlType: 'clob'
    }

    def toDto() {
        [
                id                : this.id,
                specialEvents     : this.eventNameList ,
                specialProducts   : this.productNameList,
                lastUpdated       : this.lastUpdated,
                modifiedBy        : User.findByUsername(this.modifiedBy)?.fullName
        ]
    }

    def getProductNameList() {
        String prdName = getNameFieldFromJson(this.specialProducts)
        if (prdName) {
            prdName.tokenize(',')
        } else {
            []
        }
    }
    def getEventNameList() {
        String prdName = getNameFieldFromJson(this.specialEvents)
        if (prdName) {
            prdName.tokenize(',')
        } else {
            []
        }
    }



}
