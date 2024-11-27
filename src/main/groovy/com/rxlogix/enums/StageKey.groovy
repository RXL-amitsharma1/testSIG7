package com.rxlogix.enums

enum StageKey {

    LM_STAGE('etl.master.lmStage'),
    MEDDRA_STAGE('etl.master.meddraState'),
    WHO_STAGE('etl.master.whoState'),
    CASE_STAGE('etl.master.caseStage'),
    LM_TRANSFORM('etl.master.lmTransform'),
    MEDDRA_TRANSFORM('etl.master.meddraTransform'),
    WHO_TRANSFORM('etl.master.whoTransform'),
    CASE_VERSIONS_TRANSFORM('etl.master.caseVersionsTransform'),
    CASE_TRANSFORM('etl.master.caseTransform')

    String messageKey

    StageKey(String messageKey) {
        this.messageKey = messageKey
    }

}