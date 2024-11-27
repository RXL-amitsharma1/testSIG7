package com.rxlogix.domain

import grails.gorm.dirty.checking.DirtyCheck
import grails.plugins.orm.auditable.AuditEntityIdentifier

@DirtyCheck
trait ValueObject {
    Long id
    @AuditEntityIdentifier
    String value
    String displayName
    String displayName_local
    String description
    String description_local

    static constraints = {
        value nullable: false
        displayName nullable: false
        displayName_local nullable: true
        description_local nullable: true
    }

    @Override
    def boolean equals(object) {
        if (object) {
            this.value == object.value
        } else
            false
    }

    @Override
    String toString() {value}
}