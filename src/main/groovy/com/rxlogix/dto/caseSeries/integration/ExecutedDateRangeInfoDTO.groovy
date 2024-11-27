package com.rxlogix.dto.caseSeries.integration

import com.rxlogix.config.ExecutedAlertDateRangeInformation
import com.rxlogix.config.ExecutedDateRangeInformation
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.util.DateUtil
import grails.util.Holders

class ExecutedDateRangeInfoDTO {
    Integer relativeDateRangeValue = 1
    Date dateRangeStartAbsolute
    Date dateRangeEndAbsolute
    DateRangeEnum dateRangeEnum = DateRangeEnum.CUMULATIVE

    static final Date MIN_DATE = new Date().parse(DateUtil.DATETIME_FMT, "01-01-1900 00:00:01")

    ExecutedDateRangeInfoDTO(ExecutedDateRangeInformation exDateRangeInfo, ExecutedAlertDateRangeInformation exAlertDateRangeInfo,boolean isMissedCase,Date prevExecStartDate, Long alertCaseSeriesId) {
        if (exDateRangeInfo.dateRangeEnum == DateRangeEnum.CUMULATIVE) {
            dateRangeEnum = DateRangeEnum.CUMULATIVE
        } else if (exDateRangeInfo.dateRangeEnum == DateRangeEnum.PR_DATE_RANGE && exAlertDateRangeInfo.dateRangeEnum == DateRangeEnum.CUMULATIVE && alertCaseSeriesId) {
            dateRangeEnum = DateRangeEnum.CUSTOM
        } else {
            dateRangeEnum = exDateRangeInfo.dateRangeEnum == DateRangeEnum.PR_DATE_RANGE ? exAlertDateRangeInfo.dateRangeEnum : DateRangeEnum.CUSTOM
        }
        dateRangeStartAbsolute = getStartDate(exDateRangeInfo,exAlertDateRangeInfo,isMissedCase,prevExecStartDate)
        dateRangeEndAbsolute = (exAlertDateRangeInfo.dateRangeEnum != DateRangeEnum.CUMULATIVE) && (exDateRangeInfo.dateRangeEnum == DateRangeEnum.CUMULATIVE) ? exAlertDateRangeInfo.dateRangeEndAbsolute : exDateRangeInfo.dateRangeEndAbsolute
        relativeDateRangeValue = (exAlertDateRangeInfo.dateRangeEnum != DateRangeEnum.CUMULATIVE) && (exDateRangeInfo.dateRangeEnum == DateRangeEnum.CUMULATIVE) ? 1 : exAlertDateRangeInfo.relativeDateRangeValue
    }

    ExecutedDateRangeInfoDTO() {}

    ExecutedDateRangeInfoDTO(ExecutedAlertDateRangeInformation dateRangeInformation) {
        dateRangeEnum = DateRangeEnum.CUMULATIVE   //DOnt changes this to dateRangeEnum as this may impacet data of generated report refer PVS-65790 reverted after PO discussion
        dateRangeStartAbsolute = MIN_DATE
        dateRangeEndAbsolute = dateRangeInformation.dateRangeEndAbsolute
        relativeDateRangeValue = dateRangeInformation.relativeDateRangeValue
    }

    void setCummulativeDateRangeInfoDTO(ExecutedAlertDateRangeInformation dateRangeInformation) {
        dateRangeEnum = DateRangeEnum.CUMULATIVE
        dateRangeStartAbsolute = MIN_DATE
        dateRangeEndAbsolute = dateRangeInformation.dateRangeEndAbsolute
        relativeDateRangeValue = dateRangeInformation.relativeDateRangeValue
    }

    Date getStartDate(ExecutedDateRangeInformation exDateRangeInfo, ExecutedAlertDateRangeInformation exAlertDateRangeInfo, boolean isMissedCase,
                      Date prevExecStartDate) {

        if (exAlertDateRangeInfo.dateRangeEnum != DateRangeEnum.CUMULATIVE && exDateRangeInfo.dateRangeEnum == DateRangeEnum.CUMULATIVE) {
            return MIN_DATE
        } else if(isMissedCase){
            return MIN_DATE
        }
        return exDateRangeInfo.dateRangeStartAbsolute
    }

}