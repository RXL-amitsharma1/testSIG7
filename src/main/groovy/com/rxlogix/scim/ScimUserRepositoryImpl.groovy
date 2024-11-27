package com.rxlogix.scim

import com.rxlogix.CRUDService
import com.rxlogix.SeedDataService
import com.rxlogix.user.User
import com.rxlogix.user.Group
import com.rxlogix.user.UserGroupMapping
import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional
import grails.plugins.scim.exceptions.InvalidRequestDataException
import grails.plugins.scim.exceptions.UnsupportedActionException
import grails.plugins.scim.resources.Email
import grails.plugins.scim.resources.operations.PatchRequest
import grails.plugins.scim.repositories.ScimResourceRepository
import grails.plugins.scim.exceptions.ResourceConflictException
import grails.plugins.scim.exceptions.ResourceNotFoundException
import grails.plugins.scim.messages.ListResponse
import grails.plugins.scim.resources.Meta
import grails.plugins.scim.resources.ScimGroup
import grails.plugins.scim.resources.ScimUser
import grails.util.Holders
import groovy.util.logging.Slf4j
import org.apache.commons.lang.StringUtils


@Slf4j
@Transactional
class ScimUserRepositoryImpl implements ScimResourceRepository<ScimUser> {

    def CRUDService

    boolean allowExistingUserMigrate = false

    private static final String USER_RESOURCE_TYPE = 'User'
    private static final String EMAIL_TYPE = 'work'
    private static final Integer MAX_RECORDS = 20

    private static final Map<String, String> propertiesMap = [userName   : 'username',
                                                              displayName: 'fullName',
                                                              timezone   : 'preference.timeZone',
                                                              locale     : 'preference.locale',
                                                              active     : 'enabled',
                                                              emails     : 'email'
    ]

    @ReadOnly
    @Override
    ScimUser get(String scimId, String excludedAttributes) {
        User user = scimId ? User.findByScimId(scimId) : null
        if (!user) {
            throw new ResourceNotFoundException("User Resource not found by id $scimId")
        }
        log.debug("User get for ${user.username}")
        return getFrom(user, excludedAttributes)
    }

    @Override
    synchronized ScimUser save(ScimUser scimUser) {
        if (!scimUser.userName) {
            throw new InvalidRequestDataException('Invalid incoming request as no user userName for create')
        }
        User existing = User.findByUsernameIlike(scimUser.userName)
        if (existing && (!allowExistingUserMigrate || existing.scimId)) {
            throw new ResourceConflictException("User resource already exist  with userName $scimUser.userName")
        }
        if (existing) {
            log.info("Upgrading existing user with Scim integration for : ${scimUser.userName}")
            existing.scimId = UUID.randomUUID().toString()
            existing.enabled = scimUser.active ?: false
            existing.modifiedBy = SeedDataService.USERNAME
            CRUDService.save(existing)
            existing.save(flush: true, failOnError: true)
            return getFrom(existing)
        }
        User user = null
        log.debug("User save request for ${scimUser.userName}")
        user = new User(scimId: UUID.randomUUID().toString(), createdBy: SeedDataService.USERNAME)
        user.preference.createdBy = SeedDataService.USERNAME
        copyUser(scimUser, user)
        scimUser.groups.each {
            Group userGroup = Group.findByScimId(it.id)
            if (userGroup)
                UserGroupMapping.create(userGroup, user, false)
        }
        CRUDService.save(user)
        user.save(flush: true, failOnError: true)
        return getFrom(user)
    }

    @Override
    ScimUser update(ScimUser scimUser) {
        User user = scimUser.id ? User.findByScimId(scimUser?.id) : null
        if (!user) {
            throw new ResourceNotFoundException("User Resource not found by id ${scimUser?.id}")
        }
        if (!scimUser.userName) {
            throw new InvalidRequestDataException('Invalid incoming request as no user userName for update')
        }
        user.lock()
        copyUser(scimUser, user)
        UserGroupMapping.removeAll(user)
        scimUser.groups.each {
            Group userGroup = Group.findByScimId(it.id)
            if (userGroup)
                UserGroupMapping.create(userGroup, user, false)
        }
        CRUDService.update(user)
        user.save(flush: true, failOnError: true)
        return getFrom(user)
    }

