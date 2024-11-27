package com.rxlogix.signal

import com.rxlogix.util.DbUtil

class UserGroupFieldMapping {
    Long groupId
    String blindedFields
    String redactedFields
    String availableFields

    static constraints = {
        groupId nullable: false, unique: true
        blindedFields nullable: true
        redactedFields nullable: true
        availableFields nullable: true
    }

    static mapping = {
        blindedFields sqlType: DbUtil.longStringType
        redactedFields sqlType: DbUtil.longStringType
        availableFields sqlType: DbUtil.longStringType
    }
}
