package com.rxlogix

import com.rxlogix.audit.AuditTrail
import com.rxlogix.config.Disposition
import com.rxlogix.config.DispositionRule
import com.rxlogix.config.PVSState
import com.rxlogix.config.SafetyGroup
import com.rxlogix.enums.GroupType
import com.rxlogix.enums.RolesEnum
import com.rxlogix.enums.TimeZoneEnum
import com.rxlogix.user.Group
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.util.ExcelDataImporter
import com.rxlogix.util.FileNameCleaner
import grails.plugin.springsecurity.annotation.Secured
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang3.EnumUtils
import org.springframework.core.io.Resource
import org.springframework.web.multipart.MultipartFile

@Secured(["isAuthenticated()"])
class AdminController {
    AdminService adminService
    def assetResourceLocator
    def signalAuditLogService

    def index() {}

    def addUsers() {
        MultipartFile file = request.getFile('excelFile')
        String fileExtension = FilenameUtils.getExtension(file.originalFilename)
            if(fileExtension) {
                if (!fileExtension.equalsIgnoreCase('xlsx')) {
                    flash.error = message(code: 'controlPanel.file.upload.format.not.supported')

                } else {
                    Map getUserDataFromFile = ExcelDataImporter.fetchDataFromFile(file)
                    if (!getUserDataFromFile.values) {
                        flash.error = message(code: 'file.empty')
                    }
                    List users = getUserDataFromFile.values
                    int totalUsers = users.size()
                    int userCount = 0
                    List fetchAllRoles = Role.list()
                    List fetchAllGroups = Group.findAllByGroupType(GroupType.USER_GROUP)
                    List fetchAllSafetyGroups = SafetyGroup.list()
                    List workflowGroups = Group.findAllByGroupType(GroupType.WORKFLOW_GROUP)
                    users.each { user ->
                        Set groupsToBeAdded = []
                        String fullName = user.FULL_NAME ? user.FULL_NAME.trim() : null
                        if (!(User.findByUsername(user.USERNAME) || (user.EMAIL ? User.findByEmail(user.EMAIL) : false)) && fullName) {
                            List<Role> rolesToBeAdded = []
                            String finalTimeZone
                            if (!user.TIMEZONE) {
                                finalTimeZone = "UTC"
                            } else {
                                TimeZoneEnum.values().each {
                                    String getTimeZone = message(code: it.getI18nKey(), args: [it.gmtOffset])
                                    if (getTimeZone.equals(user.TIMEZONE)) {
                                        finalTimeZone = it.timezoneId
                                    }
                                }
                            }
                            if (user.WORKFLOW_GROUP) {
                                Group workflowGroup = workflowGroups.find { it -> it.name == user.WORKFLOW_GROUP }
                                if (workflowGroup) {
                                    groupsToBeAdded.add(workflowGroup)
                                }
                            }
                            if (user.ROLES) {
                                List roles = user.ROLES.tokenize(',')
                                roles.each { role ->
                                    String roleName = role.toString().trim().replaceAll(" ", "_").replaceAll("-", "_")
                                    if (EnumUtils.isValidEnum(RolesEnum, roleName)) {
                                        String roleValue = RolesEnum.valueOf(roleName).value
                                        rolesToBeAdded.add(fetchAllRoles.find { it.authority == roleValue })
                                    }
                                }
                            }

                            if (!rolesToBeAdded.isEmpty() && !groupsToBeAdded.isEmpty()) {
                                Map userAdded = adminService.addUser(user as Map, finalTimeZone, rolesToBeAdded, fetchAllGroups, fetchAllSafetyGroups, groupsToBeAdded)
                                if (userAdded.status) {
                                    userCount++
                                }
                            }

                        }
                        flash.message = message(code: 'controlPanel.users.registration.success', args: [userCount, totalUsers])
                    }
                    signalAuditLogService.createAuditLog([
                            entityName : "Control Panel",
                            moduleName : "Control Panel",
                            category   : AuditTrail.Category.INSERT.toString(),
                            entityValue: "Add New Users",
                            description: "Uploaded Users"
                    ] as Map, [[propertyName: "Number of users added", oldValue: "", newValue: userCount]])
                }
            }
            else {
                flash.error= message(code: "controlPanel.file.upload.all.fields.required")
                render(view: "index")
            }
        redirect action: 'index'
    }

