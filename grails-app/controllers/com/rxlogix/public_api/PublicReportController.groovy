package com.rxlogix.public_api

import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ExecutionStatus
import com.rxlogix.config.ReportExecutionStatus
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.signal.SignalReport
import com.rxlogix.signal.SpotfireNotificationQuery
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured('permitAll')
class PublicReportController {
    def messageSource
    def reportIntegrationService
    def reportTemplateRestService
    def spotfireService
    def CRUDService

    def reportsCallback(Long id, String executionStatus, Long executedConfigurationId) {
        log.info("reportsCallback called : Response from PVR : ${params}")
        ResponseDTO responseDTO = new ResponseDTO()
        try {
            ExecutedConfiguration executedConfiguration = ExecutedConfiguration.get(id)
            if (executedConfiguration) {
                ExecutionStatus.withTransaction {
                    ExecutionStatus alertExecutionStatus = ExecutionStatus.findByExecutedConfigId(executedConfiguration.id)
                    executedConfiguration.reportExecutionStatus = ReportExecutionStatus.getReportExecutionStatus(executionStatus)
                    if (executedConfiguration.reportExecutionStatus && executedConfiguration.reportExecutionStatus == ReportExecutionStatus.COMPLETED) {
                        if (!executedConfiguration.reportId) {
                            executedConfiguration.reportId = executedConfigurationId
                        }
                        reportIntegrationService.saveConfigurationAndReportNotificationAndActivity(executedConfiguration)
                        alertExecutionStatus?.reportExecutionStatus = ReportExecutionStatus.COMPLETED
                        responseDTO.status = true
                    } else {
                        alertExecutionStatus?.reportExecutionStatus = ReportExecutionStatus.ERROR
                        reportIntegrationService.saveConfigurationAndReportNotificationAndActivity(executedConfiguration)
                        responseDTO.setErrorResponse('Invalid Report Execution Status.')
                        responseDTO.status = true
                    }
                    if (alertExecutionStatus) {
                        CRUDService.saveWithoutAuditLog(alertExecutionStatus)
                    }
                }
            } else {
                responseDTO.setErrorResponse('Invalid executed Alert id.')
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e)
            responseDTO.setErrorResponse('Some error occurred while executing reportsCallback')
        }
        render(responseDTO as JSON)
    }

    def adhocReportsCallback(Long id, String executionStatus, Long executedConfigurationId) {
        log.info("adhocReportsCallback called : Response from PVR : ${params}")
        ResponseDTO responseDTO = new ResponseDTO()
        try {
            SignalReport signalReport = SignalReport.get(id)
            if (signalReport) {
                if (ReportExecutionStatus.getReportExecutionStatus(executionStatus)) {
                    if (!signalReport.reportId) {
                        signalReport.reportId = executedConfigurationId
                    }
                    if (ReportExecutionStatus.getReportExecutionStatus(executionStatus) == ReportExecutionStatus.COMPLETED) {
                        reportTemplateRestService.saveExportedFiles(executedConfigurationId, signalReport)
                        reportIntegrationService.createAuditForReportGenerate(signalReport, "(Executed)")
                    }else{
                        reportTemplateRestService.saveSignalReportNotification(signalReport)
                        reportIntegrationService.createAuditForReportGenerate(signalReport, "(Failed)")
                    }
                    responseDTO.status = true
                } else {
                    responseDTO.setErrorResponse('Invalid Report Execution Status.')
                }
            } else {
                responseDTO.setErrorResponse('Invalid SignalReport id.')
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e)
            responseDTO.setErrorResponse('Some error occurred while executing adhocReportsCallback')
        }
        render(responseDTO as JSON)
    }

}