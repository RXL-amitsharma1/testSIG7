package com.rxlogix.enums

enum StageKeyEnum {

    PRE_STAGE('etl.master.preStage'),
    LM_STAGE('etl.master.lmStage'),
    MEDDRA_STAGE('etl.master.meddraStage'),
    WHO_STAGE('etl.master.whoStage'),
    CASE_STAGE('etl.master.caseStage'),
    E2B_STAGE('etl.master.e2bStage'),
    LM_TRANSFORM('etl.master.lmTransform'),
    MEDDRA_TRANSFORM('etl.master.meddraTransform'),
    WHO_TRANSFORM('etl.master.whoTransform'),
    CASE_VERSIONS_TRANSFORM('etl.master.caseVersionsTransform'),
    CASE_TRANSFORM('etl.master.caseTransform'),
    E2B_TRANSFORM('etl.master.e2bTransform'),
    POST_LOAD('etl.master.postLoad')

    String messageKey

    StageKeyEnum(String messageKey) {
        this.messageKey = messageKey
    }

}