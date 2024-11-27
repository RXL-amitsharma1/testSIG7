package com.rxlogix.api


import com.rxlogix.config.Tag
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.helper.AlertTagHelper
import grails.converters.JSON
import grails.rest.RestfulController

/**
 * TagController
 * A controller class handles incoming web requests and performs actions such as redirects, rendering views and so on.n
 */

class TagRestController extends RestfulController {

    def pvsAlertTagService

    TagRestController() {
        super(Tag);
    }

    def saveAlertCategories(AlertTagHelper listAlertTags) {
        ResponseDTO responseDTO = new ResponseDTO(status: true, code: 200)
        responseDTO = pvsAlertTagService.persistCategories(listAlertTags, responseDTO)
        render responseDTO as JSON
    }
}
