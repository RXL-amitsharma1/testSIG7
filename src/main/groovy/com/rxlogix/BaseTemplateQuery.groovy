package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.enums.QueryLevelEnum
import grails.gorm.dirty.checking.DirtyCheck

@DirtyCheck
abstract class BaseTemplateQuery {
    Long id

    String header
    String title
    String footer
    boolean headerProductSelection = false
    boolean headerDateRange = false
    boolean blindProtected = false // Used for CIOMS I Template.
    boolean privacyProtected = false // Used for CIOMS I Template.
    QueryLevelEnum queryLevel = QueryLevelEnum.CASE


    //Standard fields
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy

    static mapping = {
        header column: "HEADER"
        title column: "TITLE"
        footer column: "FOOTER"
        headerProductSelection column: "HEADER_PRODUCT_SELECTION"
        headerDateRange column: "HEADER_DATE_RANGE"
        privacyProtected column: "PRIVACY_PROTECTED"
        blindProtected column: "BLIND_PROTECTED"
        queryLevel column: "QUERY_LEVEL"

    }

    static constraints = {
        header(nullable:true, maxSize: 255)
        title(nullable:true, maxSize: 255)
        footer(nullable:true, maxSize: 1000)
    }

}
