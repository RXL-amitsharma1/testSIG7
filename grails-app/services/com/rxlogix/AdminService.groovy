package com.rxlogix

import com.rxlogix.cache.CacheService
import com.rxlogix.config.Disposition
import com.rxlogix.config.DispositionRule
import com.rxlogix.config.SafetyGroup
import com.rxlogix.enums.GroupType
import com.rxlogix.enums.TimeZoneEnum
import com.rxlogix.user.Group
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserGroupMapping
import com.rxlogix.user.UserGroupRole
import com.rxlogix.user.UserRole
import grails.gorm.transactions.Transactional
import org.apache.poi.xssf.usermodel.XSSFCell
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import grails.validation.ValidationException

@Transactional
class AdminService {

    UserService userService
    UserGroupService userGroupService
    CacheService cacheService
    DynamicReportService dynamicReportService
    def messageSource

    Map addUser(Map user, String timeZone, List rolesToBeAdded, List<Group> fetchAllGroups, List<SafetyGroup> fetchAllSafetyGroups, Set<Group> groupsToBeAdded) {
        def resultMap = [status: false, userAdded:null]
        List safetyGroupsToBeAdded = []
        try {
            if (user.GROUP_NAMES) {
                List groups = user.GROUP_NAMES.tokenize(',')
                groups.each { group ->
                    Group isGroup = fetchAllGroups.find { it -> it.name == group.trim() }
                    if (isGroup) {
                        groupsToBeAdded.add(isGroup)
                    }
                }
            }else{
                Group allUsers = Group.findByNameAndGroupType('All Users',GroupType.USER_GROUP)
                groupsToBeAdded?.add(allUsers)
            }
            if (user.SAFETY_GROUPS) {
                List safetyGroups = user.SAFETY_GROUPS.tokenize(',')
                safetyGroups.each { safety_group ->
                    SafetyGroup isGroup = fetchAllSafetyGroups.find { it -> it.name == safety_group }
                    if (isGroup) {
                        safetyGroupsToBeAdded.add(isGroup)
                    }
                }
            }
            String createdBy = userService.getUser().username
            String modifiedBy = userService.getUser().username
            Preference preference = new Preference(locale: user.LANGUAGE, timeZone: timeZone, dashboardConfig: null, createdBy: createdBy, modifiedBy: modifiedBy)
            User userToAdd = new User(username: user.USERNAME, fullName: user.FULL_NAME, email: user.EMAIL, enabled: user.ENABLED, accountExpired: user.ACCOUNT_EXPIRED,
                    accountLocked: user.ACCOUNT_LOCKED, preference: preference, groups: groupsToBeAdded,
                    createdBy: createdBy, modifiedBy: modifiedBy, safetyGroups: safetyGroupsToBeAdded).save(flush: true, failOnError: true)
            userService.saveUserInfoInPvUserWebappTable(userToAdd)
            userGroupService.createUserGroupMappingsForUser(userToAdd, userToAdd.groups.collect { it.id })
            rolesToBeAdded?.each { role ->
                new UserRole(user: userToAdd, role: role).save(flush: true, failOnError: true)
            }
            cacheService.prepareGroupAndSafetyGroupProductsCacheForUser(userToAdd)
            new UserDashboardCounts(userId: userToAdd.id).save(flush: true, failOnError: true)
            userService.updateUserGroupCountsInBackground(userToAdd, [])
            resultMap.status = true
            resultMap.userAdded = userToAdd
        } catch (ValidationException ve) {
            log.error(ve.getMessage())
            resultMap.status = false
        }
        return resultMap
    }

    List fetchUsers() {
        List<Map> users = []
        List roles = []
        List workflowGroup = []
        List userGroups = []
        String timeMessageCode
        String timeZoneArgs
        User currentUser = userService.getUser()
        User.list().each {
            roles = []
            workflowGroup = []
            userGroups = []
            UserRole.findAllByUser(it).each { userRole ->
                roles.add(Role.find(userRole.role))
            }
            List userGroupMappingList = UserGroupMapping.findAllByUser( it ).collect { it.group.id }
            userGroupMappingList?.each {
                roles = roles + UserGroupRole.findAllByUserGroup( Group.get( it ) )?.collect { it?.role }
            }
            userGroups = it.groups.findAll { group -> group.groupType.equals(GroupType.USER_GROUP) } as List
            workflowGroup = it.groups.findAll { group -> group.groupType.equals(GroupType.WORKFLOW_GROUP) } as List
            TimeZoneEnum.values().find { time ->
                if (time.timezoneId == it.preference.timeZone) {
                    timeMessageCode = time.getI18nKey()
                    timeZoneArgs = time.getGmtOffset()
                }
            }
            String setTimeZone = messageSource.getMessage(timeMessageCode, [timeZoneArgs] as Object[], currentUser.preference.locale)
            users.add(USERNAME: it.username, FULL_NAME: it.fullName, EMAIL: it.email, ENABLED: it.enabled,
                    ACCOUNT_LOCKED: it.accountLocked, ACCOUNT_EXPIRED: it.accountExpired, LANGUAGE: it.preference.locale,
                    TIMEZONE: setTimeZone, GROUP_NAMES: userGroups.join(','),
                    WORKFLOW_GROUP: workflowGroup.join(','),
                    SAFETY_GROUPS: it.safetyGroups.join(','),
                    ROLES: roles.join(',')
            )
        }
        return users
    }

