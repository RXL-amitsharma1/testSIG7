package com.rxlogix.config

import com.rxlogix.dto.ResponseDTO
import grails.gorm.transactions.Transactional

/**
 * CaseNarrativeConfigurationService has methods for writing and reading Case Narrative configuration
 */

@Transactional
class CaseNarrativeConfigurationService {

    CaseNarrativeConfiguration caseNarrativeConfiguration

    CaseNarrativeConfigurationService() {
        caseNarrativeConfiguration = CaseNarrativeConfiguration.getInstance()
    }

    ResponseDTO setExportAlways(Boolean exportAlways) {
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        try {
            caseNarrativeConfiguration = CaseNarrativeConfiguration.first()
            caseNarrativeConfiguration.exportAlways = exportAlways
            caseNarrativeConfiguration.save(flush: true)
            responseDTO.data = exportAlways
        } catch (Exception ex) {
            caseNarrativeConfiguration.exportAlways = !exportAlways
            log.error(ex.printStackTrace())
            responseDTO.code = 500
            responseDTO.status = false
        }
        return responseDTO
    }


    ResponseDTO setPromptUser(Boolean promptUser) {
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        try {
            caseNarrativeConfiguration = CaseNarrativeConfiguration.first()
            caseNarrativeConfiguration.promptUser = promptUser
            caseNarrativeConfiguration.save(flush: true)
            responseDTO.data = promptUser
        } catch (Exception ex) {
            caseNarrativeConfiguration.promptUser = !promptUser
            log.error(ex.printStackTrace())
            responseDTO.code = 500
            responseDTO.status = false
        }
        return responseDTO
    }

    Boolean isExportAlwaysEnabled() {
        caseNarrativeConfiguration.exportAlways
    }

    Boolean isPromptUserEnabled() {
        caseNarrativeConfiguration.promptUser
    }
}