    def downloadUserTemplate() {
        Resource fileResource = assetResourceLocator.findAssetForURI("data/template/Users.xlsx")
        response.contentType = "xlsm; charset=UTF-8"
        response.contentLength = fileResource.contentLength()
        response.setCharacterEncoding("UTF-8")
        response.setHeader("Content-Disposition", "Attachment; filename=\"" + "${URLEncoder.encode(FileNameCleaner.cleanFileName("Users.xlsx"), "UTF-8")}" + "\"")
        response.getOutputStream().write(fileResource.byteArray)
        response.outputStream.flush()
    }


    def downloadExistingUsers() {
        List existingUsers = adminService.fetchUsers()
        def metadata = [
                'USERNAME', 'FULL_NAME', 'EMAIL', 'ENABLED',
                'ACCOUNT_LOCKED', 'ACCOUNT_EXPIRED', 'LANGUAGE',
                'TIMEZONE', 'GROUP_NAMES', 'WORKFLOW_GROUP',
                'SAFETY_GROUPS', 'ROLES'
        ]
        params.outputFormat = 'xlsx'
        String fileName = 'ExistingUsers'
        byte[] userFile = adminService.exportData(existingUsers, metadata)
        if (userFile) {
            response.contentType = "xlsm; charset=UTF-8"
            response.setCharacterEncoding("UTF-8")
            response.setHeader("Content-Disposition", "Attachment; filename=\"" + "${URLEncoder.encode(FileNameCleaner.cleanFileName(fileName), "UTF-8")}.$params.outputFormat" + "\"")
            response.getOutputStream().write(userFile)
            response.outputStream.flush()
            signalAuditLogService.createAuditForExport(null,"List Of Existing Users" , "Control Panel", params, "${URLEncoder.encode(FileNameCleaner.cleanFileName(fileName), "UTF-8")}.$params.outputFormat")
        }
    }

    def addDispositions() {
        MultipartFile file = request.getFile('excel_File')
        String fileExtension = FilenameUtils.getExtension(file.originalFilename)
        if (!fileExtension.equalsIgnoreCase('xlsx')) {
            flash.message = message(code: 'controlPanel.file.upload.format.not.supported')

        } else {
            Map fetchDispositionsFromFile = ExcelDataImporter.fetchDataFromFile(file)
            if (!fetchDispositionsFromFile.status) {
                flash.message = message(code: 'file.empty')
            }
            List dispositions = fetchDispositionsFromFile.values
            int totalDispositions = dispositions.size()
            int dispositionCount = 0
            dispositions.each { disposition ->
                if (!Disposition.findByDisplayName(disposition.DISPLAY_NAME) || !Disposition.findByValue(disposition.VALUE)) {
                    Map dispositionAdded = adminService.addDisposition(disposition as Map)
                    if (dispositionAdded.status) {
                        dispositionCount++
                    }
                }
            }
            flash.message = message(code: 'controlPanel.disposition.registration.success', args: [dispositionCount, totalDispositions])
        }
        redirect action: 'index'
    }

    def downloadDispositionTemplate() {
        File file = new File('data/controlPanelInfo/DispositionTemplate.xlsx')
        response.contentType = "xlsm; charset=UTF-8"
        response.contentLength = file.length()
        response.setCharacterEncoding("UTF-8")
        response.setHeader("Content-Disposition", "Attachment; filename=\"" + "${URLEncoder.encode(FileNameCleaner.cleanFileName(file.name), "UTF-8")}" + "\"")
        response.getOutputStream().write(file.bytes)
        response.outputStream.flush()
    }

