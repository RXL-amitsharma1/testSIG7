package com.rxlogix.dto

class TriggeredReviewDTO {

    List<Long> reviewedList
    List<String> requiresReviewedList
    List<String> shareWithNames
    def domain
    Long userId
    List<Long> executedIdList
    List<String> allowedProducts
    Boolean isProductSecurity
}
