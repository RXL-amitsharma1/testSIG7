package com.rxlogix.util

import com.rxlogix.Constants

class RestApiResponse {
    static void successResponse(def responseData, def successMessage = null, def resultCode = null) {
        responseData.put(Constants.RESULT_CODE, resultCode ?: Constants.TXN_SUCCESS[Constants.RESULT_CODE])
        responseData.put(Constants.RESULT_STATUS, Constants.TXN_SUCCESS[Constants.RESULT_STATUS])
        responseData.put(Constants.RESULT_MESSAGE, successMessage ?: Constants.TXN_SUCCESS[Constants.RESULT_MESSAGE])
    }

    static Map<String, Object> successResponse() {
        Map<String, Object> responseData = [:]
        successResponse(responseData)
        responseData
    }

    static void successResponseWithData(def responseData, def successMessage, def data) {
        responseData.put(Constants.RESULT_CODE, Constants.TXN_SUCCESS[Constants.RESULT_CODE])
        responseData.put(Constants.RESULT_STATUS, Constants.TXN_SUCCESS[Constants.RESULT_STATUS])
        responseData.put(Constants.RESULT_MESSAGE, successMessage ?: Constants.TXN_SUCCESS[Constants.RESULT_MESSAGE])
        responseData.put(Constants.RESULT_DATA, data)
    }

    static Map<String, Object> successResponseWithData(Object data, String message = null) {
        Map<String, Object> responseData = [:]
        successResponseWithData(responseData, message, data)
        responseData
    }

    static void redirectResponse(def responseData, def message = null) {
        responseData.put(Constants.RESULT_CODE, Constants.REDIRECT[Constants.RESULT_CODE])
        responseData.put(Constants.RESULT_STATUS, Constants.REDIRECT[Constants.RESULT_STATUS])
        responseData.put(Constants.RESULT_MESSAGE, message ?: Constants.REDIRECT[Constants.RESULT_MESSAGE])
    }

    static void recordAbsentResponse(def responseData, def message = null) {
        responseData.put(Constants.RESULT_CODE, Constants.NO_RECORD_FOUND[Constants.RESULT_CODE])
        responseData.put(Constants.RESULT_STATUS, Constants.NO_RECORD_FOUND[Constants.RESULT_STATUS])
        responseData.put(Constants.RESULT_MESSAGE, message ?: Constants.NO_RECORD_FOUND[Constants.RESULT_MESSAGE])
    }

    static void failureResponse(def responseData, def errorMessage = null) {
        responseData.put(Constants.RESULT_CODE, Constants.TXN_FAILURE[Constants.RESULT_CODE])
        responseData.put(Constants.RESULT_STATUS, Constants.TXN_FAILURE[Constants.RESULT_STATUS])
        responseData.put(Constants.RESULT_MESSAGE, errorMessage?: Constants.TXN_FAILURE[Constants.RESULT_MESSAGE])
    }

    static void serverErrorResponse(def responseData, def errorMessage = null) {
        responseData.put(Constants.RESULT_CODE, Constants.INTERNAL_SERVER_ERROR[Constants.RESULT_CODE])
        responseData.put(Constants.RESULT_STATUS, Constants.INTERNAL_SERVER_ERROR[Constants.RESULT_STATUS])
        responseData.put(Constants.RESULT_MESSAGE, errorMessage?: Constants.INTERNAL_SERVER_ERROR[Constants.RESULT_MESSAGE])
    }

    static void accessDeniedResponse(def responseData, def errorMessage = null) {
        responseData.put(Constants.RESULT_CODE, Constants.ACCESS_DENIED[Constants.RESULT_CODE])
        responseData.put(Constants.RESULT_STATUS, Constants.ACCESS_DENIED[Constants.RESULT_STATUS])
        responseData.put(Constants.RESULT_MESSAGE, errorMessage ?: Constants.ACCESS_DENIED[Constants.RESULT_MESSAGE])
    }

    static void invalidParametersResponse(def responseData, def errorMessage = null) {
        responseData.put(Constants.RESULT_CODE, Constants.INVALID_PARAMETERS[Constants.RESULT_CODE])
        responseData.put(Constants.RESULT_STATUS, Constants.INVALID_PARAMETERS[Constants.RESULT_STATUS])
        responseData.put(Constants.RESULT_MESSAGE, errorMessage ?: Constants.INVALID_PARAMETERS[Constants.RESULT_MESSAGE])
    }

    static void forbiddenResponse(def responseData, def errorMessage = null) {
        responseData.put(Constants.RESULT_CODE, Constants.FORBIDDEN[Constants.RESULT_CODE])
        responseData.put(Constants.RESULT_STATUS, Constants.FORBIDDEN[Constants.RESULT_STATUS])
        responseData.put(Constants.RESULT_MESSAGE, errorMessage ?: Constants.FORBIDDEN[Constants.RESULT_MESSAGE])
        responseData.put(Constants.RESULT_TITLE, Constants.FORBIDDEN[Constants.RESULT_TITLE])
    }
}