    def downloadExistingDispositions() {
        List existingDispositions = adminService.fetchDispositions()
        def metadata = [
                'VALUE', 'COLOR_CODE', 'DISPLAY_NAME', 'DESCRIPTION', 'ABBREVIATION',
                'DISPLAY', 'VALIDATED_CONFIRMED', 'CLOSED', 'REVIEW_COMPLETED',
                'RESET_REVIEW_PROCESS'
        ]
        params.outputFormat = 'xlsx'
        String fileName = 'ExistingDispositions'
        byte[] DispositionFile = adminService.exportData(existingDispositions, metadata)
        if (DispositionFile) {
            response.contentType = "xlsm; charset=UTF-8"
            response.setCharacterEncoding("UTF-8")
            response.setHeader("Content-Disposition", "Attachment; filename=\"" + "${URLEncoder.encode(FileNameCleaner.cleanFileName(fileName), "UTF-8")}.$params.outputFormat" + "\"")
            response.getOutputStream().write(DispositionFile)
            response.outputStream.flush()
        }

    }

    def downloadExistingWorkFlowRules() {
        List existingDispositionRules = adminService.fetchDispositionRules()
        def metadata = ['NAME', 'DESCRIPTION', 'INCOMING_DISPOSITION',
                        'TARGET_DISPOSITION', 'APPROVAL_REQUIRED', 'DISPLAY',
                        'WORKFLOW_GROUPS', 'ALLOWED_GROUPS', 'NOTIFY'
        ]
        params.outputFormat = 'xlsx'
        String fileName = 'ExistingDispositionRules'
        byte[] DispositionRulesFile = adminService.exportData(existingDispositionRules, metadata)
        if (DispositionRulesFile) {
            response.contentType = "xlsm; charset=UTF-8"
            response.setCharacterEncoding("UTF-8")
            response.setHeader("Content-Disposition", "Attachment; filename=\"" + "${URLEncoder.encode(FileNameCleaner.cleanFileName(fileName), "UTF-8")}.$params.outputFormat" + "\"")
            response.getOutputStream().write(DispositionRulesFile)
            response.outputStream.flush()
        }
    }

    def addDispositionRules() {
        MultipartFile file = request.getFile('excel')
        String fileExtension = FilenameUtils.getExtension(file.originalFilename)
        if (!fileExtension.equalsIgnoreCase('xlsx')) {
            flash.message = message(code: 'controlPanel.file.upload.format.not.supported')

        } else {
            Map fetchDispositionsRules = ExcelDataImporter.fetchDataFromFile(file)
            List fetchAllUserGroups = Group.findAllByGroupType(GroupType.USER_GROUP)
            List workflowGroups = Group.findAllByGroupType(GroupType.WORKFLOW_GROUP)
            List dispositionList = Disposition.list()
            if (!fetchDispositionsRules.status) {
                flash.message = message(code: 'file.empty')
            }
            List dispositionRules = fetchDispositionsRules.values
            int totalDispositionRules = dispositionRules.size()
            int dispositionRuleCount = 0
            dispositionRules.each { dispositionRule ->
                if (!DispositionRule.findByName(dispositionRule.NAME)) {
                    Map dispositionRuleAdded = adminService.addDispositionRule(dispositionRule as Map, fetchAllUserGroups, workflowGroups, dispositionList)
                    if (dispositionRuleAdded.status) {
                        dispositionRuleCount++
                    }
                }
            }
            flash.message = message(code: 'controlPanel.disposition.workflow.rules.success', args: [dispositionRuleCount, totalDispositionRules])
        }
        redirect action: 'index'
    }

    def downloadDispositionWorkflowRulesTemplate() {
        File file = new File('data/controlPanelInfo/DispositionWorkflowRulesTemplate.xlsx')
        response.contentType = "xlsm; charset=UTF-8"
        response.contentLength = file.length()
        response.setCharacterEncoding("UTF-8")
        response.setHeader("Content-Disposition", "Attachment; filename=\"" + "${URLEncoder.encode(FileNameCleaner.cleanFileName(file.name), "UTF-8")}" + "\"")
        response.getOutputStream().write(file.bytes)
        response.outputStream.flush()
    }

}
