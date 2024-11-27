package com.rxlogix.api

trait SanitizePaginationAttributes {

    // This method's test case is written in ReportFieldRestControllerSpec
    void sanitize(params) {
        params.length = params.int('length') ?: 50
        params.start = params.int('start') ?: 0
        params.sort = params.sort ? (params.sort != 'createdBy') ? params.sort : "owner.fullName" : 'dateCreated'
        params.direction = params.direction ?: 'desc'
        params.searchString = params.searchString?.trim()
    }

    void forSelectBox(params) {
        params.max = params.int('max') ?: 30
        params.page = params.int('page') ?: 1
        params.term = params.term?.trim() ?: ""
    }
}