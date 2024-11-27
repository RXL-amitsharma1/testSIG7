package com.rxlogix.dto

import com.rxlogix.enums.DateRangeEnum

class ExecutedGlobalDateRangeInformation {
    Date executedAsOfVersionDate
    int relativeDateRangeValue = 1
    Date dateRangeStartAbsolute
    Date dateRangeEndAbsolute
    DateRangeEnum dateRangeEnum = DateRangeEnum.CUMULATIVE
}
