package com.rxlogix.dto

import groovy.transform.ToString

@ToString
class FieldManagementResponseDTO {
    Integer resultCode
    String resultStatus
    String resultMsg
    Map result = [:]
}