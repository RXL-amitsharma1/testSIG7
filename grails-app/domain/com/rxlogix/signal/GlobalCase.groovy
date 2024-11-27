package com.rxlogix.signal

class GlobalCase {
    Long caseId
    Integer versionNum

    static hasMany = [singleCaseAlert : SingleCaseAlert , pvsGlobalTag : PvsGlobalTag ]

    static mapping = {
        id column: '"globalCaseId"'
        pvsGlobalTag joinTable: [name: "SINGLE_GLOBAL_TAGS", column: "PVS_GLOBAL_TAG_ID", key: "GLOBAL_CASE_ID"]
        caseId index: 'IX_GLOBAL_CASE_ID_VERSION'
        versionNum index: 'IX_GLOBAL_CASE_ID_VERSION'
    }
}
