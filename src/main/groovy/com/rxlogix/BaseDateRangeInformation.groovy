package com.rxlogix

import com.rxlogix.enums.DateRangeEnum
import grails.gorm.dirty.checking.DirtyCheck

@DirtyCheck
abstract class BaseDateRangeInformation {
    Long id
    int relativeDateRangeValue = 1
    Date dateRangeStartAbsolute
    Date dateRangeEndAbsolute
    DateRangeEnum dateRangeEnum = DateRangeEnum.CUMULATIVE

    static mapping = {
        dateRangeStartAbsolute column: "DATE_RNG_START_ABSOLUTE"
        dateRangeEndAbsolute column: "DATE_RNG_END_ABSOLUTE"
        dateRangeEnum column: "DATE_RNG_ENUM"
        relativeDateRangeValue column: "RELATIVE_DATE_RNG_VALUE"
    }

    static constraints = {

        dateRangeEnum(nullable:false, validator: { val, obj ->
            if (obj.dateRangeEnum == DateRangeEnum.CUSTOM) {
               if(obj.dateRangeStartAbsolute == null) {
                   return "com.rxlogix.config.enddate.required"
               }
               if(obj.dateRangeEndAbsolute == null) {
                   return "com.rxlogix.config.startdate.required"
               }
            }
        })

        dateRangeStartAbsolute(nullable:true,blank:true)
        dateRangeEndAbsolute(nullable:true,blank:true)
        relativeDateRangeValue(min:1)
    }

}