    @Override
    ScimUser patch(PatchRequest patch) {
        User user = patch.id ? User.findByScimId(patch.id) : null
        if (!user) {
            throw new ResourceNotFoundException("User Resource not found by id ${patch.id}")
        }
        user.lock()
        log.debug("Patch request for ${user.username}")
        patch.Operations.each {
            switch (it.op?.toLowerCase()) {
                case 'replace':
                    if (!it.path) {
                        it.value?.each { obj ->
                            updateProperties(user, obj.key, obj.value)
                        }
                    } else if (it.path.startsWith('emails') && it.path.endsWith('.value')) {
                        user['email'] = it.value
                    } else if (it.path.startsWith('groups') && it.path.endsWith('.id')) {
                        Group userGroup = Group.findByScimIdAndIsActive(it.value, true)
                        if (userGroup && !UserGroupMapping.exists(userGroup.id, user.id)) {
                            UserGroupMapping.create(userGroup, user)
                        }
                    } else if (it.path in propertiesMap.keySet()) {
                        updateProperties(user, it.path, it.value)
                    }
                    CRUDService.update(user)
                    break
                case 'add':
                    if (it.path.startsWith('emails') && it.path.endsWith('.value')) {
                        CRUDService.update(user)
                    } else {
                        it.value?.each { obj ->
                            Group userGroup = Group.findByScimIdAndIsActive(obj.value, true)
                            if (userGroup && !UserGroupMapping.exists(userGroup.id, user.id)) {
                                UserGroupMapping.create(userGroup, user)
                            }
                        }
                    }

                    break;
                case 'remove':
                    if (it.path.startsWith('emails')) {
                        user.email = null
                        CRUDService.update(user)
                    } else {
                        it.value?.each { obj ->
                            Group userGroup = Group.findByScimId(obj.value)
                            UserGroupMapping.remove(userGroup, user)
                        }
                    }
                    break;
            }
        }
        user.save(flush: true, failOnError: true)
        getFrom(user)
    }

    @Override
    void delete(String id) {
        throw new UnsupportedActionException("Delete for users is not supported due to Audit. Please use active: true/false using PATCH request for id: ${id}")
        /*
        User user = id ? User.findByScimId(id) : null
        if (!user) {
            throw new ResourceNotFoundException("User Resource not found by id $id")
        }
        user.lock()
        log.debug("Delete request for ${user.username}")
        user.enabled = false
        CRUDService.update(user)
        user.save(flush: true, failOnError: true)
         */
    }

    @ReadOnly
    @Override
    ListResponse findAll(String filter, Integer count, Integer startIndex, String excludedAttributes) {
        log.debug("User search for filter ${filter}")
        startIndex = startIndex ?: 1
        String userName = StringUtils.substringAfter(filter, 'userName eq')?.replaceAll('"', '')?.trim()
        List<User> userList = User.createCriteria().list(max: count ?: MAX_RECORDS, offset: (startIndex - 1)) {
            if (userName) {
                ilike('username', userName)
            }
            if (allowExistingUserMigrate) {
                isNotNull('scimId')
            }
            order("username", "asc")
        }
        ListResponse listResponse = new ListResponse(startIndex: startIndex, itemsPerPage: count ?: MAX_RECORDS, totalResults: userList.totalCount)
        listResponse.Resources = userList.collect { getFrom(it, excludedAttributes) }
        return listResponse
    }

    private void copyUser(ScimUser source, User destination) {
        destination.with {
            modifiedBy = SeedDataService.USERNAME
            username = source.userName
            fullName = source.displayName
            enabled = source.active
            preference.timeZone = source.timezone
            preference.modifiedBy = SeedDataService.USERNAME
            preference.locale = source.locale ? new Locale(source.locale) : Locale.ENGLISH
            email = source.emails ? (source.emails.first().value) : null //TODO need to add primary check
        }
    }

    private ScimUser getFrom(User user, String excludedAttributes = null) {
        ScimUser scimUser = new ScimUser(id: user.scimId, externalId: user.id, userName: user.username)
        List<UserGroupMapping> userGroups = UserGroupMapping.findAllByUser(user)
        scimUser.with {
            displayName = user.fullName
            timezone = user.preference.timeZone
            locale = user.preference.locale?.toString()
            active = user.enabled
            emails = user.email ? [new Email(value: user.email, type: EMAIL_TYPE, primary: true)] : []
            groups = userGroups*.group.findAll { !it.isActive }.collect {
                new ScimGroup(id: it.scimId, externalId: it.id, displayName: it.name, schemas: null)
            }
            meta = new Meta(created: user.dateCreated, lastModified: user.lastUpdated, resourceType: USER_RESOURCE_TYPE, location: scimUser.id)
        }
        if (excludedAttributes) {
            excludedAttributes.split(',').each {
                scimUser.setProperty(it, null) //Making it null so that when response render that attribute won't render
            }
        }
        return scimUser
    }

    private String getUserPropertyFromScim(String property) {
        propertiesMap.get(property)
    }

    private void updateProperties(User user, String path, def value) {
        List<String> paths = getUserPropertyFromScim(path).split('\\.')
        if (paths.size() > 1) {
            def object = user[paths.first()]
            if (paths.last() == 'locale') {
                object[paths.last()] = new Locale(value)
            } else {
                object[paths.last()] = value
            }

        } else {
            if(path == 'active'){
                user[getUserPropertyFromScim(path)] = Boolean.parseBoolean(value)
            } else {
                user[getUserPropertyFromScim(path)] = value
            }
        }
    }

}