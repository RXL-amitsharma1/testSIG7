package com.rxlogix.dto

import grails.converters.JSON

class HealthDTO extends ResponseDTO {
    int httpCode = 200
    String stackTrace = ''

    void setFailureResponse(Exception ex, String message = null, int _httpCode = 500) {
        this.message = message ?: ex.message
        this.status = false
        StringWriter errors = new StringWriter();
        ex.printStackTrace(new PrintWriter(errors));
        this.stackTrace = errors.toString()
        this.httpCode = _httpCode
    }

    void setSuccessResponse(def data, String message = "") {
        this.message = message
        this.data = data
        this.status = true
    }

    def toAjaxResponse() {
        [status: httpCode, contentType: "application/json", encoding: "UTF-8", text: this as JSON]
    }
}