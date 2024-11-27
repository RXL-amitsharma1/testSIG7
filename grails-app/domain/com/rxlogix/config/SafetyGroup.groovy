package com.rxlogix.config

import com.rxlogix.user.User
import grails.plugins.orm.auditable.AuditEntityIdentifier

class SafetyGroup implements Serializable {
    static auditable = true
    transient def auditLogService

    @AuditEntityIdentifier
    String name

    String allowedProd

    //Standard fields
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy

    List<String> allowedProductList = []

    static hasMany = [members: User]
    static belongsTo = [User]

    static constraints = {
        name unique: true, nullable: false
        allowedProd nullable: true
    }

    static mapping = {
        allowedProd type: 'text', sqlType: 'clob'
        sort "name"
    }

    static transients = ['allowedProductList', 'allowedLmProductList', 'allowedProductDisplayName']

    def beforeValidate() { syncProductsString() }

    def afterLoad() { syncProductList(this.allowedProd) }

    def setAllowedProductList(productList) {
        this.allowedProductList = productList
        syncProductsString()
    }

    def getAllowedLmProductList() {
        this.allowedProductList
    }

    def getAllowedProductDisplayName() {
        if (allowedProd) {
            if (allowedProd.size() > 50)
                "${allowedProd.take(50)} ..."
            else
                allowedProd
        } else {
            ''
        }
    }

    private def syncProductList(ids) {
        if (ids) {
            allowedProductList = ids.split('#%#').collect{it}
        } else {
            allowedProductList = []
        }
    }

    private def syncProductsString() {
        if (allowedProductList) {
            allowedProd = allowedProductList.sort().join('#%#')
        } else {
            allowedProd = null
        }
    }

    String toString() {
        "${name}"
    }

}
