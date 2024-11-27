package com.rxlogix.dto
import groovy.transform.ToString
import org.apache.http.HttpStatus

@ToString
class ResponseDTO {
    Boolean status
    String message
    Integer code
    Integer value
    def data

    void populateResponseDto(String message, Integer code = 0, Boolean status = false) {
        this.status = status
        this.message = message
        this.code = code
    }

    void setErrorResponse(Exception e=null) {
        message = e.getMessage() ?:"Some error occurred while saving menu"
        status = false
        code=HttpStatus.SC_INTERNAL_SERVER_ERROR
    }

    static ResponseDTO getErrorResponseDTO(Exception e) {
        new ResponseDTO(status: false, message: e.getMessage())
    }

    void setErrorResponse(String message) {
        this.message = message
        status = false
    }

    void populateFlashObjectWithResponseDto(Map flash) {
        if (this.status) {
            flash.success = this.message
        } else {
            flash.error = this.message
        }
    }

    void populateDummyResponse(String message) {
        this.status = true
        this.message = message
    }
}