    byte[] exportData(existingData, metadata) {
        XSSFWorkbook workbook = new XSSFWorkbook()
        XSSFSheet worksheet = workbook.createSheet("Data")
        XSSFRow row
        XSSFRow headerRow = worksheet.createRow((short) 0)
        XSSFCell cell
        metadata.eachWithIndex { it, i ->
            cell = headerRow.createCell((short) i)
            cell.setCellValue(it as String)
        }
        existingData.eachWithIndex { dataRow, j ->
            row = worksheet.createRow((short) 1 + j)
            dataRow.eachWithIndex { it, i ->
                cell = row.createCell((short) i)
                cell.setCellValue(dynamicReportService.sanitize(it.value as String))
            }
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream()
        workbook.write(out)
        out.toByteArray()
    }

    List fetchDispositions() {
        List<Map> dispositionList = []
        Disposition.list().each {
            dispositionList.add(VALUE: it.value, COLOR_CODE: it.colorCode, DISPLAY_NAME: it.displayName, DESCRIPTION: it.description, ABBREVIATION: it.abbreviation,
                    DISPLAY: it.display, VALIDATED_CONFIRMED: it.validatedConfirmed, CLOSED: it.closed, REVIEW_COMPLETED: it.reviewCompleted,
                    RESET_REVIEW_PROCESS: it.resetReviewProcess)
        }
        return dispositionList
    }

    Map addDisposition(Map disposition) {
        Map resultMap = [status: false]
        try {
            Disposition dispositionInstance = new Disposition(value: disposition.VALUE, colorCode: disposition.COLOR_CODE, displayName: disposition.DISPLAY_NAME,
                    description: disposition.DESCRIPTION, abbreviation: disposition.ABBREVIATION, display: disposition.DISPLAY, validatedConfirmed: disposition.VALIDATED_CONFIRMED,
                    closed: disposition.CLOSED, reviewCompleted: disposition.REVIEW_COMPLETED, resetReviewProcess: disposition.RESET_REVIEW_PROCESS).save(validate: true, flush: true)
            cacheService.updateDispositionCache(dispositionInstance)
            resultMap.status = true
        }
        catch (Throwable th) {
            log.error(th.getMessage())
        }
        resultMap
    }

    List fetchDispositionRules() {
        List<Map> dispositionRulesList = []
        DispositionRule.list().each {
            dispositionRulesList.add(NAME: it.name, DESCRIPTION: it.description, INCOMING_DISPOSITION: it.incomingDisposition,
                    TARGET_DISPOSITION: it.targetDisposition, APPROVAL_REQUIRED: it.approvalRequired, DISPLAY: it.display,
                    WORKFLOW_GROUPS: it.workflowGroups.join(","), ALLOWED_GROUPS: it.allowedUserGroups.join(","), NOTIFY: it.notify
            )
        }
        return dispositionRulesList
    }

    Map addDispositionRule(Map dispositionRule, List<Group> fetchAllUserGroups, List<Group> workflowGroups, List<Disposition> dispositionList) {
        Map resultMap = [status: false]
        List userGroups = []
        List workflowGroupsToAdd = []
        if (dispositionRule.WORKFLOW_GROUPS) {
            List groups = dispositionRule.WORKFLOW_GROUPS.tokenize(',')
            groups.each { group ->
                Group isGroup = workflowGroups.find { it -> it.name == group }
                if (isGroup) {
                    workflowGroupsToAdd.add(isGroup)
                }
            }
        }
        if (dispositionRule.ALLOWED_GROUPS) {
            List groups = dispositionRule.ALLOWED_GROUPS.tokenize(',')
            groups.each { group ->
                Group isGroup = fetchAllUserGroups.find { it -> it.name == group }
                if (isGroup) {
                    userGroups.add(isGroup)
                }
            }
        }
        try {
            new DispositionRule(name: dispositionRule.NAME, description: dispositionRule.DESCRIPTION,
                    incomingDisposition: dispositionList.find { it.displayName == dispositionRule.INCOMING_DISPOSITION },
                    targetDisposition: dispositionList.find { it.displayName == dispositionRule.TARGET_DISPOSITION },
                    approvalRequired: dispositionRule.APPROVAL_REQUIRED, display: dispositionRule.DISPLAY, workflowGroups: workflowGroupsToAdd,
                    allowedUserGroups: userGroups, notify: dispositionRule.NOTIFY).save(validate: true, flush: true)
            resultMap.status = true
        }
        catch (Throwable th) {
            log.error(th.getMessage())
        }
        resultMap
    }
}
