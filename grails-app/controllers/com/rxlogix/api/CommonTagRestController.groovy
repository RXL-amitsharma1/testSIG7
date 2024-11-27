package com.rxlogix.api

import com.rxlogix.security.Authorize
import com.rxlogix.user.User
import com.rxlogix.util.AlertUtil
import com.rxlogix.util.RestApiResponse
import grails.converters.JSON

@Authorize
class CommonTagRestController implements AlertUtil {
    def commonTagService
    def cacheService
    def userService

    static allowedMethods = [getQuanAlertCategories: "GET", commonTagDetails: "GET", saveAlertCategories: "POST",saveBulkCategories:"POST"]

/**
 * Fetches the quantitative alert categories based on the provided domain, alert ID, and archival status.
 *
 * @param domain      The domain for which alert categories are to be fetched.
 * @param alertId     The ID of the alert for which the categories need to be retrieved.
 * @param isArchived  Boolean flag indicating whether to fetch archived categories.
 * @return            Renders the response map containing the alert categories in JSON format.
 *
 * This method:
 * 1. Logs the incoming JSON request content.
 * 2. Retrieves the current user context (placeholder logic currently using a static username).
 * 3. Fetches the quantitative alert categories using the `commonTagService` based on the input parameters.
 * 4. Populates the response with the fetched data or an error message if an exception occurs.
 */
    String getQuanAlertCategories(String domain, String alertId, Boolean isArchived) {
        Map responseMap = [:]

        // Validate parameters
        String validationErrors = validateParams(params)
        if (!validationErrors.isEmpty()) {
            RestApiResponse.invalidParametersResponse(responseMap, validationErrors)
            render(status: 400, contentType: 'application/json', text: (responseMap as JSON))
            return
        }
        try {
            User currentUser = userService.getUser()
            List data = []
            // Fetch the quantitative alert categories from the service
            if (!alertId.contains(',')) {
                data = commonTagService.getQuantitativeAlertCategories(domain, alertId as Long, isArchived, currentUser)
            } else {
                data = new ArrayList<>(commonTagService.fetchCommonCategories(["domain"    : domain,
                                                                               "alertId"   : alertId,
                                                                               "isArchived": isArchived as String]))
            }

            // Populate response map with successful data
            RestApiResponse.successResponseWithData(responseMap, null, data)
        } catch (Exception ex) {
            // Log the error and prepare an error response
            log.error("Error fetching quantitative alert categories", ex)
            RestApiResponse.failureResponse(responseMap)
            render(status: 500, contentType: 'application/json', text: (responseMap as JSON))
            return
        }
        // Render the response as JSON and send it to the client
        render(status: 200, contentType: 'application/json', text: (responseMap as JSON))
    }

    /**
     * Fetches the common tag details from the cache and returns them in a JSON response.
     *
     * @return  Renders the response map containing common tag details in JSON format.
     *
     * This method:
     * 1. Retrieves common tag data from the cache using the `cacheService`.
     * 2. Populates the response map with the fetched data if successful.
     * 3. Logs and handles any exceptions that occur during the data retrieval process.
     */
    String commonTagDetails() {
        // Initialize the response map to store the result
        Map responseMap = [:]
        try {
            // Retrieve common tag details from the cache service and cast to a list of maps
            List data = cacheService.getCommonTagCacheJson() as List<Map>

            // Populate response map with the fetched data
            RestApiResponse.successResponseWithData(responseMap, null, data)
        } catch (Exception ex) {
            // Log the error and prepare an error response
            log.error("Error fetching common tag details Json", ex)
            RestApiResponse.serverErrorResponse(responseMap)
            render (status: 500, contentType: 'application/json', text: (responseMap as JSON))
            return
        }
        // Render the response as JSON and send it to the client
        render(status: 200, contentType: 'application/json', text: (responseMap as JSON))
    }

    /**
     * Saves alert categories for a given alert based on the provided parameters.
     *
     * @param alertId       The ID of the alert for which the categories are being saved.
     * @param existingRows  A string containing the existing rows of alert categories.
     * @param isArchived    A boolean flag indicating whether the alert is archived.
     * @param newRows       A string containing the new rows to be added to the alert categories.
     * @param alertType     The type of alert (e.g., "Quantitative") that determines how the categories are saved.
     *
     * @return  Renders a JSON response with the result of saving the alert categories.
     *
     * This method:
     * 1. Checks the alert type to determine the appropriate service method for saving categories.
     * 2. If the alert type is "Quantitative", it calls the `saveQuanAlertCategories` method of the `commonTagService`.
     * 3. Populates the response map with the result of the save operation if successful.
     * 4. Logs and handles any exceptions that occur during the save process.
     */
    String saveAlertCategories() {
        Map responseMap = [:]
        Map params = request.JSON
        List<Map> existingRows = params.existingRows
        List<Map> newRows = params.newRows
        log.info("json content received is := ${params}")
        try {
            Map result = [:]
            User currentUser = userService.getUser()
            List alertIds = params.alertId
            if (alertIds.size() > 1) {
                result = commonTagService.saveCommonCategories(params, true)
            } else {
                result = commonTagService.saveQuanAlertCategories(alertIds[0], existingRows, newRows, params.isArchived, currentUser, true)
            }
            RestApiResponse.successResponseWithData(responseMap, null, result)
        } catch (Exception ex) {
            log.error("Error saving alert categories", ex)
            RestApiResponse.serverErrorResponse(responseMap)
            render(status: 500, contentType: 'application/json', text: (responseMap as JSON))
            return
        }

        render(status: 200, contentType: 'application/json', text: (responseMap as JSON))
    }

    private static String validateParams(def params) {
        StringBuilder errorMessages = new StringBuilder()

        // Validate domain
        if (!params.domain) {
            errorMessages.append("Domain is required.\n")
        }

        // Validate alertId
        if (!params.alertId || !params.alertId.split(',').every { it.isInteger() }) {
            errorMessages.append("Alert ID must be Valid.\n")
        }

        // Validate isArchived
        if (params.isArchived == null) { // Assuming isArchived is optional but should be present if provided
            errorMessages.append("isArchived must be specified.\n")
        }

        return errorMessages.toString().trim()
    }

}